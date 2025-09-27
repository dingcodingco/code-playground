-- Initial schema creation for VibeCoding application
-- Creates all necessary tables with proper constraints and indexes

-- Create code_snippets table
CREATE TABLE code_snippets (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    code TEXT NOT NULL,
    language VARCHAR(50) NOT NULL,
    author_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Create executions table
CREATE TABLE executions (
    id BIGSERIAL PRIMARY KEY,
    code_snippet_id BIGINT NOT NULL,
    output TEXT,
    error_message TEXT,
    execution_time BIGINT NOT NULL DEFAULT 0 CHECK (execution_time >= 0),
    memory_usage BIGINT CHECK (memory_usage >= 0),
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    FOREIGN KEY (code_snippet_id) REFERENCES code_snippets(id) ON DELETE CASCADE
);

-- Create shared_codes table
CREATE TABLE shared_codes (
    id BIGSERIAL PRIMARY KEY,
    code_snippet_id BIGINT NOT NULL,
    share_id VARCHAR(50) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITHOUT TIME ZONE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    FOREIGN KEY (code_snippet_id) REFERENCES code_snippets(id) ON DELETE CASCADE
);

-- Create indexes for performance optimization
CREATE INDEX idx_code_snippets_language ON code_snippets(language);
CREATE INDEX idx_code_snippets_author_name ON code_snippets(author_name);
CREATE INDEX idx_code_snippets_is_active ON code_snippets(is_active);
CREATE INDEX idx_code_snippets_created_at ON code_snippets(created_at);

CREATE INDEX idx_executions_code_snippet_id ON executions(code_snippet_id);
CREATE INDEX idx_executions_status ON executions(status);
CREATE INDEX idx_executions_created_at ON executions(created_at);

CREATE INDEX idx_shared_codes_code_snippet_id ON shared_codes(code_snippet_id);
CREATE INDEX idx_shared_codes_share_id ON shared_codes(share_id);
CREATE INDEX idx_shared_codes_is_active ON shared_codes(is_active);
CREATE INDEX idx_shared_codes_expires_at ON shared_codes(expires_at);

-- Create composite indexes for common query patterns
CREATE INDEX idx_code_snippets_active_author ON code_snippets(is_active, author_name, created_at DESC);
CREATE INDEX idx_code_snippets_active_language ON code_snippets(is_active, language, created_at DESC);
CREATE INDEX idx_executions_snippet_status ON executions(code_snippet_id, status, created_at DESC);
CREATE INDEX idx_shared_codes_active_valid ON shared_codes(is_active, expires_at, created_at DESC);

-- Add constraints for data integrity
ALTER TABLE code_snippets ADD CONSTRAINT chk_title_not_empty CHECK (LENGTH(TRIM(title)) > 0);
ALTER TABLE code_snippets ADD CONSTRAINT chk_code_not_empty CHECK (LENGTH(TRIM(code)) > 0);
ALTER TABLE code_snippets ADD CONSTRAINT chk_language_not_empty CHECK (LENGTH(TRIM(language)) > 0);
ALTER TABLE code_snippets ADD CONSTRAINT chk_author_name_not_empty CHECK (LENGTH(TRIM(author_name)) > 0);

ALTER TABLE shared_codes ADD CONSTRAINT chk_share_id_format CHECK (share_id ~ '^[a-zA-Z0-9]{8,50}$');

-- Add comments for documentation
COMMENT ON TABLE code_snippets IS 'Stores user-created code snippets with metadata';
COMMENT ON TABLE executions IS 'Records code execution results and performance metrics';
COMMENT ON TABLE shared_codes IS 'Manages code snippet sharing with expiration control';

COMMENT ON COLUMN code_snippets.language IS 'Programming language as free text (no enum constraints)';
COMMENT ON COLUMN code_snippets.is_active IS 'Soft delete flag for code snippets';
COMMENT ON COLUMN executions.status IS 'Execution status: SUCCESS, ERROR, TIMEOUT';
COMMENT ON COLUMN executions.execution_time IS 'Execution time in milliseconds';
COMMENT ON COLUMN executions.memory_usage IS 'Memory usage in bytes';
COMMENT ON COLUMN shared_codes.expires_at IS 'Expiration timestamp, NULL means never expires';