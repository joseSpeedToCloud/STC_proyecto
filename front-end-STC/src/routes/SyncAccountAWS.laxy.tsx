import { createLazyFileRoute } from '@tanstack/react-router'
import SyncAccountAWS from '../pages/resources_aws/SyncAccountAWS'

export const Route = createLazyFileRoute('/SyncAccountAWS')({
  component: SyncAccountAWS
})


