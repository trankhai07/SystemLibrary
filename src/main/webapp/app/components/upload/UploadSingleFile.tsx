import { CloseOutlined, DeleteOutlined, DownloadOutlined, EditOutlined, PictureOutlined } from '@ant-design/icons';
import { Button, Upload } from 'antd';
import { UploadFile } from 'antd/es/upload/interface';
import { getFileName, handleDownloadFile } from 'app/utils/helpers/func';
import React, { Dispatch, SetStateAction, useEffect, useState } from 'react';
import './style.scss';
export interface IMedia {
  image?: UploadFile;
}
interface IUploadSingleFile {
  media: IMedia;
  isSingleImage: boolean;
  isFile?: boolean;
  setImage: Dispatch<SetStateAction<IMedia | undefined>>;
  style?: React.CSSProperties | undefined;
  url?: string;
  isNew: boolean;
  accept: string;
}

const UploadSingleFile = ({ setImage, isSingleImage, isFile, media, style, url, isNew, accept }: IUploadSingleFile) => {
  const [preview, setPreview] = useState<UploadFile<any>[]>([] as UploadFile<any>[]);
  const [isEdit, setShowEdit] = useState(false);
  const [showFile, setShowFile] = useState(true);

  useEffect(() => {
    if (url) {
      setShowEdit(false);
    } else {
      setShowEdit(true);
    }
  }, [url]);

  useEffect(() => {
    handlePreviewListFile();
  }, [media]);

  const handlePreviewListFile = () => {
    if (media?.image && isSingleImage) {
      setPreview([media.image]);
    }
  };

  const handleChange = async info => {
    if (info.fileList.length > 0 && isSingleImage) {
      setImage && setImage({ ...media, image: info.fileList[0] });
    }
  };

  const handleRemove = () => {
    if (media?.image && isSingleImage) {
      setImage({ ...media, image: undefined });
    }
  };

  const getIcon = () => {
    return <PictureOutlined />;
  };

  return (
    <div className="single_file mb-3" style={style}>
      {isEdit || isFile ? (
        <div className="d-flex flex-column">
          <div className="wrapper_edit">
            <Upload
              accept={accept}
              showUploadList={true}
              name="audio"
              onRemove={handleRemove}
              onChange={info => handleChange(info)}
              multiple={false}
              maxCount={1}
              className={`wrapper_button ${isEdit ? 'mx-0' : ''}`}
            >
              <div className="button_upload">
                <Button icon={getIcon()}>{isSingleImage && 'Upload Image'}</Button>
              </div>
              <div className="wrapper_img">
                {media?.image?.originFileObj?.type.includes('image/') && isSingleImage && (
                  <img
                    className="mt-3"
                    style={{ height: '300px' }}
                    src={
                      media?.image?.originFileObj?.type.includes('image/')
                        ? URL.createObjectURL(media?.image?.originFileObj as unknown as File)
                        : (url as string)
                    }
                  />
                )}
              </div>
            </Upload>
            {url && !isFile && (
              <div className="wrapper_edit">
                <CloseOutlined onClick={() => setShowEdit(false)} />
              </div>
            )}
          </div>
          {url && showFile && isFile && (
            <div className="d-flex gap-3">
              <div>{getFileName(url)}</div>
              <DownloadOutlined onClick={() => handleDownloadFile(url, getFileName(url))} />
              <DeleteOutlined
                onClick={() => {
                  setShowFile(false);
                }}
                style={{
                  color: 'red',
                }}
              />
            </div>
          )}
        </div>
      ) : (
        <>
          {isSingleImage && !isNew && url && (
            <div className="wrapper_edit">
              <img className="mt-3" style={{ height: '400px' }} src={url} />
              {!isEdit && <EditOutlined onClick={() => setShowEdit(true)} />}
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default UploadSingleFile;
