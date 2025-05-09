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
import java.util.Optional;

import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.URNUtils;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.migration.MigrationAction;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.premis.v3.File;
import gov.loc.premis.v3.ObjectIdentifierComplexType;

public class PreservationMetadataFileToVersion2 implements MigrationAction<PreservationMetadata> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PreservationMetadataFileToVersion2.class);

  @Override
  public void migrate(ModelService model) {
    try (DirectResourceAccess aipsContainer = model.getDirectAccess(AIP.class);
      CloseableIterable<DirectResourceAccess> aips = FSUtils.listDirectAccessResourceChildren(aipsContainer, false)) {

      for (DirectResourceAccess aip : aips) {
        String aipId = aip.getPath().getFileName().toString();
        try (
          DirectResourceAccess representationsContainer = FSUtils.resolve(aip,
            RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS);
          CloseableIterable<DirectResourceAccess> representations = FSUtils
            .listDirectAccessResourceChildren(representationsContainer, false)) {
          for (DirectResourceAccess representation : representations) {
            DirectResourceAccess pmAccess = FSUtils.resolve(representation, RodaConstants.STORAGE_DIRECTORY_METADATA,
              RodaConstants.STORAGE_DIRECTORY_PRESERVATION);
            String representationId = representation.getPath().getFileName().toString();
            try (
              CloseableIterable<DirectResourceAccess> pms = FSUtils.listDirectAccessResourceChildren(pmAccess, true)) {
              for (DirectResourceAccess pm : pms) {
                String pmId = pm.getPath().getFileName().toString().replace(RodaConstants.PREMIS_SUFFIX, "");
                if (!pm.isDirectory()) {
                  Optional<LiteRODAObject> pmLite = LiteRODAObjectFactory.get(PreservationMetadata.class, aipId,
                    representationId, pmId);
                  if (pmLite.isPresent()) {
                    if (pm.getPath().getFileName().startsWith(URNUtils.getPremisPrefix(PreservationMetadataType.FILE,
                      RODAInstanceUtils.getLocalInstanceIdentifier()))) {
                      Binary pmBinary = model.getBinary(pmLite.get());
                      migrate(model, pmBinary, pmLite.get(), aipId, representationId);
                    }
                  } else {
                    LOGGER.warn("Could not create LITE from preservation metadata file {}", pm.getPath().getFileName());
                  }
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

  private void migrate(ModelService model, Binary binary, LiteRODAObject oldPMLite, String aipId,
    String representationId) {
    try (InputStream inputStream = binary.getContent().createInputStream()) {
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

      Optional<LiteRODAObject> newPMLite = LiteRODAObjectFactory.get(PreservationMetadata.class, aipId,
        representationId, value);
      if (newPMLite.isEmpty()) {
        throw new RequestNotValidException("Could not create LITE for new preservation metadata file " + value);
      }
      model.moveObject(oldPMLite, newPMLite.get());

      ContentPayload newPremis = PremisV3Utils.fileToBinary(file);
      boolean asReference = false;
      boolean createIfNotExists = false;
      model.updateBinaryContent(newPMLite.get(), newPremis, asReference, createIfNotExists);
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
