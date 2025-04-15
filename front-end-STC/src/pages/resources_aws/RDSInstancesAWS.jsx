import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import ExportarDatosArchivos from '../../components/export_datos_archivos';

export default function RDSInstancesAWS() {
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

        const result = await fetch(`http://localhost:8080/api/aws/rdsinstancesAWS?accountId=${selectedAccountId}`);
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
    { header: 'DB Identifier', accessor: 'dbInstanceIdentifier' },
    { header: 'Engine', accessor: 'engine' },
    { header: 'Instance Class', accessor: 'instanceClass' },
    { header: 'Status', accessor: 'status' },
    { header: 'Multi-AZ', accessor: item => item.multiAz ? 'Yes' : 'No' },
    { header: 'Endpoint', accessor: 'endpoint' },
    { header: 'Storage (GB)', accessor: 'allocatedStorage' }
  ];

  return (
    <Sidebar showBackButton={true}>
      <div className="flex flex-col h-screen">
        <div className="w-full flex flex-col flex-grow">
          <div className="flex justify-between mb-4 px-6">
            <h1 className="text-2xl text-[#FF9900] self-center">AWS RDS Instances</h1>
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
                filename={`AWS_RDS_Instances_${accountName}`}
                title={`AWS RDS Instances - ${accountName}`}
                subtitle={`Total: ${filteredData.length} instances`}
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
                  <td className="py-2 px-4 border-b border-gray-200 text-center">DB Identifier</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Engine</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Instance Class</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Status</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Multi-AZ</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Endpoint</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Storage (GB)</td>
                </tr>
              </thead>
              <tbody>
                {filteredData.length > 0 ? (
                  filteredData.map((item, index) => (
                    <tr key={index}>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.dbInstanceIdentifier}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.engine}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.instanceClass}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.status}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.multiAz ? 'Yes' : 'No'}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.endpoint}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.allocatedStorage}</td>
                    </tr>
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

