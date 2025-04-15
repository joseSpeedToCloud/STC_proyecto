import { createLazyFileRoute } from '@tanstack/react-router'
import VirtualMachinesAzure from '../pages/resources_azure/VirtualMachinesAzure';

export const Route = createLazyFileRoute('/virtualmachinesazure')({
  component: VirtualMachinesAzure
})