import { createLazyFileRoute } from '@tanstack/react-router'
import User from '../pages/Users'

export const Route = createLazyFileRoute('/users')({
  component: User
})