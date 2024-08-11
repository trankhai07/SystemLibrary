import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Input, InputGroup, FormGroup, Form, Row, Col, Table, Dropdown, Badge, DropdownMenu, DropdownItem } from 'reactstrap';
import { Translate, translate, TextFormat, getSortState, JhiPagination, JhiItemCount, Storage } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT, AUTHORITIES } from 'app/config/constants';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { searchEntities, getEntities, getEntity, updateEntity, getEntitiesClient } from './check-out.reducer';
import { Status } from 'app/shared/model/enumerations/status.model';
import { Radio, RadioChangeEvent } from 'antd';
import { faCaretDown } from '@fortawesome/free-solid-svg-icons';
import './style.scss';

export const CheckOutClient = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();
  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getSortState(location, ITEMS_PER_PAGE, 'id'), location.search)
  );

  const checkOutList = useAppSelector(state => state.checkOut.entities);
  const loading = useAppSelector(state => state.checkOut.loading);
  const totalItems = useAppSelector(state => state.checkOut.totalItems);
  const params = new URLSearchParams(location.search);

  const statusParams = params.get('status');
  const returnedParams = params.get('returned');
  const cardNumber = Storage.session.get('cardNumber');

  const [statusCheckOut, setStatusCheckOut] = useState(statusParams ?? Status.Pending);

  const [selectedId, setSelectedId] = useState(null);
  const [selectedStatus, setSelectedStatus] = useState(statusParams ?? Status.Pending);
  const updateSuccess = useAppSelector(state => state.checkOut.updateSuccess);
  const [dropdownOpen, setDropdownOpen] = useState({});
  const [dropdownOpenReturn, setDropdownOpenReturn] = useState({});
  const [selectedReturn, setSelectedReturn] = useState(false);
  const [checkReturn, setCheckReturn] = useState(false);

  useEffect(() => {
    if (updateSuccess) {
      sortEntities();
    }
  }, [updateSuccess]);
  const getAllEntities = () => {
    console.log('STATUS: ' + returnedParams);
    dispatch(
      getEntitiesClient({
        cardNumber: cardNumber,
        status: returnedParams === 'true' ? Status.Confirmed : statusCheckOut,
        returned: returnedParams,
      })
    );
  };
  const sortEntities = () => {
    getAllEntities();
    const endURL = `?status=${statusCheckOut}&cardNumber=${cardNumber}&returned=${returnedParams}`;
    if (location.search !== endURL) {
      navigate(`${location.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [statusCheckOut, returnedParams]);

  const handleSyncList = () => {
    sortEntities();
  };

  const onChange = (e: RadioChangeEvent) => {
    setStatusCheckOut(e.target.value);
  };

  const handleUpdateEntity = async () => {
    const checkOutEntity: any = await dispatch(getEntity(selectedId));
    const entity = !checkReturn
      ? {
          ...checkOutEntity.payload.data,
          status: selectedStatus,
        }
      : {
          ...checkOutEntity.payload.data,
          isReturned: selectedReturn,
        };
    dispatch(updateEntity(entity));
  };

  useEffect(() => {
    if (selectedId) {
      handleUpdateEntity();
    }
  }, [selectedId, selectedStatus, selectedReturn]);

  const handleSelectStatus = (idCheckOut: number, statusSelected: Status) => {
    if (idCheckOut !== selectedId) setSelectedId(idCheckOut);
    if (selectedStatus !== statusSelected) setSelectedStatus(statusSelected);
    setCheckReturn(false);
  };

  const handleSelectReturn = (idCheckOut: number, isReturn: boolean) => {
    if (idCheckOut !== selectedId) setSelectedId(idCheckOut);
    if (isReturn !== selectedReturn) setSelectedReturn(isReturn);
    setCheckReturn(true);
  };

  const toggleDropdown = index => {
    setDropdownOpen(prevState => ({
      ...prevState,
      [index]: !prevState[index],
    }));
  };

  const toggleDropdownReturn = index => {
    setDropdownOpenReturn(prevState => ({
      ...prevState,
      [index]: !prevState[index],
    }));
  };

  return (
    <div>
      <div id="menu-service-heading" data-cy="MenuServiceHeading">
        <h2 id="check-out-heading" data-cy="CheckOutHeading">
          <div className="d-flex gap-3 align-items-center">
            <Translate contentKey="systemLibraryApp.checkOut.home.title">Check Outs</Translate>
          </div>
        </h2>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="systemLibraryApp.checkOut.home.refreshListLabel">Refresh List</Translate>
          </Button>
        </div>
        <div className="d-flex justify-content-center">
          {returnedParams == 'false' ? (
            <div className="d-flex align-items-center gap-3">
              <p className="mb-0">Choose Status:</p>
              <Radio.Group value={statusCheckOut} onChange={onChange} className="my-3">
                <Radio.Button value={Status.Pending}>
                  <Translate contentKey={`systemLibraryApp.checkOut.${Status.Pending}`}>Pending</Translate>
                </Radio.Button>
                <Radio.Button value={Status.Canceled}>
                  <Translate contentKey={`systemLibraryApp.checkOut.${Status.Canceled}`}>Canceled</Translate>
                </Radio.Button>
                <Radio.Button value={Status.Confirmed}>
                  <Translate contentKey={`systemLibraryApp.checkOut.${Status.Confirmed}`}>Confirmed</Translate>
                </Radio.Button>
                <Radio.Button value={Status.Refused}>
                  <Translate contentKey={`systemLibraryApp.checkOut.${Status.Refused}`}>Refused</Translate>
                </Radio.Button>
              </Radio.Group>
            </div>
          ) : null}
        </div>
      </div>
      <div className="table-responsive">
        {checkOutList && checkOutList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand">
                  <Translate contentKey="systemLibraryApp.checkOut.startTime">Start Time</Translate>
                </th>
                <th className="hand">
                  <Translate contentKey="systemLibraryApp.checkOut.endTime">End Time</Translate>
                </th>

                {returnedParams == 'true' ? (
                  <th className="hand">
                    <Translate contentKey="systemLibraryApp.checkOut.isReturned">Returned</Translate>
                  </th>
                ) : (
                  <th className="hand">
                    <Translate contentKey="systemLibraryApp.checkOut.status">Status</Translate>
                  </th>
                )}

                <th>
                  <Translate contentKey="systemLibraryApp.bookCopy.bookTitle">Book Title</Translate>
                </th>
                <th className="hand">
                  <Translate contentKey="systemLibraryApp.bookCopy.publisher">Publisher</Translate>
                </th>
                <th className="hand">
                  <Translate contentKey="systemLibraryApp.bookCopy.yearPublished">Year</Translate>
                </th>
                <th>
                  <Translate contentKey="systemLibraryApp.checkOut.patronAccount">Patron Account</Translate>
                </th>
                <th>
                  <Translate contentKey="systemLibraryApp.patronAccount.userName">Username</Translate>
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {checkOutList.map((checkOut, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>{checkOut.startTime ? <TextFormat type="date" value={checkOut.startTime} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{checkOut.endTime ? <TextFormat type="date" value={checkOut.endTime} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>
                    {returnedParams == 'false' ? (
                      <Dropdown isOpen={dropdownOpen[i] || false} toggle={() => toggleDropdown(i)} className="activation-dropdown">
                        <Button className="activation-toggle" onClick={() => toggleDropdown(i)}>
                          {checkOut.status === Status.Confirmed ? (
                            <Badge color="success">{translate('global.form.confirmed')}</Badge>
                          ) : null}
                          {checkOut.status === Status.Pending ? <Badge color="warning">{translate('global.form.pending')}</Badge> : null}
                          {checkOut.status === Status.Refused ? <Badge color="danger">{translate('global.form.refused')}</Badge> : null}
                          {checkOut.status === Status.Canceled ? <Badge color="danger">{translate('global.form.canceled')}</Badge> : null}
                          {checkOut.status === Status.Pending ? <FontAwesomeIcon icon={faCaretDown} className="ml-2" /> : null}
                        </Button>
                        {checkOut.status === Status.Pending ? (
                          <DropdownMenu>
                            <DropdownItem onClick={() => handleSelectStatus(checkOut.id, Status.Canceled)}>
                              <Badge color="danger">{translate('global.form.canceled')}</Badge>
                            </DropdownItem>
                          </DropdownMenu>
                        ) : null}
                      </Dropdown>
                    ) : (
                      <td>{checkOut.isReturned ? <Badge color="success">Yes</Badge> : <Badge color="danger">No</Badge>}</td>
                    )}
                  </td>
                  <td>{checkOut.bookCopy?.book ? checkOut.bookCopy.book.title : ''}</td>
                  <td>{checkOut.bookCopy?.publisher ? checkOut.bookCopy.publisher.name : ''}</td>
                  <td>{checkOut.bookCopy ? checkOut.bookCopy.yearPublished : ''}</td>
                  <td>{checkOut.patronAccount ? checkOut.patronAccount.cardNumber : ''}</td>
                  <td>{checkOut.patronAccount?.user ? checkOut.patronAccount.user?.login : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        tag={Link}
                        to={`/check-out/${checkOut.id}?status=${statusCheckOut}`}
                        color="info"
                        size="sm"
                        data-cy="entityDetailsButton"
                      >
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
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
              <Translate contentKey="systemLibraryApp.checkOut.home.notFound">No Check Outs found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default CheckOutClient;
