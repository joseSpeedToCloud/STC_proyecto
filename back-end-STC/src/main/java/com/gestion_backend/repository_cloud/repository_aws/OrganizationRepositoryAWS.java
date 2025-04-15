package com.gestion_backend.repository_cloud.repository_aws;

import com.gestion_backend.dataModels.modelsAWS.OrganizationDataModelAWS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepositoryAWS extends JpaRepository<OrganizationDataModelAWS, Integer> {
    
    Optional<OrganizationDataModelAWS> findByName(String name);
    
    Optional<OrganizationDataModelAWS> findByAwsOrgId(String awsOrgId);
}

