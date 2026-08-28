--
-- PostgreSQL database dump
--

-- Dumped from database version 17.9 (Debian 17.9-1.pgdg13+1)
-- Dumped by pg_dump version 17.9 (Debian 17.9-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: job_reports; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.job_reports (
                                    id character varying(255) NOT NULL,
                                    completion_percentage integer,
                                    date_created timestamp(6) without time zone,
                                    date_updated timestamp(6) without time zone,
                                    html_plugin_details boolean,
                                    ingest_type character varying(255),
                                    instance_id character varying(255),
                                    job_id character varying(255),
                                    line_separator character varying(255),
                                    outcome_object_class character varying(255),
                                    outcome_object_id character varying(255),
                                    outcome_object_state character varying(255),
                                    plugin character varying(255),
                                    plugin_details text,
                                    plugin_is_mandatory boolean,
                                    plugin_name character varying(255),
                                    plugin_state character varying(255),
                                    plugin_version character varying(255),
                                    reports text,
                                    source_object_class character varying(255),
                                    source_object_id character varying(255),
                                    source_object_original_ids text,
                                    source_object_original_name character varying(255),
                                    steps_completed integer,
                                    title character varying(255),
                                    total_steps integer,
                                    transaction_id character varying(255),
                                    CONSTRAINT job_reports_outcome_object_state_check CHECK (((outcome_object_state)::text = ANY ((ARRAY['CREATED'::character varying, 'INGEST_PROCESSING'::character varying, 'UNDER_APPRAISAL'::character varying, 'ACTIVE'::character varying, 'DELETED'::character varying, 'DESTROYED'::character varying, 'DESTROY_PROCESSING'::character varying, 'RESTORE_PROCESSING'::character varying])::text[]))),
    CONSTRAINT job_reports_plugin_state_check CHECK (((plugin_state)::text = ANY ((ARRAY['SUCCESS'::character varying, 'PARTIAL_SUCCESS'::character varying, 'FAILURE'::character varying, 'RUNNING'::character varying, 'SKIPPED'::character varying])::text[])))
);


ALTER TABLE public.job_reports OWNER TO admin;

--
-- Name: jobs; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.jobs (
                             id character varying(255) NOT NULL,
                             attachments_list text,
                             end_date timestamp(6) without time zone,
                             fields text,
                             instance_id character varying(255),
                             instance_name character varying(255),
                             job_stats text,
                             job_users_details text,
                             name character varying(255),
                             outcome_objects_class character varying(255),
                             parallelism character varying(255),
                             plugin character varying(255),
                             plugin_parameters text,
                             plugin_type character varying(255),
                             priority character varying(255),
                             source_objects text,
                             start_date timestamp(6) without time zone,
                             state character varying(255),
                             state_details text,
                             username character varying(255),
                             CONSTRAINT jobs_parallelism_check CHECK (((parallelism)::text = ANY ((ARRAY['LIMITED'::character varying, 'NORMAL'::character varying])::text[]))),
    CONSTRAINT jobs_plugin_type_check CHECK (((plugin_type)::text = ANY ((ARRAY['INGEST'::character varying, 'INTERNAL'::character varying, 'SIP_TO_AIP'::character varying, 'AIP_TO_SIP'::character varying, 'AIP_TO_AIP'::character varying, 'MISC'::character varying, 'MULTI'::character varying])::text[]))),
    CONSTRAINT jobs_priority_check CHECK (((priority)::text = ANY ((ARRAY['URGENT'::character varying, 'HIGH'::character varying, 'MEDIUM'::character varying, 'LOW'::character varying])::text[]))),
    CONSTRAINT jobs_state_check CHECK (((state)::text = ANY ((ARRAY['CREATED'::character varying, 'STARTED'::character varying, 'COMPLETED'::character varying, 'FAILED_DURING_CREATION'::character varying, 'FAILED_TO_COMPLETE'::character varying, 'STOPPED'::character varying, 'STOPPING'::character varying, 'TO_BE_CLEANED'::character varying, 'PENDING_APPROVAL'::character varying, 'REJECTED'::character varying, 'SCHEDULED'::character varying])::text[])))
);


ALTER TABLE public.jobs OWNER TO admin;

--
-- Name: transaction_log; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.transaction_log (
                                        id uuid NOT NULL,
                                        created_at timestamp(6) without time zone NOT NULL,
                                        request_id uuid,
                                        request_type character varying(255) NOT NULL,
                                        status character varying(255) NOT NULL,
                                        updated_at timestamp(6) without time zone,
                                        CONSTRAINT transaction_log_request_type_check CHECK (((request_type)::text = ANY ((ARRAY['JOB'::character varying, 'API'::character varying, 'NON_DEFINED'::character varying])::text[]))),
    CONSTRAINT transaction_log_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'COMMITTING'::character varying, 'COMMITTED'::character varying, 'ROLLING_BACK'::character varying, 'ROLL_BACK_FAILED'::character varying, 'ROLLED_BACK'::character varying])::text[])))
);


ALTER TABLE public.transaction_log OWNER TO admin;

--
-- Name: transactional_model_operation_log; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.transactional_model_operation_log (
                                                          id uuid NOT NULL,
                                                          created_at timestamp(6) without time zone NOT NULL,
                                                          lite_object text,
                                                          operation_state character varying(255) NOT NULL,
                                                          operation_type character varying(255) NOT NULL,
                                                          updated_at timestamp(6) without time zone,
                                                          transaction_id uuid NOT NULL,
                                                          CONSTRAINT transactional_model_operation_log_operation_state_check CHECK (((operation_state)::text = ANY ((ARRAY['PENDING'::character varying, 'RUNNING'::character varying, 'FAILURE'::character varying, 'SUCCESS'::character varying, 'ROLLED_BACK'::character varying, 'ROLLING_BACK'::character varying, 'ROLL_BACK_FAILURE'::character varying])::text[]))),
    CONSTRAINT transactional_model_operation_log_operation_type_check CHECK (((operation_type)::text = ANY ((ARRAY['CREATE'::character varying, 'UPDATE'::character varying, 'DELETE'::character varying, 'READ'::character varying, 'CREATE_OR_UPDATE'::character varying])::text[])))
);


