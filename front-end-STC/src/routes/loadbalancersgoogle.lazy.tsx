import { createLazyFileRoute } from '@tanstack/react-router'
import LoadBalancersGoogle from '../pages/resources_google/LoadBalancersGoogle';


export const Route = createLazyFileRoute('/loadbalancersgoogle')({
  component: LoadBalancersGoogle
})