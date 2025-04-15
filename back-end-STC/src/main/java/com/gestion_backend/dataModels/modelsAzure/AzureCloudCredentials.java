package com.gestion_backend.dataModels.modelsAzure;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public class AzureCloudCredentials implements TokenCredential {
    private final String accessToken;
    private final OffsetDateTime expiresAt;

    public AzureCloudCredentials(String accessToken) {
        this(accessToken, OffsetDateTime.now().plusHours(1));
    }

    public AzureCloudCredentials(String accessToken, OffsetDateTime expiresAt) {
        this.accessToken = accessToken;
        this.expiresAt = expiresAt;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
        // Return a Mono that contains the AccessToken
        return Mono.just(new AccessToken(accessToken, expiresAt));
    }

    // Expose the access token to be accessed from outside this class
    public String getAccessToken() {
        return accessToken;
    }

    // Expose the expiration time to be accessed from outside this class
    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }
}

