import category from 'app/entities/category/category.reducer';
import book from 'app/entities/book/book.reducer';
import author from 'app/entities/author/author.reducer';
import waitList from 'app/entities/wait-list/wait-list.reducer';
import bookCopy from 'app/entities/book-copy/book-copy.reducer';
import publisher from 'app/entities/publisher/publisher.reducer';
import checkOut from 'app/entities/check-out/check-out.reducer';
import patronAccount from 'app/entities/patron-account/patron-account.reducer';
import notification from 'app/entities/notification/notification.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const entitiesReducers = {
  category,
  book,
  author,
  waitList,
  bookCopy,
  publisher,
  checkOut,
  patronAccount,
  notification,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
};

export default entitiesReducers;
