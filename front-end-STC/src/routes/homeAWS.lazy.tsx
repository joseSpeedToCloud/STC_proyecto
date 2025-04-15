import { createLazyFileRoute } from '@tanstack/react-router'
import HomeAWS from '../pages/resources_aws/homeAWS';

export const Route = createLazyFileRoute('/homeAWS')({
  component: HomeAWS
})

