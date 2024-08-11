import { Input } from 'antd';
import React, { ReactNode } from 'react';
import { Controller, useFormContext } from 'react-hook-form';
import '../style.scss';

type TCommonInput = {
  name: string;
  label?: string | ReactNode;
  rules?: Object;
  type?: string;
  [key: string]: any;
};

const CommonInput = ({ name, label, type, rules, ...rest }: TCommonInput) => {
  const { control, formState } = useFormContext();

  return (
    <Controller
      control={control}
      name={name}
      rules={rules}
      render={({ field }) => (
        <div className="container-common-input">
          {label && <label className="label-common-input">{label}</label>}
          {type === 'password' ? (
            <Input.Password {...field} style={{ height: '38px' }} status={formState.errors[name] ? 'error' : ''} {...rest} />
          ) : type === 'textarea' ? (
            <Input.TextArea {...field} status={formState.errors[name] ? 'error' : ''} {...rest} />
          ) : (
            <Input {...field} status={formState.errors[name] ? 'error' : ''} style={{ height: '38px' }} type={type} {...rest} />
          )}
          {formState.errors[name] && <p className="error-message-common-input">{formState.errors[name]?.message}</p>}
        </div>
      )}
    />
  );
};

export default CommonInput;
