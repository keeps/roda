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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.iterables.CloseableIterables;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.utils.URNUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationMetadata;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.storage.Binary;
import org.roda.core.storage.DefaultBinary;
import org.roda.core.storage.DefaultDirectory;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceParseUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceParseUtils.class);
  private static final String RESOURCE_CANNOT_BE_NULL = "Resource cannot be null";

  private ResourceParseUtils() {
    // do nothing
  }

  public static File convertResourceToFile(Resource resource) throws GenericException, RequestNotValidException {
    if (resource == null) {
      throw new RequestNotValidException(RESOURCE_CANNOT_BE_NULL);
    }

    StoragePath resourcePath = resource.getStoragePath();

    String id = resourcePath.getName();
    String aipId = ModelUtils.extractAipId(resourcePath).orElse(null);
    String representationId = ModelUtils.extractRepresentationId(resourcePath).orElse(null);
    List<String> filePath = ModelUtils.extractFilePathFromRepresentationData(resourcePath);

    if (resource instanceof DefaultBinary) {
      return new File(id, aipId, representationId, filePath, false);
    } else if (resource instanceof DefaultDirectory) {
      return new File(id, aipId, representationId, filePath, true);
    } else {
      throw new GenericException(
        "Error while trying to convert something that it isn't a Binary into a representation file");
    }
  }

  public static DIPFile convertResourceToDIPFile(Resource resource) throws GenericException, RequestNotValidException {
    DIPFile ret;

    if (resource == null) {
      throw new RequestNotValidException(RESOURCE_CANNOT_BE_NULL);
    }

    StoragePath resourcePath = resource.getStoragePath();
    String id = resourcePath.getName();
    String dipId = ModelUtils.extractDipId(resourcePath).orElse(null);
    List<String> filePath = ModelUtils.extractFilePathFromDIPData(resourcePath);

    if (resource instanceof DefaultBinary) {
      boolean isDirectory = false;
      ret = new DIPFile(id, dipId, filePath, isDirectory);
    } else if (resource instanceof DefaultDirectory) {
      boolean isDirectory = true;
      ret = new DIPFile(id, dipId, filePath, isDirectory);
    } else {
      throw new GenericException(
        "Error while trying to convert something that is not a Binary or Directory into a DIP file");
    }

    return ret;
  }

  private static PreservationMetadata convertResourceToPreservationMetadata(Resource resource)
    throws RequestNotValidException {

    if (resource == null) {
      throw new RequestNotValidException(RESOURCE_CANNOT_BE_NULL);
    }

    StoragePath resourcePath = resource.getStoragePath();
    String filename = resourcePath.getName();
    PreservationMetadata pm = new PreservationMetadata();

    String id;
    String aipId = ModelUtils.extractAipId(resourcePath).orElse(null);
    String representationId = ModelUtils.extractRepresentationId(resourcePath).orElse(null);
    List<String> fileDirectoryPath = null;
    String fileId = null;

    PreservationMetadataType type;
    if (filename.startsWith(URNUtils.getPremisPrefix(PreservationMetadataType.AGENT))) {
      id = filename.substring(0, filename.length() - RodaConstants.PREMIS_SUFFIX.length());
      type = PreservationMetadataType.AGENT;
      aipId = null;
      representationId = null;
    } else if (filename.startsWith(URNUtils.getPremisPrefix(PreservationMetadataType.EVENT))) {
      id = filename.substring(0, filename.length() - RodaConstants.PREMIS_SUFFIX.length());
      type = PreservationMetadataType.EVENT;
      try {
        String separator = URLEncoder.encode(RodaConstants.URN_SEPARATOR, RodaConstants.DEFAULT_ENCODING);
        if (StringUtils.countMatches(id, separator) > 0) {
          fileDirectoryPath = ModelUtils.extractFilePathFromRepresentationPreservationMetadata(resourcePath);
          fileId = id.substring(id.lastIndexOf(separator) + 1);
        }
      } catch (UnsupportedEncodingException e) {
        LOGGER.error("Error encoding urn separator when converting file event preservation metadata");
      }
    } else if (filename.startsWith(URNUtils.getPremisPrefix(PreservationMetadataType.FILE))) {
      type = PreservationMetadataType.FILE;
      fileDirectoryPath = ModelUtils.extractFilePathFromRepresentationPreservationMetadata(resourcePath);
      id = filename.substring(0, filename.length() - RodaConstants.PREMIS_SUFFIX.length());
      fileId = id.substring(URNUtils.getPremisPrefix(PreservationMetadataType.FILE).length());
    } else if (filename.endsWith(RodaConstants.OTHER_TECH_METADATA_FILE_SUFFIX)) {
      type = PreservationMetadataType.OTHER;
      fileDirectoryPath = ModelUtils.extractFilePathFromRepresentationPreservationMetadata(resourcePath);
      fileId = filename.substring(0, filename.length() - RodaConstants.OTHER_TECH_METADATA_FILE_SUFFIX.length());
      id = fileId;
    } else if (filename.startsWith(URNUtils.getPremisPrefix(PreservationMetadataType.OTHER))) {
      type = PreservationMetadataType.OTHER;
      fileDirectoryPath = ModelUtils.extractFilePathFromRepresentationPreservationMetadata(resourcePath);
      fileId = filename.substring(0, filename.length() - RodaConstants.PREMIS_SUFFIX.length());
      id = fileId;
    } else if (filename.startsWith(URNUtils.getPremisPrefix(PreservationMetadataType.REPRESENTATION))) {
      id = filename.substring(0, filename.length() - RodaConstants.PREMIS_SUFFIX.length());
      type = PreservationMetadataType.REPRESENTATION;
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
      throw new RequestNotValidException(RESOURCE_CANNOT_BE_NULL);
    }

    StoragePath resourcePath = resource.getStoragePath();
    String filename = resourcePath.getName();

    String aipId = ModelUtils.extractAipId(resourcePath).orElse(null);
    String representationId = ModelUtils.extractRepresentationId(resourcePath).orElse(null);
    String type = representationId != null
      ? ModelUtils.extractTypeFromRepresentationOtherMetadata(resourcePath).orElse(null)
      : ModelUtils.extractTypeFromAipOtherMetadata(resourcePath).orElse(null);
    List<String> fileDirectoryPath = representationId != null
      ? ModelUtils.extractFilePathFromRepresentationOtherMetadata(resourcePath)
      : ModelUtils.extractFilePathFromAipOtherMetadata(resourcePath);
    String fileId = filename.substring(0, filename.lastIndexOf('.'));
    String suffix = filename.substring(filename.lastIndexOf('.'), filename.length());

    OtherMetadata om = new OtherMetadata();
    om.setId(IdUtils.getOtherMetadataId(aipId, representationId, fileDirectoryPath, fileId));
    om.setAipId(aipId);
    om.setRepresentationId(representationId);
    om.setFileDirectoryPath(fileDirectoryPath);
    om.setFileId(fileId);
    om.setType(type);
    om.setFileSuffix(suffix);
    return om;
  }

  public static Representation convertResourceToRepresentation(Resource resource)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    if (resource == null) {
      throw new RequestNotValidException(RESOURCE_CANNOT_BE_NULL);
    }

    StoragePath resourcePath = resource.getStoragePath();
    String id = resourcePath.getName();
    String aipId = ModelUtils.extractAipId(resourcePath).orElse(null);
    AIP aip = RodaCoreFactory.getModelService().retrieveAIP(aipId);
    Optional<Representation> rep = aip.getRepresentations().stream().filter(r -> r.getId().equals(id)).findFirst();
    if (rep.isPresent()) {
      return rep.get();
    } else {
      throw new NotFoundException("Unable to find representation with storage path " + resourcePath);
    }
  }

  public static <T extends Serializable> T convertResourceToObject(Resource resource, Class<T> objectClass)
    throws GenericException, RequestNotValidException, IOException {
    if (resource == null) {
      throw new RequestNotValidException(RESOURCE_CANNOT_BE_NULL);
    }

    Binary binary = (Binary) resource;
    try (InputStream inputStream = binary.getContent().createInputStream()) {
      String jsonString = IOUtils.toString(inputStream, RodaConstants.DEFAULT_ENCODING);
      T ret = JsonUtils.getObjectFromJson(jsonString, objectClass);
      return ret;
    }
  }

  public static <T extends IsRODAObject> OptionalWithCause<T> convertResourceTo(StorageService storage,
    Resource resource, Class<T> classToReturn) {
    OptionalWithCause<T> ret;

    try {
      if (classToReturn.equals(AIP.class)) {
        ret = OptionalWithCause.of(classToReturn.cast(getAIPMetadata(storage, resource.getStoragePath())));
      } else if (classToReturn.equals(Representation.class)) {
        ret = OptionalWithCause.of(classToReturn.cast(convertResourceToRepresentation(resource)));
      } else if (classToReturn.equals(File.class)) {
        ret = OptionalWithCause.of(classToReturn.cast(convertResourceToFile(resource)));
      } else if (classToReturn.equals(PreservationMetadata.class)) {
        ret = OptionalWithCause.of(classToReturn.cast(convertResourceToPreservationMetadata(resource)));
      } else if (classToReturn.equals(OtherMetadata.class)) {
        ret = OptionalWithCause.of(classToReturn.cast(convertResourceToOtherMetadata(resource)));
      } else if (classToReturn.equals(DIP.class)) {
        ret = OptionalWithCause.of(classToReturn.cast(getDIPMetadata(storage, resource.getStoragePath())));
      } else if (classToReturn.equals(DIPFile.class)) {
        ret = OptionalWithCause.of(classToReturn.cast(convertResourceToDIPFile(resource)));
      } else {
        ret = OptionalWithCause.of(convertResourceToObject(resource, classToReturn));
      }
    } catch (RODAException e) {
      ret = OptionalWithCause.empty(e);
    } catch (IOException e) {
      ret = OptionalWithCause.empty(new RequestNotValidException(e));
    }

    return ret;
  }

  public static <T extends IsRODAObject> OptionalWithCause<LiteRODAObject> convertResourceToLite(Resource resource,
    Class<T> classToReturn) {
    OptionalWithCause<LiteRODAObject> ret;
    StoragePath storagePath = resource.getStoragePath();
    String fileName = resource.getStoragePath().getName();

    if (classToReturn.equals(AIP.class) || classToReturn.equals(DIP.class)) {
      ret = OptionalWithCause.of(LiteRODAObjectFactory.get(classToReturn, fileName));
    } else if (classToReturn.equals(Representation.class)) {
      String aipId = ModelUtils.extractAipId(storagePath).orElse(null);
      String repId = ModelUtils.extractRepresentationId(storagePath).orElse(null);
      ret = OptionalWithCause.of(LiteRODAObjectFactory.get(classToReturn, aipId, repId));
    } else if (classToReturn.equals(File.class)) {
      List<String> ids = new ArrayList<>();
      ids.add(ModelUtils.extractAipId(storagePath).orElse(null));
      ids.add(ModelUtils.extractRepresentationId(storagePath).orElse(null));
      ids.addAll(ModelUtils.extractFilePathFromRepresentationData(storagePath));
      ids.add(fileName);
      ret = OptionalWithCause.of(LiteRODAObjectFactory.get(classToReturn, ids));
    } else if (classToReturn.equals(DIPFile.class)) {
      List<String> ids = new ArrayList<>();
      ids.add(ModelUtils.extractDipId(storagePath).orElse(null));
      ids.addAll(ModelUtils.extractFilePathFromDIPData(storagePath));
      ids.add(fileName);
      ret = OptionalWithCause.of(LiteRODAObjectFactory.get(classToReturn, ids));
    } else if (classToReturn.equals(Risk.class)) {
      ret = OptionalWithCause.of(LiteRODAObjectFactory.get(classToReturn, ModelUtils.getRiskId(storagePath)));
    } else if (classToReturn.equals(RiskIncidence.class)) {
      ret = OptionalWithCause.of(LiteRODAObjectFactory.get(classToReturn, ModelUtils.getRiskIncidenceId(storagePath)));
    } else if (classToReturn.equals(RepresentationInformation.class)) {
      ret = OptionalWithCause
        .of(LiteRODAObjectFactory.get(classToReturn, ModelUtils.getRepresentationInformationId(storagePath)));
    } else if (classToReturn.equals(Notification.class)) {
      ret = OptionalWithCause.of(LiteRODAObjectFactory.get(classToReturn, ModelUtils.getNotificationId(storagePath)));
    } else if (classToReturn.equals(Job.class)) {
      ret = OptionalWithCause.of(LiteRODAObjectFactory.get(classToReturn, ModelUtils.getJobId(storagePath)));
    } else if (classToReturn.equals(Report.class)) {
      ret = OptionalWithCause.of(LiteRODAObjectFactory.get(classToReturn, ModelUtils.getJobAndReportIds(storagePath)));
    } else if (classToReturn.equals(DescriptiveMetadata.class)) {
      List<String> ids = new ArrayList<>();
      ids.add(ModelUtils.extractAipId(storagePath).orElse(null));
      String representationId = ModelUtils.extractRepresentationId(storagePath).orElse(null);
      if (representationId != null) {
        ids.add(representationId);
      }
      ids.add(fileName);
      ret = OptionalWithCause.of(LiteRODAObjectFactory.get(classToReturn, ids));
    } else if (classToReturn.equals(PreservationMetadata.class)) {
      List<String> ids = new ArrayList<>();
      String aipId = ModelUtils.extractAipId(storagePath).orElse(null);
      if (aipId != null) {
        ids.add(aipId);
      }
      String representationId = ModelUtils.extractRepresentationId(storagePath).orElse(null);
      if (representationId != null) {
        ids.add(representationId);
      }
      ids.add(fileName.replace(RodaConstants.PREMIS_SUFFIX, ""));
      ret = OptionalWithCause.of(LiteRODAObjectFactory.get(classToReturn, ids));
    } else {
      ret = OptionalWithCause.of(LiteRODAObjectFactory.get(classToReturn, fileName));
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
    AIP aip = null;
    try (InputStream inputStream = binary.getContent().createInputStream()) {
      json = IOUtils.toString(inputStream, Charset.forName(RodaConstants.DEFAULT_ENCODING));
      aip = JsonUtils.getObjectFromJson(json, AIP.class);

      // Setting information that does not come in JSON
      aip.setId(aipId);
    } catch (IOException | GenericException e) {
      throw new GenericException("Could not parse AIP metadata of " + aipId + " at " + metadataStoragePath, e);
    }

    return aip;
  }

  public static DIP getDIPMetadata(StorageService storage, StoragePath storagePath)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getDIPMetadata(storage, storagePath.getName(), storagePath);
  }

  public static DIP getDIPMetadata(StorageService storage, String dipId, StoragePath storagePath)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    DefaultStoragePath metadataStoragePath = DefaultStoragePath.parse(storagePath,
      RodaConstants.STORAGE_DIP_METADATA_FILENAME);
    Binary binary = storage.getBinary(metadataStoragePath);

    String json;
    DIP dip;
    try (InputStream inputStream = binary.getContent().createInputStream()) {
      json = IOUtils.toString(inputStream, Charset.forName(RodaConstants.DEFAULT_ENCODING));
      dip = JsonUtils.getObjectFromJson(json, DIP.class);
    } catch (IOException | GenericException e) {
      throw new GenericException("Could not parse DIP metadata of " + dipId + " at " + metadataStoragePath, e);
    }

    // Setting information that does not come in JSON
    dip.setId(dipId);
    return dip;
  }

  private static <T extends Serializable> boolean isDirectoryAcceptable(Class<T> classToReturn) {
    return classToReturn.equals(File.class) || classToReturn.equals(AIP.class)
      || classToReturn.equals(Representation.class) || classToReturn.equals(TransferredResource.class)
      || classToReturn.equals(DIPFile.class) || classToReturn.equals(DIP.class)
      || classToReturn.equals(DisposalConfirmationMetadata.class);
  }

  @FunctionalInterface
  interface ResourceParser<T extends IsRODAObject, R extends Serializable> {

    OptionalWithCause<R> parse(StorageService storage, Resource resource, Class<T> classToReturn);

    default <V extends IsRODAObject> ResourceParser<T, V> andThen(
      Function<OptionalWithCause<R>, OptionalWithCause<V>> after) {
      Objects.requireNonNull(after);
      return (StorageService storage, Resource resource, Class<T> classToReturn) -> after
        .apply(parse(storage, resource, classToReturn));
    }
  }

  public static <T extends IsRODAObject> CloseableIterable<OptionalWithCause<T>> convert(final StorageService storage,
    final CloseableIterable<Resource> iterable, final Class<T> classToReturn) {
    return convert(storage, iterable, classToReturn, (s, r, c) -> ResourceParseUtils.convertResourceTo(s, r, c));
  }

  public static <T extends IsRODAObject> CloseableIterable<OptionalWithCause<LiteRODAObject>> convertLite(
    StorageService storage, CloseableIterable<Resource> iterable, Class<T> classToReturn) {
    return convert(storage, iterable, classToReturn, (s, r, c) -> ResourceParseUtils.convertResourceToLite(r, c));
  }

  private static <T extends IsRODAObject, R extends Serializable> CloseableIterable<OptionalWithCause<R>> convert(
    final StorageService storage, final CloseableIterable<Resource> iterable, final Class<T> classToReturn,
    ResourceParser<T, R> parser) {

    final CloseableIterable<Resource> filtered;
    if (isDirectoryAcceptable(classToReturn)) {
      filtered = iterable;
    } else {
      filtered = CloseableIterables.filter(iterable, p -> !p.isDirectory());
    }

    CloseableIterable<OptionalWithCause<R>> it;

    final Iterator<Resource> iterator = filtered.iterator();
    it = new CloseableIterable<OptionalWithCause<R>>() {

      @Override
      public Iterator<OptionalWithCause<R>> iterator() {
        return new Iterator<OptionalWithCause<R>>() {

          @Override
          public boolean hasNext() {
            if (iterator == null) {
              return true;
            }
            return iterator.hasNext();
          }

          @Override
          public OptionalWithCause<R> next() {
            return parser.parse(storage, iterator.next(), classToReturn);
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
