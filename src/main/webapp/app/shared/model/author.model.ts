import { IBook } from 'app/shared/model/book.model';

export interface IAuthor {
  id?: number;
  name?: string;
  books?: IBook[] | null;
}

export const defaultValue: Readonly<IAuthor> = {};
