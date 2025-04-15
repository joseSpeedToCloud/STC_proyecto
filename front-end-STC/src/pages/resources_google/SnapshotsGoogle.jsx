import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import { saveAs } from 'file-saver';
import jsPDF from 'jspdf';
import 'jspdf-autotable';

export default function SnapshotsGoogle() {
  const [filter, setFilter] = useState('');
  const [data, setData] = useState([]);
  const [projectDisplayName, setProjectDisplayName] = useState(''); // Estado para el nombre del proyecto

  useEffect(() => {
    const fetchData = async () => {
      try {
        const selectedProjectId = sessionStorage.getItem('selectedProject');
        const selectedProjectDisplayName = sessionStorage.getItem("selectedProjectDisplayName");
        
        if (!selectedProjectId) {
          console.error('No se ha seleccionado un proyecto.');
          return;
        }

        setProjectDisplayName(selectedProjectDisplayName); // Establece el nombre del proyecto en el estado

        const result = await fetch(`http://localhost:8080/api/cloud/snapshotsgoogle?projectId=${selectedProjectId}`);
        const jsonData = await result.json();
        setData(jsonData);
      } catch (error) {
        console.error('Error al obtener los datos:', error);
      }
    };
    fetchData();
  }, []);

      const exportToCSV = () => {
        const csvRows = [
          ['ID', 'Name', 'Project ID', 'Source Disk', 'Disk Size (GB)', 'Status', 'Storage Locations'],
          ...filteredData.map(item => [
            item.snapshot_id, item.name, item.project_id, item.source_disk, item.disk_size_gb, item.status, item.storage_locations || ''
          ])
        ];
          const csvContent = csvRows.map(e => e.join(',')).join('\n');
          const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
          saveAs(blob, `Snapshots_${projectDisplayName}.csv`);
        };
      
        const exportToPDF = () => {
          const doc = new jsPDF();
          doc.text(`Snapshots del Proyecto: ${projectDisplayName}`, 10, 10);
          doc.autoTable({
            head: [['ID', 'Name', 'Project ID', 'Source Disk', 'Disk Size (GB)', 'Status', 'Storage Locations']],
            body: filteredData.map(item => [
              item.snapshot_id, item.name, item.project_id, item.source_disk, item.disk_size_gb, item.status, item.storage_locations || ''
            ]),
          });
          doc.save(`Snapshots_${projectDisplayName}.pdf`);
        };

  const handleInputChange = (event) => {
    setFilter(event.target.value);
  };

  const filteredData = data.filter((item) =>
    Object.values(item).some((value) =>
      String(value).toLowerCase().includes(filter.toLowerCase())
    )
  );

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
          <div className="flex justify-end mb-4 space-x-2">
            <input
              placeholder="Filter"
              className="border-2 border-[#ccc] rounded-[5px] px-3 py-2 text-sm text-black"
              value={filter}
              onChange={handleInputChange}
            />
          </div>
          <h1 className="text-2xl mb-6 ml-6 text-[#3F9BB9]">Snapshots</h1>
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
          <div className="mt-6 flex space-x-4">
          <button
            onClick={exportToCSV}
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-700"
          >
            Exportar a CSV
          </button>
          <button
            onClick={exportToPDF}
            className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-700"
          >
            Exportar a PDF
          </button>
        </div>
        </div>       
      </div>
    </Sidebar>
  );
};


