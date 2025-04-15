import { createFileRoute } from '@tanstack/react-router'
import SyncAccountAWS from '../pages/resources_aws/SyncAccountAWS'

export const Route = createFileRoute('/SyncAccountAWS/laxy')({
  component: SyncAccountAWS
})


