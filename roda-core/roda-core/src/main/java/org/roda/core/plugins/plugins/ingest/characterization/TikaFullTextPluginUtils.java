/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.roda.core.common.PremisUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IdUtils;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.InputStreamContentPayload;
import org.roda.core.storage.InputStreamContentPayload.ProvidesInputStream;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TikaFullTextPluginUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(TikaFullTextPluginUtils.class);

  private static final Tika tika = new Tika();

  public static void runTikaFullTextOnRepresentation(IndexService index, ModelService model, StorageService storage,
    AIP aip, Representation representation, boolean notify) throws NotFoundException, GenericException,
      RequestNotValidException, AuthorizationDeniedException, ValidationException {

    boolean recursive = true;
    CloseableIterable<File> allFiles = model.listFilesUnder(aip.getId(), representation.getId(), recursive);

    boolean inotify = false;
    for (File file : allFiles) {

      if (!file.isDirectory()) {
        StoragePath storagePath = ModelUtils.getFileStoragePath(file);
        Binary binary = storage.getBinary(storagePath);

        Metadata metadata = new Metadata();
        InputStream inputStream = null;
        try {
          inputStream = binary.getContent().createInputStream();
          final Reader reader = tika.parse(inputStream, metadata);

          ContentPayload payload = new InputStreamContentPayload(new ProvidesInputStream() {

            @Override
            public InputStream createInputStream() throws IOException {
              return new ReaderInputStream(reader);
            }
          });
          model.createOtherMetadata(aip.getId(), representation.getId(), file.getPath(), file.getId(),
            TikaFullTextPlugin.FILE_SUFFIX, TikaFullTextPlugin.OTHER_METADATA_TYPE, payload, inotify);
        } catch (IOException e) {
          LOGGER.error("Error running Apache Tika", e);
        } finally {
          IOUtils.closeQuietly(inputStream);
        }

        // update PREMIS
        String creatingApplicationName = metadata.get("Application-Name");
        String creatingApplicationVersion = metadata.get("Application-Version");
        String dateCreatedByApplication = metadata.get("Creation-Date");

        Binary premis_bin = model.retrievePreservationFile(file);

        lc.xmlns.premisV2.File premis_file = PremisUtils.binaryToFile(premis_bin.getContent(), false);
        PremisUtils.updateCreatingApplication(premis_file, creatingApplicationName, creatingApplicationVersion,
          dateCreatedByApplication);

        PreservationMetadataType type = PreservationMetadataType.OBJECT_FILE;
        String id = IdUtils.getPreservationMetadataId(type, aip.getId(), representation.getId(), file.getPath(),
          file.getId());

        ContentPayload premis_file_payload = PremisUtils.fileToBinary(premis_file);

        model.updatePreservationMetadata(id, type, aip.getId(), representation.getId(), file.getPath(), file.getId(),
          premis_file_payload, inotify);

      }
    }
    IOUtils.closeQuietly(allFiles);
    if (notify) {
      model.notifyAIPUpdated(aip.getId());
    }
  }
}
