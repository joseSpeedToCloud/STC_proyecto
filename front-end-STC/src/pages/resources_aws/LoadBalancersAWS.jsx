import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import ExportarDatosArchivos from '../../components/export_datos_archivos';

export default function LoadBalancersAWS() {
  const [filter, setFilter] = useState('');
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const accountName = sessionStorage.getItem('selectedAWSAccountName') || 'AWS';

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const selectedAccountId = sessionStorage.getItem('selectedAWSAccount');
        if (!selectedAccountId) {
          console.error('No AWS account selected.');
          setLoading(false);
          return;
        }

        const result = await fetch(`http://localhost:8080/api/aws/loadbalancersAWS?accountId=${selectedAccountId}`);
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
  const exportColumns = [
    { header: 'Name', accessor: 'name' },
    { header: 'DNS Name', accessor: 'dnsName' },
    { header: 'Type', accessor: 'type' },
    { header: 'Scheme', accessor: 'scheme' },
    { header: 'State', accessor: 'state' },
    { header: 'Region', accessor: 'region' }
  ];

  return (
    <Sidebar showBackButton={true}>
      <div className="flex flex-col h-screen">
        <div className="w-full flex flex-col flex-grow">
          <div className="flex justify-between mb-4 px-6">
            <h1 className="text-2xl text-[#FF9900] self-center">AWS Load Balancers</h1>
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
                filename={`AWS_LoadBalancers_${accountName}`}
                title={`AWS Load Balancers - ${accountName}`}
                subtitle={`Total: ${filteredData.length} load balancers`}
              />
            </div>
          </div>
          
          {loading ? (
            <div className="flex justify-center items-center h-64">
              <div className="loader border-t-transparent border-orange-400 w-10 h-10 border-4 rounded-full animate-spin"></div>
            </div>
          ) : (
            <table className="min-w-full bg-white rounded-lg">
              <thead>
                <tr className="bg-gray-200 text-[#232F3E] font-semibold">
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Name</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">DNS Name</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Type</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Scheme</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">State</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Region</td>
                </tr>
              </thead>
              <tbody>
                {filteredData.length > 0 ? (
                  filteredData.map((item, index) => (
                    <tr key={index}>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.name}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.dnsName}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.type}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.scheme}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.state}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.region}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="6" className="text-center py-4 text-gray-500">No data available</td>
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

