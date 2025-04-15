package com.gestion_backend.repository_cloud.repository_azure;

import com.gestion_backend.dataModels.modelsAzure.DiskDataModelAzure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiskRepositoryAzure extends JpaRepository<DiskDataModelAzure, String> {
    
    DiskDataModelAzure findByNameAndSubscription_Id(String name, String subscriptionId);
    
    List<DiskDataModelAzure> findBySubscription_Id(String subscriptionId);
    
    List<DiskDataModelAzure> findByResourceGroup_Id(String resourceGroupId);
}

