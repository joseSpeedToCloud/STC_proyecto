import { createLazyFileRoute } from '@tanstack/react-router'
import AksClustersAzure from '../pages/resources_azure/AksClusterAzure';

export const Route = createLazyFileRoute('/aksclusterazure')({
  component: AksClustersAzure
})

