package com.gestion_backend.dataModels.modelsAWS;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "vpcs", schema = "aws_cloud")
@Data
public class VPCDataModel {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false)
    private String vpcId;

    private String name;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private AccountDataModel account;

    private String cidrBlock;
    private Boolean isDefault;
    private String region;
    private String tenancy;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String details;
}