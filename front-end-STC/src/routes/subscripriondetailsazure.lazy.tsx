import { createLazyFileRoute } from '@tanstack/react-router'
import SubscriptionDetailsAzure from '../pages/resources_azure/SubscriptionDetailsAzure';

export const Route = createLazyFileRoute('/subscripriondetailsazure')({
  component: SubscriptionDetailsAzure
})

