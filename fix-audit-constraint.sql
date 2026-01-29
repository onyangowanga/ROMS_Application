-- Fix audit table constraint
-- First check what invalid data exists
SELECT DISTINCT current_status FROM candidates_aud WHERE current_status IS NOT NULL;

-- Drop the old constraint
ALTER TABLE candidates_aud DROP CONSTRAINT IF EXISTS candidates_aud_current_status_check;

-- Add the new constraint
ALTER TABLE candidates_aud ADD CONSTRAINT candidates_aud_current_status_check 
CHECK (current_status IN ('APPLIED','DOCUMENTS_PENDING','DOCUMENTS_UNDER_REVIEW','DOCUMENTS_APPROVED','INTERVIEW_SCHEDULED','INTERVIEW_COMPLETED','MEDICAL_IN_PROGRESS','MEDICAL_PASSED','OFFER_ISSUED','OFFER_SIGNED','DEPLOYED','PLACED','REJECTED','WITHDRAWN'));
