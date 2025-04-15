package com.gestion_backend.dataModels.modelsAzure;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "subnets", schema = "azure_cloud")
@Data
public class SubnetDataModelAzure {
    
    @Id
    @Column(length = 255)
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @ManyToOne
    @JoinColumn(name = "virtual_network_id", referencedColumnName = "id")
    private VirtualNetworkDataModelAzure virtualNetwork;
    
    @ManyToOne
    @JoinColumn(name = "subscription_id", referencedColumnName = "id")
    private SubscriptionDataModelAzure subscription;
    
    private String addressPrefix;
    private String privateEndpointNetworkPolicies;
    private String privateLinkServiceNetworkPolicies;
    private String description;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String details;
}

