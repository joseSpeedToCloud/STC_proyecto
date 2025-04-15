package com.gestion_backend.dataModels.modelsGoogle;

import com.google.auth.Credentials;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GoogleCloudCredentials extends Credentials {
    private final String accessToken;

    public GoogleCloudCredentials(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public void refresh() throws IOException {
        // No se realiza acción de refresco
    }

    @Override
    public String getAuthenticationType() {
        return "Bearer"; // Tipo de autenticación
    }

    @Override
    public Map<String, List<String>> getRequestMetadata(URI uri) throws IOException {
        // Retorna metadatos con el token de acceso
        return Collections.singletonMap("Authorization", Collections.singletonList("Bearer " + accessToken));
    }

    @Override
    public boolean hasRequestMetadata() {
        return true;
    }

    @Override
    public boolean hasRequestMetadataOnly() {
        return true;
    }

    // Exponemos el token de acceso para poder accederlo desde fuera de esta clase
    public String getAccessToken() {
        return accessToken;
    }
}

