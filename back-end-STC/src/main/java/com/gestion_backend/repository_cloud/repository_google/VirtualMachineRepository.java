package com.gestion_backend.repository_cloud.repository_google;


import com.gestion_backend.dataModels.modelsGoogle.VirtualMachineDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VirtualMachineRepository extends JpaRepository<VirtualMachineDataModel, String> {

    VirtualMachineDataModel findByNameAndProject_Id(String name, String projectId);

    List<VirtualMachineDataModel> findByProject_Id(String projectId);
}
