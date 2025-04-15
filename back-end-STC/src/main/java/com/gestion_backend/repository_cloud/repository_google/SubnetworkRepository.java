package com.gestion_backend.repository_cloud.repository_google;

import com.gestion_backend.dataModels.modelsGoogle.NetworkDataModel;
import com.gestion_backend.dataModels.modelsGoogle.SubnetworkDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubnetworkRepository extends JpaRepository<SubnetworkDataModel, String> {
    SubnetworkDataModel findByNameAndNetwork_IdAndProject_Id(String name, String networkId, String projectId);
    List<SubnetworkDataModel> findByProject_Id(String projectId);
    List<SubnetworkDataModel> findByNetwork(NetworkDataModel network);
}