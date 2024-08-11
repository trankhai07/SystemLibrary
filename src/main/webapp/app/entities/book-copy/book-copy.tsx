import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Input, InputGroup, FormGroup, Form, Row, Col, Table } from 'reactstrap';
import { Translate, translate, getSortState, JhiPagination, JhiItemCount, IPaginationBaseState } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IBookCopy } from 'app/shared/model/book-copy.model';
import { searchEntities, getEntities } from './book-copy.reducer';
import { EPath } from 'app/utils/constants/EPath';
import InputSearch from 'app/components/input-search';
export interface IPaginationStateExtend {
  itemsPerPage: number;
  sort: string;
  order: string;
  activePage: number;
  bookId?: string | number;
  status?: string;
  categoryId?: string | number;
}
export const overridePaginationStateWithQueryParamsExtend = (
  paginationBaseState: IPaginationBaseState,
  locationSearch: string
): IPaginationStateExtend => {
  const params = new URLSearchParams(locationSearch);
  const page = params.get('page');
  const sort = params.get('sort');
  const bookId = params.get('bookId');
  if (page && sort) {
    const sortSplit = sort.split(',');
    paginationBaseState.activePage = +page;
    paginationBaseState.sort = sortSplit[0];
    paginationBaseState.order = sortSplit[1];
  }
  return bookId
    ? {
        ...paginationBaseState,
        bookId,
      }
    : paginationBaseState;
};
export const BookCopy = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();
  const params = new URLSearchParams(location.search);
  const page = params.get('page');
  const bookId = params.get('bookId');

  const [search, setSearch] = useState('');
  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParamsExtend(getSortState(location, ITEMS_PER_PAGE, 'id'), location.search)
  );

  const bookCopyList = useAppSelector(state => state.bookCopy.entities);
  const loading = useAppSelector(state => state.bookCopy.loading);
  const totalItems = useAppSelector(state => state.bookCopy.totalItems);

  const getAllEntities = () => {
    if (search) {
      dispatch(
        searchEntities({
          query: search,
          page: paginationState.activePage - 1,
          size: paginationState.itemsPerPage,
          sort: `${paginationState.sort},${paginationState.order}`,
        })
      );
    } else {
      dispatch(
        getEntities({
          bookId: paginationState.bookId,
          page: paginationState.activePage - 1,
          size: paginationState.itemsPerPage,
          sort: `${paginationState.sort},${paginationState.order}`,
        })
      );
    }
  };

  const startSearching = e => {
    if (search) {
      setPaginationState({
        ...paginationState,
        activePage: 1,
      });
      dispatch(
        searchEntities({
          query: search,
          page: paginationState.activePage - 1,
          size: paginationState.itemsPerPage,
          sort: `${paginationState.sort},${paginationState.order}`,
        })
      );
    }
    e.preventDefault();
  };

  const clear = () => {
    setSearch('');
    setPaginationState({
      ...paginationState,
      activePage: 1,
    });
    dispatch(getEntities({}));
  };

  const handleSearch = event => setSearch(event.target.value);

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?bookId=${paginationState.bookId}&page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (location.search !== endURL) {
      navigate(`${location.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [paginationState.activePage, paginationState.order, paginationState.sort, search, paginationState.bookId]);

  useEffect(() => {
    const sort = params.get(SORT);
    let data = {} as IPaginationStateExtend;
    if (page && sort) {
      const sortSplit = sort.split(',');
      data.activePage = +page;
      data.sort = sortSplit[0];
      data.order = sortSplit[1];
      if (bookId) data.bookId = bookId;
    }
    setPaginationState({
      ...paginationState,
      ...data,
    });
  }, [location.search]);

  const sort = p => () => {
    setPaginationState({
      ...paginationState,
      order: paginationState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handlePagination = currentPage =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });

  const handleSyncList = () => {
    sortEntities();
  };

  return (
    <div>
      <h2 id="book-copy-heading" data-cy="BookCopyHeading">
        <div className="d-flex gap-3 align-items-center">
          <FontAwesomeIcon icon="arrow-left" className="cursor-pointer" onClick={() => navigate(EPath.Book)} />
          <Translate contentKey="systemLibraryApp.bookCopy.home.title">Book Copies</Translate>
        </div>

        <div className="d-flex justify-content-between align-items-center mt-3">
          <div className="w-25">
            <InputSearch name={'search'} onChange={handleSearch} defaultValue={search} />
            <Button type="reset" className="input-group-addon input-clear" onClick={clear}>
              <FontAwesomeIcon icon="trash" />
            </Button>
          </div>
          <div>
            <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
              <FontAwesomeIcon icon="sync" spin={loading} />{' '}
              <Translate contentKey="systemLibraryApp.bookCopy.home.refreshListLabel">Refresh List</Translate>
            </Button>
            <Link
              to={`${EPath.BookCopy}/new?bookId=${paginationState.bookId}`}
              className="btn btn-primary jh-create-entity"
              id="jh-create-entity"
              data-cy="entityCreateButton"
            >
              <FontAwesomeIcon icon="plus" />
              &nbsp;
              <Translate contentKey="systemLibraryApp.bookCopy.home.createLabel">Create new Book Copy</Translate>
            </Link>
          </div>
        </div>
      </h2>
      <div className="table-responsive">
        {bookCopyList && bookCopyList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  <Translate contentKey="systemLibraryApp.bookCopy.id">ID</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  <Translate contentKey="systemLibraryApp.bookCopy.book">Book</Translate>
                </th>
                <th className="">
                  <Translate contentKey="systemLibraryApp.bookCopy.image">Image</Translate>
                </th>
                <th>
                  <Translate contentKey="systemLibraryApp.bookCopy.publisher">Publisher</Translate>
                </th>
                <th className="hand" onClick={sort('year_published')}>
                  <Translate contentKey="systemLibraryApp.bookCopy.yearPublished">Year Published</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('amount')}>
                  <Translate contentKey="systemLibraryApp.bookCopy.amount">Amount</Translate> <FontAwesomeIcon icon="sort" />
                </th>

                <th className="">
                  <Translate contentKey="systemLibraryApp.bookCopy.description">Description</Translate>
                </th>

                <th />
              </tr>
            </thead>
            <tbody>
              {bookCopyList.map((bookCopy, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>{bookCopy.id}</td>
                  <td>{bookCopy.book ? bookCopy.book.title : ''}</td>
                  <td>
                    {bookCopy.image && (
                      <img style={{ width: '100px', height: '50px', objectFit: 'cover' }} src={bookCopy.image} alt="image_book_copy" />
                    )}
                  </td>
                  <td>{bookCopy.publisher ? bookCopy.publisher.name : ''}</td>
                  <td>{bookCopy.yearPublished}</td>
                  <td>{bookCopy.amount}</td>
                  <td>{bookCopy.description}</td>

                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/book-copy/${bookCopy.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/book-copy/${bookCopy.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                        color="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/book-copy/${bookCopy.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                        color="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.delete">Delete</Translate>
                        </span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && (
            <div className="alert alert-warning">
              <Translate contentKey="systemLibraryApp.bookCopy.home.notFound">No Book Copies found</Translate>
            </div>
          )
        )}
      </div>
      {totalItems ? (
        <div className={bookCopyList && bookCopyList.length > 0 ? '' : 'd-none'}>
          <div className="justify-content-center d-flex">
            <JhiItemCount page={paginationState.activePage} total={totalItems} itemsPerPage={paginationState.itemsPerPage} i18nEnabled />
          </div>
          <div className="justify-content-center d-flex">
            <JhiPagination
              activePage={paginationState.activePage}
              onSelect={handlePagination}
              maxButtons={5}
              itemsPerPage={paginationState.itemsPerPage}
              totalItems={totalItems}
            />
          </div>
        </div>
      ) : (
        ''
      )}
    </div>
  );
};

export default BookCopy;
