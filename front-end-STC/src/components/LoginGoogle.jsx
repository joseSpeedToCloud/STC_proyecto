import React, { useState, useEffect, useCallback } from "react";
import axios from "axios";
import { useNavigate } from "@tanstack/react-router";
import Sidebar from '../components/Sidebar';
import { Copy } from "lucide-react";

const LoginGoogle = () => {
  const [accessToken, setAccessToken] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [showCopied, setShowCopied] = useState(null); // To manage the "copied" message
  const navigate = useNavigate();

  // Check if already authenticated on component mount
  useEffect(() => {
    const storedToken = sessionStorage.getItem("gcpAccessToken");
    const selectedProject = sessionStorage.getItem("selectedProject");
    
    if (storedToken) {
      // If they have a token and project, go to home
      if (selectedProject) {
        navigate({ to: "/homegcp" });
      } 
      // If they have token but no project, go to project sync
      else {
        navigate({ to: "/syncproject" });
      }
    }
  }, [navigate]);

  // Función para renovar el token
  const renewToken = useCallback(async () => {
    try {
      const response = await axios.get("http://localhost:8080/api/cloud/renew-token");

      if (response.status === 200) {
        const { accessToken: newToken, email } = response.data;

        // Actualizar el token en sessionStorage
        sessionStorage.setItem("gcpAccessToken", newToken);
        sessionStorage.setItem("gcpEmail", email);

        return newToken;
      }
    } catch (error) {
      console.error("Error renovando token:", error);
      // Manejar error de renovación (opcional: desloguear al usuario)
      sessionStorage.removeItem("gcpAccessToken");
      sessionStorage.removeItem("gcpEmail");
      sessionStorage.removeItem("projects");
      sessionStorage.removeItem("selectedProject");
      sessionStorage.removeItem("selectedProjectDisplayName");
      navigate({ to: "/googleAuth" });
    }
  }, [navigate]);

  // Hook para renovación automática de token
  useEffect(() => {
    // Verificar si hay un token guardado
    const storedToken = sessionStorage.getItem("gcpAccessToken");

    if (storedToken) {
      // Configurar intervalo de renovación
      const renewalInterval = setInterval(async () => {
        await renewToken();
      }, 5 * 60 * 1000); // Renovar cada 5 minutos

      // Limpiar intervalo al desmontar
      return () => clearInterval(renewalInterval);
    }
  }, [renewToken]);

  const handleValidateToken = async () => {
    setIsLoading(true);
    setErrorMessage("");
    try {
      const response = await axios.post(
        "http://localhost:8080/api/cloud/validate-token",
        { token: accessToken }
      );

      if (response.status === 200) {
        const { email } = response.data;

        // Guardar solo el email y token, no los proyectos
        sessionStorage.setItem("gcpEmail", email);
        sessionStorage.setItem("gcpAccessToken", accessToken);

        // Configurar renovación automática
        const renewalInterval = setInterval(async () => {
          await renewToken();
        }, 5 * 60 * 1000);

        // Navegar a la página para ingresar el ID del proyecto
        navigate({ to: "/syncproject" });
      }
    } catch (error) {
      console.error("Error validating token:", error);
      setErrorMessage(
        error.response?.data?.message ||
        "Invalid or expired token. Please try again."
      );
    } finally {
      setIsLoading(false);
    }
  };
  
  // Limpiar intervalo al desmontar el componente
  useEffect(() => {
    return () => {
      const intervalId = sessionStorage.getItem("tokenRenewalIntervalId");
      if (intervalId) {
        clearInterval(Number(intervalId));
        sessionStorage.removeItem("tokenRenewalIntervalId");
      }
    };
  }, []);

  // Handle copy functionality
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
      <div className="relative w-full h-full flex flex-col">
        <div className="flex items-center justify-center flex-1 bg-gray-100 p-4">
          <div className="bg-white shadow-lg rounded-lg p-8 text-center space-y-6 w-[90%] max-w-md">
            <h1 className="text-xl font-bold">Login with Google Cloud</h1>
            <input
              type="text"
              value={accessToken}
              onChange={(e) => setAccessToken(e.target.value)}
              className="w-full px-3 py-2 border rounded"
              placeholder="Paste your gcloud auth token here"
              disabled={isLoading}
            />
            {isLoading ? (
              <div className="flex justify-center items-center mt-4">
                <div className="loader border-t-transparent border-blue-400 w-10 h-10 border-4 rounded-full animate-spin"></div>
              </div>
            ) : (
              <button
                onClick={handleValidateToken}
                className="py-2 px-4 rounded bg-blue-500 hover:bg-blue-600 text-white"
                disabled={!accessToken || isLoading}
              >
                Validate Token
              </button>
            )}
            <div className="text-sm text-gray-600 leading-relaxed bg-gray-50 p-4 rounded-md border border-gray-300">
              Run the following commands in your terminal to get the token:
              <br />
              <div className="flex items-center justify-center mt-2">
                <strong><code>gcloud auth login</code></strong>
                <button onClick={() => handleCopy('gcloud auth login', 1)} className="ml-2 relative">
                  <Copy className="w-5 h-5" />
                  {showCopied === 1 && (
                    <span className="absolute top-[-22px] right-[-10px] text-xs text-green-500 transition-opacity duration-300 bg-white px-1 rounded">
                      Copied!
                    </span>
                  )}
                </button>
              </div>
              <div className="mt-2">to select your account.</div>
              <div className="flex items-center justify-center mt-2">
                Then run <strong><code className="mx-1">gcloud auth print-access-token</code></strong>
                <button onClick={() => handleCopy('gcloud auth print-access-token', 2)} className="ml-2 relative">
                  <Copy className="w-5 h-5" />
                  {showCopied === 2 && (
                    <span className="absolute top-[-22px] right-[-10px] text-xs text-green-500 transition-opacity duration-300 bg-white px-1 rounded">
                      Copied!
                    </span>
                  )}
                </button>
              </div>
              <div className="mt-2">to get the token.</div>
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

export default LoginGoogle;

