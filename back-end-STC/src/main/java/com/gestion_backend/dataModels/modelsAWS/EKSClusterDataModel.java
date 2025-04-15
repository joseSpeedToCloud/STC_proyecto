package com.gestion_backend.dataModels.modelsAWS;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "eks_clusters", schema = "aws_cloud")
@Data
public class EKSClusterDataModel {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private AccountDataModel account;

    private String region;
    private String status;
    private String version;
    private String roleArn;

    @ManyToOne
    @JoinColumn(name = "vpc_id", referencedColumnName = "id")
    private VPCDataModel vpc;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String details;
}