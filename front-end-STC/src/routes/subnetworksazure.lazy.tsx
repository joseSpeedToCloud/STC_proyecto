import { createLazyFileRoute } from '@tanstack/react-router'
import SubnetworksAzure from '../pages/resources_azure/SubnetworksAzure';

export const Route = createLazyFileRoute('/subnetworksazure')({
  component: SubnetworksAzure
})