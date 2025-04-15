package com.gestion_backend.repository_cloud.repository_azure;

import com.gestion_backend.dataModels.modelsAzure.LoadBalancerDataModelAzure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoadBalancerRepositoryAzure extends JpaRepository<LoadBalancerDataModelAzure, String> {
    
    LoadBalancerDataModelAzure findByNameAndSubscription_Id(String name, String subscriptionId);
    
    List<LoadBalancerDataModelAzure> findBySubscription_Id(String subscriptionId);
    
    List<LoadBalancerDataModelAzure> findByResourceGroup_Id(String resourceGroupId);
}

