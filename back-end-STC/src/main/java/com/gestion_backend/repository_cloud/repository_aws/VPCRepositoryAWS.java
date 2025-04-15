package com.gestion_backend.repository_cloud.repository_aws;

import com.gestion_backend.dataModels.modelsAWS.VPCDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VPCRepositoryAWS extends JpaRepository<VPCDataModel, String> {
    
    Optional<VPCDataModel> findByVpcIdAndAccount_Id(String vpcId, String accountId);
    
    List<VPCDataModel> findByAccount_Id(String accountId);
    
    List<VPCDataModel> findByIsDefault(Boolean isDefault);
    
    List<VPCDataModel> findByRegion(String region);
}

