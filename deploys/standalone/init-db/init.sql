-- init.sql
--CREATE TABLE IF NOT EXISTS transaction_log (
--    id VARCHAR(255) PRIMARY KEY,
--    status VARCHAR(20) NOT NULL,
--    operation_type VARCHAR(20) NOT NULL,
--    created_at TIMESTAMP NOT NULL,
--    updated_at TIMESTAMP
--);
--
--CREATE TABLE IF NOT EXISTS transactional_storage_path (
--     storage_path VARCHAR(255) PRIMARY KEY
--);
--
--CREATE TABLE IF NOT EXISTS transaction_log_storage_path (
--    transaction_log_id VARCHAR(255) NOT NULL,
--    storage_path VARCHAR(255) NOT NULL,
--    PRIMARY KEY (transaction_log_id, storage_path),
--    FOREIGN KEY (transaction_log_id) REFERENCES transaction_log(id)
--        ON DELETE CASCADE ON UPDATE CASCADE,
--    FOREIGN KEY (storage_path) REFERENCES transactional_storage_path(storage_path)
--        ON DELETE CASCADE ON UPDATE CASCADE
--);

CREATE TABLE IF NOT EXISTS INT_LOCK  (
	LOCK_KEY CHAR(36) NOT NULL,
	REGION VARCHAR(100) NOT NULL,
	CLIENT_ID CHAR(36),
	CREATED_DATE TIMESTAMP NOT NULL,
	constraint INT_LOCK_PK primary key (LOCK_KEY, REGION)
);
