package com.gestion_backend.service_cloud.services_aws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion_backend.controller_bd.IniciarBD;
import com.gestion_backend.dataModels.modelsAWS.*;
import software.amazon.awssdk.regions.Region;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.ec2.model.Subnet;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.*;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.*;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.*;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class ResourceManagerServiceAWS {

    @Autowired
    private IniciarBD iniciarBD;

    @Autowired
    private CloudResourceServiceAWS cloudResourceServiceAWS;

    @PostConstruct
    public void init() {
        try {
            iniciarBD.creacionBD();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para obtener el ID de cuenta AWS a partir de las credenciales
    public String getAccountIdFromCredentials(AWSCloudCredentials credentials) {
        try {
            IamClient iamClient = IamClient.builder()
                    .credentialsProvider(credentials)
                    .build();

            GetUserResponse userResponse = iamClient.getUser();
            String arn = userResponse.user().arn();
            // El accountId es el cuarto elemento del ARN separado por ":"
            return arn.split(":")[4];
        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo el ID de cuenta AWS: " + e.getMessage(), e);
        }
    }

    private String getEmailFromCredentials(AWSCloudCredentials credentials) {
        try {
            StsClient stsClient = StsClient.builder()
                    .credentialsProvider(credentials)
                    .region(Region.US_EAST_1)
                    .build();

            GetCallerIdentityResponse response = stsClient.getCallerIdentity();
            String arn = response.arn();

            // Extraer email del ARN si es un usuario IAM
            if (arn.contains("user/")) {
                return arn.split("user/")[1] + "@aws.com";
            }

            return "unknown@aws.com";
        } catch (Exception e) {
            return "unknown@aws.com";
        }
    }

    // Método para autenticar al usuario con credenciales de AWS
    public AccountDataModel authenticateWithCredentials(AWSCloudCredentials credentials) {
        try {
            // Verificar si las credenciales son válidas
            if (!areCredentialsValid(credentials)) {
                return null;
            }

            // Obtener ID de cuenta
            String accountId = getAccountIdFromCredentials(credentials);

            // Buscar la cuenta en la base de datos
            AccountDataModel account = cloudResourceServiceAWS.getAccount(accountId);

            if (account == null) {
                // Crear nueva cuenta si no existe
                account = new AccountDataModel();
                account.setId(accountId);
                account.setName("AWS Account " + accountId);
                account.setEmail(getEmailFromCredentials(credentials));
                account.setStatus("ACTIVE");
                cloudResourceServiceAWS.saveAccount(account.getId(), account.getEmail(), null,
                        account.getStatus(), account.getName(), null, null);
            }

            return account;
        } catch (Exception e) {
            throw new RuntimeException("Error autenticando con credenciales AWS: " + e.getMessage(), e);
        }
    }

    // Método para sincronizar el usuario con las cuentas AWS
    public void syncUserAccountsAndResources(AWSCloudCredentials credentials, String email) {
        try {
            // Obtener información de la cuenta AWS
            String accountId = getAccountIdFromCredentials(credentials);

            // Verificar si la cuenta ya existe
            AccountDataModel account = cloudResourceServiceAWS.getAccount(accountId);

            if (account == null) {
                // Crear nueva cuenta si no existe
                account = new AccountDataModel();
                account.setId(accountId);
                account.setName("AWS Account " + accountId);
                account.setEmail(email);
                account.setStatus("ACTIVE");
                cloudResourceServiceAWS.saveAccount(account.getId(), account.getEmail(), null,
                        account.getStatus(), account.getName(), null, null);
            }

            // Extraer todos los recursos de AWS
            extraerEC2Instances(credentials, account);
            extraerVPCs(credentials, account);
            extraerSubnets(credentials, account);
            extraerSecurityGroups(credentials, account);
            extraerEBSVolumes(credentials, account);
            extraerSnapshots(credentials, account);
            extraerLoadBalancers(credentials, account);
            extraerRDSInstances(credentials, account);
            extraerEKSClusters(credentials, account);
            extraerS3Buckets(credentials, account);
            extraerIAMRoles(credentials, account);
            extraerVPNConnections(credentials, account);

            System.out.println("Recursos de AWS extraídos correctamente para la cuenta: " + accountId);
        } catch (Exception e) {
            throw new RuntimeException("Error sincronizando recursos de AWS: " + e.getMessage(), e);
        }
    }

    private void extraerEC2Instances(AWSCloudCredentials credentials, AccountDataModel account) {
        try {
            Ec2Client ec2Client = Ec2Client.builder()
                    .credentialsProvider(credentials)
                    .region(Region.US_EAST_1) // Puedes ajustar la región según sea necesario
                    .build();

            DescribeInstancesResponse response = ec2Client.describeInstances();
            for (Reservation reservation : response.reservations()) {
                for (Instance instance : reservation.instances()) {
                    EC2InstanceDataModel instanceModel = new EC2InstanceDataModel();
                    instanceModel.setId(instance.instanceId());
                    instanceModel.setInstanceId(instance.instanceId());
                    instanceModel.setName(instance.tags().stream()
                            .filter(tag -> tag.key().equals("Name"))
                            .findFirst()
                            .map(software.amazon.awssdk.services.ec2.model.Tag::value)
                            .orElse(""));
                    instanceModel.setAccount(account);
                    instanceModel.setInstanceType(instance.instanceTypeAsString());
                    instanceModel.setAvailabilityZone(instance.placement().availabilityZone());
                    instanceModel.setState(instance.state().nameAsString());
                    instanceModel.setPublicIp(instance.publicIpAddress());
                    instanceModel.setPrivateIp(instance.privateIpAddress());

                    // Convertir detalles a JSON
                    ObjectMapper mapper = new ObjectMapper();
                    instanceModel.setDetails(mapper.writeValueAsString(instance));

                    cloudResourceServiceAWS.saveEC2Instance(
                            instanceModel.getId(),
                            instanceModel.getInstanceId(),
                            account,
                            instanceModel.getInstanceType(),
                            instanceModel.getState(),
                            instanceModel.getAvailabilityZone(),
                            instanceModel.getPublicIp(),
                            instanceModel.getPrivateIp(),
                            instanceModel.getName(),
                            instanceModel.getDetails()
                    );
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo instancias EC2: " + e.getMessage(), e);
        }
    }

    private void extraerVPCs(AWSCloudCredentials credentials, AccountDataModel account) {
        try {
            Ec2Client ec2Client = Ec2Client.builder()
                    .credentialsProvider(credentials)
                    .region(Region.US_EAST_1)
                    .build();

            DescribeVpcsResponse response = ec2Client.describeVpcs();
            for (Vpc vpc : response.vpcs()) {
                VPCDataModel vpcModel = new VPCDataModel();
                vpcModel.setId(vpc.vpcId());
                vpcModel.setVpcId(vpc.vpcId());
                vpcModel.setName(vpc.tags().stream()
                        .filter(tag -> tag.key().equals("Name"))
                        .findFirst()
                        .map(software.amazon.awssdk.services.ec2.model.Tag::value)
                        .orElse(""));
                vpcModel.setAccount(account);
                vpcModel.setCidrBlock(vpc.cidrBlock());
                vpcModel.setIsDefault(vpc.isDefault());
                vpcModel.setTenancy(vpc.instanceTenancyAsString());

                ObjectMapper mapper = new ObjectMapper();
                vpcModel.setDetails(mapper.writeValueAsString(vpc));

                cloudResourceServiceAWS.saveVPC(
                        vpcModel.getId(),
                        vpcModel.getVpcId(),
                        account,
                        vpcModel.getCidrBlock(),
                        vpcModel.getIsDefault(),
                        null, // region
                        vpcModel.getName(),
                        vpcModel.getTenancy(),
                        vpcModel.getDetails()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo VPCs: " + e.getMessage(), e);
        }
    }

    private void extraerSubnets(AWSCloudCredentials credentials, AccountDataModel account) {
        try {
            Ec2Client ec2Client = Ec2Client.builder()
                    .credentialsProvider(credentials)
                    .region(Region.US_EAST_1)
                    .build();

            DescribeSubnetsResponse response = ec2Client.describeSubnets();
            for (Subnet subnet : response.subnets()) {
                SubnetDataModel subnetModel = new SubnetDataModel();
                subnetModel.setId(subnet.subnetId());
                subnetModel.setSubnetId(subnet.subnetId());
                subnetModel.setName(subnet.tags().stream()
                        .filter(tag -> tag.key().equals("Name"))
                        .findFirst()
                        .map(software.amazon.awssdk.services.ec2.model.Tag::value)
                        .orElse(""));

                // Obtener el VPC asociado
                VPCDataModel vpc = cloudResourceServiceAWS.getVPC(subnet.vpcId());
                subnetModel.setVpc(vpc);
                subnetModel.setAccount(account);
                subnetModel.setCidrBlock(subnet.cidrBlock());
                subnetModel.setAvailabilityZone(subnet.availabilityZone());
                subnetModel.setMapPublicIp(subnet.mapPublicIpOnLaunch());

                ObjectMapper mapper = new ObjectMapper();
                subnetModel.setDetails(mapper.writeValueAsString(subnet));

                cloudResourceServiceAWS.saveSubnet(
                        subnetModel.getId(),
                        subnetModel.getSubnetId(),
                        account,
                        subnetModel.getCidrBlock(),
                        subnetModel.getAvailabilityZone(),
                        subnetModel.getMapPublicIp(),
                        subnetModel.getVpc(),
                        subnetModel.getName(),
                        subnetModel.getDetails()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo subredes: " + e.getMessage(), e);
        }
    }

    private void extraerSecurityGroups(AWSCloudCredentials credentials, AccountDataModel account) {
        try {
            Ec2Client ec2Client = Ec2Client.builder()
                    .credentialsProvider(credentials)
                    .region(Region.US_EAST_1)
                    .build();

            DescribeSecurityGroupsResponse response = ec2Client.describeSecurityGroups();
            for (SecurityGroup sg : response.securityGroups()) {
                SecurityGroupDataModel sgModel = new SecurityGroupDataModel();
                sgModel.setId(sg.groupId());
                sgModel.setGroupId(sg.groupId());
                sgModel.setName(sg.groupName());

                // Obtener el VPC asociado
                VPCDataModel vpc = cloudResourceServiceAWS.getVPC(sg.vpcId());
                sgModel.setVpc(vpc);
                sgModel.setAccount(account);
                sgModel.setDescription(sg.description());

                ObjectMapper mapper = new ObjectMapper();
                sgModel.setDetails(mapper.writeValueAsString(sg));

                cloudResourceServiceAWS.saveSecurityGroup(
                        sgModel.getId(),
                        sgModel.getGroupId(),
                        sgModel.getName(),
                        account,
                        sgModel.getDescription(),
                        sgModel.getVpc(),
                        sgModel.getDetails()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo grupos de seguridad: " + e.getMessage(), e);
        }
    }

    private void extraerEBSVolumes(AWSCloudCredentials credentials, AccountDataModel account) {
        try {
            Ec2Client ec2Client = Ec2Client.builder()
                    .credentialsProvider(credentials)
                    .region(Region.US_EAST_1)
                    .build();

            DescribeVolumesResponse response = ec2Client.describeVolumes();
            for (Volume volume : response.volumes()) {
                EBSVolumeDataModel volumeModel = new EBSVolumeDataModel();
                volumeModel.setId(volume.volumeId());
                volumeModel.setVolumeId(volume.volumeId());
                volumeModel.setName(volume.tags().stream()
                        .filter(tag -> tag.key().equals("Name"))
                        .findFirst()
                        .map(software.amazon.awssdk.services.ec2.model.Tag::value)
                        .orElse(""));
                volumeModel.setAccount(account);
                volumeModel.setType(volume.volumeTypeAsString());
                volumeModel.setSizeGb(volume.size());
                volumeModel.setState(volume.stateAsString());
                volumeModel.setAvailabilityZone(volume.availabilityZone());
                volumeModel.setEncrypted(volume.encrypted());

                ObjectMapper mapper = new ObjectMapper();
                volumeModel.setDetails(mapper.writeValueAsString(volume));

                cloudResourceServiceAWS.saveEBSVolume(
                        volumeModel.getId(),
                        volumeModel.getVolumeId(),
                        account,
                        volumeModel.getType(),
                        volumeModel.getState(),
                        volumeModel.getAvailabilityZone(),
                        volumeModel.getSizeGb(),
                        volumeModel.getEncrypted(),
                        volumeModel.getName(),
                        volumeModel.getDetails()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo volúmenes EBS: " + e.getMessage(), e);
        }
    }

    private void extraerSnapshots(AWSCloudCredentials credentials, AccountDataModel account) {
        try {
            Ec2Client ec2Client = Ec2Client.builder()
                    .credentialsProvider(credentials)
                    .region(Region.US_EAST_1)
                    .build();

            DescribeSnapshotsResponse response = ec2Client.describeSnapshots(
                    DescribeSnapshotsRequest.builder()
                            .ownerIds(account.getId())
                            .build());

            for (Snapshot snapshot : response.snapshots()) {
                SnapshotDataModelAWS snapshotModel = new SnapshotDataModelAWS();
                snapshotModel.setId(snapshot.snapshotId());
                snapshotModel.setSnapshotId(snapshot.snapshotId());
                snapshotModel.setName(snapshot.tags().stream()
                        .filter(tag -> tag.key().equals("Name"))
                        .findFirst()
                        .map(software.amazon.awssdk.services.ec2.model.Tag::value)
                        .orElse(""));
                snapshotModel.setAccount(account);
                snapshotModel.setVolumeId(snapshot.volumeId());
                snapshotModel.setVolumeSize(snapshot.volumeSize());
                snapshotModel.setState(snapshot.stateAsString());
                snapshotModel.setEncrypted(snapshot.encrypted());
                snapshotModel.setDescription(snapshot.description());

                ObjectMapper mapper = new ObjectMapper();
                snapshotModel.setDetails(mapper.writeValueAsString(snapshot));

                cloudResourceServiceAWS.saveSnapshot(
                        snapshotModel.getId(),
                        snapshotModel.getSnapshotId(),
                        account,
                        snapshotModel.getVolumeId(),
                        snapshotModel.getVolumeSize(),
                        snapshotModel.getState(),
                        snapshotModel.getEncrypted(),
                        snapshotModel.getName(),
                        snapshotModel.getDescription(),
                        snapshotModel.getDetails()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo snapshots: " + e.getMessage(), e);
        }
    }

    private void extraerLoadBalancers(AWSCloudCredentials credentials, AccountDataModel account) {
        try {
            ElasticLoadBalancingV2Client elbv2Client = ElasticLoadBalancingV2Client.builder()
                    .credentialsProvider(credentials)
                    .region(Region.US_EAST_1)
                    .build();

            DescribeLoadBalancersResponse response = elbv2Client.describeLoadBalancers();
            for (LoadBalancer lb : response.loadBalancers()) {
                LoadBalancerDataModelAWS lbModel = new LoadBalancerDataModelAWS();
                lbModel.setId(lb.loadBalancerArn());
                lbModel.setArn(lb.loadBalancerArn());
                lbModel.setName(lb.loadBalancerName());
                lbModel.setAccount(account);
                lbModel.setRegion(lb.availabilityZones().get(0).zoneName());
                lbModel.setType(lb.typeAsString());
                lbModel.setScheme(lb.schemeAsString());

                // Obtener el VPC asociado
                VPCDataModel vpc = cloudResourceServiceAWS.getVPC(lb.vpcId());
                lbModel.setVpc(vpc);

                lbModel.setDnsName(lb.dnsName());
                lbModel.setState(lb.state().codeAsString());

                ObjectMapper mapper = new ObjectMapper();
                lbModel.setDetails(mapper.writeValueAsString(lb));

                cloudResourceServiceAWS.saveLoadBalancer(
                        lbModel.getId(),
                        lbModel.getArn(),
                        lbModel.getName(),
                        account,
                        lbModel.getDnsName(),
                        lbModel.getVpc(),
                        lbModel.getRegion(),
                        lbModel.getType(),
                        lbModel.getScheme(),
                        lbModel.getState(),
                        lbModel.getDetails()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo balanceadores de carga: " + e.getMessage(), e);
        }
    }

    private void extraerRDSInstances(AWSCloudCredentials credentials, AccountDataModel account) {
        try {
            RdsClient rdsClient = RdsClient.builder()
                    .credentialsProvider(credentials)
                    .region(Region.US_EAST_1)
                    .build();

            DescribeDbInstancesResponse response = rdsClient.describeDBInstances();
            for (DBInstance instance : response.dbInstances()) {
                RDSInstanceDataModel instanceModel = new RDSInstanceDataModel();
                instanceModel.setId(instance.dbInstanceArn());
                instanceModel.setDbInstanceIdentifier(instance.dbInstanceIdentifier());
                instanceModel.setAccount(account);
                instanceModel.setEngine(instance.engine());
                instanceModel.setStatus(instance.dbInstanceStatus());
                instanceModel.setInstanceClass(instance.dbInstanceClass());
                instanceModel.setAllocatedStorage(instance.allocatedStorage());

                // Obtener el VPC asociado
                VPCDataModel vpc = cloudResourceServiceAWS.getVPC(instance.dbSubnetGroup().vpcId());
                instanceModel.setVpc(vpc);

                instanceModel.setEndpoint(instance.endpoint().address());
                instanceModel.setMultiAz(instance.multiAZ());

                ObjectMapper mapper = new ObjectMapper();
                instanceModel.setDetails(mapper.writeValueAsString(instance));

                cloudResourceServiceAWS.saveRDSInstance(
                        instanceModel.getId(),
                        instanceModel.getDbInstanceIdentifier(),
                        account,
                        instanceModel.getEngine(),
                        instanceModel.getInstanceClass(),
                        instanceModel.getVpc(),
                        instanceModel.getStatus(),
                        instanceModel.getMultiAz(),
                        instanceModel.getEndpoint(),
                        instanceModel.getAllocatedStorage(),
                        instanceModel.getDetails()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo instancias RDS: " + e.getMessage(), e);
        }
    }

    private void extraerEKSClusters(AWSCloudCredentials credentials, AccountDataModel account) {
        try {
            EksClient eksClient = EksClient.builder()
                    .credentialsProvider(credentials)
                    .region(Region.US_EAST_1)
                    .build();

            ListClustersResponse response = eksClient.listClusters();
            for (String clusterName : response.clusters()) {
                DescribeClusterRequest describeRequest = DescribeClusterRequest.builder()
                        .name(clusterName)
                        .build();

                DescribeClusterResponse describeResponse = eksClient.describeCluster(describeRequest);
                Cluster cluster = describeResponse.cluster();

                EKSClusterDataModel clusterModel = new EKSClusterDataModel();
                clusterModel.setId(cluster.arn());
                clusterModel.setName(cluster.name());
                clusterModel.setAccount(account);
                clusterModel.setRegion(Region.US_EAST_1.id());
                clusterModel.setStatus(cluster.statusAsString());
                clusterModel.setVersion(cluster.version());
                clusterModel.setRoleArn(cluster.roleArn());

                // Obtener el VPC asociado
                VPCDataModel vpc = cloudResourceServiceAWS.getVPC(cluster.resourcesVpcConfig().vpcId());
                clusterModel.setVpc(vpc);

                ObjectMapper mapper = new ObjectMapper();
                clusterModel.setDetails(mapper.writeValueAsString(cluster));

                cloudResourceServiceAWS.saveEKSCluster(
                        clusterModel.getId(),
                        clusterModel.getName(),
                        account,
                        clusterModel.getRegion(),
                        clusterModel.getVersion(),
                        clusterModel.getStatus(),
                        clusterModel.getRoleArn(),
                        clusterModel.getVpc(),
                        clusterModel.getDetails()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo clusters EKS: " + e.getMessage(), e);
        }
    }

    private void extraerS3Buckets(AWSCloudCredentials credentials, AccountDataModel account) {
        try {
            S3Client s3Client = S3Client.builder()
                    .credentialsProvider(credentials)
                    .region(Region.US_EAST_1)
                    .build();

            ListBucketsResponse response = s3Client.listBuckets();
            for (Bucket bucket : response.buckets()) {
                // Obtener detalles adicionales del bucket
                GetBucketLocationRequest locationRequest = GetBucketLocationRequest.builder()
                        .bucket(bucket.name())
                        .build();

                String region = s3Client.getBucketLocation(locationRequest).locationConstraintAsString();
                if (region == null || region.isEmpty()) {
                    region = "us-east-1"; // El valor por defecto para us-east-1 es null
                }

                S3BucketDataModel bucketModel = new S3BucketDataModel();
                bucketModel.setId(bucket.name());
                bucketModel.setName(bucket.name());
                bucketModel.setAccount(account);
                bucketModel.setRegion(region);

                // Convertir Instant a LocalDateTime
                LocalDateTime creationDate = LocalDateTime.ofInstant(
                        bucket.creationDate(),
                        ZoneId.systemDefault());
                bucketModel.setCreationDate(creationDate);

                // Obtener configuración de versionado
                try {
                    GetBucketVersioningRequest versioningRequest = GetBucketVersioningRequest.builder()
                            .bucket(bucket.name())
                            .build();
                    GetBucketVersioningResponse versioningResponse = s3Client.getBucketVersioning(versioningRequest);
                    bucketModel.setVersioningEnabled(
                            versioningResponse.status() == BucketVersioningStatus.ENABLED);
                } catch (Exception e) {
                    bucketModel.setVersioningEnabled(false);
                }

                // Obtener configuración de acceso público
                try {
                    GetPublicAccessBlockRequest publicAccessRequest = GetPublicAccessBlockRequest.builder()
                            .bucket(bucket.name())
                            .build();
                    GetPublicAccessBlockResponse publicAccessResponse = s3Client.getPublicAccessBlock(publicAccessRequest);
                    bucketModel.setPublicAccessBlocked(
                            publicAccessResponse.publicAccessBlockConfiguration().blockPublicAcls() ||
                                    publicAccessResponse.publicAccessBlockConfiguration().blockPublicPolicy() ||
                                    publicAccessResponse.publicAccessBlockConfiguration().ignorePublicAcls() ||
                                    publicAccessResponse.publicAccessBlockConfiguration().restrictPublicBuckets());
                } catch (Exception e) {
                    bucketModel.setPublicAccessBlocked(false);
                }

                ObjectMapper mapper = new ObjectMapper();
                bucketModel.setDetails(mapper.writeValueAsString(bucket));

                cloudResourceServiceAWS.saveS3Bucket(
                        bucketModel.getId(),
                        bucketModel.getName(),
                        account,
                        bucketModel.getRegion(),
                        bucketModel.getCreationDate(),
                        bucketModel.getVersioningEnabled(),
                        bucketModel.getPublicAccessBlocked(),
                        bucketModel.getDetails()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo buckets S3: " + e.getMessage(), e);
        }
    }

    private void extraerIAMRoles(AWSCloudCredentials credentials, AccountDataModel account) {
        try {
            IamClient iamClient = IamClient.builder()
                    .credentialsProvider(credentials)
                    .region(Region.AWS_GLOBAL) // IAM es un servicio global
                    .build();

            ListRolesResponse response = iamClient.listRoles();
            for (Role role : response.roles()) {
                IAMRoleDataModel roleModel = new IAMRoleDataModel();
                roleModel.setId(role.arn());
                roleModel.setArn(role.arn());
                roleModel.setName(role.roleName());
                roleModel.setAccount(account);
                roleModel.setPath(role.path());

                // Convertir Instant a LocalDateTime
                LocalDateTime createDate = LocalDateTime.ofInstant(
                        role.createDate(),
                        ZoneId.systemDefault());
                roleModel.setCreateDate(createDate);

                roleModel.setDescription(role.description());

                ObjectMapper mapper = new ObjectMapper();
                roleModel.setDetails(mapper.writeValueAsString(role));

                cloudResourceServiceAWS.saveIAMRole(
                        roleModel.getId(),
                        roleModel.getArn(),
                        roleModel.getName(),
                        account,
                        roleModel.getPath(),
                        roleModel.getCreateDate(),
                        roleModel.getDescription(),
                        roleModel.getDetails()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo roles IAM: " + e.getMessage(), e);
        }
    }

    private void extraerVPNConnections(AWSCloudCredentials credentials, AccountDataModel account) {
        try {
            Ec2Client ec2Client = Ec2Client.builder()
                    .credentialsProvider(credentials)
                    .region(Region.US_EAST_1)
                    .build();

            DescribeVpnConnectionsResponse response = ec2Client.describeVpnConnections();
            for (VpnConnection vpn : response.vpnConnections()) {
                VPNConnectionDataModel vpnModel = new VPNConnectionDataModel();
                vpnModel.setId(vpn.vpnConnectionId());
                vpnModel.setVpnConnectionId(vpn.vpnConnectionId());
                vpnModel.setName(vpn.tags().stream()
                        .filter(tag -> tag.key().equals("Name"))
                        .findFirst()
                        .map(software.amazon.awssdk.services.ec2.model.Tag::value)
                        .orElse(""));
                vpnModel.setAccount(account);

                // Obtener el VPC asociado
                VPCDataModel vpc = cloudResourceServiceAWS.getVPC(vpn.vpnGatewayId());
                vpnModel.setVpc(vpc);

                vpnModel.setState(vpn.stateAsString());
                vpnModel.setCustomerGatewayId(vpn.customerGatewayId());
                vpnModel.setVpnGatewayId(vpn.vpnGatewayId());
                vpnModel.setType(vpn.typeAsString());

                ObjectMapper mapper = new ObjectMapper();
                vpnModel.setDetails(mapper.writeValueAsString(vpn));

                cloudResourceServiceAWS.saveVPNConnection(
                        vpnModel.getId(),
                        vpnModel.getVpnConnectionId(),
                        account,
                        vpnModel.getVpc(),
                        vpnModel.getState(),
                        vpnModel.getCustomerGatewayId(),
                        vpnModel.getVpnGatewayId(),
                        vpnModel.getType(),
                        vpnModel.getName(),
                        vpnModel.getDetails()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Error extrayendo conexiones VPN: " + e.getMessage(), e);
        }
    }

    //metodo para verificar si las credenciales son válidas
    public boolean areCredentialsValid(AWSCloudCredentials credentials) {
        try {
            StsClient stsClient = StsClient.builder()
                    .credentialsProvider(credentials)
                    .region(Region.US_EAST_1)
                    .build();

            stsClient.getCallerIdentity();
            return true;
        } catch (Exception e) {
            System.err.println("Error validating AWS credentials: " + e.getMessage());
            return false;
        }
    }
}

