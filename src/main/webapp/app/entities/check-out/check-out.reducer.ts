import axios from 'axios';
import { createAsyncThunk, isFulfilled, isPending, isRejected } from '@reduxjs/toolkit';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { IQueryParams, createEntitySlice, EntityState, serializeAxiosError } from 'app/shared/reducers/reducer.utils';
import { ICheckOut, defaultValue } from 'app/shared/model/check-out.model';

const initialState: EntityState<ICheckOut> = {
  loading: false,
  errorMessage: null,
  entities: [],
  entity: defaultValue,
  updating: false,
  totalItems: 0,
  updateSuccess: false,
};

const apiUrl = 'api/check-outs';
const apiUrlClient = 'api/check-outs-client';
const apiSearchUrl = 'api/_search/check-outs';

// Actions

export const searchEntities = createAsyncThunk('checkOut/search_entity', async ({ query, page, size, sort }: IQueryParams) => {
  const requestUrl = `${apiSearchUrl}?query=${query}${sort ? `&page=${page}&size=${size}&sort=${sort}` : ''}`;
  return axios.get<ICheckOut[]>(requestUrl);
});
type TCheckOutParams = IQueryParams & {
  status?: string;
  cardNumber?: string;
  returned?: string | boolean;
};
export const getEntities = createAsyncThunk('checkOut/fetch_entity_list', async ({ page, size, sort, status }: TCheckOutParams) => {
  const requestUrl = `${apiUrl}${
    sort ? `?status=${status}&page=${page}&size=${size}&sort=${sort}&` : '?'
  }cacheBuster=${new Date().getTime()}`;
  return axios.get<ICheckOut[]>(requestUrl);
});

export const getEntitiesClient = createAsyncThunk(
  'checkOut/fetch_entity_list',
  async ({ status, cardNumber, returned }: TCheckOutParams) => {
    const requestUrl = `${apiUrlClient}?returned=${returned}&cardNumber=${cardNumber}&status=${status}&cacheBuster=${new Date().getTime()}`;
    return axios.get<ICheckOut[]>(requestUrl);
  }
);

export const getEntity = createAsyncThunk(
  'checkOut/fetch_entity',
  async (id: string | number) => {
    const requestUrl = `${apiUrl}/${id}`;
    return axios.get<ICheckOut>(requestUrl);
  },
  { serializeError: serializeAxiosError }
);

export const createEntity = createAsyncThunk(
  'checkOut/create_entity',
  async (entity: ICheckOut, thunkAPI) => {
    const result = await axios.post<ICheckOut>(apiUrl, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const updateEntity = createAsyncThunk(
  'checkOut/update_entity',
  async (entity: ICheckOut, thunkAPI) => {
    const result = await axios.put<ICheckOut>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const partialUpdateEntity = createAsyncThunk(
  'checkOut/partial_update_entity',
  async (entity: ICheckOut, thunkAPI) => {
    const result = await axios.patch<ICheckOut>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const deleteEntity = createAsyncThunk(
  'checkOut/delete_entity',
  async (id: string | number, thunkAPI) => {
    const requestUrl = `${apiUrl}/${id}`;
    const result = await axios.delete<ICheckOut>(requestUrl);
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError }
);

// slice

export const CheckOutSlice = createEntitySlice({
  name: 'checkOut',
  initialState,
  extraReducers(builder) {
    builder
      .addCase(getEntity.fulfilled, (state, action) => {
        state.loading = false;
        state.entity = action.payload.data;
      })
      .addCase(deleteEntity.fulfilled, state => {
        state.updating = false;
        state.updateSuccess = true;
        state.entity = {};
      })
      .addMatcher(isFulfilled(getEntities, searchEntities, getEntitiesClient), (state, action) => {
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
      .addMatcher(isPending(getEntities, getEntity, searchEntities, getEntitiesClient), state => {
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

export const { reset } = CheckOutSlice.actions;

// Reducer
export default CheckOutSlice.reducer;
