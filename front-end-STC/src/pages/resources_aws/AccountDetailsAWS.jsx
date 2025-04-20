import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import axios from 'axios';
import { useNavigate } from '@tanstack/react-router';
import ExportarDatosArchivos from '../../components/export_datos_archivos';

const AccountDetailsAWS = () => {
  const [totals, setTotals] = useState({
    ec2InstancesAWS: 0,
    vpcsAWS: 0,
    subnetsAWS: 0,
    loadBalancersAWS: 0,
    ebsVolumesAWS: 0,
    snapshotsAWS: 0,
    vpnConnectionsAWS: 0,
    securityGroupsAWS: 0,
    eksClustersAWS: 0,
    rdsInstancesAWS: 0,
    s3BucketsAWS: 0,
    iamRolesAWS: 0
  });

  const [accountName, setAccountName] = useState('');
  const [accountId, setAccountId] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const routes = {
    ec2InstancesAWS: '/ec2instancesaws',
    vpcsAWS: '/vpcsaws',
    subnetsAWS: '/subnetsaws',
    securityGroupsAWS: '/securitygroupsaws',
    ebsVolumesAWS: '/ebsvolumesaws',
    snapshotsAWS: '/snapshotsaws',
    loadBalancersAWS: '/loadbalancersaws',
    vpnConnectionsAWS: '/vpnconnectionsaws',
    eksClustersAWS: '/eksclustersaws',
    rdsInstancesAWS: '/rdsinstancesaws',
    s3BucketsAWS: '/s3bucketsaws',
    iamRolesAWS: '/iamrolesaws'
  };

  useEffect(() => {
    const fetchTotals = async () => {
      setLoading(true);
      setError(null);

      const selectedAccountId = sessionStorage.getItem("selectedAccountAWS");
      const selectedAccountName = sessionStorage.getItem("selectedAccountNameAWS");
      const syncCompleted = sessionStorage.getItem("awsSyncCompleted");
      const awsToken = sessionStorage.getItem("awsToken");

      if (!selectedAccountId || !awsToken || syncCompleted !== "true") {
        setError("No account selected or session expired.");
        setLoading(false);
        navigate({ to: "/loginaws" });
        return;
      }

      setAccountId(selectedAccountId);
      setAccountName(selectedAccountName || 'AWS Account');
      const totalsData = { ...totals };

      try {
        // Define all API endpoints with consistent naming
        const resourceEndpoints = {
          ec2InstancesAWS: 'ec2instancesAWS',
          vpcsAWS: 'vpcsAWS',
          subnetsAWS: 'subnetsAWS',
          securityGroupsAWS: 'securitygroupsAWS',
          ebsVolumesAWS: 'ebsvolumesAWS',
          snapshotsAWS: 'snapshotsAWS',
          loadBalancersAWS: 'loadbalancersAWS',
          vpnConnectionsAWS: 'vpnconnectionsAWS',
          eksClustersAWS: 'eksclustersAWS',
          rdsInstancesAWS: 'rdsinstancesAWS',
          s3BucketsAWS: 's3bucketsAWS',
          iamRolesAWS: 'iamrolesAWS'
        };

        // Fetch each resource type with better error handling
        for (const [key, endpoint] of Object.entries(resourceEndpoints)) {
          try {
            const response = await axios.get(`http://localhost:8080/api/aws/${endpoint}`, {
              params: { accountId: selectedAccountId }
            });
            
            // Only update the total if we got a valid response
            if (response.status === 200 && Array.isArray(response.data)) {
              totalsData[key] = response.data.length;
            } else {
              console.warn(`Invalid response for ${endpoint}:`, response);
              totalsData[key] = 0;
            }
          } catch (resourceError) {
            console.warn(`Error fetching ${endpoint}:`, resourceError);
            // Continue with other resources even if one fails
            totalsData[key] = 0;
          }
        }

        setTotals(totalsData);
      } catch (error) {
        console.error('Error fetching account details:', error);
        setError('Failed to load account details. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    fetchTotals();
  }, [navigate]);

  // Define columns for export
  const exportColumns = [
    { header: 'Resource Type', accessor: key => key.charAt(0).toUpperCase() + key.slice(1).replace(/([A-Z])/g, ' $1') },
    { header: 'Count', accessor: 'count' }
  ];

  // Format data for export
  const exportData = Object.entries(totals).map(([key, value]) => ({
    key: key,
    name: key.charAt(0).toUpperCase() + key.slice(1).replace(/([A-Z])/g, ' $1'),
    count: value
  }));

  if (loading) {
    return (
      <Sidebar showBackButton={true}>
        <div className="flex justify-center items-center h-full">
          <div className="loader border-t-transparent border-yellow-400 w-10 h-10 border-4 rounded-full animate-spin"></div>
        </div>
      </Sidebar>
    );
  }

  if (error) {
    return (
      <Sidebar showBackButton={true}>
        <div className="p-6">
          <div className="bg-red-50 border border-red-300 rounded-md p-4 mb-4">
            <h2 className="text-lg font-semibold text-red-700">Error</h2>
            <p className="text-red-600">{error}</p>
            <button
              onClick={() => navigate({ to: "/homeaws" })}
              className="mt-3 px-4 py-2 bg-yellow-500 text-white rounded hover:bg-yellow-600"
            >
              Return to Accounts
            </button>
          </div>
        </div>
      </Sidebar>
    );
  }

  return (
    <Sidebar showBackButton={true}>
      <div className="p-6">
        <div className="flex justify-between mb-6">
          <div>
            <h1 className="text-2xl font-bold text-yellow-600">
              Resources in AWS Account: {accountName}
            </h1>
            <p className="text-gray-600 mt-1">Account ID: {accountId}</p>
          </div>
          <ExportarDatosArchivos
            data={exportData}
            columns={exportColumns}
            filename={`AWS_Account_${accountName}_${accountId}`}
            title={`AWS Account: ${accountName}`}
            subtitle={`Account ID: ${accountId}`}
          />
        </div>
        
        <div className="flex flex-wrap">
          {Object.entries(totals).map(([key, value]) => (
            <a
              key={key}
              href={routes[key]}
              className="w-full sm:w-1/2 md:w-1/3 lg:w-1/4 xl:w-1/5 p-3"
            >
              <div className="border-2 rounded-md p-4 hover:shadow-lg transition-shadow duration-200">
                <div className="font-semibold text-lg text-yellow-600">
                  {key.charAt(0).toUpperCase() + key.slice(1).replace(/([A-Z])/g, ' $1')}
                </div>
                <p className="text-right text-2xl font-bold text-yellow-800">
                  {value}
                </p>
              </div>
            </a>
          ))}
        </div>
      </div>
    </Sidebar>
  );
};

export default AccountDetailsAWS;

