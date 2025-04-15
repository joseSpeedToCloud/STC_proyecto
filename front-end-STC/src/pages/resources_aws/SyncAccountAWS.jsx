import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "@tanstack/react-router";
import Sidebar from '../../components/Sidebar';
import { Copy } from "lucide-react";

const SyncAWSAccount = () => {
  const [accessKeyId, setAccessKeyId] = useState("");
  const [secretAccessKey, setSecretAccessKey] = useState("");
  const [sessionToken, setSessionToken] = useState("");
  const [accountName, setAccountName] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [errorMessage, setErrorMessage] = useState("");
  const [showCopied, setShowCopied] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    // Verifica si el usuario ya está autenticado con AWS
    const accessKey = sessionStorage.getItem("awsAccessKeyId");
    if (!accessKey) {
      navigate({ to: "/LoginAWS" });
    }
  }, [navigate]);

  const handleSyncAccount = async () => {
    setIsLoading(true);
    setErrorMessage("");
    setProgress(0);

    try {
      const storedAccessKeyId = sessionStorage.getItem("awsAccessKeyId") || accessKeyId;
      const storedSecretAccessKey = sessionStorage.getItem("awsSecretAccessKey") || secretAccessKey;
      const storedSessionToken = sessionStorage.getItem("awsSessionToken") || sessionToken;
      const email = sessionStorage.getItem("awsEmail") || "";

      // Intervalo para simular progreso
      const progressInterval = setInterval(() => {
        setProgress(prev => Math.min(prev + 20, 90));
      }, 500);

      // Primero valida las credenciales de AWS
      const validationResponse = await axios.post(
        "http://localhost:8080/api/aws/validate-token-aws",
        {
          accessKeyId: storedAccessKeyId,
          secretAccessKey: storedSecretAccessKey,
          sessionToken: storedSessionToken
        }
      );

      if (validationResponse.status === 200) {
        // Si son válidas, sincroniza la cuenta de AWS
        const response = await axios.post(
          "http://localhost:8080/api/aws/save-account",
          {
            accessKeyId: storedAccessKeyId,
            secretAccessKey: storedSecretAccessKey,
            sessionToken: storedSessionToken,
            email: email,
            accountName: accountName || "AWS Account"
          }
        );

        clearInterval(progressInterval);
        setProgress(100);

        if (response.status === 200) {
          const accountId = response.data.accountId;
          sessionStorage.setItem("selectedAWSAccount", accountId);
          sessionStorage.setItem("selectedAWSAccountName", accountName || accountId);
          setTimeout(() => navigate({ to: "/HomeAWS" }), 500);
        }
      }
    } catch (error) {
      console.error("Error sincronizando cuenta AWS:", error);
      setErrorMessage(
        error.response?.data?.message || "Fallo al sincronizar cuenta AWS. Por favor verifica tus credenciales e intenta nuevamente."
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
      setTimeout(() => setShowCopied(null), 2000); // "Copiado!" desaparece después de 2 segundos
    } catch (err) {
      console.error("Error copiando texto:", err);
    }
  };

  return (
    <Sidebar showBackButton={true}>
      <div className="flex items-center justify-center flex-1 bg-gray-100 p-4">
        <div className="bg-white shadow-lg rounded-lg p-8 text-center space-y-6 w-[90%] max-w-md">
          <h1 className="text-xl font-bold">Configuración de Cuenta AWS</h1>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Access Key ID</label>
              <input
                type="text"
                value={accessKeyId}
                onChange={(e) => setAccessKeyId(e.target.value)}
                className="w-full px-3 py-2 border rounded"
                placeholder="Ingresa tu AWS Access Key ID"
                disabled={isLoading}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Secret Access Key</label>
              <input
                type="password"
                value={secretAccessKey}
                onChange={(e) => setSecretAccessKey(e.target.value)}
                className="w-full px-3 py-2 border rounded"
                placeholder="Ingresa tu AWS Secret Access Key"
                disabled={isLoading}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Session Token (opcional)</label>
              <input
                type="password"
                value={sessionToken}
                onChange={(e) => setSessionToken(e.target.value)}
                className="w-full px-3 py-2 border rounded"
                placeholder="Ingresa tu AWS Session Token"
                disabled={isLoading}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Nombre de Cuenta (opcional)</label>
              <input
                type="text"
                value={accountName}
                onChange={(e) => setAccountName(e.target.value)}
                className="w-full px-3 py-2 border rounded"
                placeholder="Ingresa un nombre para mostrar"
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
              onClick={handleSyncAccount}
              className="py-2 px-4 rounded bg-blue-500 hover:bg-blue-600 text-white"
              disabled={(!accessKeyId || !secretAccessKey) && !sessionStorage.getItem("awsAccessKeyId") || isLoading}
            >
              Sincronizar Cuenta AWS
            </button>
          )}
          <div className="text-sm text-gray-500 mt-4">
            <div className="bg-gray-50 p-4 rounded-md border border-gray-300 text-left">
              <p className="font-medium mb-2">Cómo encontrar tus credenciales de AWS:</p>
              <p className="mb-2">1. Usando AWS CLI:</p>
              <div className="text-sm text-gray-600 leading-relaxed bg-gray-50 p-4 rounded-md border border-gray-300">
                Ejecuta el siguiente comando en tu terminal:
                <br />
                <div className="flex items-center justify-center">
                  <strong><code>aws configure get aws_access_key_id</code></strong>
                  <button onClick={() => handleCopy('aws configure get aws_access_key_id', 1)} className="ml-2 relative">
                    <Copy className="w-5 h-5" />
                    {showCopied === 1 && (
                      <span className="absolute top-[-22px] right-[-10px] text-xs text-green-500 transition-opacity duration-300 bg-white px-1 rounded">
                        Copiado!
                      </span>
                    )}
                  </button>
                </div>
              </div>
              <p className="mt-4 mb-2">2. En AWS Management Console:</p>
              <p>IAM → Usuarios → Selecciona tu usuario → Credenciales de seguridad → Claves de acceso</p>
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

export default SyncAWSAccount;



