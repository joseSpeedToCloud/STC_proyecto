package com.gestion_backend.dataModels.modelsAzure;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "virtual_machines", schema = "azure_cloud")
@Data
public class VirtualMachineDataModelAzure {
    
    @Id
    @Column(length = 255)
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @ManyToOne
    @JoinColumn(name = "resource_group_id", referencedColumnName = "id")
    private ResourceGroupDataModelAzure resourceGroup;
    
    @ManyToOne
    @JoinColumn(name = "subscription_id", referencedColumnName = "id")
    private SubscriptionDataModelAzure subscription;
    
    private String vmSize;
    private String location;
    private String status;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String details;
}

