import { createLazyFileRoute } from '@tanstack/react-router'
import NetworkSecurityGroupAzure from '../pages/resources_azure/NetworkSecurityGroupAzure';

export const Route = createLazyFileRoute('/networkssecurityazure')({
  component: NetworkSecurityGroupAzure
})

