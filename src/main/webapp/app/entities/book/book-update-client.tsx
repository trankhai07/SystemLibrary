import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText, Input } from 'reactstrap';
import { isNumber, Translate, translate, ValidatedField, ValidatedForm } from 'react-jhipster';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntity as getBook } from './book.reducer';
import { FormProvider, useForm } from 'react-hook-form';
import UploadSingleFile, { IMedia } from 'app/components/upload/UploadSingleFile';
import { CONST_CONVERSION, variables } from 'app/utils/constants/variables';
import './style.scss';
import { getEntities as getPublishers } from '../publisher/publisher.reducer';
import { checkBookAvailable, getEntityByYearPublisher } from '../book-copy/book-copy.reducer';
import { IBookCopy } from 'app/shared/model/book-copy.model';
import { ICheckOut } from 'app/shared/model/check-out.model';
import dayjs from 'dayjs';
import { createEntity } from '../check-out/check-out.reducer';
import { createEntity as createWaitList } from '../wait-list/wait-list.reducer';
import { getEntityByUser } from '../patron-account/patron-account.reducer';
import { IWaitList } from 'app/shared/model/wait-list.model';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
export interface IResponsePayload {
  payload: { imgUrl: string };
}

export const convertDateTimeToServerExtend = (date: any): string | null => (date ? dayjs(date).toISOString() : null);

