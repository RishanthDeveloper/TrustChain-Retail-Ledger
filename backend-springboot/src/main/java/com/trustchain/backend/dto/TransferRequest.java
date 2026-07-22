package com.trustchain.backend.dto;

public class TransferRequest {
    private String callerId;
    private String newOwnerId;
    private String newOwnerRole; // WHOLESALER, RETAILER, CONSUMER
    private String newStatus;    // IN_TRANSIT, DELIVERED, SOLD

    public String getCallerId() { return callerId; }
    public void setCallerId(String callerId) { this.callerId = callerId; }

    public String getNewOwnerId() { return newOwnerId; }
    public void setNewOwnerId(String newOwnerId) { this.newOwnerId = newOwnerId; }

    public String getNewOwnerRole() { return newOwnerRole; }
    public void setNewOwnerRole(String newOwnerRole) { this.newOwnerRole = newOwnerRole; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
}
