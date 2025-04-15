package com.gestion_backend.repository_cloud.repository_azure;

import com.gestion_backend.dataModels.modelsAzure.SubscriptionDataModelAzure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepositoryAzure extends JpaRepository<SubscriptionDataModelAzure, String> {
    
    SubscriptionDataModelAzure findByName(String name);
    
    List<SubscriptionDataModelAzure> findByDirectory_Id(Integer directoryId);
}


