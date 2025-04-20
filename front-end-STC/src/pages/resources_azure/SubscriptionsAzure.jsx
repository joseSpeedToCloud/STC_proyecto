import React, { useState, useEffect } from 'react';
import { useNavigate } from '@tanstack/react-router';
import Sidebar from '../../components/Sidebar';
import axios from 'axios';
import ExportarDatosArchivos from '../../components/export_datos_archivos';

const SubscriptionsAzure = () => {
  const [subscriptions, setSubscriptions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const fetchSubscriptions = async () => {
      try {
        const token = sessionStorage.getItem('azureAccessToken');
        if (!token) {
          navigate({ to: '/syncazure' });
          return;
        }

        const response = await axios.get('http://localhost:8080/api/cloud/subscriptionsAzure', {
          headers: { 'Authorization': `Bearer ${token}` },
        });

        setSubscriptions(response.data);
        setLoading(false);
      } catch (err) {
        console.error('Error fetching subscriptions:', err);
        setError('Error fetching subscriptions');
        setLoading(false);
      }
    };

    fetchSubscriptions();
  }, [navigate]);

  const handleSubscriptionClick = (subscription) => {
    sessionStorage.setItem('selectedSubscription', subscription.id);
    sessionStorage.setItem('selectedSubscriptionDisplayName', subscription.display_name || subscription.displayName);
    navigate({ to: '/subscriptionDetails' });
  };

  const handleFilterChange = (e) => {
    setFilter(e.target.value);
  };

  const filteredSubscriptions = subscriptions.filter(sub => 
    (sub.name && sub.name.toLowerCase().includes(filter.toLowerCase())) ||
    (sub.id && sub.id.toLowerCase().includes(filter.toLowerCase())) ||
    (sub.displayName && sub.displayName.toLowerCase().includes(filter.toLowerCase()))
  );

  // Define columns for export
  const exportColumns = [
    { header: 'ID', accessor: 'id' },
    { header: 'Name', accessor: 'name' },
    { header: 'Display Name', accessor: 'displayName', 
      // Handle cases where displayName might be null
      accessor: (item) => item.displayName || 'N/A' 
    },
    { header: 'Organization', accessor: (item) => item.directory?.name || 'N/A' }
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

  if (error) {
    return (
      <Sidebar showBackButton={true}>
        <div className="text-red-500 p-4">{error}</div>
      </Sidebar>
    );
  }

  return (
    <Sidebar showBackButton={true}>
      <div className="flex flex-col h-screen">
        <div className="w-full flex flex-col flex-grow">
          <div className="flex justify-between mb-4 px-6">
            <h1 className="text-2xl text-[#0078D4] self-center">Azure Subscriptions</h1>
            <div className="flex space-x-4">
              <input
                placeholder="Filter"
                className="border-2 border-[#ccc] rounded-[5px] px-3 py-2 text-sm text-black"
                value={filter}
                onChange={handleFilterChange}
              />
              <ExportarDatosArchivos
                data={filteredSubscriptions}
                columns={exportColumns}
                filename="Azure_Subscriptions"
                title="Azure Subscriptions"
              />
            </div>
          </div>
          
          <table className="min-w-full bg-white rounded-lg">
            <thead>
              <tr className="bg-gray-200 text-[#004E8C] font-semibold">
                <td className="py-2 px-4 border-b border-gray-200 text-center">ID</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Name</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Display Name</td>
                <td className="py-2 px-4 border-b border-gray-200 text-center">Organization</td>
              </tr>
            </thead>
            <tbody>
              {filteredSubscriptions.length > 0 ? (
                filteredSubscriptions.map((subscription, index) => (
                  <tr
                    key={index}
                    className="hover:bg-gray-100 cursor-pointer"
                    onClick={() => handleSubscriptionClick(subscription)}
                  >
                    <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{subscription.id}</td>
                    <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{subscription.name}</td>
                    <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{subscription.displayName || 'N/A'}</td>
                    <td className="text-sm text-center py-2 px-4 border-b border-gray-200">{subscription.directory?.name || 'N/A'}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="4" className="text-center py-4 text-gray-500">No subscriptions found</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </Sidebar>
  );
};

export default SubscriptionsAzure;
