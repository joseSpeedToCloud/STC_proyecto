package com.gestion_backend.dataModels.modelsGoogle;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "virtual_machines", schema = "google_cloud")
@Data
public class VirtualMachineDataModel {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    private ProjectDataModel project;

    private String machineType;
    private String zone;
    private String status;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String details;
}




