import { createLazyFileRoute } from '@tanstack/react-router'

import GoogleAuth from '../pages/resources_google/GoogleAuth';

export const Route = createLazyFileRoute('/googleAuth')({
  component: GoogleAuth
})