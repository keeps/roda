package org.roda.core.transactions;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.entity.transaction.OperationState;
import org.roda.core.entity.transaction.OperationType;
import org.roda.core.entity.transaction.TransactionalStoragePathOperationLog;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.transaction.ConsolidatedOperation;
import org.roda.core.transaction.TransactionLogConsolidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class TransactionLogConsolidatorTest extends AbstractTestNGSpringContextTests {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionLogConsolidatorTest.class);

  private TransactionalStoragePathOperationLog buildLog(String path, OperationType type, OperationState state,
    String version) {
    TransactionalStoragePathOperationLog log = new TransactionalStoragePathOperationLog(path, type, version);
    log.setOperationState(state);
    return log;
  }

  @Test
  public void testCreateFollowedByDeleteResultsInNoOperation() throws RODAException {
    List<TransactionalStoragePathOperationLog> logs = List.of(
      buildLog("x/y/z.txt", OperationType.CREATE, OperationState.SUCCESS, null),
      buildLog("x/y/z.txt", OperationType.DELETE, OperationState.SUCCESS, null));

    Map<StoragePath, List<ConsolidatedOperation>> result = TransactionLogConsolidator.consolidateLogs(logs);
    assertTrue(result.values().iterator().next().isEmpty());
  }

  @Test
  public void testCreateFollowedByUpdateAndDeleteResultsInNoOperations() throws RODAException {
    List<TransactionalStoragePathOperationLog> logs = List.of(
      buildLog("a/b/c.txt", OperationType.CREATE, OperationState.SUCCESS, null),
      buildLog("a/b/c.txt", OperationType.UPDATE, OperationState.SUCCESS, null),
      buildLog("a/b/c.txt", OperationType.DELETE, OperationState.SUCCESS, null));

    Map<StoragePath, List<ConsolidatedOperation>> storagePathListMap = TransactionLogConsolidator.consolidateLogs(logs);

    for (Map.Entry<StoragePath, List<ConsolidatedOperation>> storagePathListEntry : storagePathListMap.entrySet()) {
      System.out
        .println("Storage Path: " + storagePathListEntry.getKey() + ", Operations: " + storagePathListEntry.getValue());
      // operations should be empty
      assertTrue(storagePathListEntry.getValue().isEmpty(),
        "Consolidated operations should be empty for path: " + storagePathListEntry.getKey());
    }
  }

  @Test
  public void testConsolidatesConsecutiveUpdateOperationsIntoSingleUpdate() throws RODAException {
    List<TransactionalStoragePathOperationLog> logs = List.of(
      buildLog("a/b/file.txt", OperationType.UPDATE, OperationState.SUCCESS, null),
      buildLog("a/b/file.txt", OperationType.UPDATE, OperationState.SUCCESS, null),
      buildLog("a/b/file.txt", OperationType.UPDATE, OperationState.SUCCESS, null));

    Map<StoragePath, List<ConsolidatedOperation>> result = TransactionLogConsolidator.consolidateLogs(logs);

    assertEquals(1, result.size());
    List<ConsolidatedOperation> operations = result.values().iterator().next();

    assertEquals(1, operations.size());
    assertEquals(OperationType.UPDATE, operations.get(0).operationType());
  }

  @Test
  public void testUpdateAfterCreateIsIgnoredInConsolidation() throws RODAException {
    List<TransactionalStoragePathOperationLog> logs = List.of(
      buildLog("a/b/file.txt", OperationType.CREATE, OperationState.SUCCESS, null),
      buildLog("a/b/file.txt", OperationType.UPDATE, OperationState.SUCCESS, null));

    Map<StoragePath, List<ConsolidatedOperation>> result = TransactionLogConsolidator.consolidateLogs(logs);

    List<ConsolidatedOperation> operations = result.values().iterator().next();
    assertEquals(1, operations.size());
    assertEquals(OperationType.CREATE, operations.get(0).operationType());
  }

  @Test
  public void testDeleteRemovesChildOperationsFromConsolidation() throws RODAException {
    List<TransactionalStoragePathOperationLog> logs = List.of(
      buildLog("a/b/c", OperationType.UPDATE, OperationState.SUCCESS, null),
      buildLog("a/b/c/d", OperationType.CREATE, OperationState.SUCCESS, null),
      buildLog("a/b", OperationType.DELETE, OperationState.SUCCESS, null));

    Map<StoragePath, List<ConsolidatedOperation>> result = TransactionLogConsolidator.consolidateLogs(logs);

    assertEquals(1, result.size());
    StoragePath onlyPath = result.keySet().iterator().next();
    assertEquals(DefaultStoragePath.parse(List.of("a", "b")), onlyPath);
  }

  @Test
  public void testDeleteDoesNotRemoveChildOperationsFromConsolidation() throws RODAException {
    List<TransactionalStoragePathOperationLog> logs = List.of(
      buildLog("a/b", OperationType.DELETE, OperationState.SUCCESS, null),
      buildLog("a/b/c", OperationType.UPDATE, OperationState.SUCCESS, null),
      buildLog("a/b/c/d", OperationType.CREATE, OperationState.SUCCESS, null));

    Map<StoragePath, List<ConsolidatedOperation>> result = TransactionLogConsolidator.consolidateLogs(logs);

    assertEquals(3, result.size());

    assertTrue(result.containsKey(DefaultStoragePath.parse(List.of("a", "b"))));
    assertTrue(result.containsKey(DefaultStoragePath.parse(List.of("a", "b", "c"))));
    assertTrue(result.containsKey(DefaultStoragePath.parse(List.of("a", "b", "c", "d"))));

    List<ConsolidatedOperation> abOps = result.get(DefaultStoragePath.parse(List.of("a", "b")));
    assertEquals(1, abOps.size());
    assertEquals(OperationType.DELETE, abOps.getFirst().operationType());

    List<ConsolidatedOperation> abcOps = result.get(DefaultStoragePath.parse(List.of("a", "b", "c")));
    assertEquals(1, abcOps.size());
    assertEquals(OperationType.UPDATE, abcOps.getFirst().operationType());

    List<ConsolidatedOperation> abcdOps = result.get(DefaultStoragePath.parse(List.of("a", "b", "c", "d")));
    assertEquals(1, abcdOps.size());
    assertEquals(OperationType.CREATE, abcdOps.getFirst().operationType());
  }

}
