package com.gestion_backend.repository_cloud.repository_azure;

import com.gestion_backend.dataModels.modelsAzure.SnapshotDataModelAzure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SnapshotRepositoryAzure extends JpaRepository<SnapshotDataModelAzure, String> {
    
    SnapshotDataModelAzure findByNameAndSubscription_Id(String name, String subscriptionId);
    
    List<SnapshotDataModelAzure> findBySubscription_Id(String subscriptionId);
    
    List<SnapshotDataModelAzure> findByResourceGroup_Id(String resourceGroupId);
}

