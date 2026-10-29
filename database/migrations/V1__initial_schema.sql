-- ========================================================
-- CivicFix - Database Schema Migration V1
-- ========================================================

-- 1. Departments Table
CREATE TABLE IF NOT EXISTS departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    contact_email VARCHAR(100),
    contact_phone VARCHAR(30),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 2. Issue Categories Table
CREATE TABLE IF NOT EXISTS issue_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    department_id BIGINT REFERENCES departments(id) ON DELETE SET NULL,
    sla_hours INT DEFAULT 48 NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 3. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30),
    role VARCHAR(30) NOT NULL,
    department_id BIGINT REFERENCES departments(id) ON DELETE SET NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 4. Issues Table
CREATE TABLE IF NOT EXISTS issues (
    id BIGSERIAL PRIMARY KEY,
    tracking_number VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category_id BIGINT NOT NULL REFERENCES issue_categories(id),
    department_id BIGINT REFERENCES departments(id) ON DELETE SET NULL,
    reporter_id BIGINT NOT NULL REFERENCES users(id),
    assigned_worker_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    status VARCHAR(50) NOT NULL,
    severity VARCHAR(30) NOT NULL,
    calculated_priority VARCHAR(30) NOT NULL,
    priority_score DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
    latitude NUMERIC(10, 7) NOT NULL,
    longitude NUMERIC(10, 7) NOT NULL,
    address_text VARCHAR(255),
    confirmation_count INT DEFAULT 0 NOT NULL,
    reopen_count INT DEFAULT 0 NOT NULL,
    due_date TIMESTAMP WITH TIME ZONE,
    resolved_at TIMESTAMP WITH TIME ZONE,
    closed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 5. Issue Images & Evidence Table
CREATE TABLE IF NOT EXISTS issue_images (
    id BIGSERIAL PRIMARY KEY,
    issue_id BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    uploader_id BIGINT NOT NULL REFERENCES users(id),
    image_url TEXT NOT NULL,
    image_type VARCHAR(30) NOT NULL,
    caption VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 6. Worker Assignments Table
CREATE TABLE IF NOT EXISTS worker_assignments (
    id BIGSERIAL PRIMARY KEY,
    issue_id BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    worker_id BIGINT NOT NULL REFERENCES users(id),
    assigned_by_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(30) NOT NULL,
    decline_reason TEXT,
    worker_notes TEXT,
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    accepted_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE
);

-- 7. Issue Status History (Audit Timeline) Table
CREATE TABLE IF NOT EXISTS issue_status_history (
    id BIGSERIAL PRIMARY KEY,
    issue_id BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    actor_id BIGINT NOT NULL REFERENCES users(id),
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    reason_or_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 8. Community Confirmations Table (Duplicate prevention via unique composite key)
CREATE TABLE IF NOT EXISTS community_confirmations (
    id BIGSERIAL PRIMARY KEY,
    issue_id BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    citizen_id BIGINT NOT NULL REFERENCES users(id),
    note TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_issue_citizen UNIQUE (issue_id, citizen_id)
);

-- 9. Notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    issue_id BIGINT REFERENCES issues(id) ON DELETE CASCADE,
    is_read BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 10. Resolution Ratings Table
CREATE TABLE IF NOT EXISTS resolution_ratings (
    id BIGSERIAL PRIMARY KEY,
    issue_id BIGINT NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    citizen_id BIGINT NOT NULL REFERENCES users(id),
    worker_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    feedback TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_rating_issue_citizen UNIQUE (issue_id, citizen_id)
);

-- 11. Audit Logs Table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(100),
    actor_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    actor_email VARCHAR(150),
    actor_role VARCHAR(50),
    details TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexes for high-performance querying and filtering
CREATE INDEX IF NOT EXISTS idx_issues_status ON issues(status);
CREATE INDEX IF NOT EXISTS idx_issues_category ON issues(category_id);
CREATE INDEX IF NOT EXISTS idx_issues_reporter ON issues(reporter_id);
CREATE INDEX IF NOT EXISTS idx_issues_worker ON issues(assigned_worker_id);
CREATE INDEX IF NOT EXISTS idx_issues_department ON issues(department_id);
CREATE INDEX IF NOT EXISTS idx_issues_priority ON issues(calculated_priority);
CREATE INDEX IF NOT EXISTS idx_issues_created_at ON issues(created_at);
CREATE INDEX IF NOT EXISTS idx_notifications_recipient ON notifications(recipient_id, is_read);
CREATE INDEX IF NOT EXISTS idx_status_history_issue ON issue_status_history(issue_id);
