/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.plugins.PluginHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SIPToAIPPlugin extends AbstractPlugin<TransferredResource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SIPToAIPPlugin.class);

  public static final String UNPACK_SUCCESS_MESSAGE = "The SIP has been successfully unpacked.";
  public static final String UNPACK_FAILURE_MESSAGE = "The ingest process failed to unpack the SIP.";
  public static final String UNPACK_PARTIAL_MESSAGE = null;
  public static final PreservationEventType UNPACK_EVENT_TYPE = PreservationEventType.UNPACKING;

  public static final String WELLFORMED_DESCRIPTION = "Checked that the received SIP is well formed, complete and that no unexpected files were included.";
  public static final String WELLFORMED_SUCCESS_MESSAGE = "The SIP was well formed and complete.";
  public static final String WELLFORMED_FAILURE_MESSAGE = "The SIP was not well formed or some files were missing.";
  public static final String WELLFORMED_PARTIAL_MESSAGE = null;
  public static final PreservationEventType WELLFORMED_EVENT_TYPE = PreservationEventType.WELLFORMEDNESS_CHECK;

  private String successMessage;
  private String failureMessage;
  private PreservationEventType eventType;
  private String eventDescription;

  @Override
  public PluginType getType() {
    return PluginType.SIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return eventType;
  }

  @Override
  public String getPreservationEventDescription() {
    return eventDescription;
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return successMessage;
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return failureMessage;
  }

  public void setPreservationEventType(PreservationEventType t) {
    this.eventType = t;
  }

  public void setPreservationSuccessMessage(String message) {
    this.successMessage = message;
  }

  public void setPreservationFailureMessage(String message) {
    this.failureMessage = message;
  }

  public void setPreservationEventDescription(String description) {
    this.eventDescription = description;
  }

  protected void createUnpackingEventSuccess(ModelService model, IndexService index,
    TransferredResource transferredResource, AIP aip, String unpackDescription) {
    setPreservationEventType(UNPACK_EVENT_TYPE);
    setPreservationSuccessMessage(UNPACK_SUCCESS_MESSAGE);
    setPreservationFailureMessage(UNPACK_FAILURE_MESSAGE);
    setPreservationEventDescription(unpackDescription);
    try {
      boolean notify = true;
      PluginHelper.createPluginEvent(this, aip.getId(), model, index, transferredResource, PluginState.SUCCESS, "",
        notify);
    } catch (NotFoundException | RequestNotValidException | GenericException | AuthorizationDeniedException
      | ValidationException | AlreadyExistsException e) {
      LOGGER.warn("Error creating unpacking event: " + e.getMessage(), e);
    }
  }

  protected void createWellformedEventSuccess(ModelService model, IndexService index,
    TransferredResource transferredResource, AIP aip) {
    setPreservationEventType(WELLFORMED_EVENT_TYPE);
    setPreservationSuccessMessage(WELLFORMED_SUCCESS_MESSAGE);
    setPreservationFailureMessage(WELLFORMED_FAILURE_MESSAGE);
    setPreservationEventDescription(WELLFORMED_DESCRIPTION);
    try {
      boolean notify = true;
      PluginHelper.createPluginEvent(this, aip.getId(), model, index, transferredResource, PluginState.SUCCESS, "",
        notify);
    } catch (NotFoundException | RequestNotValidException | GenericException | AuthorizationDeniedException
      | ValidationException | AlreadyExistsException e) {
      LOGGER.warn("Error creating unpacking event: " + e.getMessage(), e);
    }
  }
}
