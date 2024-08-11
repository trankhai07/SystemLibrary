import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import {
  Button,
  Input,
  InputGroup,
  FormGroup,
  Form,
  Row,
  Col,
  Table,
  Badge,
  Dropdown,
  DropdownToggle,
  DropdownMenu,
  DropdownItem,
} from 'reactstrap';
import { Translate, translate, getSortState, JhiPagination, JhiItemCount } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IPatronAccount } from 'app/shared/model/patron-account.model';
import { searchEntities, getEntities, getEntitiesNotEnough, getEntity, updateEntityUserStatus } from './patron-account.reducer';
import InputSearch from 'app/components/input-search';
import { Radio, RadioChangeEvent } from 'antd';
import { Status } from 'app/shared/model/enumerations/status.model';
import { faCaretDown } from '@fortawesome/free-solid-svg-icons';
import './style.scss';

export const PatronAccount = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();

  const [search, setSearch] = useState('');
  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getSortState(location, ITEMS_PER_PAGE, 'cardNumber'), location.search)
  );

  const patronAccountList = useAppSelector(state => state.patronAccount.entities);
  const patronAccountEntity = useAppSelector(state => state.patronAccount.entity);
  const [selectedCardNumber, setSelectedCardNumber] = useState(null);
  const [statusUser, setStatusUser] = useState(false);
  const updateSuccess = useAppSelector(state => state.patronAccount.updateSuccess);

  const loading = useAppSelector(state => state.patronAccount.loading);
  const totalItems = useAppSelector(state => state.patronAccount.totalItems);
  const [dropdownOpen, setDropdownOpen] = useState({});
  const [notCondition, setCondition] = useState('');

  useEffect(() => {
    if (updateSuccess) {
      getAllEntities();
    }
  }, [updateSuccess]);

  const getAllEntities = () => {
    if (search) {
      setCondition('');
      dispatch(
        searchEntities({
          query: search,
          page: paginationState.activePage - 1,
          size: paginationState.itemsPerPage,
          sort: `${paginationState.sort},${paginationState.order}`,
        })
      );
    } else if (!search && !notCondition) {
      dispatch(
        getEntities({
          page: paginationState.activePage - 1,
          size: paginationState.itemsPerPage,
          sort: `${paginationState.sort},${paginationState.order}`,
        })
      );
    } else if (!search && notCondition) {
      dispatch(
        getEntitiesNotEnough({
          page: paginationState.activePage - 1,
          size: paginationState.itemsPerPage,
        })
      );
    }
  };

  const startSearching = e => {
    if (search) {
      setPaginationState({
        ...paginationState,
        activePage: 1,
      });
      dispatch(
        searchEntities({
          query: search,
          page: paginationState.activePage - 1,
          size: paginationState.itemsPerPage,
          sort: `${paginationState.sort},${paginationState.order}`,
        })
      );
    }
    e.preventDefault();
  };

  const clear = () => {
    setSearch('');
    setPaginationState({
      ...paginationState,
      activePage: 1,
    });
    dispatch(getEntities({}));
  };

  const handleSearch = event => setSearch(event.target.value);

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (location.search !== endURL) {
      navigate(`${location.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [paginationState.activePage, paginationState.order, paginationState.sort, search, notCondition]);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const page = params.get('page');
    const sort = params.get(SORT);
    if (page && sort) {
      const sortSplit = sort.split(',');
      setPaginationState({
        ...paginationState,
        activePage: +page,
        sort: sortSplit[0],
        order: sortSplit[1],
      });
    }
  }, [location.search]);

  const sort = p => () => {
    setPaginationState({
      ...paginationState,
      order: paginationState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handlePagination = currentPage =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });

  const handleSyncList = () => {
    sortEntities();
  };
  const handleClick = value => {
    if (notCondition === value) setCondition('');
    else setCondition(value);
  };

  const toggleDropdown = index => {
    setDropdownOpen(prevState => ({
      ...prevState,
      [index]: !prevState[index],
    }));
  };
  useEffect(() => {
    if (selectedCardNumber) {
      dispatch(updateEntityUserStatus({ entity: patronAccountEntity, statusUser: statusUser, cardNumber: selectedCardNumber }));
    }
  }, [selectedCardNumber, statusUser]);

  const handleSelect = (cardNumber: string, status: boolean) => {
    if (cardNumber !== selectedCardNumber) setSelectedCardNumber(cardNumber);
    if (status !== statusUser) setStatusUser(status);
  };
  return (
    <div>
      <div id="menu-service-heading" data-cy="MenuServiceHeading">
        <h2 id="patron-account-heading" data-cy="PatronAccountHeading">
          <div className="d-flex gap-3 align-items-center">
            <Translate contentKey="systemLibraryApp.patronAccount.home.title">Patron Accounts</Translate>
          </div>
        </h2>
        <div className="d-flex justify-content-between align-items-center mt-3">
          <div className="w-25">
            <InputSearch name={'search'} onChange={handleSearch} defaultValue={search} />
            <Button type="reset" className="input-group-addon input-clear" onClick={clear}>
              <FontAwesomeIcon icon="trash" />
            </Button>
          </div>
          <div>
            <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
              <FontAwesomeIcon icon="sync" spin={loading} />{' '}
              <Translate contentKey="systemLibraryApp.patronAccount.home.refreshListLabel">Refresh List</Translate>
            </Button>
          </div>
        </div>
        <div className="d-flex justify-content-center">
          <div className="d-flex align-items-center gap-3">
            <p className="mb-0">Choose</p>
            <Radio.Group value={notCondition} className="my-3">
              <Radio.Button value={Status.Confirmed} onClick={() => handleClick(Status.Confirmed)}>
                <Translate contentKey={`systemLibraryApp.patronAccount.notEnough`}>Not Enough Condition</Translate>
              </Radio.Button>
            </Radio.Group>
          </div>
        </div>
      </div>
      <div className="table-responsive">
        {patronAccountList && patronAccountList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('cardNumber')}>
                  <Translate contentKey="systemLibraryApp.patronAccount.cardNumber">Card Number</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('user.login')}>
                  <Translate contentKey="systemLibraryApp.patronAccount.userName">User Name</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('user.email')}>
                  <Translate contentKey="systemLibraryApp.patronAccount.email">Email</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  <Translate contentKey="systemLibraryApp.patronAccount.status">Status</Translate>
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {patronAccountList.map((patronAccount, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>{patronAccount.cardNumber}</td>
                  <td>{patronAccount.user ? patronAccount.user.login : ''}</td>
                  <td>{patronAccount.user ? patronAccount.user.email : ''}</td>
                  <td>
                    <Dropdown isOpen={dropdownOpen[i] || false} toggle={() => toggleDropdown(i)} className="activation-dropdown">
                      <Button className="activation-toggle" onClick={() => toggleDropdown(i)}>
                        {patronAccount.user?.activated ? (
                          <Badge color="success">{translate('global.form.active')}</Badge>
                        ) : (
                          <Badge color="danger">{translate('global.form.deactive')}</Badge>
                        )}
                        <FontAwesomeIcon icon={faCaretDown} className="ml-2" />
                      </Button>
                      <DropdownMenu>
                        <DropdownItem onClick={() => handleSelect(patronAccount.cardNumber, true)}>
                          <Badge color="success">{translate('global.form.active')}</Badge>
                        </DropdownItem>
                        <DropdownItem onClick={() => handleSelect(patronAccount.cardNumber, false)}>
                          <Badge color="danger">{translate('global.form.deactive')}</Badge>
                        </DropdownItem>
                      </DropdownMenu>
                    </Dropdown>
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        tag={Link}
                        to={`/patron-account/${patronAccount.cardNumber}`}
                        color="info"
                        size="sm"
                        data-cy="entityDetailsButton"
                      >
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/patron-account/${patronAccount.cardNumber}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                        color="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.delete">Delete</Translate>
                        </span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && (
            <div className="alert alert-warning">
              <Translate contentKey="systemLibraryApp.patronAccount.home.notFound">No Patron Accounts found</Translate>
            </div>
          )
        )}
      </div>
      {totalItems ? (
        <div className={patronAccountList && patronAccountList.length > 0 ? '' : 'd-none'}>
          <div className="justify-content-center d-flex">
            <JhiItemCount page={paginationState.activePage} total={totalItems} itemsPerPage={paginationState.itemsPerPage} i18nEnabled />
          </div>
          <div className="justify-content-center d-flex">
            <JhiPagination
              activePage={paginationState.activePage}
              onSelect={handlePagination}
              maxButtons={5}
              itemsPerPage={paginationState.itemsPerPage}
              totalItems={totalItems}
            />
          </div>
        </div>
      ) : (
        ''
      )}
    </div>
  );
};

export default PatronAccount;
