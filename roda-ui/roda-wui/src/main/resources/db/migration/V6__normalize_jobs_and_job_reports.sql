--
-- V6: normalize jobs and job_reports (3NF)
--
-- Replaces the JSON-in-TEXT columns of jobs/job_reports with relational tables:
--
--   plugins                         plugin identity: (class, version) -> name
--   jobs                            job attributes (source selection kind/class/filter inline)
--   job_stats                       1:1, the frequently updated job counters, kept narrow
--   job_plugin_parameters           was jobs.plugin_parameters (JSON map)
--   job_source_objects              was jobs.source_objects ids (LIST selections)
--   job_users                       was jobs.job_users_details (JSON list)
--   job_attachments                 was jobs.attachments_list (JSON list)
--   job_reports                     one row per (job, source object, outcome object)
--   job_report_source_original_ids  was job_reports.source_object_original_ids (JSON list)
--   job_report_steps                was job_reports.reports (JSON list of step reports)
--
-- No longer stored, derived when read from job_report_steps:
--   steps_completed       = count(steps)
--   completion_percentage = round(100 * count(steps) / total_steps)
--   plugin_state          = FAILURE if any mandatory step failed, else PARTIAL_SUCCESS if any step
--                           failed or partially succeeded, else SUCCESS if any step succeeded,
--                           else SKIPPED if any step was skipped, else RUNNING
--   plugin_details        = step details joined with line breaks, in step order
--   date_updated          = greatest(date_created, max(step date_updated))
--
-- Dropped: job_reports.line_separator (presentation), job_reports.instance_id (the job's),
-- jobs.instance_name (depends on instance_id), jobs.fields (not written by the model layer).
--
-- Steps are append-only while a job runs: adding a step is a single INSERT and no longer
-- rewrites the parent report row.
--
-- The old tables are dropped, not migrated: rows of jobs still running at upgrade time are lost.
--

-- ---------------------------------------------------------------------------
-- 1. Drop the old tables
-- ---------------------------------------------------------------------------

DROP TABLE IF EXISTS job_reports;
DROP TABLE IF EXISTS jobs;

-- ---------------------------------------------------------------------------
-- 2. Create the new schema
-- ---------------------------------------------------------------------------

CREATE TABLE plugins (
    id         integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    class_name character varying(255) NOT NULL,
    version    character varying(64)  NOT NULL DEFAULT '',
    name       character varying(255) NOT NULL,
    CONSTRAINT plugins_class_version_uk UNIQUE (class_name, version)
);

CREATE TABLE jobs (
    id                    uuid PRIMARY KEY,
    name                  character varying(255),
    username              character varying(255),
    state                 character varying(32) NOT NULL,
    state_details         text,
    plugin                character varying(255),
    plugin_type           character varying(32),
    priority              character varying(16),
    parallelism           character varying(16),
    outcome_objects_class character varying(255),
    source_selection      character varying(8) NOT NULL,
    source_objects_class  character varying(255),
    source_filter         jsonb,
    source_just_active    boolean,
    instance_id           character varying(255),
    start_date            timestamp(6) without time zone NOT NULL,
    end_date              timestamp(6) without time zone,
    flushed_at            timestamp(6) without time zone,
    CONSTRAINT jobs_state_check CHECK (state IN ('CREATED', 'STARTED', 'COMPLETED', 'FAILED_DURING_CREATION',
        'FAILED_TO_COMPLETE', 'STOPPED', 'STOPPING', 'TO_BE_CLEANED', 'PENDING_APPROVAL', 'REJECTED', 'SCHEDULED')),
    CONSTRAINT jobs_plugin_type_check CHECK (plugin_type IN ('INGEST', 'INTERNAL', 'SIP_TO_AIP', 'AIP_TO_SIP',
        'AIP_TO_AIP', 'MISC', 'MULTI')),
    CONSTRAINT jobs_priority_check CHECK (priority IN ('URGENT', 'HIGH', 'MEDIUM', 'LOW')),
    CONSTRAINT jobs_parallelism_check CHECK (parallelism IN ('LIMITED', 'NORMAL')),
    CONSTRAINT jobs_source_selection_check CHECK (source_selection IN ('NONE', 'ALL', 'LIST', 'FILTER')),
    -- the filter (an opaque, never partially queried value) exists exactly for FILTER selections
    CONSTRAINT jobs_source_filter_check CHECK ((source_selection = 'FILTER') = (source_filter IS NOT NULL))
);

-- used by the flush cleanup task to find jobs already written to storage
CREATE INDEX idx_jobs_flushed_at ON jobs USING btree (flushed_at) WHERE flushed_at IS NOT NULL;

-- 1:1 with jobs; split out so the frequent counter updates rewrite a small row (HOT-friendly)
CREATE TABLE job_stats (
    job_id                    uuid PRIMARY KEY REFERENCES jobs (id) ON DELETE CASCADE,
    completion_percentage     integer NOT NULL DEFAULT 0,
    source_objects_count      integer NOT NULL DEFAULT 0,
    being_processed           integer NOT NULL DEFAULT 0,
    waiting_to_be_processed   integer NOT NULL DEFAULT 0,
    processed_success         integer NOT NULL DEFAULT 0,
    processed_partial_success integer NOT NULL DEFAULT 0,
    processed_failure         integer NOT NULL DEFAULT 0,
    processed_skipped         integer NOT NULL DEFAULT 0,
    manual_intervention       integer NOT NULL DEFAULT 0
) WITH (fillfactor = 50);

CREATE TABLE job_plugin_parameters (
    id     bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    job_id uuid NOT NULL REFERENCES jobs (id) ON DELETE CASCADE,
    name   character varying(255) NOT NULL,
    value  text,
    CONSTRAINT job_plugin_parameters_job_name_uk UNIQUE (job_id, name)
);

-- ids of a LIST source selection
CREATE TABLE job_source_objects (
    id        bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    job_id    uuid NOT NULL REFERENCES jobs (id) ON DELETE CASCADE,
    object_id character varying(255) NOT NULL,
    CONSTRAINT job_source_objects_job_object_uk UNIQUE (job_id, object_id)
);

-- full_name/email are a snapshot taken when the job was created (users live in LDAP, not here)
CREATE TABLE job_users (
    id        bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    job_id    uuid NOT NULL REFERENCES jobs (id) ON DELETE CASCADE,
    username  character varying(255) NOT NULL,
    role      character varying(64)  NOT NULL DEFAULT '',
    full_name character varying(255),
    email     character varying(255),
    CONSTRAINT job_users_job_username_role_uk UNIQUE (job_id, username, role)
);

CREATE TABLE job_attachments (
    id        bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    job_id    uuid NOT NULL REFERENCES jobs (id) ON DELETE CASCADE,
    file_name character varying(255) NOT NULL,
    -- an attachment name is unique within its job; also serves lookups/deletes by job_id
    CONSTRAINT job_attachments_job_file_name_uk UNIQUE (job_id, file_name)
);

CREATE TABLE job_reports (
    pk                          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    job_id                      uuid NOT NULL REFERENCES jobs (id) ON DELETE CASCADE,
    source_object_id            character varying(255) NOT NULL,
    source_object_class         character varying(255),
    source_object_original_name character varying(255),
    outcome_object_id           character varying(255) NOT NULL,
    outcome_object_class        character varying(255),
    outcome_object_state        character varying(32),
    plugin_id                   integer REFERENCES plugins (id),
    total_steps                 integer NOT NULL DEFAULT 0,
    title                       character varying(255),
    ingest_type                 character varying(64),
    transaction_id              character varying(255),
    date_created                timestamp(6) without time zone NOT NULL,
    -- business id (IdUtils.getJobReportId), maintained by the database so it can never drift
    -- from its parts; changing the outcome object is a plain UPDATE, steps keep pointing at pk
    id                          text GENERATED ALWAYS AS
                                    (job_id::text || '-' || source_object_id || '-' || outcome_object_id) STORED,
    CONSTRAINT job_reports_outcome_object_state_check CHECK (outcome_object_state IN ('CREATED',
        'INGEST_PROCESSING', 'UNDER_APPRAISAL', 'ACTIVE', 'DELETED', 'DESTROYED', 'DESTROY_PROCESSING',
        'RESTORE_PROCESSING'))
);

CREATE UNIQUE INDEX job_reports_id_uk ON job_reports USING btree (id);
-- at the end of every block the transaction manager reads the reports written in that block's
-- transaction; (job_id, transaction_id) keeps that cost proportional to the block instead of to
-- the whole job, and as it leads with job_id it also serves every lookup by job
CREATE INDEX job_reports_job_transaction_idx ON job_reports USING btree (job_id, transaction_id);

CREATE TABLE job_report_source_original_ids (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    report_pk   bigint NOT NULL REFERENCES job_reports (pk) ON DELETE CASCADE,
    original_id character varying(255) NOT NULL,
    CONSTRAINT job_report_source_original_ids_report_original_uk UNIQUE (report_pk, original_id)
);

-- one row per plugin step of a report, in seq order; append-only while the job runs
CREATE TABLE job_report_steps (
    report_pk            bigint NOT NULL REFERENCES job_reports (pk) ON DELETE CASCADE,
    seq                  bigint GENERATED ALWAYS AS IDENTITY,
    plugin_id            integer REFERENCES plugins (id),
    plugin_state         character varying(16) NOT NULL,
    plugin_is_mandatory  boolean NOT NULL DEFAULT true,
    plugin_details       text,
    html_plugin_details  boolean NOT NULL DEFAULT false,
    outcome_object_state character varying(32),
    date_created         timestamp(6) without time zone,
    date_updated         timestamp(6) without time zone NOT NULL,
    PRIMARY KEY (report_pk, seq),
    CONSTRAINT job_report_steps_plugin_state_check CHECK (plugin_state IN ('SUCCESS', 'PARTIAL_SUCCESS',
        'FAILURE', 'RUNNING', 'SKIPPED')),
    CONSTRAINT job_report_steps_outcome_object_state_check CHECK (outcome_object_state IN ('CREATED',
        'INGEST_PROCESSING', 'UNDER_APPRAISAL', 'ACTIVE', 'DELETED', 'DESTROYED', 'DESTROY_PROCESSING',
        'RESTORE_PROCESSING'))
);

-- these tables are transient (rows live only while a job runs) and churn a lot:
-- vacuum them well before the default 20% of dead rows
ALTER TABLE jobs SET (autovacuum_vacuum_scale_factor = 0.02, autovacuum_analyze_scale_factor = 0.05);
ALTER TABLE job_stats SET (autovacuum_vacuum_scale_factor = 0.02, autovacuum_analyze_scale_factor = 0.05);
ALTER TABLE job_reports SET (autovacuum_vacuum_scale_factor = 0.02, autovacuum_analyze_scale_factor = 0.05);
ALTER TABLE job_report_steps SET (autovacuum_vacuum_scale_factor = 0.02, autovacuum_analyze_scale_factor = 0.05);
