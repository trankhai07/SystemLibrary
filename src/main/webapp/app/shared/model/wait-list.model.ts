import dayjs from 'dayjs';
import { IPatronAccount } from 'app/shared/model/patron-account.model';
import { IBook } from 'app/shared/model/book.model';

export interface IWaitList {
  id?: number;
  creatAt?: string | null;
  patronAccount?: IPatronAccount | null;
  book?: IBook | null;
}

export const defaultValue: Readonly<IWaitList> = {};
