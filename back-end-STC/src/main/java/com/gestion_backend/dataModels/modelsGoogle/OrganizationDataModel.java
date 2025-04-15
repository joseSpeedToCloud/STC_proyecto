package com.gestion_backend.dataModels.modelsGoogle;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Table(name = "organizations", schema = "google_cloud")
@Data
public class OrganizationDataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "organization")
    private Set<ProjectDataModel> projectList;
}

