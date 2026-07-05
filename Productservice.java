package com.trustchain.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trustchain.backend.dto.TransferRequest;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.EndorseException;
import org.hyperledger.fabric.client.GatewayException;
import org.hyperledger.fabric.client.SubmitException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class ProductService {

    private final FabricGatewayService fabricGatewayService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProductService(FabricGatewayService fabricGatewayService) {
        this.fabricGatewayService = fabricGatewayService;
    }

    public String createProduct(String productId, String name, String batchNumber,
                                 String manufacturerId, String certificateHash) throws Exception {
        Contract contract = fabricGatewayService.getContract();
        byte[] result = contract.submitTransaction(
                "CreateProduct", productId, name, batchNumber, manufacturerId, certificateHash);
        return new String(result, StandardCharsets.UTF_8);
    }

    /**
     * Transfers ownership of a product. `callerId` is taken from the authenticated
     * JWT principal on the controller side -- never trust a client-supplied "from" field.
     */
    public String transferOwnership(String productId, String callerId, TransferRequest req)
            throws EndorseException, SubmitException, GatewayException, InterruptedException {
        Contract contract = fabricGatewayService.getContract();
        byte[] result = contract.submitTransaction(
                "TransferOwnership",
                productId,
                callerId,
                req.getNewOwnerId(),
                req.getNewOwnerRole(),
                req.getNewStatus()
        );
        return new String(result, StandardCharsets.UTF_8);
    }

    public String getProduct(String productId) throws GatewayException {
        Contract contract = fabricGatewayService.getContract();
        byte[] result = contract.evaluateTransaction("GetProduct", productId);
        return new String(result, StandardCharsets.UTF_8);
    }

    public String getProductHistory(String productId) throws GatewayException {
        Contract contract = fabricGatewayService.getContract();
        byte[] result = contract.evaluateTransaction("GetProductHistory", productId);
        return new String(result, StandardCharsets.UTF_8);
    }
}
