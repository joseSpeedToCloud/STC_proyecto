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
  const [needsSync, setNeedsSync] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    // Check authentication and sync status
    const token = sessionStorage.getItem('awsToken');
    const syncCompleted = sessionStorage.getItem('awsSyncCompleted');

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

    const fetchAccounts = async () => {
      try {
        // Get accounts from backend
        const response = await axios.get('http://localhost:8080/api/aws/homeaws');
        
        if (response.data && response.data.length) {
          setAccounts(response.data);
        } else {
          // If no accounts returned but we have a selected account, create one manually
          const selectedAccountId = sessionStorage.getItem('selectedAccountAWS');
          const selectedAccountName = sessionStorage.getItem('selectedAccountNameAWS');
          
          if (selectedAccountId && selectedAccountName) {
            const manualAccount = {
              id: selectedAccountId,
              name: selectedAccountName,
              email: sessionStorage.getItem('awsEmail') || 'unknown@example.com',
              status: 'active',
              ec2Instances: []
            };
            
            setAccounts([manualAccount]);
          }
        }
        
        setLoading(false);
      } catch (err) {
        console.error('Error fetching AWS accounts:', err);
        
        // Check if this is an authentication error
        if (err.response && (err.response.status === 401 || err.response.status === 403)) {
          // Clear session storage and redirect to login
          sessionStorage.removeItem('awsToken');
          sessionStorage.removeItem('awsEmail');
          sessionStorage.removeItem('selectedAccountAWS');
          sessionStorage.removeItem('selectedAccountNameAWS');
          sessionStorage.removeItem('awsSyncCompleted');
          navigate({ to: '/loginaws' });
          return;
        }
        
        // Fallback to data saved in sessionStorage if API fails
        const fallbackAccountId = sessionStorage.getItem('selectedAccountAWS');
        const fallbackAccountName = sessionStorage.getItem('selectedAccountNameAWS');
        
        if (fallbackAccountId && fallbackAccountName) {
          const fallbackAccount = {
            id: fallbackAccountId,
            name: fallbackAccountName,
            email: sessionStorage.getItem('awsEmail') || 'unknown@example.com',
            status: 'active',
            ec2Instances: []
          };
          
          setAccounts([fallbackAccount]);
          setError(null);
        } else {
          setError('Error fetching AWS accounts. Please try logging in again.');
        }
        
        setLoading(false);
      }
    };

    fetchAccounts();
  }, [navigate]);

  const handleAccountClick = (account) => {
    sessionStorage.setItem('selectedAccountAWS', account.id);
    sessionStorage.setItem('selectedAccountNameAWS', account.name);
    navigate({ to: '/accountdetailsaws' });
  };

  // Define columns for export
  const exportColumns = [
    { header: 'ID', accessor: 'id' },
    { header: 'Name', accessor: 'name' },
    { header: 'Email', accessor: 'email' },
    { header: 'Status', accessor: 'status' },
    { header: 'EC2 Instances Count', accessor: account => account.ec2Instances?.length || 0 }
  ];

  if (loading) {
    return (
      <Sidebar showBackButton={true}>
        <div className="flex justify-center items-center h-full">
          <div className="loader border-t-transparent border-[#4285F4] w-10 h-10 border-4 rounded-full animate-spin"></div>
        </div>
      </Sidebar>
    );
  }

  // Show login screen if no token
  const token = sessionStorage.getItem('awsToken');
  if (!token) {
    return (
      <Sidebar showBackButton={true}>
        <div className="p-6 flex flex-col items-center justify-center h-full">
          <div className="max-w-md text-center bg-white p-8 rounded-lg shadow-lg">
            <h2 className="text-2xl font-bold text-yellow-600 mb-4">Amazon Web Services</h2>
            <p className="text-gray-600 mb-6">Sign in to view your AWS accounts</p>
            <button
              onClick={() => navigate({ to: '/loginaws' })}
              className="px-6 py-3 bg-[#4285F4] text-white rounded-lg hover:bg-[#3367D6] transition-colors"
            >
              Sign in to AWS
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
            <h2 className="text-2xl font-bold text-yellow-600 mb-4">Account Sync Required</h2>
            <p className="text-gray-600 mb-6">You need to complete the account sync process before accessing AWS resources</p>
            <button
              onClick={() => navigate({ to: '/SyncAccountAWS' })}
              className="px-6 py-3 bg-[#4285F4] text-white rounded-lg hover:bg-[#3367D6] transition-colors"
            >
              Complete Account Sync
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
          <h1 className="text-2xl font-bold text-yellow-600">AWS Accounts</h1>
          
          {accounts.length > 0 && (
            <ExportarDatosArchivos
              data={accounts}
              columns={exportColumns}
              filename="AWS_Accounts"
              title="AWS Accounts"
              subtitle={`Total: ${accounts.length} accounts`}
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
                <p className="text-gray-500 text-sm">Email: {account.email}</p>
                <p className="text-gray-500 text-sm">Status: {account.status}</p>
                {account.ec2Instances && (
                  <p className="text-gray-500 text-sm">EC2 Instances: {account.ec2Instances.length}</p>
                )}
              </div>
            ))
          ) : error ? (
            <div className="col-span-3 border rounded-lg p-4 shadow-md bg-red-50 text-red-600">
              {error}
              <div className="mt-2">
                <button
                  onClick={() => navigate({ to: '/loginaws' })}
                  className="px-6 py-3 bg-[#4285F4] text-white rounded-lg hover:bg-[#3367D6] transition-colors"
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
                No accounts found. Click here to log in.
              </span>
            </div>
          )}
          
          {accounts.length > 0 && (
            <Link
              to="/SyncAccountAWS"
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

export default HomeAWS;

