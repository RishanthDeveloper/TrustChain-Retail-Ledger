# TrustChain-Retail-Ledger

Java-first blockchain supply-chain traceability platform.

- **Chaincode:** Java (`fabric-chaincode-java`), running on Hyperledger Fabric.
- **Backend:** Spring Boot 3 + Fabric Gateway Java SDK.
- **Off-chain store:** MongoDB.
- **Frontend:** React (or Next.js) — thin client, talks only to the Spring Boot REST API.

## Repo layout
```
trustchain-retail-ledger/
├── chaincode-java/        # ProductContract.java, Product.java, TransferRecord.java
├── backend-springboot/    # REST API, Fabric Gateway client, MongoDB sync
└── frontend/              # React dashboards + consumer QR scan page
```

## 3. Backend → Chaincode call flow (ownership transfer)

`POST /api/products/{productId}/transfer` (see `ProductController.transferOwnership`) does this:

1. Spring Security resolves the authenticated user from the JWT (`Authentication auth`) — this becomes `callerId`, the **current owner**. It is never taken from the request body, so a wholesaler can't spoof a transfer on someone else's product.
2. `ProductService.transferOwnership(...)` calls `contract.submitTransaction("TransferOwnership", productId, callerId, newOwnerId, newOwnerRole, newStatus)` via the Fabric Gateway SDK. This transaction is **endorsed** by the required peers, ordered, and committed to the ledger.
3. Chaincode's `TransferOwnership` verifies `callerId` really is `product.currentOwnerId`, then updates state and emits an `OwnershipTransferred` event.
4. Backend listens for chaincode events (via `network.getChaincodeEvents(...)` in a background listener bean, not shown above for brevity) and mirrors the new state into MongoDB for fast dashboard queries — the ledger stays the source of truth; MongoDB is just a read cache + metadata store.

## Setup instructions (local dev, starting today)

### 1. Prerequisites
```bash
# Java (chaincode + backend)
sdk install java 17.0.9-tem      # or use your OS package manager

# Hyperledger Fabric samples, binaries, and Docker images
curl -sSLO https://raw.githubusercontent.com/hyperledger/fabric/main/scripts/install-fabric.sh
chmod +x install-fabric.sh
./install-fabric.sh docker samples binary

# Docker + Docker Compose (Fabric runs as containers)
docker --version
docker compose version

# Node.js only needed for the frontend
node -v   # v18+
```

### 2. Bring up a test network
```bash
cd fabric-samples/test-network
./network.sh down
./network.sh up createChannel -c mychannel -ca
```
This starts Org1/Org2 peers, an orderer, and a channel called `mychannel` with TLS + CAs — enough to develop against.

### 3. Package and deploy the Java chaincode
```bash
cd chaincode-java
mvn clean package        # produces target dependencies fabric-chaincode-java needs

cd ../fabric-samples/test-network
./network.sh deployCC -ccn productcontract \
  -ccp ../../trustchain-retail-ledger/chaincode-java \
  -ccl java
```

### 4. Add Maven dependencies to `chaincode-java/pom.xml`
```xml
<dependency>
    <groupId>org.hyperledger.fabric-chaincode-java</groupId>
    <artifactId>fabric-chaincode-shim</artifactId>
    <version>2.5.4</version>
</dependency>
<dependency>
    <groupId>com.owlike</groupId>
    <artifactId>genson</artifactId>
    <version>1.6</version>
</dependency>
```

### 5. Backend (`backend-springboot`) dependencies (`pom.xml`)
```xml
<dependency>
    <groupId>org.hyperledger.fabric</groupId>
    <artifactId>fabric-gateway</artifactId>
    <version>1.5.1</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-netty-shaded</artifactId>
    <version>1.62.2</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

`application.yml` needs the paths `FabricGatewayService` reads: `fabric.mspId`, `fabric.certPath`, `fabric.keyPath`, `fabric.tlsCertPath`, `fabric.peerEndpoint`, `fabric.overrideAuth`, `fabric.channelName=mychannel`, `fabric.chaincodeName=productcontract`. Point these at the crypto material `network.sh` generates under `organizations/peerOrganizations/org1.example.com/...`.

### 6. Run the backend
```bash
cd backend-springboot
mvn spring-boot:run
```

### 7. Frontend
```bash
cd frontend
npx create-react-app . --template typescript   # or `npx create-next-app@latest .`
npm install axios react-router-dom qrcode.react
npm start
```

### Suggested build order
1. Get chaincode installed and callable via the Fabric CLI (`peer chaincode invoke/query`) before touching the backend — confirms the ledger logic works in isolation.
2. Wire up `FabricGatewayService` and one endpoint (`GetProduct`) end-to-end.
3. Add `CreateProduct` and `TransferOwnership` with JWT-based RBAC.
4. Add MongoDB sync via chaincode event listener.
5. Build the consumer-facing public history page + QR generation (encode `productId` into the QR, resolve via `GET /api/products/{id}/history`).
6. Build manufacturer/wholesaler/retailer dashboards last — they're the thinnest layer once the API is solid.

## Note on RBAC design
Two layers, deliberately kept separate:
- **Fabric-level identity** (MSP/certs) — proves *which organization* submitted a transaction.
- **Application-level roles** (Spring Security + JWT claims, e.g. `MANUFACTURER`/`WHOLESALER`/`RETAILER`/`CONSUMER`) — proves *which business role* a user has within that org. The chaincode's `TransferOwnership` re-validates `callerId == currentOwnerId` as a second, on-ledger check, so even a compromised backend can't forge a transfer that the ledger itself would reject.
