-- Fix candidate status check constraint to include all valid enum values
-- This updates the constraint to match the current CandidateStatus enum

-- First, check what invalid values exist in audit table
SELECT DISTINCT current_status 
FROM candidates_aud 
WHERE current_status NOT IN (
    'APPLIED',
    'DOCUMENTS_PENDING',
    'DOCUMENTS_UNDER_REVIEW',
    'DOCUMENTS_APPROVED',
    'INTERVIEW_SCHEDULED',
    'INTERVIEW_COMPLETED',
    'MEDICAL_IN_PROGRESS',
    'MEDICAL_PASSED',
    'OFFER_ISSUED',
    'OFFER_SIGNED',
    'DEPLOYED',
    'PLACED',
    'REJECTED',
    'WITHDRAWN'
);

-- Drop the old constraints (main table and audit table)
ALTER TABLE candidates DROP CONSTRAINT IF EXISTS candidates_current_status_check;
ALTER TABLE candidates_aud DROP CONSTRAINT IF EXISTS candidates_aud_current_status_check;

-- Add the updated constraint with all valid statuses to main table
ALTER TABLE candidates ADD CONSTRAINT candidates_current_status_check 
CHECK (current_status IN (
    'APPLIED',
    'DOCUMENTS_PENDING',
    'DOCUMENTS_UNDER_REVIEW',
    'DOCUMENTS_APPROVED',
    'INTERVIEW_SCHEDULED',
    'INTERVIEW_COMPLETED',
    'MEDICAL_IN_PROGRESS',
    'MEDICAL_PASSED',
    'OFFER_ISSUED',
    'OFFER_SIGNED',
    'DEPLOYED',
    'PLACED',
    'REJECTED',
    'WITHDRAWN'
));

-- Add the updated constraint with all valid statuses to audit table
ALTER TABLE candidates_aud ADD CONSTRAINT candidates_aud_current_status_check 
CHECK (current_status IN (
    'APPLIED',
    'DOCUMENTS_PENDING',
    'DOCUMENTS_UNDER_REVIEW',
    'DOCUMENTS_APPROVED',
    'INTERVIEW_SCHEDULED',
    'INTERVIEW_COMPLETED',
    'MEDICAL_IN_PROGRESS',
    'MEDICAL_PASSED',
    'OFFER_ISSUED',
    'OFFER_SIGNED',
    'DEPLOYED',
    'PLACED',
    'REJECTED',
    'WITHDRAWN'
));

-- Verify the constraints were created
SELECT conname, pg_get_constraintdef(oid) 
FROM pg_constraint 
WHERE conrelid IN ('candidates'::regclass, 'candidates_aud'::regclass) AND contype = 'c';
