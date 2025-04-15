package com.gestion_backend.dataModels.modelsAWS;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "security_groups", schema = "aws_cloud")
@Data
public class SecurityGroupDataModel {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false)
    private String groupId;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private AccountDataModel account;

    @ManyToOne
    @JoinColumn(name = "vpc_id", referencedColumnName = "id")
    private VPCDataModel vpc;

    @Column(length = 500)
    private String description;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String details;
}