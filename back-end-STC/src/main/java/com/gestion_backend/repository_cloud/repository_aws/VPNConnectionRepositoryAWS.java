package com.gestion_backend.repository_cloud.repository_aws;

import com.gestion_backend.dataModels.modelsAWS.VPNConnectionDataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VPNConnectionRepositoryAWS extends JpaRepository<VPNConnectionDataModel, String> {
    
    Optional<VPNConnectionDataModel> findByVpnConnectionIdAndAccount_Id(String vpnConnectionId, String accountId);
    
    List<VPNConnectionDataModel> findByAccount_Id(String accountId);
    
    List<VPNConnectionDataModel> findByVpc_Id(String vpcId);
    
    List<VPNConnectionDataModel> findByState(String state);
    
    List<VPNConnectionDataModel> findByCustomerGatewayId(String customerGatewayId);
    
    List<VPNConnectionDataModel> findByVpnGatewayId(String vpnGatewayId);
}

