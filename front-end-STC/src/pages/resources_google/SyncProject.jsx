import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "@tanstack/react-router";
import Sidebar from '../../components/Sidebar';
import { Copy } from "lucide-react";

const SyncProject = () => {
  const [projectId, setProjectId] = useState("");
  const [projectName, setProjectName] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [errorMessage, setErrorMessage] = useState("");
  const [showCopied, setShowCopied] = useState(null); // To manage the "copied" message
  const navigate = useNavigate();

  // Check authentication on component mount
  useEffect(() => {
    const checkAuth = async () => {
      const token = sessionStorage.getItem("gcpAccessToken");
      
      if (!token) {
        // Redirect to login if no token
        navigate({ to: "/googleAuth" });
        return;
      }
      
      // If they already have a project selected, redirect to home
      const selectedProject = sessionStorage.getItem("selectedProject");
      if (selectedProject) {
        navigate({ to: "/homegcp" });
      }
      
      // Validate token is still valid
      try {
        await axios.post(
          "http://localhost:8080/api/cloud/validate-token",
          { token }
        );
      } catch (error) {
        console.error("Invalid token:", error);
        // Clear invalid token and redirect to login
        sessionStorage.removeItem("gcpAccessToken");
        sessionStorage.removeItem("gcpEmail");
        navigate({ to: "/googleAuth" });
      }
    };
    
    checkAuth();
  }, [navigate]);

  const handleSyncProject = async () => {
    setIsLoading(true);
    setErrorMessage("");
    setProgress(0);
    try {
      const storedToken = sessionStorage.getItem("gcpAccessToken");
      
      if (!storedToken) {
        setErrorMessage("Authentication token missing. Please login again.");
        navigate({ to: "/googleAuth" });
        return;
      }
      
      const progressInterval = setInterval(() => {
        setProgress((prev) => {
          const nextValue = Math.min(prev + 20, 90);
          return nextValue;
        });
      }, 500);

      // First validate the project
      const validationResponse = await axios.post(
        "http://localhost:8080/api/cloud/validate-project",
        {
          token: storedToken,
          projectId: projectId
        }
      );

      if (validationResponse.status === 200) {
        // If project is valid, proceed to sync
        const response = await axios.post(
          "http://localhost:8080/api/cloud/save-project",
          {
            token: storedToken,
            projectId: projectId
          }
        );

        clearInterval(progressInterval);
        setProgress(100);

        if (response.status === 200) {
          sessionStorage.setItem("selectedProject", projectId);
          sessionStorage.setItem("selectedProjectDisplayName", projectName || projectId);
          setTimeout(() => navigate({ to: "/homegcp" }), 500);
        }
      }
    } catch (error) {
      console.error("Error syncing project:", error);
      setErrorMessage(
        error.response?.data?.message || "Failed to sync project. Please try again."
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
          <h1 className="text-xl font-bold">Enter Project ID</h1>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Project ID</label>
              <input
                type="text"
                value={projectId}
                onChange={(e) => setProjectId(e.target.value)}
                className="w-full px-3 py-2 border rounded"
                placeholder="Enter your GCP project ID"
                disabled={isLoading}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Project Name (optional)</label>
              <input
                type="text"
                value={projectName}
                onChange={(e) => setProjectName(e.target.value)}
                className="w-full px-3 py-2 border rounded"
                placeholder="Enter project display name"
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
              onClick={handleSyncProject}
              className="py-2 px-4 rounded bg-blue-500 hover:bg-blue-600 text-white"
              disabled={!projectId || isLoading}
            >
              Sync Project
            </button>
          )}
          {errorMessage && (
            <p className="text-red-500 text-sm">{errorMessage}</p>
          )}
          <div className="text-sm text-gray-500 mt-4">
            <div className="bg-gray-50 p-4 rounded-md border border-gray-300 text-left">
              <p className="font-medium mb-2">How to find your Project ID:</p>
              <p className="mb-2">1. Using gcloud CLI:</p>
              <div className="text-sm text-gray-600 leading-relaxed bg-gray-50 p-4 rounded-md border border-gray-300">
                Run the following command in your terminal:
                <br />
                <div className="flex items-center justify-center">
                <strong><code>gcloud projects list</code></strong>
                <button onClick={() => handleCopy('gcloud projects list', 1)} className="ml-2 relative">
                  <Copy className="w-5 h-5" />
                  {showCopied === 1 && (
                    <span className="absolute top-[-22px] right-[-10px] text-xs text-green-500 transition-opacity duration-300 bg-white px-1 rounded">
                      Copiado!
                    </span>
                  )}
                </button>
              </div>
              </div>
              <p className="mt-4 mb-2">2. In GCP Console:</p>
              <p>Navigation Menu → IAM & Admin → Settings</p>
            </div>
          </div>
        </div>
      </div>
    </Sidebar>
  );
};

export default SyncProject;
