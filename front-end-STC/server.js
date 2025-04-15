const express = require('express');
const { google } = require('google-auth-library');
const fetch = require('node-fetch');

const app = express();

// Credenciales del cliente
const client = new google.auth.GoogleAuth({
  scopes: [
    'https://www.googleapis.com/auth/devstorage.read_only',
    'https://www.googleapis.com/auth/logging.write',
    'https://www.googleapis.com/auth/monitoring',
    'https://www.googleapis.com/auth/service.management.readonly',
    'https://www.googleapis.com/auth/servicecontrol',
    'https://www.googleapis.com/auth/trace.append'
  ]
});

app.get('/api/clusters', async (req, res) => {
  try {
    const authClient = await client.getClient();
    const projectId = await client.getProjectId();

    const response = await fetch(`https://container.googleapis.com/v1/projects/${projectId}/locations/-/clusters`, {
      headers: {
        Authorization: `Bearer ${authClient.credentials.access_token}`,
      },
    });

    const data = await response.json();
    res.json(data);
  } catch (error) {
    console.error('Error fetching clusters:', error);
    res.status(500).send('Error fetching clusters');
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
