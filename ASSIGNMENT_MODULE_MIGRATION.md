# Phase 2A.1 Assignment Module - Migration Guide

## Overview
This update implements the **Assignment entity** as an intermediary between Candidate and JobOrder, completing Phase 2A requirements.

## What Changed

### 1. Database Schema Changes

**REMOVED:**
- `candidates.job_order_id` column (direct foreign key to job_orders)

**ADDED:**
- New table: `assignments`
  - `id` (PK, auto-increment)
  - `candidate_id` (FK to candidates, NOT NULL)
  - `job_order_id` (FK to job_orders, NOT NULL)
  - `status` (ENUM: ASSIGNED, OFFERED, PLACED, CANCELLED)
  - `is_active` (BOOLEAN, default TRUE)
  - `assigned_at` (TIMESTAMP, NOT NULL)
  - `offer_issued_at` (TIMESTAMP, nullable)
  - `placement_confirmed_at` (TIMESTAMP, nullable)
  - `cancelled_at` (TIMESTAMP, nullable)
  - `notes` (TEXT, nullable)
  - Unique constraint on (candidate_id, is_active) when is_active = TRUE

### 2. Migration Steps

#### Option A: Fresh Database (Recommended for Development)
```bash
# Drop and recreate database
DROP DATABASE roms;
CREATE DATABASE roms;

# Restart application - Hibernate will create new schema with assignments table
```

#### Option B: Migrate Existing Data
```sql
-- Step 1: Create assignments table (Hibernate will auto-create on restart)

-- Step 2: Migrate existing candidate-job relationships to assignments
INSERT INTO assignments (candidate_id, job_order_id, status, is_active, assigned_at, notes, created_at, updated_at)
SELECT 
    id as candidate_id,
    job_order_id,
    CASE 
        WHEN current_status = 'PLACED' THEN 'PLACED'
        WHEN current_status IN ('OFFER_ISSUED', 'OFFER_SIGNED', 'DEPLOYED') THEN 'OFFERED'
        ELSE 'ASSIGNED'
    END as status,
    true as is_active,
    created_at as assigned_at,
    'Migrated from old candidate.job_order_id relationship' as notes,
    NOW() as created_at,
    NOW() as updated_at
FROM candidates
WHERE job_order_id IS NOT NULL 
  AND deleted_at IS NULL
  AND current_status NOT IN ('REJECTED', 'WITHDRAWN');

-- Step 3: Remove old column (after verifying migration)
ALTER TABLE candidates DROP COLUMN job_order_id;
```

## New Features

### Backend
1. **Assignment Entity** (`com.roms.entity.Assignment`)
   - Full lifecycle tracking (assigned → offered → placed)
   - Soft delete support
   - Audit trail with timestamps

2. **Assignment Service** (`com.roms.service.AssignmentService`)
   - Business rule enforcement:
     - One active assignment per candidate
     - Job must be OPEN
     - Headcount capacity validation
   - Auto-increment/decrement job headcount

3. **Assignment Controller** (`com.roms.controller.AssignmentController`)
   - `POST /api/assignments` - Create assignment
   - `GET /api/assignments` - List all
   - `GET /api/assignments/candidate/{id}` - Candidate's assignments
   - `GET /api/assignments/job-order/{id}` - Job's assignments
   - `DELETE /api/assignments/{id}` - Cancel assignment
   - `PUT /api/assignments/{id}/issue-offer` - Issue offer
   - `PUT /api/assignments/{id}/confirm-placement` - Confirm placement

4. **Updated Workflow Guards**
   - PLACED transition now requires active assignment
   - Removed direct `jobOrder` check from Candidate entity

### Frontend
1. **Assignment Panel** in Candidate Profile Page
   - Dropdown to assign candidate to OPEN jobs
   - Shows current active assignment with status
   - Assignment history display
   - Cancel assignment functionality

2. **New API Client** (`frontend/src/api/assignments.ts`)
   - Full CRUD operations for assignments

## Testing Checklist

- [ ] Backend compiles without errors
- [ ] Database migrated successfully
- [ ] Can create assignment for candidate
- [ ] Cannot create 2nd active assignment for same candidate
- [ ] Cannot assign to CLOSED job
- [ ] Cannot assign to full job (headcount limit)
- [ ] Job headcount increments on assignment
- [ ] Job headcount decrements on cancel
- [ ] Candidate cannot transition to PLACED without active assignment
- [ ] Frontend shows assignment panel
- [ ] Can assign candidate via UI
- [ ] Can cancel assignment via UI
- [ ] Assignment history displays correctly

## API Usage Examples

### Create Assignment
```bash
POST /api/assignments
Content-Type: application/json
Authorization: Bearer <token>

{
  "candidateId": 1,
  "jobOrderId": 5,
  "notes": "Strong technical background"
}
```

### Get Candidate Assignments
```bash
GET /api/assignments/candidate/1
Authorization: Bearer <token>
```

### Cancel Assignment
```bash
DELETE /api/assignments/123
Authorization: Bearer <token>
```

## Rollback Plan
If issues arise:
1. Restore database backup
2. Git revert to previous commit
3. The old code still has `jobOrder` relationship in git history

## Next Steps
After successful deployment:
1. Remove migration SQL scripts
2. Update API documentation
3. Train staff on new assignment workflow
4. Monitor assignment creation logs for business rule violations
