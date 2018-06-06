/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for IsRODAObject <-> LiteRODAObject conversions. Brief information
 * about how objects information is stored in a LiteRODAObject:
 * 
 * <table>
 * <tr>
 * <td>AIP</td>
 * <td>org.roda.core.data.v2.ip.AIP | aipId</td>
 * </tr>
 * <tr>
 * <td>DIP</td>
 * <td>org.roda.core.data.v2.ip.DIP | dipId</td>
 * </tr>
 * <tr>
 * <td>Format</td>
 * <td>org.roda.core.data.v2.formats.RepresentationInformation |
 * representationInformationId</td>
 * </tr>
 * <tr>
 * <td>Job</td>
 * <td>org.roda.core.data.v2.jobs.Job | jobId</td>
 * </tr>
 * <tr>
 * <td>Report</td>
 * <td>org.roda.core.data.v2.jobs.Report | jobId | reportId</td>
 * </tr>
 * <tr>
 * <td>Notification</td>
 * <td>org.roda.core.data.v2.notifications.Notification | notificationId</td>
 * </tr>
 * <tr>
 * <td>Risk</td>
 * <td>org.roda.core.data.v2.risks.Risk | riskId</td>
 * </tr>
 * <tr>
 * <td>RiskIncidence</td>
 * <td>org.roda.core.data.v2.risks.RiskIncidence | riskincidenceId</td>
 * </tr>
 * <tr>
 * <td>Representation</td>
 * <td>org.roda.core.data.v2.ip.Representation | aipId | representationId</td>
 * </tr>
 * <tr>
 * <td>TransferredResource</td>
 * <td>org.roda.core.data.v2.ip.TransferredResource | fullPath</td>
 * </tr>
 * <tr>
 * <td>DescriptiveMetadata</td>
 * <td>org.roda.core.data.v2.ip.DescriptiveMetadata | aipId |
 * descriptiveMetadataId</td>
 * </tr>
 * <tr>
 * <td>DescriptiveMetadata</td>
 * <td>org.roda.core.data.v2.ip.DescriptiveMetadata | aipId | representationId |
 * descriptiveMetadataId</td>
 * </tr>
 * <tr>
 * <td>LogEntry</td>
 * <td>org.roda.core.data.v2.log.LogEntry|logId</td>
 * </tr>
 * <tr>
 * <td>User</td>
 * <td>org.roda.core.data.v2.user.User|name</td>
 * </tr>
 * <tr>
 * <td>Group</td>
 * <td>org.roda.core.data.v2.user.Group|name</td>
 * </tr>
 * </table>
 * 
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 * 
 */
