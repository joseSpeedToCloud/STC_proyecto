import { createLazyFileRoute } from '@tanstack/react-router'
import DisksGoogle from '../pages/resources_google/DisksGoogle';

export const Route = createLazyFileRoute('/disksgoogle')({
  component: DisksGoogle,
})