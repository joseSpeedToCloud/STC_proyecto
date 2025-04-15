package com.gestion_backend.dataModels.modelsAWS;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

import java.util.List;

@Entity
@Table(name = "accounts", schema = "aws_cloud")
@Data
public class AccountDataModel {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;

    @ManyToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    private OrganizationDataModelAWS organization;

    private String status;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String details;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private List<EC2InstanceDataModel> ec2Instances;
}

