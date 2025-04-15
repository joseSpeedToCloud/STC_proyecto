package com.gestion_backend.repository_cloud.repository_google;

import com.gestion_backend.dataModels.modelsGoogle.ProjectDataModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<ProjectDataModel, String> {

    Optional<ProjectDataModel> findById(String projectId);

    Optional<ProjectDataModel> findByName(String name);

    Optional<ProjectDataModel> findByDisplayName(String displayName);

}
