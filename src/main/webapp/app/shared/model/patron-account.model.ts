import { IUser } from 'app/shared/model/user.model';
import { INotification } from 'app/shared/model/notification.model';
import { IWaitList } from 'app/shared/model/wait-list.model';
import { ICheckOut } from 'app/shared/model/check-out.model';

export interface IPatronAccount {
  cardNumber?: string;
  user?: IUser | null;
  notifications?: INotification[] | null;
  waitLists?: IWaitList[] | null;
  checkOuts?: ICheckOut[] | null;
}

export const defaultValue: Readonly<IPatronAccount> = {};
