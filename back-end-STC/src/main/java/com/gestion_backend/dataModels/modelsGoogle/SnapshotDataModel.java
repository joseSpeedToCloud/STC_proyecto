package com.gestion_backend.dataModels.modelsGoogle;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "snapshots", schema = "google_cloud")
@Data
public class SnapshotDataModel {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    private ProjectDataModel project;

    private String sourceDisk;
    private Long diskSizeGb;
    private String status;
    private String storageLocations;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String details;
}

