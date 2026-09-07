CREATE TABLE lesson_concepts (
    id SERIAL PRIMARY KEY,
    lesson_code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    difficulty_level difficulty_level,
    status lesson_status,
    goal TEXT NOT NULL,
    objectives JSONB NOT NULL
)