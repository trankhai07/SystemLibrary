import { createAsyncThunk } from '@reduxjs/toolkit';
import axios from 'axios';
import { toast } from 'react-toastify';

export interface ResponseS3Img {
  imgUrl: string;
}
interface IRequest {
  file: File;
}
const apiUrl1 = 'api/images/upload';
export const uploadSingleFileS3 = createAsyncThunk('product/uploadSingleFile', async ({ file }: IRequest) => {
  try {
    const formData = new FormData();
    formData.append('file', file);
    const response = await axios.post(apiUrl1, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return { imgUrl: response?.data } as ResponseS3Img;
  } catch (error) {
    toast.error('upload fail');
    console.error('error', error);
  }
});
