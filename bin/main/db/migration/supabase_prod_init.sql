-- Infinite Prints Production Schema Initialization for Supabase
-- Run this ONCE on your Supabase PostgreSQL database to set up all required tables
-- DO NOT modify this file after running on production

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table (core authentication)
CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User roles (for authorization)
CREATE TABLE IF NOT EXISTS user_roles (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  role VARCHAR(50) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- JWT token tracking (optional, for token blacklist/refresh)
CREATE TABLE IF NOT EXISTS jwt_tokens (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash VARCHAR(255) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Audit log (track all user actions)
CREATE TABLE IF NOT EXISTS audit_log (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID REFERENCES users(id),
  entity_type VARCHAR(100),
  action VARCHAR(50),
  details TEXT,
  ip_address VARCHAR(45),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_jwt_tokens_user_id ON jwt_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_created_at ON audit_log(created_at);

-- Insert production admin user
-- NOTE: Replace the password hash with your actual bcrypt hash
-- To generate: use https://bcrypt-generator.com or run:
--   htpasswd -bnBC 10 "" your_password | tr -d ':\n'
INSERT INTO users (id, email, password, first_name, last_name, created_at)
VALUES (
  '00000000-0000-0000-0000-000000000001'::uuid,
  'admin@infiniteprints.com',
  '$2a$12$TevPSg2YLEhrsy9qWvijJeecNpuGJ6zvbyafVL.RrQ7/af7G.uaD.', -- REPLACE WITH YOUR BCRYPT HASH
  'Admin',
  'User',
  NOW()
)
ON CONFLICT (email) DO NOTHING;

-- Assign admin role to admin user
INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ADMIN' FROM users WHERE email = 'admin@infiniteprints.com'
ON CONFLICT DO NOTHING;

-- Verify tables created
SELECT 'Schema initialization complete. Tables created:' as status;
SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tablename IN ('users', 'user_roles', 'jwt_tokens', 'audit_log');
