package com.trustchain.backend.service;

import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.Network;
import org.hyperledger.fabric.client.identity.Identities;
import org.hyperledger.fabric.client.identity.Identity;
import org.hyperledger.fabric.client.identity.Signer;
import org.hyperledger.fabric.client.identity.Signers;
import org.slf.Logger;
import org.slf.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

@Service
public class FabricGatewayService {

    private static final Logger log = LoggerFactory.getLogger(FabricGatewayService.class);

    @Value("${fabric.enabled:false}")
    private boolean fabricEnabled;

    @Value("${fabric.mspId:Org1MSP}")
    private String mspId;

    @Value("${fabric.certPath:}")
    private String certPath;

    @Value("${fabric.keyPath:}")
    private String keyPath;

    @Value("${fabric.tlsCertPath:}")
    private String tlsCertPath;

    @Value("${fabric.peerEndpoint:localhost:7051}")
    private String peerEndpoint;

    @Value("${fabric.overrideAuth:peer0.org1.example.com}")
    private String overrideAuth;

    @Value("${fabric.channelName:mychannel}")
    private String channelName;

    @Value("${fabric.chaincodeName:productcontract}")
    private String chaincodeName;

    private ManagedChannel channel;
    private Gateway gateway;
    private Contract contract;

    @PostConstruct
    public void init() {
        if (!fabricEnabled) {
            log.info("Fabric Gateway is DISABLED. Running backend in Dev Mock Ledger Mode.");
            return;
        }

        try {
            log.info("Connecting to Hyperledger Fabric network at {}...", peerEndpoint);
            channel = newGrpcChannel();
            Gateway.Builder builder = Gateway.newInstance()
                    .identity(newIdentity())
                    .signer(newSigner())
                    .connection(channel);

            gateway = builder.connect();
            Network network = gateway.getNetwork(channelName);
            contract = network.getContract(chaincodeName, "ProductContract");
            log.info("Successfully connected to Fabric network channel: {}", channelName);
        } catch (Exception e) {
            log.warn("Failed to connect to Fabric network: {}. Falling back to Dev Mock Ledger Mode.", e.getMessage());
            this.fabricEnabled = false;
        }
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        if (gateway != null) gateway.close();
        if (channel != null) channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    public boolean isFabricEnabled() {
        return fabricEnabled;
    }

    public Contract getContract() {
        return contract;
    }

    private ManagedChannel newGrpcChannel() throws Exception {
        X509Certificate tlsCert;
        try (Reader r = Files.newBufferedReader(Path.of(tlsCertPath))) {
            tlsCert = Identities.readX509Certificate(r);
        }
        var credentials = TlsChannelCredentials.newBuilder()
                .trustManager(tlsCert)
                .build();
        return Grpc.newChannelBuilder(peerEndpoint, credentials)
                .overrideAuthority(overrideAuth)
                .build();
    }

    private Identity newIdentity() throws Exception {
        try (Reader r = Files.newBufferedReader(Path.of(certPath))) {
            X509Certificate cert = Identities.readX509Certificate(r);
            return new org.hyperledger.fabric.client.identity.X509Identity(mspId, cert);
        }
    }

    private Signer newSigner() throws Exception {
        try (Reader r = Files.newBufferedReader(Path.of(keyPath))) {
            PrivateKey key = Identities.readPrivateKey(r);
            return Signers.newPrivateKeySigner(key);
        }
    }
}
