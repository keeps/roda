/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.iterables.CloseableIterables;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.storage.Binary;
import org.roda.core.storage.DefaultBinary;
import org.roda.core.storage.DefaultDirectory;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceParseUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceParseUtils.class);

  private ResourceParseUtils() {

  }

  public static File convertResourceToFile(Resource resource)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    File ret;

    if (resource == null) {
      throw new RequestNotValidException("Resource cannot be null");
    }

    StoragePath resourcePath = resource.getStoragePath();

    String id = resourcePath.getName();
    String aipId = ModelUtils.extractAipId(resourcePath);
    String representationId = ModelUtils.extractRepresentationId(resourcePath);
    List<String> filePath = ModelUtils.extractFilePathFromRepresentationData(resourcePath);

    if (resource instanceof DefaultBinary) {
      boolean isDirectory = false;

      ret = new File(id, aipId, representationId, filePath, isDirectory);
    } else if (resource instanceof DefaultDirectory) {
      boolean isDirectory = true;

      ret = new File(resourcePath.getName(), aipId, representationId, filePath, isDirectory);
    } else {
      throw new GenericException(
        "Error while trying to convert something that it isn't a Binary into a representation file");
    }
    return ret;
  }

  private static PreservationMetadata convertResourceToPreservationMetadata(Resource resource)
    throws RequestNotValidException {

    if (resource == null) {
      throw new RequestNotValidException("Resource cannot be null");
    }

    StoragePath resourcePath = resource.getStoragePath();

    String filename = resourcePath.getName();

    PreservationMetadata pm = new PreservationMetadata();

    String id;
    String aipId = ModelUtils.extractAipId(resourcePath);
    String representationId = ModelUtils.extractRepresentationId(resourcePath);
    List<String> fileDirectoryPath = null;
    String fileId = null;

    PreservationMetadataType type;
    if (filename.endsWith(RodaConstants.PREMIS_AGENT_SUFFIX)) {
      id = filename.substring(0, filename.length() - RodaConstants.PREMIS_AGENT_SUFFIX.length());
      type = PreservationMetadataType.AGENT;
      aipId = null;
      representationId = null;
    } else if (filename.endsWith(RodaConstants.PREMIS_EVENT_SUFFIX)) {
      id = filename.substring(0, filename.length() - RodaConstants.PREMIS_EVENT_SUFFIX.length());
      type = PreservationMetadataType.EVENT;
    } else if (filename.endsWith(RodaConstants.PREMIS_FILE_SUFFIX)) {
      type = PreservationMetadataType.OBJECT_FILE;
      fileDirectoryPath = ModelUtils.extractFilePathFromRepresentationPreservationMetadata(resourcePath);
      fileId = filename.substring(0, filename.length() - RodaConstants.PREMIS_FILE_SUFFIX.length());
      id = fileId;
    } else if (filename.endsWith(RodaConstants.OTHER_TECH_METADATA_FILE_SUFFIX)) {
      type = PreservationMetadataType.OTHER;
      fileDirectoryPath = ModelUtils.extractFilePathFromRepresentationPreservationMetadata(resourcePath);
      fileId = filename.substring(0, filename.length() - RodaConstants.OTHER_TECH_METADATA_FILE_SUFFIX.length());
      id = fileId;
    } else if (filename.endsWith(RodaConstants.PREMIS_REPRESENTATION_SUFFIX)) {
      id = filename.substring(0, filename.length() - RodaConstants.PREMIS_REPRESENTATION_SUFFIX.length());
      type = PreservationMetadataType.OBJECT_REPRESENTATION;
    } else {
      throw new RequestNotValidException("Unsupported PREMIS extension type in file: " + filename);
    }

    pm.setId(id);
    pm.setAipId(aipId);
    pm.setRepresentationId(representationId);
    pm.setFileDirectoryPath(fileDirectoryPath);
    pm.setFileId(fileId);
    pm.setType(type);

    return pm;
  }

  private static OtherMetadata convertResourceToOtherMetadata(Resource resource) throws RequestNotValidException {

    if (resource == null) {
      throw new RequestNotValidException("Resource cannot be null");
    }

    StoragePath resourcePath = resource.getStoragePath();

    String filename = resourcePath.getName();

    OtherMetadata om = new OtherMetadata();

    String aipId = ModelUtils.extractAipId(resourcePath);
    String representationId = ModelUtils.extractRepresentationId(resourcePath);
    String type = representationId != null ? ModelUtils.extractTypeFromRepresentationOtherMetadata(resourcePath)
      : ModelUtils.extractTypeFromAipOtherMetadata(resourcePath);
    List<String> fileDirectoryPath = representationId != null
      ? ModelUtils.extractFilePathFromRepresentationOtherMetadata(resourcePath)
      : ModelUtils.extractFilePathFromAipOtherMetadata(resourcePath);
    String fileId = filename.substring(0, filename.lastIndexOf('.'));
    String suffix = filename.substring(filename.lastIndexOf('.'), filename.length());

    om.setAipId(aipId);
    om.setRepresentationId(representationId);
    om.setFileDirectoryPath(fileDirectoryPath);
    om.setFileId(fileId);
    om.setType(type);
    om.setFileSuffix(suffix);
    return om;
  }

  public static <T extends Serializable> OptionalWithCause<T> convertResourceTo(StorageService storage,
    Resource resource, Class<T> classToReturn) {
    OptionalWithCause<T> ret;

    try {
      if (classToReturn.equals(AIP.class)) {
        ret = OptionalWithCause.of(classToReturn.cast(getAIPMetadata(storage, resource.getStoragePath())));
      } else if (classToReturn.equals(File.class)) {
        ret = OptionalWithCause.of(classToReturn.cast(convertResourceToFile(resource)));
      } else if (classToReturn.equals(PreservationMetadata.class)) {
        ret = OptionalWithCause.of(classToReturn.cast(convertResourceToPreservationMetadata(resource)));
      } else if (classToReturn.equals(OtherMetadata.class)) {
        ret = OptionalWithCause.of(classToReturn.cast(convertResourceToOtherMetadata(resource)));
      } else {
        ret = OptionalWithCause
          .empty(new RequestNotValidException("Cannot convert a resource to " + classToReturn.getName()));
      }
    } catch (RODAException e) {
      ret = OptionalWithCause.empty(e);
    }

    return ret;
  }

  public static AIP getAIPMetadata(StorageService storage, String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getAIPMetadata(storage, aipId, ModelUtils.getAIPStoragePath(aipId));
  }

  public static AIP getAIPMetadata(StorageService storage, StoragePath storagePath)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getAIPMetadata(storage, storagePath.getName(), storagePath);
  }

  public static AIP getAIPMetadata(StorageService storage, String aipId, StoragePath storagePath)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    DefaultStoragePath metadataStoragePath = DefaultStoragePath.parse(storagePath,
      RodaConstants.STORAGE_AIP_METADATA_FILENAME);
    Binary binary = storage.getBinary(metadataStoragePath);

    String json;
    AIP aip;
    InputStream inputStream = null;
    try {
      inputStream = binary.getContent().createInputStream();
      json = IOUtils.toString(inputStream);
      aip = JsonUtils.getObjectFromJson(json, AIP.class);
    } catch (IOException | GenericException e) {
      throw new GenericException("Could not parse AIP metadata of " + aipId + " at " + metadataStoragePath, e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }

    // Setting information that does not come in JSON
    aip.setId(aipId);

    return aip;
  }

  private static <T extends Serializable> boolean isDirectoryAcceptable(Class<T> classToReturn) {
    return classToReturn.equals(File.class) || classToReturn.equals(AIP.class);
  }

  public static <T extends Serializable> CloseableIterable<OptionalWithCause<T>> convert(final StorageService storage,
    final CloseableIterable<Resource> iterable, final Class<T> classToReturn) {

    final CloseableIterable<Resource> filtered;
    if (isDirectoryAcceptable(classToReturn)) {
      filtered = iterable;
    } else {
      filtered = CloseableIterables.filter(iterable, p -> !p.isDirectory());
    }

    CloseableIterable<OptionalWithCause<T>> it = null;

    final Iterator<Resource> iterator = filtered.iterator();
    it = new CloseableIterable<OptionalWithCause<T>>() {

      @Override
      public Iterator<OptionalWithCause<T>> iterator() {
        return new Iterator<OptionalWithCause<T>>() {

          @Override
          public boolean hasNext() {
            if (iterator == null) {
              return true;
            }
            return iterator.hasNext();
          }

          @Override
          public OptionalWithCause<T> next() {
            return ResourceParseUtils.convertResourceTo(storage, iterator.next(), classToReturn);
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }

      @Override
      public void close() throws IOException {
        filtered.close();
      }
    };

    return it;
  }

}
