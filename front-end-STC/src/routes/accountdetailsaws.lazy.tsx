import { createLazyFileRoute } from '@tanstack/react-router';
import AccountDetailsAWS from '../pages/resources_aws/AccountDetailsAWS';

export const Route = createLazyFileRoute('/accountdetailsaws')({
  component: AccountDetailsAWS
});


