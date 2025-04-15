import React, { useState } from 'react';
import { saveAs } from 'file-saver';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

const ExportarDatosArchivos = ({ 
  data, 
  columns, 
  filename, 
  title,
  subtitle = ''
}) => {
  const [pdfOrientation, setPdfOrientation] = useState('portrait');
  
  // Function to export to CSV
  const exportToCSV = () => {
    // Extract headers from columns
    const headers = columns.map(col => col.header);
    
    // Generate CSV rows
    const csvRows = [
      headers,
      ...data.map(item => columns.map(col => 
        typeof col.accessor === 'function' 
          ? col.accessor(item) 
          : item[col.accessor] !== undefined 
            ? item[col.accessor] 
            : ''
      ))
    ];
    
    const csvContent = csvRows.map(e => e.join(',')).join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    saveAs(blob, `${filename}_${new Date().toISOString().split('T')[0]}.csv`);
  };
  
  // Function to export to PDF
  const exportToPDF = () => {
    const doc = new jsPDF({ orientation: pdfOrientation });
    
    // Add title
    doc.text(title, 14, 15);
    
    // Add subtitle if provided
    if (subtitle) {
      doc.text(subtitle, 14, 22);
    }
    
    // Generate table headers and body
    const headers = columns.map(col => col.header);
    const body = data.map(item => 
      columns.map(col => 
        typeof col.accessor === 'function' 
          ? col.accessor(item) 
          : item[col.accessor] !== undefined 
            ? item[col.accessor] 
            : ''
      )
    );
    
    autoTable(doc, {
      head: [headers],
      body: body,
      startY: subtitle ? 25 : 20,
      styles: { fontSize: 8, cellPadding: 1 },
      didDrawPage: (data) => {
        doc.text(`Date: ${new Date().toLocaleDateString()}`, 14, 10);
      }
    });
    
    doc.save(`${filename}_${new Date().toISOString().split('T')[0]}.pdf`);
  };
  
  return (
    <div className="flex flex-col sm:flex-row gap-4 items-end">
      <div>
        <label className="block mb-1 font-medium text-gray-700">PDF Orientation</label>
        <select
          value={pdfOrientation}
          onChange={(e) => setPdfOrientation(e.target.value)}
          className="px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-yellow-500"
        >
          <option value="portrait">Vertical (Portrait)</option>
          <option value="landscape">Horizontal (Landscape)</option>
        </select>
      </div>
      <button
        onClick={exportToPDF}
        className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-700"
      >
        Export to PDF
      </button>
      <button
        onClick={exportToCSV}
        className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-700"
      >
        Export to CSV
      </button>
    </div>
  );
};

export default ExportarDatosArchivos;