public final class LiteRODAObjectFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(LiteRODAObjectFactory.class);

  public static final String SEPARATOR = "|";
  public static final String SEPARATOR_REGEX = "\\|";
  public static final String SEPARATOR_URL_ENCODED = "%7C";

  private LiteRODAObjectFactory() {
    // do nothing
  }

  public static <T extends IsRODAObject> Optional<LiteRODAObject> get(Class<T> objectClass, List<String> ids) {
    return get(objectClass, ids, true);
  }

  public static <T extends IsRODAObject> Optional<LiteRODAObject> get(Class<T> objectClass, String... ids) {
    return get(objectClass, Arrays.asList(ids), true);
  }

  public static <T extends IsRODAObject> Optional<LiteRODAObject> get(T object) {
    Optional<LiteRODAObject> ret = Optional.empty();

    if (object instanceof AIP || object instanceof IndexedAIP || object instanceof DIP || object instanceof IndexedDIP
      || object instanceof RepresentationInformation || object instanceof Format || object instanceof Job
      || object instanceof Notification || object instanceof Risk || object instanceof IndexedRisk
      || object instanceof RiskIncidence || object instanceof LogEntry) {
      ret = get(ModelUtils.giveRespectiveModelClass(object.getClass()), Arrays.asList(object.getId()), false);
    } else if (object instanceof DescriptiveMetadata) {
      ret = getDescriptiveMetadata(object);
    } else if (object instanceof PreservationMetadata) {
      ret = getPreservationMetadata(object);
    } else if (object instanceof DIPFile) {
      ret = getDIPFile(object);
    } else if (object instanceof File) {
      ret = getFile(object);
    } else if (object instanceof IndexedFile) {
      ret = getFileFromIndex(object);
    } else if (object instanceof TransferredResource) {
      TransferredResource o = (TransferredResource) object;
      ret = get(TransferredResource.class, Arrays.asList(o.getFullPath()), false);
    } else if (object instanceof Report || object instanceof IndexedReport) {
      Report o = (Report) object;
      ret = get(Report.class, Arrays.asList(o.getJobId(), o.getId()), false);
    } else if (object instanceof Representation || object instanceof IndexedRepresentation) {
      Representation o = (Representation) object;
      ret = get(Representation.class, Arrays.asList(o.getAipId(), o.getId()), false);
    } else if (object instanceof User) {
      User o = (User) object;
      ret = get(User.class, Arrays.asList(o.getName()), false);
    } else if (object instanceof Group) {
      Group o = (Group) object;
      ret = get(Group.class, Arrays.asList(o.getName()), false);
    }

    if (!ret.isPresent()) {
      LOGGER.error("Unable to create {} from object with class '{}' & id '{}'", LiteRODAObject.class.getSimpleName(),
        object.getClass().getName(), object.getId());
    }

    return ret;
  }

  public static <T extends IsRODAObject> Optional<LiteRODAObject> get(Class<T> objectClass, List<String> ids,
    boolean logIfReturningEmpty) {
    Optional<LiteRODAObject> ret = Optional.empty();

    if (objectClass == AIP.class || objectClass == IndexedAIP.class || objectClass == DIP.class
      || objectClass == RepresentationInformation.class || objectClass == Format.class || objectClass == Job.class
      || objectClass == Notification.class || objectClass == Risk.class || objectClass == RiskIncidence.class
      || objectClass == User.class || objectClass == Group.class || objectClass == LogEntry.class) {
      ret = create(objectClass, 1, ids);
    } else if (objectClass == DescriptiveMetadata.class) {
      if (ids.size() == 2 || ids.size() == 3) {
        ret = create(objectClass, ids.size(), ids);
      }
    } else if (objectClass == DIPFile.class) {
      if (ids.size() >= 2) {
        ret = create(objectClass, ids.size(), ids);
      }
    } else if (objectClass == File.class) {
      if (ids.size() >= 3) {
        ret = create(objectClass, ids.size(), ids);
      }
    } else if (objectClass == Report.class || objectClass == Representation.class) {
      ret = create(objectClass, 2, ids);
    } else if (objectClass == TransferredResource.class || objectClass == PreservationMetadata.class) {
      ret = create(objectClass, ids.size(), ids);
    }

    if (logIfReturningEmpty && !ret.isPresent()) {
      LOGGER.error("Unable to create {} from objectClass '{}' with ids '{}'", LiteRODAObject.class.getSimpleName(),
        objectClass.getName(), ids);
    }

    return ret;
  }

  private static <T extends IsRODAObject> Optional<LiteRODAObject> getDescriptiveMetadata(T object) {
    Optional<LiteRODAObject> ret;

    DescriptiveMetadata o = (DescriptiveMetadata) object;
    if (o.getRepresentationId() == null) {
      ret = get(DescriptiveMetadata.class, Arrays.asList(o.getAipId(), o.getId()), false);
    } else {
      ret = get(DescriptiveMetadata.class, Arrays.asList(o.getAipId(), o.getRepresentationId(), o.getId()), false);
    }

    return ret;
  }

  private static <T extends IsRODAObject> Optional<LiteRODAObject> getPreservationMetadata(T object) {
    Optional<LiteRODAObject> ret;

    PreservationMetadata o = (PreservationMetadata) object;
    if (o.getAipId() == null) {
      ret = get(PreservationMetadata.class, Arrays.asList(o.getId()), false);
    } else if (o.getRepresentationId() == null) {
      ret = get(PreservationMetadata.class, Arrays.asList(o.getAipId(), o.getId()), false);
    } else if (o.getFileId() == null) {
      ret = get(PreservationMetadata.class, Arrays.asList(o.getAipId(), o.getRepresentationId(), o.getId()), false);
    } else {
      List<String> list = new ArrayList<>();
      list.add(o.getAipId());
      list.add(o.getRepresentationId());
      list.addAll(o.getFileDirectoryPath());
      list.add(o.getFileId());
      list.add(o.getId());
      ret = get(DIPFile.class, list, false);
    }

    return ret;
  }

  private static <T extends IsRODAObject> Optional<LiteRODAObject> getDIPFile(T object) {
    DIPFile o = (DIPFile) object;
    List<String> list = new ArrayList<>();
    list.add(o.getDipId());
    list.addAll(o.getPath());
    list.add(o.getId());
    return get(DIPFile.class, list, false);
  }

  private static <T extends IsRODAObject> Optional<LiteRODAObject> getFile(T object) {
    File o = (File) object;
    List<String> list = new ArrayList<>();
    list.add(o.getAipId());
    list.add(o.getRepresentationId());
    list.addAll(o.getPath());
    list.add(o.getId());
    return get(File.class, list, false);
  }

  private static <T extends IsRODAObject> Optional<LiteRODAObject> getFileFromIndex(T object) {
    IndexedFile o = (IndexedFile) object;
    List<String> list = new ArrayList<>();
    list.add(o.getAipId());
    list.add(o.getRepresentationId());
    list.addAll(o.getPath());
    list.add(o.getId());
    return get(File.class, list, false);
  }

  public static <T extends IsRODAObject> OptionalWithCause<T> get(ModelService model, LiteRODAObject liteRODAObject) {
    try {
      T ret = null;

      String[] split = liteRODAObject.getInfo().split(SEPARATOR_REGEX);
      if (split.length >= 2) {
        String clazz = split[0];
        String firstId = decodeId(split[1]);
        if (AIP.class.getName().equals(clazz) || IndexedAIP.class.getName().equals(clazz)) {
          ret = (T) model.retrieveAIP(firstId);
        } else if (DescriptiveMetadata.class.getName().equals(clazz)) {
          ret = getDescriptiveMetadata(model, split);
        } else if (DIP.class.getName().equals(clazz) || IndexedDIP.class.getName().equals(clazz)) {
          ret = (T) model.retrieveDIP(firstId);
        } else if (DIPFile.class.getName().equals(clazz)) {
          ret = getDIPFile(model, split);
        } else if (File.class.getName().equals(clazz) || IndexedFile.class.getName().equals(clazz)) {
          ret = getFile(model, split);
        } else if (RepresentationInformation.class.getName().equals(clazz)) {
          ret = (T) model.retrieveRepresentationInformation(firstId);
        } else if (Format.class.getName().equals(clazz)) {
          ret = (T) model.retrieveFormat(firstId);
        } else if (Job.class.getName().equals(clazz)) {
          ret = (T) model.retrieveJob(firstId);
        } else if (LogEntry.class.getName().equals(clazz)) {
          // INFO 20161229 nvieira It is too complex to use model/storage and
          // using index creates a circular dependency
        } else if (Notification.class.getName().equals(clazz)) {
          ret = (T) model.retrieveNotification(firstId);
        } else if (PreservationMetadata.class.getName().equals(clazz)) {
          ret = getPreservationMetadata(model, split);
        } else if (Report.class.getName().equals(clazz) || IndexedReport.class.getName().equals(clazz)) {
          if (split.length == 3) {
            ret = (T) model.retrieveJobReport(firstId, decodeId(split[2]));
          }
        } else if (Risk.class.getName().equals(clazz) || IndexedRisk.class.getName().equals(clazz)) {
          ret = (T) model.retrieveRisk(firstId);
        } else if (RiskIncidence.class.getName().equals(clazz)) {
          ret = (T) model.retrieveRiskIncidence(firstId);
        } else if (Representation.class.getName().equals(clazz)
          || IndexedRepresentation.class.getName().equals(clazz)) {
          if (split.length == 3) {
            ret = (T) model.retrieveRepresentation(firstId, decodeId(split[2]));
          }
        } else if (TransferredResource.class.getName().equals(clazz)) {
          ret = (T) model.retrieveTransferredResource(firstId);
        } else if (User.class.getName().equals(clazz)) {
          ret = (T) model.retrieveUser(firstId);
        } else if (Group.class.getName().equals(clazz)) {
          ret = (T) model.retrieveGroup(firstId);
        }
      }

      return OptionalWithCause.of(ret);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Unable to create object from {}", liteRODAObject, e);
      return OptionalWithCause.empty(e);
    }
  }

  private static <T extends IsRODAObject> T getDescriptiveMetadata(ModelService model, String[] split)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    T ret = null;

    if (split.length == 3) {
      ret = (T) model.retrieveDescriptiveMetadata(decodeId(split[1]), decodeId(split[2]));
    } else if (split.length == 4) {
      ret = (T) model.retrieveDescriptiveMetadata(decodeId(split[1]), decodeId(split[2]), decodeId(split[3]));
    }

    return ret;
  }

  private static <T extends IsRODAObject> T getPreservationMetadata(ModelService model, String[] split)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    T ret = null;
    int size = split.length;
    PreservationMetadataType type = IdUtils.getPreservationTypeFromId(decodeId(split[size - 1]));

    if (split.length == 2) {
      ret = (T) model.retrievePreservationMetadata(null, null, null, null, type);
    } else if (split.length == 3) {
      ret = (T) model.retrievePreservationMetadata(decodeId(split[1]), null, null, null, type);
    } else if (split.length == 4) {
      ret = (T) model.retrievePreservationMetadata(decodeId(split[1]), decodeId(split[2]), null, null, type);
    } else if (split.length > 4) {
      List<String> directoryPath = new ArrayList<>();
      String fileId = null;
      for (int i = 2; i < split.length - 1; i++) {
        if (i + 1 == split.length) {
          fileId = decodeId(split[i]);
        } else {
          directoryPath.add(decodeId(split[i]));
        }
      }
      ret = (T) model.retrievePreservationMetadata(decodeId(split[1]), decodeId(split[2]), directoryPath, fileId, type);
    }

    return ret;
  }

  private static <T extends IsRODAObject> T getDIPFile(ModelService model, String[] split)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    T ret = null;

    if (split.length >= 3) {
      List<String> directoryPath = new ArrayList<>();
      String fileId = null;
      for (int i = 2; i < split.length; i++) {
        if (i + 1 == split.length) {
          fileId = decodeId(split[i]);
        } else {
          directoryPath.add(decodeId(split[i]));
        }
      }
      ret = (T) model.retrieveDIPFile(decodeId(split[1]), directoryPath, fileId);
    }

    return ret;
  }

  private static <T extends IsRODAObject> T getFile(ModelService model, String[] split)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    T ret = null;

    if (split.length >= 4) {
      List<String> directoryPath = new ArrayList<>();
      String fileId = null;
      for (int i = 3; i < split.length; i++) {
        if (i + 1 == split.length) {
          fileId = decodeId(split[i]);
        } else {
          directoryPath.add(decodeId(split[i]));
        }
      }
      ret = (T) model.retrieveFile(decodeId(split[1]), decodeId(split[2]), directoryPath, fileId);
    }

    return ret;
  }

  private static <T extends IsRODAObject> Optional<LiteRODAObject> create(Class<T> objectClass, int numberOfIdsMustHave,
    List<String> ids) {
    LiteRODAObject ret = null;

    if (numberOfIdsMustHave == ids.size()) {
      StringBuilder sb = new StringBuilder();
      sb.append(objectClass.getName());
      for (String id : ids) {
        sb.append(SEPARATOR);
        try {
          sb.append(encodeId(id));
        } catch (GenericException e) {
          // do nothing
        }
      }
      ret = new LiteRODAObject(sb.toString());
    }

    return Optional.ofNullable(ret);
  }

  private static String encodeId(String id) throws GenericException {
    if (id != null) {
      return id.replaceAll(SEPARATOR_REGEX, SEPARATOR_URL_ENCODED);
    } else {
      throw new GenericException("Was trying to encode an 'id' but it is NULL");
    }
  }

  private static String decodeId(String id) throws GenericException {
    if (id != null) {
      return id.replaceAll(SEPARATOR_URL_ENCODED, SEPARATOR);
    } else {
      throw new GenericException("Was trying to decode an 'id' but it is NULL");
    }
  }

  public static <T extends IsRODAObject> List<LiteRODAObject> transformIntoLite(ModelService model,
    List<T> modelObjects) {
    return modelObjects.stream().map(o -> model.retrieveLiteFromObject(o)).filter(o -> o.isPresent()).map(o -> o.get())
      .collect(Collectors.toList());
  }

  public static <T extends IsRODAObject> List<LiteOptionalWithCause> transformIntoLiteWithCause(ModelService model,
    List<T> modelObjects) {
    return modelObjects.stream().map(o -> model.retrieveLiteFromObject(o)).filter(o -> o.isPresent())
      .map(o -> LiteOptionalWithCause.of(o.get())).collect(Collectors.toList());
  }

  public static <T extends IsRODAObject> CloseableIterable<OptionalWithCause<LiteRODAObject>> transformIntoLite(
    final CloseableIterable<OptionalWithCause<T>> list) {
    CloseableIterable<OptionalWithCause<LiteRODAObject>> it;

    final Iterator<OptionalWithCause<T>> iterator = list.iterator();
    it = new CloseableIterable<OptionalWithCause<LiteRODAObject>>() {

      @Override
      public Iterator<OptionalWithCause<LiteRODAObject>> iterator() {
        return new Iterator<OptionalWithCause<LiteRODAObject>>() {

          @Override
          public boolean hasNext() {
            if (iterator == null) {
              return true;
            }
            return iterator.hasNext();
          }

          @Override
          public OptionalWithCause<LiteRODAObject> next() {
            OptionalWithCause<T> next = iterator.next();
            if (next.isPresent()) {
              return OptionalWithCause.of(get(next.get()));
            } else {
              return OptionalWithCause.empty(next.getCause());
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
        list.close();
      }
    };

    return it;
  }

  public static <T extends IsRODAObject> CloseableIterable<OptionalWithCause<T>> transformFromLite(ModelService model,
    final CloseableIterable<OptionalWithCause<LiteRODAObject>> list) {
    CloseableIterable<OptionalWithCause<T>> it;

    final Iterator<OptionalWithCause<LiteRODAObject>> iterator = list.iterator();
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
            OptionalWithCause<LiteRODAObject> next = iterator.next();
            if (next.isPresent()) {
              return get(model, next.get());
            } else {
              return OptionalWithCause.empty(next.getCause());
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
        list.close();
      }
    };

    return it;
  }

}
