package com.gestion_backend.service_cloud.services_google;

import com.gestion_backend.controller_bd.IniciarBD;
import com.gestion_backend.dataModels.modelsGoogle.*;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.services.cloudresourcemanager.v3.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.v3.model.Project;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.model.*;
import com.google.auth.Credentials;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.container.v1.ClusterManagerClient;
import com.google.cloud.container.v1.ClusterManagerSettings;
import com.google.container.v1.Cluster;
import com.google.container.v1.ListClustersResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ResourceManagerServiceGoogle {

    @Autowired
    private IniciarBD iniciarBD;

    @Autowired
    private CloudResourceServiceGoogle cloudResourceServiceGoogle;

    @PostConstruct
    public void init() {
        try {
            //creo la base de datos y las tablas si no existen
            iniciarBD.creacionBD();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //metodo para sincronizar el usuario con los proyectos
    public void syncUserProjectsAndResources(GoogleCloudCredentials token, String email) {
        try {
            //obtengo proyectos asociados al token
            List<Map<String, Object>> projects = cloudResourceServiceGoogle.getProjects(token.getAccessToken());

            //mapeo los proyectos por su ID
            for (Map<String, Object> project : projects) {
                String projectId = (String) project.get("projectId");
                try {
                    //valido el acceso al proyecto antes de extraer recursos
                    if (isProjectAccessible(token, projectId)) {

                        //guardo los proyectos en la base de datos
                        cloudResourceServiceGoogle.saveOrUpdateProject(project, email);
                        extraerRecursos(token, projectId);
                    } else {
                        System.err.println("El proyecto no es accesible: " + projectId);
                    }
                } catch (Exception e) {
                    System.err.println("Error procesando proyecto " + projectId + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error sincronizando recursos: " + e.getMessage(), e);
        }
    }

    //metodo para comprobar si el proyecto es accesible o no
    public boolean isProjectAccessible(GoogleCloudCredentials token, String projectId) {
        try {
            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            CloudResourceManager resourceManagerService = new CloudResourceManager.Builder(
                    httpTransport, jsonFactory, new HttpCredentialsAdapter(token))
                    .setApplicationName("GoogleCloudResourceExtractor")
                    .build();

            //intento obtener detalles del proyecto para validar el acceso
            resourceManagerService.projects().get("projects/" + projectId).execute();
            return true;
        } catch (IOException e) {
            System.err.println("Acceso denegado o proyecto no encontrado: " + projectId);
            return false;
        }
    }

    //metodo para sacar la informacion de una organizacion para un proyecto
    private void extraerOrganizaciones(CloudResourceManager resourceManagerService, String proyectoId) throws IOException {

        //hago la solicitud a la API de gcloud resource manager
        Project proyecto = resourceManagerService.projects().get("projects/" + proyectoId).execute();

        //obtengo el ID de la organizacion
        String organizacionId = proyecto.getParent();

        //guardo la organizacion en la BD
        cloudResourceServiceGoogle.saveOrganization(organizacionId);

        System.out.println("Organizacion guardada en la BD: " + organizacionId);
    }

    //metodo para sacar la informacion de un proyecto
    private ProjectDataModel extraerProyectos(CloudResourceManager resourceManagerService, String proyectoId) throws IOException {

        //llamo a la API de gcloud resource manager
        Project proyecto = resourceManagerService.projects().get("projects/" + proyectoId).execute();

        //obtengo el ID de la organización del proyecto
        String organizacionId = proyecto.getParent();

        //obtengo la organización desde el servicio de recursos
        OrganizationDataModel organizacion = cloudResourceServiceGoogle.getOrganization(organizacionId);

        //creo el objeto de proyecto con toda la información
        ProjectDataModel projectEntity = new ProjectDataModel();
        projectEntity.setId(proyecto.getProjectId());
        projectEntity.setName(proyecto.getName());
        projectEntity.setDisplayName(proyecto.getDisplayName());
        projectEntity.setOrganization(organizacion);

        //guardo el proyecto en la BD
        cloudResourceServiceGoogle.saveProject(projectEntity);

        System.out.println("Proyecto guardado en la BD: " + proyecto.getProjectId());

        return projectEntity;
    }

    //metodo para sacar la infomcacion de las maquinas virtuales
    private void extraerVMs(Compute computeService, String proyectoId, ProjectDataModel projectEntity) throws IOException {

        //obtengo una lista de las instancias añadidas por zona
        Compute.Instances.AggregatedList solicitud = computeService.instances().aggregatedList(proyectoId);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        //recorro las zonas y sus instancias
        for (Map.Entry<String, InstancesScopedList> entrada : solicitud.execute().getItems().entrySet()) {
            String zona = entrada.getKey();
            InstancesScopedList instancias = entrada.getValue();

            //guardo la informacion de cada instancia en la BD
            if (instancias.getInstances() != null) {
                for (Instance instancia : instancias.getInstances()) {
                    String machineType = instancia.getMachineType().substring(instancia.getMachineType().lastIndexOf("/") + 1);
                    String zone = zona.substring(zona.lastIndexOf("/") + 1);

                    cloudResourceServiceGoogle.saveVM(
                            String.valueOf(instancia.getId()),
                            instancia.getName(),
                            projectEntity,
                            machineType,
                            zone,
                            instancia.getStatus(),
                            gson.toJson(instancia)
                    );
                    System.out.println("VM guardada en la base de datos: " + instancia.getName());
                }
            }
        }
    }

    private void extraerNetworks(Compute computeService, String proyectoId, ProjectDataModel projectEntity) throws IOException {
        Compute.Networks.List solicitudNetworks = computeService.networks().list(proyectoId);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        List<Network> networks = solicitudNetworks.execute().getItems();
        if (networks != null) {
            for (Network network : networks) {
                cloudResourceServiceGoogle.saveNetwork(
                        String.valueOf(network.getId()),
                        network.getName(),
                        projectEntity,
                        network.getAutoCreateSubnetworks(),
                        network.getRoutingConfig() != null ? network.getRoutingConfig().getRoutingMode() : null,
                        null,
                        network.getDescription(),
                        gson.toJson(network)
                );
                System.out.println("Network guardada en la base de datos: " + network.getName());
            }
        } else {
            System.err.println("No se encontraron redes para el proyecto ID: " + proyectoId);
        }
    }

    private void extraerSubnetworks(Compute computeService, String proyectoId, ProjectDataModel projectEntity) throws IOException {
        Compute.Subnetworks.AggregatedList solicitudSubnetworks = computeService.subnetworks().aggregatedList(proyectoId);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Map<String, SubnetworksScopedList> subnetworksMap = solicitudSubnetworks.execute().getItems();
        if (subnetworksMap != null) {
            for (Map.Entry<String, SubnetworksScopedList> entrada : subnetworksMap.entrySet()) {
                String region = entrada.getKey();
                SubnetworksScopedList listaSubnetworks = entrada.getValue();

                if (listaSubnetworks.getSubnetworks() != null) {
                    for (Subnetwork subnetwork : listaSubnetworks.getSubnetworks()) {
                        String regionNombre = region.substring(region.lastIndexOf("/") + 1);
                        String networkId = subnetwork.getNetwork().substring(subnetwork.getNetwork().lastIndexOf("/") + 1);

                        NetworkDataModel networkEntity = cloudResourceServiceGoogle.getNetworkById(networkId).orElse(null);

                        if (networkEntity != null) {
                            cloudResourceServiceGoogle.saveSubnetwork(
                                    String.valueOf(subnetwork.getId()),
                                    subnetwork.getName(),
                                    networkEntity,
                                    projectEntity,
                                    regionNombre,
                                    subnetwork.getIpCidrRange(),
                                    subnetwork.getPrivateIpGoogleAccess(),
                                    null,
                                    subnetwork.getDescription(),
                                    gson.toJson(subnetwork)
                            );
                            System.out.println("Subnetwork guardada en la base de datos: " + subnetwork.getName());
                        } else {
                            System.err.println("Network no encontrada para el ID de la subred: " + networkId);
                        }
                    }
                } else {
                    System.out.println("No se encontraron subredes en la región: " + region);
                }
            }
        } else {
            System.err.println("No se encontraron subnetworks para el proyecto ID: " + proyectoId);
        }
    }

    //metodo para sacar la informacion de los discos
    private void extraerDiscos(Compute computeService, String proyectoId, ProjectDataModel projectEntity) throws IOException {

        Compute.Disks.AggregatedList solicitudDiscos = computeService.disks().aggregatedList(proyectoId);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        for (Map.Entry<String, DisksScopedList> entrada : solicitudDiscos.execute().getItems().entrySet()) {

            String zona = entrada.getKey();
            DisksScopedList listaDiscos = entrada.getValue();

            if (listaDiscos.getDisks() != null) {
                for (Disk disco : listaDiscos.getDisks()) {

                    String zonaNombre = zona.substring(zona.lastIndexOf("/") + 1);

                    cloudResourceServiceGoogle.saveDisk(
                            String.valueOf(disco.getId()),
                            disco.getName(),
                            projectEntity,
                            disco.getType(),
                            zonaNombre,
                            disco.getSizeGb(),
                            disco.getStatus(),
                            gson.toJson(disco)
                    );
                    System.out.println("Disco guardado en la base de datos: " + disco.getName());
                }
            }
        }
    }

    //metodo para extraer las snapshot
    private void extraerSnapshots(Compute computeService, String proyectoId, ProjectDataModel projectEntity) throws IOException {

        Compute.Snapshots.List solicitudSnapshots = computeService.snapshots().list(proyectoId);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String pageToken = null;
        do {
            solicitudSnapshots.setPageToken(pageToken);
            SnapshotList snapshotList = solicitudSnapshots.execute();

            if (snapshotList.getItems() != null) {
                for (Snapshot snapshot : snapshotList.getItems()) {

                    String sourceDisk = snapshot.getSourceDisk() != null ? snapshot.getSourceDisk().substring(snapshot.getSourceDisk().lastIndexOf("/") + 1) : null;

                    String storageLocations = snapshot.getStorageLocations() != null && !snapshot.getStorageLocations().isEmpty() ?
                            String.join(",", snapshot.getStorageLocations()) : null;

                    cloudResourceServiceGoogle.saveSnapshot(
                            String.valueOf(snapshot.getId()),
                            snapshot.getName(),
                            projectEntity,
                            sourceDisk,
                            snapshot.getDiskSizeGb(),
                            snapshot.getStatus(),
                            storageLocations,
                            gson.toJson(snapshot)
                    );
                    System.out.println("Snapshot guardado en la base de datos: " + snapshot.getName());
                }
            }
            pageToken = snapshotList.getNextPageToken();
        } while (pageToken != null);
    }

    // Función para truncar cadenas a un límite específico
    private String truncateString(String value, int maxLength) {
        return (value != null && value.length() > maxLength) ? value.substring(0, maxLength) : value;
    }

    private void extraerBalanceadores(Compute computeService, String proyectoId, ProjectDataModel projectEntity) throws IOException {

        // Inicializo la solicitud para obtener los balanceadores en todas las regiones
        Compute.ForwardingRules.AggregatedList solicitudBalanceadores = computeService.forwardingRules().aggregatedList(proyectoId);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String pageToken = null;
        do {
            // Establezco el token de página si hay más resultados
            solicitudBalanceadores.setPageToken(pageToken);
            ForwardingRuleAggregatedList listaBalanceadores = solicitudBalanceadores.execute();

            // Recorro la lista de balanceadores en todas las regiones
            if (listaBalanceadores.getItems() != null) {
                for (Map.Entry<String, ForwardingRulesScopedList> entrada : listaBalanceadores.getItems().entrySet()) {

                    ForwardingRulesScopedList scopedList = entrada.getValue();
                    if (scopedList.getForwardingRules() != null) {
                        for (ForwardingRule balanceador : scopedList.getForwardingRules()) {

                            // Asigno el estado predeterminado ya que getStatus() no existe
                            String status = "UNKNOWN";

                            // Obtengo el nombre de la región desde el valor de la key
                            String regionName = entrada.getKey().substring(entrada.getKey().lastIndexOf("/") + 1);

                            // Truncar valores antes de guardarlos
                            String truncatedName = truncateString(balanceador.getName(), 255);
                            String truncatedLoadBalancingScheme = truncateString(balanceador.getLoadBalancingScheme(), 255);
                            String truncatedNetworkTier = truncateString(balanceador.getNetworkTier(), 255);
                            String truncatedIPAddress = truncateString(balanceador.getIPAddress(), 255);
                            String truncatedPortRange = truncateString(balanceador.getPortRange(), 255);
                            String truncatedTarget = truncateString(
                                    balanceador.getTarget() != null
                                            ? balanceador.getTarget().substring(balanceador.getTarget().lastIndexOf("/") + 1)
                                            : null,
                                    255
                            );

                            // Guardar la información del balanceador en la base de datos
                            cloudResourceServiceGoogle.saveLoadBalancer(
                                    String.valueOf(balanceador.getId()),
                                    truncatedName,
                                    projectEntity,
                                    regionName,
                                    truncatedLoadBalancingScheme,
                                    truncatedNetworkTier,
                                    truncatedIPAddress,
                                    truncatedPortRange,
                                    status,
                                    truncatedTarget,
                                    gson.toJson(balanceador)
                            );

                            System.out.println("Balanceadores guardados en la base de datos: " + truncatedName);
                        }
                    }
                }
            }
            pageToken = listaBalanceadores.getNextPageToken();
        } while (pageToken != null);
    }




    private void extraerVPNs(Compute computeService, String proyectoId, ProjectDataModel projectEntity) throws IOException {

        //inicializo la solicitud para obtener los VPNs en todas las regiones
        Compute.VpnTunnels.AggregatedList solicitudVPNs = computeService.vpnTunnels().aggregatedList(proyectoId);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String pageToken = null;
        do {
            //establezco el token de pagina si hay mas resultados
            solicitudVPNs.setPageToken(pageToken);
            VpnTunnelAggregatedList listaVPNs = solicitudVPNs.execute();

            //recorro la lista de VPNs en todas las regiones
            if (listaVPNs.getItems() != null) {
                for (Map.Entry<String, VpnTunnelsScopedList> entrada : listaVPNs.getItems().entrySet()) {

                    VpnTunnelsScopedList scopedList = entrada.getValue();

                    if (scopedList.getVpnTunnels() != null) {
                        for (VpnTunnel vpn : scopedList.getVpnTunnels()) {

                            //obtengo el nombre de la region desde el valor de la key
                            String regionName = entrada.getKey().substring(entrada.getKey().lastIndexOf("/") + 1);

                            //guardo la informacion del VPN en la BD
                            cloudResourceServiceGoogle.saveVpn(
                                    String.valueOf(vpn.getId()),
                                    vpn.getName(),
                                    projectEntity,
                                    regionName,
                                    vpn.getStatus(),
                                    gson.toJson(vpn)
                            );

                            System.out.println("VPN guardado en la base de datos: " + vpn.getName());
                        }
                    }
                }
            }
            pageToken = listaVPNs.getNextPageToken();
        } while (pageToken != null);
    }

    public void extraerFirewalls(Compute computeService, String proyectoId, ProjectDataModel projectEntity) throws IOException {

        // Creo la solicitud para listar firewalls en el proyecto
        Compute.Firewalls.List solicitud = computeService.firewalls().list(proyectoId);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Ejecuto la solicitud y obtengo la lista de firewalls
        FirewallList firewallList = solicitud.execute();
        List<Firewall> firewalls = firewallList.getItems();

        // Guardo cada firewall en la BD
        if (firewalls != null) {
            for (Firewall firewall : firewalls) {

                // Truncar valores individuales si superan el límite permitido (255 caracteres)
                String truncatedName = truncateString(firewall.getName(), 255);
                String truncatedDirection = truncateString(firewall.getDirection(), 255);
                String truncatedPriority = firewall.getPriority() != null
                        ? truncateString(firewall.getPriority().toString(), 255)
                        : null;
                String truncatedProtocol = firewall.getAllowed() != null && !firewall.getAllowed().isEmpty()
                        ? truncateString(firewall.getAllowed().get(0).getIPProtocol(), 255)
                        : null;
                String truncatedSourceRanges = firewall.getSourceRanges() != null
                        ? truncateString(String.join(",", firewall.getSourceRanges()), 255)
                        : null;
                String truncatedTargetTags = firewall.getTargetTags() != null
                        ? truncateString(String.join(",", firewall.getTargetTags()), 255)
                        : null;

                // Guardar en la base de datos
                cloudResourceServiceGoogle.saveFirewall(
                        String.valueOf(firewall.getId()),
                        truncatedName,
                        projectEntity,
                        truncatedDirection,
                        truncatedPriority,
                        truncatedProtocol,
                        truncatedSourceRanges,
                        truncatedTargetTags,
                        gson.toJson(firewall) // Serializa el objeto completo
                );

                System.out.println("Firewall guardado en la base de datos: " + truncatedName);
            }
        }
    }


    private List<GKEClusterDataModel> extraerGKEClusters(String projectId, ProjectDataModel projectEntity) {
        List<GKEClusterDataModel> clustersList = new ArrayList<>();
        try {
            //configuracion del cliente con credenciales
            ClusterManagerSettings settings = ClusterManagerSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(GoogleCredentials.getApplicationDefault()))
                    .build();

            //creo el cliente con la configuracion
            try (ClusterManagerClient clusterManagerClient = ClusterManagerClient.create(settings)) {

                //obtengo la respuesta de la lista de clusters
                ListClustersResponse response = clusterManagerClient.listClusters(projectId);

                //proceso los clusters
                for (Cluster cluster : response.getClustersList()) {
                    GKEClusterDataModel clusterDataModel = new GKEClusterDataModel();
                    clusterDataModel.setId(cluster.getName());
                    clusterDataModel.setName(cluster.getName());
                    clusterDataModel.setZone(cluster.getZone());
                    clusterDataModel.setStatus(String.valueOf(cluster.getStatus()));

                    //guardo el cluster en la BD
                    cloudResourceServiceGoogle.saveGKECluster(
                            clusterDataModel.getId(),
                            clusterDataModel.getName(),
                            projectEntity,
                            clusterDataModel.getZone(),
                            clusterDataModel.getStatus(),
                            new Gson().toJson(clusterDataModel)
                    );

                    clustersList.add(clusterDataModel);
                    System.out.println("Cluster guardado: " + cluster.getName());
                }
            }
        } catch (IOException e) {
            System.err.println("Error al extraer clusters: " + e.getMessage());
        }
        return clustersList;
    }


    public void extraerRecursos(Credentials token, String projectId) {
        try {
            validateProjectId(projectId);

            //configuro servicios de gcloud con el token
            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            CloudResourceManager resourceManagerService = new CloudResourceManager.Builder(
                    httpTransport, jsonFactory, new HttpCredentialsAdapter(token))
                    .setApplicationName("GoogleCloudResourceExtractor")
                    .build();

            Compute computeService = new Compute.Builder(
                    httpTransport, jsonFactory, new HttpCredentialsAdapter(token))
                    .setApplicationName("GoogleCloudResourceExtractor")
                    .build();

            // Extraer y guardar recursos
            extraerOrganizaciones(resourceManagerService, projectId);
            ProjectDataModel projectEntity = extraerProyectos(resourceManagerService, projectId);
            extraerVMs(computeService, projectId, projectEntity);
            extraerNetworks(computeService, projectId, projectEntity);
            //extraerSubnetworks(computeService, projectId, projectEntity);
            extraerDiscos(computeService, projectId, projectEntity);
            extraerSnapshots(computeService, projectId, projectEntity);
            extraerBalanceadores(computeService, projectId, projectEntity);
            extraerVPNs(computeService, projectId, projectEntity);
            extraerFirewalls(computeService, projectId, projectEntity);
            //extraerGKEClusters(projectId, projectEntity);

            System.out.println("Recursos extraídos de la cuenta de Google Cloud: " + projectId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error extrayendo recursos del proyecto " + projectId + ": " + e.getMessage());
        }
    }

    private void validateProjectId(String projectId) {
        if (projectId == null || projectId.isEmpty()) {
            throw new IllegalArgumentException("El ID del proyecto no puede estar vacío.");
        }
    }
}


