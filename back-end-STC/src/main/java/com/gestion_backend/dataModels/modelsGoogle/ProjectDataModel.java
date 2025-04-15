package com.gestion_backend.dataModels.modelsGoogle;

import java.util.Set;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "projects", schema = "google_cloud")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class ProjectDataModel {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @Column(length = 255, nullable = false)
    private String id;

    @Column(unique = true, nullable = false, length = 255)
    @ToString.Include
    private String name;

    @Column(name = "display_name", length = 255)
    @ToString.Include
    private String displayName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    @ToString.Include
    private OrganizationDataModel organization;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VirtualMachineDataModel> virtualMachines;
}


