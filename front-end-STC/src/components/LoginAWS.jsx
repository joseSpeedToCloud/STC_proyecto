import React, { useState, useEffect, useCallback } from "react";
import axios from "axios";
import { useNavigate } from "@tanstack/react-router";
import Sidebar from '../components/Sidebar';
import { Copy } from "lucide-react";

const LoginAWS = () => {
  const [accessToken, setAccessToken] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [showCopied, setShowCopied] = useState(null);
  const navigate = useNavigate();

  // Verifica si el token ya existe al cargar el componente
  useEffect(() => {
    const storedToken = sessionStorage.getItem("awsToken");
    if (storedToken) {
      navigate({ to: "/SyncAccountAWS" });
    }
  }, [navigate]);

  // Función para renovar el token
  const renewCredentials = useCallback(async () => {
    try {
      const response = await axios.get("http://localhost:8080/api/aws/renew-token-aws");

      if (response.status === 200) {
        const { credentials, accountId } = response.data;
        
        sessionStorage.setItem("awsToken", JSON.stringify(credentials));
        sessionStorage.setItem("awsAccountId", accountId);

        console.log("Credenciales de AWS renovadas exitosamente");
        return credentials;
      }
    } catch (error) {
      console.error("Error renovando credenciales AWS:", error);
      sessionStorage.removeItem("awsToken");
      sessionStorage.removeItem("awsAccountId");
      navigate({ to: "/LoginAWS" });
    }
  }, [navigate]);

  // Renovación automática del token
  useEffect(() => {
    const storedToken = sessionStorage.getItem("awsToken");

    if (storedToken) {
      const renewalInterval = setInterval(async () => {
        await renewCredentials();
      }, 5 * 60 * 1000); // Renovar cada 5 minutos

      sessionStorage.setItem("credentialRenewalIntervalIdAWS", renewalInterval.toString());

      return () => clearInterval(renewalInterval);
    }
  }, [renewCredentials]);

  // Limpiar intervalo al desmontar el componente
  useEffect(() => {
    return () => {
      const intervalId = sessionStorage.getItem("credentialRenewalIntervalIdAWS");
      if (intervalId) {
        clearInterval(Number(intervalId));
        sessionStorage.removeItem("credentialRenewalIntervalIdAWS");
      }
    };
  }, []);

  // Función para validar el token
  const handleValidateToken = async () => {
    setIsLoading(true);
    setErrorMessage("");
    
    try {
      // Parsea las credenciales del token ingresado
      const credentialsObj = JSON.parse(accessToken);
      
      const response = await axios.post("http://localhost:8080/api/aws/validate-token-aws", {
        accessKeyId: credentialsObj.accessKeyId,
        secretAccessKey: credentialsObj.secretAccessKey,
        sessionToken: credentialsObj.sessionToken
      });

      if (response.status === 200) {
        const { accountId, email } = response.data;
        
        // Guarda credenciales y datos de la cuenta
        sessionStorage.setItem("awsToken", accessToken);
        sessionStorage.setItem("awsAccountId", accountId);
        sessionStorage.setItem("awsEmail", email);

        // Redirige a la página de sincronización
        navigate({ to: "/SyncAccountAWS" });
      }
    } catch (error) {
      console.error("Error validando token AWS:", error);
      setErrorMessage(
        error.response?.data?.message ||
        "Credenciales de AWS inválidas o expiradas. Por favor intenta nuevamente."
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
      console.error("Error copiando:", err);
    }
  };

  return (
    <Sidebar showBackButton={true}>
      <div className="relative w-full h-full flex flex-col">
        <div className="flex items-center justify-center flex-1 bg-gray-100 p-4">
          <div className="bg-white shadow-lg rounded-lg p-8 text-center space-y-6 w-[90%] max-w-md">
            <h1 className="text-xl font-bold">Iniciar sesión con AWS</h1>
            
            <textarea
              value={accessToken}
              onChange={(e) => setAccessToken(e.target.value)}
              className="w-full px-3 py-2 border rounded min-h-32"
              placeholder="Pega tu JSON de credenciales AWS aquí"
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
                Validar Credenciales
              </button>
            )}

            <div className="text-sm text-gray-600 leading-relaxed bg-gray-50 p-3 rounded-md border border-gray-300 space-y-2">
              <p>Ejecuta los siguientes comandos en tu terminal para obtener credenciales AWS:</p>
              <div className="flex items-center justify-center">
                <strong><code>aws configure</code></strong>
                <button onClick={() => handleCopy('aws configure', 0)} className="ml-2 relative">
                  <Copy className="w-5 h-5" />
                  {showCopied === 0 && (
                    <span className="absolute top-[-22px] right-[-10px] text-xs text-green-500 transition-opacity duration-300 bg-white px-1 rounded">
                      Copiado!
                    </span>
                  )}
                </button>
              </div>
              <div className="flex items-center justify-center">
                <span>para configurar tus credenciales AWS.</span>
              </div>
              <div className="flex items-center justify-center">
                <strong><code>aws sts get-session-token --output json</code></strong>
                <button onClick={() => handleCopy('aws sts get-session-token --output json', 1)} className="ml-2 relative">
                  <Copy className="w-5 h-5" />
                  {showCopied === 1 && (
                    <span className="absolute top-[-22px] right-[-10px] text-xs text-green-500 transition-opacity duration-300 bg-white px-1 rounded">
                      Copiado!
                    </span>
                  )}
                </button>
              </div>
              <div className="flex items-center justify-center">
                <span>Copia el objeto JSON completo de la respuesta.</span>
              </div>
              <div className="mt-2 p-2 bg-gray-200 rounded text-xs">
                <p>Ejemplo de formato JSON:</p>
                <pre className="text-left">
{`{
  "accessKeyId": "ASIA...",
  "secretAccessKey": "Tu+Clave+Secreta",
  "sessionToken": "Token+De+Sesión+Muy+Largo..."
}`}
                </pre>
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

