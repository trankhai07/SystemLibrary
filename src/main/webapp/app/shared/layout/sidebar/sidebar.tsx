import React, { useEffect, useState } from 'react';
import { TeamOutlined, NotificationOutlined, UnorderedListOutlined, HomeOutlined } from '@ant-design/icons';
import type { MenuProps } from 'antd';
import { Breadcrumb, Layout, Menu, theme } from 'antd';
import { useLocation, useNavigate, Route, Routes, Link } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import './sidebar.scss';
import { Storage, Translate } from 'react-jhipster';
import MenuItem from 'app/shared/layout/menus/menu-item';
import { EPath } from 'app/utils/constants/EPath';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faUserPen,
  faMarker,
  faPersonChalkboard,
  faBook,
  faCreditCard,
  faServer,
  faHouse,
  faRotate,
  faCheckToSlot,
} from '@fortawesome/free-solid-svg-icons';
import { DropdownItem } from 'reactstrap';
import { AUTHORITIES } from 'app/config/constants';
import { isArray } from 'lodash';
import { getAccount } from 'app/shared/reducers/authentication';
import { Status } from 'app/shared/model/enumerations/status.model';

const { Sider } = Layout;

export const Sidebar = ({ className }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const currentPath = location.pathname;
  const [itemActive, setItemActive] = useState([]);
  const [selectedPath, setSelectedPath] = useState(currentPath);
  const account = useAppSelector(state => state.authentication.account);

  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getAccount());
  }, []);

  useEffect(() => {
    const element = document?.querySelector('.jh-card') as HTMLElement;
    if (account.id && element) {
      element.style.marginLeft = '280px';
    } else {
      element.style.marginLeft = '0px';
    }
  }, [account.id]);

  useEffect(() => {
    setSelectedPath(currentPath);
  }, [currentPath]);

  const items2 = [
    {
      key: '/',
      label: (
        <Link style={{ textDecoration: 'none' }} to="/">
          <DropdownItem tag={Link} to="/" data-cy="data-cy-home">
            <FontAwesomeIcon icon={faHouse} /> <Translate contentKey="global.menu.home" />
          </DropdownItem>
        </Link>
      ),
      role: [AUTHORITIES.ADMIN, AUTHORITIES.USER],
    },
    {
      key: EPath.PatronAccount,
      label: (
        <Link style={{ textDecoration: 'none' }} to={EPath.PatronAccount}>
          <MenuItem icon="users" to={EPath.PatronAccount}>
            <Translate contentKey="global.menu.entities.patronAccount" />
          </MenuItem>
        </Link>
      ),
      role: [AUTHORITIES.ADMIN],
    },
    {
      key: EPath.CheckOut,
      label: (
        <Link style={{ textDecoration: 'none' }} to={EPath.CheckOut}>
          <DropdownItem tag={Link} to={EPath.CheckOut} data-cy="data-cy-checkOut">
            <FontAwesomeIcon icon={faCreditCard} /> <Translate contentKey="global.menu.entities.checkOut" />
          </DropdownItem>
        </Link>
      ),
      role: [AUTHORITIES.ADMIN],
    },
    {
      key: 'aboutPeople',
      icon: <FontAwesomeIcon icon={faMarker} />,
      label: <Translate contentKey="global.menu.entities.aboutPeople" />,
      role: [AUTHORITIES.ADMIN],
      children: [
        {
          key: EPath.Author,
          label: (
            <Link style={{ textDecoration: 'none' }} to={EPath.Author}>
              <DropdownItem tag={Link} to={EPath.Author} data-cy="data-cy-author">
                <FontAwesomeIcon icon={faUserPen} /> <Translate contentKey="global.menu.entities.author" />
              </DropdownItem>
            </Link>
          ),
          role: [AUTHORITIES.ADMIN],
        },
        {
          key: EPath.Publisher,
          label: (
            <Link style={{ textDecoration: 'none' }} to={EPath.Publisher}>
              <DropdownItem tag={Link} to={EPath.Publisher} data-cy="data-cy-publisher">
                <FontAwesomeIcon icon={faPersonChalkboard} /> <Translate contentKey="global.menu.entities.publisher" />
              </DropdownItem>
            </Link>
          ),
          role: [AUTHORITIES.ADMIN],
        },
      ],
    },
    {
      key: EPath.Category,
      label: (
        <Link style={{ textDecoration: 'none' }} to={EPath.Category}>
          <DropdownItem tag={Link} to={EPath.Category} data-cy="data-cy-category">
            <FontAwesomeIcon icon={faServer} /> <Translate contentKey="global.menu.entities.category" />
          </DropdownItem>
        </Link>
      ),
      role: [AUTHORITIES.ADMIN],
    },
    {
      key: EPath.Book,
      label: (
        <Link style={{ textDecoration: 'none' }} to={EPath.Book}>
          <DropdownItem tag={Link} to={EPath.Book} data-cy="data-cy-book">
            <FontAwesomeIcon icon={faBook} /> <Translate contentKey="global.menu.entities.book" />
          </DropdownItem>
        </Link>
      ),
      role: [AUTHORITIES.ADMIN],
    },
    {
      key: 'checkOutClient',
      icon: <FontAwesomeIcon icon={faCreditCard} />,
      label: <Translate contentKey="global.menu.entities.checkOut" />,
      role: [AUTHORITIES.USER],
      children: [
        {
          key: EPath.CheckOutClientBorow,
          label: (
            <Link style={{ textDecoration: 'none' }} to={EPath.CheckOutClientBorow + `?returned=false&status=${Status.Pending}`}>
              <DropdownItem
                tag={Link}
                to={EPath.CheckOutClientBorow + `?returned=false&status=${Status.Pending}`}
                data-cy="data-cy-client-borrow"
              >
                <FontAwesomeIcon icon={faCheckToSlot} /> <Translate contentKey="systemLibraryApp.checkOut.clientBorrow" />
              </DropdownItem>
            </Link>
          ),
          role: [AUTHORITIES.USER],
        },
        {
          key: EPath.CheckOutClientReturn,
          label: (
            <Link style={{ textDecoration: 'none' }} to={EPath.CheckOutClientReturn + `?returned=true&status=${Status.Confirmed}`}>
              <DropdownItem
                tag={Link}
                to={EPath.CheckOutClientReturn + `?returned=true&status=${Status.Confirmed}`}
                data-cy="data-cy-client-return"
              >
                <FontAwesomeIcon icon={faRotate} /> <Translate contentKey="systemLibraryApp.checkOut.clientReturn" />
              </DropdownItem>
            </Link>
          ),
          role: [AUTHORITIES.USER],
        },
      ],
    },
  ];
  useEffect(() => {
    console.log('RUN SIDEBAR: ' + account.authorities);
    if (account.authorities && isArray(account.authorities)) {
      const items = items2.filter(item => account.authorities?.some(i => item.role.includes(i)));
      setItemActive(items);
    }
    // else {
    //   Storage.session.remove(AUTH_TOKEN_KEY)
    // }
  }, [account.id]);

  return (
    <div className={`${className}`}>
      {account.id && (
        <Sider className="newsidebar" width={250}>
          <Menu
            className="scroll-menu"
            mode="inline"
            selectedKeys={[selectedPath]}
            defaultOpenKeys={['path']}
            style={{ height: '100%', borderRight: 0 }}
            items={itemActive}
          />
        </Sider>
      )}
    </div>
  );
};
