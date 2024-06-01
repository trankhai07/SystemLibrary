import dayjs from 'dayjs';
import { IBookCopy } from 'app/shared/model/book-copy.model';
import { IPatronAccount } from 'app/shared/model/patron-account.model';
import { Status } from 'app/shared/model/enumerations/status.model';

export interface ICheckOut {
  id?: number;
  startTime?: string | null;
  endTime?: string | null;
  status?: Status | null;
  isReturned?: boolean | null;
  bookCopy?: IBookCopy | null;
  patronAccount?: IPatronAccount | null;
}

export const defaultValue: Readonly<ICheckOut> = {
  isReturned: false,
};
