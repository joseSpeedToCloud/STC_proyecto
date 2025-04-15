import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import ExportarDatosArchivos from '../../components/ExportarDatosArchivos';

export default function SnapshotsAWS() {
  const [filter, setFilter] = useState('');
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);

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

        const result = await fetch(`http://localhost:8080/api/aws/snapshotsAWS?accountId=${selectedAccountId}`);
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

  // Define columns for the export component
  const columns = [
    { header: 'Snapshot ID', accessor: 'snapshotId' },
    { header: 'Name', accessor: 'name' },
    { header: 'Volume ID', accessor: 'volumeId' },
    { header: 'Volume Size', accessor: 'volumeSize' },
    { header: 'State', accessor: 'state' },
    { 
      header: 'Encrypted', 
      accessor: (item) => item.encrypted ? 'Yes' : 'No'
    },
    { header: 'Description', accessor: 'description' }
  ];

  const accountName = sessionStorage.getItem('selectedAWSAccountName') || 'AWS';

  return (
    <Sidebar showBackButton={true}>
      <div className="flex flex-col h-screen">
        <div className="w-full flex flex-col flex-grow">
          <div className="flex flex-col md:flex-row justify-between mb-4 px-6 gap-4">
            <h1 className="text-2xl text-[#FF9900] self-center">AWS Snapshots</h1>
            <div className="flex flex-col md:flex-row gap-4">
              <input
                placeholder="Filter"
                className="border-2 border-[#ccc] rounded-[5px] px-3 py-2 text-sm text-black"
                value={filter}
                onChange={handleInputChange}
              />
              <ExportarDatosArchivos 
                data={filteredData}
                columns={columns}
                filename={`AWS_Snapshots_${accountName}`}
                title={`AWS Snapshots`}
                subtitle={accountName}
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
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Snapshot ID</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Name</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Volume ID</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Volume Size</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">State</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Encrypted</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Description</td>
                </tr>
              </thead>
              <tbody>
                {filteredData.length > 0 ? (
                  filteredData.map((item, index) => (
                    <tr key={index}>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.snapshotId}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.name}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.volumeId}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.volumeSize}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.state}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.encrypted ? 'Yes' : 'No'}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.description}</td>
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

