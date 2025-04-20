import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import ExportarDatosArchivos from '../../components/export_datos_archivos';

export default function LoadBalancersGoogle() {
  const [filter, setFilter] = useState('');
  const [data, setData] = useState([]);
  const [projectDisplayName, setProjectDisplayName] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadBalancerColumns = [
    { header: 'ID', accessor: 'load_balancer_id' },
    { header: 'Name', accessor: 'name' },
    { header: 'Project ID', accessor: 'project_id' },
    { header: 'Region', accessor: 'region' },
    { header: 'Load Balancing Scheme', accessor: 'load_balancing_scheme' },
    { header: 'Network Tier', accessor: 'network_tier' },
    { header: 'Type', accessor: 'type' },
    { header: 'IP Address', accessor: 'ip_address' },
    { header: 'Port', accessor: 'port' },
    { header: 'Status', accessor: 'status' },
    { header: 'Target', accessor: 'target' },
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

        const result = await fetch(`http://localhost:8080/api/cloud/loadbalancersgoogle?projectId=${selectedProjectId}`);
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
          Load Balancers del Proyecto {projectDisplayName}
        </h1>
        <div className="w-full flex flex-col flex-grow">
          <div className="flex justify-between mb-4">
            <h1 className="text-2xl ml-6 text-[#3F9BB9]">Load Balancers</h1>
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
                <td className="py-2 px-4 border-b border-gray-200 text-center">Region</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Load Balancing Scheme</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Network Tier</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Type</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">IP Address</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Port</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Status</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Target</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Description</td>
              </tr>
            </thead>
            <tbody>
              {filteredData.map((item, index) => (
                <tr key={index}>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.load_balancer_id}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.name}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.project_id}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.region}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.load_balancing_scheme}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.network_tier}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.type}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.ip_address}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.port}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.status}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.target}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.description}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="mt-6">
            <ExportarDatosArchivos
              data={filteredData}
              columns={loadBalancerColumns}
              filename={`Load_Balancers_${projectDisplayName}`}
              title="Google Cloud Load Balancers"
              subtitle={`Project: ${projectDisplayName}`}
            />
          </div>
        </div>
      </div>
    </Sidebar>
  );
}

