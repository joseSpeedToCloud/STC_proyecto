import { createLazyFileRoute } from '@tanstack/react-router'

import HomeGoogle from '../pages/resources_google/HomeGoogle';

export const Route = createLazyFileRoute('/homegcp')({
  component: HomeGoogle
})