/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model.utils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.iterables.CloseableIterables;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.storage.DefaultBinary;
import org.roda.core.storage.DefaultDirectory;
import org.roda.core.storage.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceParseUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceParseUtils.class);

  private ResourceParseUtils() {

  }

  private static File convertResourceToFile(Resource resource)
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

    om.setAipId(aipId);
    om.setRepresentationId(representationId);
    om.setFileDirectoryPath(fileDirectoryPath);
    om.setFileId(fileId);
    om.setType(type);

    return om;
  }

  public static <T extends Serializable> T convertResourceTo(Resource resource, Class<T> classToReturn)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    T ret;
    if (classToReturn.equals(File.class)) {
      ret = classToReturn.cast(convertResourceToFile(resource));
    } else if (classToReturn.equals(PreservationMetadata.class)) {
      ret = classToReturn.cast(convertResourceToPreservationMetadata(resource));
    } else if (classToReturn.equals(OtherMetadata.class)) {
      ret = classToReturn.cast(convertResourceToOtherMetadata(resource));
    } else {
      throw new RequestNotValidException("Cannot convert a resource to " + classToReturn.getName());
    }

    return ret;
  }

  private static <T extends Serializable> boolean isDirectoryAcceptable(Class<T> classToReturn) {
    return classToReturn.equals(File.class);
  }

  public static <T extends Serializable> CloseableIterable<T> convert(final CloseableIterable<Resource> iterable,
    final Class<T> classToReturn) {

    final CloseableIterable<Resource> filtered;
    if (isDirectoryAcceptable(classToReturn)) {
      filtered = iterable;
    } else {
      filtered = CloseableIterables.filter(iterable, p -> !p.isDirectory());
    }

    CloseableIterable<T> it = null;

    final Iterator<Resource> iterator = filtered.iterator();
    it = new CloseableIterable<T>() {

      @Override
      public Iterator<T> iterator() {
        return new Iterator<T>() {

          @Override
          public boolean hasNext() {
            if (iterator == null) {
              return true;
            }
            return iterator.hasNext();
          }

          @Override
          public T next() {
            try {
              return ResourceParseUtils.convertResourceTo(iterator.next(), classToReturn);
            } catch (GenericException | NoSuchElementException | NotFoundException | AuthorizationDeniedException
              | RequestNotValidException e) {
              LOGGER.error("Error converting into " + classToReturn.getName(), e);
              return null;
            }
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
