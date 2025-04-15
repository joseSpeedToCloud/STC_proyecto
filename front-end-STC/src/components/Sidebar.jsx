import React, { useState, useEffect } from 'react';
import logo from '../assets/logo.png';
import user from '../assets/user.svg';
import SidebarData from '../data/SidebarData';
import { Link, useNavigate, useRouter } from '@tanstack/react-router';

const Sidebar = ({ children }) => {
  const [showSidebar, setShowSidebar] = useState(false);
  const [showDropdown, setShowDropdown] = useState(false);
  const [activeMenu, setActiveMenu] = useState(null);
  const [username, setUsername] = useState('Usuario');
  const [role, setRole] = useState('');
  const [authConfigured, setAuthConfigured] = useState(false); // Estado para verificar si hay configuración de autenticación
  const navigate = useNavigate();
  const router = useRouter();

  // Cargar rol del usuario y verificar configuración de autenticación
  useEffect(() => {
    const userRole = localStorage.getItem('userRole');
    const storedUsername = localStorage.getItem('username');
    // Verificar si hay configuración de autenticación guardada
    const hasAuthConfig = localStorage.getItem('authConfig');
    
    setRole(userRole || '');
    setUsername(storedUsername || 'Usuario');
    setAuthConfigured(!!hasAuthConfig); // Convertir a booleano
  }, []);

  const toggleSidebar = () => {
    setShowSidebar(!showSidebar);
  };

  const toggleDropdown = () => {
    setShowDropdown(!showDropdown);
  };

  const toggleSubMenu = (index) => {
    setActiveMenu(index === activeMenu ? null : index);
  };

  const handleChangeUser = () => {
    localStorage.clear();
    navigate({ to: '/' });
  };

  // Función para manejar la navegación a la página de autenticación
  const handleAuthNavigation = (path) => {
    // Si hay configuración de autenticación, redirigir a home
    if (authConfigured && path === '/loginToken') {
      navigate({ to: '/home' });
    } else {
      navigate({ to: path });
    }
  };

  const sidebarTopItems = SidebarData.slice(0, 5);
  const sidebarBottomItems = SidebarData.slice(-3);

  return (
    <div className="relative w-screen h-screen flex flex-col">
      {/* Barra superior */}
      <div className="bg-slate-50 flex h-[50px] justify-between items-center border border-gray-300 p-2">
        <div className="flex items-center">
          <button
            className="relative w-8 h-8 bg-slate-50 border-none cursor-pointer p-0.5 focus:outline-none"
            onClick={toggleSidebar}
            aria-label="Toggle menu"
          >
            <span className="block w-6 h-0.5 bg-gray-700 my-1.5 rounded-sm"></span>
            <span className="block w-6 h-0.5 bg-gray-700 my-1.5 rounded-sm"></span>
            <span className="block w-6 h-0.5 bg-gray-700 my-1.5 rounded-sm"></span>
          </button>
          <Link to="/home">
            <img src={logo} className="my-6 w-15 h-10 p-2" alt="logo empresa" />
          </Link>
        </div>
        {/* Menú Usuario */}
        <div className="flex items-center relative">
          <div className="relative flex items-center gap-2">
            <button
              className="p-2 focus:outline-none hover:bg-gray-200 rounded-full"
              onClick={toggleDropdown}
              title={username}
            >
              <img src={user} className="w-6 h-6" alt="Menú usuario" />
            </button>
            <span className="text-gray-700 font-medium hidden md:block">{username}</span>
            {showDropdown && (
              <div className="absolute top-[50px] right-0 bg-white shadow-md rounded-md p-2 border border-gray-200 z-50 w-[180px]">
                <button
                  className="block w-full text-left text-[#0B6A8D] px-4 py-2 hover:bg-gray-100 rounded-md"
                  onClick={handleChangeUser}
                >
                  Cambiar de usuario
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Contenedor principal */}
      <div className="flex flex-1">
        {showSidebar && (
          <div className="absolute top-[50px] left-0 h-[calc(100%-50px)] w-[180px] bg-white border-r border-gray-300 shadow-lg z-50">
            {/* Sidebar Menú */}
            <div className="flex flex-col justify-between h-full p-2">
              {/* Íconos superiores */}
              <div>
                {sidebarTopItems.map((item, index) => {
                  if (
                    (item.title === 'User' || item.title === 'Autenticación') &&
                    role === 'VIEWER'
                  ) {
                    return null; // Ocultar ítems si el rol es VIEWER
                  }

                  return (
                    <div
                      key={index}
                      className="relative w-full"
                      onMouseEnter={() => toggleSubMenu(index)}
                      onMouseLeave={() => toggleSubMenu(null)}
                    >
                      {/* Usar onClick para elementos de autenticación, Link para otros */}
                      {item.title === 'Autenticación' ? (
                        <div
                          onClick={() => handleAuthNavigation(item.path)}
                          className="flex items-center gap-2 w-full p-2 mb-4 hover:bg-gray-200 rounded-lg min-w-[10.5rem] cursor-pointer"
                        >
                          {item.icon && (
                            <img
                              src={item.icon}
                              className="w-6 h-5 flex-shrink-0"
                              alt={item.title}
                            />
                          )}
                          <span className="text-[#3F9BB9] font-medium">{item.title}</span>
                          {item.subLinks && <span className="text-[#3F9BB9] text-lg">›</span>}
                        </div>
                      ) : (
                        <Link
                          to={item.path}
                          className="flex items-center gap-2 w-full p-2 mb-4 hover:bg-gray-200 rounded-lg min-w-[10.5rem]"
                        >
                          {item.icon && (
                            <img
                              src={item.icon}
                              className="w-6 h-5 flex-shrink-0"
                              alt={item.title}
                            />
                          )}
                          <span className="text-[#3F9BB9] font-medium">{item.title}</span>
                          {item.subLinks && <span className="text-[#3F9BB9] text-lg">›</span>}
                        </Link>
                      )}

                      {activeMenu === index && item.subLinks && (
                        <ul className="absolute left-full top-0 bg-slate-200 text-center w-[150px] p-4 border border-gray-300 rounded-md shadow-lg z-10">
                          <li className="font-bold text-[#3F9BB9]">{item.title}</li>
                          {item.subLinks.map((subItem, subIndex) => (
                            <Link
                              key={subIndex}
                              to={subItem.path}
                              className="block text-[#3F9BB9] my-1 hover:underline"
                            >
                              {subItem.title}
                            </Link>
                          ))}
                        </ul>
                      )}
                    </div>
                  );
                })}
              </div>

              {/* Íconos inferiores */}
              <div>
                {sidebarBottomItems.map((item, index) => (
                  <div
                    key={index}
                    className={`relative flex flex-col items-left p-[10px] cursor-pointer ${
                      (item.title === 'User' || item.title === 'Autenticación') &&
                      role === 'VIEWER'
                        ? 'hidden'
                        : ''
                    }`}
                  >
                    <Link to={item.path} className="block mb-4">
                      <img src={item.icon} className="w-[25px] h-auto" alt={item.title} />
                    </Link>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
        {/* Contenido principal */}
        <div className="flex-grow bg-gray-100 p-4 lg:p-12 overflow-y-auto">{children}</div>
      </div>
    </div>
  );
};

export default Sidebar;

