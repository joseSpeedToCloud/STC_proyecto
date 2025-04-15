import { createLazyFileRoute } from '@tanstack/react-router'
import LoadBalancersAzure from '../pages/resources_azure/LoadBalancersAzure';

export const Route = createLazyFileRoute('/loadbalancersazure')({
  component: LoadBalancersAzure
})