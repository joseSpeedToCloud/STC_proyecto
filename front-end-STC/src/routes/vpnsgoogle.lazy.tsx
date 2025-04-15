import { createLazyFileRoute } from '@tanstack/react-router'
import VpnsGoogle from '../pages/resources_google/VpnsGoogle';


export const Route = createLazyFileRoute('/vpnsgoogle')({
  component: VpnsGoogle
})