import { createLazyFileRoute } from '@tanstack/react-router'
import FirewallsGoogle from '../pages/resources_google/FirewallsGoogle';

export const Route = createLazyFileRoute('/firewallsgoogle')({
  component: FirewallsGoogle
})