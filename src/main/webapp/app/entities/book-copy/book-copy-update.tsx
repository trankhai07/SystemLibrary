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
import UploadSingleFile, { IMedia } from 'app/components/upload/UploadSingleFile';
import { CONST_CONVERSION, variables } from 'app/utils/constants/variables';
import { ResponseS3Img, uploadSingleFileS3 } from 'app/components/S3Upload/s3.upload';
import { IResponsePayload } from '../book/book-update';
import { FormProvider, useForm } from 'react-hook-form';
import CommonInput from 'app/components/CommonInput';
import CommonSelect from 'app/components/CommonSelect';

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

  const [errorFile, setErrorFile] = useState(false);
  const [media, setMedia] = useState<IMedia>();
  const methods = useForm({
    mode: 'onSubmit',
    criteriaMode: 'firstError',
    reValidateMode: 'onChange',
  });

  const params = new URLSearchParams(location.search);
  const bookId = params.get('bookId');

  const handleClose = () => {
    history.back();
  };

  const getFileUrl = async () => {
    const mediaObj = {} as ResponseS3Img;
    const resp1 = (await dispatch(uploadSingleFileS3({ file: media.image.originFileObj as File }))) as IResponsePayload;
    if (resp1?.payload?.imgUrl) {
      mediaObj.imgUrl = resp1?.payload?.imgUrl;
    }
    return mediaObj;
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

  const saveEntity = async values => {
    if (!errorFile) {
      const newMedia = media ? await getFileUrl() : '';
      const entity = isNew
        ? {
            ...bookCopyEntity,
            ...values,
            book: books.find(it => it.id.toString() === (bookCopyEntity?.book?.id ? bookCopyEntity.book.id : bookId)),
            publisher: publishers.find(it => it.id.toString() === values.publisher.toString()),
            image: newMedia ? newMedia.imgUrl : values.image,
          }
        : {
            ...bookCopyEntity,
            ...values,
            book: bookCopyEntity?.book,
            publisher: publishers.find(it => it.id.toString() === values.publisher.toString()),
            image: newMedia ? newMedia.imgUrl : values.image,
          };

      if (isNew) {
        dispatch(createEntity(entity));
      } else {
        dispatch(updateEntity(entity));
      }
    }
  };
  useEffect(() => {
    setErrorFile(media?.image?.size / CONST_CONVERSION.VALUE > variables.IMAGE_SIZE);
  }, [media]);

  useEffect(() => {
    isNew
      ? methods.reset({})
      : methods.reset({
          ...bookCopyEntity,
          publisher: bookCopyEntity?.publisher?.id,
        });
  }, [bookCopyEntity]);

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="systemLibraryApp.bookCopy.home.createOrEditLabel" data-cy="BookCopyCreateUpdateHeading">
            {isNew ? (
              <Translate contentKey="systemLibraryApp.bookCopy.home.createLabel">Create Book Copy</Translate>
            ) : (
              <Translate contentKey="systemLibraryApp.bookCopy.home.editLabel">Edit Book Copy</Translate>
            )}
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <FormProvider {...methods}>
              <form onSubmit={methods.handleSubmit(saveEntity)}>
                <CommonInput
                  name="yearPublished"
                  label={<p style={{ margin: 0 }}>{`${translate('systemLibraryApp.bookCopy.yearPublished')}`}</p>}
                  type="number"
                  placeholder={translate('systemLibraryApp.bookCopy.yearPublished')}
                />
                <CommonInput
                  name="amount"
                  label={
                    <p style={{ margin: 0 }}>
                      {`${translate('systemLibraryApp.bookCopy.amount')}`}
                      {<span style={{ color: '#FF7851' }}>*</span>}
                    </p>
                  }
                  type="number"
                  placeholder={translate('systemLibraryApp.bookCopy.amount')}
                  rules={{
                    required: { value: true, message: translate('entity.validation.required') },
                  }}
                />
                <div className="text-center mb-2">{`${translate('entity.recomment.image')}`}</div>
                <UploadSingleFile
                  accept="image/*"
                  url={methods.getValues('image')}
                  media={media as IMedia}
                  isSingleImage={true}
                  setImage={setMedia}
                  isNew={isNew}
                />
                {errorFile && <p style={{ color: 'red', textAlign: 'center' }}>{translate('entity.recomment.image')}</p>}
                <CommonInput
                  name="description"
                  type="textarea"
                  label={translate('systemLibraryApp.bookCopy.description')}
                  placeholder={translate('systemLibraryApp.bookCopy.description')}
                  rows={5}
                />
                <CommonSelect
                  name="publisher"
                  label={translate('systemLibraryApp.bookCopy.publisher')}
                  placeholder={translate('systemLibraryApp.bookCopy.publisher')}
                  options={
                    publishers && publishers.length > 0
                      ? publishers.map(otherEntity => ({ key: otherEntity.id, value: otherEntity.id, label: otherEntity.name }))
                      : null
                  }
                  rules={{
                    required: { value: true, message: translate('entity.validation.required') },
                  }}
                />
                <Button onClick={() => history.back()} id="cancel-save" data-cy="entityCreateCancelButton" replace color="info">
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
              </form>
            </FormProvider>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default BookCopyUpdate;
