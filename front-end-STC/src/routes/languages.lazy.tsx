import { createLazyFileRoute } from '@tanstack/react-router'

export const Route = createLazyFileRoute('/languages')({
  component: () => <div>Hello /languages!</div>
})