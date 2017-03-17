/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.util.IdUtils;
import org.testng.Assert;

public class StorageTestUtils {

  private StorageTestUtils() {
    // do nothing
  }

  public static StoragePath generateRandomContainerStoragePath() throws RequestNotValidException {
    return DefaultStoragePath.parse(IdUtils.createUUID());
  }

  public static StoragePath generateRandomResourceStoragePathUnder(StoragePath basePath)
    throws RequestNotValidException {
    List<String> path = new ArrayList<>(basePath.asList());
    path.add(IdUtils.createUUID());

    return DefaultStoragePath.parse(path);
  }

  public static void populate(StorageService storage, StoragePath basepath) throws AlreadyExistsException,
    GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    // create 3 directories with 3 sub-directories each and 3 binaries under
    // each sub-directory
    int highLevelSize = 3;
    int mediumLevelSize = 3;
    int lowLevelSize = 3;

    for (int i = 0; i < highLevelSize; i++) {
      final StoragePath directoryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(basepath);
      storage.createDirectory(directoryStoragePath);

      for (int j = 0; j < mediumLevelSize; j++) {
        final StoragePath subDirectoryStoragePath = StorageTestUtils
          .generateRandomResourceStoragePathUnder(directoryStoragePath);
        storage.createDirectory(subDirectoryStoragePath);

        for (int k = 0; k < lowLevelSize; k++) {
          final StoragePath binaryStoragePath = StorageTestUtils
            .generateRandomResourceStoragePathUnder(subDirectoryStoragePath);
          final ContentPayload payload = new RandomMockContentPayload();
          storage.createBinary(binaryStoragePath, payload, false);
        }
      }
    }
  }

  public static void testEntityEqualRecursively(StorageService sourceStorage, StoragePath sourceEntityStoragePath,
    StorageService targetStorage, StoragePath targetEntityStoragePath)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException, IOException {

    assertEquals(sourceEntityStoragePath.isFromAContainer(), targetEntityStoragePath.isFromAContainer());

    Class<? extends Entity> sourceEntity = sourceStorage.getEntity(sourceEntityStoragePath);
    Class<? extends Entity> targetEntity = targetStorage.getEntity(targetEntityStoragePath);

    Iterable<Resource> sourceResourceList = null;
    if (Container.class.isAssignableFrom(sourceEntity) && Container.class.isAssignableFrom(targetEntity)) {
      sourceResourceList = sourceStorage.listResourcesUnderContainer(sourceEntityStoragePath, false);

    } else if (Directory.class.isAssignableFrom(sourceEntity) && Directory.class.isAssignableFrom(targetEntity)) {

      Directory sourceDirectory = sourceStorage.getDirectory(sourceEntityStoragePath);
      Directory targetDirectory = targetStorage.getDirectory(targetEntityStoragePath);

      assertEquals(sourceDirectory.isDirectory(), targetDirectory.isDirectory());

      sourceResourceList = sourceStorage.listResourcesUnderDirectory(sourceEntityStoragePath, false);

    } else if (Binary.class.isAssignableFrom(sourceEntity) && Binary.class.isAssignableFrom(targetEntity)) {

      Binary sourceBinary = sourceStorage.getBinary(sourceEntityStoragePath);
      Binary targetBinary = targetStorage.getBinary(targetEntityStoragePath);

      assertEquals(sourceBinary.isDirectory(), targetBinary.isDirectory());
      assertEquals(sourceBinary.getContentDigest(), targetBinary.getContentDigest());
      assertEquals(sourceBinary.getSizeInBytes(), targetBinary.getSizeInBytes());
      assertEquals(sourceBinary.isReference(), targetBinary.isReference());
      assertTrue(IOUtils.contentEquals(sourceBinary.getContent().createInputStream(),
        targetBinary.getContent().createInputStream()));

    } else {
      Assert.fail("Compared entities are not of the same type. source=" + sourceEntity + " target=" + targetEntity);
    }

    // Recursive call
    if (sourceResourceList != null) {
      for (Resource r : sourceResourceList) {
        StoragePath targetResourceStoragePath = DefaultStoragePath.parse(targetEntityStoragePath,
          r.getStoragePath().getName());
        testEntityEqualRecursively(sourceStorage, r.getStoragePath(), targetStorage, targetResourceStoragePath);
      }
    }

  }
}
