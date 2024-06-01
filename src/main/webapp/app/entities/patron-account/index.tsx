import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import PatronAccount from './patron-account';
import PatronAccountDetail from './patron-account-detail';
import PatronAccountUpdate from './patron-account-update';
import PatronAccountDeleteDialog from './patron-account-delete-dialog';

const PatronAccountRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<PatronAccount />} />
    <Route path="new" element={<PatronAccountUpdate />} />
    <Route path=":id">
      <Route index element={<PatronAccountDetail />} />
      <Route path="edit" element={<PatronAccountUpdate />} />
      <Route path="delete" element={<PatronAccountDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default PatronAccountRoutes;
