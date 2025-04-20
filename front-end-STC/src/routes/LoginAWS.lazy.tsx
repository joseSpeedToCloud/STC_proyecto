import { createLazyFileRoute } from '@tanstack/react-router'
import LoginAWS from '../components/LoginAWS'

export const Route = createLazyFileRoute('/LoginAWS')({
  component: LoginAWS
})
