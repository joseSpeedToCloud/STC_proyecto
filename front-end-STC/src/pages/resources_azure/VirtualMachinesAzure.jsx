import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import ExportarDatosArchivos from '../../components/export_datos_archivos';

export default function VirtualMachinesAzure() {
  const [filter, setFilter] = useState('');
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandedRows, setExpandedRows] = useState({});
  const [subscriptionName, setSubscriptionName] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const selectedSubscriptionId = sessionStorage.getItem('selectedSubscription');
        const displayName = sessionStorage.getItem('selectedSubscriptionDisplayName') || 'Azure';
        setSubscriptionName(displayName);
        
        if (!selectedSubscriptionId) {
          console.error('No subscription selected.');
          setLoading(false);
          return;
        }

        const result = await fetch(`http://localhost:8080/api/cloud/virtualmachinesazure?subscriptionId=${selectedSubscriptionId}`);
        const jsonData = await result.json();
        
        // Fetch additional details for each VM
        const detailedData = await Promise.all(
          jsonData.map(async (vm) => {
            try {
              const detailResponse = await fetch(`http://localhost:8080/api/cloud/subscription/${selectedSubscriptionId}`);
              const subscriptionDetails = await detailResponse.json();
              
              // Find the specific VM in the subscription details
              const vmDetails = subscriptionDetails.virtual_machines.find(
                (v) => v.machine_id === vm.machine_id
              );
              
              return { 
                ...vm, 
                ...vmDetails,
                resourceGroup: vm.resource_group || 'N/A',
                additionalDetails: vmDetails?.details || 'No additional details'
              };
            } catch (error) {
              console.error(`Error fetching details for VM ${vm.machine_id}:`, error);
              return vm;
            }
          })
        );
        
        setData(detailedData);
      } catch (error) {
        console.error('Error fetching data:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const handleInputChange = (event) => {
    setFilter(event.target.value);
  };

  const toggleRowExpand = (machineId) => {
    setExpandedRows(prev => ({
      ...prev,
      [machineId]: !prev[machineId]
    }));
  };

  const filteredData = data.filter((item) =>
    Object.values(item).some((value) =>
      String(value).toLowerCase().includes(filter.toLowerCase())
    )
  );

  // Define columns for export
  const exportColumns = [
    { header: 'ID', accessor: (item) => item.machine_id },
    { header: 'Name', accessor: 'name' },
    { header: 'Resource Group', accessor: 'resourceGroup' },
    { header: 'Subscription ID', accessor: 'subscription_id' },
    { header: 'VM Size', accessor: (item) => item.machine_type || item.vm_size },
    { header: 'Location', accessor: (item) => item.zone || item.location },
    { header: 'Status', accessor: 'status' },
    { header: 'Additional Details', accessor: 'additionalDetails' }
  ];

  return (
    <Sidebar showBackButton={true}>
      <div className="flex flex-col h-screen">
        <div className="w-full flex flex-col flex-grow">
          <div className="flex justify-between mb-4 px-6">
            <h1 className="text-2xl text-[#0078D4] self-center">Azure Virtual Machines</h1>
            <div className="flex space-x-4">
              <input
                placeholder="Filter"
                className="border-2 border-[#ccc] rounded-[5px] px-3 py-2 text-sm text-black"
                value={filter}
                onChange={handleInputChange}
              />
              <ExportarDatosArchivos
                data={filteredData}
                columns={exportColumns}
                filename={`Azure_VirtualMachines_${subscriptionName}`}
                title="Azure Virtual Machines"
                subtitle={subscriptionName}
              />
            </div>
          </div>
          
          {loading ? (
            <div className="flex justify-center items-center h-64">
              <div className="loader border-t-transparent border-blue-400 w-10 h-10 border-4 rounded-full animate-spin"></div>
            </div>
          ) : (
            <table className="min-w-full bg-white rounded-lg">
              <thead>
                <tr className="bg-gray-200 text-[#004E8C] font-semibold">
                  <td className="py-2 px-4 border-b border-gray-200 text-center">ID</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Name</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Resource Group</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Subscription ID</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">VM Size</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Location</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Status</td>
                </tr>
              </thead>
              <tbody>
                {filteredData.length > 0 ? (
                  filteredData.map((item) => (
                    <React.Fragment key={item.machine_id}>
                      <tr>
                        <td className="text-sm text-center py-2 px-4 border-b border-gray-200">
                          {item.machine_id.split('-')[0]}
                        </td>
                        <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.name}</td>
                        <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.resourceGroup}</td>
                        <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.subscription_id}</td>
                        <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.machine_type || item.vm_size}</td>
                        <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.zone || item.location}</td>
                        <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.status}</td>
                      </tr>
                    </React.Fragment>
                  ))
                ) : (
                  <tr>
                    <td colSpan="7" className="text-center py-4 text-gray-500">No data available</td>
                  </tr>
                )}
              </tbody>
            </table>
          )}
        </div>       
      </div>
    </Sidebar>
  );
}
