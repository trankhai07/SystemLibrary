import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, Translate, translate, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IPatronAccount } from 'app/shared/model/patron-account.model';
import { getEntities as getPatronAccounts } from 'app/entities/patron-account/patron-account.reducer';
import { IBook } from 'app/shared/model/book.model';
import { getEntities as getBooks } from 'app/entities/book/book.reducer';
import { IWaitList } from 'app/shared/model/wait-list.model';
import { getEntity, updateEntity, createEntity, reset } from './wait-list.reducer';

export const WaitListUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const patronAccounts = useAppSelector(state => state.patronAccount.entities);
  const books = useAppSelector(state => state.book.entities);
  const waitListEntity = useAppSelector(state => state.waitList.entity);
  const loading = useAppSelector(state => state.waitList.loading);
  const updating = useAppSelector(state => state.waitList.updating);
  const updateSuccess = useAppSelector(state => state.waitList.updateSuccess);

  const handleClose = () => {
    navigate('/wait-list' + location.search);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getPatronAccounts({}));
    dispatch(getBooks({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    values.creatAt = convertDateTimeToServer(values.creatAt);

    const entity = {
      ...waitListEntity,
      ...values,
      patronAccount: patronAccounts.find(it => it.cardNumber.toString() === values.patronAccount.toString()),
      book: books.find(it => it.id.toString() === values.book.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {
          creatAt: displayDefaultDateTime(),
        }
      : {
          ...waitListEntity,
          creatAt: convertDateTimeFromServer(waitListEntity.creatAt),
          patronAccount: waitListEntity?.patronAccount?.cardNumber,
          book: waitListEntity?.book?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="systemLibraryApp.waitList.home.createOrEditLabel" data-cy="WaitListCreateUpdateHeading">
            <Translate contentKey="systemLibraryApp.waitList.home.createOrEditLabel">Create or edit a WaitList</Translate>
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
                  id="wait-list-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('systemLibraryApp.waitList.creatAt')}
                id="wait-list-creatAt"
                name="creatAt"
                data-cy="creatAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                id="wait-list-patronAccount"
                name="patronAccount"
                data-cy="patronAccount"
                label={translate('systemLibraryApp.waitList.patronAccount')}
                type="select"
              >
                <option value="" key="0" />
                {patronAccounts
                  ? patronAccounts.map(otherEntity => (
                      <option value={otherEntity.cardNumber} key={otherEntity.cardNumber}>
                        {otherEntity.cardNumber}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField
                id="wait-list-book"
                name="book"
                data-cy="book"
                label={translate('systemLibraryApp.waitList.book')}
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
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/wait-list" replace color="info">
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

export default WaitListUpdate;
