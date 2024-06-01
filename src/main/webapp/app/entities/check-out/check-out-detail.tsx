import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './check-out.reducer';

export const CheckOutDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const checkOutEntity = useAppSelector(state => state.checkOut.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="checkOutDetailsHeading">
          <Translate contentKey="systemLibraryApp.checkOut.detail.title">CheckOut</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{checkOutEntity.id}</dd>
          <dt>
            <span id="startTime">
              <Translate contentKey="systemLibraryApp.checkOut.startTime">Start Time</Translate>
            </span>
          </dt>
          <dd>{checkOutEntity.startTime ? <TextFormat value={checkOutEntity.startTime} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="endTime">
              <Translate contentKey="systemLibraryApp.checkOut.endTime">End Time</Translate>
            </span>
          </dt>
          <dd>{checkOutEntity.endTime ? <TextFormat value={checkOutEntity.endTime} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="status">
              <Translate contentKey="systemLibraryApp.checkOut.status">Status</Translate>
            </span>
          </dt>
          <dd>{checkOutEntity.status}</dd>
          <dt>
            <span id="isReturned">
              <Translate contentKey="systemLibraryApp.checkOut.isReturned">Is Returned</Translate>
            </span>
          </dt>
          <dd>{checkOutEntity.isReturned ? 'true' : 'false'}</dd>
          <dt>
            <Translate contentKey="systemLibraryApp.checkOut.bookCopy">Book Copy</Translate>
          </dt>
          <dd>{checkOutEntity.bookCopy ? checkOutEntity.bookCopy.id : ''}</dd>
          <dt>
            <Translate contentKey="systemLibraryApp.checkOut.patronAccount">Patron Account</Translate>
          </dt>
          <dd>{checkOutEntity.patronAccount ? checkOutEntity.patronAccount.cardNumber : ''}</dd>
        </dl>
        <Button tag={Link} to="/check-out" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/check-out/${checkOutEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default CheckOutDetail;
