package com.gestion_backend.repository_cloud.repository_google;

import com.gestion_backend.dataModels.modelsGoogle.FirewallDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FirewallRepository extends JpaRepository<FirewallDataModel, String> {

    FirewallDataModel findByNameAndProject_Id(String name, String projectId);

    List<FirewallDataModel> findByProject_Id(String projectId);
}

