import React, { useState } from 'react';
import commissionApi, { CommissionAgreement, CommissionPaymentRequest } from '../api/commission';

interface CommissionManagementProps {
  candidateId: number;
  assignmentId: number;
  onUpdate?: () => void;
}

const CommissionManagement: React.FC<CommissionManagementProps> = ({
  candidateId,

  assignmentId,
  onUpdate,
}) => {
  const [activeTab, setActiveTab] = useState<'agreement' | 'payment'>('agreement');
  const [agreement, setAgreement] = useState<CommissionAgreement | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Agreement form
  const [totalCommission, setTotalCommission] = useState('200000');
  const [downpayment, setDownpayment] = useState('50000');
  const [notes, setNotes] = useState('');

  // Payment form
  const [paymentAmount, setPaymentAmount] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('M-PESA');
  const [mpesaRef, setMpesaRef] = useState('');
  const [description, setDescription] = useState('');

  const loadAgreement = async () => {
    try {
      setLoading(true);
      const response = await commissionApi.getAssignmentAgreement(assignmentId);
      if (response.data.data) {
        setAgreement(response.data.data);
      }
    } catch (err: any) {
      console.error('Error loading agreement:', err);
    } finally {
      setLoading(false);
    }
  };

  React.useEffect(() => {
    loadAgreement();
  }, [assignmentId]);

  const handleCreateAgreement = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    try {
      setLoading(true);
      const response = await commissionApi.createAgreement({
        candidateId,
        assignmentId,
        totalCommissionAmount: parseFloat(totalCommission),
        requiredDownpaymentAmount: parseFloat(downpayment),
        currency: 'KES',
        notes,
      });

      setAgreement(response.data.data);
      setSuccess('Commission agreement created successfully!');
      onUpdate?.();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create agreement');
    } finally {
      setLoading(false);
    }
  };

  const handleRecordPayment = async (e: React.FormEvent, isDownpayment: boolean) => {
    e.preventDefault();
    if (!agreement) return;

    setError(null);
    setSuccess(null);

    const paymentRequest: CommissionPaymentRequest = {
      agreementId: agreement.id,
      amount: parseFloat(paymentAmount),
      paymentMethod,
      mpesaRef: mpesaRef || undefined,
      description: description || undefined,
    };

    try {
      setLoading(true);
      if (isDownpayment) {
        await commissionApi.recordDownpayment(paymentRequest);
        setSuccess('Downpayment recorded successfully!');
      } else {
        await commissionApi.recordInstallment(paymentRequest);
        setSuccess('Installment recorded successfully!');
      }

      setPaymentAmount('');
      setMpesaRef('');
      setDescription('');
      loadAgreement();
      onUpdate?.();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to record payment');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-KE', {
      style: 'currency',
      currency: 'KES',
      minimumFractionDigits: 0,
    }).format(amount);
  };

  return (
    <div className="bg-white rounded-lg shadow">
      {/* Tabs */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex">
          <button
            onClick={() => setActiveTab('agreement')}
            className={`${
              activeTab === 'agreement'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            } whitespace-nowrap py-4 px-6 border-b-2 font-medium text-sm`}
          >
            Agreement
          </button>
          <button
            onClick={() => setActiveTab('payment')}
            className={`${
              activeTab === 'payment'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            } whitespace-nowrap py-4 px-6 border-b-2 font-medium text-sm`}
            disabled={!agreement}
          >
            Record Payment
          </button>
        </nav>
      </div>

      {/* Content */}
      <div className="p-6">
        {error && (
          <div className="mb-4 bg-red-50 border border-red-200 rounded-lg p-4">
            <p className="text-red-800">{error}</p>
          </div>
        )}

        {success && (
          <div className="mb-4 bg-green-50 border border-green-200 rounded-lg p-4">
            <p className="text-green-800">{success}</p>
          </div>
        )}

        {activeTab === 'agreement' && (
          <div>
            {agreement ? (
              <div className="space-y-4">
                <div className="bg-gray-50 rounded-lg p-4">
                  <h3 className="font-semibold text-gray-900 mb-4">Agreement Details</h3>
                  <dl className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <dt className="text-sm text-gray-600">Candidate</dt>
                      <dd className="text-lg font-medium text-gray-900">{agreement.candidateName}</dd>
                    </div>
                    <div>
                      <dt className="text-sm text-gray-600">Status</dt>
                      <dd>
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                          agreement.status === 'ACTIVE' ? 'bg-green-100 text-green-800' :
                          agreement.status === 'COMPLETED' ? 'bg-blue-100 text-blue-800' :
                          'bg-gray-100 text-gray-800'
                        }`}>
                          {agreement.status}
                        </span>
                      </dd>
                    </div>
                    <div>
                      <dt className="text-sm text-gray-600">Total Commission</dt>
                      <dd className="text-lg font-medium text-gray-900">{formatCurrency(agreement.totalCommissionAmount)}</dd>
                    </div>
                    <div>
                      <dt className="text-sm text-gray-600">Required Downpayment</dt>
                      <dd className="text-lg font-medium text-gray-900">{formatCurrency(agreement.requiredDownpaymentAmount)}</dd>
                    </div>
                    <div>
                      <dt className="text-sm text-gray-600">Total Paid</dt>
                      <dd className="text-lg font-medium text-green-600">{formatCurrency(agreement.totalPaid)}</dd>
                    </div>
                    <div>
                      <dt className="text-sm text-gray-600">Outstanding Balance</dt>
                      <dd className="text-lg font-medium text-red-600">{formatCurrency(agreement.outstandingBalance)}</dd>
                    </div>
                    <div>
                      <dt className="text-sm text-gray-600">Signed</dt>
                      <dd className="text-lg font-medium">{agreement.signed ? '✓ Yes' : '✗ No'}</dd>
                    </div>
                  </dl>
                </div>
              </div>
            ) : (
              <form onSubmit={handleCreateAgreement} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Total Commission Amount (KES)
                  </label>
                  <input
                    type="number"
                    value={totalCommission}
                    onChange={(e) => setTotalCommission(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500"
                    required
                    min="1"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Required Downpayment (KES)
                  </label>
                  <input
                    type="number"
                    value={downpayment}
                    onChange={(e) => setDownpayment(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500"
                    required
                    min="1"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Notes (Optional)
                  </label>
                  <textarea
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500"
                    rows={3}
                  />
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="w-full bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
                >
                  {loading ? 'Creating...' : 'Create Commission Agreement'}
                </button>
              </form>
            )}
          </div>
        )}

        {activeTab === 'payment' && agreement && (
          <form className="space-y-4">
            <div className="bg-blue-50 rounded-lg p-4 mb-4">
              <p className="text-sm text-blue-800">
                <strong>Outstanding Balance:</strong> {formatCurrency(agreement.outstandingBalance)}
              </p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Payment Amount (KES)
              </label>
              <input
                type="number"
                value={paymentAmount}
                onChange={(e) => setPaymentAmount(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500"
                required
                min="1"
                max={agreement.outstandingBalance}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Payment Method
              </label>
              <select
                value={paymentMethod}
                onChange={(e) => setPaymentMethod(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="M-PESA">M-PESA</option>
                <option value="Bank Transfer">Bank Transfer</option>
                <option value="Cash">Cash</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                M-PESA Reference (Optional)
              </label>
              <input
                type="text"
                value={mpesaRef}
                onChange={(e) => setMpesaRef(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500"
                placeholder="e.g., QGH7X8Y9Z0"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Description (Optional)
              </label>
              <input
                type="text"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500"
                placeholder="e.g., Partial payment installment 1"
              />
            </div>

            <div className="flex gap-4">
              {agreement.totalPaid < agreement.requiredDownpaymentAmount ? (
                <button
                  type="button"
                  onClick={(e) => handleRecordPayment(e, true)}
                  disabled={loading || !paymentAmount}
                  className="flex-1 bg-orange-600 text-white py-2 px-4 rounded-lg hover:bg-orange-700 disabled:bg-gray-400"
                >
                  {loading ? 'Recording...' : 'Record Downpayment'}
                </button>
              ) : (
                <button
                  type="button"
                  onClick={(e) => handleRecordPayment(e, false)}
                  disabled={loading || !paymentAmount}
                  className="flex-1 bg-green-600 text-white py-2 px-4 rounded-lg hover:bg-green-700 disabled:bg-gray-400"
                >
                  {loading ? 'Recording...' : 'Record Installment'}
                </button>
              )}
            </div>
          </form>
        )}
      </div>
    </div>
  );
};

export default CommissionManagement;
