import { IBookCopy } from 'app/shared/model/book-copy.model';
import { IWaitList } from 'app/shared/model/wait-list.model';
import { IAuthor } from 'app/shared/model/author.model';
import { ICategory } from 'app/shared/model/category.model';

export interface IBook {
  id?: number;
  title?: string;
  image?: string | null;
  description?: string | null;
  bookCopies?: IBookCopy[] | null;
  waitLists?: IWaitList[] | null;
  authors?: IAuthor[] | null;
  category?: ICategory | null;
}

export const defaultValue: Readonly<IBook> = {};
