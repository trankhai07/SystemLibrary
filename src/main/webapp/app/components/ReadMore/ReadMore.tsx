import React from 'react';
import { useState } from 'react';
import { translate } from 'react-jhipster';

const contentStyle: React.CSSProperties = {
  minHeight: '200px',
  margin: 0,
  textAlign: 'start',
  width: '700px',
  padding: '10px',
  fontSize: '16px',
};

const ReadMore = ({ text = '', maxLength = 600 }) => {
  const [isTruncated, setIsTruncated] = useState(true);
  const toggleTruncate = () => {
    setIsTruncated(!isTruncated);
  };

  const displayText = isTruncated && text.length > maxLength ? text.slice(0, maxLength) + '...' : text;

  return (
    // <div className="readMore" style={contentStyle}>
    //   <span
    //     dangerouslySetInnerHTML={{
    //       __html:
    //         displayText
    //           ?.replace(/https?:\/\/\S+/gi, match => `<a href="${match}" target="_blank" class="text-primary underline">${match}</a>`)
    //           .replaceAll(`\n`, '<br/>') ?? ''
    //     }}
    //     className={`break-all body-md`}
    //   ></span>
    //   {text.length > maxLength && (
    //     <span style={{ color: 'blue', cursor: 'pointer', marginLeft: '4px' }} onClick={toggleTruncate}>
    //       {isTruncated ? translate('global.button.seeMore') : translate('global.button.seeLess')}
    //     </span>
    //   )}
    // </div>
    <></>
  );
};

export default ReadMore;
