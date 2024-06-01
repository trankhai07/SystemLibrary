import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Category from './category';
import Book from './book';
import Author from './author';
import WaitList from './wait-list';
import BookCopy from './book-copy';
import Publisher from './publisher';
import CheckOut from './check-out';
import PatronAccount from './patron-account';
import Notification from './notification';
/* jhipster-needle-add-route-import - JHipster will add routes here */

export default () => {
  return (
    <div>
      <ErrorBoundaryRoutes>
        {/* prettier-ignore */}
        <Route path="category/*" element={<Category />} />
        <Route path="book/*" element={<Book />} />
        <Route path="author/*" element={<Author />} />
        <Route path="wait-list/*" element={<WaitList />} />
        <Route path="book-copy/*" element={<BookCopy />} />
        <Route path="publisher/*" element={<Publisher />} />
        <Route path="check-out/*" element={<CheckOut />} />
        <Route path="patron-account/*" element={<PatronAccount />} />
        <Route path="notification/*" element={<Notification />} />
        {/* jhipster-needle-add-route-path - JHipster will add routes here */}
      </ErrorBoundaryRoutes>
    </div>
  );
};
