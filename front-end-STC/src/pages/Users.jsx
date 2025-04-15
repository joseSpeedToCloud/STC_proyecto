import React, { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import { useNavigate } from '@tanstack/react-router';
import axios from 'axios';

const Users = () => {
  const navigate = useNavigate();
  const [filter, setFilter] = useState('');
  const [users, setUsers] = useState([]);
  const [userRole, setUserRole] = useState('');
  const [error, setError] = useState('');
  const [currentUser, setCurrentUser] = useState('');
  const [loading, setLoading] = useState(true);

  // Cargar datos iniciales
  useEffect(() => {
    const role = localStorage.getItem('userRole');
    const username = localStorage.getItem('username');
    setUserRole(role);
    setCurrentUser(username);
    fetchUsers();
  }, []);

  // Obtener usuarios
  const fetchUsers = async () => {
    try {
      setLoading(true);
      const response = await axios.get('http://localhost:8080/api/cloud/users');
      setUsers(response.data);
      setError('');
    } catch (err) {
      setError('Error fetching users: ' + (err.response?.data || 'Unknown error'));
      console.error('Error:', err);
    } finally {
      setLoading(false);
    }
  };

  // Navegación a crear usuario
  const handleCreateClick = () => {
    navigate({ to: '/createUser' });
  };

  // Manejo del filtro
  const handleFilterChange = (event) => {
    setFilter(event.target.value);
  };

  // Filtrar usuarios
  const filteredUsers = users.filter((user) =>
    Object.values(user).some((value) =>
      String(value).toLowerCase().includes(filter.toLowerCase())
    )
  );

  // Formatear nombre completo
  const formatFullName = (user) => {
    // Si tiene apellido, combinarlo con el nombre de usuario
    if (user.lastname) {
      return `${user.username} ${user.lastname}`.trim();
    } else {
      // Si no hay apellido, mostrar solo el nombre de usuario
      return user.username || 'N/A';
    }
  };

  // Verificar permisos de edición
  const canManageUser = (targetRole) => {
    if (userRole === 'ADMINISTRADOR') return true;
    if (userRole === 'VIEWER' && targetRole === 'VIEWER') return true;
    return false;
  };

  // Manejar edición de usuario
  const handleEditUser = (user) => {
    if (userRole === 'VIEWER' && user.rol === 'ADMINISTRADOR') {
      setError('Viewers cannot edit administrators');
      return;
    }

    navigate({ 
      to: '/createUser',
      search: {
        mode: 'edit',
        username: user.username
      },
      state: { 
        isEdit: true,
        userData: user
      }
    });
  };

  // Manejar eliminación de usuario
  const handleDeleteUser = async (username) => {
    // Prevenir eliminación del usuario actual
    if (username === currentUser) {
      setError('You cannot delete your own account');
      return;
    }

    // Verificar permisos basados en roles
    const targetUser = users.find(user => user.username === username);
    if (userRole === 'VIEWER' && targetUser.rol === 'ADMINISTRADOR') {
      setError('Viewers cannot delete administrators');
      return;
    }

    if (window.confirm('Are you sure you want to delete this user?')) {
      try {
        await axios.delete(`http://localhost:8080/api/cloud/users/${username}`, {
          headers: {
            'User-Role': userRole
          }
        });
        await fetchUsers(); // Recargar lista de usuarios
        setError('');
      } catch (err) {
        setError('Error deleting user: ' + (err.response?.data || 'Unknown error'));
        console.error('Error:', err);
      }
    }
  };

  // Manejar cierre de error
  const handleCloseError = () => {
    setError('');
  };

  return (
    <Sidebar showBackButton={true}>
      <div className="flex flex-col h-full p-6">
        {/* Header con botón de crear */}
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl text-[#3F9BB9]">Users Management</h1>
          <button 
            className="bg-[#3F9BB9] text-white px-4 py-2 rounded hover:bg-[#2D7A98] transition"
            onClick={handleCreateClick}
          >
            Create New User
          </button>
        </div>

        {/* Mensaje de error */}
        {error && (
          <div className="bg-red-50 border-l-4 border-red-500 text-red-700 p-4 mb-4" role="alert">
            <div className="flex justify-between items-start">
              <div>
                <p className="font-bold">Error</p>
                <p>{error}</p>
              </div>
              <button 
                onClick={handleCloseError}
                className="text-red-500 hover:text-red-700"
              >
                ×
              </button>
            </div>
          </div>
        )}

        {/* Filtro de búsqueda */}
        <div className="mb-6">
          <input
            type="text"
            placeholder="Search users..."
            className="w-full md:w-64 px-4 py-2 border-2 border-[#ccc] rounded"
            value={filter}
            onChange={handleFilterChange}
          />
        </div>

        {/* Tabla de usuarios */}
        {loading ? (
          <div className="flex justify-center items-center h-64">
            <p className="text-gray-600">Loading users...</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full bg-white rounded-lg shadow">
              <thead>
                <tr className="bg-gray-100 text-[#0B6A8D]">
                  <th className="py-3 px-4 text-left">Username</th>
                  <th className="py-3 px-4 text-left">Full Name</th>
                  <th className="py-3 px-4 text-left">Role</th>
                  <th className="py-3 px-4 text-left">Department</th>
                  <th className="py-3 px-4 text-left">Email</th>
                  <th className="py-3 px-4 text-center">Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredUsers.length === 0 ? (
                  <tr>
                    <td colSpan="6" className="text-center py-4 text-gray-500">
                      No users found
                    </td>
                  </tr>
                ) : (
                  filteredUsers.map((user) => (
                    <tr 
                      key={user.username}
                      className="border-b border-gray-200 hover:bg-gray-50"
                    >
                      <td className="py-3 px-4">{user.username}</td>
                      <td className="py-3 px-4">{formatFullName(user)}</td>
                      <td className="py-3 px-4">
                        <span className={`px-2 py-1 rounded-full text-sm ${
                          user.rol === 'ADMINISTRADOR' 
                            ? 'bg-blue-100 text-blue-800' 
                            : 'bg-gray-100 text-gray-800'
                        }`}>
                          {user.rol}
                        </span>
                      </td>
                      <td className="py-3 px-4">{user.department || '-'}</td>
                      <td className="py-3 px-4">{user.email || '-'}</td>
                      <td className="py-3 px-4 text-center">
                        {canManageUser(user.rol) && (
                          <div className="flex justify-center space-x-2">
                            <button
                              onClick={() => handleEditUser(user)}
                              className="bg-blue-500 text-white px-3 py-1 rounded hover:bg-blue-600 transition"
                            >
                              Edit
                            </button>
                            {user.username !== currentUser && (
                              <button
                                onClick={() => handleDeleteUser(user.username)}
                                className="bg-red-500 text-white px-3 py-1 rounded hover:bg-red-600 transition"
                              >
                                Delete
                              </button>
                            )}
                          </div>
                        )}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </Sidebar>
  );
};

export default Users;

