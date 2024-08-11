import './home.scss';
import React, { useEffect, useState } from 'react';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { JhiItemCount, JhiPagination, Translate, getSortState, translate } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { getEntitiesByCategory, searchEntitiesByCategory } from 'app/entities/book/book.reducer';
import { getEntities as getCategories } from 'app/entities/category/category.reducer';
import InputSearch from 'app/components/input-search';
import { Button } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AUTHORITIES } from 'app/config/constants';

export const Home = () => {
  const dispatch = useAppDispatch();
  const location = useLocation();
  const navigate = useNavigate();
  const [search, setSearch] = useState('');
  const params = new URLSearchParams(location.search);
  const categoryParam = params.get('categoryId');
  const account = useAppSelector(state => state.authentication.account);
  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getSortState(location, ITEMS_PER_PAGE, 'id'), location.search)
  );

  const [selectedCategory, setSelectedCategory] = useState(categoryParam ?? -1);

  const categories = useAppSelector(state => state.category.entities);

  const bookList = useAppSelector(state => state.book.entities);
  const totalItems = useAppSelector(state => state.book.totalItems);

  const getAllEntities = () => {
    if (search) {
      dispatch(
        searchEntitiesByCategory({
          query: search,
          categoryId: selectedCategory,
          page: paginationState.activePage - 1,
          size: paginationState.itemsPerPage,
          sort: `${paginationState.sort},${paginationState.order}`,
        })
      );
    } else {
      dispatch(
        getEntitiesByCategory({
          page: paginationState.activePage - 1,
          size: paginationState.itemsPerPage,
          sort: `${paginationState.sort},${paginationState.order}`,
          categoryId: selectedCategory,
        })
      );
    }
  };
  const clear = () => {
    setSearch('');
    setPaginationState({
      ...paginationState,
      activePage: 1,
    });
    dispatch(
      getEntitiesByCategory({
        page: paginationState.activePage - 1,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},${paginationState.order}`,
        categoryId: selectedCategory,
      })
    );
  };
  const handleSearch = event => setSearch(event.target.value);
  const sortEntities = () => {
    getAllEntities();
    const endURL = `?categoryId=${selectedCategory}&page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (location.search !== endURL) {
      navigate(`${location.pathname}${endURL}`);
    }
  };
  useEffect(() => {
    dispatch(getCategories({}));
  }, []);

  useEffect(() => {
    sortEntities();
  }, [paginationState.activePage, paginationState.order, paginationState.sort, search, selectedCategory]);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const page = params.get('page');
    const sort = params.get(SORT);
    if (page && sort) {
      const sortSplit = sort.split(',');
      setPaginationState({
        ...paginationState,
        activePage: +page,
        sort: sortSplit[0],
        order: sortSplit[1],
      });
    }
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

  const handleCategoryChange = event => {
    setSelectedCategory(event.target.value);
  };

  return (
    <>
      {account.authorities.includes(AUTHORITIES.USER) ? (
        <section id="foodies">
          <div className="container ">
            <div className="section-header d-md-flex justify-content-between align-items-center">
              <h2 className="display-5 fw-normal">Library</h2>
              <div className="search-book-client">
                <InputSearch name={'search'} onChange={handleSearch} defaultValue={search} />
                <Button type="reset" className="input-group-addon input-clear" onClick={clear}>
                  <FontAwesomeIcon icon="trash" />
                </Button>
              </div>
              <select className="filter-categories border-0 mb-0 me-5" value={selectedCategory} onChange={handleCategoryChange}>
                <option value="-1" key="0">
                  All
                </option>
                {categories && categories.length > 0
                  ? categories.map(category => (
                      <option value={category.id} key={category.id}>
                        {category.name}
                      </option>
                    ))
                  : null}
              </select>
            </div>

            <div className="isotope-container row">
              {bookList && bookList.length > 0 ? (
                bookList.map(book => (
                  <div className="item book col-md-4 col-lg-3">
                    <Link to={`/book/${book.id}/view`} className="book-detatil">
                      <div className="card position-relative hover-effect">
                        <img src={book.image} className="img-fluid" alt="image_book" />
                        <div className="card-body p-0">
                          <h3 className="card-title book pt-2">{book.title}</h3>
                          <div className="card-info-author">
                            <div className="card-info-key">
                              <Translate contentKey="global.menu.entities.author">Authors</Translate>:
                            </div>
                            <div className="card-info-value">
                              {book.authors
                                ? book.authors.map((author, it) => {
                                    return (
                                      <>
                                        <span key={it}>{author.name}</span>
                                        <br />
                                      </>
                                    );
                                  })
                                : ''}
                            </div>
                          </div>
                          <div className="card-info-type">
                            <span>Category: </span>
                            <span>Hanh Dong</span>
                          </div>
                        </div>
                      </div>
                    </Link>
                  </div>
                ))
              ) : (
                <div className="alert alert-warning">
                  <Translate contentKey="systemLibraryApp.book.home.notFound">No Books found</Translate>
                </div>
              )}
            </div>
          </div>
          {totalItems ? (
            <div className={bookList && bookList.length > 0 ? '' : 'd-none'}>
              <div className="justify-content-center d-flex">
                <JhiItemCount
                  page={paginationState.activePage}
                  total={totalItems}
                  itemsPerPage={paginationState.itemsPerPage}
                  i18nEnabled
                />
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
        </section>
      ) : (
        <h2 className="text-center mt-5">Welcome, {account.login}</h2>
      )}
    </>
  );
};

export default Home;
