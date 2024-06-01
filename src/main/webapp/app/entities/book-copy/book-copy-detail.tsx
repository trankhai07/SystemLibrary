import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './book-copy.reducer';

export const BookCopyDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const bookCopyEntity = useAppSelector(state => state.bookCopy.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="bookCopyDetailsHeading">
          <Translate contentKey="systemLibraryApp.bookCopy.detail.title">BookCopy</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{bookCopyEntity.id}</dd>
          <dt>
            <span id="yearPublished">
              <Translate contentKey="systemLibraryApp.bookCopy.yearPublished">Year Published</Translate>
            </span>
          </dt>
          <dd>{bookCopyEntity.yearPublished}</dd>
          <dt>
            <span id="amount">
              <Translate contentKey="systemLibraryApp.bookCopy.amount">Amount</Translate>
            </span>
          </dt>
          <dd>{bookCopyEntity.amount}</dd>
          <dt>
            <span id="image">
              <Translate contentKey="systemLibraryApp.bookCopy.image">Image</Translate>
            </span>
          </dt>
          <dd>{bookCopyEntity.image}</dd>
          <dt>
            <span id="description">
              <Translate contentKey="systemLibraryApp.bookCopy.description">Description</Translate>
            </span>
          </dt>
          <dd>{bookCopyEntity.description}</dd>
          <dt>
            <Translate contentKey="systemLibraryApp.bookCopy.book">Book</Translate>
          </dt>
          <dd>{bookCopyEntity.book ? bookCopyEntity.book.title : ''}</dd>
          <dt>
            <Translate contentKey="systemLibraryApp.bookCopy.publisher">Publisher</Translate>
          </dt>
          <dd>{bookCopyEntity.publisher ? bookCopyEntity.publisher.name : ''}</dd>
        </dl>
        <Button tag={Link} to="/book-copy" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/book-copy/${bookCopyEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default BookCopyDetail;
