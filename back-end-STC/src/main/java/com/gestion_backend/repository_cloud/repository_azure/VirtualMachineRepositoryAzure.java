package com.gestion_backend.repository_cloud.repository_azure;

import com.gestion_backend.dataModels.modelsAzure.VirtualMachineDataModelAzure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VirtualMachineRepositoryAzure extends JpaRepository<VirtualMachineDataModelAzure, String> {
    
    VirtualMachineDataModelAzure findByNameAndSubscription_Id(String name, String subscriptionId);
    
    List<VirtualMachineDataModelAzure> findBySubscription_Id(String subscriptionId);
    
    List<VirtualMachineDataModelAzure> findByResourceGroup_Id(String resourceGroupId);
}

