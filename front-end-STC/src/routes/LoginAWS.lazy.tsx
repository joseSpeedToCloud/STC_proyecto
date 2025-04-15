import { createLazyFileRoute } from '@tanstack/react-router'
import loginAWS from '../components/LoginAWS'

export const Route = createLazyFileRoute('/LoginAWS')({
  component: loginAWS
})