-- Assignment Uniqueness Enhancement
-- Create partial unique index to enforce one active assignment per candidate at database level
-- This provides additional safety beyond application-level validation

-- Drop existing unique constraint that doesn't work correctly with boolean
ALTER TABLE assignments 
DROP CONSTRAINT IF EXISTS uk_active_candidate_assignment;

-- Create partial unique index (PostgreSQL feature)
-- Only enforces uniqueness where is_active = true
CREATE UNIQUE INDEX IF NOT EXISTS idx_one_active_assignment
ON assignments(candidate_id)
WHERE is_active = true AND deleted_at IS NULL;

-- Verify the index was created
SELECT 
    indexname, 
    indexdef 
FROM 
    pg_indexes 
WHERE 
    tablename = 'assignments' 
    AND indexname = 'idx_one_active_assignment';
