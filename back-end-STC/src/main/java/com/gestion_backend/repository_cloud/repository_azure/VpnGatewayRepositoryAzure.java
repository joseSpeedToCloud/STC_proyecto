package com.gestion_backend.repository_cloud.repository_azure;

import com.gestion_backend.dataModels.modelsAzure.VpnGatewayDataModelAzure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VpnGatewayRepositoryAzure extends JpaRepository<VpnGatewayDataModelAzure, String> {
    
    VpnGatewayDataModelAzure findByNameAndSubscription_Id(String name, String subscriptionId);
    
    List<VpnGatewayDataModelAzure> findBySubscription_Id(String subscriptionId);
    
    List<VpnGatewayDataModelAzure> findByResourceGroup_Id(String resourceGroupId);
}

