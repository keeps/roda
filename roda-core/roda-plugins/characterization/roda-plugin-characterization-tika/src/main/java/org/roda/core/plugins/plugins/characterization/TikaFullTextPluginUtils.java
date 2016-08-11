/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.jdom2.Element;
import org.jdom2.IllegalDataException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.roda.core.common.IdUtils;
import org.roda.core.common.MetadataFileUtils;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.ProvidesInputStream;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
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
    AuthorizationDeniedException, ValidationException, IOException {

    boolean recursive = true;
    CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(), representation.getId(),
      recursive);

    boolean notify = true; // need to index tika properties...

    for (OptionalWithCause<File> oFile : allFiles) {
      if (oFile.isPresent()) {
        File file = oFile.get();
        if (!file.isDirectory() && (doFeatureExtraction || doFulltextExtraction)) {
          StoragePath storagePath = ModelUtils.getFileStoragePath(file);
          Binary binary = model.getStorage().getBinary(storagePath);

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
                RodaConstants.TIKA_FILE_SUFFIX_FULLTEXT, RodaConstants.OTHER_METADATA_TYPE_APACHE_TIKA, payload,
                notify);
            }

          } catch (Exception e) {
            throw e;
          } finally {
            IOUtils.closeQuietly(inputStream);
          }

          try {
            if (doFeatureExtraction && metadata != null && metadata.size() > 0) {
              String metadataAsString = generateMetadataFile(metadata);
              ContentPayload metadataAsPayload = new StringContentPayload(metadataAsString);
              model.createOtherMetadata(aip.getId(), representation.getId(), file.getPath(), file.getId(),
                RodaConstants.TIKA_FILE_SUFFIX_METADATA, RodaConstants.OTHER_METADATA_TYPE_APACHE_TIKA,
                metadataAsPayload, notify);

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

                PreservationMetadataType type = PreservationMetadataType.FILE;
                String id = IdUtils.getPreservationId(type, aip.getId(), representation.getId(), file.getPath(),
                  file.getId());

                ContentPayload premisFilePayload = PremisV3Utils.fileToBinary(premisFile);

                model.updatePreservationMetadata(id, type, aip.getId(), representation.getId(), file.getPath(),
                  file.getId(), premisFilePayload, notify);
              }
            }
          } catch (Exception e) {
            throw e;
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

  private static String generateMetadataFile(Metadata metadata) throws IOException {
    try {
      String[] names = metadata.names();
      Element root = new Element("metadata");
      org.jdom2.Document doc = new org.jdom2.Document();

      for (String name : names) {
        String[] values = metadata.getValues(name);
        if (values != null && values.length > 0) {
          for (String value : values) {
            Element child = new Element("field");
            child.setAttribute("name", MetadataFileUtils.escapeAttribute(name));
            child.addContent(MetadataFileUtils.escapeContent(value));
            root.addContent(child);
          }

        }
      }
      doc.setRootElement(root);
      XMLOutputter outter = new XMLOutputter();
      outter.setFormat(Format.getPrettyFormat());
      outter.outputString(doc);
      return outter.outputString(doc);
    } catch (IllegalDataException e) {
      LOGGER.debug("Error generating Tika metadata file {}", e.getMessage());
      return "";
    }
  }

}
