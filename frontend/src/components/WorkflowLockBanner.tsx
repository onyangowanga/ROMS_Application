import React, { useEffect, useState } from 'react';
import commissionApi from '../api/commission';

interface WorkflowLockBannerProps {
  assignmentId: number;
  currentStatus: string;
}

const WorkflowLockBanner: React.FC<WorkflowLockBannerProps> = ({ assignmentId, currentStatus }) => {
  const [downpaymentComplete, setDownpaymentComplete] = useState(false);
  const [fullPaymentComplete, setFullPaymentComplete] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadPaymentStatus();
  }, [assignmentId]);

  const loadPaymentStatus = async () => {
    try {
      setLoading(true);
      const [downpaymentRes, fullPaymentRes] = await Promise.all([
        commissionApi.checkDownpaymentStatus(assignmentId),
        commissionApi.checkFullPaymentStatus(assignmentId),
      ]);
      setDownpaymentComplete(downpaymentRes.data.data);
      setFullPaymentComplete(fullPaymentRes.data.data);
    } catch (err) {
      console.error('Error loading payment status:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return null;
  }

  // Show visa lock if trying to process visa without downpayment
  const showVisaLock = 
    currentStatus === 'OFFER_SIGNED' && !downpaymentComplete;

  // Show placement lock if trying to place without full payment
  const showPlacementLock = 
    (currentStatus === 'VISA_PROCESSING' || 
     currentStatus === 'VISA_APPROVED' || 
     currentStatus === 'DEPLOYED') && 
    !fullPaymentComplete;

  if (!showVisaLock && !showPlacementLock) {
    return null;
  }

  return (
    <div className="space-y-4 mb-6">
      {showVisaLock && (
        <div className="bg-red-50 border-l-4 border-red-400 p-4 rounded-lg shadow-md animate-pulse mb-4">
          <div className="flex items-start">
            <div className="flex-shrink-0">
              <svg className="h-6 w-6 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
              </svg>
            </div>
            <div className="ml-3 flex-1">
              <h3 className="text-lg font-bold text-red-800">🚫 Visa Processing Locked</h3>
              <p className="mt-2 text-sm text-red-700">
                <strong>Downpayment required before visa processing can begin.</strong>
              </p>
              <p className="mt-1 text-sm text-red-600">
                The candidate must complete the required downpayment through the commission agreement 
                before visa processing workflow can proceed. Contact the operations team to arrange payment.
              </p>
            </div>
          </div>
        </div>
      )}

      {showPlacementLock && (
        <div className="bg-orange-50 border-l-4 border-orange-400 p-4 rounded-lg shadow-md">
          <div className="flex items-start">
            <div className="flex-shrink-0">
              <svg className="h-6 w-6 text-orange-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
              </svg>
            </div>
            <div className="ml-3 flex-1">
              <h3 className="text-lg font-bold text-orange-800">🚫 Final Placement Locked</h3>
              <p className="mt-2 text-sm text-orange-700">
                <strong>Full commission payment required before final placement.</strong>
              </p>
              <p className="mt-1 text-sm text-orange-600">
                The outstanding balance must be paid in full before the candidate can be marked as PLACED. 
                Payment installments are accepted. Contact the finance team to complete payment.
              </p>
            </div>
          </div>
        </div>
      )}

      {downpaymentComplete && !fullPaymentComplete && (
        <div className="bg-blue-50 border-l-4 border-blue-400 p-4 rounded-lg">
          <div className="flex items-start">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-blue-400" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3 flex-1">
              <h3 className="text-sm font-medium text-blue-800">✓ Downpayment Complete - Visa Processing Unlocked</h3>
              <p className="mt-1 text-sm text-blue-700">
                Visa processing can now proceed. Outstanding balance must be paid before final placement.
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default WorkflowLockBanner;
