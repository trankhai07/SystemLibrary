import axios from 'axios';
import { createAsyncThunk, isFulfilled, isPending, isRejected } from '@reduxjs/toolkit';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { IQueryParams, createEntitySlice, EntityState, serializeAxiosError } from 'app/shared/reducers/reducer.utils';
import { IPatronAccount, defaultValue } from 'app/shared/model/patron-account.model';

const initialState: EntityState<IPatronAccount> = {
  loading: false,
  errorMessage: null,
  entities: [],
  entity: defaultValue,
  updating: false,
  totalItems: 0,
  updateSuccess: false,
};

const apiUrl = 'api/patron-accounts';
const apiSearchUrl = 'api/_search/patron-accounts';
const apiNotEnoughUrl = 'api/patron-accounts/not-enough-condition';

// Actions

export const searchEntities = createAsyncThunk('patronAccount/search_entity', async ({ query, page, size, sort }: IQueryParams) => {
  const requestUrl = `${apiSearchUrl}?query=${query}${sort ? `&page=${page}&size=${size}&sort=${sort}` : ''}`;
  return axios.get<IPatronAccount[]>(requestUrl);
});

export const getEntities = createAsyncThunk('patronAccount/fetch_entity_list', async ({ page, size, sort }: IQueryParams) => {
  const requestUrl = `${apiUrl}${sort ? `?page=${page}&size=${size}&sort=${sort}&` : '?'}cacheBuster=${new Date().getTime()}`;
  return axios.get<IPatronAccount[]>(requestUrl);
});
export const getEntitiesNotEnough = createAsyncThunk('patronAccount/fetch_entity_list_not_enough', async ({ page, size }: IQueryParams) => {
  const requestUrl = `${apiNotEnoughUrl}${`?page=${page}&size=${size}&`}cacheBuster=${new Date().getTime()}`;
  return axios.get<IPatronAccount[]>(requestUrl);
});

export const getEntity = createAsyncThunk(
  'patronAccount/fetch_entity',
  async (id: string | number) => {
    const requestUrl = `${apiUrl}/${id}`;
    return axios.get<IPatronAccount>(requestUrl);
  },
  { serializeError: serializeAxiosError }
);

export const getEntityByUser = createAsyncThunk(
  'patronAccount/fetch_entity',
  async () => {
    const requestUrl = `${apiUrl}/user`;
    return axios.get<IPatronAccount>(requestUrl);
  },
  { serializeError: serializeAxiosError }
);
export const createEntity = createAsyncThunk(
  'patronAccount/create_entity',
  async (entity: IPatronAccount, thunkAPI) => {
    const result = await axios.post<IPatronAccount>(apiUrl, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const updateEntity = createAsyncThunk(
  'patronAccount/update_entity',
  async (entity: IPatronAccount, thunkAPI) => {
    const result = await axios.put<IPatronAccount>(`${apiUrl}/${entity.cardNumber}`, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const updateEntityUserStatus = createAsyncThunk(
  'patronAccount/update_entity_user_status',
  async (params: { entity: IPatronAccount; statusUser: boolean; cardNumber: string }, thunkAPI) => {
    const result = await axios.put<IPatronAccount>(
      `${apiUrl}-status/${params.cardNumber}?activated=${params.statusUser}`,
      cleanEntity(params.entity)
    );
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const partialUpdateEntity = createAsyncThunk(
  'patronAccount/partial_update_entity',
  async (entity: IPatronAccount, thunkAPI) => {
    const result = await axios.patch<IPatronAccount>(`${apiUrl}/${entity.cardNumber}`, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError }
);

export const deleteEntity = createAsyncThunk(
  'patronAccount/delete_entity',
  async (id: string | number, thunkAPI) => {
    const requestUrl = `${apiUrl}/${id}`;
    const result = await axios.delete<IPatronAccount>(requestUrl);
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError }
);

// slice

export const PatronAccountSlice = createEntitySlice({
  name: 'patronAccount',
  initialState,
  extraReducers(builder) {
    builder
      .addCase(deleteEntity.fulfilled, state => {
        state.updating = false;
        state.updateSuccess = true;
        state.entity = {};
      })
      .addMatcher(isFulfilled(getEntityByUser, getEntity), (state, action) => {
        state.loading = false;
        state.entity = action.payload.data;
      })
      .addMatcher(isFulfilled(getEntities, searchEntities, getEntitiesNotEnough), (state, action) => {
        const { data, headers } = action.payload;

        return {
          ...state,
          loading: false,
          entities: data,
          totalItems: parseInt(headers['x-total-count'], 10),
        };
      })
      .addMatcher(isFulfilled(createEntity, updateEntity, updateEntityUserStatus, partialUpdateEntity), (state, action) => {
        state.updating = false;
        state.loading = false;
        state.updateSuccess = true;
        state.entity = action.payload.data;
      })
      .addMatcher(isPending(getEntities, getEntity, searchEntities, getEntitiesNotEnough), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      })
      .addMatcher(isPending(createEntity, updateEntity, partialUpdateEntity, updateEntityUserStatus, deleteEntity), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.updating = true;
      });
  },
});

export const { reset } = PatronAccountSlice.actions;

// Reducer
export default PatronAccountSlice.reducer;
