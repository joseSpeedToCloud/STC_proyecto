import { createLazyFileRoute } from '@tanstack/react-router';
import ProjectDetails from '../pages/resources_google/ProjectDetails';

export const Route = createLazyFileRoute('/projectsDetails')({
  component: ProjectDetails,
});


