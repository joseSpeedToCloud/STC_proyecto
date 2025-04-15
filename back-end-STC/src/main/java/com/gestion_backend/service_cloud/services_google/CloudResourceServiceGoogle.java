package com.gestion_backend.service_cloud.services_google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion_backend.dataModels.UserDataModel;
import com.gestion_backend.dataModels.modelsGoogle.*;
import com.gestion_backend.repository_cloud.*;
import com.gestion_backend.repository_cloud.repository_google.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class CloudResourceServiceGoogle {

    //inyeccion de dependencias de los repositorios para manejar las entidades
    @Autowired
    private VirtualMachineRepository virtualMachineRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private NetworkRepository networkRepository;
    @Autowired
    private SubnetworkRepository subnetworkRepository;
    @Autowired
    private DiskRepository diskRepository;
    @Autowired
    private SnapshotRepository snapshotRepository;
    @Autowired
    private BalancerRepository loadBalancerRepository;
    @Autowired
    private VPNRepository vpnRepository;
    @Autowired
    private FirewallRepository firewallRepository;
    @Autowired
    private GKEClusterRepository gkeClusterRepository;
    @Autowired
    private UserRepository userRepository;

    //obtengo una organizacion por su nombre
    public OrganizationDataModel getOrganization(String name) {
        return organizationRepository.findByName(name);
    }

    //guardo una organizacion en la base de datos
    @Transactional
    public void saveOrganization(String name) {
        OrganizationDataModel org = getOrganization(name);
        if (org == null) {
            org = new OrganizationDataModel();
            org.setName(name);
        }
        organizationRepository.save(org);
    }

    //obtengo un proyecto por su ID
    public ProjectDataModel getProject(String id) {
        return projectRepository.findById(id).orElse(null);
    }

    //guardo un proyecto en la base de datos vinculandolo con una organizacion
    @Transactional
    public void saveProject(ProjectDataModel projectDataModel) {
        ProjectDataModel existingProject = getProject(projectDataModel.getId());
        if (existingProject != null) {
            existingProject.setName(projectDataModel.getName());
            existingProject.setDisplayName(projectDataModel.getDisplayName());
            existingProject.setOrganization(projectDataModel.getOrganization());
            projectRepository.save(existingProject);
        } else {
            projectRepository.save(projectDataModel);
        }
    }

    //obtengo una máquina virtual por su nombre y el proyecto al que pertenece
    public VirtualMachineDataModel getVM(String name, ProjectDataModel project) {
        try {
            return virtualMachineRepository.findByNameAndProject_Id(name, project.getId());
        } catch (Exception e) {
            System.err.println("La máquina virtual no existe");
            return null;
        }
    }

    //guardo una VM en la base de datos
    @Transactional
    public void saveVM(String id, String name, ProjectDataModel project, String machineType, String zone, String status, String detailsJson) {
        VirtualMachineDataModel existingVM = getVM(name, project);

        if (existingVM != null) {
            existingVM.setId(id);
            existingVM.setMachineType(machineType);
            existingVM.setZone(zone);
            existingVM.setStatus(status);
            existingVM.setDetails(detailsJson);
            virtualMachineRepository.save(existingVM);
        } else {
            VirtualMachineDataModel newVM = new VirtualMachineDataModel();
            newVM.setId(id);
            newVM.setName(name);
            newVM.setProject(project);
            newVM.setMachineType(machineType);
            newVM.setZone(zone);
            newVM.setStatus(status);
            newVM.setDetails(detailsJson);
            virtualMachineRepository.save(newVM);
        }
    }

    // Obtener red por nombre y proyecto
    public NetworkDataModel getNetwork(String name, ProjectDataModel project) {
        return networkRepository.findByNameAndProject_Id(name, project.getId());
    }

    public Optional<NetworkDataModel> getNetworkById(String networkId) {
        return networkRepository.findById(networkId);
    }

    @Transactional
    public void saveNetwork(String id, String name, ProjectDataModel project, Boolean autoCreateSubnetworks,
                            String routingMode, Integer mtu, String description, String detailsJson) {
        NetworkDataModel existingNetwork = getNetwork(name, project);
        if (existingNetwork != null) {
            // Actualiza la red existente
            existingNetwork.setId(id);
            existingNetwork.setAutoCreateSubnetworks(autoCreateSubnetworks);
            existingNetwork.setRoutingMode(routingMode);
            existingNetwork.setMtu(mtu);
            existingNetwork.setDescription(description);
            existingNetwork.setDetails(detailsJson);
        } else {
            // Crea una nueva red
            NetworkDataModel newNetwork = new NetworkDataModel();
            newNetwork.setId(id);
            newNetwork.setName(name);
            newNetwork.setProject(project);
            newNetwork.setAutoCreateSubnetworks(autoCreateSubnetworks);
            newNetwork.setRoutingMode(routingMode);
            newNetwork.setMtu(mtu);
            newNetwork.setDescription(description);
            newNetwork.setDetails(detailsJson);
            networkRepository.save(newNetwork);
        }
    }

    public SubnetworkDataModel getSubnetwork(String name, String networkId, ProjectDataModel project) {
        return subnetworkRepository.findByNameAndNetwork_IdAndProject_Id(name, networkId, project.getId());
    }

    @Transactional
    public void saveSubnetwork(String id, String name, NetworkDataModel network, ProjectDataModel project,
                               String region, String ipCidrRange, Boolean privateIpGoogleAccess, String stackType,
                               String description, String detailsJson) {
        SubnetworkDataModel existingSubnetwork = getSubnetwork(name, network.getId(), project);
        if (existingSubnetwork != null) {
            existingSubnetwork.setId(id);
            existingSubnetwork.setNetwork(network);
            existingSubnetwork.setRegion(region);
            existingSubnetwork.setIpCidrRange(ipCidrRange);
            existingSubnetwork.setPrivateIpGoogleAccess(privateIpGoogleAccess);
            existingSubnetwork.setStackType(stackType);
            existingSubnetwork.setDescription(description);
            existingSubnetwork.setDetails(detailsJson);
        } else {
            SubnetworkDataModel newSubnetwork = new SubnetworkDataModel();
            newSubnetwork.setId(id);
            newSubnetwork.setName(name);
            newSubnetwork.setNetwork(network);
            newSubnetwork.setProject(project);
            newSubnetwork.setRegion(region);
            newSubnetwork.setIpCidrRange(ipCidrRange);
            newSubnetwork.setPrivateIpGoogleAccess(privateIpGoogleAccess);
            newSubnetwork.setStackType(stackType);
            newSubnetwork.setDescription(description);
            newSubnetwork.setDetails(detailsJson);
            subnetworkRepository.save(newSubnetwork);
        }
    }

    // Get disk by name and project
    public DiskDataModel getDisk(String name, ProjectDataModel project) {
        try {
            return diskRepository.findByNameAndProject_Id(name, project.getId());
        } catch (Exception e) {
            System.err.println("The disk does not exist");
            return null;
        }
    }

    // Save disk
    @Transactional
    public void saveDisk(String id, String name, ProjectDataModel project, String type, String zone, Long sizeGb, String status, String detailsJson) {
        DiskDataModel existingDisk = getDisk(name, project);
        if (existingDisk != null) {
            existingDisk.setId(id);
            existingDisk.setType(type);
            existingDisk.setZone(zone);
            existingDisk.setSizeGb(sizeGb);
            existingDisk.setStatus(status);
            existingDisk.setDetails(detailsJson);
            diskRepository.save(existingDisk);
        } else {
            DiskDataModel newDisk = new DiskDataModel();
            newDisk.setId(id);
            newDisk.setName(name);
            newDisk.setProject(project);
            newDisk.setType(type);
            newDisk.setZone(zone);
            newDisk.setSizeGb(sizeGb);
            newDisk.setStatus(status);
            newDisk.setDetails(detailsJson);
            diskRepository.save(newDisk);
        }
    }

    // Get snapshot by name and project
    public SnapshotDataModel getSnapshot(String name, ProjectDataModel project) {
        try {
            return snapshotRepository.findByNameAndProject_Id(name, project.getId());
        } catch (Exception e) {
            System.err.println("The snapshot does not exist");
            return null;
        }
    }

    // Save snapshot
    @Transactional
    public void saveSnapshot(String id, String name, ProjectDataModel project, String sourceDisk, Long diskSizeGb, String status, String storageLocations, String detailsJson) {
        SnapshotDataModel existingSnapshot = getSnapshot(name, project);
        if (existingSnapshot != null) {
            existingSnapshot.setId(id);
            existingSnapshot.setSourceDisk(sourceDisk);
            existingSnapshot.setDiskSizeGb(diskSizeGb);
            existingSnapshot.setStatus(status);
            existingSnapshot.setStorageLocations(storageLocations);
            existingSnapshot.setDetails(detailsJson);
            snapshotRepository.save(existingSnapshot);
        } else {
            SnapshotDataModel newSnapshot = new SnapshotDataModel();
            newSnapshot.setId(id);
            newSnapshot.setName(name);
            newSnapshot.setProject(project);
            newSnapshot.setSourceDisk(sourceDisk);
            newSnapshot.setDiskSizeGb(diskSizeGb);
            newSnapshot.setStatus(status);
            newSnapshot.setStorageLocations(storageLocations);
            newSnapshot.setDetails(detailsJson);
            snapshotRepository.save(newSnapshot);
        }
    }

    // Get load balancer by name and project
    public LoadBalancerDataModel getLoadBalancer(String name, ProjectDataModel project) {
        try {
            return loadBalancerRepository.findByNameAndProject_Id(name, project.getId());
        } catch (Exception e) {
            System.err.println("The load balancer does not exist");
            return null;
        }
    }

    int parsePortRange(String portRange) {
        try {
            // Si el rango es algo como "443-443", tomamos solo el primer valor
            String[] parts = portRange.split("-");
            return Integer.parseInt(parts[0].trim());
        } catch (Exception e) {
            System.err.println("Error al parsear el rango de puertos: " + portRange);
            return -1; // Valor por defecto si falla
        }
    }

    @Transactional
    public void saveLoadBalancer(String id, String name, ProjectDataModel project, String region,
                                 String loadBalancingScheme, String networkTier, String ipAddress,
                                 String portRange, String status, String targetResource, String detailsJson) {
        int port = parsePortRange(portRange); // Validar y parsear el puerto

        LoadBalancerDataModel loadBalancer = getLoadBalancer(name, project);
        if (loadBalancer == null) {
            loadBalancer = new LoadBalancerDataModel();
            loadBalancer.setName(name);
            loadBalancer.setProject(project);
            loadBalancer.setRegion(region);
        }

        loadBalancer.setId(id);
        loadBalancer.setLoadBalancingScheme(loadBalancingScheme);
        loadBalancer.setNetworkTier(networkTier);
        loadBalancer.setIpAddress(ipAddress);
        loadBalancer.setPort(port > 0 ? port : null); // Guarda el puerto solo si es válido
        loadBalancer.setStatus(status);
        loadBalancer.setTarget(targetResource);
        loadBalancer.setDetails(detailsJson);

        loadBalancerRepository.save(loadBalancer);
    }

    // Get VPN by name and project
    public VPNDataModel getVpn(String name, ProjectDataModel project) {
        try {
            return vpnRepository.findByNameAndProject_Id(name, project.getId());
        } catch (Exception e) {
            System.err.println("The VPN does not exist");
            return null;
        }
    }

    // Save VPN
    @Transactional
    public void saveVpn(String id, String name, ProjectDataModel project, String region, String status, String detailsJson) {
        VPNDataModel existingVpn = getVpn(name, project);
        if (existingVpn != null) {
            existingVpn.setId(id);
            existingVpn.setRegion(region);
            existingVpn.setStatus(status);
            existingVpn.setDetails(detailsJson);
            vpnRepository.save(existingVpn);
        } else {
            VPNDataModel newVpn = new VPNDataModel();
            newVpn.setId(id);
            newVpn.setName(name);
            newVpn.setProject(project);
            newVpn.setRegion(region);
            newVpn.setStatus(status);
            newVpn.setDetails(detailsJson);
            vpnRepository.save(newVpn);
        }
    }

    // Obtener todas las redes
    public List<NetworkDataModel> getAllNetworks() {
        return networkRepository.findAll();
    }

    // Obtener todas las subredes
    public List<SubnetworkDataModel> getAllSubnetworks() {
        return subnetworkRepository.findAll();
    }

    // Obtener todos los firewalls
    public List<FirewallDataModel> getAllFirewalls() {
        return firewallRepository.findAll();
    }

    // Obtener todos los balanceadores de carga
    public List<LoadBalancerDataModel> getAllLoadBalancers() {
        return loadBalancerRepository.findAll();
    }

    // Obtener todos los discos
    public List<DiskDataModel> getAllDisks() {
        return diskRepository.findAll();
    }

    // Obtener todos los snapshots
    public List<SnapshotDataModel> getAllSnapshots() {
        return snapshotRepository.findAll();
    }

    // Obtener todas las VPNs
    public List<VPNDataModel> getAllVPNs() {
        return vpnRepository.findAll();
    }

    // Obtener todos los clusters GKE
    public List<GKEClusterDataModel> getAllGKEClusters() {
        return gkeClusterRepository.findAll();
    }

    // Get firewall by name and project
    public FirewallDataModel getFirewall(String name, ProjectDataModel project) {
        try {
            return firewallRepository.findByNameAndProject_Id(name, project.getId());
        } catch (Exception e) {
            System.err.println("The firewall does not exist");
            return null;
        }
    }

    int parsePriority(String priority) {
        try {
            return Integer.parseInt(priority);
        } catch (NumberFormatException e) {
            System.err.println("Error al parsear priority: " + priority);
            return -1; // Valor por defecto si falla
        }
    }

    @Transactional
    public void saveFirewall(String id, String name, ProjectDataModel project, String direction,
                             String priority, String protocol, String sourceRanges, String targetTags,
                             String detailsJson) {
        int parsedPriority = parsePriority(priority);

        FirewallDataModel existingFirewall = getFirewall(name, project);
        if (existingFirewall != null) {
            existingFirewall.setId(id);
            existingFirewall.setDirection(direction);
            existingFirewall.setPriority(parsedPriority > 0 ? String.valueOf(parsedPriority) : null);
            existingFirewall.setProtocol(protocol);
            existingFirewall.setSourceRanges(sourceRanges);
            existingFirewall.setTargetTags(targetTags);
            existingFirewall.setDetails(detailsJson);
            firewallRepository.save(existingFirewall);
        } else {
            FirewallDataModel newFirewall = new FirewallDataModel();
            newFirewall.setId(id);
            newFirewall.setName(name);
            newFirewall.setProject(project);
            newFirewall.setDirection(direction);
            newFirewall.setPriority(priority);
            newFirewall.setProtocol(protocol);
            newFirewall.setSourceRanges(sourceRanges);
            newFirewall.setTargetTags(targetTags);
            newFirewall.setDetails(detailsJson);
            firewallRepository.save(newFirewall);
        }
    }

    // Get GKE cluster by name and project
    public GKEClusterDataModel getGKECluster(String name, ProjectDataModel project) {
        try {
            return gkeClusterRepository.findByNameAndProject_Id(name, project.getId());
        } catch (Exception e) {
            System.err.println("The GKE cluster does not exist");
            return null;
        }
    }

    // Save GKE cluster
    @Transactional
    public void saveGKECluster(String id, String name, ProjectDataModel project, String zone, String status, String detailsJson) {
        GKEClusterDataModel existingCluster = getGKECluster(name, project);
        if (existingCluster != null) {
            existingCluster.setId(id);
            existingCluster.setZone(zone);
            existingCluster.setStatus(status);
            existingCluster.setDetails(detailsJson);
            gkeClusterRepository.save(existingCluster);
        } else {
            GKEClusterDataModel newCluster = new GKEClusterDataModel();
            newCluster.setId(id);
            newCluster.setName(name);
            newCluster.setProject(project);
            newCluster.setZone(zone);
            newCluster.setStatus(status);
            newCluster.setDetails(detailsJson);
            gkeClusterRepository.save(newCluster);
        }
    }

    @Transactional(readOnly = true)
    public boolean isFirstUser() {
        return userRepository.count() == 0;
    }

    //guardo un usuario en la base de datos
    @Transactional
    public void saveUser(UserDataModel user) {
        userRepository.save(user);
    }

    //obtengo un usuario por nombre de usuario
    @Transactional(readOnly = true)
    public Optional<UserDataModel> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public boolean deleteUser(String username, String currentUserRole) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede ser null o vacío");
        }

        try {
            // Verificar si el usuario existe
            Optional<UserDataModel> userOptional = userRepository.findByUsername(username);

            if (!userOptional.isPresent()) {
                return false;
            }

            UserDataModel userToDelete = userOptional.get();

            // No permitir eliminar el último administrador
            if (userToDelete.getRol() == UserDataModel.Rol.ADMINISTRADOR) {
                long adminCount = userRepository.countByRol(UserDataModel.Rol.ADMINISTRADOR);
                if (adminCount <= 1) {
                    throw new IllegalStateException("No se puede eliminar el último administrador");
                }
            }

            // Verificar permisos basados en roles
            if (currentUserRole.equals("VIEWER")) {
                // VIEWER solo puede eliminar otros VIEWER
                if (userToDelete.getRol() == UserDataModel.Rol.ADMINISTRADOR) {
                    throw new IllegalStateException("Los usuarios con rol VIEWER no pueden eliminar administradores");
                }
            }

            // Eliminar el usuario
            userRepository.delete(userToDelete);
            return true;

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar el usuario: " + e.getMessage(), e);
        }
    }

    @Transactional
    public UserDataModel updateUser(String username, UserDataModel updatedUser, String currentUserRole) {
        Optional<UserDataModel> existingUserOpt = getUserByUsername(username);
        if (!existingUserOpt.isPresent()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        UserDataModel existingUser = existingUserOpt.get();

        // Verificar permisos basados en roles
        if (currentUserRole.equals("VIEWER")) {
            // VIEWER solo puede modificar otros VIEWER
            if (existingUser.getRol() == UserDataModel.Rol.ADMINISTRADOR) {
                throw new IllegalStateException("Los usuarios con rol VIEWER no pueden modificar administradores");
            }

            // VIEWER no puede cambiar roles
            if (updatedUser.getRol() != null && updatedUser.getRol() != existingUser.getRol()) {
                throw new IllegalStateException("Los usuarios con rol VIEWER no pueden cambiar roles");
            }

            // VIEWER no puede cambiar contraseñas de otros usuarios
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                throw new IllegalStateException("Los usuarios con rol VIEWER no pueden cambiar contraseñas de otros usuarios");
            }
        }

        // Actualizar campos
        if (updatedUser.getEmail() != null) existingUser.setEmail(updatedUser.getEmail());
        if (updatedUser.getDepartment() != null) existingUser.setDepartment(updatedUser.getDepartment());
        if (updatedUser.getLastname() != null) existingUser.setLastname(updatedUser.getLastname());

        // Solo los administradores pueden cambiar roles
        if (updatedUser.getRol() != null && currentUserRole.equals("ADMINISTRADOR")) {
            existingUser.setRol(updatedUser.getRol());
        }

        // Solo los administradores pueden cambiar contraseñas de otros usuarios
        // La contraseña ya viene codificada desde el controlador
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()
                && currentUserRole.equals("ADMINISTRADOR")) {
            existingUser.setPassword(updatedUser.getPassword());
        }

        return saveAndReturnUser(existingUser);
    }

    @Transactional
    public UserDataModel saveAndReturnUser(UserDataModel user) {
        return userRepository.save(user);
    }

    //obtengo todas las organizaciones
    public List<OrganizationDataModel> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    //obtengo todos los proyectos
    public List<ProjectDataModel> getAllProjects() {
        return projectRepository.findAll();
    }

    //obtengo todas las VM
    public List<VirtualMachineDataModel> getAllVMs() {
        return virtualMachineRepository.findAll();
    }

    //obtengo todas las VM asociadas a un proyecto especifico
    public List<VirtualMachineDataModel> getVMsByProject(String projectId) {
        return virtualMachineRepository.findByProject_Id(projectId);
    }

    //obtengo todos los usuarios
    @Transactional(readOnly = true)
    public List<UserDataModel> getAllUsers() {
        return userRepository.findAll();
    }

    //obtengo los proyectos asociados al token
    public List<Map<String, Object>> getProjects(String accessToken) {
        try {
            // Llamada inicial para obtener todos los proyectos
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://cloudresourcemanager.googleapis.com/v1/projects"))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> responseData = mapper.readValue(response.body(), Map.class);

                // Extraer proyectos de la respuesta
                List<Map<String, Object>> allProjects = (List<Map<String, Object>>) responseData.get("projects");
                if (allProjects == null) {
                    return Collections.emptyList();
                }

                // Lista para proyectos accesibles
                List<Map<String, Object>> accessibleProjects = new ArrayList<>();

                // Filtrar proyectos utilizando isProjectAccessible
                for (Map<String, Object> project : allProjects) {
                    String projectId = (String) project.get("projectId");
                    if (isProjectAccessible(accessToken, projectId)) {
                        accessibleProjects.add(project);
                    } else {
                        System.err.println("Proyecto no accesible: " + projectId);
                    }
                }

                return accessibleProjects;

            } else {
                throw new RuntimeException("Error al obtener proyectos: " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al comunicarse con la API de Google Cloud: " + e.getMessage());
        }
    }

    private boolean isProjectAccessible(String accessToken, String projectId) {
        try {
            // Llamada para obtener información de un proyecto específico
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://cloudresourcemanager.googleapis.com/v1/projects/" + projectId))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("Error al verificar acceso al proyecto: " + projectId);
            return false;
        }
    }


    //guardo los proyectos en la BD
    public void saveOrUpdateProject(Map<String, Object> project, String email) {
        String projectId = (String) project.get("projectId");
        String name = (String) project.get("name");
        String displayName = (String) project.get("displayName");
        String organizationId = (String) project.get("organizationId"); // Supongamos que también se recibe

        //busco proyecto existente en la BD
        ProjectDataModel existingProject = projectRepository.findById(projectId).orElse(null);

        if (existingProject == null) {
            ProjectDataModel newProject = new ProjectDataModel();
            newProject.setId(projectId);
            newProject.setName(name);
            newProject.setDisplayName(displayName);

            //relaciono con una organizacion si está disponible
            if (organizationId != null) {
                OrganizationDataModel organization = organizationRepository.findById(Long.valueOf(organizationId)).orElse(null);
                newProject.setOrganization(organization);
            }

            projectRepository.save(newProject);
        } else {
            //actualizo el proyecto existente
            existingProject.setName(name);
            existingProject.setDisplayName(displayName);

            //actualizo organización si existe
            if (organizationId != null) {
                OrganizationDataModel organization = organizationRepository.findById(Long.valueOf(organizationId)).orElse(null);
                existingProject.setOrganization(organization);
            }

            projectRepository.save(existingProject);
        }
    }

    public ProjectDataModel getProjectById(String projectId) {
        return projectRepository.findById(projectId).orElse(null);
    }

//    public void deleteAllDataTables() {
//        networkRepository.deleteAll();
//        subnetworkRepository.deleteAll();
//        diskRepository.deleteAll();
//        snapshotRepository.deleteAll();
//        loadBalancerRepository.deleteAll();
//        vpnRepository.deleteAll();
//        firewallRepository.deleteAll();
//        gkeClusterRepository.deleteAll();
//        virtualMachineRepository.deleteAll();
//        projectRepository.deleteAll();
//        organizationRepository.deleteAll();
//    }
}

