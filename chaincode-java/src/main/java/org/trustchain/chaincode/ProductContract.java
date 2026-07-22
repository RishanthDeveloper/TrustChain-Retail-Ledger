package org.trustchain.chaincode;

import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyModification;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Contract(
        name = "ProductContract",
        info = @Info(
                title = "TrustChain Retail Ledger Product Contract",
                description = "Handles product creation, ownership transfer and traceability",
                version = "1.0.0",
                license = @License(name = "Apache-2.0"),
                contact = @Contact(email = "dev@trustchain.example", name = "TrustChain Team")
        )
)
@Default
public final class ProductContract implements ContractInterface {

    private final Genson genson = new Genson();

    private static final String PRODUCT_PREFIX = "PRODUCT_";

    private enum ProductError {
        PRODUCT_NOT_FOUND,
        PRODUCT_ALREADY_EXISTS,
        INVALID_OWNER,
        UNAUTHORIZED_TRANSFER
    }

    private String productKey(String productId) {
        return PRODUCT_PREFIX + productId;
    }

    private Product readProduct(ChaincodeStub stub, String productId) {
        String state = stub.getStringState(productKey(productId));
        if (state == null || state.isEmpty()) {
            throw new ChaincodeException("Product " + productId + " does not exist",
                    ProductError.PRODUCT_NOT_FOUND.toString());
        }
        return genson.deserialize(state, Product.class);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Product CreateProduct(final Context ctx,
                                  final String productId,
                                  final String name,
                                  final String batchNumber,
                                  final String manufacturerId,
                                  final String certificateHash) {
        ChaincodeStub stub = ctx.getStub();

        String existing = stub.getStringState(productKey(productId));
        if (existing != null && !existing.isEmpty()) {
            throw new ChaincodeException("Product " + productId + " already exists",
                    ProductError.PRODUCT_ALREADY_EXISTS.toString());
        }

        long now = ctx.getStub().getTxTimestamp().getEpochSecond();

        Product product = new Product(
                productId,
                name,
                batchNumber,
                manufacturerId,
                manufacturerId,
                "MANUFACTURER",
                "CREATED",
                now,
                now,
                certificateHash
        );

        stub.putStringState(productKey(productId), genson.serialize(product));
        stub.setEvent("ProductCreated", genson.serialize(product).getBytes(StandardCharsets.UTF_8));

        return product;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Product TransferOwnership(final Context ctx,
                                      final String productId,
                                      final String callerId,
                                      final String newOwnerId,
                                      final String newOwnerRole,
                                      final String newStatus) {
        ChaincodeStub stub = ctx.getStub();
        Product product = readProduct(stub, productId);

        if (!product.getCurrentOwnerId().equals(callerId)) {
            throw new ChaincodeException(
                    "Caller " + callerId + " is not the current owner of product " + productId,
                    ProductError.UNAUTHORIZED_TRANSFER.toString());
        }

        if (newOwnerId == null || newOwnerId.isEmpty()) {
            throw new ChaincodeException("New owner id must not be empty",
                    ProductError.INVALID_OWNER.toString());
        }

        product.setCurrentOwnerId(newOwnerId);
        product.setCurrentOwnerRole(newOwnerRole);
        product.setStatus(newStatus);
        product.setUpdatedAt(ctx.getStub().getTxTimestamp().getEpochSecond());

        stub.putStringState(productKey(productId), genson.serialize(product));
        stub.setEvent("OwnershipTransferred", genson.serialize(product).getBytes(StandardCharsets.UTF_8));

        return product;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Product GetProduct(final Context ctx, final String productId) {
        return readProduct(ctx.getStub(), productId);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetProductHistory(final Context ctx, final String productId) {
        ChaincodeStub stub = ctx.getStub();
        readProduct(stub, productId);

        List<TransferRecord> history = new ArrayList<>();
        try (var results = stub.getHistoryForKey(productKey(productId))) {
            for (KeyModification mod : results) {
                if (mod.isDeleted()) {
                    continue;
                }
                Product snapshot = genson.deserialize(mod.getStringValue(), Product.class);
                history.add(new TransferRecord(
                        mod.getTxId(),
                        null,
                        snapshot.getCurrentOwnerId(),
                        snapshot.getCurrentOwnerRole(),
                        snapshot.getStatus(),
                        mod.getTimestamp().getEpochSecond()
                ));
            }
        }
        return genson.serialize(history);
    }
}
