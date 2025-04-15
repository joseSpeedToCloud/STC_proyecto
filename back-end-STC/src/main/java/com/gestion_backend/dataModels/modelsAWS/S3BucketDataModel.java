package com.gestion_backend.dataModels.modelsAWS;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;
import java.time.LocalDateTime;

@Entity
@Table(name = "s3_buckets", schema = "aws_cloud")
@Data
public class S3BucketDataModel {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private AccountDataModel account;

    private String region;
    private LocalDateTime creationDate;
    private Boolean versioningEnabled;
    private Boolean publicAccessBlocked;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String details;
}