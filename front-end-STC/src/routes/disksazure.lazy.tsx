import { createLazyFileRoute } from '@tanstack/react-router'
import DisksAzure from '../pages/resources_azure/DisksAzure';

export const Route = createLazyFileRoute('/disksazure')({
  component: DisksAzure
})