import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import ExportarDatosArchivos from '../../components/export_datos_archivos';

export default function SnapshotsGoogle() {
  const [filter, setFilter] = useState('');
  const [data, setData] = useState([]);
  const [projectDisplayName, setProjectDisplayName] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      try {
        const selectedProjectId = sessionStorage.getItem('selectedProject');
        const selectedProjectDisplayName = sessionStorage.getItem("selectedProjectDisplayName");
        
        if (!selectedProjectId) {
          console.error('No se ha seleccionado un proyecto.');
          return;
        }

        setProjectDisplayName(selectedProjectDisplayName);

        const result = await fetch(`http://localhost:8080/api/cloud/snapshotsgoogle?projectId=${selectedProjectId}`);
        const jsonData = await result.json();
        setData(jsonData);
      } catch (error) {
        console.error('Error al obtener los datos:', error);
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
    { header: 'ID', accessor: 'id' },
    { header: 'Name', accessor: 'name' },
    { header: 'Project ID', accessor: 'project_id' },
    { header: 'Source Disk', accessor: 'source_disk' },
    { header: 'Disk Size (GB)', accessor: 'disk_size_gb' },
    { header: 'Status', accessor: 'status' },
    { header: 'Storage Locations', accessor: 'storage_locations' }
  ];

  return (
    <Sidebar showBackButton={true}>
      <div className="flex flex-col h-screen">
        <h1 className="text-3xl font-bold mb-6 text-[#3F9BB9]">
          Google Cloud
        </h1>
        <h1 className="text-2xl font-bold mb-6 text-[#3F9BB9]">
          Snapshots del Proyecto {projectDisplayName}
        </h1>
        <div className="w-full flex flex-col flex-grow">
          <div className="flex justify-between mb-4">
            <h1 className="text-2xl ml-6 text-[#3F9BB9]">Snapshots</h1>
            <input
              placeholder="Filter"
              className="border-2 border-[#ccc] rounded-[5px] px-3 py-2 text-sm text-black"
              value={filter}
              onChange={handleInputChange}
            />
          </div>
          <table className="min-w-full bg-white rounded-lg">
            <thead>
              <tr className="bg-gray-200 text-[#0B6A8D] font-semibold">
                <td className="py-2 px-4 border-b border-gray-200 text-center">ID</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Name</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Project ID</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Source Disk</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Disk Size (GB)</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Status</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Storage Locations</td>
              </tr>
            </thead>
            <tbody>
              {filteredData.map((item, index) => (
                <tr key={index}>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.id}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.name}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.project_id}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.source_disk}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.disk_size_gb}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.status}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.storage_locations}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="mt-6 p-4">
            <ExportarDatosArchivos
              data={filteredData}
              columns={columns}
              filename={`Snapshots_${projectDisplayName}`}
              title="Google Cloud Snapshots Report"
              subtitle={`Project: ${projectDisplayName}`}
            />
          </div>
        </div>       
      </div>
    </Sidebar>
  );
}

