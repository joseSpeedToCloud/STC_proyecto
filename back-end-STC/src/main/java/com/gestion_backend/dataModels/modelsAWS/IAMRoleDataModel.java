package com.gestion_backend.dataModels.modelsAWS;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;
import java.time.LocalDateTime;

@Entity
@Table(name = "iam_roles", schema = "aws_cloud")
@Data
public class IAMRoleDataModel {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false)
    private String arn;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private AccountDataModel account;

    private String path;
    private LocalDateTime createDate;
    
    @Column(length = 500)
    private String description;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String details;
}