import React, { useState, useEffect, useCallback } from "react";
import axios from "axios";
import { useNavigate } from "@tanstack/react-router";
import Sidebar from '../components/Sidebar';
import { Copy } from "lucide-react";

const LoginAWS = () => {
  const [accessKeyId, setAccessKeyId] = useState("");
  const [secretAccessKey, setSecretAccessKey] = useState("");
  const [sessionToken, setSessionToken] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [showCopied, setShowCopied] = useState(null);
  const navigate = useNavigate();

  // Check if token already exists when component loads
  useEffect(() => {
    const storedToken = sessionStorage.getItem("awsToken");
    if (storedToken) {
      navigate({ to: "/SyncAccountAWS" });
    }
  }, [navigate]);

  // Function to renew token
  const renewCredentials = useCallback(async () => {
    try {
      const response = await axios.get("http://localhost:8080/api/aws/renew-token-aws");

      if (response.status === 200) {
        const { credentials, accountId } = response.data;
        
        sessionStorage.setItem("awsToken", JSON.stringify(credentials));
        sessionStorage.setItem("awsAccountId", accountId);

        console.log("AWS credentials renewed successfully");
        return credentials;
      }
    } catch (error) {
      console.error("Error renewing AWS credentials:", error);
      sessionStorage.removeItem("awsToken");
      sessionStorage.removeItem("awsAccountId");
      navigate({ to: "/LoginAWS" });
    }
  }, [navigate]);

  // Automatic token renewal
  useEffect(() => {
    const storedToken = sessionStorage.getItem("awsToken");

    if (storedToken) {
      const renewalInterval = setInterval(async () => {
        await renewCredentials();
      }, 5 * 60 * 1000); // Renew every 5 minutes

      sessionStorage.setItem("credentialRenewalIntervalIdAWS", renewalInterval.toString());

      return () => clearInterval(renewalInterval);
    }
  }, [renewCredentials]);

  // Clear interval when component unmounts
  useEffect(() => {
    return () => {
      const intervalId = sessionStorage.getItem("credentialRenewalIntervalIdAWS");
      if (intervalId) {
        clearInterval(Number(intervalId));
        sessionStorage.removeItem("credentialRenewalIntervalIdAWS");
      }
    };
  }, []);

  // Function to validate the token
  const handleValidateToken = async () => {
    setIsLoading(true);
    setErrorMessage("");
    
    try {
      const response = await axios.post("http://localhost:8080/api/aws/validate-token-aws", {
        accessKeyId,
        secretAccessKey,
        sessionToken
      });

      if (response.status === 200) {
        const { accountId, email } = response.data;
        
        // Save credentials and account data
        const credentials = { accessKeyId, secretAccessKey, sessionToken };
        sessionStorage.setItem("awsToken", JSON.stringify(credentials));
        sessionStorage.setItem("awsAccountId", accountId);
        sessionStorage.setItem("awsEmail", email);

        // Redirect to sync page
        navigate({ to: "/SyncAccountAWS" });
      }
    } catch (error) {
      console.error("Error validating AWS token:", error);
      setErrorMessage(
        error.response?.data?.error ||
        "Invalid or expired AWS credentials. Please try again."
      );
    } finally {
      setIsLoading(false);
    }
  };

  const handleCopy = (text, index) => {
    try {
      navigator.clipboard.writeText(text);
      setShowCopied(index);
      setTimeout(() => setShowCopied(null), 2000);
    } catch (err) {
      console.error("Error copying:", err);
    }
  };

  return (
    <Sidebar showBackButton={true}>
      <div className="relative w-full h-full flex flex-col">
        <div className="flex items-center justify-center flex-1 bg-gray-100 p-4">
          <div className="bg-white shadow-lg rounded-lg p-8 text-center space-y-6 w-[90%] max-w-md">
            <h1 className="text-xl font-bold">Sign in with AWS</h1>
            
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1 text-left">Access Key ID</label>
                <input
                  type="text"
                  value={accessKeyId}
                  onChange={(e) => setAccessKeyId(e.target.value)}
                  className="w-full px-3 py-2 border rounded"
                  placeholder="Enter your Access Key ID"
                  disabled={isLoading}
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1 text-left">Secret Access Key</label>
                <input
                  type="password"
                  value={secretAccessKey}
                  onChange={(e) => setSecretAccessKey(e.target.value)}
                  className="w-full px-3 py-2 border rounded"
                  placeholder="Enter your Secret Access Key"
                  disabled={isLoading}
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1 text-left">Session Token</label>
                <textarea
                  value={sessionToken}
                  onChange={(e) => setSessionToken(e.target.value)}
                  className="w-full px-3 py-2 border rounded min-h-20"
                  placeholder="Enter your Session Token"
                  disabled={isLoading}
                />
              </div>
            </div>
            
            {isLoading ? (
              <div className="flex justify-center items-center mt-4">
                <div className="loader border-t-transparent border-blue-400 w-10 h-10 border-4 rounded-full animate-spin"></div>
              </div>
            ) : (
              <button
                onClick={handleValidateToken}
                className="py-2 px-4 rounded bg-blue-500 hover:bg-blue-600 text-white"
                disabled={!accessKeyId || !secretAccessKey || !sessionToken || isLoading}
              >
                Validate Credentials
              </button>
            )}

            <div className="text-sm text-gray-600 leading-relaxed bg-gray-50 p-3 rounded-md border border-gray-300 space-y-2">
              <p>Run the following commands in your terminal to get AWS credentials:</p>
              <div className="flex items-center justify-center">
                <strong><code>aws configure</code></strong>
                <button onClick={() => handleCopy('aws configure', 0)} className="ml-2 relative">
                  <Copy className="w-5 h-5" />
                  {showCopied === 0 && (
                    <span className="absolute top-[-22px] right-[-10px] text-xs text-green-500 transition-opacity duration-300 bg-white px-1 rounded">
                      Copied!
                    </span>
                  )}
                </button>
              </div>
              <div className="flex items-center justify-center">
                <span>to configure your AWS credentials.</span>
              </div>
              <div className="flex items-center justify-center">
                <strong><code>aws sts get-session-token --output json</code></strong>
                <button onClick={() => handleCopy('aws sts get-session-token --output json', 1)} className="ml-2 relative">
                  <Copy className="w-5 h-5" />
                  {showCopied === 1 && (
                    <span className="absolute top-[-22px] right-[-10px] text-xs text-green-500 transition-opacity duration-300 bg-white px-1 rounded">
                      Copied!
                    </span>
                  )}
                </button>
              </div>
              <div className="flex items-center justify-center">
                <span>Copy the values from the JSON response.</span>
              </div>
            </div>
            
            {errorMessage && (
              <p className="text-red-500 text-sm">{errorMessage}</p>
            )}
          </div>
        </div>
      </div>
    </Sidebar>
  );
};

export default LoginAWS;

