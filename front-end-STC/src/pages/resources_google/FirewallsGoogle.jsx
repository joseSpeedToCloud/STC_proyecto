import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import ExportarDatosArchivos from '../../components/export_datos_archivos';

export default function FirewallsGoogle() {
  const [filter, setFilter] = useState('');
  const [data, setData] = useState([]);
  const [filteredData, setFilteredData] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [projectDisplayName, setProjectDisplayName] = useState('');

  const firewallColumns = [
    { header: 'ID', accessor: 'firewall_id' },
    { header: 'Name', accessor: 'name' },
    { header: 'Project ID', accessor: 'project_id' },
    { header: 'Direction', accessor: 'direction' },
    { header: 'Priority', accessor: 'priority' },
    { header: 'Protocol', accessor: 'protocol' },
    { header: 'Source Ranges', accessor: 'source_ranges' },
    { header: 'Target Tags', accessor: 'target_tags' }
  ];

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);

        // Obtener el project_id seleccionado del sessionStorage
        const selectedProjectId = sessionStorage.getItem("selectedProject");
        const selectedProjectDisplayName = sessionStorage.getItem("selectedProjectDisplayName");

        if (!selectedProjectId) {
          setError("No se ha seleccionado un proyecto.");
          setLoading(false);
          return;
        }

        setProjectDisplayName(selectedProjectDisplayName);

        // Obtener los datos del backend
        const result = await fetch('http://localhost:8080/api/cloud/firewallsgoogle');
        const jsonData = await result.json();

        // Filtrar los datos por project_id
        const filteredByProjectId = jsonData.filter(
          (item) => item.project_id === selectedProjectId
        );

        setData(filteredByProjectId);
        setFilteredData(filteredByProjectId); // Inicializar la tabla con los datos filtrados
        setLoading(false);
      } catch (error) {
        console.error('Error al obtener los datos:', error);
        setError("Error al cargar los datos.");
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const handleInputChange = (event) => {
    const value = event.target.value;
    setFilter(value);

    // Filtrar los datos por el término de búsqueda
    const filtered = data.filter((item) =>
      Object.values(item).some((val) =>
        String(val).toLowerCase().includes(value.toLowerCase())
      )
    );
    setFilteredData(filtered);
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
          Firewalls del Proyecto {projectDisplayName}
        </h1>
        <div className="w-full flex flex-col flex-grow">
          <div className="flex justify-between mb-4">
            <h1 className="text-2xl ml-6 text-[#3F9BB9]">Firewalls</h1>
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
                <td className="py-2 px-4 border-b border-gray-200 text-center">Direction</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Priority</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Protocol</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Source Ranges</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Target Tags</td>
              </tr>
            </thead>
            <tbody>
              {filteredData.map((item, index) => (
                <tr key={index}>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.firewall_id}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.name}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.project_id}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.direction}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.priority}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.protocol}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.source_ranges}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.target_tags}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="mt-6">
            <ExportarDatosArchivos
              data={filteredData}
              columns={firewallColumns}
              filename={`Firewalls_${projectDisplayName}`}
              title="Google Cloud Firewalls"
              subtitle={`Project: ${projectDisplayName}`}
            />
          </div>
        </div>
      </div>
    </Sidebar>
  );
}

