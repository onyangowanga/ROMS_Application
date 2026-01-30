import React, { useEffect, useState } from 'react';
import commissionApi, { CommissionStatement } from '../api/commission';

interface CommissionSummaryProps {
  candidateId: number;
}

const CommissionSummary: React.FC<CommissionSummaryProps> = ({ candidateId }) => {
  const [statement, setStatement] = useState<CommissionStatement | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadStatement();
  }, [candidateId]);

  const loadStatement = async () => {
    try {
      setLoading(true);
      // First get candidate's agreements
      const agreementsResponse = await commissionApi.getCandidateAgreements(candidateId);
      const agreements = agreementsResponse.data.data;
      
      if (!agreements || agreements.length === 0) {
        setStatement(null);
        setError(null);
        setLoading(false);
        return;
      }

      // Get the first active agreement
      const activeAgreement = agreements.find((a: any) => a.status === 'ACTIVE') || agreements[0];
      
      // Get statement for this agreement
      const response = await commissionApi.getCandidateStatement(candidateId, activeAgreement.id);
      setStatement(response.data.data);
      setError(null);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load commission statement');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-KE', {
      style: 'currency',
      currency: 'KES',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  if (loading) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <div className="animate-pulse">
          <div className="h-4 bg-gray-200 rounded w-1/4 mb-4"></div>
          <div className="h-4 bg-gray-200 rounded w-1/2 mb-2"></div>
          <div className="h-4 bg-gray-200 rounded w-1/3"></div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-4">
        <p className="text-red-800">{error}</p>
      </div>
    );
  }

  if (!statement) {
    return null;
  }

  const progressPercentage = (statement.totalPaid / statement.totalCommissionAmount) * 100;

  return (
    <div className="bg-white rounded-lg shadow-lg p-6 space-y-6">
      {/* Header */}
      <div className="border-b pb-4">
        <h2 className="text-2xl font-bold text-gray-900">Agency Commission</h2>
        <p className="text-sm text-gray-600">Payment summary for {statement.candidateName}</p>
      </div>

      {/* Commission Overview */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-blue-50 rounded-lg p-4">
          <p className="text-sm text-blue-600 font-medium">Total Commission</p>
          <p className="text-3xl font-bold text-blue-900">{formatCurrency(statement.totalCommissionAmount)}</p>
        </div>

        <div className="bg-orange-50 rounded-lg p-4">
          <p className="text-sm text-orange-600 font-medium">Required Downpayment</p>
          <p className="text-3xl font-bold text-orange-900">{formatCurrency(statement.requiredDownpaymentAmount)}</p>
        </div>

        <div className="bg-green-50 rounded-lg p-4">
          <p className="text-sm text-green-600 font-medium">Total Paid</p>
          <p className="text-3xl font-bold text-green-900">{formatCurrency(statement.totalPaid)}</p>
          {statement.downpaymentComplete && (
            <span className="inline-flex items-center px-2 py-1 rounded text-xs font-medium bg-green-100 text-green-800 mt-2">
              ✓ Downpayment Complete
            </span>
          )}
        </div>

        <div className="bg-red-50 rounded-lg p-4">
          <p className="text-sm text-red-600 font-medium">Outstanding Balance</p>
          <p className="text-3xl font-bold text-red-900">{formatCurrency(statement.outstandingBalance)}</p>
          {statement.fullPaymentComplete && (
            <span className="inline-flex items-center px-2 py-1 rounded text-xs font-medium bg-green-100 text-green-800 mt-2">
              ✓ Fully Paid
            </span>
          )}
        </div>
      </div>

      {/* Progress Bar */}
      <div>
        <div className="flex justify-between text-sm mb-2">
          <span className="text-gray-600">Payment Progress</span>
          <span className="font-medium text-gray-900">{progressPercentage.toFixed(1)}%</span>
        </div>
        <div className="w-full bg-gray-200 rounded-full h-4">
          <div
            className="bg-gradient-to-r from-blue-500 to-green-500 h-4 rounded-full transition-all duration-500"
            style={{ width: `${Math.min(progressPercentage, 100)}%` }}
          ></div>
        </div>
      </div>

      {/* Workflow Status Alerts */}
      {!statement.downpaymentComplete && (
        <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-sm text-yellow-700">
                <strong>Visa processing is locked.</strong> Complete the required downpayment of {formatCurrency(statement.requiredDownpaymentAmount)} to unlock visa processing.
              </p>
            </div>
          </div>
        </div>
      )}

      {statement.downpaymentComplete && !statement.fullPaymentComplete && (
        <div className="bg-blue-50 border-l-4 border-blue-400 p-4">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-blue-400" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-sm text-blue-700">
                <strong>Final placement is locked.</strong> Complete the remaining balance of {formatCurrency(statement.outstandingBalance)} to unlock placement.
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Payment Ledger with Running Balance */}
      <div>
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Payment Ledger</h3>
        {statement.paymentHistory.length === 0 ? (
          <p className="text-gray-500 text-center py-8">No payments recorded yet</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Reference</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Total Paid</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Balance</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {statement.paymentHistory.map((payment, index) => {
                  // Calculate cumulative total paid up to this point
                  const cumulativePaid = statement.paymentHistory
                    .slice(0, index + 1)
                    .reduce((sum, p) => sum + (p.type === 'DEBIT' ? p.amount : -p.amount), 0);
                  const remainingBalance = statement.totalCommissionAmount - cumulativePaid;
                  
                  return (
                    <tr key={payment.id} className={payment.isReversal ? 'bg-red-50' : ''}>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {formatDate(payment.paymentDate)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex px-2 text-xs leading-5 font-semibold rounded-full ${
                          payment.isReversal 
                            ? 'bg-red-100 text-red-800'
                            : payment.transactionType === 'AGENCY_COMMISSION_DOWNPAYMENT'
                            ? 'bg-orange-100 text-orange-800'
                            : 'bg-blue-100 text-blue-800'
                        }`}>
                          {payment.isReversal ? 'REVERSAL' : payment.transactionType.replace('AGENCY_COMMISSION_', '')}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {payment.transactionRef}
                      </td>
                      <td className={`px-6 py-4 whitespace-nowrap text-sm text-right font-medium ${
                        payment.type === 'DEBIT' ? 'text-green-600' : 'text-red-600'
                      }`}>
                        {payment.type === 'DEBIT' ? '+' : '-'}{formatCurrency(Math.abs(payment.amount))}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-right font-semibold text-blue-600">
                        {formatCurrency(cumulativePaid)}
                      </td>
                      <td className={`px-6 py-4 whitespace-nowrap text-sm text-right font-semibold ${
                        remainingBalance > 0 ? 'text-orange-600' : 'text-green-600'
                      }`}>
                        {formatCurrency(remainingBalance)}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default CommissionSummary;
