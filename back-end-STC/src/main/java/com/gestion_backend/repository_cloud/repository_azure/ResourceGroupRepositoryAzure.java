package com.gestion_backend.repository_cloud.repository_azure;

import com.gestion_backend.dataModels.modelsAzure.ResourceGroupDataModelAzure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceGroupRepositoryAzure extends JpaRepository<ResourceGroupDataModelAzure, String> {
    
    ResourceGroupDataModelAzure findByNameAndSubscription_Id(String name, String subscriptionId);
    
    List<ResourceGroupDataModelAzure> findBySubscription_Id(String subscriptionId);
}

