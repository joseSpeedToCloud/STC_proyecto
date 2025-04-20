import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import ExportarDatosArchivos from '../../components/export_datos_archivos';

export default function DisksAzure() {
  const [filter, setFilter] = useState('');
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const selectedSubscriptionId = sessionStorage.getItem('selectedSubscription');
        if (!selectedSubscriptionId) {
          console.error('No subscription selected.');
          setLoading(false);
          return;
        }

        const result = await fetch(`http://localhost:8080/api/cloud/disksazure?subscriptionId=${selectedSubscriptionId}`);
        const jsonData = await result.json();
        setData(jsonData);
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

  const filteredData = data.filter((item) =>
    Object.values(item).some((value) =>
      String(value).toLowerCase().includes(filter.toLowerCase())
    )
  );

  // Define columns for the ExportarDatosArchivos component
  const columns = [
    { header: 'ID', accessor: 'id' },
    { header: 'Name', accessor: 'name' },
    { header: 'Resource Group', accessor: item => item.resourceGroup?.name || 'N/A' },
    { header: 'Subscription ID', accessor: item => item.subscription?.id },
    { header: 'Disk Type', accessor: 'diskType' },
    { header: 'Location', accessor: 'location' },
    { header: 'Size (GB)', accessor: 'sizeGb' },
    { header: 'Status', accessor: 'status' }
  ];

  // Get subscription name for file naming
  const subscriptionName = sessionStorage.getItem('selectedSubscriptionDisplayName') || 'Azure';
  const exportFilename = `Azure_Disks_${subscriptionName}`;

  return (
    <Sidebar showBackButton={true}>
      <div className="flex flex-col h-screen">
        <div className="w-full flex flex-col flex-grow">
          <div className="flex justify-between mb-4 px-6">
            <h1 className="text-2xl text-[#0078D4] self-center">Azure Disks</h1>
            <div className="flex space-x-4 items-center">
              <input
                placeholder="Filter"
                className="border-2 border-[#ccc] rounded-[5px] px-3 py-2 text-sm text-black"
                value={filter}
                onChange={handleInputChange}
              />
              <ExportarDatosArchivos
                data={filteredData}
                columns={columns}
                filename={exportFilename}
                title="Azure Disks"
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
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Disk Type</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Location</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Size (GB)</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Status</td>
                </tr>
              </thead>
              <tbody>
                {filteredData.length > 0 ? (
                  filteredData.map((item, index) => (
                    <tr key={index}>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.id}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.name}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.resourceGroup?.name || 'N/A'}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.subscription?.id}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.diskType}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.location}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.sizeGb}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.status}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="8" className="text-center py-4 text-gray-500">No data available</td>
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

