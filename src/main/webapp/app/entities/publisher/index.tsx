import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Publisher from './publisher';
import PublisherDetail from './publisher-detail';
import PublisherUpdate from './publisher-update';
import PublisherDeleteDialog from './publisher-delete-dialog';

const PublisherRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Publisher />} />
    <Route path="new" element={<PublisherUpdate />} />
    <Route path=":id">
      <Route index element={<PublisherDetail />} />
      <Route path="edit" element={<PublisherUpdate />} />
      <Route path="delete" element={<PublisherDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default PublisherRoutes;
