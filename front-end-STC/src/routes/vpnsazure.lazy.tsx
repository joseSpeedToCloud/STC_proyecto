import { createLazyFileRoute } from '@tanstack/react-router'
import VpnsAzure from '../pages/resources_azure/VpnsAzure';

export const Route = createLazyFileRoute('/vpnsazure')({
  component: VpnsAzure
})