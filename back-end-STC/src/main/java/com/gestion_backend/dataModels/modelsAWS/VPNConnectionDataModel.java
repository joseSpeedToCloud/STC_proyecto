package com.gestion_backend.dataModels.modelsAWS;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "vpn_connections", schema = "aws_cloud")
@Data
public class VPNConnectionDataModel {

    @Id
    @Column(length = 255)
    private String id;

    @Column(nullable = false)
    private String vpnConnectionId;

    private String name;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private AccountDataModel account;

    @ManyToOne
    @JoinColumn(name = "vpc_id", referencedColumnName = "id")
    private VPCDataModel vpc;

    private String state;
    private String customerGatewayId;
    private String vpnGatewayId;
    private String type;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String details;
}