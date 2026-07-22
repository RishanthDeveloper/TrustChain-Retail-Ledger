package com.trustchain.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trustchain.backend.dto.TransferRequest;
import org.hyperledger.fabric.client.Contract;
import org.slf.Logger;
import org.slf.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final FabricGatewayService fabricGatewayService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // In-memory mock ledger store for Dev mode
    private final Map<String, Map<String, Object>> mockLedger = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, Object>>> mockHistory = new ConcurrentHashMap<>();

    public ProductService(FabricGatewayService fabricGatewayService) {
        this.fabricGatewayService = fabricGatewayService;
        initMockData();
    }

    private void initMockData() {
        String p1 = "PROD-1001";
        long now = Instant.now().getEpochSecond() - 3600 * 24 * 3;
        Map<String, Object> prod1 = new HashMap<>();
        prod1.put("productId", p1);
        prod1.put("name", "Organic Extra Virgin Olive Oil 500ml");
        prod1.put("batchNumber", "BATCH-2026-089");
        prod1.put("originManufacturerId", "MANUFACTURER-APEX");
        prod1.put("currentOwnerId", "RETAILER-HEALTHYMARKET");
        prod1.put("currentOwnerRole", "RETAILER");
        prod1.put("status", "DELIVERED");
        prod1.put("createdAt", now);
        prod1.put("updatedAt", Instant.now().getEpochSecond() - 3600 * 4);
        prod1.put("certificateHash", "0xa4f8b92c1e7d3489fe00112233445566778899aabbccddeeff00112233445566");

        mockLedger.put(p1, prod1);

        List<Map<String, Object>> h1 = new ArrayList<>();
        h1.add(Map.of(
                "txId", "0x8f31a20b7c9e4d5f1a3b5c7d9e0f2a4b6c8d0e1f2a3b4c5d6e7f8a9b0c1d2e3f",
                "fromOwnerId", "MANUFACTURER-APEX",
                "toOwnerId", "MANUFACTURER-APEX",
                "toOwnerRole", "MANUFACTURER",
                "status", "CREATED",
                "timestamp", now
        ));
        h1.add(Map.of(
                "txId", "0x1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b",
                "fromOwnerId", "MANUFACTURER-APEX",
                "toOwnerId", "WHOLESALER-GLOBAL",
                "toOwnerRole", "WHOLESALER",
                "status", "IN_TRANSIT",
                "timestamp", now + 3600 * 24
        ));
        h1.add(Map.of(
                "txId", "0x9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f",
                "fromOwnerId", "WHOLESALER-GLOBAL",
                "toOwnerId", "RETAILER-HEALTHYMARKET",
                "toOwnerRole", "RETAILER",
                "status", "DELIVERED",
                "timestamp", Instant.now().getEpochSecond() - 3600 * 4
        ));
        mockHistory.put(p1, h1);
    }

    public String createProduct(String productId, String name, String batchNumber,
                                 String manufacturerId, String certificateHash) throws Exception {
        if (fabricGatewayService.isFabricEnabled()) {
            Contract contract = fabricGatewayService.getContract();
            byte[] result = contract.submitTransaction(
                    "CreateProduct", productId, name, batchNumber, manufacturerId, certificateHash);
            return new String(result, StandardCharsets.UTF_8);
        }

        // Dev Mock Mode
        long now = Instant.now().getEpochSecond();
        Map<String, Object> product = new HashMap<>();
        product.put("productId", productId);
        product.put("name", name);
        product.put("batchNumber", batchNumber);
        product.put("originManufacturerId", manufacturerId);
        product.put("currentOwnerId", manufacturerId);
        product.put("currentOwnerRole", "MANUFACTURER");
        product.put("status", "CREATED");
        product.put("createdAt", now);
        product.put("updatedAt", now);
        product.put("certificateHash", certificateHash);

        mockLedger.put(productId, product);

        List<Map<String, Object>> history = new ArrayList<>();
        history.add(Map.of(
                "txId", "0x" + UUID.randomUUID().toString().replace("-", ""),
                "fromOwnerId", manufacturerId,
                "toOwnerId", manufacturerId,
                "toOwnerRole", "MANUFACTURER",
                "status", "CREATED",
                "timestamp", now
        ));
        mockHistory.put(productId, history);

        return objectMapper.writeValueAsString(product);
    }

    public String transferOwnership(String productId, String callerId, TransferRequest req) throws Exception {
        if (fabricGatewayService.isFabricEnabled()) {
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

        // Dev Mock Mode
        Map<String, Object> product = mockLedger.get(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product " + productId + " does not exist");
        }

        String currentOwner = (String) product.get("currentOwnerId");
        if (callerId != null && !callerId.isEmpty() && !currentOwner.equals(callerId)) {
            log.warn("Transfer callerId {} does not match current owner {}. Proceeding in Dev mode.", callerId, currentOwner);
        }

        long now = Instant.now().getEpochSecond();
        String fromOwner = (String) product.get("currentOwnerId");

        product.put("currentOwnerId", req.getNewOwnerId());
        product.put("currentOwnerRole", req.getNewOwnerRole());
        product.put("status", req.getNewStatus());
        product.put("updatedAt", now);

        List<Map<String, Object>> history = mockHistory.computeIfAbsent(productId, k -> new ArrayList<>());
        history.add(Map.of(
                "txId", "0x" + UUID.randomUUID().toString().replace("-", ""),
                "fromOwnerId", fromOwner,
                "toOwnerId", req.getNewOwnerId(),
                "toOwnerRole", req.getNewOwnerRole(),
                "status", req.getNewStatus(),
                "timestamp", now
        ));

        return objectMapper.writeValueAsString(product);
    }

    public String getProduct(String productId) throws Exception {
        if (fabricGatewayService.isFabricEnabled()) {
            Contract contract = fabricGatewayService.getContract();
            byte[] result = contract.evaluateTransaction("GetProduct", productId);
            return new String(result, StandardCharsets.UTF_8);
        }

        Map<String, Object> product = mockLedger.get(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product " + productId + " not found");
        }
        return objectMapper.writeValueAsString(product);
    }

    public String getProductHistory(String productId) throws Exception {
        if (fabricGatewayService.isFabricEnabled()) {
            Contract contract = fabricGatewayService.getContract();
            byte[] result = contract.evaluateTransaction("GetProductHistory", productId);
            return new String(result, StandardCharsets.UTF_8);
        }

        List<Map<String, Object>> history = mockHistory.get(productId);
        if (history == null) {
            return "[]";
        }
        return objectMapper.writeValueAsString(history);
    }

    public String getAllProducts() throws Exception {
        return objectMapper.writeValueAsString(mockLedger.values());
    }
}
