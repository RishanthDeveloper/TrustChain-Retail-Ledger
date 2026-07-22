package com.trustchain.backend.dto;

public class CreateProductRequest {
    private String productId;
    private String name;
    private String batchNumber;
    private String manufacturerId;
    private String certificateHash;

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public String getManufacturerId() { return manufacturerId; }
    public void setManufacturerId(String manufacturerId) { this.manufacturerId = manufacturerId; }

    public String getCertificateHash() { return certificateHash; }
    public void setCertificateHash(String certificateHash) { this.certificateHash = certificateHash; }
}
