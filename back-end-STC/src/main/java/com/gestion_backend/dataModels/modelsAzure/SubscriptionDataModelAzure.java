package com.gestion_backend.dataModels.modelsAzure;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "subscriptions", schema = "azure_cloud")
@Data
public class SubscriptionDataModelAzure {
    
    @Id
    @Column(length = 255)
    private String id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    private String displayName;
    
    @ManyToOne
    @JoinColumn(name = "directory_id", referencedColumnName = "id")
    private DirectoryDataModelAzure directory;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VirtualMachineDataModelAzure> virtualMachines;

}

