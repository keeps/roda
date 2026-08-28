--- Add OPTIMIST_CREATE_IF_NOT_EXISTS to model and storage_path operation type constraints
ALTER TABLE ONLY public.transactional_model_operation_log
    DROP CONSTRAINT transactional_model_operation_log_operation_type_check;

ALTER TABLE ONLY public.transactional_model_operation_log
    ADD CONSTRAINT transactional_model_operation_log_operation_type_check CHECK (((operation_type)::text = ANY ((ARRAY['CREATE'::character varying, 'UPDATE'::character varying, 'DELETE'::character varying, 'READ'::character varying, 'CREATE_OR_UPDATE'::character varying, 'OPTIMISTIC_CREATE_IF_NOT_EXISTS'::character varying])::text[])));

ALTER TABLE ONLY public.transactional_storage_path_operation_log
DROP CONSTRAINT transactional_storage_path_operation_log_operation_type_check;

ALTER TABLE ONLY public.transactional_storage_path_operation_log
    ADD CONSTRAINT transactional_storage_path_operation_log_operation_type_check CHECK (((operation_type)::text = ANY ((ARRAY['CREATE'::character varying, 'UPDATE'::character varying, 'DELETE'::character varying, 'READ'::character varying, 'CREATE_OR_UPDATE'::character varying, 'OPTIMISTIC_CREATE_IF_NOT_EXISTS'::character varying])::text[])));

ALTER TABLE ONLY public.transactional_storage_path_consolidated_operation
DROP CONSTRAINT transactional_storage_path_consolidated_op_operation_type_check;

ALTER TABLE ONLY public.transactional_storage_path_consolidated_operation
    ADD CONSTRAINT transactional_storage_path_consolidated_op_operation_type_check CHECK (((operation_type)::text = ANY ((ARRAY['CREATE'::character varying, 'UPDATE'::character varying, 'DELETE'::character varying, 'READ'::character varying, 'CREATE_OR_UPDATE'::character varying, 'OPTIMISTIC_CREATE_IF_NOT_EXISTS'::character varying])::text[])));


--- Add SKIPPED to model and storage_path operation state constraints
ALTER TABLE ONLY public.transactional_model_operation_log
    DROP CONSTRAINT transactional_model_operation_log_operation_state_check;

ALTER TABLE ONLY public.transactional_model_operation_log
    ADD CONSTRAINT transactional_model_operation_log_operation_state_check CHECK (((operation_state)::text = ANY ((ARRAY['PENDING'::character varying, 'RUNNING'::character varying, 'FAILURE'::character varying, 'SUCCESS'::character varying, 'ROLLED_BACK'::character varying, 'ROLLING_BACK'::character varying, 'ROLL_BACK_FAILURE'::character varying, 'SKIPPED'::character varying])::text[])));

ALTER TABLE ONLY public.transactional_storage_path_operation_log
DROP CONSTRAINT transactional_storage_path_operation_log_operation_state_check;

ALTER TABLE ONLY public.transactional_storage_path_operation_log
    ADD CONSTRAINT transactional_storage_path_operation_log_operation_state_check CHECK (((operation_state)::text = ANY ((ARRAY['PENDING'::character varying, 'RUNNING'::character varying, 'FAILURE'::character varying, 'SUCCESS'::character varying, 'ROLLED_BACK'::character varying, 'ROLLING_BACK'::character varying, 'ROLL_BACK_FAILURE'::character varying, 'SKIPPED'::character varying])::text[])));

ALTER TABLE ONLY public.transactional_storage_path_consolidated_operation
DROP CONSTRAINT transactional_storage_path_consolidated_o_operation_state_check;

ALTER TABLE ONLY public.transactional_storage_path_consolidated_operation
    ADD CONSTRAINT transactional_storage_path_consolidated_o_operation_state_check CHECK (((operation_state)::text = ANY ((ARRAY['PENDING'::character varying, 'RUNNING'::character varying, 'FAILURE'::character varying, 'SUCCESS'::character varying, 'ROLLED_BACK'::character varying, 'ROLLING_BACK'::character varying, 'ROLL_BACK_FAILURE'::character varying, 'SKIPPED'::character varying])::text[])));