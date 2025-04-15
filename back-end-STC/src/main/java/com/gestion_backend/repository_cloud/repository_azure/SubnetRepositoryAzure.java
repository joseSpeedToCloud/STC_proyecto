package com.gestion_backend.repository_cloud.repository_azure;

import com.gestion_backend.dataModels.modelsAzure.SubnetDataModelAzure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubnetRepositoryAzure extends JpaRepository<SubnetDataModelAzure, String> {
    
    SubnetDataModelAzure findByNameAndSubscription_Id(String name, String subscriptionId);
    
    List<SubnetDataModelAzure> findByVirtualNetwork_Id(String virtualNetworkId);
    
    List<SubnetDataModelAzure> findBySubscription_Id(String subscriptionId);
}

