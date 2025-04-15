import { createLazyFileRoute } from '@tanstack/react-router'
import SyncAzureSubscription from '../pages/resources_azure/SyncAzureSubscription';

export const Route = createLazyFileRoute('/syncazuresubscription')({
  component: SyncAzureSubscription
})