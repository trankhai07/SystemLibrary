import { HomeOutlined } from '@ant-design/icons';
import React, { useEffect, useState } from 'react';
import { Breadcrumb } from 'antd';
import { NavLink, useLocation } from 'react-router-dom';
import './style.scss';
import { Storage, Translate, translate } from 'react-jhipster';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { AUTHORITIES } from 'app/config/constants';

const BreadcrumbItem = () => {
  const location = useLocation();
  const currentPath = location.pathname;

  const [pathMovie, setPathMovie] = useState('');
  const [pathHotel, setPathHotel] = useState('');
  const channelPackMasterEntity = useAppSelector(state => state.channelPackMaster.entity);

  const role = Storage.session.get('role');

  const isValidObjectId = id => {
    const objectIdRegex = /^[0-9a-fA-F]{24}$/;
    return objectIdRegex.test(id);
  };

  const capitalizeFirstLetter = str => {
    if (str === 'movie-genres') {
      return 'Movies';
    }
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  };

  if (location.pathname === '/series-movie') {
    localStorage.setItem('pathListMovie', location.pathname + location.search);
  }

  if (location.pathname.includes('/service-hotel/hotel/')) {
    localStorage.setItem('pathHotel', location.pathname + location.search);
  }

  const splitStringLv2 = (isHref?: boolean) => {
    const inputString = currentPath.substring(1);
    const firstIndex = inputString.indexOf('/');
    const length = capitalizeFirstLetter(inputString.substring(0, firstIndex)).length;
    const lv2 = String(
      length > 0 ? capitalizeFirstLetter(inputString.substring(0, firstIndex)) : capitalizeFirstLetter(currentPath.substring(1))
    );
    if (isHref) {
      return lv2;
    }
    return lv2.replace(/-/g, ' ');
  };

  useEffect(() => {
    const value = localStorage.getItem('pathListMovie');
    if (value) {
      setPathMovie(value ?? '');
    }
  }, [localStorage.getItem('pathListMovie')]);

  useEffect(() => {
    const value = localStorage.getItem('pathHotel');
    if (value) {
      setPathHotel(value ?? '');
    }
  }, [localStorage.getItem('pathHotel')]);

  const countOccurrences = (mainString, character) => {
    let count = 0;
    for (let i = 0; i < mainString.length; i++) {
      if (mainString[i] === character) {
        count++;
      }
    }
    return count;
  };

  const splitStringLv3 = () => {
    const inputString = currentPath.substring(1);
    const lastSlashIndex = inputString.lastIndexOf('/');
    const secondPart = inputString.substring(lastSlashIndex + 1);
    if (isValidObjectId(secondPart)) {
      if (currentPath.includes('/service-pack/hotel/') && countOccurrences(currentPath, '/') === 4) {
        return channelPackMasterEntity.name;
      }
      return 'Detail';
    } else if (secondPart.toLowerCase() === currentPath.substring(1)) {
      return 'List';
    } else {
      return capitalizeFirstLetter(secondPart.replace(/-/g, ' '));
    }
  };

  const checkIsSpecialCase = () => {
    return location.pathname.includes('series-movie');
  };

  const checkServiceHotel = () => {
    return location.pathname.includes('service-hotel');
  };

  const listItem = checkIsSpecialCase()
    ? [
        {
          href: '/',
          title: (
            <>
              <HomeOutlined />
              <span>Home</span>
            </>
          ),
        },
        {
          href: 'movie-genres',
          title: (
            <>
              <span style={{ fontFamily: 'Montserrat', textDecoration: 'underline', color: '#00000073' }}>
                {<Translate contentKey="hotelTvApp.menuService.home.titleMovies">Movies</Translate>}
              </span>
            </>
          ),
        },
        {
          href: pathMovie,
          title: (
            <>
              <span style={{ fontFamily: 'Montserrat', color: '#00000073' }}>
                <Translate contentKey="hotelTvApp.seriesMovie.home.title">Series Movies</Translate>
              </span>
            </>
          ),
        },
        {
          title: checkIsSpecialCase() ? (
            <>{<span style={{ fontFamily: 'Montserrat', color: '#00000073' }}>{splitStringLv3()}</span>}</>
          ) : null,
        },
      ]
    : checkServiceHotel()
    ? [
        {
          href: '/',
          title: (
            <>
              <HomeOutlined />
              <span>Home</span>
            </>
          ),
        },
        {
          href: 'menu-service',
          title: (
            <>
              <span style={{ fontFamily: 'Montserrat', textDecoration: 'underline', color: '#00000073' }}>
                {<Translate contentKey="hotelTvApp.menuService.home.title">Menu Services</Translate>}
              </span>
            </>
          ),
        },
        {
          href: role?.includes(AUTHORITIES.ADMIN) ? pathHotel : `${window.location}`,
          title: (
            <>
              <span style={{ fontFamily: 'Montserrat', color: '#00000073' }}>
                <Translate contentKey="hotelTvApp.serviceHotel.home.title">Service Hotels</Translate>
              </span>
            </>
          ),
        },
        {
          title: (
            <>
              <span style={{ fontFamily: 'Montserrat', color: '#00000073' }}>{splitStringLv3()}</span>
            </>
          ),
        },
      ]
    : [
        {
          href: '/',
          title: (
            <>
              <HomeOutlined />
              <span>Home</span>
            </>
          ),
        },
        {
          href: splitStringLv2(true).toLowerCase(),
          title: (
            <>
              <span style={{ fontFamily: 'Montserrat', textDecoration: 'underline', color: '#00000073' }}>{splitStringLv2()}</span>
            </>
          ),
        },
        {
          title: (
            <>
              <span style={{ fontFamily: 'Montserrat', color: '#00000073' }}>{splitStringLv3()}</span>
            </>
          ),
        },
      ];

  const itemsRender = (route, params, listItem, paths) => {
    const last = listItem.indexOf(route) === listItem.length - 1;
    return !last ? <NavLink to={route.href}>{route.title}</NavLink> : route.title;
  };

  return (
    <>
      {currentPath !== '/' && currentPath !== '/logout' && currentPath !== '/login' && currentPath !== '/account/register' && (
        <div>
          <Breadcrumb itemRender={itemsRender} items={listItem} />
        </div>
      )}
    </>
  );
};

export default BreadcrumbItem;