export const BookUpdateClient = () => {
  const dispatch = useAppDispatch();
  const { id } = useParams<'id'>();
  const bookCopyCheck = useAppSelector(state => state.bookCopy.entities);
  const publishers = useAppSelector(state => state.publisher.entities);
  const [years, setYears] = useState([]);
  const [images, setImages] = useState([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const bookEntity = useAppSelector(state => state.book.entity);
  const patronAccountEntity = useAppSelector(state => state.patronAccount.entity);
  const [bookCopyEntity, setBookCopyEntity] = useState<IBookCopy>();
  const [inputClassName, setInputClassName] = useState(0);
  const updating = useAppSelector(state => state.checkOut.updating);
  const updatingWaitList = useAppSelector(state => state.waitList.updating);
  const [selectedPublisher, setSelectedPublisher] = useState(0);
  const [selectedYear, setSelectedYear] = useState(0);

  const [selectedDate, setSelectedDate] = useState(null);
  const navigate = useNavigate();
  const updateSuccess = useAppSelector(state => state.checkOut.updateSuccess);
  const updateSuccessWaitList = useAppSelector(state => state.waitList.updateSuccess);

  const handleClose = () => {
    history.back();
  };
  const yearBook = async () => {
    // const res: any = await dispatch(getBook(id)).then;
    const res: any = await dispatch(getPublishers({}));
    const publishersCopy = res.payload.data;
    if (publishersCopy.length > 0) setSelectedPublisher(publishersCopy[0].id);
    await dispatch(getBook(id)).then((res: any) => {
      const bookCheck = res.payload.data;
      console.log(bookCheck);
      if (bookCheck && bookCheck.bookCopies) {
        const yearCopySet = new Set<number>();
        const imgageCopy = [];
        for (const item of bookCheck.bookCopies) {
          if (item.yearPublished != null) yearCopySet.add(item.yearPublished);
          if (item.image != null) imgageCopy.push(item.image);
        }
        const uniqueYearCopy: number[] = Array.from(yearCopySet);
        const sortYear = uniqueYearCopy.sort((a, b) => b - a);
        setYears(sortYear);
        if (sortYear.length > 0) setSelectedYear(sortYear[0]);
        setImages(imgageCopy);
      }
    });
  };
  useEffect(() => {
    dispatch(getEntityByUser());
    dispatch(
      checkBookAvailable({
        bookId: id,
      })
    );
    yearBook();
  }, []);

  useEffect(() => {
    if (updateSuccess && bookCopyCheck.length > 0) {
      handleClose();
    }
    if (updateSuccessWaitList && bookCopyCheck.length <= 0) {
      handleClose();
    }
  }, [updateSuccess, updateSuccessWaitList]);

  const findAmount = async () => {
    console.log('RUN BOOK COPY');

    const res: any = await dispatch(
      getEntityByYearPublisher({
        bookId: id,
        year: selectedYear,
        publisherId: selectedPublisher,
      })
    );
    setBookCopyEntity(res.payload.data);
    console.log('Book amount: ' + res.payload.data.amount);
  };
  useEffect(() => {
    console.log('RUNED OUT: ' + selectedYear);
    if (bookCopyCheck.length > 0 && selectedPublisher != 0 && selectedYear != 0) {
      console.log('RUNED IN');
      findAmount();
    }
    setInputClassName(0);
  }, [selectedPublisher, selectedYear]);

  const handlePublisherChange = event => {
    console.log(event.target.value);
    setSelectedPublisher(event.target.value);
  };
  const handleYearChange = event => {
    setSelectedYear(event.target.value);
  };
  const handleDateChange = event => {
    setSelectedDate(event.target.value);
  };

  const saveCheckOut = () => {
    const dateReturn = selectedDate;
    if (dateReturn && bookCopyCheck.length > 0 && bookCopyEntity?.amount > 0) {
      const entity: ICheckOut = {
        endTime: convertDateTimeToServerExtend(dateReturn),
        bookCopy: bookCopyEntity,
        patronAccount: patronAccountEntity,
      };
      dispatch(createEntity(entity));
    }
    if (!dateReturn) setInputClassName(1);
  };
  const saveWaitList = () => {
    const entity: IWaitList = {
      book: bookEntity,
      patronAccount: patronAccountEntity,
    };
    dispatch(createWaitList(entity));
  };

  const nextSlide = () => {
    setCurrentIndex(prevIndex => (prevIndex + 1) % images.length);
  };

  const prevSlide = () => {
    setCurrentIndex(prevIndex => (prevIndex - 1 + images.length) % images.length);
  };

  const getDisplayedImages = () => {
    const displayedImages = [];
    if (images.length >= 4)
      for (let i = 0; i < 4; i++) {
        const index = (currentIndex + i) % images.length;
        displayedImages.push({
          image: images[index],
          selected: images[index] === bookCopyEntity?.image,
        });
      }
    if (images.length < 4 && images.length > 0)
      for (let i = 0; i < images.length; i++) {
        const index = (currentIndex + i) % images.length;
        displayedImages.push({
          image: images[index],
          selected: images[index] === bookCopyEntity?.image,
        });
      }
    return displayedImages;
  };

  return (
    <>
      <FontAwesomeIcon icon="arrow-left" className="back-detail cursor-pointer" onClick={() => history.back()} />
      <div className="product-page">
        <div className="row">
          <div className="col-md-6 col-sm-6">
            <div className="product-main-image">
              <img src={bookEntity.image} alt="Cool green dress with red bell" className="img-responsive"></img>
            </div>
            <div className="product-other-images">
              <button onClick={prevSlide}>&lt;</button>
              <div className="slider-container">
                {getDisplayedImages()
                  ? getDisplayedImages().map((item, index) => (
                      <img key={index} alt={`BookCopy ${index}`} src={item.image} className={item.selected ? 'slider-image' : null} />
                    ))
                  : null}
              </div>
              <button onClick={nextSlide}>&gt;</button>
            </div>
          </div>
          <div className="col-md-6 col-sm-6">
            <h1>{bookEntity.title}</h1>
            <div className="price-availability-block clearfix">
              <div className="price" style={{ display: 'flex', flexDirection: 'column' }}>
                <div style={{ marginBottom: '10px' }}>
                  <span style={{ color: '#8e9ca8' }}>
                    <Translate contentKey="global.menu.entities.author">Authors</Translate>:{' '}
                  </span>
                  {bookEntity.authors
                    ? bookEntity.authors.map((author, it) => {
                        return (
                          <>
                            <span key={it}>
                              {author.name}
                              {it !== bookEntity.authors.length - 1 ? ', ' : ''}
                            </span>
                          </>
                        );
                      })
                    : ''}
                </div>
                <div>
                  <span style={{ color: '#8e9ca8' }}>
                    <Translate contentKey="global.menu.entities.category">Category</Translate>:{' '}
                  </span>
                  {bookEntity.category ? <span>{bookEntity.category.name}</span> : null}
                </div>
              </div>
              <div className="availability">
                Availability:
                {bookCopyCheck.length > 0 ? (
                  <strong>
                    <span style={{ color: '#20c97b' }}> In Library</span>
                  </strong>
                ) : (
                  <strong>
                    <span style={{ color: '#dc3545' }}> Unavailable</span>
                  </strong>
                )}
              </div>
            </div>
            <div className="description">
              <p>{bookEntity.description}</p>
            </div>
            <div className="product-page-options">
              <div className="pull-left">
                <label className="control-label">Publisher:</label>
                <select className="filter-categories border-0 mb-0" onChange={handlePublisherChange}>
                  {publishers && publishers.length > 0
                    ? publishers.map(publisher => (
                        <option value={publisher.id} key={publisher.id}>
                          {publisher.name}
                        </option>
                      ))
                    : null}
                </select>
              </div>
              <div className="pull-left">
                <label className="control-label">Year:</label>
                <select className="filter-categories border-0 mb-0" onChange={handleYearChange}>
                  {years.length > 0 ? (
                    years.map(year => (
                      <option value={year} key={year}>
                        {year}
                      </option>
                    ))
                  ) : (
                    <option style={{ color: '#dc3545' }}>Unavailable</option>
                  )}
                </select>
              </div>
            </div>
            <div className="product-page-options">
              <div className="pull-left">
                <Input
                  type="date"
                  onChange={handleDateChange}
                  className={inputClassName === 0 ? 'form-control' : 'form-control is-invalid'}
                />
              </div>
              <div className="pull-left">
                <label className="control-label">Available:</label>
                <div className="number-box">{bookCopyEntity?.amount ? <span>{bookCopyEntity.amount}</span> : <span>0</span>}</div>
              </div>
            </div>
            <div className="borrow-book">
              {bookCopyCheck.length > 0 ? (
                bookCopyEntity?.amount > 0 ? (
                  <button className="btn btn-primary" type="button" onClick={saveCheckOut} disabled={updating}>
                    Borrow
                  </button>
                ) : (
                  <button className="btn btn-primary" type="button" disabled>
                    Borrow
                  </button>
                )
              ) : (
                <button
                  className="btn btn-primary"
                  type="button"
                  onClick={saveWaitList}
                  disabled={bookCopyCheck.length > 0 ? true : updatingWaitList}
                >
                  Add Wait List
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default BookUpdateClient;
