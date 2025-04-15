package com.gestion_backend.dataModels.modelsAWS;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "organizations", schema = "aws_cloud")
@Data
public class OrganizationDataModelAWS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(unique = true)
    private String awsOrgId;
}