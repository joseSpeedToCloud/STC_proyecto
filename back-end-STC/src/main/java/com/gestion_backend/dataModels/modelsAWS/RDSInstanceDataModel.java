package com.gestion_backend.dataModels.modelsAWS;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "rds_instances", schema = "aws_cloud")
@Data
public class RDSInstanceDataModel {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false)
    private String dbInstanceIdentifier;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private AccountDataModel account;

    private String engine;
    private String status;
    private String instanceClass;
    private Integer allocatedStorage;

    @ManyToOne
    @JoinColumn(name = "vpc_id", referencedColumnName = "id")
    private VPCDataModel vpc;

    private String endpoint;
    private Boolean multiAz;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String details;
}