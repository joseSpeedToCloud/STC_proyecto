import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import { useNavigate } from '@tanstack/react-router';
import ExportarDatosArchivos from '../../components/export_datos_archivos';

const NetworkSecurityGroupAzure = () => {
  const [securityGroups, setSecurityGroups] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState('');
  const navigate = useNavigate();
  const subscriptionName = sessionStorage.getItem('selectedSubscriptionDisplayName') || 'Azure';

  useEffect(() => {
    const fetchSecurityGroups = async () => {
      setLoading(true);
      setError(null);

      const selectedSubscriptionId = sessionStorage.getItem("selectedSubscription");
      const accessToken = sessionStorage.getItem("azureAccessToken");

      if (!selectedSubscriptionId || !accessToken) {
        setError("No subscription selected or session expired.");
        setLoading(false);
        navigate({ to: "/loginazure" });
        return;
      }

      try {
        const response = await fetch(`http://localhost:8080/api/cloud/networksecuritygroupsazure?subscriptionId=${selectedSubscriptionId}`, {
          headers: { Authorization: `Bearer ${accessToken}` }
        });
        
        if (response.ok) {
          const data = await response.json();
          if (Array.isArray(data)) {
            setSecurityGroups(data);
          } else {
            throw new Error("Invalid response format");
          }
        } else {
          throw new Error("Failed to fetch data");
        }
      } catch (error) {
        console.error('Error fetching network security groups:', error);
        setError('Failed to load network security groups. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    fetchSecurityGroups();
  }, [navigate]);

  const handleInputChange = (event) => {
    setFilter(event.target.value);
  };

  const filteredSecurityGroups = securityGroups.filter((group) =>
    Object.values(group).some((value) =>
      String(value).toLowerCase().includes(filter.toLowerCase())
    )
  );

  // Define columns for export
  const exportColumns = [
    { header: 'ID', accessor: group => group.id.split('/').pop() },
    { header: 'Name', accessor: 'name' },
    { header: 'Resource Group', accessor: 'resourceGroup' },
    { header: 'Subscription ID', accessor: 'subscription_id' },
    { header: 'Location', accessor: 'location' },
    { header: 'Rules Count', accessor: group => group.security_rules ? group.security_rules.length : 0 },
    { header: 'Status', accessor: () => 'Active' }
  ];

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
              Return to Home
            </button>
          </div>
        </div>
      </Sidebar>
    );
  }

  return (
    <Sidebar showBackButton={true}>
      <div className="flex flex-col h-screen">
        <div className="w-full flex flex-col flex-grow">
          <div className="flex justify-between mb-4 px-6">
            <h1 className="text-2xl text-[#0078D4] self-center">Network Security Groups</h1>
            <div className="flex space-x-4">
              <input
                placeholder="Filter"
                className="border-2 border-[#ccc] rounded-[5px] px-3 py-2 text-sm text-black"
                value={filter}
                onChange={handleInputChange}
              />
              <ExportarDatosArchivos
                data={filteredSecurityGroups}
                columns={exportColumns}
                filename={`Azure_NetworkSecurityGroups_${subscriptionName}`}
                title={`Azure Network Security Groups - ${subscriptionName}`}
                subtitle={`Total: ${filteredSecurityGroups.length} security groups`}
              />
            </div>
          </div>
          <table className="min-w-full bg-white rounded-lg">
            <thead>
              <tr className="bg-gray-200 text-[#004E8C] font-semibold">
                <td className="py-2 px-4 border-b border-gray-200 text-center">ID</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Name</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Resource Group</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Subscription ID</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Location</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Rules Count</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Status</td>
              </tr>
            </thead>
            <tbody>
              {filteredSecurityGroups.length === 0 ? (
                <tr>
                  <td colSpan="7" className="px-4 py-4 text-center text-gray-500">
                    No network security groups found for this subscription.
                  </td>
                </tr>
              ) : (
                filteredSecurityGroups.map((group, index) => (
                  <tr key={index}>
                    <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{group.id.split('/').pop()}</td>
                    <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{group.name}</td>
                    <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{group.resourceGroup}</td>
                    <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{group.subscription_id}</td>
                    <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{group.location}</td>
                    <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{group.security_rules ? group.security_rules.length : 0}</td>
                    <td className="text-sm text-center py-2 px-4 border-b border-gray-200">
                      <span className="px-2 py-1 bg-green-100 text-green-800 rounded-full text-xs">Active</span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </Sidebar>
  );
};

export default NetworkSecurityGroupAzure;

