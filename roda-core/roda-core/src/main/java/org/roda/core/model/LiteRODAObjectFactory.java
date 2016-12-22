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
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.storage.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for IsRODAObject <-> LiteRODAObject conversions. Brief information
 * about how objects information is stored in a LiteRODAObject:
 * 
 * <table>
 * <tr>
 * <td>AIP</td>
 * <td>org.roda.core.data.v2.ip.AIP|aipId</td>
 * </tr>
 * <tr>
 * <td>DIP</td>
 * <td>org.roda.core.data.v2.ip.DIP|dipId</td>
 * </tr>
 * <tr>
 * <td>Format</td>
 * <td>org.roda.core.data.v2.formats.Format|formatId</td>
 * </tr>
 * <tr>
 * <td>Job</td>
 * <td>org.roda.core.data.v2.jobs.Job|jobId</td>
 * </tr>
 * <tr>
 * <td>Report</td>
 * <td>org.roda.core.data.v2.jobs.Report|jobId|reportId</td>
 * </tr>
 * <tr>
 * <td>Notification</td>
 * <td>org.roda.core.data.v2.notifications.Notification|notificationId</td>
 * </tr>
 * <tr>
 * <td>Risk</td>
 * <td>org.roda.core.data.v2.risks.Risk|riskId</td>
 * </tr>
 * <tr>
 * <td>RiskIncidence</td>
 * <td>org.roda.core.data.v2.risks.RiskIncidence|riskincidenceId</td>
 * </tr>
 * <tr>
 * <td>Representation</td>
 * <td>org.roda.core.data.v2.ip.Representation|aipId|representationId</td>
 * </tr>
 * <tr>
 * <td>TransferredResource</td>
 * <td>org.roda.core.data.v2.ip.TransferredResource|fullPath</td>
 * </tr>
 * <tr>
 * <td>DescriptiveMetadata</td>
 * <td>org.roda.core.data.v2.ip.DescriptiveMetadata|aipId|descriptiveMetadataId
 * </td>
 * </tr>
 * <tr>
 * <td>DescriptiveMetadata</td>
 * <td>org.roda.core.data.v2.ip.DescriptiveMetadata|aipId|representationId|
 * descriptiveMetadataId</td>
 * </tr>
 * </table>
 * 
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 * 
 */
