package com.gestion_backend.repository_cloud.repository_azure;

import com.gestion_backend.dataModels.modelsAzure.AksClusterDataModelAzure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AksClusterRepositoryAzure extends JpaRepository<AksClusterDataModelAzure, String> {
    
    AksClusterDataModelAzure findByNameAndSubscription_Id(String name, String subscriptionId);
    
    List<AksClusterDataModelAzure> findBySubscription_Id(String subscriptionId);
    
    List<AksClusterDataModelAzure> findByResourceGroup_Id(String resourceGroupId);
}

