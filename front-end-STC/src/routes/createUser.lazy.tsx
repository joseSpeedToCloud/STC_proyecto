import { createLazyFileRoute } from '@tanstack/react-router'
import CreateUser from '../pages/CreateUser'

export const Route = createLazyFileRoute('/createUser')({
  component: CreateUser
})