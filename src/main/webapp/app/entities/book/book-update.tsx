import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, Translate, translate, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IAuthor } from 'app/shared/model/author.model';
import { getEntities as getAuthors } from 'app/entities/author/author.reducer';
import { ICategory } from 'app/shared/model/category.model';
import { getEntities as getCategories } from 'app/entities/category/category.reducer';
import { IBook } from 'app/shared/model/book.model';
import { getEntity, updateEntity, createEntity, reset } from './book.reducer';
import CommonInput from 'app/components/CommonInput';
import { FormProvider, useForm } from 'react-hook-form';
import CommonSelect from 'app/components/CommonSelect';
import UploadSingleFile, { IMedia } from 'app/components/upload/UploadSingleFile';
import { CONST_CONVERSION, variables } from 'app/utils/constants/variables';
import { ResponseS3Img, uploadSingleFileS3 } from 'app/components/S3Upload/s3.upload';

export interface IResponsePayload {
  payload: { imgUrl: string };
}

export const BookUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();
  const [media, setMedia] = useState<IMedia>();
  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const authors = useAppSelector(state => state.author.entities);
  const categories = useAppSelector(state => state.category.entities);
  const bookEntity = useAppSelector(state => state.book.entity);
  const loading = useAppSelector(state => state.book.loading);
  const updating = useAppSelector(state => state.book.updating);
  const updateSuccess = useAppSelector(state => state.book.updateSuccess);
  const [errorFile, setErrorFile] = useState(false);
  const methods = useForm({
    mode: 'onSubmit',
    criteriaMode: 'firstError',
    reValidateMode: 'onChange',
  });
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

    dispatch(getAuthors({}));
    dispatch(getCategories({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  useEffect(() => {
    setErrorFile(media?.image?.size / CONST_CONVERSION.VALUE > variables.IMAGE_SIZE);
  }, [media]);
  const saveEntity = async values => {
    if (!errorFile) {
      const newMedia = media ? await getFileUrl() : '';
      const entity = {
        ...bookEntity,
        ...values,
        authors: values.authors.map(idSelect => authors.find(author => author.id.toString() === idSelect.toString())),
        category: categories.find(it => it.id.toString() === values.category.toString()),
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
    isNew
      ? methods.reset({})
      : methods.reset({
          ...bookEntity,
          authors: bookEntity?.authors?.map(author => author.id),
          category: bookEntity?.category?.id,
        });
  }, [bookEntity]);

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="systemLibraryApp.book.home.createOrEditLabel" data-cy="BookCreateUpdateHeading">
            {isNew ? (
              <Translate contentKey="systemLibraryApp.book.home.createLabel">Create Book</Translate>
            ) : (
              <Translate contentKey="systemLibraryApp.book.home.editLabel">Edit Book</Translate>
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
                  name="title"
                  label={
                    <p style={{ margin: 0 }}>
                      {`${translate('systemLibraryApp.book.title')}`}
                      {<span style={{ color: '#FF7851' }}>*</span>}
                    </p>
                  }
                  type="text"
                  placeholder={translate('systemLibraryApp.book.title')}
                  rules={{
                    required: { value: true, message: translate('entity.validation.required') },
                    maxLength: { value: 255, message: translate('entity.validation.maxlength', { max: 255 }) },
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
                  label={translate('systemLibraryApp.book.description')}
                  placeholder={translate('systemLibraryApp.book.description')}
                  rows={5}
                />
                <CommonSelect
                  name="authors"
                  mode="multiple"
                  placeholder={translate('systemLibraryApp.book.author')}
                  label={
                    <p style={{ margin: 0 }}>
                      {`${translate('systemLibraryApp.book.author')}`}
                      {<span style={{ color: '#FF7851' }}>*</span>}
                    </p>
                  }
                  options={
                    authors && authors.length > 0 ? authors.map(author => ({ key: author.id, value: author.id, label: author.name })) : null
                  }
                  rules={{
                    required: { value: true, message: translate('entity.validation.required') },
                  }}
                />
                <CommonSelect
                  name="category"
                  label={translate('systemLibraryApp.book.category')}
                  placeholder={translate('systemLibraryApp.book.category')}
                  options={
                    categories && categories.length > 0
                      ? categories.map(otherEntity => ({ key: otherEntity.id, value: otherEntity.id, label: otherEntity.name }))
                      : null
                  }
                  rules={{
                    required: { value: true, message: translate('entity.validation.required') },
                  }}
                />
                <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/book" replace color="info">
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

export default BookUpdate;
