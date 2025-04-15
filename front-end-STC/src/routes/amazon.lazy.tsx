import { createLazyFileRoute } from '@tanstack/react-router'

export const Route = createLazyFileRoute('/amazon')({
  component: () => <div>Hello /amazon!</div>
})