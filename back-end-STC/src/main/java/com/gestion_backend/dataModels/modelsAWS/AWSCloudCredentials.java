package com.gestion_backend.dataModels.modelsAWS;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import java.time.OffsetDateTime;

public class AWSCloudCredentials implements AwsCredentialsProvider {
    private String sessionToken;
    private String accessKeyId;
    private String secretAccessKey;
    private OffsetDateTime expiration;

    public AWSCloudCredentials(String sessionTokenJson) {
        // Este constructor solo almacena el JSON - el parsing real se hace en parseCredentialsFromToken
        this.sessionToken = sessionTokenJson;
        this.accessKeyId = null;
        this.secretAccessKey = null;
    }

    public AWSCloudCredentials(String accessKeyId, String secretAccessKey, String sessionToken, OffsetDateTime expiration) {
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.sessionToken = sessionToken;
        this.expiration = expiration;
    }

    public AWSCloudCredentials(String accessKeyId, String secretAccessKey, String sessionToken) {
        this(accessKeyId, secretAccessKey, sessionToken, null);
    }

    public AWSCloudCredentials(String accessKeyId, String secretAccessKey) {
        this(accessKeyId, secretAccessKey, null, null);
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public OffsetDateTime getExpiration() {
        return expiration;
    }

    public void setCredentialComponents(String accessKeyId, String secretAccessKey, String sessionToken, OffsetDateTime expiration) {
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.sessionToken = sessionToken;
        this.expiration = expiration;
    }

    public boolean isValid() {
        // Si tenemos un token de sesión, necesitamos también sus credenciales asociadas
        if (sessionToken != null && !sessionToken.isEmpty()) {
            return accessKeyId != null && !accessKeyId.isEmpty()
                    && secretAccessKey != null && !secretAccessKey.isEmpty();
        }

        // Para credenciales no de sesión
        return accessKeyId != null && !accessKeyId.isEmpty()
                && secretAccessKey != null && !secretAccessKey.isEmpty();
    }

    @Override
    public AwsCredentials resolveCredentials() {
        if (sessionToken != null && !sessionToken.isEmpty()) {
            if (accessKeyId == null || secretAccessKey == null) {
                throw new IllegalStateException("Credenciales incompletas: se requiere parsear el token de sesión primero.");
            }
            return AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken);
        }

        if (accessKeyId == null || secretAccessKey == null) {
            throw new IllegalStateException("Credenciales inválidas: accessKeyId y secretAccessKey son requeridos.");
        }

        return new AwsCredentials() {
            @Override
            public String accessKeyId() {
                return accessKeyId;
            }

            @Override
            public String secretAccessKey() {
                return secretAccessKey;
            }
        };
    }

    public boolean needsParsing() {
        return sessionToken != null && accessKeyId == null;
    }
}

