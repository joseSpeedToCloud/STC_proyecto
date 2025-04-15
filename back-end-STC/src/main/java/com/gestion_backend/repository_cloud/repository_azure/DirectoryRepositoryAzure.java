package com.gestion_backend.repository_cloud.repository_azure;

import com.gestion_backend.dataModels.modelsAzure.DirectoryDataModelAzure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DirectoryRepositoryAzure extends JpaRepository<DirectoryDataModelAzure, Integer> {
    
    DirectoryDataModelAzure findByName(String name);
}

