package com.roms.dto;

import com.roms.enums.CandidateStatus;
import java.util.List;

public class CandidateWorkflowDTO {
    private CandidateStatus status;
    private String stageTitle;
    private String stageDescription;
    private boolean blocked;
    private String blockReason;
    private List<String> missingDocuments;

    public CandidateWorkflowDTO() {}

    public CandidateWorkflowDTO(CandidateStatus status, String stageTitle, String stageDescription, boolean blocked, String blockReason, List<String> missingDocuments) {
        this.status = status;
        this.stageTitle = stageTitle;
        this.stageDescription = stageDescription;
        this.blocked = blocked;
        this.blockReason = blockReason;
        this.missingDocuments = missingDocuments;
    }

    public CandidateStatus getStatus() { return status; }
    public void setStatus(CandidateStatus status) { this.status = status; }

    public String getStageTitle() { return stageTitle; }
    public void setStageTitle(String stageTitle) { this.stageTitle = stageTitle; }

    public String getStageDescription() { return stageDescription; }
    public void setStageDescription(String stageDescription) { this.stageDescription = stageDescription; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public String getBlockReason() { return blockReason; }
    public void setBlockReason(String blockReason) { this.blockReason = blockReason; }

    public List<String> getMissingDocuments() { return missingDocuments; }
    public void setMissingDocuments(List<String> missingDocuments) { this.missingDocuments = missingDocuments; }
}
