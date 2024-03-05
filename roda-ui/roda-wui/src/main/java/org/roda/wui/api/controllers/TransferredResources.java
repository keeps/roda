package org.roda.wui.api.controllers;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.common.ControllerAssistant;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class TransferredResources {

  TransferredResources(){
    // do nothing
  }
  public static TransferredResource createTransferredResourcesFolder(User user, String parentUUID, String folderName,
    boolean forceCommit)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    // check user permissions
    controllerAssistant.checkRoles(user);

    UserUtility.checkTransferredResourceAccess(user, Arrays.asList(parentUUID));

    // delegate
    try {
      TransferredResource transferredResource = BrowserHelper.createTransferredResourcesFolder(parentUUID, folderName,
        forceCommit);

      // register action
      controllerAssistant.registerAction(user, LogEntryState.SUCCESS, RodaConstants.CONTROLLER_PARENT_PARAM, parentUUID,
        RodaConstants.CONTROLLER_FOLDERNAME_PARAM, folderName, RodaConstants.CONTROLLER_SUCCESS_PARAM, true);
      return transferredResource;
    } catch (GenericException e) {
      // register action
      // FIXME nvieira 20170518: does this make sense to register with SUCCESS?
      // and the other exception, should they be treated differently?
      controllerAssistant.registerAction(user, LogEntryState.SUCCESS, RodaConstants.CONTROLLER_PARENT_PARAM, parentUUID,
        RodaConstants.CONTROLLER_FOLDERNAME_PARAM, folderName, RodaConstants.CONTROLLER_SUCCESS_PARAM, false,
        RodaConstants.CONTROLLER_ERROR_PARAM, e.getMessage());
      throw e;
    }
  }

  public static void deleteTransferredResources(User user, SelectedItems<TransferredResource> selected)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      BrowserHelper.deleteTransferredResources(selected, user);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected);
    }
  }

  public static void updateTransferredResources(User user, Optional<String> folderRelativePath, boolean waitToFinish)
    throws IsStillUpdatingException, AuthorizationDeniedException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      BrowserHelper.updateTransferredResources(folderRelativePath, waitToFinish);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      if (folderRelativePath.isPresent()) {
        controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_FOLDER_RELATIVEPATH_PARAM,
          folderRelativePath.get());
      } else {
        controllerAssistant.registerAction(user, state);
      }
    }
  }

  public static String renameTransferredResource(User user, String transferredResourceId, String newName)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException,
    IsStillUpdatingException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return BrowserHelper.renameTransferredResource(transferredResourceId, newName);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_TRANSFERRED_RESOURCE_ID_PARAM,
        transferredResourceId, RodaConstants.CONTROLLER_FILENAME_PARAM, newName);
    }
  }

  public static Job moveTransferredResource(User user, SelectedItems<TransferredResource> selected,
    TransferredResource transferredResource)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return BrowserHelper.moveTransferredResource(user, selected, transferredResource);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected,
        RodaConstants.CONTROLLER_TRANSFERRED_RESOURCE_PARAM, transferredResource);
    }
  }

  public static List<TransferredResource> retrieveSelectedTransferredResource(User user,
    SelectedItems<TransferredResource> selected)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return BrowserHelper.retrieveSelectedTransferredResource(selected);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected);
    }
  }
}
