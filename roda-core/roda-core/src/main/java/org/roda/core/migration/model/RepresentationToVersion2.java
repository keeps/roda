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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Date;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.RepresentationState;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.migration.MigrationAction;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepresentationToVersion2 implements MigrationAction<Representation> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RepresentationToVersion2.class);

  @Override
  public void migrate(ModelService model) {
    try (
      CloseableIterable<Resource> aips = model.listResourcesUnderDirectory(ModelUtils.getAIPContainerPath(), false)) {
      for (Resource aipResorce : aips) {
        try {
          StoragePath aipJsonPath = DefaultStoragePath.parse(aipResorce.getStoragePath(),
            RodaConstants.STORAGE_AIP_METADATA_FILENAME);
          Binary aipJson = model.getBinary(aipJsonPath);
          InputStream inputStream = aipJson.getContent().createInputStream();

          AIP aip = JsonUtils.getObjectFromJson(inputStream, AIP.class);
          for (Representation representation : aip.getRepresentations()) {
            Path representationPath = model.getDirectAccess(representation).getPath();

            BasicFileAttributes attr = Files.readAttributes(representationPath, BasicFileAttributes.class);
            Date createDate = new Date(attr.creationTime().toMillis());
            Date updateDate = new Date(attr.lastModifiedTime().toMillis());

            representation.setCreatedOn(createDate);
            representation.setCreatedBy(aip.getCreatedBy());
            representation.setUpdatedOn(updateDate);
            representation.setUpdatedBy(aip.getUpdatedBy());

            if (representation.isOriginal()) {
              representation.setRepresentationStates(Arrays.asList(RepresentationState.ORIGINAL));
            } else {
              representation.setRepresentationStates(Arrays.asList(RepresentationState.OTHER));
            }
          }

          StringContentPayload payload = new StringContentPayload(JsonUtils.getJsonFromObject(aip));
          model.updateBinaryContent(aipJsonPath, payload, false, false);
        } catch (IOException e) {
          LOGGER.warn("Could not get AIP json file of AIP " + aipResorce.getStoragePath().toString(), e);
        }
      }
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
      | IOException e) {
      LOGGER.warn("Could not find AIPs", e);
    }
  }

  @Override
  public boolean isToVersionValid(int toVersion) {
    return toVersion == 2;
  }

}
