import express from 'express';
import cors from 'cors';
import path from 'path';
import { fileURLToPath } from 'url';

const app = express();
const port = 8080;

// Configuración de CORS
app.use(cors({
  origin: 'http://localhost:4173', // Permitir solo el origen de tu frontend
  methods: ['GET', 'POST', 'PUT', 'DELETE'], // Métodos permitidos
  credentials: true // Permitir credenciales si es necesario
}));

// Servir archivos estáticos de la carpeta 'dist' si es necesario
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
app.use(express.static(path.join(__dirname, 'dist')));

// Enviar el archivo HTML de la aplicación React para cualquier otra ruta
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist', 'index.html'));
});

// Iniciar el servidor
app.listen(port, () => {
  console.log(`Servidor corriendo en http://localhost:${port}`);
});
