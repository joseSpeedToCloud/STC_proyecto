package com.gestion_backend.dataModels.modelsAzure;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "directories", schema = "azure_cloud")
@Data
public class DirectoryDataModelAzure {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, unique = true)
    private String name;
}


