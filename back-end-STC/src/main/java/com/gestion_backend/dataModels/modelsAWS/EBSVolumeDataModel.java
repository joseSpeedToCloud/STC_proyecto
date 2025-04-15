package com.gestion_backend.dataModels.modelsAWS;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "ebs_volumes", schema = "aws_cloud")
@Data
public class EBSVolumeDataModel {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false)
    private String volumeId;

    private String name;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private AccountDataModel account;

    private String type;
    private Integer sizeGb;
    private String state;
    private String availabilityZone;
    private Boolean encrypted;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String details;
}