package com.gestion_backend.repository_cloud.repository_google;

import com.gestion_backend.dataModels.modelsGoogle.NetworkDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NetworkRepository extends JpaRepository<NetworkDataModel, String> {
    NetworkDataModel findByNameAndProject_Id(String name, String projectId);
    List<NetworkDataModel> findByProject_Id(String projectId);
}