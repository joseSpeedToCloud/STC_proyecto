package com.gestion_backend.service_cloud.services_azure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion_backend.dataModels.modelsAzure.*;
import com.gestion_backend.repository_cloud.repository_azure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class CloudResourceServiceAzure {

    // Inyección de dependencias de los repositorios para manejar las entidades
    @Autowired
    private DirectoryRepositoryAzure directoryRepository;
    @Autowired
    private SubscriptionRepositoryAzure subscriptionRepositoryAzure;
    @Autowired
    private ResourceGroupRepositoryAzure resourceGroupRepositoryAzure;
    @Autowired
    private VirtualMachineRepositoryAzure virtualMachineRepositoryAzure;
    @Autowired
    private VirtualNetworkRepositoryAzure virtualNetworkRepositoryAzure;
    @Autowired
    private SubnetRepositoryAzure subnetRepositoryAzure;
    @Autowired
    private DiskRepositoryAzure diskRepositoryAzure;
    @Autowired
    private SnapshotRepositoryAzure snapshotRepositoryAzure;
    @Autowired
    private LoadBalancerRepositoryAzure loadBalancerRepositoryAzure;
    @Autowired
    private VpnGatewayRepositoryAzure vpnGatewayRepositoryAzure;
    @Autowired
    private NetworkSecurityGroupRepositoryAzure networkSecurityGroupRepositoryAzure;
    @Autowired
    private AksClusterRepositoryAzure aksClusterRepositoryAzure;

    //obtengo las suscripciones de Azure para un tenant especifico
    public List<Map<String, Object>> getSubscriptions(String accessToken, String tenantId) {
        List<Map<String, Object>> subscriptions = new ArrayList<>();

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://management.azure.com/subscriptions?api-version=2020-01-01"))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode valueNode = root.get("value");

                if (valueNode != null && valueNode.isArray()) {
                    for (JsonNode subNode : valueNode) {
                        Map<String, Object> subscription = new HashMap<>();
                        subscription.put("subscriptionId", subNode.get("subscriptionId").asText());
                        subscription.put("displayName", subNode.get("displayName").asText());
                        subscription.put("state", subNode.get("state").asText());

                        // Añadir solo si el tenantId coincide
                        if (subNode.has("tenantId") && subNode.get("tenantId").asText().equals(tenantId)) {
                            subscriptions.add(subscription);
                        }
                    }
                }
            } else {
                throw new RuntimeException("Error al obtener suscripciones: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo suscripciones: " + e.getMessage(), e);
        }

        return subscriptions;
    }

    //obtengo un directorio por su nombre
    public DirectoryDataModelAzure getDirectory(String name) {
        return directoryRepository.findByName(name);
    }

    //guardo un directorio en la base de datos
    @Transactional
    public void saveDirectory(String name) {
        DirectoryDataModelAzure directory = getDirectory(name);
        if (directory == null) {
            directory = new DirectoryDataModelAzure();
            directory.setName(name);
        }
        directoryRepository.save(directory);
    }

    //obtengo los directorios
    public List<DirectoryDataModelAzure> getAllDirectories() {
        return directoryRepository.findAll();
    }

    //obtengo una suscripcion por su ID
    public SubscriptionDataModelAzure getSubscription(String id) {
        return subscriptionRepositoryAzure.findById(id).orElse(null);
    }

    //obtengo una suscripcion por su nombre
    public SubscriptionDataModelAzure getSubscriptionByName(String name) {
        return subscriptionRepositoryAzure.findByName(name);
    }

    //guardo una suscripción en la base de datos vinculándola con un directorio
    @Transactional
    public void saveSubscription(SubscriptionDataModelAzure subscriptionDataModelAzure) {
        SubscriptionDataModelAzure existingSubscription = getSubscription(subscriptionDataModelAzure.getId());
        if (existingSubscription != null) {
            existingSubscription.setName(subscriptionDataModelAzure.getName());
            existingSubscription.setDisplayName(subscriptionDataModelAzure.getDisplayName());
            existingSubscription.setDirectory(subscriptionDataModelAzure.getDirectory());
            subscriptionRepositoryAzure.save(existingSubscription);
        } else {
            subscriptionRepositoryAzure.save(subscriptionDataModelAzure);
        }
    }

    //obtengo todas las suscripciones
    public List<SubscriptionDataModelAzure> getAllSubscriptions() {
        return subscriptionRepositoryAzure.findAll();
    }

    //obtengo todas las suscripciones asociadas a un directorio
    public List<SubscriptionDataModelAzure> getSubscriptionsByDirectory(Integer directoryId) {
        return subscriptionRepositoryAzure.findByDirectory_Id(directoryId);
    }

    //obtengo un grupo de recursos por su ID
    public ResourceGroupDataModelAzure getResourceGroup(String id) {
        return resourceGroupRepositoryAzure.findById(id).orElse(null);
    }

    //obtengo un grupo de recursos por su nombre y suscripcion
    public ResourceGroupDataModelAzure getResourceGroupByNameAndSubscription(String name, String subscriptionId) {
        return resourceGroupRepositoryAzure.findByNameAndSubscription_Id(name, subscriptionId);
    }

    //guardo un grupo de recursos en la base de datos
    @Transactional
    public void saveResourceGroup(String id, String name, SubscriptionDataModelAzure subscription,
                                  String location, String provisioningState, String detailsJson) {
        ResourceGroupDataModelAzure existingResourceGroup = getResourceGroupByNameAndSubscription(name, subscription.getId());

        if (existingResourceGroup != null) {
            existingResourceGroup.setId(id);
            existingResourceGroup.setLocation(location);
            existingResourceGroup.setProvisioningState(provisioningState);
            existingResourceGroup.setDetails(detailsJson);
            resourceGroupRepositoryAzure.save(existingResourceGroup);
        } else {
            ResourceGroupDataModelAzure newResourceGroup = new ResourceGroupDataModelAzure();
            newResourceGroup.setId(id);
            newResourceGroup.setName(name);
            newResourceGroup.setSubscription(subscription);
            newResourceGroup.setLocation(location);
            newResourceGroup.setProvisioningState(provisioningState);
            newResourceGroup.setDetails(detailsJson);
            resourceGroupRepositoryAzure.save(newResourceGroup);
        }
    }

    //obtengo todos los grupos de recursos
    public List<ResourceGroupDataModelAzure> getAllResourceGroups() {
        return resourceGroupRepositoryAzure.findAll();
    }

    //obtengo todos los grupos de recursos asociados a una suscripción
    public List<ResourceGroupDataModelAzure> getResourceGroupsBySubscription(String subscriptionId) {
        return resourceGroupRepositoryAzure.findBySubscription_Id(subscriptionId);
    }

    //obtengo una maquina virtual por su ID
    public VirtualMachineDataModelAzure getVirtualMachine(String id) {
        return virtualMachineRepositoryAzure.findById(id).orElse(null);
    }

    //obtengo una maquina virtual por su nombre y suscripción
    public VirtualMachineDataModelAzure getVirtualMachineByNameAndSubscription(String name, String subscriptionId) {
        return virtualMachineRepositoryAzure.findByNameAndSubscription_Id(name, subscriptionId);
    }

    //guardo una maquina virtual en la base de datos
    @Transactional
    public void saveVirtualMachine(String id, String name, ResourceGroupDataModelAzure resourceGroup,
                                   SubscriptionDataModelAzure subscription, String vmSize,
                                   String location, String status, String detailsJson) {
        VirtualMachineDataModelAzure existingVM = getVirtualMachineByNameAndSubscription(name, subscription.getId());

        if (existingVM != null) {
            existingVM.setId(id);
            existingVM.setResourceGroup(resourceGroup);
            existingVM.setVmSize(vmSize);
            existingVM.setLocation(location);
            existingVM.setStatus(status);
            existingVM.setDetails(detailsJson);
            virtualMachineRepositoryAzure.save(existingVM);
        } else {
            VirtualMachineDataModelAzure newVM = new VirtualMachineDataModelAzure();
            newVM.setId(id);
            newVM.setName(name);
            newVM.setResourceGroup(resourceGroup);
            newVM.setSubscription(subscription);
            newVM.setVmSize(vmSize);
            newVM.setLocation(location);
            newVM.setStatus(status);
            newVM.setDetails(detailsJson);
            virtualMachineRepositoryAzure.save(newVM);
        }
    }

    //obtengo todas las máquinas virtuales
    public List<VirtualMachineDataModelAzure> getAllVirtualMachines() {
        return virtualMachineRepositoryAzure.findAll();
    }

    //obtengo todas las máquinas virtuales asociadas a una suscripcion
    public List<VirtualMachineDataModelAzure> getVirtualMachinesBySubscription(String subscriptionId) {
        return virtualMachineRepositoryAzure.findBySubscription_Id(subscriptionId);
    }

    //obtengo todas las máquinas virtuales asociadas a un grupo de recursos
    public List<VirtualMachineDataModelAzure> getVirtualMachinesByResourceGroup(String resourceGroupId) {
        return virtualMachineRepositoryAzure.findByResourceGroup_Id(resourceGroupId);
    }

    //obtengo una red virtual por su ID
    public VirtualNetworkDataModelAzure getVirtualNetwork(String id) {
        return virtualNetworkRepositoryAzure.findById(id).orElse(null);
    }

    //obtengo una red virtual por su nombre y suscripcion
    public VirtualNetworkDataModelAzure getVirtualNetworkByNameAndSubscription(String name, String subscriptionId) {
        return virtualNetworkRepositoryAzure.findByNameAndSubscription_Id(name, subscriptionId);
    }

    //guardo una red virtual en la base de datos
    @Transactional
    public void saveVirtualNetwork(String id, String name, ResourceGroupDataModelAzure resourceGroup,
                                   SubscriptionDataModelAzure subscription, String addressSpace,
                                   Boolean enableDdosProtection, Boolean enableVmProtection,
                                   String description, String detailsJson) {
        VirtualNetworkDataModelAzure existingVNet = getVirtualNetworkByNameAndSubscription(name, subscription.getId());

        if (existingVNet != null) {
            existingVNet.setId(id);
            existingVNet.setResourceGroup(resourceGroup);
            existingVNet.setAddressSpace(addressSpace);
            existingVNet.setEnableDdosProtection(enableDdosProtection);
            existingVNet.setEnableVmProtection(enableVmProtection);
            existingVNet.setDescription(description);
            existingVNet.setDetails(detailsJson);
            virtualNetworkRepositoryAzure.save(existingVNet);
        } else {
            VirtualNetworkDataModelAzure newVNet = new VirtualNetworkDataModelAzure();
            newVNet.setId(id);
            newVNet.setName(name);
            newVNet.setResourceGroup(resourceGroup);
            newVNet.setSubscription(subscription);
            newVNet.setAddressSpace(addressSpace);
            newVNet.setEnableDdosProtection(enableDdosProtection);
            newVNet.setEnableVmProtection(enableVmProtection);
            newVNet.setDescription(description);
            newVNet.setDetails(detailsJson);
            virtualNetworkRepositoryAzure.save(newVNet);
        }
    }

    //obtengo todas las redes virtuales
    public List<VirtualNetworkDataModelAzure> getAllVirtualNetworks() {
        return virtualNetworkRepositoryAzure.findAll();
    }

    //obtengo todas las redes virtuales asociadas a una suscripcion
    public List<VirtualNetworkDataModelAzure> getVirtualNetworksBySubscription(String subscriptionId) {
        return virtualNetworkRepositoryAzure.findBySubscription_Id(subscriptionId);
    }

    //obtengo todas las redes virtuales asociadas a un grupo de recursos
    public List<VirtualNetworkDataModelAzure> getVirtualNetworksByResourceGroup(String resourceGroupId) {
        return virtualNetworkRepositoryAzure.findByResourceGroup_Id(resourceGroupId);
    }

    //obtengo una subred por su ID
    public SubnetDataModelAzure getSubnet(String id) {
        return subnetRepositoryAzure.findById(id).orElse(null);
    }

    //obtengo una subred por su nombre y suscripcion
    public SubnetDataModelAzure getSubnetByNameAndSubscription(String name, String subscriptionId) {
        return subnetRepositoryAzure.findByNameAndSubscription_Id(name, subscriptionId);
    }

    //guardo una subred en la base de datos
    @Transactional
    public void saveSubnet(String id, String name, VirtualNetworkDataModelAzure virtualNetwork,
                           SubscriptionDataModelAzure subscription, String addressPrefix,
                           String privateEndpointNetworkPolicies, String privateLinkServiceNetworkPolicies,
                           String description, String detailsJson) {
        SubnetDataModelAzure existingSubnet = getSubnetByNameAndSubscription(name, subscription.getId());

        if (existingSubnet != null) {
            existingSubnet.setId(id);
            existingSubnet.setVirtualNetwork(virtualNetwork);
            existingSubnet.setAddressPrefix(addressPrefix);
            existingSubnet.setPrivateEndpointNetworkPolicies(privateEndpointNetworkPolicies);
            existingSubnet.setPrivateLinkServiceNetworkPolicies(privateLinkServiceNetworkPolicies);
            existingSubnet.setDescription(description);
            existingSubnet.setDetails(detailsJson);
            subnetRepositoryAzure.save(existingSubnet);
        } else {
            SubnetDataModelAzure newSubnet = new SubnetDataModelAzure();
            newSubnet.setId(id);
            newSubnet.setName(name);
            newSubnet.setVirtualNetwork(virtualNetwork);
            newSubnet.setSubscription(subscription);
            newSubnet.setAddressPrefix(addressPrefix);
            newSubnet.setPrivateEndpointNetworkPolicies(privateEndpointNetworkPolicies);
            newSubnet.setPrivateLinkServiceNetworkPolicies(privateLinkServiceNetworkPolicies);
            newSubnet.setDescription(description);
            newSubnet.setDetails(detailsJson);
            subnetRepositoryAzure.save(newSubnet);
        }
    }

    //obtengo todas las subredes
    public List<SubnetDataModelAzure> getAllSubnets() {
        return subnetRepositoryAzure.findAll();
    }

    //obtengo todas las subredes asociadas a una red virtual
    public List<SubnetDataModelAzure> getSubnetsByVirtualNetwork(String virtualNetworkId) {
        return subnetRepositoryAzure.findByVirtualNetwork_Id(virtualNetworkId);
    }

    //obtengo todas las subredes asociadas a una suscripcion
    public List<SubnetDataModelAzure> getSubnetsBySubscription(String subscriptionId) {
        return subnetRepositoryAzure.findBySubscription_Id(subscriptionId);
    }

    //obtengo un disco por su ID
    public DiskDataModelAzure getDisk(String id) {
        return diskRepositoryAzure.findById(id).orElse(null);
    }

    //obtengo un disco por su nombre y suscripcion
    public DiskDataModelAzure getDiskByNameAndSubscription(String name, String subscriptionId) {
        return diskRepositoryAzure.findByNameAndSubscription_Id(name, subscriptionId);
    }

    //guardo un disco en la base de datos
    @Transactional
    public void saveDisk(String id, String name, ResourceGroupDataModelAzure resourceGroup,
                         SubscriptionDataModelAzure subscription, String diskType,
                         String location, Long sizeGb, String status, String detailsJson) {
        DiskDataModelAzure existingDisk = getDiskByNameAndSubscription(name, subscription.getId());

        if (existingDisk != null) {
            existingDisk.setId(id);
            existingDisk.setResourceGroup(resourceGroup);
            existingDisk.setDiskType(diskType);
            existingDisk.setLocation(location);
            existingDisk.setSizeGb(sizeGb);
            existingDisk.setStatus(status);
            existingDisk.setDetails(detailsJson);
            diskRepositoryAzure.save(existingDisk);
        } else {
            DiskDataModelAzure newDisk = new DiskDataModelAzure();
            newDisk.setId(id);
            newDisk.setName(name);
            newDisk.setResourceGroup(resourceGroup);
            newDisk.setSubscription(subscription);
            newDisk.setDiskType(diskType);
            newDisk.setLocation(location);
            newDisk.setSizeGb(sizeGb);
            newDisk.setStatus(status);
            newDisk.setDetails(detailsJson);
            diskRepositoryAzure.save(newDisk);
        }
    }

    //obtengo todos los discos
    public List<DiskDataModelAzure> getAllDisks() {
        return diskRepositoryAzure.findAll();
    }

    //obtengo todos los discos asociados a una suscripcion
    public List<DiskDataModelAzure> getDisksBySubscription(String subscriptionId) {
        return diskRepositoryAzure.findBySubscription_Id(subscriptionId);
    }

    //obtengo todos los discos asociados a un grupo de recursos
    public List<DiskDataModelAzure> getDisksByResourceGroup(String resourceGroupId) {
        return diskRepositoryAzure.findByResourceGroup_Id(resourceGroupId);
    }

    //obtengo una instantánea por su ID
    public SnapshotDataModelAzure getSnapshot(String id) {
        return snapshotRepositoryAzure.findById(id).orElse(null);
    }

    //obtengo una instantánea por su nombre y suscripcion
    public SnapshotDataModelAzure getSnapshotByNameAndSubscription(String name, String subscriptionId) {
        return snapshotRepositoryAzure.findByNameAndSubscription_Id(name, subscriptionId);
    }

    //guardo una snapshot en la base de datos
    @Transactional
    public void saveSnapshot(String id, String name, ResourceGroupDataModelAzure resourceGroup,
                             SubscriptionDataModelAzure subscription, String sourceDiskId,
                             Long diskSizeGb, String status, String storageAccountType, String detailsJson) {
        SnapshotDataModelAzure existingSnapshot = getSnapshotByNameAndSubscription(name, subscription.getId());

        if (existingSnapshot != null) {
            existingSnapshot.setId(id);
            existingSnapshot.setResourceGroup(resourceGroup);
            existingSnapshot.setSourceDiskId(sourceDiskId);
            existingSnapshot.setDiskSizeGb(diskSizeGb);
            existingSnapshot.setStatus(status);
            existingSnapshot.setStorageAccountType(storageAccountType);
            existingSnapshot.setDetails(detailsJson);
            snapshotRepositoryAzure.save(existingSnapshot);
        } else {
            SnapshotDataModelAzure newSnapshot = new SnapshotDataModelAzure();
            newSnapshot.setId(id);
            newSnapshot.setName(name);
            newSnapshot.setResourceGroup(resourceGroup);
            newSnapshot.setSubscription(subscription);
            newSnapshot.setSourceDiskId(sourceDiskId);
            newSnapshot.setDiskSizeGb(diskSizeGb);
            newSnapshot.setStatus(status);
            newSnapshot.setStorageAccountType(storageAccountType);
            newSnapshot.setDetails(detailsJson);
            snapshotRepositoryAzure.save(newSnapshot);
        }
    }

    //obtengo todas las snapshots
    public List<SnapshotDataModelAzure> getAllSnapshots() {
        return snapshotRepositoryAzure.findAll();
    }

    //obtengo todas las snapshots asociadas a una suscripcion
    public List<SnapshotDataModelAzure> getSnapshotsBySubscription(String subscriptionId) {
        return snapshotRepositoryAzure.findBySubscription_Id(subscriptionId);
    }

    //obtengo todas las snapshots asociadas a un grupo de recursos
    public List<SnapshotDataModelAzure> getSnapshotsByResourceGroup(String resourceGroupId) {
        return snapshotRepositoryAzure.findByResourceGroup_Id(resourceGroupId);
    }

    //obtengo un balanceador de carga por su ID
    public LoadBalancerDataModelAzure getLoadBalancer(String id) {
        return loadBalancerRepositoryAzure.findById(id).orElse(null);
    }

    //obtengo un balanceador de carga por su nombre y suscripcion
    public LoadBalancerDataModelAzure getLoadBalancerByNameAndSubscription(String name, String subscriptionId) {
        return loadBalancerRepositoryAzure.findByNameAndSubscription_Id(name, subscriptionId);
    }

    //guardo un balanceador de carga en la base de datos
    @Transactional
    public void saveLoadBalancer(String id, String name, ResourceGroupDataModelAzure resourceGroup,
                                 SubscriptionDataModelAzure subscription, String location, String sku,
                                 String frontendIpConfiguration, String backendAddressPools,
                                 String probeProtocol, Integer probePort, String status,
                                 String description, String detailsJson) {
        LoadBalancerDataModelAzure existingLB = getLoadBalancerByNameAndSubscription(name, subscription.getId());

        if (existingLB != null) {
            existingLB.setId(id);
            existingLB.setResourceGroup(resourceGroup);
            existingLB.setLocation(location);
            existingLB.setSku(sku);
            existingLB.setFrontendIpConfiguration(frontendIpConfiguration);
            existingLB.setBackendAddressPools(backendAddressPools);
            existingLB.setProbeProtocol(probeProtocol);
            existingLB.setProbePort(probePort);
            existingLB.setStatus(status);
            existingLB.setDescription(description);
            existingLB.setDetails(detailsJson);
            loadBalancerRepositoryAzure.save(existingLB);
        } else {
            LoadBalancerDataModelAzure newLB = new LoadBalancerDataModelAzure();
            newLB.setId(id);
            newLB.setName(name);
            newLB.setResourceGroup(resourceGroup);
            newLB.setSubscription(subscription);
            newLB.setLocation(location);
            newLB.setSku(sku);
            newLB.setFrontendIpConfiguration(frontendIpConfiguration);
            newLB.setBackendAddressPools(backendAddressPools);
            newLB.setProbeProtocol(probeProtocol);
            newLB.setProbePort(probePort);
            newLB.setStatus(status);
            newLB.setDescription(description);
            newLB.setDetails(detailsJson);
            loadBalancerRepositoryAzure.save(newLB);
        }
    }

    //obtengo todos los balanceadores de carga
    public List<LoadBalancerDataModelAzure> getAllLoadBalancers() {
        return loadBalancerRepositoryAzure.findAll();
    }

    //obtengo todos los balanceadores de carga asociados a una suscripcion
    public List<LoadBalancerDataModelAzure> getLoadBalancersBySubscription(String subscriptionId) {
        return loadBalancerRepositoryAzure.findBySubscription_Id(subscriptionId);
    }

    //obtengo todos los balanceadores de carga asociados a un grupo de recursos
    public List<LoadBalancerDataModelAzure> getLoadBalancersByResourceGroup(String resourceGroupId) {
        return loadBalancerRepositoryAzure.findByResourceGroup_Id(resourceGroupId);
    }

    //obtengo una puerta de enlace VPN por su ID
    public VpnGatewayDataModelAzure getVpnGateway(String id) {
        return vpnGatewayRepositoryAzure.findById(id).orElse(null);
    }

    //obtengo una puerta de enlace VPN por su nombre y suscripción
    public VpnGatewayDataModelAzure getVpnGatewayByNameAndSubscription(String name, String subscriptionId) {
        return vpnGatewayRepositoryAzure.findByNameAndSubscription_Id(name, subscriptionId);
    }

    //guardo una puerta de enlace VPN en la base de datos
    @Transactional
    public void saveVpnGateway(String id, String name, ResourceGroupDataModelAzure resourceGroup,
                               SubscriptionDataModelAzure subscription, String location,
                               String type, String status, String detailsJson) {
        VpnGatewayDataModelAzure existingVpn = getVpnGatewayByNameAndSubscription(name, subscription.getId());

        if (existingVpn != null) {
            existingVpn.setId(id);
            existingVpn.setResourceGroup(resourceGroup);
            existingVpn.setLocation(location);
            existingVpn.setType(type);
            existingVpn.setStatus(status);
            existingVpn.setDetails(detailsJson);
            vpnGatewayRepositoryAzure.save(existingVpn);
        } else {
            VpnGatewayDataModelAzure newVpn = new VpnGatewayDataModelAzure();
            newVpn.setId(id);
            newVpn.setName(name);
            newVpn.setResourceGroup(resourceGroup);
            newVpn.setSubscription(subscription);
            newVpn.setLocation(location);
            newVpn.setType(type);
            newVpn.setStatus(status);
            newVpn.setDetails(detailsJson);
            vpnGatewayRepositoryAzure.save(newVpn);
        }
    }

    //obtengo todas las puertas de enlace VPN
    public List<VpnGatewayDataModelAzure> getAllVpnGateways() {
        return vpnGatewayRepositoryAzure.findAll();
    }

    //obtengo todas las puertas de enlace VPN asociadas a una suscripcion
    public List<VpnGatewayDataModelAzure> getVpnGatewaysBySubscription(String subscriptionId) {
        return vpnGatewayRepositoryAzure.findBySubscription_Id(subscriptionId);
    }

    //obtengo todas las puertas de enlace VPN asociadas a un grupo de recursos
    public List<VpnGatewayDataModelAzure> getVpnGatewaysByResourceGroup(String resourceGroupId) {
        return vpnGatewayRepositoryAzure.findByResourceGroup_Id(resourceGroupId);
    }

    //obtengo un grupo de seguridad de red por su ID
    public NetworkSecurityGroupDataModelAzure getNetworkSecurityGroup(String id) {
        return networkSecurityGroupRepositoryAzure.findById(id).orElse(null);
    }

    //obtengo un grupo de seguridad de red por su nombre y suscripcion
    public NetworkSecurityGroupDataModelAzure getNetworkSecurityGroupByNameAndSubscription(String name, String subscriptionId) {
        return networkSecurityGroupRepositoryAzure.findByNameAndSubscription_Id(name, subscriptionId);
    }

    //guardo un grupo de seguridad de red en la base de datos
    @Transactional
    public void saveNetworkSecurityGroup(String id, String name, ResourceGroupDataModelAzure resourceGroup,
                                         SubscriptionDataModelAzure subscription, String location,
                                         String securityRulesJson, String detailsJson) {
        NetworkSecurityGroupDataModelAzure existingNsg = getNetworkSecurityGroupByNameAndSubscription(name, subscription.getId());

        if (existingNsg != null) {
            existingNsg.setId(id);
            existingNsg.setResourceGroup(resourceGroup);
            existingNsg.setLocation(location);
            existingNsg.setSecurityRules(securityRulesJson);
            existingNsg.setDetails(detailsJson);
            networkSecurityGroupRepositoryAzure.save(existingNsg);
        } else {
            NetworkSecurityGroupDataModelAzure newNsg = new NetworkSecurityGroupDataModelAzure();
            newNsg.setId(id);
            newNsg.setName(name);
            newNsg.setResourceGroup(resourceGroup);
            newNsg.setSubscription(subscription);
            newNsg.setLocation(location);
            newNsg.setSecurityRules(securityRulesJson);
            newNsg.setDetails(detailsJson);
            networkSecurityGroupRepositoryAzure.save(newNsg);
        }
    }

    //obtengo todos los grupos de seguridad de red
    public List<NetworkSecurityGroupDataModelAzure> getAllNetworkSecurityGroups() {
        return networkSecurityGroupRepositoryAzure.findAll();
    }

    //obtengo todos los grupos de seguridad de red asociados a una suscripcion
    public List<NetworkSecurityGroupDataModelAzure> getNetworkSecurityGroupsBySubscription(String subscriptionId) {
        return networkSecurityGroupRepositoryAzure.findBySubscription_Id(subscriptionId);
    }

    //obtengo todos los grupos de seguridad de red asociados a un grupo de recursos
    public List<NetworkSecurityGroupDataModelAzure> getNetworkSecurityGroupsByResourceGroup(String resourceGroupId) {
        return networkSecurityGroupRepositoryAzure.findByResourceGroup_Id(resourceGroupId);
    }

    //guardo un cluster AKS en la base de datos
    @Transactional
    public void saveAksCluster(String id, String name, ResourceGroupDataModelAzure resourceGroup,
                               SubscriptionDataModelAzure subscription, String location,
                               String kubernetesVersion, String status, String detailsJson) {
        AksClusterDataModelAzure existingAks = getAksClusterByNameAndSubscription(name, subscription.getId());

        if (existingAks != null) {
            existingAks.setId(id);
            existingAks.setResourceGroup(resourceGroup);
            existingAks.setLocation(location);
            existingAks.setKubernetesVersion(kubernetesVersion);
            existingAks.setStatus(status);
            existingAks.setDetails(detailsJson);
            aksClusterRepositoryAzure.save(existingAks);
        } else {
            AksClusterDataModelAzure newAks = new AksClusterDataModelAzure();
            newAks.setId(id);
            newAks.setName(name);
            newAks.setResourceGroup(resourceGroup);
            newAks.setSubscription(subscription);
            newAks.setLocation(location);
            newAks.setKubernetesVersion(kubernetesVersion);
            newAks.setStatus(status);
            newAks.setDetails(detailsJson);
            aksClusterRepositoryAzure.save(newAks);
        }
    }

    //obtengo todos los clusters AKS
    public List<AksClusterDataModelAzure> getAllAksClusters() {
        return aksClusterRepositoryAzure.findAll();
    }

    //obtengo todos los clusters AKS asociados a una suscripcion
    public List<AksClusterDataModelAzure> getAksClustersBySubscription(String subscriptionId) {
        return aksClusterRepositoryAzure.findBySubscription_Id(subscriptionId);
    }

    //obtengo todos los clusters AKS asociados a un grupo de recursos
    public List<AksClusterDataModelAzure> getAksClustersByResourceGroup(String resourceGroupId) {
        return aksClusterRepositoryAzure.findByResourceGroup_Id(resourceGroupId);
    }

    //obtengo un cluster AKS por su nombre y suscripcion
    public AksClusterDataModelAzure getAksClusterByNameAndSubscription(String name, String subscriptionId) {
        return aksClusterRepositoryAzure.findByNameAndSubscription_Id(name, subscriptionId);
    }

    //hago una peticion HTTP a una API externa
    public String callExternalApi(String url, String method, String body) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json");

            HttpRequest request;
            if (method.equalsIgnoreCase("POST")) {
                request = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body)).build();
            } else if (method.equalsIgnoreCase("PUT")) {
                request = requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(body)).build();
            } else if (method.equalsIgnoreCase("DELETE")) {
                request = requestBuilder.DELETE().build();
            } else {
                request = requestBuilder.GET().build();
            }

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //convierto un objeto a formato JSON
    public String convertToJson(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //convierto un JSON a un objeto del tipo especificado
    public <T> T convertFromJson(String json, Class<T> valueType) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, valueType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //obtengo todos los recursos de la nube para una suscripcion especifica
    public Map<String, Object> getAllResourcesBySubscription(String subscriptionId) {
        Map<String, Object> resources = new HashMap<>();

        resources.put("resourceGroups", getResourceGroupsBySubscription(subscriptionId));
        resources.put("virtualMachines", getVirtualMachinesBySubscription(subscriptionId));
        resources.put("virtualNetworks", getVirtualNetworksBySubscription(subscriptionId));
        resources.put("subnets", getSubnetsBySubscription(subscriptionId));
        resources.put("disks", getDisksBySubscription(subscriptionId));
        resources.put("snapshots", getSnapshotsBySubscription(subscriptionId));
        resources.put("loadBalancers", getLoadBalancersBySubscription(subscriptionId));
        resources.put("vpnGateways", getVpnGatewaysBySubscription(subscriptionId));
        resources.put("networkSecurityGroups", getNetworkSecurityGroupsBySubscription(subscriptionId));
        resources.put("aksClusters", getAksClustersBySubscription(subscriptionId));

        return resources;
    }

    //obtengo todos los recursos de la nube para un grupo de recursos especifico
    public Map<String, Object> getAllResourcesByResourceGroup(String resourceGroupId) {
        Map<String, Object> resources = new HashMap<>();

        resources.put("virtualMachines", getVirtualMachinesByResourceGroup(resourceGroupId));
        resources.put("virtualNetworks", getVirtualNetworksByResourceGroup(resourceGroupId));
        resources.put("disks", getDisksByResourceGroup(resourceGroupId));
        resources.put("snapshots", getSnapshotsByResourceGroup(resourceGroupId));
        resources.put("loadBalancers", getLoadBalancersByResourceGroup(resourceGroupId));
        resources.put("vpnGateways", getVpnGatewaysByResourceGroup(resourceGroupId));
        resources.put("networkSecurityGroups", getNetworkSecurityGroupsByResourceGroup(resourceGroupId));
        resources.put("aksClusters", getAksClustersByResourceGroup(resourceGroupId));

        return resources;
    }

}



