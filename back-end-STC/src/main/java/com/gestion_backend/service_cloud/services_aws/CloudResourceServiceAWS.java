package com.gestion_backend.service_cloud.services_aws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion_backend.dataModels.modelsAWS.*;
import com.gestion_backend.repository_cloud.repository_aws.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetSessionTokenResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class CloudResourceServiceAWS {

    @Autowired
    private OrganizationRepositoryAWS organizationRepository;
    @Autowired
    private AccountRepositoryAWS accountRepository;
    @Autowired
    private EC2InstanceRepositoryAWS ec2InstanceRepository;
    @Autowired
    private EBSVolumeRepositoryAWS ebsVolumeRepository;
    @Autowired
    private S3BucketRepositoryAWS s3BucketRepository;
    @Autowired
    private VPCRepositoryAWS vpcRepository;
    @Autowired
    private SubnetRepositoryAWS subnetRepository;
    @Autowired
    private SecurityGroupRepositoryAWS securityGroupRepository;
    @Autowired
    private LoadBalancerRepositoryAWS loadBalancerRepository;
    @Autowired
    private RDSInstanceRepositoryAWS rdsInstanceRepository;
    @Autowired
    private EKSClusterRepositoryAWS eksClusterRepository;
    @Autowired
    private IAMRoleRepositoryAWS iamRoleRepository;
    @Autowired
    private SnapshotRepositoryAWS snapshotRepository;
    @Autowired
    private VPNConnectionRepositoryAWS vpnConnectionRepository;


    public String getSessionToken(String awsAccessKey, String awsSecretKey, String sessionToken) {
        StsClient stsClient = StsClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsSessionCredentials.create(awsAccessKey, awsSecretKey, sessionToken)))
                .build();

        GetSessionTokenResponse response = stsClient.getSessionToken();
        return response.credentials().sessionToken();
    }

    private List<Region> listAllRegions() {
        return Ec2Client.builder()
                .region(Region.US_EAST_1)
                .build()
                .describeRegions()
                .regions()
                .stream()
                .map(region -> Region.of(region.regionName()))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAccounts(String accessToken, String organizationId) {
        List<Map<String, Object>> accounts = new ArrayList<>();

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://organizations.amazonaws.com/v1/accounts?organization-id=" + organizationId))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode accountsNode = root.get("Accounts");

                if (accountsNode != null && accountsNode.isArray()) {
                    for (JsonNode accNode : accountsNode) {
                        Map<String, Object> account = new HashMap<>();
                        account.put("id", accNode.get("Id").asText());
                        account.put("arn", accNode.get("Arn").asText());
                        account.put("email", accNode.get("Email").asText());
                        account.put("name", accNode.get("Name").asText());
                        account.put("status", accNode.get("Status").asText());
                        accounts.add(account);
                    }
                }
            } else {
                throw new RuntimeException("Error getting accounts: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving accounts: " + e.getMessage(), e);
        }

        return accounts;
    }

    public OrganizationDataModelAWS getOrganization(Integer id) {
        return organizationRepository.findById(id).orElse(null);
    }

    public OrganizationDataModelAWS getOrganizationByName(String name) {
        return organizationRepository.findByName(name).orElse(null);
    }

    public OrganizationDataModelAWS getOrganizationByAwsOrgId(String awsOrgId) {
        return organizationRepository.findByAwsOrgId(awsOrgId).orElse(null);
    }

    @Transactional
    public void saveOrganization(Integer id, String name, String awsOrgId) {
        OrganizationDataModelAWS existingOrg = id != null ? getOrganization(id) : null;

        if (existingOrg != null) {
            existingOrg.setName(name);
            existingOrg.setAwsOrgId(awsOrgId);
            organizationRepository.save(existingOrg);
        } else {
            OrganizationDataModelAWS newOrg = new OrganizationDataModelAWS();
            if (id != null) {
                newOrg.setId(id);
            }
            newOrg.setName(name);
            newOrg.setAwsOrgId(awsOrgId);
            organizationRepository.save(newOrg);
        }
    }

    public List<OrganizationDataModelAWS> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    public AccountDataModel getAccount(String id) {
        return accountRepository.findById(id).orElse(null);
    }

    public Optional<AccountDataModel> getAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    @Transactional
    public void saveAccount(String id, String email, OrganizationDataModelAWS organization,
                            String status, String name, String region, String detailsJson) {
        AccountDataModel existingAccount = getAccount(id);

        if (existingAccount != null) {
            existingAccount.setEmail(email);
            existingAccount.setOrganization(organization);
            existingAccount.setStatus(status);
            existingAccount.setName(name);
            existingAccount.setDetails(detailsJson);
            accountRepository.save(existingAccount);
        } else {
            AccountDataModel newAccount = new AccountDataModel();
            newAccount.setId(id);
            newAccount.setEmail(email);
            newAccount.setOrganization(organization);
            newAccount.setStatus(status);
            newAccount.setName(name);
            newAccount.setDetails(detailsJson);
            accountRepository.save(newAccount);
        }
    }

    public List<AccountDataModel> getAllAccounts() {
        return accountRepository.findAll();
    }

    public List<AccountDataModel> getAccountsByOrganization(Integer organizationId) {
        return accountRepository.findByOrganization_Id(organizationId);
    }

    public List<AccountDataModel> getAccountsByStatus(String status) {
        return accountRepository.findByStatus(status);
    }

    public EC2InstanceDataModel getEC2Instance(String id) {
        return ec2InstanceRepository.findById(id).orElse(null);
    }

    public Optional<EC2InstanceDataModel> getEC2InstanceByInstanceIdAndAccount(String instanceId, String accountId) {
        return ec2InstanceRepository.findByInstanceIdAndAccount_Id(instanceId, accountId);
    }

    @Transactional
    public void saveEC2Instance(String id, String instanceId, AccountDataModel account,
                                String instanceType, String state, String availabilityZone,
                                String publicIp, String privateIp, String name, String detailsJson) {
        Optional<EC2InstanceDataModel> existingInstance = getEC2InstanceByInstanceIdAndAccount(instanceId, account.getId());

        if (existingInstance.isPresent()) {
            EC2InstanceDataModel instance = existingInstance.get();
            instance.setInstanceType(instanceType);
            instance.setState(state);
            instance.setAvailabilityZone(availabilityZone);
            instance.setPublicIp(publicIp);
            instance.setPrivateIp(privateIp);
            instance.setName(name);
            instance.setDetails(detailsJson);
            ec2InstanceRepository.save(instance);
        } else {
            EC2InstanceDataModel newInstance = new EC2InstanceDataModel();
            newInstance.setId(id);
            newInstance.setInstanceId(instanceId);
            newInstance.setAccount(account);
            newInstance.setInstanceType(instanceType);
            newInstance.setState(state);
            newInstance.setAvailabilityZone(availabilityZone);
            newInstance.setPublicIp(publicIp);
            newInstance.setPrivateIp(privateIp);
            newInstance.setName(name);
            newInstance.setDetails(detailsJson);
            ec2InstanceRepository.save(newInstance);
        }
    }

    public List<EC2InstanceDataModel> getAllEC2Instances() {
        return ec2InstanceRepository.findAll();
    }

    public List<EC2InstanceDataModel> getEC2InstancesByAccount(String accountId) {
        return ec2InstanceRepository.findByAccount_Id(accountId);
    }

    public List<EC2InstanceDataModel> getEC2InstancesByState(String state) {
        return ec2InstanceRepository.findByState(state);
    }

    public List<EC2InstanceDataModel> getEC2InstancesByAvailabilityZone(String availabilityZone) {
        return ec2InstanceRepository.findByAvailabilityZone(availabilityZone);
    }

    public List<EC2InstanceDataModel> getEC2InstancesByInstanceType(String instanceType) {
        return ec2InstanceRepository.findByInstanceType(instanceType);
    }

    public EBSVolumeDataModel getEBSVolume(String id) {
        return ebsVolumeRepository.findById(id).orElse(null);
    }

    public Optional<EBSVolumeDataModel> getEBSVolumeByVolumeIdAndAccount(String volumeId, String accountId) {
        return ebsVolumeRepository.findByVolumeIdAndAccount_Id(volumeId, accountId);
    }

    @Transactional
    public void saveEBSVolume(String id, String volumeId, AccountDataModel account,
                              String type, String state, String availabilityZone,
                              Integer sizeGb, Boolean encrypted, String name, String detailsJson) {
        Optional<EBSVolumeDataModel> existingVolume = getEBSVolumeByVolumeIdAndAccount(volumeId, account.getId());

        if (existingVolume.isPresent()) {
            EBSVolumeDataModel volume = existingVolume.get();
            volume.setType(type);
            volume.setState(state);
            volume.setAvailabilityZone(availabilityZone);
            volume.setSizeGb(sizeGb);
            volume.setEncrypted(encrypted);
            volume.setName(name);
            volume.setDetails(detailsJson);
            ebsVolumeRepository.save(volume);
        } else {
            EBSVolumeDataModel newVolume = new EBSVolumeDataModel();
            newVolume.setId(id);
            newVolume.setVolumeId(volumeId);
            newVolume.setAccount(account);
            newVolume.setType(type);
            newVolume.setState(state);
            newVolume.setAvailabilityZone(availabilityZone);
            newVolume.setSizeGb(sizeGb);
            newVolume.setEncrypted(encrypted);
            newVolume.setName(name);
            newVolume.setDetails(detailsJson);
            ebsVolumeRepository.save(newVolume);
        }
    }

    public List<EBSVolumeDataModel> getAllEBSVolumes() {
        return ebsVolumeRepository.findAll();
    }

    public List<EBSVolumeDataModel> getEBSVolumesByAccount(String accountId) {
        return ebsVolumeRepository.findByAccount_Id(accountId);
    }

    public List<EBSVolumeDataModel> getEBSVolumesByState(String state) {
        return ebsVolumeRepository.findByState(state);
    }

    public List<EBSVolumeDataModel> getEBSVolumesByType(String type) {
        return ebsVolumeRepository.findByType(type);
    }

    public List<EBSVolumeDataModel> getEBSVolumesByAvailabilityZone(String availabilityZone) {
        return ebsVolumeRepository.findByAvailabilityZone(availabilityZone);
    }

    public List<EBSVolumeDataModel> getEBSVolumesByEncryption(Boolean encrypted) {
        return ebsVolumeRepository.findByEncrypted(encrypted);
    }

    public S3BucketDataModel getS3Bucket(String id) {
        return s3BucketRepository.findById(id).orElse(null);
    }

    public Optional<S3BucketDataModel> getS3BucketByName(String name) {
        return s3BucketRepository.findByName(name);
    }

    @Transactional
    public void saveS3Bucket(String id, String name, AccountDataModel account,
                             String region, LocalDateTime creationDate, Boolean versioningEnabled,
                             Boolean publicAccessBlocked, String detailsJson) {
        Optional<S3BucketDataModel> existingBucket = getS3BucketByName(name);

        if (existingBucket.isPresent()) {
            S3BucketDataModel bucket = existingBucket.get();
            bucket.setAccount(account);
            bucket.setRegion(region);
            bucket.setCreationDate(creationDate);
            bucket.setVersioningEnabled(versioningEnabled);
            bucket.setPublicAccessBlocked(publicAccessBlocked);
            bucket.setDetails(detailsJson);
            s3BucketRepository.save(bucket);
        } else {
            S3BucketDataModel newBucket = new S3BucketDataModel();
            newBucket.setId(id);
            newBucket.setName(name);
            newBucket.setAccount(account);
            newBucket.setRegion(region);
            newBucket.setCreationDate(creationDate);
            newBucket.setVersioningEnabled(versioningEnabled);
            newBucket.setPublicAccessBlocked(publicAccessBlocked);
            newBucket.setDetails(detailsJson);
            s3BucketRepository.save(newBucket);
        }
    }

    public List<S3BucketDataModel> getAllS3Buckets() {
        return s3BucketRepository.findAll();
    }

    public List<S3BucketDataModel> getS3BucketsByAccount(String accountId) {
        return s3BucketRepository.findByAccount_Id(accountId);
    }

    public List<S3BucketDataModel> getS3BucketsByRegion(String region) {
        return s3BucketRepository.findByRegion(region);
    }

    public List<S3BucketDataModel> getS3BucketsByCreationDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return s3BucketRepository.findByCreationDateBetween(startDate, endDate);
    }

    public List<S3BucketDataModel> getS3BucketsByVersioningEnabled(Boolean versioningEnabled) {
        return s3BucketRepository.findByVersioningEnabled(versioningEnabled);
    }

    public List<S3BucketDataModel> getS3BucketsByPublicAccessBlocked(Boolean publicAccessBlocked) {
        return s3BucketRepository.findByPublicAccessBlocked(publicAccessBlocked);
    }

    public VPCDataModel getVPC(String id) {
        return vpcRepository.findById(id).orElse(null);
    }

    public Optional<VPCDataModel> getVPCByVpcIdAndAccount(String vpcId, String accountId) {
        return vpcRepository.findByVpcIdAndAccount_Id(vpcId, accountId);
    }

    @Transactional
    public void saveVPC(String id, String vpcId, AccountDataModel account,
                        String cidrBlock, Boolean isDefault, String region,
                        String name, String tenancy, String detailsJson) {
        Optional<VPCDataModel> existingVPC = getVPCByVpcIdAndAccount(vpcId, account.getId());

        if (existingVPC.isPresent()) {
            VPCDataModel vpc = existingVPC.get();
            vpc.setCidrBlock(cidrBlock);
            vpc.setIsDefault(isDefault);
            vpc.setRegion(region);
            vpc.setName(name);
            vpc.setTenancy(tenancy);
            vpc.setDetails(detailsJson);
            vpcRepository.save(vpc);
        } else {
            VPCDataModel newVPC = new VPCDataModel();
            newVPC.setId(id);
            newVPC.setVpcId(vpcId);
            newVPC.setAccount(account);
            newVPC.setCidrBlock(cidrBlock);
            newVPC.setIsDefault(isDefault);
            newVPC.setRegion(region);
            newVPC.setName(name);
            newVPC.setTenancy(tenancy);
            newVPC.setDetails(detailsJson);
            vpcRepository.save(newVPC);
        }
    }

    public List<VPCDataModel> getAllVPCs() {
        return vpcRepository.findAll();
    }

    public List<VPCDataModel> getVPCsByAccount(String accountId) {
        return vpcRepository.findByAccount_Id(accountId);
    }

    public List<VPCDataModel> getVPCsByIsDefault(Boolean isDefault) {
        return vpcRepository.findByIsDefault(isDefault);
    }

    public List<VPCDataModel> getVPCsByRegion(String region) {
        return vpcRepository.findByRegion(region);
    }

    public SubnetDataModel getSubnet(String id) {
        return subnetRepository.findById(id).orElse(null);
    }

    public Optional<SubnetDataModel> getSubnetBySubnetIdAndAccount(String subnetId, String accountId) {
        return subnetRepository.findBySubnetIdAndAccount_Id(subnetId, accountId);
    }

    @Transactional
    public void saveSubnet(String id, String subnetId, AccountDataModel account,
                           String cidrBlock, String availabilityZone, Boolean mapPublicIp,
                           VPCDataModel vpc, String name, String detailsJson) {
        Optional<SubnetDataModel> existingSubnet = getSubnetBySubnetIdAndAccount(subnetId, account.getId());

        if (existingSubnet.isPresent()) {
            SubnetDataModel subnet = existingSubnet.get();
            subnet.setCidrBlock(cidrBlock);
            subnet.setAvailabilityZone(availabilityZone);
            subnet.setMapPublicIp(mapPublicIp);
            subnet.setVpc(vpc);
            subnet.setName(name);
            subnet.setDetails(detailsJson);
            subnetRepository.save(subnet);
        } else {
            SubnetDataModel newSubnet = new SubnetDataModel();
            newSubnet.setId(id);
            newSubnet.setSubnetId(subnetId);
            newSubnet.setAccount(account);
            newSubnet.setCidrBlock(cidrBlock);
            newSubnet.setAvailabilityZone(availabilityZone);
            newSubnet.setMapPublicIp(mapPublicIp);
            newSubnet.setVpc(vpc);
            newSubnet.setName(name);
            newSubnet.setDetails(detailsJson);
            subnetRepository.save(newSubnet);
        }
    }

    public List<SubnetDataModel> getAllSubnets() {
        return subnetRepository.findAll();
    }

    public List<SubnetDataModel> getSubnetsByAccount(String accountId) {
        return subnetRepository.findByAccount_Id(accountId);
    }

    public List<SubnetDataModel> getSubnetsByVpc(String vpcId) {
        return subnetRepository.findByVpc_Id(vpcId);
    }

    public List<SubnetDataModel> getSubnetsByAvailabilityZone(String availabilityZone) {
        return subnetRepository.findByAvailabilityZone(availabilityZone);
    }

    public List<SubnetDataModel> getSubnetsByMapPublicIp(Boolean mapPublicIp) {
        return subnetRepository.findByMapPublicIp(mapPublicIp);
    }

    public SecurityGroupDataModel getSecurityGroup(String id) {
        return securityGroupRepository.findById(id).orElse(null);
    }

    public Optional<SecurityGroupDataModel> getSecurityGroupByGroupIdAndAccount(String groupId, String accountId) {
        return securityGroupRepository.findByGroupIdAndAccount_Id(groupId, accountId);
    }

    public Optional<SecurityGroupDataModel> getSecurityGroupByNameAndAccount(String name, String accountId) {
        return securityGroupRepository.findByNameAndAccount_Id(name, accountId);
    }

    @Transactional
    public void saveSecurityGroup(String id, String groupId, String name, AccountDataModel account,
                                  String description, VPCDataModel vpc, String detailsJson) {
        Optional<SecurityGroupDataModel> existingGroup = getSecurityGroupByGroupIdAndAccount(groupId, account.getId());

        if (existingGroup.isPresent()) {
            SecurityGroupDataModel group = existingGroup.get();
            group.setName(name);
            group.setDescription(description);
            group.setVpc(vpc);
            group.setDetails(detailsJson);
            securityGroupRepository.save(group);
        } else {
            SecurityGroupDataModel newGroup = new SecurityGroupDataModel();
            newGroup.setId(id);
            newGroup.setGroupId(groupId);
            newGroup.setName(name);
            newGroup.setAccount(account);
            newGroup.setDescription(description);
            newGroup.setVpc(vpc);
            newGroup.setDetails(detailsJson);
            securityGroupRepository.save(newGroup);
        }
    }

    public List<SecurityGroupDataModel> getAllSecurityGroups() {
        return securityGroupRepository.findAll();
    }

    public List<SecurityGroupDataModel> getSecurityGroupsByAccount(String accountId) {
        return securityGroupRepository.findByAccount_Id(accountId);
    }

    public List<SecurityGroupDataModel> getSecurityGroupsByVpc(String vpcId) {
        return securityGroupRepository.findByVpc_Id(vpcId);
    }

    public LoadBalancerDataModelAWS getLoadBalancer(String id) {
        return loadBalancerRepository.findById(id).orElse(null);
    }

    public Optional<LoadBalancerDataModelAWS> getLoadBalancerByNameAndAccount(String name, String accountId) {
        return loadBalancerRepository.findByNameAndAccount_Id(name, accountId);
    }

    @Transactional
    public void saveLoadBalancer(String id, String arn, String name, AccountDataModel account,
                                 String dnsName, VPCDataModel vpc, String region, String type,
                                 String scheme, String state, String detailsJson) {
        Optional<LoadBalancerDataModelAWS> existingLB = getLoadBalancerByNameAndAccount(name, account.getId());

        if (existingLB.isPresent()) {
            LoadBalancerDataModelAWS loadBalancer = existingLB.get();
            loadBalancer.setArn(arn);
            loadBalancer.setDnsName(dnsName);
            loadBalancer.setVpc(vpc);
            loadBalancer.setRegion(region);
            loadBalancer.setType(type);
            loadBalancer.setScheme(scheme);
            loadBalancer.setState(state);
            loadBalancer.setDetails(detailsJson);
            loadBalancerRepository.save(loadBalancer);
        } else {
            LoadBalancerDataModelAWS newLoadBalancer = new LoadBalancerDataModelAWS();
            newLoadBalancer.setId(id);
            newLoadBalancer.setArn(arn);
            newLoadBalancer.setName(name);
            newLoadBalancer.setAccount(account);
            newLoadBalancer.setDnsName(dnsName);
            newLoadBalancer.setVpc(vpc);
            newLoadBalancer.setRegion(region);
            newLoadBalancer.setType(type);
            newLoadBalancer.setScheme(scheme);
            newLoadBalancer.setState(state);
            newLoadBalancer.setDetails(detailsJson);
            loadBalancerRepository.save(newLoadBalancer);
        }
    }

    public List<LoadBalancerDataModelAWS> getAllLoadBalancers() {
        return loadBalancerRepository.findAll();
    }

    public List<LoadBalancerDataModelAWS> getLoadBalancersByAccount(String accountId) {
        return loadBalancerRepository.findByAccount_Id(accountId);
    }

    public List<LoadBalancerDataModelAWS> getLoadBalancersByVpc(String vpcId) {
        return loadBalancerRepository.findByVpc_Id(vpcId);
    }

    public List<LoadBalancerDataModelAWS> getLoadBalancersByRegion(String region) {
        return loadBalancerRepository.findByRegion(region);
    }

    public List<LoadBalancerDataModelAWS> getLoadBalancersByType(String type) {
        return loadBalancerRepository.findByType(type);
    }

    public List<LoadBalancerDataModelAWS> getLoadBalancersByState(String state) {
        return loadBalancerRepository.findByState(state);
    }

    public RDSInstanceDataModel getRDSInstance(String id) {
        return rdsInstanceRepository.findById(id).orElse(null);
    }

    public Optional<RDSInstanceDataModel> getRDSInstanceByIdentifierAndAccount(String dbInstanceIdentifier, String accountId) {
        return rdsInstanceRepository.findByDbInstanceIdentifierAndAccount_Id(dbInstanceIdentifier, accountId);
    }

    @Transactional
    public void saveRDSInstance(String id, String dbInstanceIdentifier, AccountDataModel account,
                                String engine, String instanceClass, VPCDataModel vpc,
                                String status, Boolean multiAz, String endpoint,
                                Integer allocatedStorage, String detailsJson) {
        Optional<RDSInstanceDataModel> existingRDS = getRDSInstanceByIdentifierAndAccount(dbInstanceIdentifier, account.getId());

        if (existingRDS.isPresent()) {
            RDSInstanceDataModel rdsInstance = existingRDS.get();
            rdsInstance.setEngine(engine);
            rdsInstance.setInstanceClass(instanceClass);
            rdsInstance.setVpc(vpc);
            rdsInstance.setStatus(status);
            rdsInstance.setMultiAz(multiAz);
            rdsInstance.setEndpoint(endpoint);
            rdsInstance.setAllocatedStorage(allocatedStorage);
            rdsInstance.setDetails(detailsJson);
            rdsInstanceRepository.save(rdsInstance);
        } else {
            RDSInstanceDataModel newRdsInstance = new RDSInstanceDataModel();
            newRdsInstance.setId(id);
            newRdsInstance.setDbInstanceIdentifier(dbInstanceIdentifier);
            newRdsInstance.setAccount(account);
            newRdsInstance.setEngine(engine);
            newRdsInstance.setInstanceClass(instanceClass);
            newRdsInstance.setVpc(vpc);
            newRdsInstance.setStatus(status);
            newRdsInstance.setMultiAz(multiAz);
            newRdsInstance.setEndpoint(endpoint);
            newRdsInstance.setAllocatedStorage(allocatedStorage);
            newRdsInstance.setDetails(detailsJson);
            rdsInstanceRepository.save(newRdsInstance);
        }
    }

    public List<RDSInstanceDataModel> getAllRDSInstances() {
        return rdsInstanceRepository.findAll();
    }

    public List<RDSInstanceDataModel> getRDSInstancesByAccount(String accountId) {
        return rdsInstanceRepository.findByAccount_Id(accountId);
    }

    public List<RDSInstanceDataModel> getRDSInstancesByEngine(String engine) {
        return rdsInstanceRepository.findByEngine(engine);
    }

    public List<RDSInstanceDataModel> getRDSInstancesByStatus(String status) {
        return rdsInstanceRepository.findByStatus(status);
    }

    public List<RDSInstanceDataModel> getRDSInstancesByInstanceClass(String instanceClass) {
        return rdsInstanceRepository.findByInstanceClass(instanceClass);
    }

    public List<RDSInstanceDataModel> getRDSInstancesByVpc(String vpcId) {
        return rdsInstanceRepository.findByVpc_Id(vpcId);
    }

    public List<RDSInstanceDataModel> getRDSInstancesByMultiAz(Boolean multiAz) {
        return rdsInstanceRepository.findByMultiAz(multiAz);
    }

    public EKSClusterDataModel getEKSCluster(String id) {
        return eksClusterRepository.findById(id).orElse(null);
    }

    public Optional<EKSClusterDataModel> getEKSClusterByNameAndAccount(String name, String accountId) {
        return eksClusterRepository.findByNameAndAccount_Id(name, accountId);
    }

    @Transactional
    public void saveEKSCluster(String id, String name, AccountDataModel account,
                               String region, String version, String status,
                               String roleArn, VPCDataModel vpc,
                               String detailsJson) {
        Optional<EKSClusterDataModel> existingCluster = getEKSClusterByNameAndAccount(name, account.getId());

        if (existingCluster.isPresent()) {
            EKSClusterDataModel cluster = existingCluster.get();
            cluster.setRegion(region);
            cluster.setVersion(version);
            cluster.setStatus(status);
            cluster.setRoleArn(roleArn);
            cluster.setVpc(vpc);
            cluster.setDetails(detailsJson);
            eksClusterRepository.save(cluster);
        } else {
            EKSClusterDataModel newCluster = new EKSClusterDataModel();
            newCluster.setId(id);
            newCluster.setName(name);
            newCluster.setAccount(account);
            newCluster.setRegion(region);
            newCluster.setVersion(version);
            newCluster.setStatus(status);
            newCluster.setRoleArn(roleArn);
            newCluster.setVpc(vpc);
            newCluster.setDetails(detailsJson);
            eksClusterRepository.save(newCluster);
        }
    }

    public List<EKSClusterDataModel> getAllEKSClusters() {
        return eksClusterRepository.findAll();
    }

    public List<EKSClusterDataModel> getEKSClustersByAccount(String accountId) {
        return eksClusterRepository.findByAccount_Id(accountId);
    }

    public List<EKSClusterDataModel> getEKSClustersByRegion(String region) {
        return eksClusterRepository.findByRegion(region);
    }

    public List<EKSClusterDataModel> getEKSClustersByStatus(String status) {
        return eksClusterRepository.findByStatus(status);
    }

    public List<EKSClusterDataModel> getEKSClustersByVersion(String version) {
        return eksClusterRepository.findByVersion(version);
    }

    public List<EKSClusterDataModel> getEKSClustersByVpc(String vpcId) {
        return eksClusterRepository.findByVpc_Id(vpcId);
    }

    public IAMRoleDataModel getIAMRole(String id) {
        return iamRoleRepository.findById(id).orElse(null);
    }

    public Optional<IAMRoleDataModel> getIAMRoleByArnAndAccount(String arn, String accountId) {
        return iamRoleRepository.findByArnAndAccount_Id(arn, accountId);
    }

    public Optional<IAMRoleDataModel> getIAMRoleByNameAndAccount(String name, String accountId) {
        return iamRoleRepository.findByNameAndAccount_Id(name, accountId);
    }

    @Transactional
    public void saveIAMRole(String id, String arn, String name, AccountDataModel account,
                            String path, LocalDateTime createDate, String description,
                            String detailsJson) {
        Optional<IAMRoleDataModel> existingRole = getIAMRoleByArnAndAccount(arn, account.getId());

        if (existingRole.isPresent()) {
            IAMRoleDataModel role = existingRole.get();
            role.setName(name);
            role.setPath(path);
            role.setCreateDate(createDate);
            role.setDescription(description);
            role.setDetails(detailsJson);
            iamRoleRepository.save(role);
        } else {
            IAMRoleDataModel newRole = new IAMRoleDataModel();
            newRole.setId(id);
            newRole.setArn(arn);
            newRole.setName(name);
            newRole.setAccount(account);
            newRole.setPath(path);
            newRole.setCreateDate(createDate);
            newRole.setDescription(description);
            newRole.setDetails(detailsJson);
            iamRoleRepository.save(newRole);
        }
    }

    public List<IAMRoleDataModel> getAllIAMRoles() {
        return iamRoleRepository.findAll();
    }

    public List<IAMRoleDataModel> getIAMRolesByAccount(String accountId) {
        return iamRoleRepository.findByAccount_Id(accountId);
    }

    public List<IAMRoleDataModel> getIAMRolesByPath(String path) {
        return iamRoleRepository.findByPath(path);
    }

    public List<IAMRoleDataModel> getIAMRolesByCreateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return iamRoleRepository.findByCreateDateBetween(startDate, endDate);
    }

    public SnapshotDataModelAWS getSnapshot(String id) {
        return snapshotRepository.findById(id).orElse(null);
    }

    public Optional<SnapshotDataModelAWS> getSnapshotBySnapshotIdAndAccount(String snapshotId, String accountId) {
        return snapshotRepository.findBySnapshotIdAndAccount_Id(snapshotId, accountId);
    }

    @Transactional
    public void saveSnapshot(String id, String snapshotId, AccountDataModel account,
                             String volumeId, Integer volumeSize, String state,
                             Boolean encrypted, String name, String description,
                             String detailsJson) {
        Optional<SnapshotDataModelAWS> existingSnapshot = getSnapshotBySnapshotIdAndAccount(snapshotId, account.getId());

        if (existingSnapshot.isPresent()) {
            SnapshotDataModelAWS snapshot = existingSnapshot.get();
            snapshot.setVolumeId(volumeId);
            snapshot.setVolumeSize(volumeSize);
            snapshot.setState(state);
            snapshot.setEncrypted(encrypted);
            snapshot.setName(name);
            snapshot.setDescription(description);
            snapshot.setDetails(detailsJson);
            snapshotRepository.save(snapshot);
        } else {
            SnapshotDataModelAWS newSnapshot = new SnapshotDataModelAWS();
            newSnapshot.setId(id);
            newSnapshot.setSnapshotId(snapshotId);
            newSnapshot.setAccount(account);
            newSnapshot.setVolumeId(volumeId);
            newSnapshot.setVolumeSize(volumeSize);
            newSnapshot.setState(state);
            newSnapshot.setEncrypted(encrypted);
            newSnapshot.setName(name);
            newSnapshot.setDescription(description);
            newSnapshot.setDetails(detailsJson);
            snapshotRepository.save(newSnapshot);
        }
    }

    public List<SnapshotDataModelAWS> getAllSnapshots() {
        return snapshotRepository.findAll();
    }

    public List<SnapshotDataModelAWS> getSnapshotsByAccount(String accountId) {
        return snapshotRepository.findByAccount_Id(accountId);
    }

    public List<SnapshotDataModelAWS> getSnapshotsByVolumeId(String volumeId) {
        return snapshotRepository.findByVolumeId(volumeId);
    }

    public List<SnapshotDataModelAWS> getSnapshotsByState(String state) {
        return snapshotRepository.findByState(state);
    }

    public List<SnapshotDataModelAWS> getSnapshotsByEncryption(Boolean encrypted) {
        return snapshotRepository.findByEncrypted(encrypted);
    }

    public VPNConnectionDataModel getVPNConnection(String id) {
        return vpnConnectionRepository.findById(id).orElse(null);
    }

    public Optional<VPNConnectionDataModel> getVPNConnectionByVpnConnectionIdAndAccount(String vpnConnectionId, String accountId) {
        return vpnConnectionRepository.findByVpnConnectionIdAndAccount_Id(vpnConnectionId, accountId);
    }

    @Transactional
    public void saveVPNConnection(String id, String vpnConnectionId, AccountDataModel account,
                                  VPCDataModel vpc, String state, String customerGatewayId,
                                  String vpnGatewayId, String type, String name,
                                  String detailsJson) {
        Optional<VPNConnectionDataModel> existingConnection = getVPNConnectionByVpnConnectionIdAndAccount(vpnConnectionId, account.getId());

        if (existingConnection.isPresent()) {
            VPNConnectionDataModel vpnConnection = existingConnection.get();
            vpnConnection.setVpc(vpc);
            vpnConnection.setState(state);
            vpnConnection.setCustomerGatewayId(customerGatewayId);
            vpnConnection.setVpnGatewayId(vpnGatewayId);
            vpnConnection.setType(type);
            vpnConnection.setName(name);
            vpnConnection.setDetails(detailsJson);
            vpnConnectionRepository.save(vpnConnection);
        } else {
            VPNConnectionDataModel newConnection = new VPNConnectionDataModel();
            newConnection.setId(id);
            newConnection.setVpnConnectionId(vpnConnectionId);
            newConnection.setAccount(account);
            newConnection.setVpc(vpc);
            newConnection.setState(state);
            newConnection.setCustomerGatewayId(customerGatewayId);
            newConnection.setVpnGatewayId(vpnGatewayId);
            newConnection.setType(type);
            newConnection.setName(name);
            newConnection.setDetails(detailsJson);
            vpnConnectionRepository.save(newConnection);
        }
    }

    public List<VPNConnectionDataModel> getAllVPNConnections() {
        return vpnConnectionRepository.findAll();
    }

    public List<VPNConnectionDataModel> getVPNConnectionsByAccount(String accountId) {
        return vpnConnectionRepository.findByAccount_Id(accountId);
    }

    public List<VPNConnectionDataModel> getVPNConnectionsByVpc(String vpcId) {
        return vpnConnectionRepository.findByVpc_Id(vpcId);
    }

    public List<VPNConnectionDataModel> getVPNConnectionsByState(String state) {
        return vpnConnectionRepository.findByState(state);
    }

    public List<VPNConnectionDataModel> getVPNConnectionsByCustomerGatewayId(String customerGatewayId) {
        return vpnConnectionRepository.findByCustomerGatewayId(customerGatewayId);
    }

    public List<VPNConnectionDataModel> getVPNConnectionsByVpnGatewayId(String vpnGatewayId) {
        return vpnConnectionRepository.findByVpnGatewayId(vpnGatewayId);
    }
}

