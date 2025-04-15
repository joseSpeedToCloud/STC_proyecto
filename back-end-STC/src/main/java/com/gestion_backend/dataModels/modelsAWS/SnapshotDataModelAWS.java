package com.gestion_backend.dataModels.modelsAWS;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "snapshots", schema = "aws_cloud")
@Data
public class SnapshotDataModelAWS {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false)
    private String snapshotId;

    private String name;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private AccountDataModel account;

    private String volumeId;
    private Integer volumeSize;
    private String state;
    private Boolean encrypted;
    
    @Column(length = 500)
    private String description;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String details;
}