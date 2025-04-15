import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import { saveAs } from 'file-saver';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

export default function SubnetworksAzure() {
  const [filter, setFilter] = useState('');
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const selectedSubscriptionId = sessionStorage.getItem('selectedSubscription');
        if (!selectedSubscriptionId) {
          console.error('No subscription selected.');
          setLoading(false);
          return;
        }

        const result = await fetch(`http://localhost:8080/api/cloud/subnetworksazure?subscriptionId=${selectedSubscriptionId}`);
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

  // Función para exportar a CSV
  const exportToCSV = () => {
    const headers = [
      'ID',
      'Name',
      'Network ID',
      'Subscription ID',
      'Address Prefix',
      'Private Endpoint Policies',
      'Private Link Policies',
      'Description'
    ];

    const csvRows = [
      headers,
      ...filteredData.map(item => [
        item.subnetwork_id,
        item.name,
        item.network_id,
        item.subscription_id,
        item.address_prefix,
        item.private_endpoint_network_policies,
        item.private_link_service_network_policies,
        item.description
      ])
    ];

    const csvContent = csvRows.map(e => e.join(',')).join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const subscriptionName = sessionStorage.getItem('selectedSubscriptionDisplayName') || 'Azure';
    saveAs(blob, `Azure_Subnetworks_${subscriptionName}_${new Date().toISOString().split('T')[0]}.csv`);
  };

  // Función para exportar a PDF
  const exportToPDF = () => {
    const doc = new jsPDF();
    const subscriptionName = sessionStorage.getItem('selectedSubscriptionDisplayName') || 'Azure';
    
    doc.text(`Azure Subnetworks - ${subscriptionName}`, 14, 15);
    
    autoTable(doc, {
      head: [['ID', 'Name', 'Network ID', 'Subscription ID', 'Address Prefix', 'Private Endpoint Policies', 'Private Link Policies', 'Description']],
      body: filteredData.map(item => [
        item.subnetwork_id,
        item.name,
        item.network_id,
        item.subscription_id,
        item.address_prefix,
        item.private_endpoint_network_policies,
        item.private_link_service_network_policies,
        item.description
      ]),
      startY: 20,
      styles: { fontSize: 8, cellPadding: 1 },
      columnStyles: {
        0: { cellWidth: 40 }
      },
      didDrawPage: (data) => {
        doc.text(`Date: ${new Date().toLocaleDateString()}`, 14, 10);
      }
    });
    
    doc.save(`Azure_Subnetworks_${subscriptionName}_${new Date().toISOString().split('T')[0]}.pdf`);
  };

  return (
    <Sidebar showBackButton={true}>
      <div className="flex flex-col h-screen">
        <div className="w-full flex flex-col flex-grow">
          <div className="flex justify-between mb-4 px-6">
            <h1 className="text-2xl text-[#0078D4] self-center">Azure Subnetworks</h1>
            <div className="flex space-x-4">
              <input
                placeholder="Filter"
                className="border-2 border-[#ccc] rounded-[5px] px-3 py-2 text-sm text-black"
                value={filter}
                onChange={handleInputChange}
              />
              <button
                onClick={exportToCSV}
                className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-700"
              >
                Export CSV
              </button>
              <button
                onClick={exportToPDF}
                className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-700"
              >
                Export PDF
              </button>
            </div>
          </div>
          
          {loading ? (
            <div className="flex justify-center items-center h-64">
              <div className="loader border-t-transparent border-blue-400 w-10 h-10 border-4 rounded-full animate-spin"></div>
            </div>
          ) : (
            <table className="min-w-full bg-white rounded-lg">
              <thead>
                <tr className="bg-gray-200 text-[#004E8C] font-semibold">
                  <td className="py-2 px-4 border-b border-gray-200 text-center">ID</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Name</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Network ID</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Subscription ID</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Address Prefix</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Private Endpoint Policies</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Private Link Policies</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Description</td>
                </tr>
              </thead>
              <tbody>
                {filteredData.length > 0 ? (
                  filteredData.map((item, index) => (
                    <tr key={index}>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.subnetwork_id}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.name}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.network_id}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.subscription_id}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.address_prefix}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.private_endpoint_network_policies}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.private_link_service_network_policies}</td>
                      <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.description}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="8" className="text-center py-4 text-gray-500">No data available</td>
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

