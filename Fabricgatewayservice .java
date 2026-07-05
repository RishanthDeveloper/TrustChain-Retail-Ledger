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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.TimeUnit;

/**
 * Central point of contact between the REST layer and the Fabric network.
 * Wraps a single long-lived Gateway connection scoped to this backend
 * instance's identity (typically an admin/app identity for Org1).
 */
@Service
public class FabricGatewayService {

    @Value("${fabric.mspId}")
    private String mspId;

    @Value("${fabric.certPath}")
    private String certPath;

    @Value("${fabric.keyPath}")
    private String keyPath;

    @Value("${fabric.tlsCertPath}")
    private String tlsCertPath;

    @Value("${fabric.peerEndpoint}")
    private String peerEndpoint;

    @Value("${fabric.overrideAuth}")
    private String overrideAuth;

    @Value("${fabric.channelName}")
    private String channelName;

    @Value("${fabric.chaincodeName}")
    private String chaincodeName;

    private ManagedChannel channel;
    private Gateway gateway;
    private Contract contract;

    @PostConstruct
    public void init() throws Exception {
        channel = newGrpcChannel();
        Gateway.Builder builder = Gateway.newInstance()
                .identity(newIdentity())
                .signer(newSigner())
                .connection(channel);

        gateway = builder.connect();
        Network network = gateway.getNetwork(channelName);
        contract = network.getContract(chaincodeName, "ProductContract");
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        if (gateway != null) gateway.close();
        if (channel != null) channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    public Contract getContract() {
        return contract;
    }

    // ---- identity / TLS plumbing ----

    private ManagedChannel newGrpcChannel() throws IOException, CertificateException {
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

    private Identity newIdentity() throws IOException, CertificateException {
        try (Reader r = Files.newBufferedReader(Path.of(certPath))) {
            X509Certificate cert = Identities.readX509Certificate(r);
            return new org.hyperledger.fabric.client.identity.X509Identity(mspId, cert);
        }
    }

    private Signer newSigner() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        try (Reader r = Files.newBufferedReader(Path.of(keyPath))) {
            PrivateKey key = Identities.readPrivateKey(r);
            return Signers.newPrivateKeySigner(key);
        }
    }
}
