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
import { INotification } from 'app/shared/model/notification.model';
import { NoTi } from 'app/shared/model/enumerations/no-ti.model';
import { getEntity, updateEntity, createEntity, reset } from './notification.reducer';

export const NotificationUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const patronAccounts = useAppSelector(state => state.patronAccount.entities);
  const notificationEntity = useAppSelector(state => state.notification.entity);
  const loading = useAppSelector(state => state.notification.loading);
  const updating = useAppSelector(state => state.notification.updating);
  const updateSuccess = useAppSelector(state => state.notification.updateSuccess);
  const noTiValues = Object.keys(NoTi);

  const handleClose = () => {
    navigate('/notification' + location.search);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getPatronAccounts({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    values.sentAt = convertDateTimeToServer(values.sentAt);

    const entity = {
      ...notificationEntity,
      ...values,
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
          sentAt: displayDefaultDateTime(),
        }
      : {
          type: 'Warning',
          ...notificationEntity,
          sentAt: convertDateTimeFromServer(notificationEntity.sentAt),
          patronAccount: notificationEntity?.patronAccount?.cardNumber,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="systemLibraryApp.notification.home.createOrEditLabel" data-cy="NotificationCreateUpdateHeading">
            <Translate contentKey="systemLibraryApp.notification.home.createOrEditLabel">Create or edit a Notification</Translate>
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
                  id="notification-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('systemLibraryApp.notification.sentAt')}
                id="notification-sentAt"
                name="sentAt"
                data-cy="sentAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label={translate('systemLibraryApp.notification.type')}
                id="notification-type"
                name="type"
                data-cy="type"
                type="select"
              >
                {noTiValues.map(noTi => (
                  <option value={noTi} key={noTi}>
                    {translate('systemLibraryApp.NoTi.' + noTi)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                id="notification-patronAccount"
                name="patronAccount"
                data-cy="patronAccount"
                label={translate('systemLibraryApp.notification.patronAccount')}
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
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/notification" replace color="info">
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

export default NotificationUpdate;
