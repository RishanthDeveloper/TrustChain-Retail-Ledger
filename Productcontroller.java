package com.trustchain.backend.controller;

import com.trustchain.backend.dto.TransferRequest;
import com.trustchain.backend.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // MANUFACTURER only -- enforced via Spring Security role check
    @PreAuthorize("hasRole('MANUFACTURER')")
    @PostMapping
    public ResponseEntity<String> createProduct(@RequestBody CreateProductRequest req,
                                                 Authentication auth) throws Exception {
        String manufacturerId = auth.getName(); // resolved from JWT subject
        String result = productService.createProduct(
                req.productId(), req.name(), req.batchNumber(), manufacturerId, req.certificateHash());
        return ResponseEntity.ok(result);
    }

    // WHOLESALER / RETAILER / CONSUMER -- whoever currently owns the product
    @PostMapping("/{productId}/transfer")
    public ResponseEntity<String> transferOwnership(@PathVariable String productId,
                                                      @RequestBody TransferRequest req,
                                                      Authentication auth) throws Exception {
        String callerId = auth.getName(); // the CURRENT owner, taken from the auth token, not the request body
        String result = productService.transferOwnership(productId, callerId, req);
        return ResponseEntity.ok(result);
    }

    // Public read -- this is what the consumer QR-code scan hits, no auth required
    @GetMapping("/{productId}")
    public ResponseEntity<String> getProduct(@PathVariable String productId) throws Exception {
        return ResponseEntity.ok(productService.getProduct(productId));
    }

    @GetMapping("/{productId}/history")
    public ResponseEntity<String> getHistory(@PathVariable String productId) throws Exception {
        return ResponseEntity.ok(productService.getProductHistory(productId));
    }

    public record CreateProductRequest(String productId, String name, String batchNumber, String certificateHash) {}
}
