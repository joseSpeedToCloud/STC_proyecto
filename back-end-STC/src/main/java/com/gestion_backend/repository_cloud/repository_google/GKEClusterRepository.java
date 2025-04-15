package com.gestion_backend.repository_cloud.repository_google;

import com.gestion_backend.dataModels.modelsGoogle.GKEClusterDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GKEClusterRepository extends JpaRepository<GKEClusterDataModel, String> {

    GKEClusterDataModel findByNameAndProject_Id(String name, String projectId);

    List<GKEClusterDataModel> findByProject_Id(String projectId);

}