package com.gestion_backend.dataModels.modelsAWS;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "ec2_instances", schema = "aws_cloud")
@Data
public class EC2InstanceDataModel {

    @Id
    @Column(length = 255)
    private String id;

    private String name;

    @Column(nullable = false)
    private String instanceId;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private AccountDataModel account;

    private String instanceType;
    private String availabilityZone;
    private String state;
    private String publicIp;
    private String privateIp;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String details;
}