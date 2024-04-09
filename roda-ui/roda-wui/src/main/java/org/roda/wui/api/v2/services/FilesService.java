package org.roda.wui.api.v2.services;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.DownloadUtils;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.EntityResponse;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryConsumesOutputStream;
import org.roda.core.storage.Directory;
import org.roda.core.storage.StorageService;
import org.springframework.stereotype.Service;

@Service
public class FilesService {

  public StreamResponse retrieveAIPRepresentationFile(IndexedFile indexedFile)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    StoragePath filePath = ModelUtils.getFileStoragePath(indexedFile.getAipId(), indexedFile.getRepresentationId(),
      indexedFile.getPath(), indexedFile.getId());

    if (!indexedFile.isDirectory()) {
      final ConsumesOutputStream stream;
      StorageService storage = RodaCoreFactory.getStorageService();
      Binary representationFileBinary = storage.getBinary(filePath);
      if (indexedFile.getFileFormat() != null && StringUtils.isNotBlank(indexedFile.getFileFormat().getMimeType())) {
        stream = new BinaryConsumesOutputStream(representationFileBinary, indexedFile.getFileFormat().getMimeType());
      } else {
        stream = new BinaryConsumesOutputStream(representationFileBinary);
      }
      return new StreamResponse(stream);
    } else {
      Directory directory = RodaCoreFactory.getStorageService().getDirectory(filePath);
      ConsumesOutputStream download = DownloadUtils.download(RodaCoreFactory.getStorageService(), directory, null);
      return new StreamResponse(download);
    }
  }
}
