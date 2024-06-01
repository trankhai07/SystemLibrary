import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Author from './author';
import AuthorDetail from './author-detail';
import AuthorUpdate from './author-update';
import AuthorDeleteDialog from './author-delete-dialog';

const AuthorRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Author />} />
    <Route path="new" element={<AuthorUpdate />} />
    <Route path=":id">
      <Route index element={<AuthorDetail />} />
      <Route path="edit" element={<AuthorUpdate />} />
      <Route path="delete" element={<AuthorDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default AuthorRoutes;
