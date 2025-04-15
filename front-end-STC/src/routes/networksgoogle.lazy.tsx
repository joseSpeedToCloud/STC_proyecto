import { createLazyFileRoute } from '@tanstack/react-router'
import NetworksGoogle from '../pages/resources_google/NetworksGoogle';

export const Route = createLazyFileRoute('/networksgoogle')({
  component: NetworksGoogle
})