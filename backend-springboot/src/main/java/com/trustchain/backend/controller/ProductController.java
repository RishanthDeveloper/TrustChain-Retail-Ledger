package com.trustchain.backend.controller;

import com.trustchain.backend.dto.CreateProductRequest;
import com.trustchain.backend.dto.TransferRequest;
import com.trustchain.backend.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<String> getAllProducts() throws Exception {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @PostMapping
    public ResponseEntity<String> createProduct(@RequestBody CreateProductRequest req,
                                                 Authentication auth) throws Exception {
        String manufacturerId = (auth != null && auth.getName() != null) ? auth.getName() : req.getManufacturerId();
        if (manufacturerId == null || manufacturerId.isEmpty()) {
            manufacturerId = "MANUFACTURER-APEX";
        }
        String result = productService.createProduct(
                req.getProductId(), req.getName(), req.getBatchNumber(), manufacturerId, req.getCertificateHash());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{productId}/transfer")
    public ResponseEntity<String> transferOwnership(@PathVariable String productId,
                                                      @RequestBody TransferRequest req,
                                                      Authentication auth) throws Exception {
        String callerId = (auth != null && auth.getName() != null) ? auth.getName() : req.getCallerId();
        String result = productService.transferOwnership(productId, callerId, req);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<String> getProduct(@PathVariable String productId) throws Exception {
        return ResponseEntity.ok(productService.getProduct(productId));
    }

    @GetMapping("/{productId}/history")
    public ResponseEntity<String> getHistory(@PathVariable String productId) throws Exception {
        return ResponseEntity.ok(productService.getProductHistory(productId));
    }
}
