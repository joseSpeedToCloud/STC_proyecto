import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import ExportarDatosArchivos from '../../components/export_datos_archivos';

export default function SubnetworksAzure() {
  const [filter, setFilter] = useState('');
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [subscriptionDisplayName, setSubscriptionDisplayName] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const selectedSubscriptionId = sessionStorage.getItem('selectedSubscription');
        const selectedSubscriptionDisplayName = sessionStorage.getItem('selectedSubscriptionDisplayName');
        
        if (!selectedSubscriptionId) {
          console.error('No subscription selected.');
          setLoading(false);
          return;
        }

        setSubscriptionDisplayName(selectedSubscriptionDisplayName || 'Azure');

        const result = await fetch(`http://localhost:8080/api/cloud/subnetworksazure?subscriptionId=${selectedSubscriptionId}`);
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

  // Define columns for export
  const columns = [
    { header: 'ID', accessor: 'subnetwork_id' },
    { header: 'Name', accessor: 'name' },
    { header: 'Network ID', accessor: 'network_id' },
    { header: 'Subscription ID', accessor: 'subscription_id' },
    { header: 'Address Prefix', accessor: 'address_prefix' },
    { header: 'Private Endpoint Policies', accessor: 'private_endpoint_network_policies' },
    { header: 'Private Link Policies', accessor: 'private_link_service_network_policies' },
    { header: 'Description', accessor: 'description' }
  ];

  return (
    <Sidebar showBackButton={true}>
      <div className="flex flex-col h-screen">
        <h1 className="text-3xl font-bold mb-2 mt-4 ml-6 text-[#0078D4]">
          Microsoft Azure
        </h1>
        <h1 className="text-2xl font-bold mb-6 ml-6 text-[#0078D4]">
          Subnetworks - {subscriptionDisplayName}
        </h1>
        <div className="w-full flex flex-col flex-grow">
          <div className="flex justify-between mb-4 px-6">
            <h1 className="text-2xl text-[#0078D4] self-center">Azure Subnetworks</h1>
            <div className="flex space-x-4">
              <input
                placeholder="Filter"
                className="border-2 border-[#ccc] rounded-[5px] px-3 py-2 text-sm text-black"
                value={filter}
                onChange={handleInputChange}
              />
              <ExportarDatosArchivos
                data={filteredData}
                columns={columns}
                filename={`Azure_Subnetworks_${subscriptionDisplayName}`}
                title={`Azure Subnetworks - ${subscriptionDisplayName}`}
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
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Network ID</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Subscription ID</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Address Prefix</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Private Endpoint Policies</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Private Link Policies</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Description</td>
                </tr>
              </thead>
              <tbody>
                {filteredData.length > 0 ? (
                  filteredData.map((item, index) => (
                    <tr key={index}>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.subnetwork_id}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.name}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.network_id}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.subscription_id}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.address_prefix}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.private_endpoint_network_policies}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.private_link_service_network_policies}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.description}</td>
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

