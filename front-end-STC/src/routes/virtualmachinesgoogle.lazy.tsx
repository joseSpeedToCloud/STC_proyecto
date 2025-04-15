import { createLazyFileRoute } from '@tanstack/react-router'
import VirtualMachines from '../pages/resources_google/VirtualMachinesGoogle';

export const Route = createLazyFileRoute('/virtualmachinesgoogle')({
  component: VirtualMachines,
})