package com.gestion_backend.repository_cloud.repository_aws;

import com.gestion_backend.dataModels.modelsAWS.EC2InstanceDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EC2InstanceRepositoryAWS extends JpaRepository<EC2InstanceDataModel, String> {
    
    Optional<EC2InstanceDataModel> findByInstanceIdAndAccount_Id(String instanceId, String accountId);
    
    List<EC2InstanceDataModel> findByAccount_Id(String accountId);
    
    List<EC2InstanceDataModel> findByState(String state);
    
    List<EC2InstanceDataModel> findByAvailabilityZone(String availabilityZone);
    
    List<EC2InstanceDataModel> findByInstanceType(String instanceType);
}

