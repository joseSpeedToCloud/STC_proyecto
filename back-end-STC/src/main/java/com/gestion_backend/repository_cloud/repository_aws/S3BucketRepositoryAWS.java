package com.gestion_backend.repository_cloud.repository_aws;

import com.gestion_backend.dataModels.modelsAWS.S3BucketDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface S3BucketRepositoryAWS extends JpaRepository<S3BucketDataModel, String> {
    
    Optional<S3BucketDataModel> findByName(String name);
    
    List<S3BucketDataModel> findByAccount_Id(String accountId);
    
    List<S3BucketDataModel> findByRegion(String region);
    
    List<S3BucketDataModel> findByCreationDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<S3BucketDataModel> findByVersioningEnabled(Boolean versioningEnabled);
    
    List<S3BucketDataModel> findByPublicAccessBlocked(Boolean publicAccessBlocked);
}

