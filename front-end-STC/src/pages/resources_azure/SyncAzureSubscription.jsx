import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "@tanstack/react-router";
import Sidebar from '../../components/Sidebar';
import { Copy } from "lucide-react";

const SyncSubscription = () => {
  const [subscriptionId, setSubscriptionId] = useState("");
  const [subscriptionName, setSubscriptionName] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [errorMessage, setErrorMessage] = useState("");
  const [showCopied, setShowCopied] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const token = sessionStorage.getItem("azureAccessToken");
    if (!token) {
      // No token found, redirect to login
      navigate({ to: "/LoginAzure" });
    }
    
    // Check if sync has already been completed
    const syncCompleted = sessionStorage.getItem("azureSyncCompleted");
    if (syncCompleted === "true") {
      // User has already completed sync, redirect to home
      navigate({ to: "/homeazure" });
    }

    // If there's a selected subscription from before, pre-fill the form
    const savedSubscriptionId = sessionStorage.getItem("selectedSubscription");
    const savedSubscriptionName = sessionStorage.getItem("selectedSubscriptionDisplayName");
    
    if (savedSubscriptionId) {
      setSubscriptionId(savedSubscriptionId);
    }
    
    if (savedSubscriptionName) {
      setSubscriptionName(savedSubscriptionName);
    }
  }, [navigate]);

  const handleSyncSubscription = async () => {
    setIsLoading(true);
    setErrorMessage("");
    setProgress(0);
    
    try {
      const storedToken = sessionStorage.getItem("azureAccessToken");
      const email = sessionStorage.getItem("azureEmail") || "";
      
      const progressInterval = setInterval(() => {
        setProgress(prev => Math.min(prev + 20, 90));
      }, 500);

      // First validate the subscription
      const validationResponse = await axios.post(
        "http://localhost:8080/api/cloud/validate-subscription-azure",
        {
          token: storedToken,
          subscriptionId: subscriptionId
        }
      );

      if (validationResponse.status === 200) {
        // If valid, sync the subscription
        const response = await axios.post(
          "http://localhost:8080/api/cloud/save-subscription",
          {
            token: storedToken,
            subscriptionId: subscriptionId,
            email: email
          }
        );

        clearInterval(progressInterval);
        setProgress(100);

        if (response.status === 200) {
          // Save subscription data and mark sync as completed
          sessionStorage.setItem("selectedSubscription", subscriptionId);
          sessionStorage.setItem("selectedSubscriptionDisplayName", subscriptionName || subscriptionId);
          sessionStorage.setItem("azureSyncCompleted", "true"); // Important: Mark sync as completed
          setTimeout(() => navigate({ to: "/homeazure" }), 500);
        }
      }
    } catch (error) {
      console.error("Error syncing subscription:", error);
      setErrorMessage(
        error.response?.data?.message || "Failed to sync subscription. Please try again."
      );
      setProgress(0);
    } finally {
      setIsLoading(false);
    }
  };

  const handleCopy = (text, index) => {
    try {
      navigator.clipboard.writeText(text);
      setShowCopied(index);
      setTimeout(() => setShowCopied(null), 2000); // "Copied!" disappears after 2 seconds
    } catch (err) {
      console.error("Error al copiar:", err);
    }
  };

  return (
    <Sidebar showBackButton={true}>
      <div className="flex items-center justify-center flex-1 bg-gray-100 p-4">
        <div className="bg-white shadow-lg rounded-lg p-8 text-center space-y-6 w-[90%] max-w-md">
          <h1 className="text-xl font-bold">Enter Subscription ID</h1>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Subscription ID</label>
              <input
                type="text"
                value={subscriptionId}
                onChange={(e) => setSubscriptionId(e.target.value)}
                className="w-full px-3 py-2 border rounded"
                placeholder="Enter your Azure subscription ID"
                disabled={isLoading}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Subscription Name (optional)</label>
              <input
                type="text"
                value={subscriptionName}
                onChange={(e) => setSubscriptionName(e.target.value)}
                className="w-full px-3 py-2 border rounded"
                placeholder="Enter subscription display name"
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
              <p className="text-sm text-gray-600 mt-2">{progress}%</p>
            </div>
          ) : (
            <button
              onClick={handleSyncSubscription}
              className="py-2 px-4 rounded bg-blue-500 hover:bg-blue-600 text-white"
              disabled={!subscriptionId || isLoading}
            >
              Sync Subscription
            </button>
          )}
          <div className="text-sm text-gray-500 mt-4">
            <div className="bg-gray-50 p-4 rounded-md border border-gray-300 text-left">
              <p className="font-medium mb-2">How to find your Subscription ID:</p>
              <p className="mb-2">1. Using Azure CLI:</p>
              <div className="text-sm text-gray-600 leading-relaxed bg-gray-50 p-4 rounded-md border border-gray-300">
                Run the following command in your terminal:
                <br />
                <div className="flex items-center justify-center">
                <strong><code>az account show --query id -o tsv</code></strong>
                <button onClick={() => handleCopy('az account show --query id -o tsv', 1)} className="ml-2 relative">
                  <Copy className="w-5 h-5" />
                  {showCopied === 1 && (
                    <span className="absolute top-[-22px] right-[-10px] text-xs text-green-500 transition-opacity duration-300 bg-white px-1 rounded">
                      Copiado!
                    </span>
                  )}
                </button>
              </div>
              </div>
              <p className="mt-4 mb-2">2. In Azure Portal:</p>
              <p>Subscriptions → Select your subscription → Overview</p>
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

export default SyncSubscription;
