import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import axios from 'axios';
import { useNavigate } from '@tanstack/react-router';
import ExportarDatosArchivos from '../../components/export_datos_archivos';

const AccountDetailsAWS = () => {
  const [totals, setTotals] = useState({
    ec2instancesAWS: 0,
    vpcsAWS: 0,
    subnetsAWS: 0,
    securitygroupsAWS: 0,
    ebsvolumesAWS: 0,
    s3bucketsAWS: 0,
    loadbalancersAWS: 0,
    rdsinstancesAWS: 0,
    eksclustersAWS: 0,
    iamrolesAWS: 0,
    snapshotsAWS: 0,
    vpnconnectionsAWS: 0,
  });

  const [accountName, setAccountName] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const navigate = useNavigate();

  const routes = {
    ec2instancesAWS: '/ec2instancesaws',
    vpcsAWS: '/vpcsaws',
    subnetsAWS: '/subnetsaws',
    securitygroupsAWS: '/securitygroupsaws',
    ebsvolumesAWS: '/ebsvolumesaws',
    s3bucketsAWS: '/s3bucketsaws',
    loadbalancersAWS: '/loadbalancersaws',
    rdsinstancesAWS: '/rdsinstancesaws',
    eksclustersAWS: '/eksclustersaws',
    iamrolesAWS: '/iamrolesaws',
    snapshotsAWS: '/snapshotsaws',
    vpnconnectionsAWS: '/vpnconnectionsaws',
  };

  useEffect(() => {
    const fetchTotals = async () => {
      setLoading(true);
      setError(null);

      const selectedAccountId = sessionStorage.getItem("selectedAWSAccount");
      const accessKeyId = sessionStorage.getItem("awsAccessKeyId");
      const secretAccessKey = sessionStorage.getItem("awsSecretAccessKey");
      const sessionToken = sessionStorage.getItem("awsSessionToken");
      const selectedAccountName = sessionStorage.getItem("selectedAWSAccountName");

      if (!selectedAccountId || !accessKeyId || !secretAccessKey || !sessionToken) {
        setError("No account selected or session expired.");
        setLoading(false);
        navigate({ to: "/loginaws" });
        return;
      }

      setAccountName(selectedAccountName);
      const totalsData = { ...totals };

      const resourceEndpoints = {
        ec2instancesAWS: 'ec2instancesAWS',
        vpcsAWS: 'vpcsAWS',
        subnetsAWS: 'subnetsAWS',
        securitygroupsAWS: 'securitygroupsAWS',
        ebsvolumesAWS: 'ebsvolumesAWS',
        s3bucketsAWS: 's3bucketsAWS',
        loadbalancersAWS: 'loadbalancersAWS',
        rdsinstancesAWS: 'rdsinstancesAWS',
        eksclustersAWS: 'eksclustersAWS',
        iamrolesAWS: 'iamrolesAWS',
        snapshotsAWS: 'snapshotsAWS',
        vpnconnectionsAWS: 'vpnconnectionsAWS',
      };

      try {
        for (const [key, endpoint] of Object.entries(resourceEndpoints)) {
          try {
            const response = await axios.get(`http://localhost:8080/api/aws/${endpoint}`, {
              params: { accountId: selectedAccountId },
              headers: {
                'X-AWS-ACCESS-KEY': accessKeyId,
                'X-AWS-SECRET-KEY': secretAccessKey,
                'X-AWS-SESSION-TOKEN': sessionToken,
              },
            });

            if (response.status === 200 && Array.isArray(response.data)) {
              totalsData[key] = response.data.length;
            } else {
              totalsData[key] = 0;
            }
          } catch {
            totalsData[key] = 0;
          }
        }

        setTotals(totalsData);
      } catch (error) {
        setError('Failed to load account details. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    fetchTotals();
  }, [navigate]);

  const formatResourceName = (key) => {
    const name = key.replace('AWS', '');
    return name.charAt(0).toUpperCase() + name.slice(1).replace(/([A-Z])/g, ' $1');
  };

  // Prepare data for export component
  const exportData = Object.entries(totals).map(([key, value]) => ({
    resource: formatResourceName(key),
    count: value
  }));

  const exportColumns = [
    { header: 'Resource', accessor: 'resource' },
    { header: 'Count', accessor: 'count' }
  ];

  if (loading) {
    return (
      <Sidebar showBackButton={true}>
        <div className="flex justify-center items-center h-full">
          <div className="loader border-t-transparent border-yellow-500 w-10 h-10 border-4 rounded-full animate-spin"></div>
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
              className="mt-3 px-4 py-2 bg-yellow-600 text-white rounded hover:bg-yellow-700"
            >
              Return to AWS Accounts
            </button>
          </div>
        </div>
      </Sidebar>
    );
  }

  return (
    <Sidebar showBackButton={true}>
      <div className="p-6 w-full max-w-screen-xl mx-auto">
        <h1 className="text-2xl font-bold mb-6 text-yellow-600">
          Resources in AWS Account: {accountName}
        </h1>

        <div className="flex flex-wrap mt-4">
          {Object.entries(totals).map(([key, value]) => (
            <a
              key={key}
              href={routes[key]}
              className="w-full sm:w-1/2 md:w-1/3 lg:w-1/4 xl:w-1/5 p-3"
            >
              <div className="border-2 rounded-md p-4 hover:shadow-lg transition-shadow duration-200 h-full">
                <div className="font-semibold text-lg text-yellow-600">
                  {formatResourceName(key)}
                </div>
                <p className="text-right text-2xl font-bold text-yellow-800">
                  {value}
                </p>
              </div>
            </a>
          ))}
        </div>

        <div className="mt-8">
          <ExportarDatosArchivos 
            data={exportData}
            columns={exportColumns}
            filename={`AWS_Account_${accountName}`}
            title={`AWS Account Details: ${accountName}`}
          />
        </div>
      </div>
    </Sidebar>
  );
};

export default AccountDetailsAWS;

