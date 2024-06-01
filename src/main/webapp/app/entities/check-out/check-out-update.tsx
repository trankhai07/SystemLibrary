import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, Translate, translate, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IBookCopy } from 'app/shared/model/book-copy.model';
import { getEntities as getBookCopies } from 'app/entities/book-copy/book-copy.reducer';
import { IPatronAccount } from 'app/shared/model/patron-account.model';
import { getEntities as getPatronAccounts } from 'app/entities/patron-account/patron-account.reducer';
import { ICheckOut } from 'app/shared/model/check-out.model';
import { Status } from 'app/shared/model/enumerations/status.model';
import { getEntity, updateEntity, createEntity, reset } from './check-out.reducer';

export const CheckOutUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const bookCopies = useAppSelector(state => state.bookCopy.entities);
  const patronAccounts = useAppSelector(state => state.patronAccount.entities);
  const checkOutEntity = useAppSelector(state => state.checkOut.entity);
  const loading = useAppSelector(state => state.checkOut.loading);
  const updating = useAppSelector(state => state.checkOut.updating);
  const updateSuccess = useAppSelector(state => state.checkOut.updateSuccess);
  const statusValues = Object.keys(Status);

  const handleClose = () => {
    navigate('/check-out' + location.search);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getBookCopies({}));
    dispatch(getPatronAccounts({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    values.startTime = convertDateTimeToServer(values.startTime);
    values.endTime = convertDateTimeToServer(values.endTime);

    const entity = {
      ...checkOutEntity,
      ...values,
      bookCopy: bookCopies.find(it => it.id.toString() === values.bookCopy.toString()),
      patronAccount: patronAccounts.find(it => it.cardNumber.toString() === values.patronAccount.toString()),
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
          startTime: displayDefaultDateTime(),
          endTime: displayDefaultDateTime(),
        }
      : {
          status: 'Confirmed',
          ...checkOutEntity,
          startTime: convertDateTimeFromServer(checkOutEntity.startTime),
          endTime: convertDateTimeFromServer(checkOutEntity.endTime),
          bookCopy: checkOutEntity?.bookCopy?.id,
          patronAccount: checkOutEntity?.patronAccount?.cardNumber,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="systemLibraryApp.checkOut.home.createOrEditLabel" data-cy="CheckOutCreateUpdateHeading">
            <Translate contentKey="systemLibraryApp.checkOut.home.createOrEditLabel">Create or edit a CheckOut</Translate>
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
                  id="check-out-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('systemLibraryApp.checkOut.startTime')}
                id="check-out-startTime"
                name="startTime"
                data-cy="startTime"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label={translate('systemLibraryApp.checkOut.endTime')}
                id="check-out-endTime"
                name="endTime"
                data-cy="endTime"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label={translate('systemLibraryApp.checkOut.status')}
                id="check-out-status"
                name="status"
                data-cy="status"
                type="select"
              >
                {statusValues.map(status => (
                  <option value={status} key={status}>
                    {translate('systemLibraryApp.Status.' + status)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('systemLibraryApp.checkOut.isReturned')}
                id="check-out-isReturned"
                name="isReturned"
                data-cy="isReturned"
                check
                type="checkbox"
              />
              <ValidatedField
                id="check-out-bookCopy"
                name="bookCopy"
                data-cy="bookCopy"
                label={translate('systemLibraryApp.checkOut.bookCopy')}
                type="select"
              >
                <option value="" key="0" />
                {bookCopies
                  ? bookCopies.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField
                id="check-out-patronAccount"
                name="patronAccount"
                data-cy="patronAccount"
                label={translate('systemLibraryApp.checkOut.patronAccount')}
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
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/check-out" replace color="info">
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

export default CheckOutUpdate;
