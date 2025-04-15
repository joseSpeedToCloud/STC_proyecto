package com.gestion_backend.repository_cloud.repository_aws;

import com.gestion_backend.dataModels.modelsAWS.EBSVolumeDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EBSVolumeRepositoryAWS extends JpaRepository<EBSVolumeDataModel, String> {
    
    Optional<EBSVolumeDataModel> findByVolumeIdAndAccount_Id(String volumeId, String accountId);
    
    List<EBSVolumeDataModel> findByAccount_Id(String accountId);
    
    List<EBSVolumeDataModel> findByState(String state);
    
    List<EBSVolumeDataModel> findByType(String type);
    
    List<EBSVolumeDataModel> findByAvailabilityZone(String availabilityZone);
    
    List<EBSVolumeDataModel> findByEncrypted(Boolean encrypted);
}

