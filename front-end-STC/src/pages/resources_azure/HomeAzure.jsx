import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from '@tanstack/react-router';
import Sidebar from '../../components/Sidebar';
import axios from 'axios';
import azureLogo from '../../assets/azure.svg';
import ExportarDatosArchivos from '../../components/export_datos_archivos';

const HomeAzure = () => {
  const [subscriptions, setSubscriptions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [needsSync, setNeedsSync] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    // Check authentication and sync status
    const token = sessionStorage.getItem('azureAccessToken');
    const syncCompleted = sessionStorage.getItem('azureSyncCompleted');

    // No token - redirect to login page
    if (!token) {
      setLoading(false);
      return;
    }

    // Token exists but sync not completed - show need for sync
    if (token && syncCompleted !== "true") {
      setNeedsSync(true);
      setLoading(false);
      return;
    }

    const fetchSubscriptions = async () => {
      try {
        const token = sessionStorage.getItem('azureAccessToken');
        const selectedSubscriptionId = sessionStorage.getItem('selectedSubscription');

        if (!token) {
          navigate({ to: '/loginazure' });
          return;
        }

        // Intentar obtener suscripciones del backend
        const response = await axios.get('http://localhost:8080/api/cloud/subscriptionsAzure', {
          headers: { 'Authorization': `Bearer ${token}` },
        });

        // Si no hay suscripciones pero hay una seleccionada, crear una manualmente
        if (response.data && response.data.length === 0 && selectedSubscriptionId) {
          const selectedSubscriptionName = sessionStorage.getItem('selectedSubscriptionDisplayName');
          const tenantId = sessionStorage.getItem('azureTenantId');

          const manualSubscription = {
            id: selectedSubscriptionId,
            name: selectedSubscriptionName || 'My Azure Subscription',
            display_name: selectedSubscriptionName || 'My Azure Subscription',
            virtual_machines: [],
            directory: {
              name: 'Azure Directory',
              id: tenantId || ''
            }
          };

          setSubscriptions([manualSubscription]);
        } else {
          setSubscriptions(response.data || []);
        }

        setLoading(false);
      } catch (err) {
        console.error('Error fetching subscriptions:', err);

        // Check if this is an authentication error
        if (err.response && (err.response.status === 401 || err.response.status === 403)) {
          // Clear session storage and redirect to login
          sessionStorage.removeItem('azureAccessToken');
          sessionStorage.removeItem('azureEmail');
          sessionStorage.removeItem('subscriptions');
          sessionStorage.removeItem('selectedSubscription');
          sessionStorage.removeItem('selectedSubscriptionDisplayName');
          sessionStorage.removeItem('azureSyncCompleted');
          navigate({ to: '/loginazure' });
          return;
        }

        // Fallback a datos guardados en sessionStorage si la API falla
        const fallbackSubscriptionId = sessionStorage.getItem('selectedSubscription');
        const fallbackSubscriptionName = sessionStorage.getItem('selectedSubscriptionDisplayName');

        if (fallbackSubscriptionId && fallbackSubscriptionName) {
          const fallbackSubscription = {
            id: fallbackSubscriptionId,
            name: fallbackSubscriptionName,
            display_name: fallbackSubscriptionName,
            virtual_machines: [],
            directory: {
              name: 'Azure Directory',
              id: sessionStorage.getItem('azureTenantId') || ''
            }
          };

          setSubscriptions([fallbackSubscription]);
          setError(null);
        } else {
          setError('Error fetching subscriptions. Please try logging in again.');
        }

        setLoading(false);
      }
    };

    fetchSubscriptions();
  }, [navigate]);

  const handleSubscriptionClick = (subscription) => {
    sessionStorage.setItem('selectedSubscription', subscription.id);
    sessionStorage.setItem('selectedSubscriptionDisplayName', subscription.display_name || subscription.name);
    navigate({ to: '/subscripriondetailsazure' });
  };

  // Define columns for the ExportarDatosArchivos component
  const columns = [
    { header: 'ID', accessor: 'id' },
    { header: 'Name', accessor: item => item.name || item.display_name },
    { header: 'Organization', accessor: item => item.directory?.name || 'N/A' },
    { header: 'VMs Count', accessor: item => item.virtual_machines?.length || 0 }
  ];

  if (loading) {
    return (
      <Sidebar showBackButton={true}>
        <div className="flex justify-center items-center h-full">
          <div className="loader border-t-transparent border-blue-400 w-10 h-10 border-4 rounded-full animate-spin"></div>
        </div>
      </Sidebar>
    );
  }

  // Mostrar pantalla de inicio de sesión si no hay token
  const token = sessionStorage.getItem('azureAccessToken');
  if (!token) {
    return (
      <Sidebar showBackButton={true}>
        <div className="p-6 flex flex-col items-center justify-center h-full">
          <div className="max-w-md text-center bg-white p-8 rounded-lg shadow-lg">
            <h2 className="text-2xl font-bold text-[#0078D4] mb-4">Microsoft Azure</h2>
            <p className="text-gray-600 mb-6">Sign in to view your Azure subscriptions</p>
            <button
              onClick={() => navigate({ to: '/loginazure' })}
              className="px-6 py-3 bg-[#0078D4] text-white rounded-lg hover:bg-[#106EBE] transition-colors"
            >
              Sign in to Azure
            </button>
          </div>
        </div>
      </Sidebar>
    );
  }

  // Show sync required message if token exists but sync hasn't been completed
  if (needsSync) {
    return (
      <Sidebar showBackButton={true}>
        <div className="p-6 flex flex-col items-center justify-center h-full">
          <div className="max-w-md text-center bg-white p-8 rounded-lg shadow-lg">
            <h2 className="text-2xl font-bold text-[#0078D4] mb-4">Subscription Sync Required</h2>
            <p className="text-gray-600 mb-6">You need to complete the subscription sync process before accessing Azure resources</p>
            <button
              onClick={() => navigate({ to: '/syncAzureSubscription' })}
              className="px-6 py-3 bg-[#0078D4] text-white rounded-lg hover:bg-[#106EBE] transition-colors"
            >
              Complete Subscription Sync
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
          <h1 className="text-2xl font-bold text-[#0078D4]">Azure Subscriptions</h1>

          {subscriptions.length > 0 && (
            <ExportarDatosArchivos
              data={subscriptions}
              columns={columns}
              filename="Azure_Subscriptions"
              title="Azure Subscriptions"
            />
          )}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {subscriptions.length > 0 ? (
            subscriptions.map((subscription) => (
              <div
                key={subscription.id}
                className="relative border rounded-lg p-4 shadow-md hover:shadow-lg transition-shadow cursor-pointer"
                onClick={() => handleSubscriptionClick(subscription)}
              >
                <img src={azureLogo} alt="Azure" className="absolute top-2 right-2 w-6 h-6" />
                <h2 className="text-xl font-semibold text-[#0078D4]">{subscription.name || subscription.display_name}</h2>
                <p className="text-gray-600">ID: {subscription.id}</p>
                {subscription.directory && (
                  <p className="text-gray-500 text-sm">Organization: {subscription.directory.name}</p>
                )}
                {subscription.virtual_machines && subscription.virtual_machines.length > 0 && (
                  <p className="text-gray-500 text-sm">VMs: {subscription.virtual_machines.length}</p>
                )}
              </div>
            ))
          ) : error ? (
            <div className="col-span-3 border rounded-lg p-4 shadow-md bg-red-50 text-red-600">
              {error}
              <div className="mt-2">
                <button
                  onClick={() => navigate({ to: '/loginazure' })}
                  className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                >
                  Sign in to Azure
                </button>
              </div>
            </div>
          ) : (
            <div
              className="col-span-3 relative border rounded-lg p-4 shadow-md hover:shadow-lg transition-shadow cursor-pointer flex items-center justify-center bg-gray-100"
              onClick={() => navigate({ to: '/loginazure' })}
            >
              <span className="text-xl font-semibold text-gray-500 text-center">
                No subscriptions found. Click here to log in.
              </span>
            </div>
          )}

          {subscriptions.length > 0 && (
            <Link
              to="/syncAzureSubscription"
              className="border rounded-lg p-4 shadow-md hover:shadow-lg transition-shadow cursor-pointer flex items-center justify-center"
            >
              <span className="text-4xl text-[#0078D4] font-bold">+</span>
            </Link>
          )}
        </div>
      </div>
    </Sidebar>
  );
};

export default HomeAzure;