ALTER TABLE public.transactional_model_operation_log OWNER TO admin;

--
-- Name: transactional_storage_path_consolidated_operation; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.transactional_storage_path_consolidated_operation (
                                                                          id uuid NOT NULL,
                                                                          created_at timestamp(6) without time zone NOT NULL,
                                                                          operation_state character varying(255) NOT NULL,
                                                                          operation_type character varying(255) NOT NULL,
                                                                          previous_version character varying(255),
                                                                          storage_path text NOT NULL,
                                                                          updated_at timestamp(6) without time zone,
                                                                          version character varying(255),
                                                                          transaction_id uuid NOT NULL,
                                                                          CONSTRAINT transactional_storage_path_consolidated_o_operation_state_check CHECK (((operation_state)::text = ANY ((ARRAY['PENDING'::character varying, 'RUNNING'::character varying, 'FAILURE'::character varying, 'SUCCESS'::character varying, 'ROLLED_BACK'::character varying, 'ROLLING_BACK'::character varying, 'ROLL_BACK_FAILURE'::character varying])::text[]))),
    CONSTRAINT transactional_storage_path_consolidated_op_operation_type_check CHECK (((operation_type)::text = ANY ((ARRAY['CREATE'::character varying, 'UPDATE'::character varying, 'DELETE'::character varying, 'READ'::character varying, 'CREATE_OR_UPDATE'::character varying])::text[])))
);


ALTER TABLE public.transactional_storage_path_consolidated_operation OWNER TO admin;

--
-- Name: transactional_storage_path_operation_log; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.transactional_storage_path_operation_log (
                                                                 id uuid NOT NULL,
                                                                 created_at timestamp(6) without time zone NOT NULL,
                                                                 operation_state character varying(255) NOT NULL,
                                                                 operation_type character varying(255) NOT NULL,
                                                                 previous_version character varying(255),
                                                                 storage_path text NOT NULL,
                                                                 updated_at timestamp(6) without time zone,
                                                                 version character varying(255),
                                                                 transaction_id uuid NOT NULL,
                                                                 CONSTRAINT transactional_storage_path_operation_log_operation_state_check CHECK (((operation_state)::text = ANY ((ARRAY['PENDING'::character varying, 'RUNNING'::character varying, 'FAILURE'::character varying, 'SUCCESS'::character varying, 'ROLLED_BACK'::character varying, 'ROLLING_BACK'::character varying, 'ROLL_BACK_FAILURE'::character varying])::text[]))),
    CONSTRAINT transactional_storage_path_operation_log_operation_type_check CHECK (((operation_type)::text = ANY ((ARRAY['CREATE'::character varying, 'UPDATE'::character varying, 'DELETE'::character varying, 'READ'::character varying, 'CREATE_OR_UPDATE'::character varying])::text[])))
);


ALTER TABLE public.transactional_storage_path_operation_log OWNER TO admin;

--
-- Name: job_reports job_reports_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.job_reports
    ADD CONSTRAINT job_reports_pkey PRIMARY KEY (id);


--
-- Name: jobs jobs_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.jobs
    ADD CONSTRAINT jobs_pkey PRIMARY KEY (id);


--
-- Name: transaction_log transaction_log_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.transaction_log
    ADD CONSTRAINT transaction_log_pkey PRIMARY KEY (id);


--
-- Name: transactional_model_operation_log transactional_model_operation_log_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.transactional_model_operation_log
    ADD CONSTRAINT transactional_model_operation_log_pkey PRIMARY KEY (id);


--
-- Name: transactional_storage_path_consolidated_operation transactional_storage_path_consolidated_operation_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.transactional_storage_path_consolidated_operation
    ADD CONSTRAINT transactional_storage_path_consolidated_operation_pkey PRIMARY KEY (id);


--
-- Name: transactional_storage_path_operation_log transactional_storage_path_operation_log_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.transactional_storage_path_operation_log
    ADD CONSTRAINT transactional_storage_path_operation_log_pkey PRIMARY KEY (id);


--
-- Name: idx_report_job_id; Type: INDEX; Schema: public; Owner: admin
--

CREATE INDEX idx_report_job_id ON public.job_reports USING btree (job_id);


--
-- Name: transactional_model_operation_log fk67f6aqryq0kt4kseko94y53ri; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.transactional_model_operation_log
    ADD CONSTRAINT fk67f6aqryq0kt4kseko94y53ri FOREIGN KEY (transaction_id) REFERENCES public.transaction_log(id);


--
-- Name: transactional_storage_path_consolidated_operation fkc97nbb5vxbp8kaxeco280p9mb; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.transactional_storage_path_consolidated_operation
    ADD CONSTRAINT fkc97nbb5vxbp8kaxeco280p9mb FOREIGN KEY (transaction_id) REFERENCES public.transaction_log(id);

--
-- Name: transactional_storage_path_operation_log fko951crqyu5425flp1q55ke9yg; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.transactional_storage_path_operation_log
    ADD CONSTRAINT fko951crqyu5425flp1q55ke9yg FOREIGN KEY (transaction_id) REFERENCES public.transaction_log(id);


--
-- PostgreSQL database dump complete
--
