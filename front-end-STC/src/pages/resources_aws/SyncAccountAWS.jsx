import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "@tanstack/react-router";
import Sidebar from '../../components/Sidebar';

const SyncAccountAWS = () => {
  const [accountId, setAccountId] = useState("");
  const [accountName, setAccountName] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [errorMessage, setErrorMessage] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    // Check if token exists
    const token = sessionStorage.getItem("awsToken");
    if (!token) {
      // No token found, redirect to login
      navigate({ to: "/LoginAWS" });
    }

    // Check if sync has already been completed
    const syncCompleted = sessionStorage.getItem("awsSyncCompleted");
    if (syncCompleted === "true") {
      // User has already completed sync, redirect to home
      navigate({ to: "/homeaws" });
    }

    // If there's a selected account from before, pre-fill the form
    const savedAccountId = sessionStorage.getItem("selectedAccountAWS");
    const savedAccountName = sessionStorage.getItem("selectedAccountNameAWS");

    if (savedAccountId) {
      setAccountId(savedAccountId);
    }

    if (savedAccountName) {
      setAccountName(savedAccountName);
    }
  }, [navigate]);

  const handleSyncAccount = async () => {
    setIsLoading(true);
    setErrorMessage("");
    setProgress(0);

    try {
      const storedToken = JSON.parse(sessionStorage.getItem("awsToken"));
      if (!storedToken?.accessKeyId || !storedToken?.secretAccessKey || !storedToken?.sessionToken) {
        throw new Error("Credenciales AWS incompletas en el almacenamiento");
      }

      const email = sessionStorage.getItem("awsEmail") || "";

      const progressInterval = setInterval(() => {
        setProgress(prev => Math.min(prev + 10, 90)); // Progreso más lento
      }, 800);

      // Sync the AWS account
      const response = await axios.post(
        "http://localhost:8080/api/aws/save-account",
        {
          accessKeyId: storedToken.accessKeyId,
          secretAccessKey: storedToken.secretAccessKey,
          sessionToken: storedToken.sessionToken,
          email: email,
          accountName: accountName || "AWS Account"
        },
        {
          timeout: 300000 // 5 minutos timeout
        }
      );

      clearInterval(progressInterval);
      setProgress(100);

      if (response.data?.error) {
        throw new Error(response.data.error);
      }

      if (response.status === 200) {
        const accountId = response.data.accountId ||
          storedToken.accessKeyId.substring(0, 12);

        sessionStorage.setItem("selectedAccountAWS", accountId);
        sessionStorage.setItem("selectedAccountNameAWS", accountName || "AWS Account");
        sessionStorage.setItem("awsSyncCompleted", "true");

        setTimeout(() => navigate({ to: "/homeaws" }), 1000);
      }
    } catch (error) {
      console.error("Error syncing AWS account:", error);
      setErrorMessage(
        error.response?.data?.message ||
        error.response?.data?.error ||
        error.message ||
        "Error desconocido al sincronizar cuenta AWS"
      );
      setProgress(0);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Sidebar showBackButton={true}>
      <div className="flex items-center justify-center flex-1 bg-gray-100 p-4">
        <div className="bg-white shadow-lg rounded-lg p-8 text-center space-y-6 w-[90%] max-w-md">
          <h1 className="text-xl font-bold">Sync AWS Account</h1>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Account ID</label>
              <input
                type="text"
                value={accountId}
                onChange={(e) => setAccountId(e.target.value)}
                className="w-full px-3 py-2 border rounded"
                placeholder="Enter your AWS Account ID (optional)"
                disabled={isLoading}
              />
              <p className="text-xs text-gray-500 text-left mt-1">
                Will be auto-detected if not provided
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Account Name</label>
              <input
                type="text"
                value={accountName}
                onChange={(e) => setAccountName(e.target.value)}
                className="w-full px-3 py-2 border rounded"
                placeholder="Enter a display name for this account"
                disabled={isLoading}
              />
            </div>
          </div>
          {isLoading ? (
            <div className="mt-4">
              <div className="relative w-full bg-gray-200 rounded">
                <div
                  className="absolute bottom-1 left-0 h-2 bg-blue-500 rounded transition-all duration-200"
                  style={{ width: `${progress}%` }}
                ></div>
              </div>
              <p className="text-sm text-gray-600 mt-2">{progress}% - Syncing resources...</p>
            </div>
          ) : (
            <button
              onClick={handleSyncAccount}
              className="py-2 px-4 rounded bg-blue-500 hover:bg-blue-600 text-white"
              disabled={isLoading}
            >
              Sync AWS Account
            </button>
          )}
          <div className="text-sm text-gray-500 mt-4">
            <div className="bg-gray-50 p-4 rounded-md border border-gray-300 text-left">
              <p className="font-medium mb-2">What happens during sync:</p>
              <ul className="list-disc pl-5 space-y-1">
                <li>We scan your AWS account for resources</li>
                <li>All EC2 instances, S3 buckets, and other resources will be indexed</li>
                <li>This process may take a few moments to complete</li>
                <li>No changes will be made to your AWS account</li>
              </ul>
            </div>
          </div>
          {errorMessage && (
            <p className="text-red-500 text-sm">{errorMessage}</p>
          )}
        </div>
      </div>
    </Sidebar>
  );
};

export default SyncAccountAWS;

