import { createLazyFileRoute } from '@tanstack/react-router'
import LoginAzure from '../components/LoginAzure'

export const Route = createLazyFileRoute('/LoginAzure')({
  component: LoginAzure
})