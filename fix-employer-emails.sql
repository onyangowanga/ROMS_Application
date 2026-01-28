-- Fix existing employers to match user email
-- This ensures employer dashboard works for existing users

-- Update employer contactEmail to match user email where they differ
UPDATE employers e
SET contact_email = u.email
FROM app_users u
WHERE u.role = 'EMPLOYER' 
  AND u.full_name = e.contact_person
  AND u.email != e.contact_email
  AND e.deleted_at IS NULL;

-- Verify the update
SELECT 
    u.username,
    u.email as user_email,
    e.company_name,
    e.contact_email as employer_email,
    CASE 
        WHEN u.email = e.contact_email THEN 'MATCHED ✓'
        ELSE 'MISMATCHED ✗'
    END as status
FROM app_users u
LEFT JOIN employers e ON u.full_name = e.contact_person
WHERE u.role = 'EMPLOYER'
  AND e.deleted_at IS NULL;
