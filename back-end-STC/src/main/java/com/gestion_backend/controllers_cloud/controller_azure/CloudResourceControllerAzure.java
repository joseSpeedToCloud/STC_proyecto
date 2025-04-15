package com.gestion_backend.controllers_cloud.controller_azure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion_backend.dataModels.modelsAzure.*;
import com.gestion_backend.service_cloud.services_azure.CloudResourceServiceAzure;
import com.gestion_backend.service_cloud.services_azure.ResourceManagerServiceAzure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cloud")
//permitir solicitudes CORS desde el puerto
@CrossOrigin(origins = "http://localhost:4173")
public class CloudResourceControllerAzure {

    @Autowired
    private CloudResourceServiceAzure cloudResourceServiceAzure;

    @Autowired
    private ResourceManagerServiceAzure resourceManagerServiceAzure;

    @GetMapping("/statusAzure")
    public ResponseEntity<Map<String, String>> getStatus() {
        return ResponseEntity.ok(Map.of("status", "connected"));
    }

    @GetMapping("/directoriesAzure")
    public ResponseEntity<List<Map<String, Object>>> getOrganizations() {
        List<DirectoryDataModelAzure> directories = cloudResourceServiceAzure.getAllDirectories();
        List<Map<String, Object>> response = new ArrayList<>();

        for (DirectoryDataModelAzure directory : directories) {
            Map<String, Object> directoryData = new HashMap<>();
            directoryData.put("directory_id", directory.getId());
            directoryData.put("name", directory.getName());
            response.add(directoryData);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/homeazure")
    public ResponseEntity<List<SubscriptionDataModelAzure>> getProjectsHome() {
        List<SubscriptionDataModelAzure> subscriptions = cloudResourceServiceAzure.getAllSubscriptions();
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/subscriptionsAzure")
    public ResponseEntity<List<Map<String, Object>>> getSubscriptions() {
        List<SubscriptionDataModelAzure> subscriptions = cloudResourceServiceAzure.getAllSubscriptions();
        List<Map<String, Object>> response = new ArrayList<>();

        for (SubscriptionDataModelAzure subscription : subscriptions) {
            Map<String, Object> subscriptionData = new HashMap<>();
            subscriptionData.put("id", subscription.getId());
            subscriptionData.put("name", subscription.getName());
            subscriptionData.put("display_name", subscription.getDisplayName() != null ? subscription.getDisplayName() : "");

            if (subscription.getDirectory() != null) {
                Map<String, Object> directoryData = new HashMap<>();
                directoryData.put("directory_id", subscription.getDirectory().getId());
                directoryData.put("name", subscription.getDirectory().getName());
                subscriptionData.put("directory", directoryData);
            }

            List<Map<String, Object>> vmList = new ArrayList<>();
            if (subscription.getVirtualMachines() != null) {
                for (VirtualMachineDataModelAzure vm : subscription.getVirtualMachines()) {
                    Map<String, Object> vmData = new HashMap<>();
                    vmData.put("machine_id", vm.getId());
                    vmData.put("name", vm.getName());
                    vmData.put("machine_type", vm.getVmSize());
                    vmData.put("zone", vm.getLocation());
                    vmData.put("status", vm.getStatus());
                    vmList.add(vmData);
                }
            }
            subscriptionData.put("virtual_machines", vmList);

            response.add(subscriptionData);
        }

        return ResponseEntity.ok(response);
    }

    //endpoint para obtener todas las VM
    @GetMapping("/virtualmachinesazure")
    public ResponseEntity<List<Map<String, Object>>> getVirtualMachines(
            @RequestParam(value = "subscriptionId", required = false) String subscriptionId) {
        List<VirtualMachineDataModelAzure> vms = cloudResourceServiceAzure.getAllVirtualMachines();
        List<Map<String, Object>> response = new ArrayList<>();

        for (VirtualMachineDataModelAzure vm : vms) {
            if (subscriptionId == null || vm.getSubscription().getId().equals(subscriptionId)) {
                Map<String, Object> vmData = new HashMap<>();
                vmData.put("machine_id", vm.getId());
                vmData.put("name", vm.getName());
                vmData.put("subscription_id", vm.getSubscription().getId());
                vmData.put("machine_type", vm.getVmSize());
                vmData.put("zone", vm.getLocation());
                vmData.put("status", vm.getStatus());
                response.add(vmData);
            }
        }

        return ResponseEntity.ok(response);
    }

    //endpoint para obtener todas las redes
    @GetMapping("/networksazure")
    public ResponseEntity<List<Map<String, Object>>> getNetworks(
            @RequestParam(value = "subscriptionId", required = false) String subscriptionId) {
        List<VirtualNetworkDataModelAzure> networks = cloudResourceServiceAzure.getAllVirtualNetworks();
        List<Map<String, Object>> response = new ArrayList<>();

        for (VirtualNetworkDataModelAzure network : networks) {
            if (subscriptionId == null || network.getSubscription().getId().equals(subscriptionId)) {
                Map<String, Object> networkData = new HashMap<>();
                networkData.put("network_id", network.getId());
                networkData.put("name", network.getName());
                networkData.put("subscription_id", network.getSubscription().getId());
                networkData.put("address_space", network.getAddressSpace());
                networkData.put("enable_ddos_protection", network.getEnableDdosProtection());
                networkData.put("enable_vm_protection", network.getEnableVmProtection());
                networkData.put("description", network.getDescription());
                response.add(networkData);
            }
        }

        return ResponseEntity.ok(response);
    }

    //endpoint para obtener todas las subredes
    @GetMapping("/subnetworksazure")
    public ResponseEntity<List<Map<String, Object>>> getSubnetworks(
            @RequestParam(value = "subscriptionId", required = false) String subscriptionId) {
        List<SubnetDataModelAzure> subnetworks = cloudResourceServiceAzure.getAllSubnets();
        List<Map<String, Object>> response = new ArrayList<>();

        for (SubnetDataModelAzure subnet : subnetworks) {
            if (subscriptionId == null || subnet.getSubscription().getId().equals(subscriptionId)) {
                Map<String, Object> subnetData = new HashMap<>();
                subnetData.put("subnetwork_id", subnet.getId());
                subnetData.put("name", subnet.getName());
                subnetData.put("network_id", subnet.getVirtualNetwork().getId());
                subnetData.put("subscription_id", subnet.getSubscription().getId());
                subnetData.put("address_prefix", subnet.getAddressPrefix());
                subnetData.put("private_endpoint_network_policies", subnet.getPrivateEndpointNetworkPolicies());
                subnetData.put("private_link_service_network_policies", subnet.getPrivateLinkServiceNetworkPolicies());
                subnetData.put("description", subnet.getDescription());
                response.add(subnetData);
            }
        }

        return ResponseEntity.ok(response);
    }

    //endpoint para obtener todos los discos
    @GetMapping("/disksazure")
    public ResponseEntity<List<Map<String, Object>>> getDisks(
            @RequestParam(value = "subscriptionId", required = false) String subscriptionId) {
        List<DiskDataModelAzure> disks = cloudResourceServiceAzure.getAllDisks();
        List<Map<String, Object>> response = new ArrayList<>();

        for (DiskDataModelAzure disk : disks) {
            if (subscriptionId == null || disk.getSubscription().getId().equals(subscriptionId)) {
                Map<String, Object> diskData = new HashMap<>();
                diskData.put("disk_id", disk.getId());
                diskData.put("name", disk.getName());
                diskData.put("subscription_id", disk.getSubscription().getId());
                diskData.put("disk_type", disk.getDiskType());
                diskData.put("location", disk.getLocation());
                diskData.put("size_gb", disk.getSizeGb());
                diskData.put("status", disk.getStatus());
                diskData.put("details", disk.getDetails());
                response.add(diskData);
            }
        }
        return ResponseEntity.ok(response);
    }

    //endpoint para obtener todos los snapshots
    @GetMapping("/snapshotsazure")
    public ResponseEntity<List<Map<String, Object>>> getSnapshots(
            @RequestParam(value = "subscriptionId", required = false) String subscriptionId) {
        List<SnapshotDataModelAzure> snapshots = cloudResourceServiceAzure.getAllSnapshots();
        List<Map<String, Object>> response = new ArrayList<>();

        for (SnapshotDataModelAzure snapshot : snapshots) {
            if (subscriptionId == null || snapshot.getSubscription().getId().equals(subscriptionId)) {
                Map<String, Object> snapshotData = new HashMap<>();
                snapshotData.put("snapshot_id", snapshot.getId());
                snapshotData.put("name", snapshot.getName());
                snapshotData.put("subscription_id", snapshot.getSubscription().getId());
                snapshotData.put("source_disk", snapshot.getSourceDiskId());
                snapshotData.put("disk_size_gb", snapshot.getDiskSizeGb());
                snapshotData.put("status", snapshot.getStatus());
                snapshotData.put("storage_account_type", snapshot.getStorageAccountType());
                snapshotData.put("details", snapshot.getDetails());
                response.add(snapshotData);
            }
        }
        return ResponseEntity.ok(response);
    }

    //endpoint para obtener todos los balanceadores
    @GetMapping("/loadbalancersazure")
    public ResponseEntity<List<Map<String, Object>>> getLoadBalancers(
            @RequestParam(value = "subscriptionId", required = false) String subscriptionId) {
        List<LoadBalancerDataModelAzure> loadBalancers = cloudResourceServiceAzure.getAllLoadBalancers();
        List<Map<String, Object>> response = new ArrayList<>();

        for (LoadBalancerDataModelAzure loadBalancer : loadBalancers) {
            if (subscriptionId == null || loadBalancer.getSubscription().getId().equals(subscriptionId)) {
                Map<String, Object> loadBalancerData = new HashMap<>();
                loadBalancerData.put("load_balancer_id", loadBalancer.getId());
                loadBalancerData.put("name", loadBalancer.getName());
                loadBalancerData.put("subscription_id", loadBalancer.getSubscription().getId());
                loadBalancerData.put("location", loadBalancer.getLocation());
                loadBalancerData.put("sku", loadBalancer.getSku());
                loadBalancerData.put("status", loadBalancer.getStatus());
                loadBalancerData.put("details", loadBalancer.getDetails());
                response.add(loadBalancerData);
            }
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vpnsazure")
    public ResponseEntity<List<Map<String, Object>>> getVPNs(
            @RequestParam(value = "subscriptionId", required = false) String subscriptionId) {
        List<VpnGatewayDataModelAzure> vpns = cloudResourceServiceAzure.getAllVpnGateways();
        List<Map<String, Object>> response = new ArrayList<>();

        for (VpnGatewayDataModelAzure vpn : vpns) {
            if (subscriptionId == null || vpn.getSubscription().getId().equals(subscriptionId)) {
                Map<String, Object> vpnData = new HashMap<>();
                vpnData.put("vpn_id", vpn.getId());
                vpnData.put("name", vpn.getName());
                vpnData.put("subscription_id", vpn.getSubscription().getId());
                vpnData.put("location", vpn.getLocation());
                vpnData.put("type", vpn.getType());
                vpnData.put("status", vpn.getStatus());
                vpnData.put("details", vpn.getDetails());
                response.add(vpnData);
            }
        }
        return ResponseEntity.ok(response);
    }

    //endpoint para obtener todos los firewalls
    @GetMapping("/networksecuritygroupsazure")
    public ResponseEntity<List<Map<String, Object>>> getFirewalls(
            @RequestParam(value = "subscriptionId", required = false) String subscriptionId) {
        List<NetworkSecurityGroupDataModelAzure> firewalls = cloudResourceServiceAzure.getAllNetworkSecurityGroups();
        List<Map<String, Object>> response = new ArrayList<>();

        for (NetworkSecurityGroupDataModelAzure firewall : firewalls) {
            if (subscriptionId == null || firewall.getSubscription().getId().equals(subscriptionId)) {
                Map<String, Object> firewallData = new HashMap<>();
                firewallData.put("id", firewall.getId());
                firewallData.put("name", firewall.getName());
                firewallData.put("resourceGroup", firewall.getResourceGroup());
                firewallData.put("location", firewall.getLocation());
                firewallData.put("subscription_id", firewall.getSubscription().getId());
                firewallData.put("security_rules", firewall.getSecurityRules());
                firewallData.put("details", firewall.getDetails());
                response.add(firewallData);
            }
        }
        return ResponseEntity.ok(response);
    }

    //endpoint para obtener todos los clusters GKE
    @GetMapping("/aksclustersazure")
    public ResponseEntity<List<Map<String, Object>>> getAksClusters(
            @RequestParam(value = "subscriptionId", required = false) String subscriptionId) {
        List<AksClusterDataModelAzure> clusters = cloudResourceServiceAzure.getAllAksClusters();
        List<Map<String, Object>> response = new ArrayList<>();

        for (AksClusterDataModelAzure cluster : clusters) {
            if (subscriptionId == null || cluster.getSubscription().getId().equals(subscriptionId)) {
                Map<String, Object> clusterData = new HashMap<>();
                clusterData.put("cluster_id", cluster.getId());
                clusterData.put("name", cluster.getName());
                clusterData.put("subscription_id", cluster.getSubscription().getId());
                clusterData.put("location", cluster.getLocation());
                clusterData.put("kubernetes_version", cluster.getKubernetesVersion());
                clusterData.put("status", cluster.getStatus());
                clusterData.put("details", cluster.getDetails());
                response.add(clusterData);
            }
        }
        return ResponseEntity.ok(response);
    }

    // Endpoint para iniciar sesión y obtener un token y correo de Azure
    @GetMapping("/login-azure")
    public ResponseEntity<?> loginAzure() {
        try {
            // Ejecutar login de Azure CLI
            ProcessBuilder processBuilder = new ProcessBuilder("az", "login", "--output", "json");
            processBuilder.environment().put("CLOUDSDK_CORE_DISABLE_PROMPTS", "1");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error al iniciar sesión con Azure CLI");
            }

            // Obtener el token de acceso
            ProcessBuilder tokenBuilder = new ProcessBuilder(
                    "az", "account", "get-access-token", "--output", "tsv", "--query", "accessToken");
            Process tokenProcess = tokenBuilder.start();
            BufferedReader tokenReader = new BufferedReader(new InputStreamReader(tokenProcess.getInputStream()));
            String token = tokenReader.readLine();

            // Obtener el correo electrónico asociado
            ProcessBuilder emailBuilder = new ProcessBuilder(
                    "az", "account", "show", "--query", "user.name", "--output", "tsv");
            Process emailProcess = emailBuilder.start();
            BufferedReader emailReader = new BufferedReader(new InputStreamReader(emailProcess.getInputStream()));
            String email = emailReader.readLine();

            if (token != null && email != null) {
                return ResponseEntity.ok(Map.of(
                        "email", email,
                        "accessToken", token,
                        "message", "Login exitoso y sincronización completada"
                ));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo obtener el token o el correo.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error durante el login: " + e.getMessage());
        }
    }

    // Endpoint para renovar el token de Azure
    @GetMapping("/renew-token-azure")
    public ResponseEntity<?> renewTokenAzure() {
        try {
            // Obtener un nuevo token sin volver a iniciar sesión
            ProcessBuilder tokenBuilder = new ProcessBuilder(
                    "az", "account", "get-access-token", "--output", "tsv", "--query", "accessToken");
            Process tokenProcess = tokenBuilder.start();
            BufferedReader tokenReader = new BufferedReader(new InputStreamReader(tokenProcess.getInputStream()));
            String newToken = tokenReader.readLine();

            // Obtener el email de la cuenta autenticada
            ProcessBuilder emailBuilder = new ProcessBuilder(
                    "az", "account", "show", "--query", "user.name", "--output", "tsv");
            Process emailProcess = emailBuilder.start();
            BufferedReader emailReader = new BufferedReader(new InputStreamReader(emailProcess.getInputStream()));
            String email = emailReader.readLine();

            if (newToken != null && email != null) {
                return ResponseEntity.ok(Map.of(
                        "email", email,
                        "accessToken", newToken,
                        "message", "Token renovado exitosamente"
                ));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo renovar el token.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al renovar el token: " + e.getMessage());
        }
    }

    // Endpoint para cerrar sesión en Azure
    @GetMapping("/logout-azure")
    public ResponseEntity<?> logoutAzure() {
        try {
            ProcessBuilder logoutBuilder = new ProcessBuilder("az", "logout");
            Process logoutProcess = logoutBuilder.start();
            int exitCode = logoutProcess.waitFor();

            if (exitCode == 0) {
                return ResponseEntity.ok("Sesión cerrada exitosamente");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cerrar la sesión de Azure");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error durante el logout de Azure: " + e.getMessage());
        }
    }

    @PostMapping("/validate-subscription-azure")
    public ResponseEntity<?> validateSubscriptionAzure(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String subscriptionId = request.get("subscriptionId");

        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Token is required"));
        }

        if (subscriptionId == null || subscriptionId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Subscription ID is required"));
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // Check subscription exists and is accessible
            HttpRequest subRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://management.azure.com/subscriptions/" + subscriptionId + "?api-version=2020-01-01"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> subResponse = client.send(subRequest, HttpResponse.BodyHandlers.ofString());

            if (subResponse.statusCode() != 200) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Invalid subscription or no access"));
            }

            // Parse subscription details
            ObjectMapper mapper = new ObjectMapper();
            JsonNode subscription = mapper.readTree(subResponse.body());

            return ResponseEntity.ok(Map.of(
                    "subscriptionId", subscriptionId,
                    "displayName", subscription.get("displayName").asText(),
                    "state", subscription.get("state").asText()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Validation failed", "details", e.getMessage()));
        }
    }

    // Endpoint para validar el token de Azure
    @PostMapping("/validate-token-azure")
    public ResponseEntity<?> validateTokenAzure(@RequestBody Map<String, String> request) {
        String token = request.get("token");

        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Token is required"));
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // Simple validation by checking the token works
            HttpRequest testRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://management.azure.com/tenants?api-version=2020-01-01"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(testRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token"));
            }

            // Get email from token
            String email = getEmailFromToken(token);

            return ResponseEntity.ok(Map.of(
                    "message", "Token is valid",
                    "email", email
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Validation failed", "details", e.getMessage()));
        }
    }

    // Helper method to extract email from token
    private String getEmailFromToken(String token) {
        try {
            // Split the JWT token
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return "unknown@email.com";
            }

            // Decode the payload
            String payload = new String(Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode payloadJson = mapper.readTree(payload);

            // Try to get the email from different possible claims
            if (payloadJson.has("upn")) {
                return payloadJson.get("upn").asText();
            } else if (payloadJson.has("email")) {
                return payloadJson.get("email").asText();
            } else if (payloadJson.has("unique_name")) {
                return payloadJson.get("unique_name").asText();
            }

            return "unknown@email.com";
        } catch (Exception e) {
            System.err.println("Error extracting email from token: " + e.getMessage());
            return "unknown@email.com";
        }
    }

    // Endpoint para guardar una suscripción seleccionada en la base de datos
    @PostMapping("/save-subscription")
    public ResponseEntity<?> saveSelectedSubscription(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String subscriptionId = request.get("subscriptionId");
        String tenantId = request.get("tenantId");
        String email = request.get("email"); // Asegúrate de enviar el email desde el frontend

        // Log the incoming request parameters (excluding sensitive token)
        System.out.println("Received subscription sync request for subscription: " + subscriptionId);
        System.out.println("With tenant ID: " + tenantId);

        if (token == null || subscriptionId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Missing required parameters: token or subscriptionId");
        }

        // Si tenantId es null, intentaremos extraerlo del token o usar un enfoque predeterminado
        if (tenantId == null) {
            System.out.println("Warning: tenantId is null, will attempt to proceed anyway");
            tenantId = getTenantIdFromSubscription(token, subscriptionId);
            System.out.println("Retrieved tenant ID: " + tenantId);
        }

        try {
            // Crear objeto de credenciales
            AzureCloudCredentials credentials = new AzureCloudCredentials(token);

            // Verificar que el token es válido
            boolean tokenValid = verifyTokenIsValid(token);
            if (!tokenValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid or expired token. Please log in again.");
            }

            // Verificar acceso a la suscripción
            boolean hasAccess = isSubscriptionAccessible(token, subscriptionId);
            System.out.println("Subscription access check result: " + hasAccess);

            if (hasAccess) {
                try {
                    // AQUÍ ESTÁ EL CAMBIO: llamar a syncUserSubscriptionsAndResources en lugar de extractAzureResources
                    System.out.println("Starting resource synchronization for subscription: " + subscriptionId);

                    // Si el email es null, utilizar un valor predeterminado o extraerlo del token
                    if (email == null) {
                        email = getEmailFromToken(token);
                    }

                    // Llamar al método correcto para sincronizar usuario y recursos
                    resourceManagerServiceAzure.syncUserSubscriptionsAndResources(credentials, email, tenantId);

                    return ResponseEntity.ok(Map.of(
                            "message", "Suscripción sincronizada correctamente.",
                            "subscriptionId", subscriptionId
                    ));
                } catch (Exception e) {
                    // Log del error detallado para depuración
                    System.err.println("Error during resource synchronization: " + e.getMessage());
                    e.printStackTrace();

                    // Devolver un mensaje de error más detallado
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("message", "Error processing subscription resources");
                    errorResponse.put("error", e.getMessage());
                    errorResponse.put("errorType", e.getClass().getName());

                    // Añadir los primeros elementos de la traza de pila para depuración
                    StackTraceElement[] stackTrace = e.getStackTrace();
                    List<String> stackTraceDetail = new ArrayList<>();
                    for (int i = 0; i < Math.min(5, stackTrace.length); i++) {
                        stackTraceDetail.add(stackTrace[i].toString());
                    }
                    errorResponse.put("stackTrace", stackTraceDetail);

                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No tienes acceso a la suscripción seleccionada.");
            }
        } catch (Exception e) {
            // Log de errores inesperados
            System.err.println("Unexpected error in saveSelectedSubscription: " + e.getMessage());
            e.printStackTrace();

            // Devolver información detallada del error
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Unexpected error processing subscription");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("errorType", e.getClass().getName());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Helper method to verify token is valid
    private boolean verifyTokenIsValid(String token) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://management.azure.com/subscriptions?api-version=2020-01-01"))
                    .header("Authorization", "Bearer " + token)
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("Error validating token: " + e.getMessage());
            return false;
        }
    }

    // Helper method to get tenant ID from subscription
    private String getTenantIdFromSubscription(String token, String subscriptionId) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://management.azure.com/subscriptions/" + subscriptionId + "?api-version=2020-01-01"))
                    .header("Authorization", "Bearer " + token)
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode responseJson = mapper.readTree(response.body());
                if (responseJson.has("tenantId")) {
                    return responseJson.get("tenantId").asText();
                }
            }

            // Default tenant ID if we can't extract it
            return "default-tenant-id";
        } catch (Exception e) {
            System.err.println("Error getting tenant ID: " + e.getMessage());
            return "default-tenant-id";
        }
    }

    // Método auxiliar para verificar acceso a una suscripción
    private boolean isSubscriptionAccessible(String token, String subscriptionId) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://management.azure.com/subscriptions/" + subscriptionId + "?api-version=2020-01-01"))
                    .header("Authorization", "Bearer " + token)
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Subscription access check response status: " + response.statusCode());

            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("Error checking subscription access: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Endpoint para obtener detalles de una suscripción específica
    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<?> getSubscriptionById(@PathVariable("subscriptionId") String subscriptionId) {
        try {
            // Busca la suscripción en la base de datos utilizando el `subscriptionId`
            SubscriptionDataModelAzure subscription = cloudResourceServiceAzure.getSubscription(subscriptionId);

            if (subscription == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("La suscripción con ID " + subscriptionId + " no existe.");
            }

            // Construye la respuesta con la información de la suscripción
            Map<String, Object> subscriptionData = new HashMap<>();
            subscriptionData.put("id", subscription.getId());
            subscriptionData.put("name", subscription.getName());
            subscriptionData.put("display_name", subscription.getDisplayName());

            // Si el directorio está presente, añade su información
            if (subscription.getDirectory() != null) {
                Map<String, Object> directoryData = new HashMap<>();
                directoryData.put("directory_id", subscription.getDirectory().getId());
                directoryData.put("name", subscription.getDirectory().getName());
                subscriptionData.put("directory", directoryData);
            }

            // Añade la lista de máquinas virtuales asociadas (si las hay)
            if (subscription.getVirtualMachines() != null && !subscription.getVirtualMachines().isEmpty()) {
                List<Map<String, Object>> vmList = new ArrayList<>();
                for (VirtualMachineDataModelAzure vm : subscription.getVirtualMachines()) {
                    Map<String, Object> vmData = new HashMap<>();
                    vmData.put("machine_id", vm.getId());
                    vmData.put("name", vm.getName());
                    vmData.put("machine_type", vm.getVmSize());
                    vmData.put("zone", vm.getLocation());
                    vmData.put("status", vm.getStatus());
                    vmList.add(vmData);
                }
                subscriptionData.put("virtual_machines", vmList);
            } else {
                subscriptionData.put("virtual_machines", List.of());
            }

            return ResponseEntity.ok(subscriptionData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener la suscripción: " + e.getMessage());
        }
    }
}



