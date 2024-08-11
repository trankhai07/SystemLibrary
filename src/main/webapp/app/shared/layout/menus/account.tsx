import React from 'react';
import MenuItem from 'app/shared/layout/menus/menu-item';
import { translate } from 'react-jhipster';
import { NavDropdown } from './menu-components';

type TypeAccountMenu = {
  isAuthenticated: boolean;
  name: string;
};

const accountMenuItemsAuthenticated = () => (
  <>
    <MenuItem icon="wrench" to="/account/settings/asdasd" data-cy="settings">
      {translate('global.menu.account.settings')}
    </MenuItem>
    <MenuItem icon="lock" to="/account/password" data-cy="passwordItem">
      {translate('global.menu.account.password')}
    </MenuItem>
    <MenuItem icon="sign-out-alt" to="/logout" data-cy="logout">
      {translate('global.menu.account.logout')}
    </MenuItem>
  </>
);

const accountMenuItems = () => (
  <>
    <MenuItem id="login-item" icon="sign-in-alt" to="/login" data-cy="login">
      {translate('global.menu.account.login')}
    </MenuItem>
  </>
);

export const AccountMenu = (props: TypeAccountMenu) => {
  const { isAuthenticated, name } = props;

  return (
    <NavDropdown
      icon="user"
      name={name}
      id="account-menu"
      data-cy="accountMenu"
      style={{
        position: 'absolute',
        right: '0',
      }}
    >
      {isAuthenticated ? accountMenuItemsAuthenticated() : accountMenuItems()}
    </NavDropdown>
  );
};
export default AccountMenu;
