package com.gestion_backend.controllers_cloud.controller_aws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion_backend.dataModels.modelsAWS.*;
import com.gestion_backend.service_cloud.services_aws.CloudResourceServiceAWS;
import com.gestion_backend.service_cloud.services_aws.ResourceManagerServiceAWS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/aws")
@CrossOrigin(origins = "http://localhost:4173")
public class CloudResourceControllerAWS {

    @Autowired
    private CloudResourceServiceAWS cloudResourceServiceAWS;

    @Autowired
    private ResourceManagerServiceAWS resourceManagerServiceAWS;

    @GetMapping("/statusAWS")
    public ResponseEntity<Map<String, String>> getStatus() {
        return ResponseEntity.ok(Map.of("status", "connected"));
    }

    @GetMapping("/homeaws")
    public ResponseEntity<List<AccountDataModel>> getProjectsHome() {
        List<AccountDataModel> accounts = cloudResourceServiceAWS.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<Map<String, Object>>> getAccounts() {
        List<AccountDataModel> accounts = cloudResourceServiceAWS.getAllAccounts();
        List<Map<String, Object>> response = new ArrayList<>();

        for (AccountDataModel account : accounts) {
            Map<String, Object> accountData = new HashMap<>();
            accountData.put("id", account.getId());
            accountData.put("name", account.getName());
            accountData.put("email", account.getEmail());
            accountData.put("status", account.getStatus());

            List<Map<String, Object>> ec2List = new ArrayList<>();
            if (account.getEc2Instances() != null) {
                for (EC2InstanceDataModel instance : account.getEc2Instances()) {
                    Map<String, Object> instanceData = new HashMap<>();
                    instanceData.put("instance_id", instance.getId());
                    instanceData.put("name", instance.getName());
                    instanceData.put("instance_type", instance.getInstanceType());
                    instanceData.put("zone", instance.getAvailabilityZone());
                    instanceData.put("status", instance.getState());
                    ec2List.add(instanceData);
                }
            }
            accountData.put("ec2_instances", ec2List);

            response.add(accountData);
        }

        return ResponseEntity.ok(response);
    }

    // Endpoint para login con AWS CLI
    @GetMapping("/login-aws")
    public ResponseEntity<?> loginAWS() {
        try {
            // Ejecutar login de AWS CLI
            ProcessBuilder processBuilder = new ProcessBuilder("aws", "sts", "get-caller-identity", "--output", "json");
            processBuilder.environment().put("AWS_CLI_AUTO_PROMPT", "off");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error al obtener identidad con AWS CLI");
            }

            // Leer la respuesta JSON
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String jsonResponse = reader.lines().collect(Collectors.joining());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseJson = mapper.readTree(jsonResponse);

            // Obtener información del usuario
            String accountId = responseJson.get("Account").asText();
            String arn = responseJson.get("Arn").asText();
            String userId = responseJson.get("UserId").asText();

            // Obtener credenciales temporales
            ProcessBuilder credsBuilder = new ProcessBuilder("aws", "sts", "get-session-token", "--output", "json");
            Process credsProcess = credsBuilder.start();
            BufferedReader credsReader = new BufferedReader(new InputStreamReader(credsProcess.getInputStream()));
            String credsJson = credsReader.lines().collect(Collectors.joining());

            JsonNode credsJsonNode = mapper.readTree(credsJson);
            JsonNode credentials = credsJsonNode.get("Credentials");

            Map<String, String> credsMap = new HashMap<>();
            credsMap.put("accessKeyId", credentials.get("AccessKeyId").asText());
            credsMap.put("secretAccessKey", credentials.get("SecretAccessKey").asText());
            credsMap.put("sessionToken", credentials.get("SessionToken").asText());

            return ResponseEntity.ok(Map.of(
                    "accountId", accountId,
                    "arn", arn,
                    "userId", userId,
                    "credentials", credsMap,
                    "message", "Login exitoso con AWS"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error durante el login: " + e.getMessage());
        }
    }

    // Endpoint para renovar el token de AWS
    @GetMapping("/renew-token-aws")
    public ResponseEntity<?> renewTokenAWS() {
        try {
            // Obtener nuevas credenciales temporales
            ProcessBuilder credsBuilder = new ProcessBuilder("aws", "sts", "get-session-token", "--output", "json");
            Process credsProcess = credsBuilder.start();
            BufferedReader credsReader = new BufferedReader(new InputStreamReader(credsProcess.getInputStream()));
            String credsJson = credsReader.lines().collect(Collectors.joining());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode credsJsonNode = mapper.readTree(credsJson);
            JsonNode credentials = credsJsonNode.get("Credentials");

            Map<String, String> credsMap = new HashMap<>();
            credsMap.put("accessKeyId", credentials.get("AccessKeyId").asText());
            credsMap.put("secretAccessKey", credentials.get("SecretAccessKey").asText());
            credsMap.put("sessionToken", credentials.get("SessionToken").asText());

            // Obtener información de la cuenta
            ProcessBuilder identityBuilder = new ProcessBuilder("aws", "sts", "get-caller-identity", "--output", "json");
            Process identityProcess = identityBuilder.start();
            BufferedReader identityReader = new BufferedReader(new InputStreamReader(identityProcess.getInputStream()));
            String identityJson = identityReader.lines().collect(Collectors.joining());

            JsonNode identityJsonNode = mapper.readTree(identityJson);
            String accountId = identityJsonNode.get("Account").asText();

            return ResponseEntity.ok(Map.of(
                    "accountId", accountId,
                    "credentials", credsMap,
                    "message", "Token renovado exitosamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al renovar el token: " + e.getMessage());
        }
    }

    // Endpoint para cerrar sesión en AWS
    @GetMapping("/logout-aws")
    public ResponseEntity<?> logoutAWS() {
        try {
            // Limpiar credenciales de AWS CLI
            ProcessBuilder logoutBuilder = new ProcessBuilder("aws", "configure", "set", "aws_access_key_id", "");
            Process logoutProcess = logoutBuilder.start();
            int exitCode = logoutProcess.waitFor();

            if (exitCode == 0) {
                return ResponseEntity.ok("Sesión cerrada exitosamente");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error al cerrar la sesión de AWS");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error durante el logout de AWS: " + e.getMessage());
        }
    }

    // Endpoint para validar el token de AWS
    @PostMapping("/validate-token-aws")
    public ResponseEntity<?> validateTokenAWS(@RequestBody Map<String, String> request) {
        String accessKeyId = request.get("accessKeyId");
        String secretAccessKey = request.get("secretAccessKey");
        String sessionToken = request.get("sessionToken");

        if (accessKeyId == null || secretAccessKey == null || sessionToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Credenciales incompletas", "details", "Se requieren accessKeyId, secretAccessKey y sessionToken"));
        }

        try {
            // Configurar las variables de entorno para el proceso
            Map<String, String> env = new HashMap<>();
            env.put("AWS_ACCESS_KEY_ID", accessKeyId);
            env.put("AWS_SECRET_ACCESS_KEY", secretAccessKey);
            env.put("AWS_SESSION_TOKEN", sessionToken);

            ProcessBuilder builder = new ProcessBuilder(
                    "aws", "sts", "get-caller-identity",
                    "--output", "json"
            );
            builder.environment().putAll(env);
            builder.redirectErrorStream(true);

            Process process = builder.start();

            // Leer la salida del proceso
            String jsonResponse = new BufferedReader(new InputStreamReader(process.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Credenciales inválidas",
                                "details", jsonResponse));
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseJson = mapper.readTree(jsonResponse);

            String accountId = responseJson.get("Account").asText();
            String arn = responseJson.get("Arn").asText();
            String userId = responseJson.get("UserId").asText();

            // Extraer email si es posible (ARN de usuario IAM)
            String email = "unknown@aws.com";
            if (arn.contains("user/")) {
                email = arn.split("user/")[1] + "@aws.com";
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Token válido",
                    "accountId", accountId,
                    "email", email,
                    "arn", arn,
                    "userId", userId
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error de E/S al validar credenciales",
                            "details", e.getMessage()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Proceso interrumpido",
                            "details", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error validando credenciales",
                            "details", e.getMessage(),
                            "stackTrace", Arrays.stream(e.getStackTrace())
                                    .limit(5)
                                    .map(StackTraceElement::toString)
                                    .collect(Collectors.toList())));
        }
    }

    // Endpoint para sincronizar una cuenta AWS
    @PostMapping("/save-account")
    public ResponseEntity<?> saveSelectedAccount(@RequestBody Map<String, String> request) {
        String accessKeyId = request.get("accessKeyId");
        String secretAccessKey = request.get("secretAccessKey");
        String sessionToken = request.get("sessionToken");
        String email = request.get("email");
        String accountName = request.get("accountName");

        // Validación mejorada de parámetros
        if (accessKeyId == null || accessKeyId.isEmpty() ||
                secretAccessKey == null || secretAccessKey.isEmpty() ||
                sessionToken == null || sessionToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "Credenciales incompletas",
                            "details", "Se requieren accessKeyId, secretAccessKey y sessionToken"
                    ));
        }

        try {
            AWSCloudCredentials credentials = new AWSCloudCredentials(accessKeyId, secretAccessKey, sessionToken);

            // Validar credenciales con timeout
            boolean credentialsValid = false;
            try {
                credentialsValid = resourceManagerServiceAWS.areCredentialsValid(credentials);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "error", "Error validando credenciales",
                                "details", e.getMessage()
                        ));
            }

