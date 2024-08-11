import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import React from 'react';
import { ValidatedInput, translate } from 'react-jhipster';
import './style.scss';

type TypeInputSearch = {
  name: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  defaultValue: string;
};

const InputSearch = (props: TypeInputSearch) => {
  const { name, onChange, defaultValue } = props;
  return (
    <div className="d-flex align-items-center position-relative w-100">
      <ValidatedInput
        className="input-search"
        name={name}
        placeholder={`${translate('global.form.search')}...`}
        onChange={e => onChange(e)}
        value={defaultValue}
      />
      <FontAwesomeIcon className="icon-input-search position-absolute" icon={'search'} />
    </div>
  );
};

export default InputSearch;
