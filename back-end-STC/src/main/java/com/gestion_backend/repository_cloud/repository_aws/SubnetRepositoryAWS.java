package com.gestion_backend.repository_cloud.repository_aws;

import com.gestion_backend.dataModels.modelsAWS.SubnetDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubnetRepositoryAWS extends JpaRepository<SubnetDataModel, String> {
    
    Optional<SubnetDataModel> findBySubnetIdAndAccount_Id(String subnetId, String accountId);
    
    List<SubnetDataModel> findByAccount_Id(String accountId);
    
    List<SubnetDataModel> findByVpc_Id(String vpcId);
    
    List<SubnetDataModel> findByAvailabilityZone(String availabilityZone);
    
    List<SubnetDataModel> findByMapPublicIp(Boolean mapPublicIp);
}

