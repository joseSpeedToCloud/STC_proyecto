import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from '@tanstack/react-router';
import Sidebar from '../../components/Sidebar';
import axios from 'axios';
import googleLogo from '../../assets/google.svg';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { saveAs } from 'file-saver';

const HomeGoogle = () => {
  const [extractedProjects, setExtractedProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [needsSync, setNeedsSync] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    // Check authentication and sync status
    const token = sessionStorage.getItem('gcpAccessToken');
    const selectedProject = sessionStorage.getItem('selectedProject');

    // No token - stay on this page with sign in option
    if (!token) {
      setLoading(false);
      return;
    }

    // Token exists but no project selected - show need for sync
    if (token && !selectedProject) {
      setNeedsSync(true);
      setLoading(false);
      return;
    }

    const fetchProjects = async () => {
      try {
        // Validate token is still valid
        await axios.post(
          "http://localhost:8080/api/cloud/validate-token",
          { token }
        );
        
        // Fetch projects
        const response = await axios.get('http://localhost:8080/api/cloud/projectsgoogle', {
          headers: { 'Authorization': `Bearer ${token}` },
        });
        
        setExtractedProjects(response.data);
        setLoading(false);
      } catch (err) {
        console.error('Authentication or fetch error:', err);
        
        // If token validation fails, clear session and redirect to login
        if (err.response?.status === 401) {
          sessionStorage.removeItem('gcpAccessToken');
          sessionStorage.removeItem('gcpEmail');
          sessionStorage.removeItem('selectedProject');
          sessionStorage.removeItem('selectedProjectDisplayName');
          setLoading(false);
          return;
        }
        
        setError('Error fetching projects. Please try again.');
        setLoading(false);
      }
    };

    if (token && selectedProject) {
      fetchProjects();
    }
  }, [navigate]);

  const handleProjectClick = (project) => {
    sessionStorage.setItem('selectedProject', project.id);
    sessionStorage.setItem('selectedProjectDisplayName', project.display_name || project.name);
    navigate({ to: '/projectsDetails' });
  };

  // Función para exportar a CSV
  const exportToCSV = () => {
    const headers = [
      'Project ID',
      'Name',
      'Organization'
    ];

    const csvRows = [
      headers,
      ...extractedProjects.map(project => [
        project.id,
        project.name,
        project.organization?.name || 'No organization'
      ])
    ];

    const csvContent = csvRows.map(e => e.join(',')).join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    saveAs(blob, `Google_Cloud_Projects_${new Date().toISOString().split('T')[0]}.csv`);
  };

  // Función para exportar a PDF
  const exportToPDF = () => {
    const doc = new jsPDF();
    
    doc.text('Google Cloud Projects List', 10, 10);
    
    autoTable(doc, {
      head: [['Project ID', 'Name', 'Organization']],
      body: extractedProjects.map(project => [
        project.id,
        project.name,
        project.organization?.name || 'No organization'
      ]),
    });
    
    doc.save(`Google_Cloud_Projects_${new Date().toISOString().split('T')[0]}.pdf`);
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

  // Mostrar pantalla de inicio de sesión simplificada si no hay token
  const token = sessionStorage.getItem('gcpAccessToken');
  if (!token) {
    return (
      <Sidebar showBackButton={true}>
        <div className="p-6 flex flex-col items-center justify-center h-full">
          <div className="max-w-md text-center bg-white p-8 rounded-lg shadow-lg">
            <h2 className="text-2xl font-bold text-[#4285F4] mb-4">Google Cloud Platform</h2>
            <p className="text-gray-600 mb-6">Sign in to view your Google Cloud projects</p>
            <button
              onClick={() => navigate({ to: '/googleAuth' })}
              className="px-6 py-3 bg-[#4285F4] text-white rounded-lg hover:bg-[#3367D6] transition-colors"
            >
              Sign in to Google Cloud
            </button>
          </div>
        </div>
      </Sidebar>
    );
  }

  // Show sync required message if token exists but no project has been selected
  if (needsSync) {
    return (
      <Sidebar showBackButton={true}>
        <div className="p-6 flex flex-col items-center justify-center h-full">
          <div className="max-w-md text-center bg-white p-8 rounded-lg shadow-lg">
            <h2 className="text-2xl font-bold text-[#4285F4] mb-4">Project Sync Required</h2>
            <p className="text-gray-600 mb-6">You need to complete the project sync process before accessing Google Cloud resources</p>
            <button
              onClick={() => navigate({ to: '/syncproject' })}
              className="px-6 py-3 bg-[#4285F4] text-white rounded-lg hover:bg-[#3367D6] transition-colors"
            >
              Complete Project Sync
            </button>
          </div>
        </div>
      </Sidebar>
    );
  }

  return (
    <Sidebar showBackButton={true}>
      <div className="p-6">
        <div className="flex justify-between mb-4">
          <h1 className="text-2xl font-bold text-[#4285F4]">Google Cloud Projects</h1>
          
          {extractedProjects.length > 0 && (
            <div className="flex space-x-4">
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
          )}
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {extractedProjects.length > 0 ? (
            extractedProjects.map((project) => (
              <div
                key={project.id}
                className="relative border rounded-lg p-4 shadow-md hover:shadow-lg transition-shadow cursor-pointer"
                onClick={() => handleProjectClick(project)}
              >
                <img src={googleLogo} alt="Google" className="absolute top-2 right-2 w-6 h-6" />
                <h2 className="text-xl font-semibold text-[#4285F4]">{project.name}</h2>
                <p className="text-gray-600">Project ID: {project.id}</p>
                {project.organization && (
                  <p className="text-gray-500 text-sm">
                    Organization: {project.organization.name}
                  </p>
                )}
              </div>
            ))
          ) : error ? (
            <div className="col-span-3 border rounded-lg p-4 shadow-md bg-red-50 text-red-600">
              {error}
              <div className="mt-2">
                <button
                  onClick={() => navigate({ to: '/googleAuth' })}
                  className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                >
                  Sign in to Google Cloud
                </button>
              </div>
            </div>
          ) : (
            <div
              className="col-span-3 relative border rounded-lg p-4 shadow-md hover:shadow-lg transition-shadow cursor-pointer flex items-center justify-center bg-gray-100"
              onClick={() => navigate({ to: '/syncproject' })}
            >
              <span className="text-xl font-semibold text-gray-500 text-center">
                No projects found. Click here to sync a project.
              </span>
            </div>
          )}
          
          {extractedProjects.length > 0 && (
            <Link
              to="/syncproject"
              className="border rounded-lg p-4 shadow-md hover:shadow-lg transition-shadow cursor-pointer flex items-center justify-center"
            >
              <span className="text-4xl text-[#4285F4] font-bold">+</span>
            </Link>
          )}
        </div>
      </div>
    </Sidebar>
  );
};

export default HomeGoogle;
