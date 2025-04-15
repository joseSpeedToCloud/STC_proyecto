import React from 'react'
import { RouterProvider } from '@tanstack/react-router'
import { routeTree } from './routeTree.gen'

function App() {
  return (
    <RouterProvider router={routeTree}>
      <div>
        <Outlet />
      </div>
    </RouterProvider>
  )
}

export default App
