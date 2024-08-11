import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './patron-account.reducer';

export const PatronAccountDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const patronAccountEntity = useAppSelector(state => state.patronAccount.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="patronAccountDetailsHeading">
          <Translate contentKey="systemLibraryApp.patronAccount.detail.title">PatronAccount</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="cardNumber">
              <Translate contentKey="systemLibraryApp.patronAccount.cardNumber">Card Number</Translate>
            </span>
          </dt>
          <dd>{patronAccountEntity.cardNumber}</dd>
          <dt>
            <Translate contentKey="systemLibraryApp.patronAccount.user">User</Translate>
          </dt>
          <dd>{patronAccountEntity.user ? patronAccountEntity.user.login : ''}</dd>
        </dl>
        <Button onClick={() => history.back()} replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/patron-account/${patronAccountEntity.cardNumber}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default PatronAccountDetail;
