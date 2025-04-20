import React, { useState, useEffect } from 'react';
import Sidebar from '../../components/Sidebar';
import axios from 'axios';
import ExportarDatosArchivos from '../../components/export_datos_archivos';

const ProjectDetails = () => {
  const [totals, setTotals] = useState({
    virtualMachines: 0,
    networks: 0,
    firewalls: 0,
    loadBalancers: 0,
    disks: 0,
    snapshots: 0,
    vpns: 0,
  });

  const [projectDisplayName, setProjectDisplayName] = useState('');
  const [loadingTotals, setLoadingTotals] = useState(true);
  const [errorTotals, setErrorTotals] = useState(null);

  const routes = {
    virtualMachines: '/virtualmachinesgoogle',
    networks: '/networksgoogle',
    firewalls: '/firewallsgoogle',
    loadBalancers: '/loadbalancersgoogle',
    disks: '/disksgoogle',
    snapshots: '/snapshotsgoogle',
    vpns: '/vpnsgoogle',
  };

  useEffect(() => {
    const fetchTotals = async () => {
      setLoadingTotals(true);
      setErrorTotals(null);

      const selectedProjectId = sessionStorage.getItem("selectedProject");
      const accessToken = sessionStorage.getItem("gcpAccessToken");
      const selectedProjectDisplayName = sessionStorage.getItem("selectedProjectDisplayName");

      if (!selectedProjectId || !accessToken) {
        setErrorTotals("No se ha seleccionado un proyecto o la sesión ha expirado.");
        setLoadingTotals(false);
        return;
      }

      setProjectDisplayName(selectedProjectDisplayName);

      const totalsData = { ...totals };

      try {
        const fetchResource = async (resource) => {
          const response = await axios.get(`http://localhost:8080/api/cloud/${resource}`, {
            headers: { Authorization: `Bearer ${accessToken}` },
            params: { projectId: selectedProjectId },
          });
          return response.data;
        };

        // Fetch y filtrar recursos por project ID
        totalsData.virtualMachines = (await fetchResource('virtualmachinesgoogle')).length;
        totalsData.networks = (await fetchResource('networksgoogle')).length;
        totalsData.firewalls = (await fetchResource('firewallsgoogle')).length;
        totalsData.loadBalancers = (await fetchResource('loadbalancersgoogle')).length;
        totalsData.disks = (await fetchResource('disksgoogle')).length;
        totalsData.snapshots = (await fetchResource('snapshotsgoogle')).length;
        totalsData.vpns = (await fetchResource('vpnsgoogle')).length;

        setTotals(totalsData);
      } catch (error) {
        console.error('Error al cargar los datos del proyecto:', error);
        setErrorTotals('No se pudieron cargar los detalles del proyecto.');
      } finally {
        setLoadingTotals(false);
      }
    };

    fetchTotals();
  }, []);

  // Preparar los datos para exportar
  const prepareExportData = () => {
    return Object.entries(totals).map(([key, value]) => ({
      recurso: key.charAt(0).toUpperCase() + key.slice(1).replace(/([A-Z])/g, ' $1'),
      cantidad: value
    }));
  };

  // Definir columnas para exportar
  const columns = [
    { header: 'Recurso', accessor: 'recurso' },
    { header: 'Cantidad', accessor: 'cantidad' }
  ];

  if (loadingTotals) {
    return (
      <Sidebar showBackButton={true}>
        <div className="flex justify-center items-center h-full">
          <div className="loader border-t-transparent border-blue-400 w-10 h-10 border-4 rounded-full animate-spin"></div>
        </div>
      </Sidebar>
    );
  }

  if (errorTotals) {
    return (
      <Sidebar showBackButton={true}>
        <div className="text-red-500 p-4">{errorTotals}</div>
      </Sidebar>
    );
  }

  return (
    <Sidebar showBackButton={true}>
      <div className="p-6">
        <h1 className="text-3xl font-bold mb-6 text-[#3F9BB9]">
          Google Cloud
        </h1>
        <h1 className="text-2xl font-bold mb-6 text-[#3F9BB9]">
          Recursos del Proyecto {projectDisplayName}
        </h1>
        <div className="flex flex-wrap mt-8">
          {Object.entries(totals).map(([key, value]) => (
            <a
              key={key}
              href={routes[key]}
              className="w-1/5 sm:w-1/2 md:w-1/5 flex flex-row justify-between items-start
              py-6 px-6 border-2 rounded-md font-sans text-lg mx-2 mb-6
              text-left font-semibold text-xl text-[#3F9BB9] hover:shadow-lg transition-shadow duration-200"
            >
              <div className="flex-1">
                <div>
                  {key.charAt(0).toUpperCase() + key.slice(1).replace(/([A-Z])/g, ' $1')}
                </div>
                <p className="text-right text-1xl font-bold text-[#0B6A8D]">
                  {value}
                </p>
              </div>
            </a>
          ))}
          {errorTotals && <div className="text-red-500 mt-4">{errorTotals}</div>}
        </div>
        <div className="mt-6">
          <ExportarDatosArchivos
            data={prepareExportData()}
            columns={columns}
            filename={`Proyecto_${projectDisplayName}`}
            title={`Detalles del Proyecto: ${projectDisplayName}`}
            subtitle="Resumen de Recursos"
          />
        </div>
      </div>
    </Sidebar>
  );
};

export default ProjectDetails;

