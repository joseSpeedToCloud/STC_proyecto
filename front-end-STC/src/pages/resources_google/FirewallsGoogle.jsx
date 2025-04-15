import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import { saveAs } from 'file-saver';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

export default function FirewallsGoogle() {
  const [filter, setFilter] = useState('');
  const [data, setData] = useState([]);
  const [filteredData, setFilteredData] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [projectDisplayName, setProjectDisplayName] = useState(''); // Estado para el nombre del proyecto
  

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

        setProjectDisplayName(selectedProjectDisplayName); // Establece el nombre del proyecto en el estado

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

  const exportToCSV = () => {
    const csvRows = [
      ['ID', 'Name', 'Project ID', 'Direction', 'Priority', 'Protocol', 'Source Ranges', 'Target Tags'],
      ...filteredData.map(item => [
        item.firewall_id, item.name, item.project_id, item.direction, item.priority, item.protocol, item.source_ranges, item.target_tags || ''
      ])
    ];
      const csvContent = csvRows.map(e => e.join(',')).join('\n');
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      saveAs(blob, `Firewalls_${projectDisplayName}.csv`);
    };
  
    const exportToPDF = () => {
      const doc = new jsPDF();
      doc.text(`Firewalls del Proyecto: ${projectDisplayName}`, 10, 10);
      autoTable(doc, {
        head: [['ID', 'Name', 'Project ID', 'Direction', 'Priority', 'Protocol', 'Source Ranges', 'Target Tags']],
        body: filteredData.map(item => [
          item.firewall_id, item.name, item.project_id, item.direction, item.priority, item.protocol, item.source_ranges, item.target_tags || ''
        ]),
      });
      doc.save(`Firewalls_${projectDisplayName}.pdf`);
    };

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
          <div className="flex justify-end mb-4 space-x-2">
            <input
              placeholder="Filter"
              className="border-2 border-[#ccc] rounded-[5px] px-3 py-2 text-sm text-black"
              value={filter}
              onChange={handleInputChange}
            />
          </div>
          <h1 className="text-2xl mb-6 ml-6 text-[#3F9BB9]">Firewalls</h1>
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
}