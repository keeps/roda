/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.migration.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.URNUtils;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.migration.MigrationAction;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.premis.v3.File;
import gov.loc.premis.v3.ObjectIdentifierComplexType;

public class PreservationMetadataFileToVersion2 implements MigrationAction<PreservationMetadata> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PreservationMetadataFileToVersion2.class);

  @Override
  public void migrate(StorageService storage) {
    try (
      CloseableIterable<Resource> aips = storage.listResourcesUnderDirectory(ModelUtils.getAIPContainerPath(), false)) {

      for (Resource aip : aips) {
        try (CloseableIterable<Resource> representations = storage.listResourcesUnderDirectory(
          ModelUtils.getRepresentationsContainerPath(aip.getStoragePath().getName()), false)) {

          for (Resource representation : representations) {
            StoragePath pmPath = DefaultStoragePath.parse(representation.getStoragePath(),
              RodaConstants.STORAGE_DIRECTORY_METADATA, RodaConstants.STORAGE_DIRECTORY_PRESERVATION);

            try (CloseableIterable<Resource> pms = storage.listResourcesUnderDirectory(pmPath, true)) {
              for (Resource pm : pms) {
                if (!pm.isDirectory() && pm instanceof Binary && pm.getStoragePath().getName().startsWith(URNUtils
                  .getPremisPrefix(PreservationMetadataType.FILE, RODAInstanceUtils.getLocalInstanceIdentifier()))) {
                  Binary binary = (Binary) pm;
                  migrate(storage, binary);
                }
              }
            } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
              | IOException e) {
              LOGGER.warn("Could not find preservation metadata files", e);
            }
          }

        } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
          | IOException e) {
          LOGGER.warn("Could not find representations", e);
        }
      }
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
      | IOException e) {
      LOGGER.warn("Could not find AIPs", e);
    }
  }

  private void migrate(StorageService storage, Binary binary) {
    try (InputStream inputStream = binary.getContent().createInputStream()) {
      StoragePath oldStoragePath = binary.getStoragePath();

      File file = PremisV3Utils.binaryToFile(inputStream);
      String originalName = file.getOriginalName().getValue();
      String value = null;

      for (ObjectIdentifierComplexType objectIdentifier : file.getObjectIdentifier()) {
        if (RodaConstants.PREMIS_IDENTIFIER_TYPE_URN.equals(objectIdentifier.getObjectIdentifierType().getValue())) {
          value = objectIdentifier.getObjectIdentifierValue();
          value = value.substring(0, value.lastIndexOf(RodaConstants.URN_SEPARATOR) + 1);
          value += originalName;
          objectIdentifier.setObjectIdentifierValue(value);
        }
      }

      List<String> pathList = new ArrayList<>(oldStoragePath.asList());

      if (value != null) {
        pathList.remove(pathList.size() - 1);
        pathList.add(value + RodaConstants.PREMIS_SUFFIX);
      }

      StoragePath newStoragePath = DefaultStoragePath.parse(pathList);
      storage.move(storage, oldStoragePath, newStoragePath);

      ContentPayload newPremis = PremisV3Utils.fileToBinary(file);
      boolean asReference = false;
      boolean createIfNotExists = false;
      storage.updateBinaryContent(newStoragePath, newPremis, asReference, createIfNotExists);
    } catch (GenericException | IOException | ValidationException | NotFoundException | RequestNotValidException
      | AuthorizationDeniedException | AlreadyExistsException e) {
      LOGGER.error("Could not migrate preservation metadata file {}", binary.getStoragePath(), e);
    }
  }

  @Override
  public boolean isToVersionValid(int toVersion) {
    return toVersion == 2;
  }

}
