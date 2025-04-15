package com.gestion_backend.repository_cloud.repository_azure;

import com.gestion_backend.dataModels.modelsAzure.NetworkSecurityGroupDataModelAzure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NetworkSecurityGroupRepositoryAzure extends JpaRepository<NetworkSecurityGroupDataModelAzure, String> {
    
    NetworkSecurityGroupDataModelAzure findByNameAndSubscription_Id(String name, String subscriptionId);
    
    List<NetworkSecurityGroupDataModelAzure> findBySubscription_Id(String subscriptionId);
    
    List<NetworkSecurityGroupDataModelAzure> findByResourceGroup_Id(String resourceGroupId);
}

