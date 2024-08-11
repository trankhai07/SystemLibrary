import axios from 'axios';
import { createAsyncThunk, isFulfilled, isPending, isRejected } from '@reduxjs/toolkit';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { IQueryParams, createEntitySlice, EntityState, serializeAxiosError } from 'app/shared/reducers/reducer.utils';
import { IBookCopy, defaultValue } from 'app/shared/model/book-copy.model';

const initialState: EntityState<IBookCopy> = {
  loading: false,
  errorMessage: null,
  entities: [],
  entity: defaultValue,
  updating: false,
  totalItems: 0,
  updateSuccess: false,
};
type bookCopyQueryParams = IQueryParams & {
  bookId?: string | number;
  year?: string | number;
  publisherId?: string | number;
};

const apiUrl = 'api/book-copies';
const apiSearchUrl = 'api/_search/book-copies';

// Actions

export const searchEntities = createAsyncThunk('bookCopy/search_entity', async ({ query, page, size, sort }: IQueryParams) => {
  const requestUrl = `${apiSearchUrl}?query=${query}${sort ? `&page=${page}&size=${size}&sort=${sort}` : ''}`;
  return axios.get<IBookCopy[]>(requestUrl);
});

export const getEntities = createAsyncThunk('bookCopy/fetch_entity_list', async ({ bookId, page, size, sort }: bookCopyQueryParams) => {
  const requestUrl = `${apiUrl}/book${
    sort ? `?bookId=${bookId}&page=${page}&size=${size}&sort=${sort}&` : '?'
  }cacheBuster=${new Date().getTime()}`;
  return axios.get<IBookCopy[]>(requestUrl);
});

export const checkBookAvailable = createAsyncThunk('bookCopy/fetch_entity_list', async ({ bookId }: bookCopyQueryParams) => {
  const requestUrl = `${apiUrl}/check-book-available?bookId=${bookId}&cacheBuster=${new Date().getTime()}`;
  return axios.get<IBookCopy[]>(requestUrl);
});

export const getEntity = createAsyncThunk(
  'bookCopy/fetch_entity',
  async (id: string | number) => {
    const requestUrl = `${apiUrl}/${id}`;
    return axios.get<IBookCopy>(requestUrl);
  },
  { serializeError: serializeAxiosError }
);
export const getEntityByYearPublisher = createAsyncThunk(
  'bookCopy/fetch_entity',
  async ({ bookId, year, publisherId }: bookCopyQueryParams) => {
    const requestUrl = `${apiUrl}/publish-year?bookId=${bookId}&year=${year}&publisherId=${publisherId}`;
    return axios.get<IBookCopy>(requestUrl);
  },
  { serializeError: serializeAxiosError }
);

export const createEntity = createAsyncThunk(
  'bookCopy/create_entity',
  async (entity: IBookCopy, thunkAPI) => {
    const result = await axios.post<IBookCopy>(apiUrl, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const updateEntity = createAsyncThunk(
  'bookCopy/update_entity',
  async (entity: IBookCopy, thunkAPI) => {
    const result = await axios.put<IBookCopy>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const partialUpdateEntity = createAsyncThunk(
  'bookCopy/partial_update_entity',
  async (entity: IBookCopy, thunkAPI) => {
    const result = await axios.patch<IBookCopy>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const deleteEntity = createAsyncThunk(
  'bookCopy/delete_entity',
  async (id: string | number, thunkAPI) => {
    const requestUrl = `${apiUrl}/${id}`;
    const result = await axios.delete<IBookCopy>(requestUrl);
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError }
);

// slice

export const BookCopySlice = createEntitySlice({
  name: 'bookCopy',
  initialState,
  extraReducers(builder) {
    builder
      .addCase(deleteEntity.fulfilled, state => {
        state.updating = false;
        state.updateSuccess = true;
        state.entity = {};
      })
      .addMatcher(isFulfilled(getEntityByYearPublisher, getEntity), (state, action) => {
        state.loading = false;
        state.entity = action.payload.data;
      })
      .addMatcher(isFulfilled(getEntities, searchEntities, checkBookAvailable), (state, action) => {
        const { data, headers } = action.payload;

        return {
          ...state,
          loading: false,
          entities: data,
          totalItems: parseInt(headers['x-total-count'], 10),
        };
      })
      .addMatcher(isFulfilled(createEntity, updateEntity, partialUpdateEntity), (state, action) => {
        state.updating = false;
        state.loading = false;
        state.updateSuccess = true;
        state.entity = action.payload.data;
      })
      .addMatcher(isPending(getEntities, getEntity, searchEntities, checkBookAvailable, getEntityByYearPublisher), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      })
      .addMatcher(isPending(createEntity, updateEntity, partialUpdateEntity, deleteEntity), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.updating = true;
      });
  },
});

export const { reset } = BookCopySlice.actions;

// Reducer
export default BookCopySlice.reducer;
