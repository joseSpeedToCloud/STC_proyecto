// import path from "path"
// import { defineConfig } from 'vite'
// import { TanStackRouterVite } from '@tanstack/router-vite-plugin'
// import react from '@vitejs/plugin-react'

// // https://vitejs.dev/config/
// export default defineConfig({
//   plugins: [react(),
//   TanStackRouterVite(),
//   ],
//   resolve: {
//     alias: {
//       "@": path.resolve(__dirname, "./src"),
//     },
//   },
// server: {
//   host:true,
//   port: 5173,  // Puerto para el servidor de desarrollo de Vite
// },
// })
import path from "path";
import { defineConfig } from 'vite';
import { TanStackRouterVite } from '@tanstack/router-vite-plugin';
import react from '@vitejs/plugin-react';

// https://vitejs.dev/config/
export default defineConfig({
  root: '.',
  plugins: [
    react(),
    TanStackRouterVite(),
  ],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    host: true,
    port: 4173,  // Puerto para el servidor de producción de Vite, desarrollo 5173
  },
  build: {
    outDir: 'dist',  // La carpeta donde se genera el build final
    emptyOutDir: true,  // Vacía el directorio de salida antes de construir
  },
});
