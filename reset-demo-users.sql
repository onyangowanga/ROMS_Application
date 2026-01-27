-- Reset demo users to allow DataInitializer to recreate them with correct passwords
DELETE FROM app_users WHERE username IN ('admin', 'operations', 'finance', 'employer', 'applicant');
