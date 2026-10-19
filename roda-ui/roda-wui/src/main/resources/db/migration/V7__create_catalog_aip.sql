--- Structural information of every committed AIP, used by business logic instead of the search index.
--- Rebuilt from the main storage by the "Rebuild AIP catalog" job.
CREATE TABLE catalog_aip (
    id                    text        PRIMARY KEY,
    parent_id             text,
    state                 text        NOT NULL,
    ghost                 boolean     NOT NULL DEFAULT false,
    ingest_job_id         text,
    ingest_update_job_ids text[]      NOT NULL DEFAULT '{}',
    ingest_sip_ids        text[]      NOT NULL DEFAULT '{}',
    instance_id           text,
    updated_on            timestamptz NOT NULL
);

CREATE INDEX catalog_aip_parent_idx ON catalog_aip (parent_id);
CREATE INDEX catalog_aip_job_ghost_idx ON catalog_aip (ingest_job_id, ghost);
CREATE INDEX catalog_aip_state_idx ON catalog_aip (state);
CREATE INDEX catalog_aip_sip_ids_gin ON catalog_aip USING gin (ingest_sip_ids);
CREATE INDEX catalog_aip_update_job_ids_gin ON catalog_aip USING gin (ingest_update_job_ids);
