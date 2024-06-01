import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import WaitList from './wait-list';
import WaitListDetail from './wait-list-detail';
import WaitListUpdate from './wait-list-update';
import WaitListDeleteDialog from './wait-list-delete-dialog';

const WaitListRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<WaitList />} />
    <Route path="new" element={<WaitListUpdate />} />
    <Route path=":id">
      <Route index element={<WaitListDetail />} />
      <Route path="edit" element={<WaitListUpdate />} />
      <Route path="delete" element={<WaitListDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default WaitListRoutes;
