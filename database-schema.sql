-- ================================================
-- ROMS Database Schema
-- Recruitment Operations Management System
-- PostgreSQL 15+
-- ================================================

-- Enable UUID extension (optional, for future use)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ================================================
-- INDEXES FOR SOFT DELETE AND PERFORMANCE
-- ================================================

-- Partial unique index for active candidates (soft delete support)
-- Allows re-registration of same passport after deletion
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_active_passport 
ON candidates (passport_no) 
WHERE (deleted_at IS NULL);

-- Performance indexes
CREATE INDEX IF NOT EXISTS idx_candidates_status ON candidates(current_status) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_candidates_ref ON candidates(internal_ref_no);
CREATE INDEX IF NOT EXISTS idx_job_orders_ref ON job_orders(job_order_ref);
CREATE INDEX IF NOT EXISTS idx_job_orders_employer ON job_orders(employer_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_payments_candidate ON payments(candidate_id);
CREATE INDEX IF NOT EXISTS idx_payments_date ON payments(payment_date);
CREATE INDEX IF NOT EXISTS idx_documents_candidate ON candidate_documents(candidate_id) WHERE deleted_at IS NULL;

-- ================================================
-- VIEWS FOR COMMON QUERIES
-- ================================================

-- Active candidates with full details
CREATE OR REPLACE VIEW v_active_candidates AS
SELECT 
    c.id,
    c.internal_ref_no,
    CONCAT(c.first_name, ' ', COALESCE(c.middle_name || ' ', ''), c.last_name) as full_name,
    c.passport_no,
    c.passport_expiry,
    c.email,
    c.phone_number,
    c.current_status,
    c.medical_status,
    c.expected_position,
    j.job_title,
    j.job_order_ref,
    e.company_name as employer_name,
    c.created_at,
    c.last_modified_at
FROM candidates c
LEFT JOIN job_orders j ON c.job_order_id = j.id
LEFT JOIN employers e ON j.employer_id = e.id
WHERE c.deleted_at IS NULL;

-- Candidate payment balance summary
CREATE OR REPLACE VIEW v_candidate_balances AS
SELECT 
    c.id as candidate_id,
    c.internal_ref_no,
    CONCAT(c.first_name, ' ', c.last_name) as candidate_name,
    COALESCE(SUM(
        CASE 
            WHEN p.type = 'DEBIT' AND p.is_reversal = false THEN p.amount
            WHEN p.type = 'CREDIT' AND p.is_reversal = false THEN -p.amount
            ELSE 0
        END
    ), 0) as balance,
    COUNT(p.id) as transaction_count,
    MAX(p.payment_date) as last_payment_date
FROM candidates c
LEFT JOIN payments p ON c.id = p.candidate_id
WHERE c.deleted_at IS NULL
GROUP BY c.id, c.internal_ref_no, c.first_name, c.last_name;

-- Job order fulfillment status
CREATE OR REPLACE VIEW v_job_order_fulfillment AS
SELECT 
    j.id,
    j.job_order_ref,
    j.job_title,
    e.company_name,
    j.headcount_required,
    j.headcount_filled,
    (j.headcount_required - j.headcount_filled) as positions_remaining,
    ROUND((j.headcount_filled::decimal / j.headcount_required::decimal) * 100, 2) as fulfillment_percentage,
    j.status,
    j.start_date,
    j.end_date,
    COUNT(c.id) as total_candidates,
    COUNT(CASE WHEN c.current_status = 'PLACED' THEN 1 END) as placed_candidates
FROM job_orders j
JOIN employers e ON j.employer_id = e.id
LEFT JOIN candidates c ON j.id = c.job_order_id AND c.deleted_at IS NULL
WHERE j.deleted_at IS NULL
GROUP BY j.id, j.job_order_ref, j.job_title, e.company_name, j.headcount_required, 
         j.headcount_filled, j.status, j.start_date, j.end_date;

-- ================================================
-- FUNCTIONS
-- ================================================

-- Function to get candidate payment balance
CREATE OR REPLACE FUNCTION get_candidate_balance(p_candidate_id BIGINT)
RETURNS NUMERIC(19,2) AS $$
DECLARE
    v_balance NUMERIC(19,2);
BEGIN
    SELECT COALESCE(SUM(
        CASE 
            WHEN type = 'DEBIT' AND is_reversal = false THEN amount
            WHEN type = 'CREDIT' AND is_reversal = false THEN -amount
            ELSE 0
        END
    ), 0)
    INTO v_balance
    FROM payments
    WHERE candidate_id = p_candidate_id;
    
    RETURN v_balance;
END;
$$ LANGUAGE plpgsql;

-- Function to check if passport is valid for required months
CREATE OR REPLACE FUNCTION is_passport_valid(p_expiry_date DATE, p_months INTEGER DEFAULT 6)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN p_expiry_date > (CURRENT_DATE + (p_months || ' months')::INTERVAL);
END;
$$ LANGUAGE plpgsql;

-- ================================================
-- TRIGGERS
-- ================================================

-- Trigger to auto-update job order status when filled
CREATE OR REPLACE FUNCTION update_job_order_status()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.headcount_filled >= NEW.headcount_required THEN
        NEW.status = 'FILLED';
    ELSIF NEW.headcount_filled > 0 THEN
        NEW.status = 'IN_PROGRESS';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_job_order_status
BEFORE UPDATE ON job_orders
FOR EACH ROW
WHEN (NEW.headcount_filled <> OLD.headcount_filled)
EXECUTE FUNCTION update_job_order_status();

-- ================================================
-- REPORTING QUERIES (Examples)
-- ================================================

-- Daily payment summary
-- SELECT 
--     DATE(payment_date) as payment_day,
--     COUNT(*) as transaction_count,
--     SUM(CASE WHEN type = 'DEBIT' THEN amount ELSE 0 END) as total_debits,
--     SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE 0 END) as total_credits,
--     SUM(CASE WHEN type = 'DEBIT' THEN amount ELSE -amount END) as net_amount
-- FROM payments
-- WHERE is_reversal = false
-- GROUP BY DATE(payment_date)
-- ORDER BY payment_day DESC;

-- Candidate status distribution
-- SELECT 
--     current_status,
--     COUNT(*) as candidate_count,
--     ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 2) as percentage
-- FROM candidates
-- WHERE deleted_at IS NULL
-- GROUP BY current_status
-- ORDER BY candidate_count DESC;

-- Employer performance
-- SELECT 
--     e.company_name,
--     COUNT(DISTINCT j.id) as total_job_orders,
--     SUM(j.headcount_required) as total_positions,
--     SUM(j.headcount_filled) as total_filled,
--     COUNT(DISTINCT c.id) as total_candidates
-- FROM employers e
-- LEFT JOIN job_orders j ON e.id = j.employer_id AND j.deleted_at IS NULL
-- LEFT JOIN candidates c ON j.id = c.job_order_id AND c.deleted_at IS NULL
-- WHERE e.deleted_at IS NULL
-- GROUP BY e.id, e.company_name
-- ORDER BY total_job_orders DESC;