            if (!credentialsValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Credenciales AWS inválidas o expiradas");
            }

            // Obtener email si no se proporcionó
            if (email == null || email.isEmpty()) {
                email = resourceManagerServiceAWS.getEmailFromCredentials(credentials);
            }

            // Sincronizar recursos
            resourceManagerServiceAWS.syncUserAccountsAndResources(credentials, email);

            // Obtener accountId para la respuesta
            String accountId = resourceManagerServiceAWS.getAccountIdFromCredentials(credentials);

            return ResponseEntity.ok(Map.of(
                    "message", "Cuenta AWS sincronizada correctamente",
                    "accountId", accountId,
                    "accountName", accountName != null ? accountName : "AWS Account"
            ));
        } catch (Exception e) {
            // Respuesta de error más detallada
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error procesando recursos AWS");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("errorType", e.getClass().getName());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }


    // Endpoint para obtener detalles de una cuenta específica
    @GetMapping("/account/{accountId}")
    public ResponseEntity<?> getAccountById(@PathVariable("accountId") String accountId) {
        try {
            AccountDataModel account = cloudResourceServiceAWS.getAccount(accountId);

            if (account == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("La cuenta con ID " + accountId + " no existe.");
            }

            Map<String, Object> accountData = new HashMap<>();
            accountData.put("id", account.getId());
            accountData.put("name", account.getName());
            accountData.put("email", account.getEmail());
            accountData.put("status", account.getStatus());

            // Añadir la lista de instancias EC2 asociadas
            if (account.getEc2Instances() != null && !account.getEc2Instances().isEmpty()) {
                List<Map<String, Object>> ec2List = new ArrayList<>();
                for (EC2InstanceDataModel instance : account.getEc2Instances()) {
                    Map<String, Object> instanceData = new HashMap<>();
                    instanceData.put("instance_id", instance.getId());
                    instanceData.put("name", instance.getName());
                    instanceData.put("instance_type", instance.getInstanceType());
                    instanceData.put("zone", instance.getAvailabilityZone());
                    instanceData.put("status", instance.getState());
                    ec2List.add(instanceData);
                }
                accountData.put("ec2_instances", ec2List);
            } else {
                accountData.put("ec2_instances", List.of());
            }

            return ResponseEntity.ok(accountData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener la cuenta: " + e.getMessage());
        }
    }

    @GetMapping("/ec2instancesAWS")
    public ResponseEntity<List<Map<String, Object>>> getEC2Instances(
            @RequestParam(value = "accountId", required = false) String accountId) {
        List<EC2InstanceDataModel> instances = cloudResourceServiceAWS.getAllEC2Instances();
        List<Map<String, Object>> response = new ArrayList<>();

        for (EC2InstanceDataModel instance : instances) {
            if (accountId == null || instance.getAccount().getId().equals(accountId)) {
                Map<String, Object> instanceData = new HashMap<>();
                instanceData.put("id", instance.getId());
                instanceData.put("instanceId", instance.getInstanceId());
                instanceData.put("name", instance.getName());
                instanceData.put("accountId", instance.getAccount().getId());
                instanceData.put("instanceType", instance.getInstanceType());
                instanceData.put("state", instance.getState());
                instanceData.put("availabilityZone", instance.getAvailabilityZone());
                instanceData.put("publicIp", instance.getPublicIp());
                instanceData.put("privateIp", instance.getPrivateIp());
                response.add(instanceData);
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/vpcsAWS")
    public ResponseEntity<List<Map<String, Object>>> getVPCs(
            @RequestParam(value = "accountId", required = false) String accountId) {
        List<VPCDataModel> vpcs = cloudResourceServiceAWS.getAllVPCs();
        List<Map<String, Object>> response = new ArrayList<>();

        for (VPCDataModel vpc : vpcs) {
            if (accountId == null || vpc.getAccount().getId().equals(accountId)) {
                Map<String, Object> vpcData = new HashMap<>();
                vpcData.put("id", vpc.getId());
                vpcData.put("vpcId", vpc.getVpcId());
                vpcData.put("name", vpc.getName());
                vpcData.put("accountId", vpc.getAccount().getId());
                vpcData.put("cidrBlock", vpc.getCidrBlock());
                vpcData.put("isDefault", vpc.getIsDefault());
                vpcData.put("region", vpc.getRegion());
                vpcData.put("tenancy", vpc.getTenancy());
                response.add(vpcData);
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/subnetsAWS")
    public ResponseEntity<List<Map<String, Object>>> getSubnets(
            @RequestParam(value = "accountId", required = false) String accountId,
            @RequestParam(value = "vpcId", required = false) String vpcId) {
        List<SubnetDataModel> subnets = cloudResourceServiceAWS.getAllSubnets();
        List<Map<String, Object>> response = new ArrayList<>();

        for (SubnetDataModel subnet : subnets) {
            boolean accountMatch = accountId == null || subnet.getAccount().getId().equals(accountId);
            boolean vpcMatch = vpcId == null || (subnet.getVpc() != null && subnet.getVpc().getId().equals(vpcId));

            if (accountMatch && vpcMatch) {
                Map<String, Object> subnetData = new HashMap<>();
                subnetData.put("id", subnet.getId());
                subnetData.put("subnetId", subnet.getSubnetId());
                subnetData.put("name", subnet.getName());
                subnetData.put("accountId", subnet.getAccount().getId());
                subnetData.put("vpcId", subnet.getVpc() != null ? subnet.getVpc().getId() : null);
                subnetData.put("cidrBlock", subnet.getCidrBlock());
                subnetData.put("availabilityZone", subnet.getAvailabilityZone());
                subnetData.put("mapPublicIp", subnet.getMapPublicIp());
                response.add(subnetData);
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/securitygroupsAWS")
    public ResponseEntity<List<Map<String, Object>>> getSecurityGroups(
            @RequestParam(value = "accountId", required = false) String accountId,
            @RequestParam(value = "vpcId", required = false) String vpcId) {
        List<SecurityGroupDataModel> securityGroups = cloudResourceServiceAWS.getAllSecurityGroups();
        List<Map<String, Object>> response = new ArrayList<>();

        for (SecurityGroupDataModel sg : securityGroups) {
            boolean accountMatch = accountId == null || sg.getAccount().getId().equals(accountId);
            boolean vpcMatch = vpcId == null || (sg.getVpc() != null && sg.getVpc().getId().equals(vpcId));

            if (accountMatch && vpcMatch) {
                Map<String, Object> sgData = new HashMap<>();
                sgData.put("id", sg.getId());
                sgData.put("groupId", sg.getGroupId());
                sgData.put("name", sg.getName());
                sgData.put("accountId", sg.getAccount().getId());
                sgData.put("vpcId", sg.getVpc() != null ? sg.getVpc().getId() : null);
                sgData.put("description", sg.getDescription());
                response.add(sgData);
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/ebsvolumesAWS")
    public ResponseEntity<List<Map<String, Object>>> getEBSVolumes(
            @RequestParam(value = "accountId", required = false) String accountId) {
        List<EBSVolumeDataModel> volumes = cloudResourceServiceAWS.getAllEBSVolumes();
        List<Map<String, Object>> response = new ArrayList<>();

        for (EBSVolumeDataModel volume : volumes) {
            if (accountId == null || volume.getAccount().getId().equals(accountId)) {
                Map<String, Object> volumeData = new HashMap<>();
                volumeData.put("id", volume.getId());
                volumeData.put("volumeId", volume.getVolumeId());
                volumeData.put("name", volume.getName());
                volumeData.put("accountId", volume.getAccount().getId());
                volumeData.put("type", volume.getType());
                volumeData.put("state", volume.getState());
                volumeData.put("availabilityZone", volume.getAvailabilityZone());
                volumeData.put("sizeGb", volume.getSizeGb());
                volumeData.put("encrypted", volume.getEncrypted());
                response.add(volumeData);
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/s3bucketsAWS")
    public ResponseEntity<List<Map<String, Object>>> getS3Buckets(
            @RequestParam(value = "accountId", required = false) String accountId) {
        List<S3BucketDataModel> buckets = cloudResourceServiceAWS.getAllS3Buckets();
        List<Map<String, Object>> response = new ArrayList<>();

        for (S3BucketDataModel bucket : buckets) {
            if (accountId == null || bucket.getAccount().getId().equals(accountId)) {
                Map<String, Object> bucketData = new HashMap<>();
                bucketData.put("id", bucket.getId());
                bucketData.put("name", bucket.getName());
                bucketData.put("accountId", bucket.getAccount().getId());
                bucketData.put("region", bucket.getRegion());
                bucketData.put("creationDate", bucket.getCreationDate());
                bucketData.put("versioningEnabled", bucket.getVersioningEnabled());
                bucketData.put("publicAccessBlocked", bucket.getPublicAccessBlocked());
                response.add(bucketData);
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/loadbalancersAWS")
    public ResponseEntity<List<Map<String, Object>>> getLoadBalancers(
            @RequestParam(value = "accountId", required = false) String accountId) {
        List<LoadBalancerDataModelAWS> loadBalancers = cloudResourceServiceAWS.getAllLoadBalancers();
        List<Map<String, Object>> response = new ArrayList<>();

        for (LoadBalancerDataModelAWS lb : loadBalancers) {
            if (accountId == null || lb.getAccount().getId().equals(accountId)) {
                Map<String, Object> lbData = new HashMap<>();
                lbData.put("id", lb.getId());
                lbData.put("arn", lb.getArn());
                lbData.put("name", lb.getName());
                lbData.put("accountId", lb.getAccount().getId());
                lbData.put("dnsName", lb.getDnsName());
                lbData.put("vpcId", lb.getVpc() != null ? lb.getVpc().getId() : null);
                lbData.put("region", lb.getRegion());
                lbData.put("type", lb.getType());
                lbData.put("scheme", lb.getScheme());
                lbData.put("state", lb.getState());
                response.add(lbData);
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/rdsinstancesAWS")
    public ResponseEntity<List<Map<String, Object>>> getRDSInstances(
            @RequestParam(value = "accountId", required = false) String accountId) {
        List<RDSInstanceDataModel> rdsInstances = cloudResourceServiceAWS.getAllRDSInstances();
        List<Map<String, Object>> response = new ArrayList<>();

        for (RDSInstanceDataModel rds : rdsInstances) {
            if (accountId == null || rds.getAccount().getId().equals(accountId)) {
                Map<String, Object> rdsData = new HashMap<>();
                rdsData.put("id", rds.getId());
                rdsData.put("dbInstanceIdentifier", rds.getDbInstanceIdentifier());
                rdsData.put("accountId", rds.getAccount().getId());
                rdsData.put("engine", rds.getEngine());
                rdsData.put("instanceClass", rds.getInstanceClass());
                rdsData.put("vpcId", rds.getVpc() != null ? rds.getVpc().getId() : null);
                rdsData.put("status", rds.getStatus());
                rdsData.put("multiAz", rds.getMultiAz());
                rdsData.put("endpoint", rds.getEndpoint());
                rdsData.put("allocatedStorage", rds.getAllocatedStorage());
                response.add(rdsData);
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/eksclustersAWS")
    public ResponseEntity<List<Map<String, Object>>> getEKSClusters(
            @RequestParam(value = "accountId", required = false) String accountId) {
        List<EKSClusterDataModel> clusters = cloudResourceServiceAWS.getAllEKSClusters();
        List<Map<String, Object>> response = new ArrayList<>();

        for (EKSClusterDataModel cluster : clusters) {
            if (accountId == null || cluster.getAccount().getId().equals(accountId)) {
                Map<String, Object> clusterData = new HashMap<>();
                clusterData.put("id", cluster.getId());
                clusterData.put("name", cluster.getName());
                clusterData.put("accountId", cluster.getAccount().getId());
                clusterData.put("region", cluster.getRegion());
                clusterData.put("version", cluster.getVersion());
                clusterData.put("status", cluster.getStatus());
                clusterData.put("roleArn", cluster.getRoleArn());
                clusterData.put("vpcId", cluster.getVpc() != null ? cluster.getVpc().getId() : null);
                response.add(clusterData);
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/iamrolesAWS")
    public ResponseEntity<List<Map<String, Object>>> getIAMRoles(
            @RequestParam(value = "accountId", required = false) String accountId) {
        List<IAMRoleDataModel> roles = cloudResourceServiceAWS.getAllIAMRoles();
        List<Map<String, Object>> response = new ArrayList<>();

        for (IAMRoleDataModel role : roles) {
            if (accountId == null || role.getAccount().getId().equals(accountId)) {
                Map<String, Object> roleData = new HashMap<>();
                roleData.put("id", role.getId());
                roleData.put("arn", role.getArn());
                roleData.put("name", role.getName());
                roleData.put("accountId", role.getAccount().getId());
                roleData.put("path", role.getPath());
                roleData.put("createDate", role.getCreateDate());
                roleData.put("description", role.getDescription());
                response.add(roleData);
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/snapshotsAWS")
    public ResponseEntity<List<Map<String, Object>>> getSnapshots(
            @RequestParam(value = "accountId", required = false) String accountId) {
        List<SnapshotDataModelAWS> snapshots = cloudResourceServiceAWS.getAllSnapshots();
        List<Map<String, Object>> response = new ArrayList<>();

        for (SnapshotDataModelAWS snapshot : snapshots) {
            if (accountId == null || snapshot.getAccount().getId().equals(accountId)) {
                Map<String, Object> snapshotData = new HashMap<>();
                snapshotData.put("id", snapshot.getId());
                snapshotData.put("snapshotId", snapshot.getSnapshotId());
                snapshotData.put("accountId", snapshot.getAccount().getId());
                snapshotData.put("volumeId", snapshot.getVolumeId());
                snapshotData.put("volumeSize", snapshot.getVolumeSize());
                snapshotData.put("state", snapshot.getState());
                snapshotData.put("encrypted", snapshot.getEncrypted());
                snapshotData.put("name", snapshot.getName());
                snapshotData.put("description", snapshot.getDescription());
                response.add(snapshotData);
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/vpnconnectionsAWS")
    public ResponseEntity<List<Map<String, Object>>> getVPNConnections(
            @RequestParam(value = "accountId", required = false) String accountId) {
        List<VPNConnectionDataModel> vpnConnections = cloudResourceServiceAWS.getAllVPNConnections();
        List<Map<String, Object>> response = new ArrayList<>();

        for (VPNConnectionDataModel vpn : vpnConnections) {
            if (accountId == null || vpn.getAccount().getId().equals(accountId)) {
                Map<String, Object> vpnData = new HashMap<>();
                vpnData.put("id", vpn.getId());
                vpnData.put("vpnConnectionId", vpn.getVpnConnectionId());
                vpnData.put("accountId", vpn.getAccount().getId());
                vpnData.put("vpcId", vpn.getVpc() != null ? vpn.getVpc().getId() : null);
                vpnData.put("state", vpn.getState());
                vpnData.put("customerGatewayId", vpn.getCustomerGatewayId());
                vpnData.put("vpnGatewayId", vpn.getVpnGatewayId());
                vpnData.put("type", vpn.getType());
                vpnData.put("name", vpn.getName());
                response.add(vpnData);
            }
        }

        return ResponseEntity.ok(response);
    }

}


