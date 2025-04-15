import { createLazyFileRoute } from '@tanstack/react-router'
import SubscriptionsAzure from '../pages/resources_azure/SubscriptionsAzure';

export const Route = createLazyFileRoute('/subscriptionsazure')({
  component: SubscriptionsAzure
})