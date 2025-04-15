import { createLazyFileRoute } from '@tanstack/react-router'
import SnapshotsAzure from '../pages/resources_azure/SnapshotsAzure';

export const Route = createLazyFileRoute('/snapshotsazure')({
  component: SnapshotsAzure
})