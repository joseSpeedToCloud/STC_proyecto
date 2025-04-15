package com.gestion_backend.repository_cloud.repository_aws;

import com.gestion_backend.dataModels.modelsAWS.RDSInstanceDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RDSInstanceRepositoryAWS extends JpaRepository<RDSInstanceDataModel, String> {
    
    Optional<RDSInstanceDataModel> findByDbInstanceIdentifierAndAccount_Id(String dbInstanceIdentifier, String accountId);
    
    List<RDSInstanceDataModel> findByAccount_Id(String accountId);
    
    List<RDSInstanceDataModel> findByEngine(String engine);
    
    List<RDSInstanceDataModel> findByStatus(String status);
    
    List<RDSInstanceDataModel> findByInstanceClass(String instanceClass);
    
    List<RDSInstanceDataModel> findByVpc_Id(String vpcId);
    
    List<RDSInstanceDataModel> findByMultiAz(Boolean multiAz);
}

