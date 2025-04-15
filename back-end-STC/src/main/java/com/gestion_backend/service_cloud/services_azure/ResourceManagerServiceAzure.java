package com.gestion_backend.service_cloud.services_azure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion_backend.controller_bd.IniciarBD;
import com.gestion_backend.dataModels.modelsAzure.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Component
public class ResourceManagerServiceAzure {

    @Autowired
    private IniciarBD iniciarBD;

    @Autowired
    private CloudResourceServiceAzure cloudResourceServiceAzure;

    @PostConstruct
    public void init() {
        try {
            // Crear la base de datos y las tablas si no existen
            iniciarBD.creacionBD();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para sincronizar el usuario con los proyectos
    public void syncUserSubscriptionsAndResources(AzureCloudCredentials credentials, String email, String tenantId) {
        try {
            // Guardar el directorio (tenant) si no existe
            cloudResourceServiceAzure.saveDirectory(tenantId);
            DirectoryDataModelAzure directory = cloudResourceServiceAzure.getDirectory(tenantId);

            if (directory == null) {
                throw new RuntimeException("No se pudo crear o recuperar el directorio: " + tenantId);
            }

            // Obtener suscripciones asociadas al token y tenant
            List<Map<String, Object>> subscriptions = cloudResourceServiceAzure.getSubscriptions(
                    credentials.getAccessToken(),
                    tenantId
            );

            // Recorrer y procesar cada suscripción
            for (Map<String, Object> subscription : subscriptions) {
                String subscriptionId = (String) subscription.get("subscriptionId");
                String displayName = (String) subscription.get("displayName");
                String state = (String) subscription.get("state");

                try {
                    // Verificar si la suscripción está activa antes de extraer recursos
                    if (isSubscriptionActive(state)) {
                        // Guardar la suscripción en la base de datos
                        SubscriptionDataModelAzure subscriptionModel = new SubscriptionDataModelAzure();
                        subscriptionModel.setId(subscriptionId);
                        subscriptionModel.setName(displayName);
                        subscriptionModel.setDisplayName(displayName);
                        subscriptionModel.setDirectory(directory);

                        cloudResourceServiceAzure.saveSubscription(subscriptionModel);

                        // Obtener la suscripción guardada para usarla en la extracción de recursos
                        SubscriptionDataModelAzure savedSubscription = cloudResourceServiceAzure.getSubscription(subscriptionId);

                        if (savedSubscription == null) {
                            throw new RuntimeException("No se pudo recuperar la suscripción guardada: " + subscriptionId);
                        }

                        // Extraer todos los recursos de Azure
                        extraerResourceGroups(credentials, subscriptionId, tenantId, savedSubscription);
                        extraerVirtualMachines(credentials, subscriptionId, tenantId, savedSubscription);
                        extraerVirtualNetworks(credentials, subscriptionId, tenantId, savedSubscription);
                        extraerDiscos(credentials, subscriptionId, tenantId, savedSubscription);
                        extraerSnapshots(credentials, subscriptionId, tenantId, savedSubscription);
                        extraerLoadBalancers(credentials, subscriptionId, tenantId, savedSubscription);
                        extraerVpnGateways(credentials, subscriptionId, tenantId, savedSubscription);
                        extraerNetworkSecurityGroups(credentials, subscriptionId, tenantId, savedSubscription);
                        extraerAksClusters(credentials, subscriptionId, tenantId, savedSubscription);

                        System.out.println("Recursos extraídos correctamente para la suscripción: " + subscriptionId);
                    } else {
                        System.err.println("La suscripción no está activa: " + subscriptionId);
                    }
                } catch (Exception e) {
                    System.err.println("Error procesando suscripción " + subscriptionId + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error sincronizando recursos de Azure: " + e.getMessage(), e);
        }
    }

    // Método para comprobar si la suscripción es accesible o no
    private boolean isSubscriptionActive(String state) {
        // Comprobar si el estado es "Enabled" o similar
        return "Enabled".equalsIgnoreCase(state) || "Active".equalsIgnoreCase(state);
    }

    // Método para comprobar si la suscripción es accesible o no usando credentials
    public boolean isSubscriptionActive(AzureCloudCredentials credentials, String subscriptionId, String tenantId) {
        try {
            // Obtener todas las suscripciones y buscar la que coincida con el ID proporcionado
            List<Map<String, Object>> subscriptions = cloudResourceServiceAzure.getSubscriptions(
                    credentials.getAccessToken(),
                    tenantId
            );

            // Buscar la suscripción específica por su ID
            for (Map<String, Object> subscription : subscriptions) {
                if (subscriptionId.equals(subscription.get("subscriptionId"))) {
                    // Verificar si el estado es "Enabled"
                    return "Enabled".equalsIgnoreCase((String) subscription.get("state"));
                }
            }

            // Si no se encuentra la suscripción
            System.err.println("Suscripción no encontrada: " + subscriptionId);
            return false;
        } catch (Exception e) {
            System.err.println("Acceso denegado o error al verificar suscripción " + subscriptionId + ": " + e.getMessage());
            return false;
        }
    }

    private void extraerResourceGroups(AzureCloudCredentials credentials, String subscriptionId, String tenantId, SubscriptionDataModelAzure subscription) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://management.azure.com/subscriptions/" + subscriptionId + "/resourcegroups?api-version=2021-04-01"))
                    .header("Authorization", "Bearer " + credentials.getAccessToken())
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode valueNode = root.get("value");

                if (valueNode != null && valueNode.isArray()) {
                    for (JsonNode groupNode : valueNode) {
                        String id = groupNode.get("id").asText();
                        String name = groupNode.get("name").asText();
                        String location = groupNode.get("location").asText();
                        String provisioningState = groupNode.has("properties") ?
                                groupNode.get("properties").get("provisioningState").asText() : "Unknown";
                        String detailsJson = groupNode.toString();

                        cloudResourceServiceAzure.saveResourceGroup(id, name, subscription, location, provisioningState, detailsJson);
                    }
                }
            } else {
                throw new RuntimeException("Error al obtener los grupos de recursos: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo grupos de recursos: " + e.getMessage(), e);
        }
    }

    private void extraerVirtualMachines(AzureCloudCredentials credentials, String subscriptionId, String tenantId, SubscriptionDataModelAzure subscription) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://management.azure.com/subscriptions/" + subscriptionId + "/providers/Microsoft.Compute/virtualMachines?api-version=2021-03-01"))
                    .header("Authorization", "Bearer " + credentials.getAccessToken())
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode valueNode = root.get("value");

                if (valueNode != null && valueNode.isArray()) {
                    for (JsonNode vmNode : valueNode) {
                        String id = vmNode.get("id").asText();
                        String name = vmNode.get("name").asText();
                        String location = vmNode.get("location").asText();
                        String vmSize = vmNode.get("properties").get("hardwareProfile").get("vmSize").asText();
                        String status = "Unknown";
                        if (vmNode.has("properties") && vmNode.get("properties").has("provisioningState")) {
                            status = vmNode.get("properties").get("provisioningState").asText();
                        }
                        String detailsJson = vmNode.toString();

                        cloudResourceServiceAzure.saveVirtualMachine(id, name, null, subscription, vmSize, location, status, detailsJson);
                    }
                }
            } else {
                throw new RuntimeException("Error al obtener las máquinas virtuales: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo máquinas virtuales: " + e.getMessage(), e);
        }
    }

    private void extraerVirtualNetworks(AzureCloudCredentials credentials, String subscriptionId, String tenantId, SubscriptionDataModelAzure subscription) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://management.azure.com/subscriptions/" + subscriptionId + "/providers/Microsoft.Network/virtualNetworks?api-version=2021-03-01"))
                    .header("Authorization", "Bearer " + credentials.getAccessToken())
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode valueNode = root.get("value");

                if (valueNode != null && valueNode.isArray()) {
                    for (JsonNode vnetNode : valueNode) {
                        String id = vnetNode.get("id").asText();
                        String name = vnetNode.get("name").asText();
                        String location = vnetNode.get("location").asText();
                        String addressSpace = vnetNode.get("properties").get("addressSpace").get("addressPrefixes").toString();
                        boolean enableDdosProtection = vnetNode.get("properties").get("enableDdosProtection").asBoolean();
                        boolean enableVmProtection = vnetNode.get("properties").get("enableVmProtection").asBoolean();
                        String description = vnetNode.has("properties") && vnetNode.get("properties").has("description") ?
                                vnetNode.get("properties").get("description").asText() : "";
                        String detailsJson = vnetNode.toString();

                        cloudResourceServiceAzure.saveVirtualNetwork(id, name, null, subscription, addressSpace, enableDdosProtection, enableVmProtection, description, detailsJson);
                    }
                }
            } else {
                throw new RuntimeException("Error al obtener las redes virtuales: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo redes virtuales: " + e.getMessage(), e);
        }
    }

    private void extraerDiscos(AzureCloudCredentials credentials, String subscriptionId, String tenantId, SubscriptionDataModelAzure subscription) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://management.azure.com/subscriptions/" + subscriptionId + "/providers/Microsoft.Compute/disks?api-version=2021-03-01"))
                    .header("Authorization", "Bearer " + credentials.getAccessToken())
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode valueNode = root.get("value");

                if (valueNode != null && valueNode.isArray()) {
                    for (JsonNode diskNode : valueNode) {
                        String id = diskNode.get("id").asText();
                        String name = diskNode.get("name").asText();
                        String location = diskNode.get("location").asText();
                        String diskType = diskNode.get("properties").get("creationData").get("createOption").asText();
                        long sizeGb = diskNode.get("properties").get("diskSizeGB").asLong();
                        String status = diskNode.get("properties").get("provisioningState").asText();
                        String detailsJson = diskNode.toString();

                        cloudResourceServiceAzure.saveDisk(id, name, null, subscription, diskType, location, sizeGb, status, detailsJson);
                    }
                }
            } else {
                throw new RuntimeException("Error al obtener los discos: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo discos: " + e.getMessage(), e);
        }
    }

    private void extraerSnapshots(AzureCloudCredentials credentials, String subscriptionId, String tenantId, SubscriptionDataModelAzure subscription) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://management.azure.com/subscriptions/" + subscriptionId + "/providers/Microsoft.Compute/snapshots?api-version=2021-03-01"))
                    .header("Authorization", "Bearer " + credentials.getAccessToken())
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode valueNode = root.get("value");

                if (valueNode != null && valueNode.isArray()) {
                    for (JsonNode snapshotNode : valueNode) {
                        String id = snapshotNode.get("id").asText();
                        String name = snapshotNode.get("name").asText();
                        String location = snapshotNode.get("location").asText();
                        String sourceDiskId = snapshotNode.get("properties").get("creationData").get("sourceResourceId").asText();
                        long diskSizeGb = snapshotNode.get("properties").get("diskSizeGB").asLong();
                        String status = snapshotNode.get("properties").get("provisioningState").asText();
                        String storageAccountType = snapshotNode.get("properties").get("storageAccountType").asText();
                        String detailsJson = snapshotNode.toString();

                        cloudResourceServiceAzure.saveSnapshot(id, name, null, subscription, sourceDiskId, diskSizeGb, status, storageAccountType, detailsJson);
                    }
                }
            } else {
                throw new RuntimeException("Error al obtener las snapshots: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo snapshots: " + e.getMessage(), e);
        }
    }

    private void extraerLoadBalancers(AzureCloudCredentials credentials, String subscriptionId, String tenantId, SubscriptionDataModelAzure subscription) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://management.azure.com/subscriptions/" + subscriptionId + "/providers/Microsoft.Network/loadBalancers?api-version=2021-03-01"))
                    .header("Authorization", "Bearer " + credentials.getAccessToken())
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode valueNode = root.get("value");

                if (valueNode != null && valueNode.isArray()) {
                    for (JsonNode lbNode : valueNode) {
                        String id = lbNode.get("id").asText();
                        String name = lbNode.get("name").asText();
                        String location = lbNode.get("location").asText();
                        String sku = lbNode.get("properties").get("sku").get("name").asText();
                        String frontendIpConfiguration = lbNode.get("properties").get("frontendIPConfigurations").toString();
                        String backendAddressPools = lbNode.get("properties").get("backendAddressPools").toString();
                        String probeProtocol = lbNode.get("properties").get("probes").get(0).get("properties").get("protocol").asText();
                        int probePort = lbNode.get("properties").get("probes").get(0).get("properties").get("port").asInt();
                        String status = lbNode.get("properties").get("provisioningState").asText();
                        String description = lbNode.has("properties") && lbNode.get("properties").has("description") ?
                                lbNode.get("properties").get("description").asText() : "";
                        String detailsJson = lbNode.toString();

                        cloudResourceServiceAzure.saveLoadBalancer(id, name, null, subscription, location, sku, frontendIpConfiguration, backendAddressPools, probeProtocol, probePort, status, description, detailsJson);
                    }
                }
            } else {
                throw new RuntimeException("Error al obtener los balanceadores de carga: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo balanceadores de carga: " + e.getMessage(), e);
        }
    }

    private void extraerVpnGateways(AzureCloudCredentials credentials, String subscriptionId, String tenantId, SubscriptionDataModelAzure subscription) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://management.azure.com/subscriptions/" + subscriptionId + "/providers/Microsoft.Network/vpnGateways?api-version=2021-03-01"))
                    .header("Authorization", "Bearer " + credentials.getAccessToken())
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode valueNode = root.get("value");

                if (valueNode != null && valueNode.isArray()) {
                    for (JsonNode vpnNode : valueNode) {
                        String id = vpnNode.get("id").asText();
                        String name = vpnNode.get("name").asText();
                        String location = vpnNode.get("location").asText();
                        String type = vpnNode.get("properties").get("gatewayType").asText();
                        String status = vpnNode.get("properties").get("provisioningState").asText();
                        String detailsJson = vpnNode.toString();

                        cloudResourceServiceAzure.saveVpnGateway(id, name, null, subscription, location, type, status, detailsJson);
                    }
                }
            } else {
                throw new RuntimeException("Error al obtener las puertas de enlace VPN: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo puertas de enlace VPN: " + e.getMessage(), e);
        }
    }

    private void extraerNetworkSecurityGroups(AzureCloudCredentials credentials, String subscriptionId, String tenantId, SubscriptionDataModelAzure subscription) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://management.azure.com/subscriptions/" + subscriptionId + "/providers/Microsoft.Network/networkSecurityGroups?api-version=2021-03-01"))
                    .header("Authorization", "Bearer " + credentials.getAccessToken())
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode valueNode = root.get("value");

                if (valueNode != null && valueNode.isArray()) {
                    for (JsonNode nsgNode : valueNode) {
                        String id = nsgNode.get("id").asText();
                        String name = nsgNode.get("name").asText();
                        String location = nsgNode.get("location").asText();
                        String securityRulesJson = nsgNode.get("properties").get("securityRules").toString();
                        String detailsJson = nsgNode.toString();

                        cloudResourceServiceAzure.saveNetworkSecurityGroup(id, name, null, subscription, location, securityRulesJson, detailsJson);
                    }
                }
            } else {
                throw new RuntimeException("Error al obtener los grupos de seguridad de red: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo grupos de seguridad de red: " + e.getMessage(), e);
        }
    }

    private void extraerAksClusters(AzureCloudCredentials credentials, String subscriptionId, String tenantId, SubscriptionDataModelAzure subscription) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://management.azure.com/subscriptions/" + subscriptionId + "/providers/Microsoft.ContainerService/managedClusters?api-version=2021-03-01"))
                    .header("Authorization", "Bearer " + credentials.getAccessToken())
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode valueNode = root.get("value");

                if (valueNode != null && valueNode.isArray()) {
                    for (JsonNode aksNode : valueNode) {
                        String id = aksNode.get("id").asText();
                        String name = aksNode.get("name").asText();
                        String location = aksNode.get("location").asText();
                        String kubernetesVersion = aksNode.get("properties").get("kubernetesVersion").asText();
                        String status = aksNode.get("properties").get("provisioningState").asText();
                        String detailsJson = aksNode.toString();

                        cloudResourceServiceAzure.saveAksCluster(id, name, null, subscription, location, kubernetesVersion, status, detailsJson);
                    }
                }
            } else {
                throw new RuntimeException("Error al obtener los clusters AKS: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo clusters AKS: " + e.getMessage(), e);
        }
    }

    // Este método ya no es necesario ya que hacemos la extracción directamente en syncUserSubscriptionsAndResources
    @Deprecated
    public void extractAzureResources(AzureCloudCredentials credentials, String subscriptionId, String tenantId) {
        try {
            validateSubscriptionId(subscriptionId);

            // Obtener el modelo de suscripción
            SubscriptionDataModelAzure subscription = cloudResourceServiceAzure.getSubscription(subscriptionId);

            if (subscription == null) {
                throw new RuntimeException("No se pudo recuperar la suscripción: " + subscriptionId);
            }

            // Este método está deprecado, ahora la extracción se hace directamente en syncUserSubscriptionsAndResources
            System.out.println("Este método está deprecado, utilice syncUserSubscriptionsAndResources");

            System.out.println("Recursos extraídos de la cuenta de Azure: " + subscriptionId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error extrayendo recursos de la suscripción " + subscriptionId + ": " + e.getMessage());
        }
    }

    private void validateSubscriptionId(String subscriptionId) {
        if (subscriptionId == null || subscriptionId.isEmpty()) {
            throw new IllegalArgumentException("El ID de la suscripción no puede estar vacío.");
        }
    }
}

