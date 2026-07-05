package org.trustchain.chaincode;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

/**
 * A single ownership-transfer event, used to reconstruct full traceability history
 * from the chaincode's GetHistoryForKey call combined with parsed product states.
 */
@DataType()
public final class TransferRecord {

    @Property() private final String txId;
    @Property() private final String fromOwnerId;
    @Property() private final String toOwnerId;
    @Property() private final String toOwnerRole;
    @Property() private final String status;
    @Property() private final long timestamp;

    public TransferRecord(@JsonProperty("txId") String txId,
                           @JsonProperty("fromOwnerId") String fromOwnerId,
                           @JsonProperty("toOwnerId") String toOwnerId,
                           @JsonProperty("toOwnerRole") String toOwnerRole,
                           @JsonProperty("status") String status,
                           @JsonProperty("timestamp") long timestamp) {
        this.txId = txId;
        this.fromOwnerId = fromOwnerId;
        this.toOwnerId = toOwnerId;
        this.toOwnerRole = toOwnerRole;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getTxId() { return txId; }
    public String getFromOwnerId() { return fromOwnerId; }
    public String getToOwnerId() { return toOwnerId; }
    public String getToOwnerRole() { return toOwnerRole; }
    public String getStatus() { return status; }
    public long getTimestamp() { return timestamp; }
}
