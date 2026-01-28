-- Insert sample job orders directly
-- Run this in pgAdmin Query Tool for roms_db database

-- First, let's check and delete any existing test jobs if needed
DELETE FROM job_orders WHERE job_order_ref LIKE 'JO-0%';

-- Create a default employer if it doesn't exist
INSERT INTO employers (
    company_name, contact_person, contact_email, contact_phone, 
    country, industry, address, is_active, 
    created_at, created_by, last_modified_at, last_modified_by
)
SELECT 
    'ROMS Test Company', 'HR Manager', 'hr@romstest.com', '+971-50-1234567',
    'UAE', 'Recruitment', 'Dubai, UAE', true,
    CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'admin'
WHERE NOT EXISTS (
    SELECT 1 FROM employers WHERE company_name = 'ROMS Test Company'
);

-- Get the employer ID (will be 1 if it's the first employer)
-- Insert job orders with all required audit fields
INSERT INTO job_orders (
    employer_id, job_order_ref, job_title, headcount_required, headcount_filled, 
    salary_min, salary_max, currency, location, country, 
    contract_duration_months, required_skills, description, status, 
    is_active, created_at, created_by, last_modified_at, last_modified_by
) VALUES
((SELECT id FROM employers WHERE company_name = 'ROMS Test Company' LIMIT 1), 'JO-001', 'Mechanical Engineer', 10, 0, 2500.00, 3500.00, 'USD', 'Dubai', 'UAE', 
 24, 'AutoCAD, SolidWorks, 3+ years experience', 'Seeking qualified Mechanical Engineers for construction projects', 'OPEN', 
 true, CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'admin'),

((SELECT id FROM employers WHERE company_name = 'ROMS Test Company' LIMIT 1), 'JO-002', 'Chef', 5, 0, 2000.00, 3000.00, 'USD', 'Dubai', 'UAE', 
 24, 'Culinary degree, 5+ years experience', 'Experienced Chef for 5-star hotel', 'OPEN', 
 true, CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'admin'),

((SELECT id FROM employers WHERE company_name = 'ROMS Test Company' LIMIT 1), 'JO-003', 'Civil Engineer', 8, 0, 3000.00, 4000.00, 'USD', 'Abu Dhabi', 'UAE', 
 36, 'AutoCAD, Civil 3D, 4+ years experience', 'Civil Engineers for infrastructure projects', 'OPEN', 
 true, CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'admin'),

((SELECT id FROM employers WHERE company_name = 'ROMS Test Company' LIMIT 1), 'JO-004', 'Nurse', 15, 0, 1800.00, 2500.00, 'USD', 'Riyadh', 'Saudi Arabia', 
 24, 'Nursing degree, valid license, 3+ years experience', 'Registered Nurses for hospital', 'OPEN', 
 true, CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'admin'),

((SELECT id FROM employers WHERE company_name = 'ROMS Test Company' LIMIT 1), 'JO-005', 'Electrical Engineer', 6, 0, 2800.00, 3800.00, 'USD', 'Doha', 'Qatar', 
 36, 'Electrical systems, 4+ years in oil & gas', 'Electrical Engineers for oil & gas sector', 'OPEN', 
 true, CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'admin'),

((SELECT id FROM employers WHERE company_name = 'ROMS Test Company' LIMIT 1), 'JO-006', 'Software Developer', 12, 0, 3500.00, 5000.00, 'USD', 'Dubai', 'UAE', 
 24, 'Java, Spring Boot, React, 3+ years experience', 'Software Developers for enterprise applications', 'OPEN', 
 true, CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'admin'),

((SELECT id FROM employers WHERE company_name = 'ROMS Test Company' LIMIT 1), 'JO-007', 'Accountant', 4, 0, 2200.00, 3200.00, 'USD', 'Abu Dhabi', 'UAE', 
 24, 'CPA, 4+ years experience', 'Senior Accountant for financial operations', 'OPEN', 
 true, CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'admin'),

((SELECT id FROM employers WHERE company_name = 'ROMS Test Company' LIMIT 1), 'JO-008', 'Sales Manager', 3, 0, 3000.00, 4500.00, 'USD', 'Doha', 'Qatar', 
 36, 'MBA, 5+ years in sales management', 'Sales Manager for regional operations', 'OPEN', 
 true, CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP, 'admin');

-- Verify the insert
SELECT job_order_ref, job_title, status, country FROM job_orders WHERE job_order_ref LIKE 'JO-0%';

COMMIT;
