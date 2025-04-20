import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import ExportarDatosArchivos from '../../components/export_datos_archivos';

export default function DisksGoogle() {
  const [filter, setFilter] = useState('');
  const [data, setData] = useState([]);
  const [projectDisplayName, setProjectDisplayName] = useState(''); 
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const selectedProjectId = sessionStorage.getItem('selectedProject');
        const selectedProjectDisplayName = sessionStorage.getItem("selectedProjectDisplayName");
        
        if (!selectedProjectId) {
          console.error('No se ha seleccionado un proyecto.');
          setLoading(false);
          return;
        }

        setProjectDisplayName(selectedProjectDisplayName); 

        const result = await fetch(`http://localhost:8080/api/cloud/disksgoogle?projectId=${selectedProjectId}`);
        const jsonData = await result.json();
        setData(jsonData);
      } catch (error) {
        console.error('Error al obtener los datos:', error);
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
    { header: 'ID', accessor: 'disk_id' },
    { header: 'Name', accessor: 'name' },
    { header: 'Project ID', accessor: 'project_id' },
    { header: 'Type', accessor: 'type' },
    { header: 'Zone', accessor: 'zone' },
    { header: 'Size (GB)', accessor: 'size_gb' },
    { header: 'Status', accessor: (item) => item.status || '' }
  ];

  return (
    <Sidebar showBackButton={true}>
      <div className="flex flex-col h-screen">
        <h1 className="text-3xl font-bold mb-6 text-[#3F9BB9]">
          Google Cloud
        </h1>
        <h1 className="text-2xl font-bold mb-6 text-[#3F9BB9]">
          Disks del Proyecto {projectDisplayName}
        </h1>
        <div className="w-full flex flex-col flex-grow">
          <div className="flex justify-between mb-4 px-6">
            <h1 className="text-2xl mb-6 text-[#3F9BB9]">Disks</h1>
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
                filename={`Disks_${projectDisplayName}`}
                title="Google Cloud Disks"
                subtitle={`Proyecto: ${projectDisplayName}`}
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
                <tr className="bg-gray-200 text-[#0B6A8D] font-semibold">
                  <td className="py-2 px-4 border-b border-gray-200 text-center">ID</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Name</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Project ID</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Type</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Zone</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Size (GB)</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Status</td>
                </tr>
              </thead>
              <tbody>
                {filteredData.length > 0 ? (
                  filteredData.map((item, index) => (
                    <tr key={index}>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.disk_id}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.name}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.project_id}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.type}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.zone}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.size_gb}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.status || ''}</td>
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

