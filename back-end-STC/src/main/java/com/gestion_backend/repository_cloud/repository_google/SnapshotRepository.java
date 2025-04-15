package com.gestion_backend.repository_cloud.repository_google;

import com.gestion_backend.dataModels.modelsGoogle.SnapshotDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SnapshotRepository extends JpaRepository<SnapshotDataModel, String> {

    SnapshotDataModel findByNameAndProject_Id(String name, String projectId);

    List<SnapshotDataModel> findByProject_Id(String projectId);
}


