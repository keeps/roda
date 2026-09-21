--
-- Name: jobs flushed_at; Type: COLUMN
--
-- Marks the moment a completed job's reports were durably written to file
-- storage. The row itself (and its job_reports rows) is only removed later,
-- in a batch, by the asynchronous cleanup task -- decoupling the (must
-- happen immediately) storage flush from the (can be deferred) DB cleanup.
--

ALTER TABLE jobs ADD COLUMN flushed_at timestamp(6) without time zone;

CREATE INDEX idx_jobs_flushed_at ON jobs USING btree (flushed_at) WHERE flushed_at IS NOT NULL;
