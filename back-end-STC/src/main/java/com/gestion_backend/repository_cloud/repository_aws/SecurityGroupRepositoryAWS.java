package com.gestion_backend.repository_cloud.repository_aws;

import com.gestion_backend.dataModels.modelsAWS.SecurityGroupDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityGroupRepositoryAWS extends JpaRepository<SecurityGroupDataModel, String> {
    
    Optional<SecurityGroupDataModel> findByGroupIdAndAccount_Id(String groupId, String accountId);
    
    Optional<SecurityGroupDataModel> findByNameAndAccount_Id(String name, String accountId);
    
    List<SecurityGroupDataModel> findByAccount_Id(String accountId);
    
    List<SecurityGroupDataModel> findByVpc_Id(String vpcId);
}

