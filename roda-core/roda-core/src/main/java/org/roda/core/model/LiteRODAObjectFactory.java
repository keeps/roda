/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteRODAObject;
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

public final class LiteRODAObjectFactory {
  private static final String SEPARATOR = "|";
  private static final String SEPARATOR_REGEX = "\\|";

  private LiteRODAObjectFactory() {
    // do nothing
  }

  public static <T extends IsRODAObject> Optional<LiteRODAObject> get(Class<T> objectClass, List<String> ids) {
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
    return ret;
  }

  public static <T extends IsRODAObject> Optional<LiteRODAObject> get(T object) {
    Optional<LiteRODAObject> ret;
    List<String> list;
    if (object instanceof AIP || object instanceof DIP || object instanceof Format || object instanceof Job
      || object instanceof Notification || object instanceof Risk || object instanceof RiskIncidence) {
      IsRODAObject o = (IsRODAObject) object;
      ret = get(object.getClass(), Arrays.asList(o.getId()));
    } else if (object instanceof DescriptiveMetadata) {
      DescriptiveMetadata o = (DescriptiveMetadata) object;
      if (o.getRepresentationId() == null) {
        ret = get(DescriptiveMetadata.class, Arrays.asList(o.getAipId(), o.getId()));
      } else {
        ret = get(DescriptiveMetadata.class, Arrays.asList(o.getAipId(), o.getRepresentationId(), o.getId()));
      }
    } else if (object instanceof DIPFile) {
      DIPFile o = (DIPFile) object;
      list = new ArrayList<>();
      list.add(o.getDipId());
      list.addAll(o.getPath());
      list.add(o.getId());
      ret = get(DIPFile.class, list);
    } else if (object instanceof File) {
      File o = (File) object;
      list = new ArrayList<>();
      list.add(o.getAipId());
      list.add(o.getRepresentationId());
      list.addAll(o.getPath());
      list.add(o.getId());
      ret = get(File.class, list);
    } else if (object instanceof TransferredResource) {
      TransferredResource o = (TransferredResource) object;
      ret = get(TransferredResource.class, Arrays.asList(o.getFullPath()));
    } else if (object instanceof Report) {
      Report o = (Report) object;
      ret = get(Report.class, Arrays.asList(o.getJobId(), o.getId()));
    } else if (object instanceof Representation) {
      Representation o = (Representation) object;
      ret = get(Representation.class, Arrays.asList(o.getAipId(), o.getId()));
    } else {
      ret = Optional.empty();
    }
    return ret;
  }

  public static <T extends IsRODAObject> Optional<T> get(ModelService model, LiteRODAObject liteRODAObject) {
    try {
      T ret = null;

      String[] split = liteRODAObject.getInfo().split(SEPARATOR_REGEX);
      if (split.length >= 2) {
        String clazz = split[0];
        if (AIP.class.getName().equals(clazz)) {
          // AIP|aipId
          ret = (T) model.retrieveAIP(split[1]);
        } else if (DescriptiveMetadata.class.getName().equals(clazz)) {
          ret = getDescriptiveMetadata(model, split);
        } else if (DIP.class.getName().equals(clazz)) {
          // DIP|dipId
          ret = (T) model.retrieveDIP(split[1]);
        } else if (DIPFile.class.getName().equals(clazz)) {
          ret = getDIPFile(model, split);
        } else if (File.class.getName().equals(clazz)) {
          ret = getFile(model, split);
        } else if (Format.class.getName().equals(clazz)) {
          // Format|formatId
          ret = (T) model.retrieveFormat(split[1]);
        } else if (Job.class.getName().equals(clazz)) {
          // Job|jobId
          ret = (T) model.retrieveJob(split[1]);
        } else if (LogEntry.class.getName().equals(clazz)) {
          // FIXME 20161221 hsilva: to implement whenever model has methods for
          // LogEntry
        } else if (Notification.class.getName().equals(clazz)) {
          // Notification|notificationId
          ret = (T) model.retrieveNotification(split[1]);
        } else if (PreservationMetadata.class.getName().equals(clazz)) {
          // FIXME 20161221 hsilva: how to?
        } else if (Report.class.getName().equals(clazz)) {
          if (split.length == 3) {
            // Report|jobId|reportId
            ret = (T) model.retrieveJobReport(split[1], split[2]);
          }
        } else if (Risk.class.getName().equals(clazz)) {
          // Risk|riskId
          ret = (T) model.retrieveRisk(split[1]);
        } else if (RiskIncidence.class.getName().equals(clazz)) {
          // RiskIncidence|riskincidenceId
          ret = (T) model.retrieveRiskIncidence(split[1]);
        } else if (Representation.class.getName().equals(clazz)) {
          if (split.length == 3) {
            // Representation|aipId|representationId
            ret = (T) model.retrieveRepresentation(split[1], split[2]);
          }
        } else if (TransferredResource.class.getName().equals(clazz)) {
          // TransferredResource|fullPath
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
    T ret;
    if (split.length == 3) {
      // DescriptiveMetadata|aipId|descriptiveMetadataId
      ret = (T) model.retrieveDescriptiveMetadata(split[1], split[2]);
    } else if (split.length == 4) {
      // DescriptiveMetadata|aipId|representationId|descriptiveMetadataId
      ret = (T) model.retrieveDescriptiveMetadata(split[1], split[2], split[3]);
    } else {
      ret = null;
    }
    return ret;
  }

  private static <T extends IsRODAObject> T getDIPFile(ModelService model, String[] split)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    T ret;
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
    } else {
      ret = null;
    }
    return ret;
  }

  private static <T extends IsRODAObject> T getFile(ModelService model, String[] split)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    T ret;
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
    } else {
      ret = null;
    }
    return ret;
  }

  private static <T extends IsRODAObject> Optional<LiteRODAObject> create(Class<T> objectClass, int numberOfIdsMustHave,
    List<String> ids) {
    LiteRODAObject ret;
    if (numberOfIdsMustHave == ids.size()) {
      StringBuilder sb = new StringBuilder();
      sb.append(objectClass.getName());
      for (String id : ids) {
        sb.append(SEPARATOR).append(id);
      }
      ret = new LiteRODAObject(sb.toString());
    } else {
      ret = null;
    }
    return Optional.ofNullable(ret);
  }

}
