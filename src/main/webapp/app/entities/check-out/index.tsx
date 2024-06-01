import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import CheckOut from './check-out';
import CheckOutDetail from './check-out-detail';
import CheckOutUpdate from './check-out-update';
import CheckOutDeleteDialog from './check-out-delete-dialog';

const CheckOutRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<CheckOut />} />
    <Route path="new" element={<CheckOutUpdate />} />
    <Route path=":id">
      <Route index element={<CheckOutDetail />} />
      <Route path="edit" element={<CheckOutUpdate />} />
      <Route path="delete" element={<CheckOutDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default CheckOutRoutes;
