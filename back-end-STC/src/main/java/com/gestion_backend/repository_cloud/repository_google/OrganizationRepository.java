package com.gestion_backend.repository_cloud.repository_google;

import com.gestion_backend.dataModels.modelsGoogle.OrganizationDataModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<OrganizationDataModel, Long> {
    OrganizationDataModel findByName(String name);
}