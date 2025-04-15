import React, { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";

const Home = () => {
  const [showInstructions, setShowInstructions] = useState(false);

  useEffect(() => {
    const firstLogin = localStorage.getItem("firstLogin") === "true";
    if (firstLogin) {
      setShowInstructions(true);
      localStorage.setItem("firstLogin", "false");
    }
  }, []);

  const role = localStorage.getItem("userRole");

  return (
    <Sidebar showBackButton={true}>
      {showInstructions && (
        <div className="p-4 bg-blue-100 rounded-lg shadow-md mb-6">
          <h2 className="font-bold text-lg text-blue-800">Instrucciones de uso</h2>
          {role === "ADMINISTRADOR" ? (
            <ul className="list-disc ml-4">
              <li>Selecciona un proveedor de nube en el menú lateral.</li>
              <li>Puedes crear nuevos usuarios en la sección de Usuarios.</li>
              <li>Genera tokens para extraer información de proyectos en la sección de Autenticación.</li>
              <li>Configura credenciales globales en la sección de Global Settings.</li>
            </ul>
          ) : (
            <ul className="list-disc ml-4">
              <li>Selecciona un proveedor de nube en el menú lateral.</li>
              <li>Puedes extraer información de proyectos utilizando el token generado por un administrador.</li>
              <li>Revisa los detalles de los proyectos y el uso de recursos en cada plataforma.</li>
            </ul>
          )}
          <button
            onClick={() => setShowInstructions(false)}
            className="mt-4 py-2 px-4 bg-blue-600 text-white rounded-md"
          >
            Cerrar
          </button>
        </div>
      )}
      <div className="flex flex-wrap lg:flex-nowrap">
        <div className="w-full lg:w-3/4 overflow-x-auto">
          <h1 className="text-2xl mb-6 ml-6 font-bold text-[#3F9BB9]">Welcome to the Speed to Cloud App</h1>
          
          {/* Reemplazando la imagen con instrucciones */}
          <div className="bg-white p-6 rounded-lg shadow-md mx-6">
            <h2 className="text-xl font-semibold text-[#3F9BB9] mb-4">Primeros pasos</h2>
            
            <div className="space-y-6">
              <div className="p-4 bg-gray-50 rounded-lg border border-gray-200">
                <h3 className="font-medium text-lg mb-2">¿Cómo empezar?</h3>
                <ol className="list-decimal ml-5 space-y-2">
                  <li>Selecciona un proveedor de nube desde el menú lateral (Google Cloud, Amazon AWS o Azure)</li>
                  <li>Autentica con tus credenciales del proveedor seleccionado</li>
                  <li>Explora los proyectos y recursos disponibles</li>
                </ol>
              </div>
              
              <div className="p-4 bg-blue-50 rounded-lg border border-blue-200">
                <h3 className="font-medium text-lg mb-2">Proveedores disponibles</h3>
                <ul className="list-disc ml-5">
                  <li>Google Cloud Platform</li>
                  <li>Amazon Web Services</li>
                  <li>Microsoft Azure</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
        <div className="w-full lg:w-1/5 p-4 mt-4 lg:mt-0 lg:ml-10 bg-gray-100 rounded-lg shadow-sm shadow-[#3F9BB9]">
          <h2 className="text-xl mb-4 font-bold text-[#3F9BB9]">Links</h2>
          <ul className="space-y-2">
            <li><a href="#" className="text-[#3F9BB9] hover:underline">Confluence</a></li>
            <li><a href="#" className="text-[#3F9BB9] hover:underline">Slack</a></li>
          </ul>
        </div>
      </div>
    </Sidebar>
  );
};

export default Home;


