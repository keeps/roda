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
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.roda.core.common.IdUtils;
import org.roda.core.common.MetadataFileUtils;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.InputStreamContentPayload;
import org.roda.core.storage.InputStreamContentPayload.ProvidesInputStream;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TikaFullTextPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(TikaFullTextPluginUtils.class);

  private static final Tika tika = new Tika();

  public static Report runTikaFullTextOnRepresentation(Report reportItem, IndexService index, ModelService model,
    StorageService storage, AIP aip, Representation representation, boolean doFeatureExtraction,
    boolean doFulltextExtraction) throws NotFoundException, GenericException, RequestNotValidException,
    AuthorizationDeniedException, ValidationException {

    boolean recursive = true;
    CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(), representation.getId(),
      recursive);

    boolean notify = true; // need to index tika properties...

    for (OptionalWithCause<File> oFile : allFiles) {
      if (oFile.isPresent()) {
        File file = oFile.get();
        if (!file.isDirectory() && (doFeatureExtraction || doFulltextExtraction)) {
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
                return new ReaderInputStream(reader, RodaConstants.DEFAULT_ENCODING);
              }
            });

            if (doFulltextExtraction) {
              model.createOtherMetadata(aip.getId(), representation.getId(), file.getPath(), file.getId(),
                TikaFullTextPlugin.FILE_SUFFIX_FULLTEXT, TikaFullTextPlugin.OTHER_METADATA_TYPE, payload, notify);
            }

          } catch (IOException | RODAException e) {
            if (reportItem != null) {
              StringWriter sw = new StringWriter();
              PrintWriter pw = new PrintWriter(sw);
              e.printStackTrace(pw);
              String details = reportItem.getPluginDetails();
              if (details == null) {
                details = "";
              }
              details += sw.toString();
              reportItem.setPluginDetails(details);
              pw.close();
            } else {
              LOGGER.error("Error running Apache Tika", e);
            }
          } finally {
            IOUtils.closeQuietly(inputStream);
          }

          try {
            if (doFeatureExtraction && metadata != null && metadata.size() > 0) {
              String metadataAsString = MetadataFileUtils.generateMetadataFile(metadata);
              ContentPayload metadataAsPayload = new StringContentPayload(metadataAsString);
              model.createOtherMetadata(aip.getId(), representation.getId(), file.getPath(), file.getId(),
                TikaFullTextPlugin.FILE_SUFFIX_METADATA, TikaFullTextPlugin.OTHER_METADATA_TYPE, metadataAsPayload,
                notify);

              // update PREMIS
              String creatingApplicationName = metadata.get("Application-Name");
              String creatingApplicationVersion = metadata.get("Application-Version");
              String dateCreatedByApplication = metadata.get("Creation-Date");

              if (StringUtils.isNotBlank(creatingApplicationName) || StringUtils.isNotBlank(creatingApplicationVersion)
                || StringUtils.isNotBlank(dateCreatedByApplication)) {
                Binary premisBin = model.retrievePreservationFile(file);

                gov.loc.premis.v3.File premisFile = PremisV3Utils.binaryToFile(premisBin.getContent(), false);
                PremisV3Utils.updateCreatingApplication(premisFile, creatingApplicationName, creatingApplicationVersion,
                  dateCreatedByApplication);

                PreservationMetadataType type = PreservationMetadataType.OBJECT_FILE;
                String id = IdUtils.getPreservationMetadataId(type, aip.getId(), representation.getId(), file.getPath(),
                  file.getId());

                ContentPayload premisFilePayload = PremisV3Utils.fileToBinary(premisFile);

                model.updatePreservationMetadata(id, type, aip.getId(), representation.getId(), file.getPath(),
                  file.getId(), premisFilePayload, notify);
              }
            }
          } catch (IOException e) {
            LOGGER.error("Error running Apache Tika", e);
          } finally {
            IOUtils.closeQuietly(inputStream);
          }
        }
      } else {
        LOGGER.error("Cannot process File", oFile.getCause());
      }
    }
    IOUtils.closeQuietly(allFiles);
    return reportItem;
  }

}
