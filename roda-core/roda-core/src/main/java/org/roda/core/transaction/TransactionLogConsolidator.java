package org.roda.core.transaction;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.entity.transaction.OperationState;
import org.roda.core.entity.transaction.OperationType;
import org.roda.core.entity.transaction.TransactionalStoragePathOperationLog;
import org.roda.core.storage.DefaultStoragePath;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 *
 *         Utility class responsible for consolidating logs of storage path
 *         operations performed as part of transactions. It processes and
 *         simplifies sequences of operations while preserving meaningful state
 *         transitions.
 */
public class TransactionLogConsolidator {

  /**
   * Consolidates a list of transactional storage path operation logs by: 1.
   * Grouping operations per storage path, 2. Applying consolidation rules to each
   * path's operation sequence, 3. Identifying all paths whose last operation is
   * DELETE, 4. Removing any paths that are descendants of a deleted path.
   *
   * @param transactionsLogs
   *          the list of storage path operation logs to be consolidated
   * @return a map from each storage path to its final, consolidated list of
   *         operations
   * @throws RequestNotValidException
   *           if any storage path in the logs cannot be parsed or is otherwise
   *           invalid
   */
  public static Map<StoragePathVersion, List<ConsolidatedOperation>> consolidateLogs(
    List<TransactionalStoragePathOperationLog> transactionsLogs) throws RequestNotValidException {
    Map<StoragePathVersion, List<ConsolidatedOperation>> groupByStoragePath = groupByStoragePath(transactionsLogs);
    Map<StoragePathVersion, List<ConsolidatedOperation>> consolidateTransaction = consolidateTransaction(
      groupByStoragePath);
    Set<StoragePathVersion> deletePaths = findDeletePaths(consolidateTransaction);
    removeChildrenOfDeleted(consolidateTransaction, deletePaths);
    return consolidateTransaction;
  }

  /**
   * Groups operation logs by their storage paths, filtering only successful
   * operations.
   *
   * @param logs
   *          the list of transaction logs
   * @return a map of storage paths to lists of their corresponding operations
   * @throws RequestNotValidException
   *           if the storage path cannot be parsed
   */
  private static Map<StoragePathVersion, List<ConsolidatedOperation>> groupByStoragePath(
    List<TransactionalStoragePathOperationLog> logs) throws RequestNotValidException {
    Map<StoragePathVersion, List<ConsolidatedOperation>> map = new LinkedHashMap<>();
    for (TransactionalStoragePathOperationLog log : logs) {
      if (log.getOperationState() == OperationState.SUCCESS) {
        OperationType operationType = log.getOperationType();
        String version = log.getVersion();
        String previousVersionId = log.getPreviousVersion();
        LocalDateTime updatedAt = log.getUpdatedAt();

        DefaultStoragePath storagePath = DefaultStoragePath
          .parse(StreamSupport.stream(Paths.get(log.getStoragePath()).spliterator(), false).map(Path::toString)
            .collect(Collectors.toList()));

        StoragePathVersion storagePathVersion = new StoragePathVersion(storagePath, version);
        map.computeIfAbsent(storagePathVersion, k -> new ArrayList<>())
          .add(new ConsolidatedOperation(operationType, updatedAt, previousVersionId));
      }
    }
    return map;
  }

  /**
   * Applies transaction consolidation rules to all storage paths in the given
   * map. For each storage path, its list of operations is simplified using a
   * custom set of rules that eliminate redundant or canceling operations.
   *
   * @param grouped
   *          a map where each key is a storage path and the value is a list of
   *          its operations
   * @return a new map with the same keys, but with refined and consolidated
   *         operations
   */
  private static Map<StoragePathVersion, List<ConsolidatedOperation>> consolidateTransaction(
    Map<StoragePathVersion, List<ConsolidatedOperation>> grouped) {
    Map<StoragePathVersion, List<ConsolidatedOperation>> consolidate = new LinkedHashMap<>();
    for (var entry : grouped.entrySet()) {
      consolidate.put(entry.getKey(), consolidateTransaction(entry.getValue()));
    }
    return consolidate;
  }

