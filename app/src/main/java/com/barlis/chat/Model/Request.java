package com.barlis.chat.Model;

import java.io.Serializable;

public class Request implements Serializable {
    private String requestTitle;
    private String requestDetails;
    private String creatorId;
    private String creatorName;
    private String workerId;
    private String workerName;
    private String requiredProfession;
    private String qualifications;
    private String notes;
    private ERequestStatus status;
    private String requestId;

    public Request() {
    }

    public Request(String requestTitle, String requestDetails, String creatorId, String requiredProfession, String creatorName, String qualifications, String notes) {
        this.requestTitle = requestTitle;
        this.requestDetails = requestDetails;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.requiredProfession = requiredProfession;
        this.qualifications = qualifications;
        this.notes = notes;
        status = ERequestStatus.REQUEST_AVAILABLE;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestTitle() {
        return requestTitle;
    }

    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }

    public String getNotes() {
        return notes;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getRequestDetails() {
        return requestDetails;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getRequiredProfession() {
        return requiredProfession;
    }

    public ERequestStatus getStatus() {
        return status;
    }

    public void setStatus(ERequestStatus status) {
        this.status = status;
    }
}
