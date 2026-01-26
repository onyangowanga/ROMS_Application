-- PostgreSQL Database Setup for ROMS
-- Run this script as postgres superuser

-- Create database user
CREATE USER roms_user WITH PASSWORD 'roms_password';

-- Create database
CREATE DATABASE roms_db OWNER roms_user;

-- Grant all privileges
GRANT ALL PRIVILEGES ON DATABASE roms_db TO roms_user;

-- Connect to the roms_db database
\c roms_db;

-- Grant schema permissions (PostgreSQL 15+)
GRANT ALL ON SCHEMA public TO roms_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO roms_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO roms_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO roms_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO roms_user;

-- Verify setup
SELECT current_database(), current_user;
