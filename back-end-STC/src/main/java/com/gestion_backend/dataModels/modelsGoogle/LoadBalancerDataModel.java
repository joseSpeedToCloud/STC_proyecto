package com.gestion_backend.dataModels.modelsGoogle;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "load_balancers", schema = "google_cloud")
@Data
public class LoadBalancerDataModel {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    private ProjectDataModel project;

    private String region;
    private String loadBalancingScheme;
    private String networkTier;
    private String type;
    private String ipAddress;
    private Integer port;
    private String status;
    private String target;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String details;
}


