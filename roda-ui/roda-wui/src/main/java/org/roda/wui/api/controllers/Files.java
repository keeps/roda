package org.roda.wui.api.controllers;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.wui.common.ControllerAssistant;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class Files {

  Files(){
    // do nothing
  }

  public static Job deleteFile(User user, SelectedItems<IndexedFile> files, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    controllerAssistant.checkObjectPermissions(user, files);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return BrowserHelper.deleteFile(user, files, details);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, files,
        RodaConstants.CONTROLLER_DETAILS_PARAM, details);
    }
  }

  public static void deleteFile(User user, String fileUUID, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;
    IndexedFile file = null;

    try {
      file = BrowserHelper.retrieve(IndexedFile.class, fileUUID, RodaConstants.FILE_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, file);

      // delegate
      BrowserHelper.deleteFile(user, SelectedItemsList.create(IndexedFile.class, fileUUID), details);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      String id = file == null ? fileUUID : file.getAipId();
      controllerAssistant.registerAction(user, id, state, RodaConstants.CONTROLLER_FILE_UUID_PARAM, fileUUID,
        RodaConstants.CONTROLLER_DETAILS_PARAM, details);
    }
  }

  public static Job moveFiles(User user, String aipId, String representationId,
    SelectedItems<IndexedFile> selectedFiles, IndexedFile toFolder, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    controllerAssistant.checkObjectPermissions(user, selectedFiles);

    IndexedAIP destinationAIP = RodaCoreFactory.getIndexService().retrieve(IndexedAIP.class, aipId,
      RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
    controllerAssistant.checkObjectPermissions(user, destinationAIP);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return BrowserHelper.moveFiles(user, aipId, representationId, selectedFiles, toFolder, details);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aipId, state, RodaConstants.CONTROLLER_AIP_ID_PARAM, aipId,
        RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, representationId, RodaConstants.CONTROLLER_FILES_PARAM,
        selectedFiles, RodaConstants.CONTROLLER_FILE_PARAM, toFolder, RodaConstants.CONTROLLER_DETAILS_PARAM, details);
    }
  }
}
