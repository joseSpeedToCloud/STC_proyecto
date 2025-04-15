import { createLazyFileRoute } from '@tanstack/react-router'
import SyncProject from '../pages/resources_google/SyncProject'

export const Route = createLazyFileRoute('/syncProject')({
  component: SyncProject
})