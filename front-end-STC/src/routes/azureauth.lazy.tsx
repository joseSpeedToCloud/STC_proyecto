import { createLazyFileRoute } from '@tanstack/react-router'
import AzureAuth from '../pages/resources_azure/AzureAuth';

export const Route = createLazyFileRoute('/azureauth')({
  component: AzureAuth
})