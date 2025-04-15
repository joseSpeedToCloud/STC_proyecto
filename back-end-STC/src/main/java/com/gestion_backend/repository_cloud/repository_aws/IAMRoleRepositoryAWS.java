package com.gestion_backend.repository_cloud.repository_aws;

import com.gestion_backend.dataModels.modelsAWS.IAMRoleDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface IAMRoleRepositoryAWS extends JpaRepository<IAMRoleDataModel, String> {
    
    Optional<IAMRoleDataModel> findByArnAndAccount_Id(String arn, String accountId);
    
    Optional<IAMRoleDataModel> findByNameAndAccount_Id(String name, String accountId);
    
    List<IAMRoleDataModel> findByAccount_Id(String accountId);
    
    List<IAMRoleDataModel> findByPath(String path);
    
    List<IAMRoleDataModel> findByCreateDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}

