package com.gestion_backend.dataModels.modelsGoogle;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "firewalls", schema = "google_cloud")
@Data
public class FirewallDataModel {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    private ProjectDataModel project;

    private String direction;
    private String priority;
    private String protocol;
    private String sourceRanges;
    private String targetTags;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String details;
}


