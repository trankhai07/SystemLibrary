import 'react-toastify/dist/ReactToastify.css';
import './app.scss';
import React, { useEffect } from 'react';
import { Card } from 'reactstrap';
import { BrowserRouter } from 'react-router-dom';
import { ToastContainer, toast } from 'react-toastify';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { ROLE_KEY, getAccount, getSession } from 'app/shared/reducers/authentication';
import { getProfile } from 'app/shared/reducers/application-profile';
import { setLocale } from 'app/shared/reducers/locale';
import Header from 'app/shared/layout/header/header';
import Footer from 'app/shared/layout/footer/footer';
import { hasAnyAuthority } from 'app/shared/auth/private-route';
import ErrorBoundary from 'app/shared/error/error-boundary';
import { AUTHORITIES } from 'app/config/constants';
import AppRoutes from 'app/routes';
import { Storage } from 'react-jhipster';
import { translate } from 'react-jhipster';
import { Sidebar } from './shared/layout/sidebar/sidebar';

const baseHref = document.querySelector('base').getAttribute('href').replace(/\/$/, '');

export const App = () => {
  const dispatch = useAppDispatch();

  const currentLocale = useAppSelector(state => state.locale.currentLocale);
  const isAuthenticated = useAppSelector(state => state.authentication.isAuthenticated);
  const isAdmin = useAppSelector(state => hasAnyAuthority(state.authentication.account.authorities, [AUTHORITIES.ADMIN]));
  const ribbonEnv = useAppSelector(state => state.applicationProfile.ribbonEnv);
  const isInProduction = useAppSelector(state => state.applicationProfile.inProduction);
  const isOpenAPIEnabled = useAppSelector(state => state.applicationProfile.isOpenAPIEnabled);
  const account = useAppSelector(state => state.authentication.account);

  useEffect(() => {
    dispatch(getSession());
    dispatch(getProfile());
  }, []);

  useEffect(() => {
    isAuthenticated ? dispatch(getAccount()) : null;
  }, [isAuthenticated]);

  const paddingTop = '60px';
  return (
    <BrowserRouter basename={baseHref}>
      <div className="app-container" style={{ paddingTop }}>
        <ToastContainer position={toast.POSITION.TOP_LEFT} className="toastify-container" toastClassName="toastify-toast" />
        <ErrorBoundary>
          <Header
            isAuthenticated={isAuthenticated}
            isAdmin={isAdmin}
            currentLocale={currentLocale}
            ribbonEnv={ribbonEnv}
            isInProduction={isInProduction}
            isOpenAPIEnabled={isOpenAPIEnabled}
            name={isAuthenticated ? account.login : translate('global.header.nameAccount')}
          />
        </ErrorBoundary>
        <div className="container-fluid view-container" id="app-view-container">
          <div className="body-container">
            <Sidebar className="sidebar" />
            <div>
              <div style={{ paddingLeft: '285px', paddingBottom: '15px' }}>{/* <BreadcrumbItem /> */}</div>
              <Card className="jh-card">
                <ErrorBoundary>
                  <AppRoutes />
                </ErrorBoundary>
              </Card>
            </div>
          </div>
          {/* <Footer /> */}
        </div>
      </div>
    </BrowserRouter>
  );
};

export default App;