public final class LiteRODAObjectFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(LiteRODAObjectFactory.class);

  private static final String SEPARATOR = "|";
  private static final String SEPARATOR_REGEX = "\\|";

  private LiteRODAObjectFactory() {
    // do nothing
  }

  public static <T extends IsRODAObject> Optional<LiteRODAObject> get(Class<T> objectClass, List<String> ids) {
    return get(objectClass, ids, true);
  }

  public static <T extends IsRODAObject> Optional<LiteRODAObject> get(Class<T> objectClass, String... ids) {
    return get(objectClass, Arrays.asList(ids), true);
  }

  private static <T extends IsRODAObject> Optional<LiteRODAObject> get(Class<T> objectClass, List<String> ids,
    boolean logIfReturningEmpty) {
    Optional<LiteRODAObject> ret = Optional.empty();

    if (objectClass == AIP.class || objectClass == TransferredResource.class || objectClass == DIP.class
      || objectClass == Format.class || objectClass == Job.class || objectClass == Notification.class
      || objectClass == Risk.class || objectClass == RiskIncidence.class) {
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
    }

    if (logIfReturningEmpty && !ret.isPresent()) {
      LOGGER.error("Unable to create {} from objectClass '{}' with ids '{}'", LiteRODAObject.class.getSimpleName(),
        objectClass.getName(), ids);
    }

    return ret;
  }

  public static <T extends IsRODAObject> Optional<LiteRODAObject> get(T object) {
    Optional<LiteRODAObject> ret = Optional.empty();

    if (object instanceof AIP || object instanceof DIP || object instanceof Format || object instanceof Job
      || object instanceof Notification || object instanceof Risk || object instanceof RiskIncidence) {
      IsRODAObject o = (IsRODAObject) object;
      ret = get(object.getClass(), Arrays.asList(o.getId()), false);
    } else if (object instanceof DescriptiveMetadata) {
      ret = getDescriptiveMetadata(object);
    } else if (object instanceof DIPFile) {
      ret = getDIPFile(object);
    } else if (object instanceof File) {
      ret = getFile(object);
    } else if (object instanceof TransferredResource) {
      TransferredResource o = (TransferredResource) object;
      ret = get(TransferredResource.class, Arrays.asList(o.getFullPath()), false);
    } else if (object instanceof Report) {
      Report o = (Report) object;
      ret = get(Report.class, Arrays.asList(o.getJobId(), o.getId()), false);
    } else if (object instanceof Representation) {
      Representation o = (Representation) object;
      ret = get(Representation.class, Arrays.asList(o.getAipId(), o.getId()), false);
    }

    if (!ret.isPresent()) {
      LOGGER.error("Unable to create {} from object with class '{}' & id '{}'", LiteRODAObject.class.getSimpleName(),
        object.getClass().getName(), object.getId());
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

  public static <T extends IsRODAObject> Optional<T> get(ModelService model, LiteRODAObject liteRODAObject) {
    try {
      T ret = null;

      String[] split = liteRODAObject.getInfo().split(SEPARATOR_REGEX);
      if (split.length >= 2) {
        String clazz = split[0];
        if (AIP.class.getName().equals(clazz)) {
          ret = (T) model.retrieveAIP(split[1]);
        } else if (DescriptiveMetadata.class.getName().equals(clazz)) {
          ret = getDescriptiveMetadata(model, split);
        } else if (DIP.class.getName().equals(clazz)) {
          ret = (T) model.retrieveDIP(split[1]);
        } else if (DIPFile.class.getName().equals(clazz)) {
          ret = getDIPFile(model, split);
        } else if (File.class.getName().equals(clazz)) {
          ret = getFile(model, split);
        } else if (Format.class.getName().equals(clazz)) {
          ret = (T) model.retrieveFormat(split[1]);
        } else if (Job.class.getName().equals(clazz)) {
          ret = (T) model.retrieveJob(split[1]);
        } else if (LogEntry.class.getName().equals(clazz)) {
          // FIXME 20161221 hsilva: to implement whenever model has methods for
          // LogEntry
        } else if (Notification.class.getName().equals(clazz)) {
          ret = (T) model.retrieveNotification(split[1]);
        } else if (PreservationMetadata.class.getName().equals(clazz)) {
          // FIXME 20161221 hsilva: how to?
        } else if (Report.class.getName().equals(clazz)) {
          if (split.length == 3) {
            ret = (T) model.retrieveJobReport(split[1], split[2]);
          }
        } else if (Risk.class.getName().equals(clazz)) {
          ret = (T) model.retrieveRisk(split[1]);
        } else if (RiskIncidence.class.getName().equals(clazz)) {
          ret = (T) model.retrieveRiskIncidence(split[1]);
        } else if (Representation.class.getName().equals(clazz)) {
          if (split.length == 3) {
            ret = (T) model.retrieveRepresentation(split[1], split[2]);
          }
        } else if (TransferredResource.class.getName().equals(clazz)) {
          ret = (T) model.retrieveTransferredResource(split[1]);
        }
      }

      return Optional.ofNullable(ret);

    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      return Optional.empty();
    }
  }

  private static <T extends IsRODAObject> T getDescriptiveMetadata(ModelService model, String[] split)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    T ret = null;

    if (split.length == 3) {
      ret = (T) model.retrieveDescriptiveMetadata(split[1], split[2]);
    } else if (split.length == 4) {
      ret = (T) model.retrieveDescriptiveMetadata(split[1], split[2], split[3]);
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
          fileId = split[i];
        } else {
          directoryPath.add(split[i]);
        }
      }
      ret = (T) model.retrieveDIPFile(split[1], directoryPath, fileId);
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
          fileId = split[i];
        } else {
          directoryPath.add(split[i]);
        }
      }
      ret = (T) model.retrieveFile(split[1], split[2], directoryPath, fileId);
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
        sb.append(SEPARATOR).append(id);
      }
      ret = new LiteRODAObject(sb.toString());
    }

    return Optional.ofNullable(ret);
  }

  public static <T extends IsRODAObject> List<LiteRODAObject> transformIntoLite(ModelService model,
    List<T> modelObjects) {
    return modelObjects.stream().map(o -> model.retrieveLiteFromObject(o)).filter(o -> o.isPresent()).map(o -> o.get())
      .collect(Collectors.toList());
  }

  public static <T extends IsRODAObject> CloseableIterable<OptionalWithCause<LiteRODAObject>> transformIntoLite(
    final CloseableIterable<OptionalWithCause<T>> list) {
    CloseableIterable<OptionalWithCause<LiteRODAObject>> it = null;

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

}
