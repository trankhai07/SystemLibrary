import { ICheckOut } from 'app/shared/model/check-out.model';
import { IBook } from 'app/shared/model/book.model';
import { IPublisher } from 'app/shared/model/publisher.model';

export interface IBookCopy {
  id?: number;
  yearPublished?: number | null;
  amount?: number | null;
  image?: string | null;
  description?: string | null;
  checkOuts?: ICheckOut[] | null;
  book?: IBook | null;
  publisher?: IPublisher | null;
}

export const defaultValue: Readonly<IBookCopy> = {};