  /**
   * Consolidates a list of operations for a single storage path. The goal is to
   * remove redundant, conflicting, or canceling operations.
   *
   * Rules applied: - READ operations are ignored. - CREATE is added if it does
   * not immediately repeat (e.g., CREATE followed by CREATE is collapsed). -
   * UPDATE is skipped if it immediately follows a CREATE (as the creation already
   * implies the final state). - DELETE is always added. - Repeated UPDATEs are
   * reduced to one. - Repeated DELETEs are reduced to one. - A CREATE immediately
   * followed by DELETE is removed (they cancel each other). - An UPDATE
   * immediately followed by DELETE removes the UPDATE.
   *
   * @param consolidatedOperations
   *          the original list of operations for one storage path
   * @return a new list with consolidated operations
   */
  private static List<ConsolidatedOperation> consolidateTransaction(
    List<ConsolidatedOperation> consolidatedOperations) {
    List<ConsolidatedOperation> result = new ArrayList<>();

    for (ConsolidatedOperation consolidatedOperation : consolidatedOperations) {
      OperationType op = consolidatedOperation.operationType();
      LocalDateTime updatedAt = consolidatedOperation.updatedAt();
      String previousVersion = consolidatedOperation.previousVersionId();
      switch (op) {
        case READ:
          break;

        case CREATE:
          if (result.isEmpty() || result.getLast().operationType() != OperationType.CREATE) {
            result.add(new ConsolidatedOperation(OperationType.CREATE, updatedAt, null));
          }
          break;

        case UPDATE:
          if (!result.isEmpty()) {
            OperationType last = result.getLast().operationType();
            if (last == OperationType.CREATE) {
              break;
            }
          }
          result.add(new ConsolidatedOperation(OperationType.UPDATE, updatedAt, previousVersion));
          break;

        case CREATE_OR_UPDATE:
          if (result.isEmpty()) {
            result.add(new ConsolidatedOperation(OperationType.CREATE_OR_UPDATE, updatedAt, previousVersion));
          } else {
            OperationType last = result.getLast().operationType();
            if (last == OperationType.CREATE || last == OperationType.UPDATE
              || last == OperationType.CREATE_OR_UPDATE) {
              if (result.getLast().previousVersionId() != null) {
                previousVersion = result.getLast().previousVersionId();
              }
              result.removeLast();
            }
            result.add(new ConsolidatedOperation(OperationType.CREATE_OR_UPDATE, updatedAt, previousVersion));
          }
          break;

        case DELETE:
          result.add(new ConsolidatedOperation(OperationType.DELETE, updatedAt, null));
          break;
      }
    }

    for (int i = 0; i < result.size() - 1;) {
      OperationType curr = result.get(i).operationType();
      OperationType next = result.get(i + 1).operationType();
      String currPreviousVersion = result.get(i).previousVersionId();

      if (curr == OperationType.UPDATE && next == OperationType.UPDATE) {
        result.remove(i);
        if (currPreviousVersion != null) {
          result.add(i,
            new ConsolidatedOperation(OperationType.UPDATE, result.get(i).updatedAt(), currPreviousVersion));
          result.remove(i + 1);
        }
        continue;
      }

      if (curr == OperationType.DELETE && next == OperationType.DELETE) {
        result.remove(i + 1);
        continue;
      }

      if ((curr == OperationType.CREATE || curr == OperationType.CREATE_OR_UPDATE) && next == OperationType.DELETE) {
        result.remove(i);
        result.remove(i);
        if (i > 0)
          i--;
        continue;
      }

      if (curr == OperationType.UPDATE && next == OperationType.DELETE) {
        result.remove(i);
        continue;
      }

      if (curr == OperationType.CREATE_OR_UPDATE && next == OperationType.CREATE_OR_UPDATE) {
        result.remove(i);
        if (currPreviousVersion != null) {
          result.remove(i);
          result.add(i,
            new ConsolidatedOperation(OperationType.UPDATE, result.get(i).updatedAt(), currPreviousVersion));
        }
        continue;
      }

      i++;
    }

    return result;
  }

  /**
   * Identifies all storage paths whose last operation is a DELETE.
   *
   * @param refined
   *          a map of storage paths and their refined operations
   * @return a set of storage paths marked for deletion
   */
  private static Set<StoragePathVersion> findDeletePaths(Map<StoragePathVersion, List<ConsolidatedOperation>> refined) {
    Set<StoragePathVersion> set = new HashSet<>();
    refined.forEach((key, consolidateOperations) -> {
      if (!consolidateOperations.isEmpty()) {
        ConsolidatedOperation consolidatedOperation = consolidateOperations.getLast();
        if (consolidatedOperation.operationType() == OperationType.DELETE) {
          set.add(key);
        }
      }
    });
    return set;
  }

  /**
   * Removes storage paths that are descendants of paths marked for deletion only
   * if the DELETE operation on the ancestor occurred after the child's last
   * operation.
   *
   * @param refined
   *          the map of refined storage paths and their operations
   * @param deletePaths
   *          the set of paths that are deleted
   */
  private static void removeChildrenOfDeleted(Map<StoragePathVersion, List<ConsolidatedOperation>> refined,
    Set<StoragePathVersion> deletePaths) {

    Map<StoragePathVersion, LocalDateTime> deleteTimes = new HashMap<>();
    for (StoragePathVersion deletePath : deletePaths) {
      List<ConsolidatedOperation> ops = refined.get(deletePath);
      if (ops != null && !ops.isEmpty()) {
        LocalDateTime deleteTime = ops.getLast().updatedAt();
        deleteTimes.put(deletePath, deleteTime);
      }
    }

    refined.entrySet().removeIf(entry -> {
      StoragePathVersion storagePathVersion = entry.getKey();
      List<ConsolidatedOperation> ops = entry.getValue();
      if (ops.isEmpty())
        return false;

      ConsolidatedOperation lastOp = ops.getLast();

      if (lastOp.operationType() != OperationType.DELETE) {
        LocalDateTime lastOpTime = lastOp.updatedAt();

        return deleteTimes.entrySet().stream()
          .anyMatch(del -> isAncestor(del.getKey(), storagePathVersion) && del.getValue().isAfter(lastOpTime));
      }
      return false;
    });
  }

  /**
   * Checks whether the given ancestor storage path is a prefix of the given child
   * path.
   *
   * @param ancestor
   *          the potential ancestor path
   * @param child
   *          the path to be checked
   * @return true if ancestor is a prefix of child, false otherwise
   */
  private static boolean isAncestor(StoragePathVersion ancestor, StoragePathVersion child) {
    if (ancestor == null || child == null) {
      return false;
    }
    String ancestorVersion = ancestor.version();
    String childVersion = child.version();

    if (!Objects.equals(ancestorVersion, childVersion)) {
      return false;
    }

    List<String> directoryPath = ancestor.storagePath().asList();
    List<String> childPath = child.storagePath().asList();
    if (directoryPath.size() >= childPath.size()) {
      return false;
    }
    for (int i = 0; i < directoryPath.size(); i++) {
      if (!directoryPath.get(i).equals(childPath.get(i))) {
        return false;
      }
    }
    return true;
  }
}
