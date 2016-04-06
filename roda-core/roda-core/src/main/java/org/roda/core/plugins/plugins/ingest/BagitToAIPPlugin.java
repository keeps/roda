/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.utilities.SimpleResult;

public class BagitToAIPPlugin extends AbstractPlugin<TransferredResource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(BagitToAIPPlugin.class);

  public static String UNPACK_DESCRIPTION = "Extracted objects from package in Bagit format.";
  public static String UNPACK_SUCCESS_MESSAGE = "The SIP has been successfully unpacked.";
  public static String UNPACK_FAILURE_MESSAGE = "The ingest process failed to unpack the SIP.";
  public static String UNPACK_PARTIAL_MESSAGE = null;
  public static PreservationEventType UNPACK_EVENT_TYPE = PreservationEventType.UNPACKING;

  public static String WELLFORMED_DESCRIPTION = "Checked that the received SIP is well formed, complete and that no unexpected files were included.";
  public static String WELLFORMED_SUCCESS_MESSAGE = "The SIP was well formed and complete.";
  public static String WELLFORMED_FAILURE_MESSAGE = "The SIP was not well formed or some files were missing.";
  public static String WELLFORMED_PARTIAL_MESSAGE = null;
  public static PreservationEventType WELLFORMED_EVENT_TYPE = PreservationEventType.WELLFORMEDNESS_CHECK;

  private String successMessage;
  private String failureMessage;
  private PreservationEventType eventType;
  private String eventDescription;

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Bagit";
  }

  @Override
  public String getDescription() {
    return "BagIt as a zip file";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<TransferredResource> list)
    throws PluginException {
    Report report = PluginHelper.createPluginReport(this);

    for (TransferredResource transferredResource : list) {
      Path bagitPath = Paths.get(transferredResource.getFullPath());

      Report reportItem = PluginHelper.createPluginReportItem(this, transferredResource);

      try {
        LOGGER.debug("Converting {} to AIP", bagitPath);
        BagFactory bagFactory = new BagFactory();
        Bag bag = bagFactory.createBag(bagitPath.toFile());
        SimpleResult result = bag.verifyPayloadManifests();
        if (!result.isSuccess()) {
          throw new BagitNotValidException(result.getMessages() + "");
        }

        String parentId = PluginHelper.getParentId(this, index, bag.getBagInfoTxt().get("parent"));

        AIP aipCreated = BagitToAIPPluginUtils.bagitToAip(bag, bagitPath, model, "metadata.xml", parentId);
        createUnpackingEventSuccess(model, index, transferredResource, aipCreated);
        reportItem.setItemId(aipCreated.getId()).setPluginState(PluginState.SUCCESS);

        if (aipCreated.getParentId() == null) {
          reportItem.setPluginDetails(String.format("Parent with id '%s' not found", parentId));
        }

        createWellformedEventSuccess(model, index, transferredResource, aipCreated);
        LOGGER.debug("Done with converting {} to AIP {}", bagitPath, aipCreated.getId());
      } catch (Throwable e) {
        reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());

        LOGGER.error("Error converting " + bagitPath + " to AIP", e);
      }
      report.addReport(reportItem);
      PluginHelper.createJobReport(this, model, reportItem);
    }

    return report;
  }

  @Override
  public Report beforeBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {

    return null;
  }

  @Override
  public Report afterBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {

    return null;
  }

  @Override
  public Plugin<TransferredResource> cloneMe() {
    return new BagitToAIPPlugin();
  }

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

  private void createUnpackingEventSuccess(ModelService model, IndexService index,
    TransferredResource transferredResource, AIP aip) {
    setPreservationEventType(UNPACK_EVENT_TYPE);
    setPreservationSuccessMessage(UNPACK_SUCCESS_MESSAGE);
    setPreservationFailureMessage(UNPACK_FAILURE_MESSAGE);
    setPreservationEventDescription(UNPACK_DESCRIPTION);
    try {
      boolean notify = true;
      PluginHelper.createPluginEvent(this, aip.getId(), model, index, transferredResource, PluginState.SUCCESS, "",
        notify);
    } catch (NotFoundException | RequestNotValidException | GenericException | AuthorizationDeniedException
      | ValidationException | AlreadyExistsException e) {
      LOGGER.warn("Error creating unpacking event: " + e.getMessage(), e);
    }
  }

  private void createWellformedEventSuccess(ModelService model, IndexService index,
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

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // TODO Auto-generated method stub
    return null;
  }
}
