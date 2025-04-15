import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import { saveAs } from 'file-saver';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

export default function VirtualMachinesAzure() {
  const [filter, setFilter] = useState('');
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandedRows, setExpandedRows] = useState({});

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

        const result = await fetch(`http://localhost:8080/api/cloud/virtualmachinesazure?subscriptionId=${selectedSubscriptionId}`);
        const jsonData = await result.json();
        
        // Fetch additional details for each VM
        const detailedData = await Promise.all(
          jsonData.map(async (vm) => {
            try {
              const detailResponse = await fetch(`http://localhost:8080/api/cloud/subscription/${selectedSubscriptionId}`);
              const subscriptionDetails = await detailResponse.json();
              
              // Find the specific VM in the subscription details
              const vmDetails = subscriptionDetails.virtual_machines.find(
                (v) => v.machine_id === vm.machine_id
              );
              
              return { 
                ...vm, 
                ...vmDetails,
                resourceGroup: vm.resource_group || 'N/A',
                additionalDetails: vmDetails?.details || 'No additional details'
              };
            } catch (error) {
              console.error(`Error fetching details for VM ${vm.machine_id}:`, error);
              return vm;
            }
          })
        );
        
        setData(detailedData);
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

  const toggleRowExpand = (machineId) => {
    setExpandedRows(prev => ({
      ...prev,
      [machineId]: !prev[machineId]
    }));
  };

  const filteredData = data.filter((item) =>
    Object.values(item).some((value) =>
      String(value).toLowerCase().includes(filter.toLowerCase())
    )
  );

  // Function to export to CSV
  const exportToCSV = () => {
    const headers = [
      'ID', 'Name', 'Resource Group', 'Subscription ID', 
      'VM Size', 'Location', 'Status', 'Additional Details'
    ];

    const csvRows = [
      headers,
      ...filteredData.map(item => [
        item.machine_id,
        item.name,
        item.resourceGroup,
        item.subscription_id,
        item.machine_type || item.vm_size,
        item.zone || item.location,
        item.status,
        item.additionalDetails
      ])
    ];

    const csvContent = csvRows.map(e => e.join(',')).join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const subscriptionName = sessionStorage.getItem('selectedSubscriptionDisplayName') || 'Azure';
    saveAs(blob, `Azure_VirtualMachines_${subscriptionName}_${new Date().toISOString().split('T')[0]}.csv`);
  };

  // Function to export to PDF
  const exportToPDF = () => {
    const doc = new jsPDF();
    const subscriptionName = sessionStorage.getItem('selectedSubscriptionDisplayName') || 'Azure';
    
    doc.text(`Azure Virtual Machines - ${subscriptionName}`, 14, 15);
    
    autoTable(doc, {
      head: [['ID', 'Name', 'Resource Group', 'Subscription ID', 'VM Size', 'Location', 'Status', 'Additional Details']],
      body: filteredData.map(item => [
        item.machine_id,
        item.name,
        item.resourceGroup,
        item.subscription_id,
        item.machine_type || item.vm_size,
        item.zone || item.location,
        item.status,
        item.additionalDetails
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
    
    doc.save(`Azure_VirtualMachines_${subscriptionName}_${new Date().toISOString().split('T')[0]}.pdf`);
  };

  return (
    <Sidebar showBackButton={true}>
      <div className="flex flex-col h-screen">
        <div className="w-full flex flex-col flex-grow">
          <div className="flex justify-between mb-4 px-6">
            <h1 className="text-2xl text-[#0078D4] self-center">Azure Virtual Machines</h1>
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
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Resource Group</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Subscription ID</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">VM Size</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Location</td>
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Status</td>
                </tr>
              </thead>
              <tbody>
                {filteredData.length > 0 ? (
                  filteredData.map((item) => (
                    <React.Fragment key={item.machine_id}>
                      <tr>
                        <td className="text-sm text-center py-2 px-4 border-b border-gray-200">
                          {item.machine_id.split('-')[0]}
                        </td>
                        <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.name}</td>
                        <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.resourceGroup}</td>
                        <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.subscription_id}</td>
                        <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.machine_type || item.vm_size}</td>
                        <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.zone || item.location}</td>
                        <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.status}</td>
                      </tr>
                    </React.Fragment>
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