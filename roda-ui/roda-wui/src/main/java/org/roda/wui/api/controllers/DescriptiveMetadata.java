package org.roda.wui.api.controllers;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StringContentPayload;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataEditBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataVersionsBundle;
import org.roda.wui.client.browse.bundle.SupportedMetadataTypeBundle;
import org.roda.wui.common.ControllerAssistant;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class DescriptiveMetadata {

  DescriptiveMetadata(){
    // do nothing
  }

  public static org.roda.core.data.v2.ip.metadata.DescriptiveMetadata createDescriptiveMetadataFile(User user, String aipId, String representationId, DescriptiveMetadataEditBundle bundle)
    throws AuthorizationDeniedException, GenericException, ValidationException, NotFoundException,
    RequestNotValidException, AlreadyExistsException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    // If the bundle has values from the form, we need to update the XML by
    // applying the values of the form to the raw template
    if (bundle.getValues() != null && !bundle.getValues().isEmpty()) {
      SupportedMetadataTypeBundle smtb = new SupportedMetadataTypeBundle(bundle.getId(), bundle.getType(),
        bundle.getVersion(), bundle.getId(), bundle.getRawTemplate(), bundle.getValues());
      bundle.setXml(DescriptiveMetadata.retrieveDescriptiveMetadataPreview(user, smtb));
    }

    String metadataId = bundle.getId();
    String descriptiveMetadataType = bundle.getType();
    String descriptiveMetadataVersion = bundle.getVersion();
    ContentPayload payload = new StringContentPayload(bundle.getXml());

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // check state
      controllerAssistant.checkAIPstate(aip);

      // check if AIP is in a disposal confirmation
      controllerAssistant.checkIfAIPInConfirmation(aip);

      // delegate
      return BrowserHelper.createDescriptiveMetadataFile(aipId, representationId, metadataId, descriptiveMetadataType,
        descriptiveMetadataVersion, payload, user.getId());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        metadataId, RodaConstants.CONTROLLER_TYPE_PARAM, descriptiveMetadataType, RodaConstants.CONTROLLER_VERSION_ID_PARAM,
        descriptiveMetadataVersion);
    }
  }

  public static String retrieveDescriptiveMetadataPreview(User user, SupportedMetadataTypeBundle bundle)
    throws AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return BrowserHelper.retrieveDescriptiveMetadataPreview(bundle);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_TEMPLATE_PARAM, bundle.getLabel());
    }
  }

  public static org.roda.core.data.v2.ip.metadata.DescriptiveMetadata updateDescriptiveMetadataFile(User user, String aipId, String representationId,
                                                                                                    DescriptiveMetadataEditBundle bundle) throws AuthorizationDeniedException, GenericException, ValidationException, NotFoundException,
    RequestNotValidException {
    String metadataId = bundle.getId();
    String metadataType = bundle.getType();
    String metadataVersion = bundle.getVersion();
    ContentPayload payload = new StringContentPayload(bundle.getXml());
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // check state
      controllerAssistant.checkAIPstate(aip);

      // check if AIP is in a disposal confirmation
      controllerAssistant.checkIfAIPInConfirmation(aip);

      // delegate
      Map<String, String> properties = new HashMap<>();
      properties.put(RodaConstants.VERSION_USER, user.getId());
      properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.UPDATED.toString());

      return BrowserHelper.updateDescriptiveMetadataFile(aipId, representationId, metadataId, metadataType,
        metadataVersion, payload, properties, user.getId());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        metadataId);
    }
  }

  public static void deleteDescriptiveMetadataFile(User user, String aipId, String representationId, String metadataId)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // check state
      controllerAssistant.checkAIPstate(aip);

      // check if AIP is in a disposal confirmation
      controllerAssistant.checkIfAIPInConfirmation(aip);

      // delegate
      BrowserHelper.deleteDescriptiveMetadataFile(aipId, representationId, metadataId, user.getId());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        metadataId);
    }
  }

  public static DescriptiveMetadataVersionsBundle retrieveDescriptiveMetadataVersionsBundle(User user, String aipId,
    String representationId, String metadataId, Locale locale)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      return BrowserHelper.retrieveDescriptiveMetadataVersionsBundle(aipId, representationId, metadataId, locale);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        metadataId);
    }
  }

  public static void revertDescriptiveMetadataVersion(User user, String aipId, String representationId,
    String descriptiveMetadataId, String versionId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      Map<String, String> properties = new HashMap<>();
      properties.put(RodaConstants.VERSION_USER, user.getId());
      properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.REVERTED.toString());

      BrowserHelper.revertDescriptiveMetadataVersion(aipId, representationId, descriptiveMetadataId, versionId,
        properties);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        descriptiveMetadataId, RodaConstants.CONTROLLER_VERSION_ID_PARAM, versionId);
    }
  }

  public static void deleteDescriptiveMetadataVersion(User user, String aipId, String representationId,
    String descriptiveMetadataId, String versionId)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      IndexedAIP aip = BrowserHelper.retrieve(IndexedAIP.class, aipId, RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, aip);

      // delegate
      BrowserHelper.deleteDescriptiveMetadataVersion(aipId, representationId, descriptiveMetadataId, versionId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_METADATA_ID_PARAM,
        descriptiveMetadataId, RodaConstants.CONTROLLER_VERSION_ID_PARAM, versionId);
    }
  }
}
