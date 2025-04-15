import { createLazyFileRoute } from '@tanstack/react-router'
import ProjectsGoogle from '../pages/resources_google/ProjectsGoogle';

export const Route = createLazyFileRoute('/projectsgoogle')({
  component: ProjectsGoogle
})