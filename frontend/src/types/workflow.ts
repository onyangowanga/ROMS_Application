// DTO returned by /api/applicant/workflow
export interface CandidateWorkflowDTO {
  status: string;
  stageTitle: string;
  stageDescription: string;
  blocked: boolean;
  blockReason: string | null;
  missingDocuments: string[];
}
