import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from '@tanstack/react-router';
import Sidebar from '../../components/Sidebar';
import axios from 'axios';
import awsLogo from '../../assets/amazon.svg';
import ExportarDatosArchivos from '../../components/export_datos_archivos';

const HomeAWS = () => {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const accessKeyId = sessionStorage.getItem('awsAccessKeyId');
    const secretAccessKey = sessionStorage.getItem('awsSecretAccessKey');
    const sessionToken = sessionStorage.getItem('awsSessionToken');

    if (!accessKeyId || !secretAccessKey || !sessionToken) {
      setLoading(false);
      return;
    }

    const fetchAccounts = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/aws/accounts');
        setAccounts(response.data || []);
        setLoading(false);
      } catch (err) {
        console.error('Error al obtener las cuentas de AWS:', err);

        const accountId = sessionStorage.getItem('selectedAWSAccount');
        const accountName = sessionStorage.getItem('selectedAWSAccountName');

        if (accountId && accountName) {
          const fallbackAccount = {
            id: accountId,
            name: accountName,
            email: sessionStorage.getItem('awsUserEmail') || 'unknown@aws.com',
            status: 'active',
            ec2_instances: []
          };

          setAccounts([fallbackAccount]);
          setError(null);
        } else {
          setError('Error al obtener las cuentas de AWS. Intenta iniciar sesión nuevamente.');
        }

        setLoading(false);
      }
    };

    fetchAccounts();
  }, [navigate]);

  const handleAccountClick = (account) => {
    sessionStorage.setItem('selectedAWSAccount', account.id);
    sessionStorage.setItem('selectedAWSAccountName', account.name);
    navigate({ to: '/AccountDetailsAWS' });
  };

  // Define columns for export
  const exportColumns = [
    { header: 'ID', accessor: 'id' },
    { header: 'Nombre', accessor: 'name' },
    { header: 'Correo electrónico', accessor: item => item.email || 'N/A' },
    { header: 'Estado', accessor: item => item.status || 'Active' },
    { header: 'Cantidad de EC2', accessor: item => item.ec2_instances?.length || 0 }
  ];

  if (loading) {
    return (
      <Sidebar showBackButton={true}>
        <div className="flex justify-center items-center h-full">
          <div className="loader border-t-transparent border-yellow-500 w-10 h-10 border-4 rounded-full animate-spin"></div>
        </div>
      </Sidebar>
    );
  }

  const hasCredentials = sessionStorage.getItem('awsAccessKeyId') &&
    sessionStorage.getItem('awsSecretAccessKey') &&
    sessionStorage.getItem('awsSessionToken');

  if (!hasCredentials) {
    return (
      <Sidebar showBackButton={true}>
        <div className="p-6 flex flex-col items-center justify-center h-full">
          <div className="max-w-md text-center bg-white p-8 rounded-lg shadow-lg">
            <h2 className="text-2xl font-bold text-yellow-600 mb-4">Amazon Web Services</h2>
            <p className="text-gray-600 mb-6">Inicia sesión para ver tus cuentas de AWS</p>
            <button
              onClick={() => navigate({ to: '/loginaws' })}
              className="px-6 py-3 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700 transition-colors"
            >
              Iniciar sesión en AWS
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
          <h1 className="text-2xl font-bold text-yellow-600">Cuentas de AWS</h1>

          {accounts.length > 0 && (
            <ExportarDatosArchivos 
              data={accounts}
              columns={exportColumns}
              filename="AWS_Cuentas"
              title="Cuentas de AWS"
              subtitle={`Total: ${accounts.length} cuentas`}
            />
          )}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {accounts.length > 0 ? (
            accounts.map((account) => (
              <div
                key={account.id}
                className="relative border rounded-lg p-4 shadow-md hover:shadow-lg transition-shadow cursor-pointer"
                onClick={() => handleAccountClick(account)}
              >
                <img src={awsLogo} alt="AWS" className="absolute top-2 right-2 w-6 h-6" />
                <h2 className="text-xl font-semibold text-yellow-600">{account.name}</h2>
                <p className="text-gray-600">ID: {account.id}</p>
                <p className="text-gray-500 text-sm">Correo: {account.email || 'N/A'}</p>
                <p className="text-gray-500 text-sm">Estado: {account.status || 'Active'}</p>
                {account.ec2_instances && account.ec2_instances.length > 0 && (
                  <p className="text-gray-500 text-sm">Instancias EC2: {account.ec2_instances.length}</p>
                )}
              </div>
            ))
          ) : error ? (
            <div className="col-span-3 border rounded-lg p-4 shadow-md bg-red-50 text-red-600">
              {error}
              <div className="mt-2">
                <button
                  onClick={() => navigate({ to: '/loginaws' })}
                  className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                >
                  Sign in to AWS
                </button>
              </div>
            </div>
          ) : (
            <div
              className="col-span-3 relative border rounded-lg p-4 shadow-md hover:shadow-lg transition-shadow cursor-pointer flex items-center justify-center bg-gray-100"
              onClick={() => navigate({ to: '/loginaws' })}
            >
              <span className="text-xl font-semibold text-gray-500 text-center">
                No se encontraron cuentas. Haz clic aquí para iniciar sesión.
              </span>
            </div>
          )}

          {accounts.length > 0 && (
            <Link
              to="/syncAWSAccount"
              className="border rounded-lg p-4 shadow-md hover:shadow-lg transition-shadow cursor-pointer flex items-center justify-center"
            >
              <span className="text-4xl text-yellow-600 font-bold">+</span>
            </Link>
          )}
        </div>
      </div>
    </Sidebar>
  );
};

export default HomeAWS;

