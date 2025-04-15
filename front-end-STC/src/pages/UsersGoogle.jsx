import React, { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';

export default function UsersGoogle() {
  const [filter, setFilter] = useState('');
  const [data, setData] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        //poner el puerto de la BBDD que utiliza el back
        const result = await fetch('http://localhost:8080/api/cloud/usersgoogle');
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

  return (
    <Sidebar showBackButton={true}>
      <div className="flex flex-col h-screen">
        <div className="w-full flex flex-col flex-grow">
          <div className="flex justify-end mb-4 space-x-2">
            <input
              placeholder="Filter"
              className="border-2 border-[#ccc] rounded-[5px] px-3 py-2 text-sm text-black"
              value={filter}
              onChange={handleInputChange}
            />
          </div>
          <h1 className="text-2xl mb-6 ml-6 text-[#3F9BB9]">Users</h1>
          <table className="min-w-full bg-white rounded-lg">
            <thead>
              <tr className="bg-gray-200 text-[#0B6A8D] font-semibold">
                <td className="py-2 px-4 border-b border-gray-200 text-center">ID</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Username</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Full Name</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Rol</td>
              </tr>
            </thead>
            <tbody>
              {filteredData.map((item, index) => (
                <tr key={index}>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.id}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.username}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{`${item.username} ${item.lastname || ''}`}</td>
                  <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{item.rol}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </Sidebar>
  );
};

