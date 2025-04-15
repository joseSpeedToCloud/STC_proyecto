package com.gestion_backend.dataModels.modelsAWS;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "load_balancers", schema = "aws_cloud")
@Data
public class LoadBalancerDataModelAWS {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false)
    private String arn;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private AccountDataModel account;

    private String region;
    private String type;
    private String scheme;

    @ManyToOne
    @JoinColumn(name = "vpc_id", referencedColumnName = "id")
    private VPCDataModel vpc;

    private String dnsName;
    private String state;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String details;
}