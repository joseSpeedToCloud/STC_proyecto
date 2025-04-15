package com.gestion_backend.repository_cloud.repository_aws;

import com.gestion_backend.dataModels.modelsAWS.LoadBalancerDataModelAWS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoadBalancerRepositoryAWS extends JpaRepository<LoadBalancerDataModelAWS, String> {
    
    Optional<LoadBalancerDataModelAWS> findByNameAndAccount_Id(String name, String accountId);
    
    List<LoadBalancerDataModelAWS> findByAccount_Id(String accountId);
    
    List<LoadBalancerDataModelAWS> findByVpc_Id(String vpcId);
    
    List<LoadBalancerDataModelAWS> findByRegion(String region);
    
    List<LoadBalancerDataModelAWS> findByType(String type);
    
    List<LoadBalancerDataModelAWS> findByState(String state);
}

