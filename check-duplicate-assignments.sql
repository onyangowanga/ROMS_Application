-- Check for duplicate active assignments per candidate
-- A candidate should only have ONE active assignment at a time

SELECT 
    candidate_id,
    COUNT(*) as active_assignment_count,
    STRING_AGG(CAST(id AS VARCHAR), ', ') as assignment_ids
FROM 
    assignments
WHERE 
    is_active = true
    AND deleted_at IS NULL
GROUP BY 
    candidate_id
HAVING 
    COUNT(*) > 1
ORDER BY 
    COUNT(*) DESC;

-- Get detailed view of candidates with multiple active assignments
SELECT 
    c.id as candidate_id,
    c.first_name,
    c.last_name,
    c.internal_ref_no,
    c.current_status,
    a.id as assignment_id,
    a.status as assignment_status,
    a.assigned_at,
    a.is_active,
    j.job_title
FROM 
    candidates c
    INNER JOIN assignments a ON c.id = a.candidate_id
    INNER JOIN job_orders j ON a.job_order_id = j.id
WHERE 
    a.is_active = true
    AND a.deleted_at IS NULL
    AND c.id IN (
        SELECT candidate_id 
        FROM assignments 
        WHERE is_active = true AND deleted_at IS NULL
        GROUP BY candidate_id 
        HAVING COUNT(*) > 1
    )
ORDER BY 
    c.id, a.assigned_at DESC;
