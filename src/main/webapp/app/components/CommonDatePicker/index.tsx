import { DatePicker, DatePickerProps } from 'antd';
import React, { ReactNode } from 'react';
import { Controller, useFormContext } from 'react-hook-form';
import '../style.scss';
import dayjs, { Dayjs } from 'dayjs';

type TCommonDatePicker = {
  name: string;
  label?: string | ReactNode;
  rules?: Object;
  minDate?: Dayjs;
  maxDate?: Dayjs;
  setEndTime?: React.Dispatch<React.SetStateAction<any>>;
  [key: string]: any;
} & DatePickerProps;

const CommonDatePicker = ({ name, label, rules, minDate, maxDate, setEndTime, ...rest }: TCommonDatePicker) => {
  const { control, formState } = useFormContext();
  const onChangeTime: DatePickerProps['onChange'] = (date, dateString) => {
    setEndTime(date?.toISOString() ?? '');
  };

  return (
    <Controller
      control={control}
      name={name}
      rules={rules}
      render={({ field }) => (
        <div className="container-common-input">
          {label && <label className="label-common-input">{label}</label>}
          <DatePicker
            {...field}
            style={{ width: '100%', height: '38px' }}
            status={formState.errors[name] && 'error'}
            format={'DD/MM/YYYY'}
            onChange={onChangeTime}
            disabledDate={date => !date || (maxDate && date.isAfter(maxDate)) || (minDate && date.isBefore(minDate))}
            {...rest}
          />
          {formState.errors[name] && <p className="error-message-common-input">{formState.errors[name]?.message}</p>}
        </div>
      )}
    />
  );
};

export default CommonDatePicker;
