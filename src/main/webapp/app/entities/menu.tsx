import React from 'react';
import { Translate } from 'react-jhipster';

import MenuItem from 'app/shared/layout/menus/menu-item';

const EntitiesMenu = () => {
  return (
    <>
      {/* prettier-ignore */}
      <MenuItem icon="asterisk" to="/category">
        <Translate contentKey="global.menu.entities.category" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/book">
        <Translate contentKey="global.menu.entities.book" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/author">
        <Translate contentKey="global.menu.entities.author" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/wait-list">
        <Translate contentKey="global.menu.entities.waitList" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/book-copy">
        <Translate contentKey="global.menu.entities.bookCopy" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/publisher">
        <Translate contentKey="global.menu.entities.publisher" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/check-out">
        <Translate contentKey="global.menu.entities.checkOut" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/patron-account">
        <Translate contentKey="global.menu.entities.patronAccount" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/notification">
        <Translate contentKey="global.menu.entities.notification" />
      </MenuItem>
      {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
    </>
  );
};

export default EntitiesMenu;
