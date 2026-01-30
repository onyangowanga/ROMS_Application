package com.roms.enums;

/**
 * Transaction types for the immutable payment ledger
 * Phase 2B: Agency Commission Management
 */
public enum TransactionType {
    /**
     * Initial downpayment toward agency commission (typically 50,000 KES)
     * MUST be paid before visa processing can begin
     */
    AGENCY_COMMISSION_DOWNPAYMENT,
    
    /**
     * Installment payment toward remaining agency commission balance
     */
    AGENCY_COMMISSION_INSTALLMENT,
    
    /**
     * Final balance payment to complete agency commission
     */
    AGENCY_COMMISSION_BALANCE,
    
    /**
     * Reversal entry (negative amount) linked to original transaction
     * Used for error correction - original transaction is never deleted/modified
     */
    REVERSAL
}
