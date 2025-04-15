package com.gestion_backend.repository_cloud.repository_azure;

import com.gestion_backend.dataModels.modelsAzure.VirtualNetworkDataModelAzure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VirtualNetworkRepositoryAzure extends JpaRepository<VirtualNetworkDataModelAzure, String> {
    
    VirtualNetworkDataModelAzure findByNameAndSubscription_Id(String name, String subscriptionId);
    
    List<VirtualNetworkDataModelAzure> findBySubscription_Id(String subscriptionId);
    
    List<VirtualNetworkDataModelAzure> findByResourceGroup_Id(String resourceGroupId);
}

