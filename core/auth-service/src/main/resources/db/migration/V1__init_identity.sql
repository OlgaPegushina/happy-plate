CREATE SCHEMA IF NOT EXISTS identity;

CREATE TABLE IF NOT EXISTS identity.users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email ON identity.users(email);

CREATE TABLE IF NOT EXISTS identity.user_roles (
    user_id UUID NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_roles_user_id_role ON identity.user_roles(user_id, role);
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON identity.user_roles(user_id);