-- ROMS Database Setup for Windows PostgreSQL Installation
-- Run this script as postgres superuser after installing PostgreSQL on Windows
--
-- Usage:
--   Option 1: psql -U postgres -f setup-windows-postgresql.sql
--   Option 2: Open pgAdmin and run this script in Query Tool

-- Create database
CREATE DATABASE roms_db;

-- Create user with password
CREATE USER roms_user WITH ENCRYPTED PASSWORD 'roms_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE roms_db TO roms_user;

-- Connect to the database
\c roms_db

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO roms_user;

-- Grant all privileges on all tables (for existing and future tables)
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO roms_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO roms_user;

-- Verify setup
SELECT current_database(), current_user;

\echo 'Database setup complete!'
\echo 'Database: roms_db'
\echo 'Username: roms_user'
\echo 'Password: roms_password'
