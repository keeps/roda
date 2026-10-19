/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.transaction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.model.ModelObserver;
import org.roda.core.model.ModelService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Records the model notifications emitted inside a RODA transaction instead of
 * delivering them, so that observers (e.g. the index) only see data after it
 * has been committed to the main storage.
 * <p>
 * During the transaction, {@link #recorder()} is registered as the only
 * observer of the staging model. After the storage commit, the recorded
 * notifications are consolidated ({@link #consolidate(boolean)}) and replayed
 * against the observers of the main model ({@link #replay}), with AIPs,
 * representations and files read again from the main storage. Model methods are
 * not changed: they keep emitting the same notifications, only their delivery
 * is deferred.
 * </p>
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DeferredNotificationJournal {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeferredNotificationJournal.class);

  /**
   * AIP notifications whose effect is fully covered by aipCreated/aipUpdated of
   * the same AIP, which re-index the AIP, its representations, files and
   * preservation events from the model.
   */
  private static final Set<String> AIP_PARTIAL_UPDATES = Set.of("aipUpdatedOn", "aipOnHoldStatusUpdated",
    "aipOnDisposalHoldUpdated", "aipStateUpdated", "aipInstanceIdUpdated", "aipHasShallowFiles", "aipChangedType",
    "aipPermissionsUpdated");

  /** Notifications on objects inside an AIP that aipCreated/aipUpdated cover. */
  private static final Set<String> AIP_CHILD_UPDATES = Set.of("representationCreated", "representationUpdated",
    "representationUpdatedOn", "fileCreated", "fileUpdated", "descriptiveMetadataCreated",
    "descriptiveMetadataUpdated", "preservationMetadataCreated", "preservationMetadataUpdated");

  private static final Set<String> AIP_FULL_REINDEX = Set.of("aipCreated", "aipUpdated");

  private final List<Entry> entries = Collections.synchronizedList(new ArrayList<>());
  private final Set<String> touchedIndexIds = Collections.synchronizedSet(new HashSet<>());
  private final Set<Class<? extends IsIndexed>> deferredCommits = Collections.synchronizedSet(new LinkedHashSet<>());

  private final ModelObserver recorder = (ModelObserver) Proxy.newProxyInstance(ModelObserver.class.getClassLoader(),
    new Class<?>[] {ModelObserver.class}, (proxy, method, args) -> {
      if (method.getDeclaringClass() == Object.class) {
        return switch (method.getName()) {
          case "equals" -> proxy == args[0];
          case "hashCode" -> System.identityHashCode(proxy);
          default -> "DeferredNotificationJournal.recorder";
        };
      }
      record(method, args == null ? new Object[0] : args);
      return new ReturnWithExceptions<Void, ModelObserver>();
    });

  /**
   * One recorded notification.
   *
   * @param method
   *          the {@link ModelObserver} method that was invoked
   * @param args
   *          the arguments it was invoked with
   * @param key
   *          the entity it refers to (null when it doesn't refer to a single
   *          entity)
   * @param delete
   *          whether it removes the entity
   */
  public record Entry(Method method, Object[] args, EntityKey key, boolean delete) {
    String identity() {
      return method.getName() + "#" + Arrays.stream(args).map(DeferredNotificationJournal::identityOf)
        .collect(Collectors.joining(","));
    }
  }

  /**
   * Identifies an entity touched by a notification.
   *
   * @param type
   *          entity type
   * @param id
   *          entity identifier, unique within its type
   * @param aipId
   *          AIP the entity belongs to, if any
   * @param representationId
   *          representation the entity belongs to, if any
   */
  public record EntityKey(String type, String id, String aipId, String representationId) {
  }

  /** The observer to register on the staging model of the transaction. */
  public ModelObserver recorder() {
    return recorder;
  }

  public boolean isEmpty() {
    return entries.isEmpty();
  }

  public int size() {
    return entries.size();
  }

  public List<Entry> getEntries() {
    synchronized (entries) {
      return new ArrayList<>(entries);
    }
  }

  /**
   * Index document ids (uuid) of AIPs, representations and files touched by the
   * transaction. Used to detect index reads of data written by the same
   * transaction.
   */
  public boolean isTouched(String indexId) {
    return touchedIndexIds.contains(indexId);
  }

  public void clear() {
    entries.clear();
    touchedIndexIds.clear();
    deferredCommits.clear();
  }

  /**
   * Records an index commit requested inside the transaction (to make changes
   * searchable right away); it is done after the notifications are replayed.
   */
  public void deferCommit(Class<? extends IsIndexed>[] classes) {
    deferredCommits.addAll(Arrays.asList(classes));
  }

  public List<Class<? extends IsIndexed>> getDeferredCommits() {
    synchronized (deferredCommits) {
      return new ArrayList<>(deferredCommits);
    }
  }

  private void record(Method method, Object[] args) {
    Entry entry = new Entry(method, args, keyOf(method.getName(), args), method.getName().endsWith("Deleted"));
    entries.add(entry);
    if (entry.key() != null) {
      switch (entry.key().type()) {
        case "AIP" -> touchedIndexIds.add(entry.key().id());
        case "REP" -> touchedIndexIds.add(IdUtils.getRepresentationId(entry.key().aipId(), entry.key().id()));
        case "FILE" -> {
          if (args.length > 0 && args[0] instanceof File file) {
            touchedIndexIds.add(IdUtils.getFileId(file));
          }
        }
        default -> {
          // only AIPs, representations and files are tracked
        }
      }
    }
  }

  /**
   * Consolidates the recorded notifications without changing their effect on
   * the observers:
   * <ul>
   * <li>notifications on an entity that is deleted later in the transaction are
   * dropped (the delete notification is kept);</li>
   * <li>exact duplicates (same method, same entity, same arguments) are reduced
   * to their last occurrence, as they are replayed with the final state
   * anyway;</li>
   * <li>optionally ({@code absorbPartialUpdates}), partial AIP notifications and
   * notifications on objects inside an AIP are dropped when the AIP itself is
   * fully re-indexed by aipCreated/aipUpdated.</li>
   * </ul>
   * The relative order of the remaining notifications is kept.
   */
  public List<Entry> consolidate(boolean absorbPartialUpdates) {
    List<Entry> all = getEntries();

    // 1. drop notifications on entities deleted later (walk backwards)
    Set<EntityKey> deletedKeys = new HashSet<>();
    Set<String> deletedAIPs = new HashSet<>();
    Set<String> deletedRepresentations = new HashSet<>();
    List<Entry> reversed = new ArrayList<>();
    for (int i = all.size() - 1; i >= 0; i--) {
      Entry entry = all.get(i);
      EntityKey key = entry.key();
      if (entry.delete()) {
        reversed.add(entry);
        if (key != null) {
          deletedKeys.add(key);
          if ("AIP".equals(key.type())) {
            deletedAIPs.add(key.id());
          } else if ("REP".equals(key.type())) {
            deletedRepresentations.add(key.aipId() + "/" + key.id());
          }
        }
      } else if (key == null || !isDeletedLater(key, deletedKeys, deletedAIPs, deletedRepresentations)) {
        reversed.add(entry);
      }
    }

    // 2. keep only the last occurrence of exact duplicates
    Set<String> seen = new HashSet<>();
    List<Entry> deduplicated = new ArrayList<>();
    for (Entry entry : reversed) {
      if (seen.add(entry.identity())) {
        deduplicated.add(entry);
      }
    }
    Collections.reverse(deduplicated);

    if (!absorbPartialUpdates) {
      return deduplicated;
    }

    // 3. drop notifications covered by a full re-index of the same AIP
    Set<String> fullyReindexedAIPs = deduplicated.stream()
      .filter(e -> AIP_FULL_REINDEX.contains(e.method().getName()) && e.key() != null).map(e -> e.key().id())
      .collect(Collectors.toSet());
    List<Entry> absorbed = deduplicated.stream().filter(e -> !isCoveredByFullReindex(e, fullyReindexedAIPs))
      .toList();

    // 4. one full re-index per AIP: the last one (aipUpdated if there is one, as it also removes documents of
    // objects that no longer exist)
    Set<String> withUpdate = absorbed.stream().filter(e -> "aipUpdated".equals(e.method().getName()))
      .map(e -> e.key().id()).collect(Collectors.toSet());
    Set<String> kept = new HashSet<>();
    List<Entry> result = new ArrayList<>();
    for (int i = absorbed.size() - 1; i >= 0; i--) {
      Entry entry = absorbed.get(i);
      String name = entry.method().getName();
      if (AIP_FULL_REINDEX.contains(name)) {
        String aipId = entry.key().id();
        boolean wanted = withUpdate.contains(aipId) ? "aipUpdated".equals(name) : true;
        if (!wanted || !kept.add(aipId)) {
          continue;
        }
      }
      result.add(entry);
    }
    Collections.reverse(result);
    return result;
  }

  private static boolean isDeletedLater(EntityKey key, Set<EntityKey> deletedKeys, Set<String> deletedAIPs,
    Set<String> deletedRepresentations) {
    if (deletedKeys.contains(key)) {
      return true;
    }
    if (key.aipId() != null && deletedAIPs.contains(key.aipId()) && !"AIP".equals(key.type())) {
      return true;
    }
    return key.representationId() != null
      && deletedRepresentations.contains(key.aipId() + "/" + key.representationId()) && !"REP".equals(key.type());
  }

  private static boolean isCoveredByFullReindex(Entry entry, Set<String> fullyReindexedAIPs) {
    String name = entry.method().getName();
    EntityKey key = entry.key();
    if (key == null) {
      return false;
    }
    if (AIP_PARTIAL_UPDATES.contains(name)) {
      return fullyReindexedAIPs.contains(key.id());
    }
    return AIP_CHILD_UPDATES.contains(name) && key.aipId() != null && fullyReindexedAIPs.contains(key.aipId());
  }

  /**
   * Replays notifications against observers. AIPs, representations and files
   * given as arguments are read again from {@code mainModel}, so observers see
   * the committed state; a notification whose object no longer exists is
   * skipped (a later delete notification takes care of it).
   *
   * @return the exceptions reported by the observers, plus failures to invoke
   *         them
   */
  public static List<ReturnWithExceptions<?, ?>> replay(List<Entry> toReplay, ModelService mainModel,
    List<ModelObserver> observers) {
    List<ReturnWithExceptions<?, ?>> results = new ArrayList<>();
    for (Entry entry : toReplay) {
      Optional<Object[]> args = resolveArguments(entry, mainModel);
      if (args.isEmpty()) {
        LOGGER.debug("Skipping {}: object no longer exists in the main storage", entry.method().getName());
        continue;
      }
      for (ModelObserver observer : observers) {
        try {
          Object result = entry.method().invoke(observer, args.get());
          if (result instanceof ReturnWithExceptions<?, ?> ret) {
            results.add(ret);
          }
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException e) {
          Throwable cause = e instanceof InvocationTargetException ite ? ite.getCause() : e;
          LOGGER.error("Error replaying {} on {}", entry.method().getName(), observer.getClass().getSimpleName(),
            cause);
          ReturnWithExceptions<Void, ModelObserver> failure = new ReturnWithExceptions<>();
          failure.add(cause instanceof Exception ex ? ex : new GenericException(cause.getMessage()));
          results.add(failure);
        }
      }
    }
    return results;
  }

  private static Optional<Object[]> resolveArguments(Entry entry, ModelService mainModel) {
    Object[] resolved = entry.args().clone();
    try {
      for (int i = 0; i < resolved.length; i++) {
        if (resolved[i] instanceof AIP aip) {
          resolved[i] = mainModel.retrieveAIP(aip.getId());
        } else if (resolved[i] instanceof Representation rep) {
          resolved[i] = mainModel.retrieveRepresentation(rep.getAipId(), rep.getId());
        } else if (resolved[i] instanceof File file) {
          resolved[i] = mainModel.retrieveFile(file.getAipId(), file.getRepresentationId(), file.getPath(),
            file.getId());
        }
      }
    } catch (NotFoundException e) {
      return Optional.empty();
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException e) {
      LOGGER.warn("Could not read {} from the main storage, replaying with the recorded object",
        entry.method().getName(), e);
      return Optional.of(entry.args());
    }
    return Optional.of(resolved);
  }

  /** Ids of the AIPs referred to by the recorded notifications. */
  public Set<String> getTouchedAIPIds() {
    return getEntries().stream().map(Entry::key).filter(k -> k != null && k.aipId() != null).map(EntityKey::aipId)
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private static EntityKey keyOf(String method, Object[] args) {
    if (args.length == 0) {
      return null;
    }
    Object first = args[0];
    if (first instanceof AIP aip) {
      return new EntityKey("AIP", aip.getId(), aip.getId(), null);
    } else if (first instanceof Representation rep) {
      return new EntityKey("REP", rep.getId(), rep.getAipId(), rep.getId());
    } else if (first instanceof File file) {
      return new EntityKey("FILE", fileKey(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId()),
        file.getAipId(), file.getRepresentationId());
    } else if (first instanceof DescriptiveMetadata dm) {
      return new EntityKey("DM", dm.getRepresentationId() + "/" + dm.getId(), dm.getAipId(), dm.getRepresentationId());
    } else if (first instanceof PreservationMetadata pm) {
      return new EntityKey("PM", pm.getId(), pm.getAipId(), pm.getRepresentationId());
    } else if (first instanceof OtherMetadata om) {
      return new EntityKey("OM", om.getId(), om.getAipId(), om.getRepresentationId());
    } else if (first instanceof DIPFile dipFile) {
      return new EntityKey("DIPFILE", dipFile.getDipId() + "/" + dipFile.getPath() + "/" + dipFile.getId(),
        null, null);
    } else if (first instanceof DIP dip) {
      return new EntityKey("DIP", dip.getId(), null, null);
    } else if (first instanceof IsRODAObject object) {
      return new EntityKey(object.getClass().getSimpleName(), object.getId(), null, null);
    }
    return keyOfDelete(method, args);
  }

  private static EntityKey keyOfDelete(String method, Object[] args) {
    return switch (method) {
      case "aipDeleted" -> new EntityKey("AIP", (String) args[0], (String) args[0], null);
      case "representationDeleted" -> new EntityKey("REP", (String) args[1], (String) args[0], (String) args[1]);
      case "fileDeleted" -> {
        @SuppressWarnings("unchecked")
        List<String> path = (List<String>) args[2];
        yield new EntityKey("FILE", fileKey((String) args[0], (String) args[1], path, (String) args[3]),
          (String) args[0], (String) args[1]);
      }
      case "descriptiveMetadataDeleted" -> new EntityKey("DM", args[1] + "/" + args[2], (String) args[0],
        (String) args[1]);
      case "dipDeleted" -> new EntityKey("DIP", (String) args[0], null, null);
      case "dipFileDeleted" -> new EntityKey("DIPFILE", args[0] + "/" + args[1] + "/" + args[2], null, null);
      case "riskDeleted" -> new EntityKey("Risk", (String) args[0], null, null);
      case "riskIncidenceDeleted" -> new EntityKey("RiskIncidence", (String) args[0], null, null);
      case "representationInformationDeleted" -> new EntityKey("RepresentationInformation", (String) args[0], null,
        null);
      case "notificationDeleted" -> new EntityKey("Notification", (String) args[0], null, null);
      case "disposalConfirmationDeleted" -> new EntityKey("DisposalConfirmation", (String) args[0], null, null);
      case "disposalScheduleDeleted" -> new EntityKey("DisposalSchedule", (String) args[0], null, null);
      case "disposalHoldDeleted" -> new EntityKey("DisposalHold", (String) args[0], null, null);
      case "disposalRuleDeleted" -> new EntityKey("DisposalRule", (String) args[0], null, null);
      default -> null;
    };
  }

  private static String fileKey(String aipId, String representationId, List<String> path, String fileId) {
    return aipId + "/" + representationId + "/" + (path == null ? "" : String.join("/", path)) + "/" + fileId;
  }

  private static String identityOf(Object arg) {
    if (arg == null) {
      return "null";
    }
    EntityKey key = keyOf("", new Object[] {arg});
    if (key != null) {
      return key.type() + ":" + key.aipId() + ":" + key.id();
    } else if (arg instanceof LiteRODAObject lite) {
      return "lite:" + lite.getInfo();
    }
    return String.valueOf(arg);
  }
}
