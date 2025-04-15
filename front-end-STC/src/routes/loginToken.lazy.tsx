import { createLazyFileRoute } from '@tanstack/react-router'
import loginToken from '../pages/LoginToken'

export const Route = createLazyFileRoute('/loginToken')({
  component: loginToken
})