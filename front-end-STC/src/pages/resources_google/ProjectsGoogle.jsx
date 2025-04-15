import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import { useNavigate } from '@tanstack/react-router';

export default function ProjectsGoogle() {
  const [filter, setFilter] = useState('');
  const [data, setData] = useState([]);
  const [searchType, setSearchType] = useState('name'); // 'name' or 'id'
  const [selectedProject, setSelectedProject] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchData = async () => {
      try {
        let url = 'http://localhost:8080/api/cloud/projectsgoogle';
        if (filter && searchType === 'id') {
          url += `?projectId=${filter}`;
        }
        
        const result = await fetch(url);
        const jsonData = await result.json();
        setData(jsonData);
      } catch (error) {
        console.error('Error al obtener los datos:', error);
      }
    };
    
    fetchData();
  }, [filter, searchType]);

  const handleInputChange = (event) => {
    setFilter(event.target.value);
  };

  const handleSearchTypeChange = (type) => {
    setSearchType(type);
    setFilter('');
    setData([]);
  };

  const handleProjectClick = (projectId) => {
    navigate({
      to: "/project-details",
      search: { projectId }
    });
  };

  return (
    <Sidebar showBackButton={true}>
      <div className="flex flex-col h-screen">
        <div className="w-full flex flex-col flex-grow">
          <div className="flex justify-between mb-4 space-x-2">
            <div className="flex space-x-2">
              <button
                onClick={() => handleSearchTypeChange('name')}
                className={`px-4 py-2 rounded ${searchType === 'name' ? 'bg-blue-500 text-white' : 'bg-gray-200'}`}
              >
                Buscar por Nombre
              </button>
              <button
                onClick={() => handleSearchTypeChange('id')}
                className={`px-4 py-2 rounded ${searchType === 'id' ? 'bg-blue-500 text-white' : 'bg-gray-200'}`}
              >
                Buscar por ID
              </button>
            </div>
            <input
              placeholder={searchType === 'name' ? "Filter by name" : "Enter Project ID"}
              className="border-2 border-[#ccc] rounded-[5px] px-3 py-2 text-sm text-black"
              value={filter}
              onChange={handleInputChange}
            />
          </div>
          <h1 className="text-2xl mb-6 ml-6 text-[#3F9BB9]">Projects</h1>
          <table className="min-w-full bg-white rounded-lg">
            <thead>
              <tr className="bg-gray-200 text-[#0B6A8D] font-semibold">
                <td className="py-2 px-4 border-b border-gray-200 text-center">ID</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Name</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Display Name</td>
                {searchType === 'name' && (
                  <td className="py-2 px-4 border-b border-gray-200 text-center">Organization ID</td>
                )}
              </tr>
            </thead>
            <tbody>
              {data.map((item, index) => (
                <tr 
                  key={index} 
                  className="hover:bg-gray-100 cursor-pointer"
                  onClick={() => handleProjectClick(item.id)}
                >
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.id}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.name}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.display_name}</td>
                  {searchType === 'name' && (
                    <td className="text-sm text-center py-2 px-4 border-b border-gray-200">
                      {item.organization?.organization_id || item.organization_id || 'N/A'}
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
          {selectedProject && (
            <div className="mt-4 p-4 bg-gray-100 rounded">
              <h3 className="font-bold">Project Details</h3>
              <pre>{JSON.stringify(selectedProject, null, 2)}</pre>
            </div>
          )}
        </div>       
      </div>
    </Sidebar>
  );
};


