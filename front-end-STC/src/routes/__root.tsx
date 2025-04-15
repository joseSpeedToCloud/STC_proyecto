import { createRootRoute, Outlet, useNavigate } from '@tanstack/react-router'
import { useEffect } from 'react'
import { TanStackRouterDevtools } from '@tanstack/router-devtools'
import React from 'react'

// Variable para evitar guardar rutas no válidas durante redirecciones
let isRedirecting = false;

// Mantener un registro de las rutas válidas visitadas
export const Route = createRootRoute({
  component: () => {
    useEffect(() => {
      // Solo guardamos la ruta cuando se renderiza un componente válido
      // y no estamos en medio de una redirección
      if (!isRedirecting) {
        const currentPath = window.location.pathname
        
        // No guardar la ruta inicial (login) como última ruta
        if (currentPath !== '/') {
          sessionStorage.setItem('lastValidPath', currentPath)
        }
      }
      
      // Resetear el flag de redirección después de un breve momento
      setTimeout(() => {
        isRedirecting = false
      }, 100)
    }, [window.location.pathname])
    
    return (
      <>
        <Outlet />
        {process.env.NODE_ENV === 'development' && <TanStackRouterDevtools />}
      </>
    )
  },
  
  // Manejador de rutas no encontradas
  notFoundComponent: () => {
    const navigate = useNavigate()
    
    useEffect(() => {
      // Establecer el flag de redirección
      isRedirecting = true
      
      // Obtener la última ruta válida
      const lastValidPath = sessionStorage.getItem('lastValidPath')
      
      // Si existe una última ruta válida, redireccionar a ella
      // De lo contrario, ir a la página principal (login)
      if (lastValidPath) {
        navigate({ to: lastValidPath, replace: true })
      } else {
        navigate({ to: '/', replace: true })
      }
    }, [navigate])
    
    // Mientras se realiza la redirección
    return (
      <div className="flex items-center justify-center h-screen">
        <p className="text-xl">Redireccionando a la última página visitada</p>
      </div>
    )
  }
})


