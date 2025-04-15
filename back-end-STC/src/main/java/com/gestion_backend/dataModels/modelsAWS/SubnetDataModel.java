package com.gestion_backend.dataModels.modelsAWS;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "subnets", schema = "aws_cloud")
@Data
public class SubnetDataModel {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false)
    private String subnetId;

    private String name;

    @ManyToOne
    @JoinColumn(name = "vpc_id", referencedColumnName = "id")
    private VPCDataModel vpc;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private AccountDataModel account;

    private String cidrBlock;
    private String availabilityZone;
    private Boolean mapPublicIp;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String details;
}