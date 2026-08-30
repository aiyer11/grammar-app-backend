CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    proficiency_level proficiency_level NOT NULL DEFAULT 'BEGINNER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT email_lowercase CHECK (email = LOWER(email)),
    CONSTRAINT username_format CHECK (username ~ '^[a-zA-Z0-9_]{3,30}$'));

    CREATE INDEX idx_users_email ON users (email);
    CREATE INDEX idx_users_created_at ON users (created_at);