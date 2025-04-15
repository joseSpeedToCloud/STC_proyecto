import { createLazyFileRoute } from '@tanstack/react-router'
import SnapshotsGoogle from '../pages/resources_google/SnapshotsGoogle';


export const Route = createLazyFileRoute('/snapshotsgoogle')({
  component: SnapshotsGoogle
})