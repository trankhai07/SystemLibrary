import dayjs from 'dayjs';
import { IPatronAccount } from 'app/shared/model/patron-account.model';
import { NoTi } from 'app/shared/model/enumerations/no-ti.model';

export interface INotification {
  id?: number;
  sentAt?: string | null;
  type?: NoTi | null;
  patronAccount?: IPatronAccount | null;
}

export const defaultValue: Readonly<INotification> = {};
