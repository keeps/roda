/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.migration.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Date;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.RepresentationState;
import org.roda.core.migration.MigrationAction;
import org.roda.core.model.ModelService;
import org.roda.core.storage.StringContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepresentationToVersion2 implements MigrationAction<Representation> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RepresentationToVersion2.class);

  @Override
  public void migrate(ModelService model) {
    try (CloseableIterable<OptionalWithCause<AIP>> aips = model.listAIPs()) {
      for (OptionalWithCause<AIP> aip : aips) {
        if (aip.isPresent()) {
          for (Representation representation : aip.get().getRepresentations()) {
            Path representationPath = model.getDirectAccess(representation).getPath();

              BasicFileAttributes attr = Files.readAttributes(representationPath, BasicFileAttributes.class);
              Date createDate = new Date(attr.creationTime().toMillis());
              Date updateDate = new Date(attr.lastModifiedTime().toMillis());

              representation.setCreatedOn(createDate);
              representation.setCreatedBy(aip.get().getCreatedBy());
              representation.setUpdatedOn(updateDate);
              representation.setUpdatedBy(aip.get().getUpdatedBy());

              if (representation.isOriginal()) {
                representation.setRepresentationStates(Arrays.asList(RepresentationState.ORIGINAL));
              } else {
                representation.setRepresentationStates(Arrays.asList(RepresentationState.OTHER));
              }
            }

            StringContentPayload payload = new StringContentPayload(JsonUtils.getJsonFromObject(aip));
            model.updateBinaryContent(aip.get(), payload, false, false);
          } else {
            LOGGER.warn("Could not get list an AIP");
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
