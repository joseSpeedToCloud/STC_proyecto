import { createLazyFileRoute } from '@tanstack/react-router'
import NetworksAzure from '../pages/resources_azure/NetworksAzure';

export const Route = createLazyFileRoute('/networksazure')({
  component: NetworksAzure
})