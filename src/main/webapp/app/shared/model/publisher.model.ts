import { IBookCopy } from 'app/shared/model/book-copy.model';

export interface IPublisher {
  id?: number;
  name?: string;
  bookCopies?: IBookCopy[] | null;
}

export const defaultValue: Readonly<IPublisher> = {};
