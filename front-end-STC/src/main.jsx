import React, { StrictMode } from 'react';
import ReactDOM from 'react-dom/client';
import './index.css'; /*esta importación es para que aplique tailwind*/
import { RouterProvider, createRouter } from '@tanstack/react-router';

// Import the generated route tree
import { routeTree } from './routeTree.gen';

// Create a new router instance
const router = createRouter({ routeTree });

// Render the app
const rootElement = document.getElementById('root');
if (rootElement) {
  // Create the root if it doesn't already have content
  if (!rootElement.innerHTML) {
    const root = ReactDOM.createRoot(rootElement);
    root.render(
      <StrictMode>
        <RouterProvider router={router} />
      </StrictMode>,
    );
  }
} else {
  console.error("Root element not found");
}
