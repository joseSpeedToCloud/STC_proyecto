package com.gestion_backend.repository_cloud.repository_aws;

import com.gestion_backend.dataModels.modelsAWS.SnapshotDataModelAWS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SnapshotRepositoryAWS extends JpaRepository<SnapshotDataModelAWS, String> {
    
    Optional<SnapshotDataModelAWS> findBySnapshotIdAndAccount_Id(String snapshotId, String accountId);
    
    List<SnapshotDataModelAWS> findByAccount_Id(String accountId);
    
    List<SnapshotDataModelAWS> findByVolumeId(String volumeId);
    
    List<SnapshotDataModelAWS> findByState(String state);
    
    List<SnapshotDataModelAWS> findByEncrypted(Boolean encrypted);
}

