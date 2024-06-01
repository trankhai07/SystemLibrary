import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './wait-list.reducer';

export const WaitListDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const waitListEntity = useAppSelector(state => state.waitList.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="waitListDetailsHeading">
          <Translate contentKey="systemLibraryApp.waitList.detail.title">WaitList</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{waitListEntity.id}</dd>
          <dt>
            <span id="creatAt">
              <Translate contentKey="systemLibraryApp.waitList.creatAt">Creat At</Translate>
            </span>
          </dt>
          <dd>{waitListEntity.creatAt ? <TextFormat value={waitListEntity.creatAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <Translate contentKey="systemLibraryApp.waitList.patronAccount">Patron Account</Translate>
          </dt>
          <dd>{waitListEntity.patronAccount ? waitListEntity.patronAccount.cardNumber : ''}</dd>
          <dt>
            <Translate contentKey="systemLibraryApp.waitList.book">Book</Translate>
          </dt>
          <dd>{waitListEntity.book ? waitListEntity.book.title : ''}</dd>
        </dl>
        <Button tag={Link} to="/wait-list" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/wait-list/${waitListEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default WaitListDetail;
