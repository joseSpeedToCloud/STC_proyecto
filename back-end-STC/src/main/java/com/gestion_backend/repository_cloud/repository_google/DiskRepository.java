package com.gestion_backend.repository_cloud.repository_google;

import com.gestion_backend.dataModels.modelsGoogle.DiskDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiskRepository extends JpaRepository<DiskDataModel, String> {

    DiskDataModel findByNameAndProject_Id(String name, String projectId);

    List<DiskDataModel> findByProject_Id(String projectId);
}




