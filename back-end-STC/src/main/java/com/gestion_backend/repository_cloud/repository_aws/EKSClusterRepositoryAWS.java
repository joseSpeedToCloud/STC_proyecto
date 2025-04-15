package com.gestion_backend.repository_cloud.repository_aws;

import com.gestion_backend.dataModels.modelsAWS.EKSClusterDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EKSClusterRepositoryAWS extends JpaRepository<EKSClusterDataModel, String> {
    
    Optional<EKSClusterDataModel> findByNameAndAccount_Id(String name, String accountId);
    
    List<EKSClusterDataModel> findByAccount_Id(String accountId);
    
    List<EKSClusterDataModel> findByRegion(String region);
    
    List<EKSClusterDataModel> findByStatus(String status);
    
    List<EKSClusterDataModel> findByVersion(String version);
    
    List<EKSClusterDataModel> findByVpc_Id(String vpcId);
}

