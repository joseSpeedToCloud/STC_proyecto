import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import ExportarDatosArchivos from '../../components/export_datos_archivos';

export default function NetworksGoogle() {
  const [filter, setFilter] = useState('');
  const [data, setData] = useState([]);
  const [projectDisplayName, setProjectDisplayName] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const networkColumns = [
    { header: 'ID', accessor: 'network_id' },
    { header: 'Name', accessor: 'name' },
    { header: 'Project ID', accessor: 'project_id' },
    { header: 'Auto Create Subnetworks', accessor: 'auto_create_subnetworks' },
    { header: 'MTU', accessor: 'mtu' },
    { header: 'Description', accessor: 'description' }
  ];

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const selectedProjectId = sessionStorage.getItem('selectedProject');
        const selectedProjectDisplayName = sessionStorage.getItem("selectedProjectDisplayName");
        
        if (!selectedProjectId) {
          setError('No se ha seleccionado un proyecto.');
          setLoading(false);
          return;
        }

        setProjectDisplayName(selectedProjectDisplayName);

        const result = await fetch(`http://localhost:8080/api/cloud/networksgoogle?projectId=${selectedProjectId}`);
        const jsonData = await result.json();
        setData(jsonData);
        setLoading(false);
      } catch (error) {
        console.error('Error al obtener los datos:', error);
        setError('Error al cargar los datos.');
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
        <div className="text-red-500 p-4">{error}</div>
      </Sidebar>
    );
  }

  return (
    <Sidebar showBackButton={true}>
      <div className="flex flex-col h-screen">
        <h1 className="text-3xl font-bold mb-6 text-[#3F9BB9]">
          Google Cloud
        </h1>
        <h1 className="text-2xl font-bold mb-6 text-[#3F9BB9]">
          Networks del Proyecto {projectDisplayName}
        </h1>
        <div className="w-full flex flex-col flex-grow">
          <div className="flex justify-between mb-4">
            <h1 className="text-2xl ml-6 text-[#3F9BB9]">Networks</h1>
            <div className="flex items-center space-x-4">
              <input
                placeholder="Filter"
                className="border-2 border-[#ccc] rounded-[5px] px-3 py-2 text-sm text-black"
                value={filter}
                onChange={handleInputChange}
              />
            </div>
          </div>
          <table className="min-w-full bg-white rounded-lg">
            <thead>
              <tr className="bg-gray-200 text-[#0B6A8D] font-semibold">
                <td className="py-2 px-4 border-b border-gray-200 text-center">ID</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Name</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Project ID</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Auto Create Subnetworks</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">MTU</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Description</td>
              </tr>
            </thead>
            <tbody>
              {filteredData.map((item, index) => (
                <tr key={index}>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.network_id}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.name}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.project_id}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.auto_create_subnetworks}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.mtu}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.description}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="mt-6">
            <ExportarDatosArchivos
              data={filteredData}
              columns={networkColumns}
              filename={`Networks_${projectDisplayName}`}
              title="Google Cloud Networks"
              subtitle={`Project: ${projectDisplayName}`}
            />
          </div>
        </div>
      </div>
    </Sidebar>
  );
}

