package com.gestion_backend.dataModels.modelsGoogle;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "disks", schema = "google_cloud")
@Data
public class DiskDataModel {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    private ProjectDataModel project;

    private String type;
    private String zone;
    private Long sizeGb;
    private String status;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String details;
}


