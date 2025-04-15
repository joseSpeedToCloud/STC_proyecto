import React, { useState, useEffect, useCallback } from "react";
import axios from "axios";
import { useNavigate } from "@tanstack/react-router";
import Sidebar from '../components/Sidebar';
import { Copy } from "lucide-react";

const LoginAzure = () => {
  const [accessToken, setAccessToken] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [showCopied, setShowCopied] = useState(null); // To manage the "copied" message
  const navigate = useNavigate();

  // Verificar si ya hay un token guardado al cargar el componente
  useEffect(() => {
    const storedToken = sessionStorage.getItem("azureAccessToken");
    const hasCompletedSync = sessionStorage.getItem("azureSyncCompleted");
    
    if (storedToken && hasCompletedSync === "true") {
      // Si ya hay un token y se ha completado la sincronización, redirigir a la página de suscripciones
      navigate({ to: "/homeazure" });
    } else if (storedToken && !hasCompletedSync) {
      // Si hay token pero no se ha completado la sincronización, redirigir a la página de sincronización
      navigate({ to: "/syncAzureSubscription" });
    }
  }, [navigate]);

  // Función para renovar el token
  const renewToken = useCallback(async () => {
    try {
      const response = await axios.get("http://localhost:8080/api/cloud/renew-token-azure");

      if (response.status === 200) {
        const { accessToken: newToken, email } = response.data;

        // Actualizar el token en sessionStorage
        sessionStorage.setItem("azureAccessToken", newToken);
        sessionStorage.setItem("azureEmail", email);

        console.log("Token renovado correctamente");
        return newToken;
      }
    } catch (error) {
      console.error("Error renovando token:", error);
      // Manejar error de renovación (opcional: desloguear al usuario)
      sessionStorage.removeItem("azureAccessToken");
      sessionStorage.removeItem("azureEmail");
      sessionStorage.removeItem("subscriptions");
      sessionStorage.removeItem("selectedSubscription");
      sessionStorage.removeItem("selectedSubscriptionDisplayName");
      sessionStorage.removeItem("azureSyncCompleted");
      navigate({ to: "/azureAuth" });
    }
  }, [navigate]);

  // Hook para renovación automática de token
  useEffect(() => {
    // Verificar si hay un token guardado
    const storedToken = sessionStorage.getItem("azureAccessToken");

    if (storedToken) {
      // Configurar intervalo de renovación
      const renewalInterval = setInterval(async () => {
        await renewToken();
      }, 5 * 60 * 1000); // Renovar cada 5 minutos

      // Guardar el ID del intervalo en sessionStorage
      sessionStorage.setItem("tokenRenewalIntervalIdAzure", renewalInterval.toString());

      // Limpiar intervalo al desmontar
      return () => clearInterval(renewalInterval);
    }
  }, [renewToken]);

  const handleValidateToken = async () => {
    setIsLoading(true);
    setErrorMessage("");
    try {
      const response = await axios.post(
        "http://localhost:8080/api/cloud/validate-token-azure",
        { token: accessToken }
      );

      if (response.status === 200) {
        const { email, subscriptions } = response.data;

        // Guardar datos en sessionStorage
        sessionStorage.setItem("azureEmail", email);
        sessionStorage.setItem("azureAccessToken", accessToken);
        // Marcar explícitamente que la sincronización NO se ha completado aún
        sessionStorage.removeItem("azureSyncCompleted");

        // Guardar las suscripciones si existen
        if (subscriptions && Array.isArray(subscriptions)) {
          sessionStorage.setItem("subscriptions", JSON.stringify(subscriptions));

          // Si hay suscripciones, guardar la primera como seleccionada por defecto
          if (subscriptions.length > 0) {
            const firstSub = subscriptions[0];
            sessionStorage.setItem("selectedSubscription", firstSub.subscriptionId);
            sessionStorage.setItem("selectedSubscriptionDisplayName", firstSub.displayName);

            if (firstSub.tenantId) {
              sessionStorage.setItem("azureTenantId", firstSub.tenantId);
            }
          }
        }

        // Redirect to subscription sync page
        navigate({ to: "/syncAzureSubscription" });
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
      const intervalId = sessionStorage.getItem("tokenRenewalIntervalIdAzure");
      if (intervalId) {
        clearInterval(Number(intervalId));
        sessionStorage.removeItem("tokenRenewalIntervalIdAzure");
      }
    };
  }, []);

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
            <h1 className="text-xl font-bold">Login with Azure</h1>
            <input
              type="text"
              value={accessToken}
              onChange={(e) => setAccessToken(e.target.value)}
              className="w-full px-3 py-2 border rounded"
              placeholder="Paste your Azure access token here"
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
            <div className="text-sm text-gray-600 leading-relaxed bg-gray-50 p-3 rounded-md border border-gray-300 space-y-2">
              <p>Run the following commands in your terminal to get the token:</p>
              <div className="flex items-center justify-center">
                <strong><code>az login</code></strong>
                <button onClick={() => handleCopy('az login', 0)} className="ml-2 relative">
                  <Copy className="w-5 h-5" />
                  {showCopied === 0 && (
                    <span className="absolute top-[-22px] right-[-10px] text-xs text-green-500 transition-opacity duration-300 bg-white px-1 rounded">
                      Copiado!
                    </span>
                  )}
                </button>
              </div>
              <div className="flex items-center justify-center">
                <span>to authenticate with Azure.</span>
              </div>
              <div className="flex items-center justify-center">
                <span>Then run:</span>
              </div>
              <div className="flex items-center justify-center">
                <strong><code>az account get-access-token --query accessToken --output tsv</code></strong>
                <button onClick={() => handleCopy('az account get-access-token --query accessToken --output tsv', 1)} className="ml-2 relative">
                  <Copy className="w-5 h-5" />
                  {showCopied === 1 && (
                    <span className="absolute top-[-22px] right-[-10px] text-xs text-green-500 transition-opacity duration-300 bg-white px-1 rounded">
                      Copiado!
                    </span>
                  )}
                </button>
              </div>
              <div className="flex items-center justify-center">
                <span>and copy the accessToken value.</span>
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

export default LoginAzure;

