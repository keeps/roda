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
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.URNUtils;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.migration.MigrationAction;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.premis.v3.File;
import gov.loc.premis.v3.ObjectIdentifierComplexType;

public class PreservationMetadataFileToVersion2 implements MigrationAction<PreservationMetadata> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PreservationMetadataFileToVersion2.class);

  @Override
  public void migrate(ModelService model) {
    try (CloseableIterable<OptionalWithCause<Representation>> representations = model.list(Representation.class)) {
      for (OptionalWithCause<Representation> representation : representations) {
        if (representation.isPresent()) {
          try (CloseableIterable<OptionalWithCause<PreservationMetadata>> pms = model
                  .listPreservationMetadata(representation.get().getAipId(), representation.get().getId())) {
            for (OptionalWithCause<PreservationMetadata> pm : pms) {
              if (pm.isPresent()) {
                if (!model.hasDirectory(pm.get()) && pm.get().getId().startsWith(URNUtils
                        .getPremisPrefix(PreservationMetadataType.FILE, RODAInstanceUtils.getLocalInstanceIdentifier()))) {
                  Binary pmBinary = model.getBinary(pm.get());
                  migrate(model, pmBinary, pm.get(), representation.get().getAipId(), representation.get().getId());
                }
              }
              else {
                LOGGER.debug("Couldn't get preservation metadata", pm.getCause());
              }
            }
          } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
                   | IOException e) {
            LOGGER.warn("Could not find preservation metadata files", e);
          }
        }
        else {
          LOGGER.warn("Couldn't get representation", representation.getCause());
        }
      }

    } catch (IOException | RODAException e) {
      LOGGER.warn("Could not find representations", e);
    }
  }

  private void migrate(ModelService model, Binary binary, PreservationMetadata oldPM, String aipId,
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

      Optional<LiteRODAObject> oldPMLite = LiteRODAObjectFactory.get(PreservationMetadata.class, aipId,
        representationId, oldPM.getId());
      Optional<LiteRODAObject> newPMLite = LiteRODAObjectFactory.get(PreservationMetadata.class, aipId,
        representationId, value);
      if (oldPMLite.isEmpty() || newPMLite.isEmpty()) {
        throw new RequestNotValidException("Could not create new LITE for preservation metadata file " + oldPM.getId());
      }
      model.moveObject(oldPMLite.get(), newPMLite.get());

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
