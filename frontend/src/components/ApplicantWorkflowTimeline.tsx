import React, { useEffect, useState } from 'react';
import { candidateApi } from '../api/candidates';
import { CandidateWorkflowDTO } from '../types/workflow';
import { useAuth } from '../context/AuthContext';

const WORKFLOW_STEPS = [
  'Application Submitted',
  'Documents Under Review',
  'Documents Insufficient',
  'Documents Approved',
  'Interview Scheduled',
  'Interview Passed',
  'Medical Pending',
  'Medical Passed',
  'Visa Processing',
  'Offer Issued',
  'Offer Accepted',
  'Deployment Pending',
  'Placed',
  'Rejected',
];

export const ApplicantWorkflowTimeline: React.FC = () => {
  const { user } = useAuth();
  const [workflow, setWorkflow] = useState<CandidateWorkflowDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (user?.email) {
      candidateApi.getApplicantWorkflow(user.email)
        .then(setWorkflow)
        .catch((err: any) => setError('Failed to load workflow: ' + (err.response?.data?.message || err.message)))
        .finally(() => setLoading(false));
    }
  }, [user]);

  if (loading) return <div className="p-6">Loading workflow...</div>;
  if (error) return <div className="p-6 text-red-600">{error}</div>;
  if (!workflow) return <div className="p-6">No workflow data available.</div>;

  const currentStepIdx = WORKFLOW_STEPS.findIndex(
    (step) => step === workflow.stageTitle
  );

  return (
    <div className="bg-white shadow rounded-lg p-6">
      <h2 className="text-lg font-semibold mb-4">Application Workflow</h2>
      <ol className="relative border-l border-gray-300 ml-4">
        {WORKFLOW_STEPS.map((step, idx) => (
          <li key={step} className="mb-8 ml-6">
            <span
              className={`absolute -left-3 flex items-center justify-center w-6 h-6 rounded-full border-2 ${
                idx < currentStepIdx
                  ? 'bg-green-500 border-green-500 text-white'
                  : idx === currentStepIdx
                  ? 'bg-blue-600 border-blue-600 text-white animate-pulse'
                  : 'bg-gray-200 border-gray-300 text-gray-400'
              }`}
            >
              {idx < currentStepIdx ? '✓' : idx + 1}
            </span>
            <div className="flex flex-col">
              <span className={`font-medium ${idx === currentStepIdx ? 'text-blue-700' : ''}`}>{step}</span>
              {idx === currentStepIdx && (
                <span className="text-xs text-gray-600 mt-1">{workflow.stageDescription}</span>
              )}
            </div>
          </li>
        ))}
      </ol>
      {workflow.blocked && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mt-6">
          <strong className="font-bold">Workflow Blocked:</strong> {workflow.blockReason}
        </div>
      )}
      {workflow.missingDocuments && workflow.missingDocuments.length > 0 && (
        <div className="bg-yellow-100 border border-yellow-400 text-yellow-800 px-4 py-3 rounded relative mt-4">
          <strong className="font-bold">Missing Documents:</strong>
          <ul className="list-disc ml-6 mt-1">
            {workflow.missingDocuments.map((doc) => (
              <li key={doc}>{doc.replace(/_/g, ' ')}</li>
            ))}
          </ul>
        </div>
      )}
      {workflow.status === 'OFFER_ISSUED' && !workflow.blocked && (
        <button className="mt-6 px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700">
          Accept Offer
        </button>
      )}
    </div>
  );
};

export default ApplicantWorkflowTimeline;
