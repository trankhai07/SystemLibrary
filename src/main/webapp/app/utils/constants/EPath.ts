import BookCopy from 'app/entities/book-copy/book-copy';
import Book from 'app/entities/book/book';
import Category from 'app/entities/category/category';
import CheckOut from 'app/entities/check-out/check-out';
import PatronAccount from 'app/entities/patron-account/patron-account';
import Publisher from 'app/entities/publisher/publisher';

export const EPath = {
  Author: '/author',
  Publisher: '/publisher',
  Category: '/category',
  PatronAccount: '/patron-account',
  Book: '/book',
  BookCopy: '/book-copy',
  CheckOut: '/check-out',
  CheckOutClientBorow: '/check-out-client/borrow',
  CheckOutClientReturn: '/check-out-client/return',
  UserManagement: '/admin/user-management',
  Tracker: '/admin/tracker',
  Metrics: '/admin/metrics',
  Health: '/admin/health',
  Configuration: '/admin/configuration',
  Logs: '/admin/logs',
  Docs: '/admin/docs',
};
