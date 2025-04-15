package com.gestion_backend.controllers_cloud.controller_google;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion_backend.dataModels.UserDataModel;
import com.gestion_backend.dataModels.modelsGoogle.*;
import com.gestion_backend.service_cloud.services_google.CloudResourceServiceGoogle;
import com.gestion_backend.service_cloud.services_google.ResourceManagerServiceGoogle;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.cloudresourcemanager.v3.CloudResourceManager;
import com.google.api.services.compute.Compute;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/api/cloud")
//permitir solicitudes CORS desde el puerto
@CrossOrigin(origins = "http://localhost:4173")
public class CloudResourceControllerGoogle {

    @Autowired
    private CloudResourceServiceGoogle cloudResourceServiceGoogle;

    @Autowired
    private ResourceManagerServiceGoogle resourceManagerServiceGoogle;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    //endpoint para verificar el estado de la conexión del servicio
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        return ResponseEntity.ok(Map.of("status", "connected"));
    }

    //endpoint para obtener todas las organizaciones
    @GetMapping("/organizationsgoogle")
    public ResponseEntity<List<Map<String, Object>>> getOrganizations() {
        List<OrganizationDataModel> organizations = cloudResourceServiceGoogle.getAllOrganizations();
        List<Map<String, Object>> response = new ArrayList<>();

        for (OrganizationDataModel organization : organizations) {
            Map<String, Object> organizationData = new HashMap<>();
            organizationData.put("organization_id", organization.getId());
            organizationData.put("name", organization.getName());
            response.add(organizationData);
        }

        return ResponseEntity.ok(response);
    }

    //endpoint para obtener todos los proyectos
    @GetMapping("/homegcp")
    public ResponseEntity<List<ProjectDataModel>> getProjectsHome() {
        List<ProjectDataModel> projects = cloudResourceServiceGoogle.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    //endpoint para obtener todos los proyectos
    @GetMapping("/projectsgoogle")
    public ResponseEntity<?> getProjects(
            @RequestParam(value = "projectId", required = false) String projectId) {

        if (projectId != null && !projectId.isEmpty()) {
            // Si se proporciona un projectId, devolver solo ese proyecto
            ProjectDataModel project = cloudResourceServiceGoogle.getProjectById(projectId);
            if (project == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Project not found");
            }

            Map<String, Object> projectData = new HashMap<>();
            projectData.put("id", project.getId());
            projectData.put("name", project.getName());
            projectData.put("display_name", project.getDisplayName());

            if (project.getOrganization() != null) {
                Map<String, Object> organizationData = new HashMap<>();
                organizationData.put("organization_id", project.getOrganization().getId());
                organizationData.put("name", project.getOrganization().getName());
                projectData.put("organization", organizationData);
            }

            if (project.getVirtualMachines() != null && !project.getVirtualMachines().isEmpty()) {
                List<Map<String, Object>> vmList = new ArrayList<>();
                for (VirtualMachineDataModel vm : project.getVirtualMachines()) {
                    Map<String, Object> vmData = new HashMap<>();
                    vmData.put("machine_id", vm.getId());
                    vmData.put("name", vm.getName());
                    vmData.put("machine_type", vm.getMachineType());
                    vmData.put("zone", vm.getZone());
                    vmData.put("status", vm.getStatus());
                    vmList.add(vmData);
                }
                projectData.put("virtual_machines", vmList);
            } else {
                projectData.put("virtual_machines", List.of());
            }

            return ResponseEntity.ok(List.of(projectData));
        } else {
            // Si no se proporciona projectId, devolver todos los proyectos (solo información básica)
            List<ProjectDataModel> projects = cloudResourceServiceGoogle.getAllProjects();
            List<Map<String, Object>> response = new ArrayList<>();

            for (ProjectDataModel project : projects) {
                Map<String, Object> projectData = new HashMap<>();
                projectData.put("id", project.getId());
                projectData.put("name", project.getName());
                projectData.put("display_name", project.getDisplayName());

                if (project.getOrganization() != null) {
                    projectData.put("organization_id", project.getOrganization().getId());
                }

                response.add(projectData);
            }

            return ResponseEntity.ok(response);
        }
    }

    //endpoint para obtener todas las VM
    @GetMapping("/virtualmachinesgoogle")
    public ResponseEntity<List<Map<String, Object>>> getVirtualMachines(
            @RequestParam(value = "projectId", required = false) String projectId) {
        List<VirtualMachineDataModel> vms = cloudResourceServiceGoogle.getAllVMs();
        List<Map<String, Object>> response = new ArrayList<>();

        for (VirtualMachineDataModel vm : vms) {
            // Si no se proporciona projectId, devolver todos los datos
            if (projectId == null || vm.getProject().getId().equals(projectId)) {
                Map<String, Object> vmData = new HashMap<>();
                vmData.put("machine_id", vm.getId());
                vmData.put("name", vm.getName());
                vmData.put("project_id", vm.getProject().getId());
                vmData.put("machine_type", vm.getMachineType());
                vmData.put("zone", vm.getZone());
                vmData.put("status", vm.getStatus());
                response.add(vmData);
            }
        }

        return ResponseEntity.ok(response);
    }


    //endpoint para obtener todas las redes
    @GetMapping("/networksgoogle")
    public ResponseEntity<List<Map<String, Object>>> getNetworks(
            @RequestParam(value = "projectId", required = false) String projectId) {
        List<NetworkDataModel> networks = cloudResourceServiceGoogle.getAllNetworks();
        List<Map<String, Object>> response = new ArrayList<>();

        for (NetworkDataModel network : networks) {
            // Si no se proporciona projectId, devolver todos los datos
            if (projectId == null || network.getProject().getId().equals(projectId)) {
                Map<String, Object> networkData = new HashMap<>();
                networkData.put("network_id", network.getId());
                networkData.put("name", network.getName());
                networkData.put("project_id", network.getProject().getId());
                networkData.put("auto_create_subnetworks", String.valueOf(network.getAutoCreateSubnetworks()));
                networkData.put("routing_mode", network.getRoutingMode());
                networkData.put("mtu", network.getMtu());
                networkData.put("description", network.getDescription());
                response.add(networkData);
            }
        }

        return ResponseEntity.ok(response);
    }

    //endpoint para obtener todas las subredes
    @GetMapping("/subnetworksgoogle")
    public ResponseEntity<List<Map<String, Object>>> getSubnetworks(
            @RequestParam(value = "projectId", required = false) String projectId) {
        List<SubnetworkDataModel> subnetworks = cloudResourceServiceGoogle.getAllSubnetworks();
        List<Map<String, Object>> response = new ArrayList<>();

        for (SubnetworkDataModel subnetwork : subnetworks) {
            // Si no se proporciona projectId, devolver todos los datos
            if (projectId == null || subnetwork.getProject().getId().equals(projectId)) {
                Map<String, Object> subnetworkData = new HashMap<>();
                subnetworkData.put("subnetwork_id", subnetwork.getId());
                subnetworkData.put("name", subnetwork.getName());
                subnetworkData.put("network_id", subnetwork.getNetwork().getId());
                subnetworkData.put("project_id", subnetwork.getProject().getId());
                subnetworkData.put("region", subnetwork.getRegion());
                subnetworkData.put("ip_cidr_range", subnetwork.getIpCidrRange());
                subnetworkData.put("private_ip_google_access", subnetwork.getPrivateIpGoogleAccess());
                subnetworkData.put("stack_type", subnetwork.getStackType());
                subnetworkData.put("description", subnetwork.getDescription());
                response.add(subnetworkData);
            }
        }

        return ResponseEntity.ok(response);
    }

    //endpoint para obtener todos los discos
    @GetMapping("/disksgoogle")
    public ResponseEntity<List<Map<String, Object>>> getDisks(
            @RequestParam(value = "projectId", required = false) String projectId) {
        List<DiskDataModel> disks = cloudResourceServiceGoogle.getAllDisks();
        List<Map<String, Object>> response = new ArrayList<>();

        for (DiskDataModel disk : disks) {
            // Si no se proporciona projectId, devolver todos los datos
            if (projectId == null || disk.getProject().getId().equals(projectId)) {
                Map<String, Object> diskData = new HashMap<>();
                diskData.put("disk_id", disk.getId());
                diskData.put("name", disk.getName());
                diskData.put("project_id", disk.getProject().getId());
                diskData.put("type", disk.getType());
                diskData.put("zone", disk.getZone());
                diskData.put("size_gb", disk.getSizeGb());
                diskData.put("status", disk.getStatus());
                diskData.put("details", disk.getDetails());
                response.add(diskData);
            }
        }

        return ResponseEntity.ok(response);
    }


    //endpoint para obtener todos los snapshots
    @GetMapping("/snapshotsgoogle")
    public ResponseEntity<List<Map<String, Object>>> getSnapshots(
            @RequestParam(value = "projectId", required = false) String projectId) {
        List<SnapshotDataModel> snapshots = cloudResourceServiceGoogle.getAllSnapshots();
        List<Map<String, Object>> response = new ArrayList<>();

        for (SnapshotDataModel snapshot : snapshots) {
            // Si no se proporciona projectId, devolver todos los datos
            if (projectId == null || snapshot.getProject().getId().equals(projectId)) {
                Map<String, Object> snapshotData = new HashMap<>();
                snapshotData.put("snapshot_id", snapshot.getId());
                snapshotData.put("name", snapshot.getName());
                snapshotData.put("project_id", snapshot.getProject().getId());
                snapshotData.put("source_disk", snapshot.getSourceDisk());
                snapshotData.put("disk_size_gb", snapshot.getDiskSizeGb());
                snapshotData.put("status", snapshot.getStatus());
                snapshotData.put("storage_locations", snapshot.getStorageLocations());
                response.add(snapshotData);
            }
        }

        return ResponseEntity.ok(response);
    }


    //endpoint para obtener todos los balanceadores
    @GetMapping("/loadbalancersgoogle")
    public ResponseEntity<List<Map<String, Object>>> getLoadBalancers(
            @RequestParam(value = "projectId", required = false) String projectId) {
        List<LoadBalancerDataModel> loadBalancers = cloudResourceServiceGoogle.getAllLoadBalancers();
        List<Map<String, Object>> response = new ArrayList<>();

        for (LoadBalancerDataModel loadBalancer : loadBalancers) {
            // Si no se proporciona projectId, devolver todos los datos
            if (projectId == null || loadBalancer.getProject().getId().equals(projectId)) {
                Map<String, Object> loadBalancerData = new HashMap<>();
                loadBalancerData.put("load_balancer_id", loadBalancer.getId());
                loadBalancerData.put("name", loadBalancer.getName());
                loadBalancerData.put("project_id", loadBalancer.getProject().getId());
                loadBalancerData.put("region", loadBalancer.getRegion());
                loadBalancerData.put("load_balancing_scheme", loadBalancer.getLoadBalancingScheme());
                loadBalancerData.put("network_tier", loadBalancer.getNetworkTier());
                loadBalancerData.put("type", loadBalancer.getType());
                loadBalancerData.put("ip_address", loadBalancer.getIpAddress());
                loadBalancerData.put("port", loadBalancer.getPort());
                loadBalancerData.put("status", loadBalancer.getStatus());
                loadBalancerData.put("target", loadBalancer.getTarget());
                response.add(loadBalancerData);
            }
        }

        return ResponseEntity.ok(response);
    }


    @GetMapping("/vpnsgoogle")
    public ResponseEntity<List<Map<String, Object>>> getVPNs(
            @RequestParam(value = "projectId", required = false) String projectId) {
        List<VPNDataModel> vpns = cloudResourceServiceGoogle.getAllVPNs();
        List<Map<String, Object>> response = new ArrayList<>();

        for (VPNDataModel vpn : vpns) {
            // Si no se proporciona projectId, devolver todos los datos
            if (projectId == null || vpn.getProject().getId().equals(projectId)) {
                Map<String, Object> vpnData = new HashMap<>();
                vpnData.put("vpn_id", vpn.getId());
                vpnData.put("name", vpn.getName());
                vpnData.put("project_id", vpn.getProject().getId());
                vpnData.put("region", vpn.getRegion());
                vpnData.put("status", vpn.getStatus());
                response.add(vpnData);
            }
        }

        return ResponseEntity.ok(response);
    }


    //endpoint para obtener todos los firewalls
    @GetMapping("/firewallsgoogle")
    public ResponseEntity<List<Map<String, Object>>> getFirewalls(
            @RequestParam(value = "projectId", required = false) String projectId) {
        List<FirewallDataModel> firewalls = cloudResourceServiceGoogle.getAllFirewalls();
        List<Map<String, Object>> response = new ArrayList<>();

        for (FirewallDataModel firewall : firewalls) {
            // Si no se proporciona projectId, devolver todos los datos
            if (projectId == null || firewall.getProject().getId().equals(projectId)) {
                Map<String, Object> firewallData = new HashMap<>();
                firewallData.put("firewall_id", firewall.getId());
                firewallData.put("name", firewall.getName());
                firewallData.put("project_id", firewall.getProject().getId());
                firewallData.put("direction", firewall.getDirection());
                firewallData.put("priority", firewall.getPriority());
                firewallData.put("protocol", firewall.getProtocol());
                firewallData.put("source_ranges", firewall.getSourceRanges());
                firewallData.put("target_tags", firewall.getTargetTags());
                response.add(firewallData);
            }
        }

        return ResponseEntity.ok(response);
    }



    //endpoint para obtener todos los clusters GKE
    @GetMapping("/gkeclustersgoogle")
    public ResponseEntity<List<Map<String, Object>>> getGKEClusters(
            @RequestParam(value = "projectId", required = false) String projectId) {
        List<GKEClusterDataModel> clusters = cloudResourceServiceGoogle.getAllGKEClusters();
        List<Map<String, Object>> response = new ArrayList<>();

        for (GKEClusterDataModel cluster : clusters) {
            // Si no se proporciona projectId, devolver todos los datos
            if (projectId == null || cluster.getProject().getId().equals(projectId)) {
                Map<String, Object> clusterData = new HashMap<>();
                clusterData.put("cluster_id", cluster.getId());
                clusterData.put("name", cluster.getName());
                clusterData.put("project_id", cluster.getProject().getId());
                clusterData.put("zone", cluster.getZone());
                clusterData.put("status", cluster.getStatus());
                clusterData.put("details", cluster.getDetails());
                response.add(clusterData);
            }
        }

        return ResponseEntity.ok(response);
    }

    //endpoint para obtener todas los usuarios
    @GetMapping("/usersgoogle")
    public ResponseEntity<List<Map<String, Object>>> getUsers() {
        List<UserDataModel> uds = cloudResourceServiceGoogle.getAllUsers();
        List<Map<String, Object>> response = new ArrayList<>();

        for (UserDataModel ud : uds) {
            Map<String, Object> udData = new HashMap<>();
            udData.put("id", ud.getId());
            udData.put("username", ud.getUsername());
            udData.put("password", ud.getPassword());
            udData.put("rol", ud.getRol());
            response.add(udData);
        }

        return ResponseEntity.ok(response);
    }

    //endpoint para guardar el nuevo usuario en la BD
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody UserDataModel user) {
        try {
            // Validar campos obligatorios
            if (user.getUsername() == null || user.getPassword() == null || user.getEmail() == null || user.getDepartment() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Faltan campos requeridos");
            }

            // Si es el primer usuario, forzamos a que sea ADMINISTRADOR
            if (cloudResourceServiceGoogle.isFirstUser()) {
                user.setPasswordChanged(false);
                user.setRol(UserDataModel.Rol.ADMINISTRADOR);
            } else {
                if (user.getRol() == null) {
                    user.setRol(UserDataModel.Rol.VIEWER);
                }
                user.setPasswordChanged(true);
            }

            // Encrypt password before saving
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            cloudResourceServiceGoogle.saveUser(user);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Usuario creado exitosamente",
                    "requirePasswordChange", !user.isPasswordChanged()
            ));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El nombre de usuario ya existe");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el usuario");
        }
    }

    @DeleteMapping("/users/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username,
                                             @RequestHeader("User-Role") String currentUserRole) {
        try {
            boolean deleted = cloudResourceServiceGoogle.deleteUser(username, currentUserRole);
            if (deleted) {
                return ResponseEntity.ok("Usuario eliminado exitosamente");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar usuario: " + e.getMessage());
        }
    }

    @PutMapping("/users/{username}")
    public ResponseEntity<?> updateUser(@PathVariable String username,
                                        @RequestBody UserDataModel updatedUser,
                                        @RequestHeader("User-Role") String currentUserRole) {
        try {
            // If password is provided, encode it before passing to service
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                updatedUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }

            UserDataModel updated = cloudResourceServiceGoogle.updateUser(username, updatedUser, currentUserRole);
            return ResponseEntity.ok(Map.of(
                    "message", "Usuario actualizado exitosamente",
                    "usuario", updated
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Usuario no encontrado");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar usuario: " + e.getMessage());
        }
    }

    //endpoint para que el usuario pueda cambiar la contraseña
    @PutMapping("/users/{username}/change-password")
    public ResponseEntity<String> changePassword(@PathVariable String username, @RequestBody Map<String, String> passwordMap) {
        String newPassword = passwordMap.get("newPassword");

        Optional<UserDataModel> userOptional = cloudResourceServiceGoogle.getUserByUsername(username);

        if(userOptional.isPresent()) {
            UserDataModel user = userOptional.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setPasswordChanged(true);
            cloudResourceServiceGoogle.saveUser(user);
            return ResponseEntity.ok("Contraseña cambiada correctamente");
        }

        return ResponseEntity.notFound().build();
    }

    //endpoint para obtener un usuario específico a través de su nombre
    @GetMapping("/users/{username}")
    public ResponseEntity<UserDataModel> getUser(@PathVariable String username) {
        return cloudResourceServiceGoogle.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //endpoint para obtener todos los usuarios
    @GetMapping("/users")
    public ResponseEntity<List<UserDataModel>> getAllUsers() {
        List<UserDataModel> users = cloudResourceServiceGoogle.getAllUsers();
        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(users);
    }

    //endpoint para el inicio de sesion del usuario
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticateUser(@RequestBody UserDataModel user) {
        Optional<UserDataModel> foundUser = cloudResourceServiceGoogle.getUserByUsername(user.getUsername());

        if (foundUser.isPresent() && passwordEncoder.matches(user.getPassword(), foundUser.get().getPassword())) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Inicio de sesión exitoso");
            response.put("requirePasswordChange", !foundUser.get().isPasswordChanged());
            response.put("role", foundUser.get().getRol().toString());
            response.put("username", foundUser.get().getUsername());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }
    }

    //enpoint para iniciar sesion y obtener un token y correo
    @GetMapping("/login")
    public ResponseEntity<?> loginGcloud() {
        try {
            // Ejecutar login de gcloud
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "gcloud", "auth", "application-default", "login", "--quiet");
            processBuilder.environment().put("CLOUDSDK_CORE_DISABLE_PROMPTS", "1");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Error al iniciar sesión con gcloud");
            }

            // Obtener el token de acceso
            ProcessBuilder tokenBuilder = new ProcessBuilder("gcloud", "auth", "print-access-token");
            Process tokenProcess = tokenBuilder.start();
            BufferedReader tokenReader = new BufferedReader(new InputStreamReader(tokenProcess.getInputStream()));
            String token = tokenReader.readLine();

            // Obtener el correo electrónico asociado
            ProcessBuilder emailBuilder = new ProcessBuilder("gcloud", "config", "get-value", "account");
            Process emailProcess = emailBuilder.start();
            BufferedReader emailReader = new BufferedReader(new InputStreamReader(emailProcess.getInputStream()));
            String email = emailReader.readLine();

            if (token != null && email != null) {
                // Sincronizar proyectos y recursos con el token
                resourceManagerServiceGoogle.syncUserProjectsAndResources(new GoogleCloudCredentials(token), email);

                return ResponseEntity.ok(Map.of(
                        "email", email,
                        "accessToken", token,
                        "message", "Login exitoso y sincronización completada"
                ));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("No se pudo obtener el token o el correo.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error durante el login: " + e.getMessage());
        }
    }


    @GetMapping("/renew-token")
    public ResponseEntity<?> renewToken() {
        try {
            // Obtain a new access token using the default credentials
            ProcessBuilder tokenBuilder = new ProcessBuilder("gcloud", "auth", "print-access-token");
            Process tokenProcess = tokenBuilder.start();
            BufferedReader tokenReader = new BufferedReader(new InputStreamReader(tokenProcess.getInputStream()));
            String newToken = tokenReader.readLine();

            // Obtain the email of the currently authenticated account
            ProcessBuilder emailBuilder = new ProcessBuilder("gcloud", "config", "get-value", "account");
            Process emailProcess = emailBuilder.start();
            BufferedReader emailReader = new BufferedReader(new InputStreamReader(emailProcess.getInputStream()));
            String email = emailReader.readLine();

            if (newToken != null && email != null) {
                // Validate the new token
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest userInfoRequest = HttpRequest.newBuilder()
                        .uri(URI.create("https://www.googleapis.com/oauth2/v3/userinfo"))
                        .header("Authorization", "Bearer " + newToken)
                        .GET()
                        .build();

                HttpResponse<String> userInfoResponse = client.send(userInfoRequest, HttpResponse.BodyHandlers.ofString());

                if (userInfoResponse.statusCode() != 200) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid renewed token");
                }

                return ResponseEntity.ok(Map.of(
                        "email", email,
                        "accessToken", newToken,
                        "message", "Token renovado exitosamente"
                ));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("No se pudo renovar el token.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al renovar el token: " + e.getMessage());
        }
    }

    @PostMapping("/validate-project")
    public ResponseEntity<?> validateProject(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String projectId = request.get("projectId");

        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "Token is empty or null",
                            "message", "Please provide a valid access token"
                    ));
        }

        if (projectId == null || projectId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "Project ID is empty or null",
                            "message", "Please provide a valid project ID"
                    ));
        }

        try {
            // Validar el token primero
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest userInfoRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.googleapis.com/oauth2/v3/userinfo"))
                    .header("Authorization", "Bearer " + token)
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> userInfoResponse = client.send(userInfoRequest, HttpResponse.BodyHandlers.ofString());

            if (userInfoResponse.statusCode() != 200) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", "Invalid Token",
                                "message", "The provided token is invalid or expired",
                                "statusCode", userInfoResponse.statusCode()
                        ));
            }

            // Validar acceso al proyecto específico
            if (!isProjectAccessible(token, projectId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "error", "Access Denied",
                                "message", "You don't have access to this project or it doesn't exist"
                        ));
            }

            // Obtener información básica del proyecto
            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            GoogleCredentials googleCredentials = GoogleCredentials.create(new AccessToken(token, null));

            CloudResourceManager resourceManager = new CloudResourceManager.Builder(
                    httpTransport, jsonFactory, new HttpCredentialsAdapter(googleCredentials))
                    .setApplicationName("GoogleCloudResourceExtractor")
                    .build();

            CloudResourceManager.Projects.Get getRequest = resourceManager.projects()
                    .get("projects/" + projectId);
            com.google.api.services.cloudresourcemanager.v3.model.Project project = getRequest.execute();

            return ResponseEntity.ok(Map.of(
                    "message", "Project is accessible",
                    "projectId", projectId,
                    "projectName", project.getDisplayName()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Unexpected Error",
                            "message", "An error occurred while validating the project",
                            "details", e.getMessage()
                    ));
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            // Use "gcloud auth revoke --all" to remove all credentials
            ProcessBuilder logoutBuilder = new ProcessBuilder("gcloud", "auth", "revoke", "--all");
            Process logoutProcess = logoutBuilder.start();
            int exitCode = logoutProcess.waitFor();

            if (exitCode == 0) {
                return ResponseEntity.ok("Sesión cerrada exitosamente");
            } else {
                // If specific crenndentials revoke fails, try a more general approach
                ProcessBuilder clearCredentialsBuilder = new ProcessBuilder("gcloud", "auth", "application-default", "revoke");
                Process clearCredentialsProcess = clearCredentialsBuilder.start();
                int clearExitCode = clearCredentialsProcess.waitFor();

                if (clearExitCode == 0) {
                    return ResponseEntity.ok("Sesión cerrada exitosamente");
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error al cerrar la sesión");
                }
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error durante el logout: " + e.getMessage());
        }
    }

    //endpoint para guardar los proyectos de gcloud en la base de datos
    @PostMapping("/save-project")
    public ResponseEntity<?> saveSelectedProject(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String projectId = request.get("projectId"); // Ahora usamos solo el ID

        try {
            // Validar acceso al proyecto
            GoogleCloudCredentials credentials = new GoogleCloudCredentials(token);
            if (resourceManagerServiceGoogle.isProjectAccessible(credentials, projectId)) {
                resourceManagerServiceGoogle.extraerRecursos(credentials, projectId);

                // Obtener el nombre del proyecto para mostrarlo en la UI
                ProjectDataModel project = cloudResourceServiceGoogle.getProjectById(projectId);
                String projectName = project != null ? project.getDisplayName() : projectId;

                return ResponseEntity.ok(Map.of(
                        "message", "Project synchronized successfully",
                        "projectId", projectId,
                        "projectName", projectName
                ));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You don't have access to this project or it doesn't exist.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing the project: " + e.getMessage());
        }
    }

    //endpoint para validar el token obtenido con gcloud
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");

        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "Token is empty or null",
                            "message", "Please provide a valid access token"
                    ));
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest userInfoRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.googleapis.com/oauth2/v3/userinfo"))
                    .header("Authorization", "Bearer " + token)
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> userInfoResponse = client.send(userInfoRequest, HttpResponse.BodyHandlers.ofString());

            if (userInfoResponse.statusCode() != 200) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", "Invalid Token",
                                "message", "The provided token is invalid or expired",
                                "statusCode", userInfoResponse.statusCode()
                        ));
            }

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> userInfo = objectMapper.readValue(userInfoResponse.body(), new TypeReference<Map<String, Object>>() {});
            String email = (String) userInfo.get("email");

            // Obtén todos los proyectos y filtra los accesibles
            List<Map<String, Object>> allProjects = cloudResourceServiceGoogle.getProjects(token);
            List<Map<String, Object>> accessibleProjects = new ArrayList<>();

            for (Map<String, Object> project : allProjects) {
                String projectId = (String) project.get("projectId");
                try {
                    if (isProjectAccessible(token, projectId)) {
                        accessibleProjects.add(project);
                    } else {
                    }
                } catch (Exception e) {
                }
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Token válido.",
                    "email", email,
                    "projects", accessibleProjects
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Unexpected Error",
                            "message", "An unexpected error occurred during token validation",
                            "details", e.getMessage()
                    ));
        }
    }

    public boolean isProjectAccessible(String token, String projectId) {
        try {
            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            GoogleCredentials googleCredentials = GoogleCredentials.create(new AccessToken(token, null));

            CloudResourceManager resourceManagerService = new CloudResourceManager.Builder(
                    httpTransport, jsonFactory, new HttpCredentialsAdapter(googleCredentials))
                    .setApplicationName("GoogleCloudResourceExtractor")
                    .build();

            resourceManagerService.projects().get("projects/" + projectId).execute();

            Compute computeService = new Compute.Builder(
                    httpTransport, jsonFactory, new HttpCredentialsAdapter(googleCredentials))
                    .setApplicationName("GoogleCloudResourceExtractor")
                    .build();

            Compute.Instances.List request = computeService.instances().list(projectId, "us-central1-a");
            request.setMaxResults(1L);
            request.execute();

            return true;
        } catch (IOException e) {
            System.err.println("Acceso denegado o error al realizar operaciones en el proyecto: " + projectId + ". Error: " + e.getMessage());
            return false;
        }
    }

    @GetMapping("/projectgoogle/{projectId}")
    public ResponseEntity<?> getProjectById(@PathVariable("projectId") String projectId) {
        try {
            // Busca el proyecto en la base de datos utilizando el `projectId`
            ProjectDataModel project = cloudResourceServiceGoogle.getProjectById(projectId);

            if (project == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El proyecto con ID " + projectId + " no existe.");
            }

            // Construye la respuesta con la información del proyecto
            Map<String, Object> projectData = new HashMap<>();
            projectData.put("id", project.getId());
            projectData.put("name", project.getName());
            projectData.put("display_name", project.getDisplayName());

            // Si la organización está presente, añade su información
            if (project.getOrganization() != null) {
                Map<String, Object> organizationData = new HashMap<>();
                organizationData.put("organization_id", project.getOrganization().getId());
                organizationData.put("name", project.getOrganization().getName());
                projectData.put("organization", organizationData);
            }

            // Añade la lista de máquinas virtuales asociadas (si las hay)
            if (project.getVirtualMachines() != null && !project.getVirtualMachines().isEmpty()) {
                List<Map<String, Object>> vmList = new ArrayList<>();
                for (VirtualMachineDataModel vm : project.getVirtualMachines()) {
                    Map<String, Object> vmData = new HashMap<>();
                    vmData.put("machine_id", vm.getId());
                    vmData.put("name", vm.getName());
                    vmData.put("machine_type", vm.getMachineType());
                    vmData.put("zone", vm.getZone());
                    vmData.put("status", vm.getStatus());
                    vmList.add(vmData);
                }
                projectData.put("virtual_machines", vmList);
            } else {
                projectData.put("virtual_machines", List.of());
            }

            return ResponseEntity.ok(projectData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener el proyecto: " + e.getMessage());
        }
    }


//    @PostMapping("/service-account")
//    public ResponseEntity<?> saveServiceAccount(@RequestBody ServiceAccountDataModel account) {
//        try {
//
//            //comrpuebo que los datos no esten vacios
//            if (account.getProjectId() == null || account.getEmail() == null || account.getClientId() == null) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Faltan campos requeridos");
//            }
//
//            //obtengo las propiedades crear la cuenta de servicio
//            ServiceAccountDataModel serviceAccountDataModel = new ServiceAccountDataModel();
//            serviceAccountDataModel.setProjectId(account.getProjectId());
//            serviceAccountDataModel.setEmail(account.getEmail());
//            serviceAccountDataModel.setClientId(account.getClientId());
//            serviceAccountDataModel.setPrivateKeyId(account.getPrivateKeyId());
//            serviceAccountDataModel.setPrivateKey(account.getPrivateKey());
//            serviceAccountDataModel.setCreatedAt(LocalDateTime.now());
//            serviceAccountDataModel.setUpdatedAt(LocalDateTime.now());
//
//            //guardo la cuenta de servicio en la BD
//            cloudResourceService.saveServiceAccount(serviceAccountDataModel);
//
//            return ResponseEntity.ok("Cuenta de servicio guardada correctamente");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error al guardar la cuenta de servicio");
//        }
//    }


}



