import { Select } from 'antd';
import React, { ReactNode } from 'react';
import { Controller, useFormContext } from 'react-hook-form';
import { SelectProps } from 'antd/es/select';
import '../style.scss';

type TCommonSelect = {
  name: string;
  label?: string | ReactNode;
  rules?: Object;
  options: SelectProps['options'];
  mode?: 'multiple' | null;
  [key: string]: any;
};

const CommonSelect = ({ name, mode, label, rules, options, ...rest }: TCommonSelect) => {
  const { control, formState } = useFormContext();

  return (
    <Controller
      control={control}
      name={name}
      rules={rules}
      render={({ field }) => (
        <div className="container-common-input">
          {label && <label className="label-common-input">{label}</label>}
          <Select
            style={{ width: '100%', height: '38px' }}
            {...field}
            mode={mode}
            options={options}
            status={formState.errors[name] && 'error'}
            {...rest}
          />
          {formState.errors[name] && <p className="error-message-common-input">{formState.errors[name]?.message}</p>}
        </div>
      )}
    />
  );
};

export default CommonSelect;
