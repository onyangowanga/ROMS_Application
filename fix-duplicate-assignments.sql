-- Fix duplicate active assignments
-- This script will keep the MOST RECENT active assignment for each candidate
-- and deactivate all older ones

-- First, let's see what we're about to fix
SELECT 
    'Before cleanup - Candidates with multiple active assignments:' as info,
    COUNT(DISTINCT candidate_id) as affected_candidates
FROM (
    SELECT candidate_id 
    FROM assignments 
    WHERE is_active = true AND deleted_at IS NULL
    GROUP BY candidate_id 
    HAVING COUNT(*) > 1
) duplicates;

-- Deactivate all but the most recent active assignment per candidate
-- Uses a CTE to identify which assignments should remain active
WITH assignments_to_keep AS (
    SELECT DISTINCT ON (candidate_id) 
        id,
        candidate_id
    FROM assignments
    WHERE is_active = true AND deleted_at IS NULL
    ORDER BY candidate_id, assigned_at DESC, id DESC
),
assignments_to_deactivate AS (
    SELECT a.id, a.candidate_id, a.assigned_at
    FROM assignments a
    WHERE a.is_active = true 
        AND a.deleted_at IS NULL
        AND a.id NOT IN (SELECT id FROM assignments_to_keep)
)
UPDATE assignments
SET 
    is_active = false,
    status = 'CANCELLED',
    cancelled_at = NOW(),
    last_modified_at = NOW(),
    last_modified_by = 'SYSTEM_CLEANUP'
WHERE id IN (SELECT id FROM assignments_to_deactivate);

-- Show results
SELECT 
    'After cleanup - Remaining active assignments per candidate:' as info,
    candidate_id,
    COUNT(*) as active_count
FROM assignments
WHERE is_active = true AND deleted_at IS NULL
GROUP BY candidate_id
ORDER BY active_count DESC, candidate_id;

-- Verify no duplicates remain
SELECT 
    'Final check - Candidates with multiple active assignments:' as info,
    COUNT(DISTINCT candidate_id) as remaining_duplicates
FROM (
    SELECT candidate_id 
    FROM assignments 
    WHERE is_active = true AND deleted_at IS NULL
    GROUP BY candidate_id 
    HAVING COUNT(*) > 1
) duplicates;
