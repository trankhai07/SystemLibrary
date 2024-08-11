export function getFileName(url: string) {
  const nameFile = url.split('/');

  return nameFile[nameFile.length - 1];
}

export function handleDownloadFile(url: string | null, fileName: string) {
  if (!url) throw new Error('No url found');

  const link = document.createElement('a');
  link.href = url;
  link.download = fileName as string;

  document.body.appendChild(link);

  link.click();

  link.parentNode?.removeChild(link);
  URL.revokeObjectURL(url);
}
