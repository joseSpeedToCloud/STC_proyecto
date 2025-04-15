package com.gestion_backend.repository_cloud.repository_aws;

import com.gestion_backend.dataModels.modelsAWS.AccountDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepositoryAWS extends JpaRepository<AccountDataModel, String> {
    
    Optional<AccountDataModel> findByEmail(String email);
    
    List<AccountDataModel> findByOrganization_Id(Integer organizationId);
    
    List<AccountDataModel> findByStatus(String status);
}

