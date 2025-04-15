import { createLazyFileRoute } from '@tanstack/react-router'
import HomeAzure from '../pages/resources_azure/HomeAzure';

export const Route = createLazyFileRoute('/homeazure')({
  component: HomeAzure
})