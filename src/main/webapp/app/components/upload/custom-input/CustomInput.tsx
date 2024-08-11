import React, { forwardRef, useImperativeHandle } from 'react';
import { Input, Button } from 'antd';

interface CustomInputProps {
  value: string;
  onChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
  onFocus: () => void;
  // onBlur: () => void;
  style: React.CSSProperties;
  id: string;
  name: string;
  placeholder: string;
  type: string;
}

export interface CustomInputRef {
  addName: () => void;
}

export const CustomInput = forwardRef<any, CustomInputProps>(({ value, onChange, onFocus, style, id, name, placeholder, type }, ref) => {
  useImperativeHandle(ref, () => ({
    addName: () => {
      onChange({ target: { value: value.trim() + ' [name]' } } as React.ChangeEvent<HTMLInputElement>);
    },
  }));

  return (
    <Input
      type={type}
      id={id}
      name={name}
      value={value}
      placeholder={placeholder}
      onChange={onChange}
      onFocus={() => {
        onFocus();
      }}
      style={style}
    />
  );
});
