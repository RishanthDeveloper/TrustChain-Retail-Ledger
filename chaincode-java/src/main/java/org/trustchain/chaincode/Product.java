package org.trustchain.chaincode;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

/**
 * Represents a single product "digital twin" on the ledger.
 * Stored as JSON under key: PRODUCT_<productId>
 */
@DataType()
public final class Product {

    @Property()
    private final String productId;

    @Property()
    private final String name;

    @Property()
    private final String batchNumber;

    @Property()
    private final String originManufacturerId;

    @Property()
    private String currentOwnerId;

    @Property()
    private String currentOwnerRole; // MANUFACTURER, WHOLESALER, RETAILER, CONSUMER

    @Property()
    private String status; // CREATED, IN_TRANSIT, DELIVERED, SOLD

    @Property()
    private final long createdAt;

    @Property()
    private long updatedAt;

    @Property()
    private final String certificateHash; // hash of off-chain quality/origin cert stored in MongoDB

    public Product(@JsonProperty("productId") String productId,
                    @JsonProperty("name") String name,
                    @JsonProperty("batchNumber") String batchNumber,
                    @JsonProperty("originManufacturerId") String originManufacturerId,
                    @JsonProperty("currentOwnerId") String currentOwnerId,
                    @JsonProperty("currentOwnerRole") String currentOwnerRole,
                    @JsonProperty("status") String status,
                    @JsonProperty("createdAt") long createdAt,
                    @JsonProperty("updatedAt") long updatedAt,
                    @JsonProperty("certificateHash") String certificateHash) {
        this.productId = productId;
        this.name = name;
        this.batchNumber = batchNumber;
        this.originManufacturerId = originManufacturerId;
        this.currentOwnerId = currentOwnerId;
        this.currentOwnerRole = currentOwnerRole;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.certificateHash = certificateHash;
    }

    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getBatchNumber() { return batchNumber; }
    public String getOriginManufacturerId() { return originManufacturerId; }
    public String getCurrentOwnerId() { return currentOwnerId; }
    public String getCurrentOwnerRole() { return currentOwnerRole; }
    public String getStatus() { return status; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public String getCertificateHash() { return certificateHash; }

    public void setCurrentOwnerId(String currentOwnerId) { this.currentOwnerId = currentOwnerId; }
    public void setCurrentOwnerRole(String currentOwnerRole) { this.currentOwnerRole = currentOwnerRole; }
    public void setStatus(String status) { this.status = status; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
