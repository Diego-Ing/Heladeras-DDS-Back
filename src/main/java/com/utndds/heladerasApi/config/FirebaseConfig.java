package com.utndds.heladerasApi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "firebase")
public class FirebaseConfig {
    private String projectId;
    private String privateKeyId;
    private String privateKey;
    private String clientEmail;
    private String clientId;
    private String authUri;
    private String tokenUri;
    private String certUrl;
    private String clientCertUrl;
    private String universeDomain;

    // Getters and setters
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    public String getPrivateKeyId() { return privateKeyId; }
    public void setPrivateKeyId(String privateKeyId) { this.privateKeyId = privateKeyId; }
    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getAuthUri() { return authUri; }
    public void setAuthUri(String authUri) { this.authUri = authUri; }
    public String getTokenUri() { return tokenUri; }
    public void setTokenUri(String tokenUri) { this.tokenUri = tokenUri; }
    public String getCertUrl() { return certUrl; }
    public void setCertUrl(String certUrl) { this.certUrl = certUrl; }
    public String getClientCertUrl() { return clientCertUrl; }
    public void setClientCertUrl(String clientCertUrl) { this.clientCertUrl = clientCertUrl; }
    public String getUniverseDomain() { return universeDomain; }
    public void setUniverseDomain(String universeDomain) { this.universeDomain = universeDomain; }
}
