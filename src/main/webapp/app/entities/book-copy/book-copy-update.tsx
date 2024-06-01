import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, Translate, translate, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IBook } from 'app/shared/model/book.model';
import { getEntities as getBooks } from 'app/entities/book/book.reducer';
import { IPublisher } from 'app/shared/model/publisher.model';
import { getEntities as getPublishers } from 'app/entities/publisher/publisher.reducer';
import { IBookCopy } from 'app/shared/model/book-copy.model';
import { getEntity, updateEntity, createEntity, reset } from './book-copy.reducer';

export const BookCopyUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const books = useAppSelector(state => state.book.entities);
  const publishers = useAppSelector(state => state.publisher.entities);
  const bookCopyEntity = useAppSelector(state => state.bookCopy.entity);
  const loading = useAppSelector(state => state.bookCopy.loading);
  const updating = useAppSelector(state => state.bookCopy.updating);
  const updateSuccess = useAppSelector(state => state.bookCopy.updateSuccess);

  const handleClose = () => {
    navigate('/book-copy' + location.search);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getBooks({}));
    dispatch(getPublishers({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...bookCopyEntity,
      ...values,
      book: books.find(it => it.id.toString() === values.book.toString()),
      publisher: publishers.find(it => it.id.toString() === values.publisher.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          ...bookCopyEntity,
          book: bookCopyEntity?.book?.id,
          publisher: bookCopyEntity?.publisher?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="systemLibraryApp.bookCopy.home.createOrEditLabel" data-cy="BookCopyCreateUpdateHeading">
            <Translate contentKey="systemLibraryApp.bookCopy.home.createOrEditLabel">Create or edit a BookCopy</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="book-copy-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('systemLibraryApp.bookCopy.yearPublished')}
                id="book-copy-yearPublished"
                name="yearPublished"
                data-cy="yearPublished"
                type="text"
              />
              <ValidatedField
                label={translate('systemLibraryApp.bookCopy.amount')}
                id="book-copy-amount"
                name="amount"
                data-cy="amount"
                type="text"
              />
              <ValidatedField
                label={translate('systemLibraryApp.bookCopy.image')}
                id="book-copy-image"
                name="image"
                data-cy="image"
                type="text"
              />
              <ValidatedField
                label={translate('systemLibraryApp.bookCopy.description')}
                id="book-copy-description"
                name="description"
                data-cy="description"
                type="text"
              />
              <ValidatedField
                id="book-copy-book"
                name="book"
                data-cy="book"
                label={translate('systemLibraryApp.bookCopy.book')}
                type="select"
              >
                <option value="" key="0" />
                {books
                  ? books.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.title}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField
                id="book-copy-publisher"
                name="publisher"
                data-cy="publisher"
                label={translate('systemLibraryApp.bookCopy.publisher')}
                type="select"
              >
                <option value="" key="0" />
                {publishers
                  ? publishers.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.name}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/book-copy" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default BookCopyUpdate;
