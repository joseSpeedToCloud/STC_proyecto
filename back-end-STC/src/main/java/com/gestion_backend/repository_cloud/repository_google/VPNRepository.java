package com.gestion_backend.repository_cloud.repository_google;

import com.gestion_backend.dataModels.modelsGoogle.VPNDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VPNRepository extends JpaRepository<VPNDataModel, String> {

    VPNDataModel findByNameAndProject_Id(String name, String projectId);

    List<VPNDataModel> findByProject_Id(String projectId);
}

