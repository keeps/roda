package org.roda.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;

import pt.gov.dgarq.roda.core.common.RodaConstants;

public class StorageTestUtils {

  public static StoragePath generateRandomContainerStoragePath() throws StorageServiceException {
    return DefaultStoragePath.parse(UUID.randomUUID().toString());
  }

  public static StoragePath generateRandomResourceStoragePathUnder(StoragePath basePath)
    throws StorageServiceException {
    return DefaultStoragePath.parse(basePath.asString(), UUID.randomUUID().toString());
  }

  public static Map<String, Set<String>> generateRandomMetadata() {
    int metadataCount = new Random().nextInt(10) + 1;
    Map<String, Set<String>> metadata = new HashMap<String, Set<String>>();
    for (int i = 0; i < metadataCount; i++) {
      Set<String> value = new HashSet<String>();
      int listCount = new Random().nextInt(3) + 1;
      for (int j = 0; j < listCount; j++) {
        value.add(RandomStringUtils.randomAlphabetic(5));
      }
      metadata.put(RandomStringUtils.randomAlphabetic(5), value);
    }
    return metadata;
  }

  public static void populate(StorageService storage, StoragePath basepath) throws StorageServiceException {
    // create 3 directories with 3 sub-directories each and 3 binaries under
    // each sub-directory
    int highLevelSize = 3;
    int mediumLevelSize = 3;
    int lowLevelSize = 3;

    for (int i = 0; i < highLevelSize; i++) {
      final StoragePath directoryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(basepath);
      final Map<String, Set<String>> directoryMetadata = StorageTestUtils.generateRandomMetadata();
      storage.createDirectory(directoryStoragePath, directoryMetadata);

      for (int j = 0; j < mediumLevelSize; j++) {
        final StoragePath subDirectoryStoragePath = StorageTestUtils
          .generateRandomResourceStoragePathUnder(directoryStoragePath);
        final Map<String, Set<String>> subDirectoryMetadata = StorageTestUtils.generateRandomMetadata();
        storage.createDirectory(subDirectoryStoragePath, subDirectoryMetadata);

        for (int k = 0; k < lowLevelSize; k++) {
          final StoragePath binaryStoragePath = StorageTestUtils
            .generateRandomResourceStoragePathUnder(subDirectoryStoragePath);
          final Map<String, Set<String>> binaryMetadata = StorageTestUtils.generateRandomMetadata();
          final ContentPayload payload = new RandomMockContentPayload();
          storage.createBinary(binaryStoragePath, binaryMetadata, payload, false);
        }
      }
    }
  }

  public static void testEntityEqualRecursively(StorageService sourceStorage, StoragePath sourceEntityStoragePath,
    StorageService targetStorage, StoragePath targetEntityStoragePath) throws StorageServiceException, IOException {
    testEntityEqualRecursively(sourceStorage, sourceEntityStoragePath, targetStorage, targetEntityStoragePath, false);
  }

  public static void testEntityEqualRecursively(StorageService sourceStorage, StoragePath sourceEntityStoragePath,
    StorageService targetStorage, StoragePath targetEntityStoragePath, boolean ignoreDateModified)
      throws StorageServiceException, IOException {

    assertEquals(sourceEntityStoragePath.isFromAContainer(), targetEntityStoragePath.isFromAContainer());

    Class<? extends Entity> sourceEntity = sourceStorage.getEntity(sourceEntityStoragePath);
    Class<? extends Entity> targetEntity = targetStorage.getEntity(targetEntityStoragePath);

    Iterable<Resource> sourceResourceList = null;
    if (Container.class.isAssignableFrom(sourceEntity) && Container.class.isAssignableFrom(targetEntity)) {
      Container sourceContainer = sourceStorage.getContainer(sourceEntityStoragePath);
      Container targetContainer = targetStorage.getContainer(targetEntityStoragePath);

      Map<String, Set<String>> sourceMetadata = sourceContainer.getMetadata();
      Map<String, Set<String>> targetMetadata = targetContainer.getMetadata();
      if (ignoreDateModified) {
        sourceMetadata.remove(RodaConstants.STORAGE_META_DATE_MODIFIED);
        targetMetadata.remove(RodaConstants.STORAGE_META_DATE_MODIFIED);
      }
      assertEquals(sourceMetadata, targetMetadata);

      sourceResourceList = sourceStorage.listResourcesUnderContainer(sourceEntityStoragePath);

    } else if (Directory.class.isAssignableFrom(sourceEntity) && Directory.class.isAssignableFrom(targetEntity)) {

      Directory sourceDirectory = sourceStorage.getDirectory(sourceEntityStoragePath);
      Directory targetDirectory = targetStorage.getDirectory(targetEntityStoragePath);

      Map<String, Set<String>> sourceMetadata = sourceDirectory.getMetadata();
      Map<String, Set<String>> targetMetadata = sourceDirectory.getMetadata();
      if (ignoreDateModified) {
        sourceMetadata.remove(RodaConstants.STORAGE_META_DATE_MODIFIED);
        targetMetadata.remove(RodaConstants.STORAGE_META_DATE_MODIFIED);
      }
      assertEquals(sourceMetadata, targetMetadata);

      assertEquals(sourceDirectory.isDirectory(), targetDirectory.isDirectory());

      sourceResourceList = sourceStorage.listResourcesUnderDirectory(sourceEntityStoragePath);

    } else if (Binary.class.isAssignableFrom(sourceEntity) && Binary.class.isAssignableFrom(targetEntity)) {

      Binary sourceBinary = sourceStorage.getBinary(sourceEntityStoragePath);
      Binary targetBinary = targetStorage.getBinary(targetEntityStoragePath);

      Map<String, Set<String>> sourceMetadata = sourceBinary.getMetadata();
      Map<String, Set<String>> targetMetadata = targetBinary.getMetadata();
      if (ignoreDateModified) {
        sourceMetadata.remove(RodaConstants.STORAGE_META_DATE_MODIFIED);
        targetMetadata.remove(RodaConstants.STORAGE_META_DATE_MODIFIED);
      }
      assertEquals(sourceMetadata, targetMetadata);

      assertEquals(sourceBinary.isDirectory(), targetBinary.isDirectory());
      assertEquals(sourceBinary.getContentDigest(), targetBinary.getContentDigest());
      assertEquals(sourceBinary.getSizeInBytes(), targetBinary.getSizeInBytes());
      assertEquals(sourceBinary.isReference(), targetBinary.isReference());
      assertTrue(IOUtils.contentEquals(sourceBinary.getContent().createInputStream(),
        targetBinary.getContent().createInputStream()));

    } else {
      fail("Compared entities are not of the same type. source=" + sourceEntity + " target=" + targetEntity);
    }

    // Recursive call
    if (sourceResourceList != null) {
      for (Resource r : sourceResourceList) {
        StoragePath targetResourceStoragePath = DefaultStoragePath.parse(targetEntityStoragePath,
          r.getStoragePath().getName());
        testEntityEqualRecursively(sourceStorage, r.getStoragePath(), targetStorage, targetResourceStoragePath,
          ignoreDateModified);
      }
    }

  }
}
