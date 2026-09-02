--
-- Name: idx_transactional_model_operation_log_transaction_id; Type: INDEX; Schema: public; Owner: admin
--
-- All queries on this table filter by transaction_id (the FK to transaction_log),
-- which had no index, forcing a sequential scan of the whole (globally shared) table.

CREATE INDEX idx_transactional_model_operation_log_transaction_id ON public.transactional_model_operation_log USING btree (transaction_id);

--
-- Name: idx_transactional_storage_path_op_log_transaction_id; Type: INDEX; Schema: public; Owner: admin
--

CREATE INDEX idx_transactional_storage_path_op_log_transaction_id ON public.transactional_storage_path_operation_log USING btree (transaction_id);

--
-- Name: idx_transactional_storage_path_op_log_tx_op_type; Type: INDEX; Schema: public; Owner: admin
--
-- Matches TransactionalStoragePathRepository.findByTransactionLogAndOperationType.

CREATE INDEX idx_transactional_storage_path_op_log_tx_op_type ON public.transactional_storage_path_operation_log USING btree (transaction_id, operation_type);

--
-- Name: idx_transactional_storage_path_op_log_tx_path; Type: INDEX; Schema: public; Owner: admin
--
-- Matches TransactionalStoragePathRepository.findAnyByTransactionLogAndStoragePathAndOperationType
-- and findModificationsUnderStoragePath.

CREATE INDEX idx_transactional_storage_path_op_log_tx_path ON public.transactional_storage_path_operation_log USING btree (transaction_id, storage_path);

--
-- Name: idx_transactional_storage_path_consolidated_op_transaction_id; Type: INDEX; Schema: public; Owner: admin
--

CREATE INDEX idx_transactional_storage_path_consolidated_op_transaction_id ON public.transactional_storage_path_consolidated_operation USING btree (transaction_id);
