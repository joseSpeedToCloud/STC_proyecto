import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import axios from 'axios';
import { useNavigate } from '@tanstack/react-router';
import { saveAs } from 'file-saver';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';


const SubscriptionDetails = () => {
  const [totals, setTotals] = useState({
    virtualMachinesAzure: 0,
    networksAzure: 0,
    subnetworksAzure: 0,
    loadBalancersAzure: 0,
    disksAzure: 0,
    snapshotsAzure: 0,
    vpnsAzure: 0,
    networkSecurityGroupsAzure: 0,
    aksClustersAzure: 0,
  });

  const [subscriptionDisplayName, setSubscriptionDisplayName] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const routes = {
    virtualMachinesAzure: '/virtualmachinesazure',
    networksAzure: '/networksazure',
    subnetworksAzure: '/subnetworksazure',
    disksAzure: '/disksazure',
    snapshotsAzure: '/snapshotsazure',
    loadBalancersAzure: '/loadbalancersazure',
    vpnsAzure: '/vpnsazure',
    networkSecurityGroupsAzure: '/networkssecurityazure',
    aksClustersAzure: '/aksclusterazure',
  };

  useEffect(() => {
    const fetchTotals = async () => {
      setLoading(true);
      setError(null);

      const selectedSubscriptionId = sessionStorage.getItem("selectedSubscription");
      const accessToken = sessionStorage.getItem("azureAccessToken");
      const selectedSubscriptionDisplayName = sessionStorage.getItem("selectedSubscriptionDisplayName");

      if (!selectedSubscriptionId || !accessToken) {
        setError("No subscription selected or session expired.");
        setLoading(false);
        navigate({ to: "/loginazure" });
        return;
      }

      setSubscriptionDisplayName(selectedSubscriptionDisplayName);
      const totalsData = { ...totals };

      try {
        // Define all API endpoints with consistent naming
        const resourceEndpoints = {
          virtualMachinesAzure: 'virtualmachinesazure',
          networksAzure: 'networksazure',
          subnetworksAzure: 'subnetworksazure',
          disksAzure: 'disksazure',
          snapshotsAzure: 'snapshotsazure',
          loadBalancersAzure: 'loadbalancersazure',
          vpnsAzure: 'vpnsazure',
          networkSecurityGroupsAzure: 'networksecuritygroupsazure', // Fixed endpoint
          aksClustersAzure: 'aksclustersazure',
        };

        // Fetch each resource type with better error handling
        for (const [key, endpoint] of Object.entries(resourceEndpoints)) {
          try {
            const response = await axios.get(`http://localhost:8080/api/cloud/${endpoint}`, {
              headers: { Authorization: `Bearer ${accessToken}` },
              params: { subscriptionId: selectedSubscriptionId },
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
        console.error('Error fetching subscription details:', error);
        setError('Failed to load subscription details. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    fetchTotals();
  }, [navigate]);

  // Función para exportar a CSV
  const exportToCSV = () => {
    const csvRows = [
      ['Resource', 'Count'],
      ...Object.entries(totals).map(([key, value]) => [
        key.charAt(0).toUpperCase() + key.slice(1).replace(/([A-Z])/g, ' $1'), 
        value
      ]),
    ];
    const csvContent = csvRows.map(e => e.join(',')).join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    saveAs(blob, `Subscription_${subscriptionDisplayName}.csv`);
  };

  // Función para exportar a PDF
  const exportToPDF = () => {
    const doc = new jsPDF();
    doc.text(`Subscription Details: ${subscriptionDisplayName}`, 10, 10);
  
    // Usar autoTable
    autoTable(doc, {
      head: [['Resource', 'Count']],
      body: Object.entries(totals).map(([key, value]) => [
        key.charAt(0).toUpperCase() + key.slice(1).replace(/([A-Z])/g, ' $1'),
        value
      ]),
    });
  
    doc.save(`Subscription_${subscriptionDisplayName}.pdf`);
  };

  if (loading) {
    return (
      <Sidebar showBackButton={true}>
        <div className="flex justify-center items-center h-full">
          <div className="loader border-t-transparent border-blue-400 w-10 h-10 border-4 rounded-full animate-spin"></div>
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
              onClick={() => navigate({ to: "/homeazure" })}
              className="mt-3 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
            >
              Return to Subscriptions
            </button>
          </div>
        </div>
      </Sidebar>
    );
  }

  return (
    <Sidebar showBackButton={true}>
      <div className="p-6">
        <h1 className="text-2xl font-bold mb-6 text-[#0078D4]">
          Resources in Subscription {subscriptionDisplayName}
        </h1>
        <div className="flex flex-wrap mt-8">
          {Object.entries(totals).map(([key, value]) => (
            <a
              key={key}
              href={routes[key]}
              className="w-full sm:w-1/2 md:w-1/3 lg:w-1/4 xl:w-1/5 p-3"
            >
              <div className="border-2 rounded-md p-4 hover:shadow-lg transition-shadow duration-200">
                <div className="font-semibold text-lg text-[#0078D4]">
                  {key.charAt(0).toUpperCase() + key.slice(1).replace(/([A-Z])/g, ' $1')}
                </div>
                <p className="text-right text-2xl font-bold text-[#004E8C]">
                  {value}
                </p>
              </div>
            </a>
          ))}
        </div>
        <div className="mt-6 flex space-x-4">
          <button
            onClick={exportToCSV}
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-700"
          >
            Export to CSV
          </button>
          <button
            onClick={exportToPDF}
            className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-700"
          >
            Export to PDF
          </button>
        </div>
      </div>
    </Sidebar>
  );
};

export default SubscriptionDetails;

