package com.gestion_backend.dataModels.modelsGoogle;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "subnetworks", schema = "google_cloud")
@Data
public class SubnetworkDataModel {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "network_id", referencedColumnName = "id")
    private NetworkDataModel network;

    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    private ProjectDataModel project;

    private String region;
    private String ipCidrRange;
    private Boolean privateIpGoogleAccess;
    private String stackType;

    @Column(length = 500)
    private String description;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String details;
}

