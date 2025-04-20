import React from 'react';
import { useNavigate } from '@tanstack/react-router';
import Sidebar from '../components/Sidebar';
import googleLogo from '../assets/logoGoogle.png';
import awsLogo from '../assets/amazon.svg';
import azureLogo from '../assets/azure.svg';
import { useEffect } from 'react';

const LoginToken = () => {
  const navigate = useNavigate();
  const userRole = localStorage.getItem('userRole');

  useEffect(() => {
    // Redirigir si el usuario es VIEWER
    if (userRole === 'VIEWER') {
      navigate({ to: '/home' });
    }
  }, [userRole, navigate]);

  const handleCardClick = (provider) => {
    // Verificar rol nuevamente por seguridad
    if (userRole === 'VIEWER') {
      alert('No tienes permisos para realizar esta acción');
      return;
    }

    switch (provider) {
      case 'google':
        navigate({ to: '/googleAuth' });
        break;
      case 'azure':
        navigate({ to: '/LoginAzure' });
        break;
      case 'aws':
        navigate({ to: '/LoginAWS' });
        break;
      default:
        alert(`Login for ${provider.toUpperCase()} is not implemented yet.`);
    }
  };

  // No mostrar nada si el usuario es VIEWER
  if (userRole === 'VIEWER') {
    return null;
  }

  return (
    <Sidebar showBackButton={true}>
      <div className="flex items-center justify-center flex-1 bg-gray-100 p-4">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-4xl">
          {/* Google Card */}
          <div
            className="relative border rounded-lg p-6 shadow-md hover:shadow-lg transition-shadow cursor-pointer bg-white"
            onClick={() => handleCardClick('google')}
          >
            <img src={googleLogo} alt="Google" className="w-16 h-14 mx-auto mb-4" />
            <h2 className="text-xl font-bold text-center text-[#3F9BB9]">Login with Google</h2>
          </div>

          {/* AWS Card */}
          <div
            className="relative border rounded-lg p-6 shadow-md hover:shadow-lg transition-shadow cursor-pointer bg-white"
            onClick={() => handleCardClick('aws')}
          >
            <img src={awsLogo} alt="AWS" className="w-16 h-16 mx-auto mb-4" />
            <h2 className="text-xl font-bold text-center text-yellow-500">Login with AWS</h2>
          </div>

          {/* Azure Card */}
          <div
            className="relative border rounded-lg p-6 shadow-md hover:shadow-lg transition-shadow cursor-pointer bg-white"
            onClick={() => handleCardClick('azure')}
          >
            <img src={azureLogo} alt="Azure" className="w-16 h-16 mx-auto mb-4" />
            <h2 className="text-xl font-bold text-center text-blue-700">Login with Azure</h2>
          </div>
        </div>
      </div>
    </Sidebar>
  );
};

export default LoginToken;

