package org.roda.wui.api.controllers;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.LockingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetValue;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.core.plugins.PluginHelper;
import org.roda.wui.common.ControllerAssistant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class Aips extends RemoteServiceServlet {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobsHelper.class);

  Aips(){
    // do nothing
  }

  public static void releaseAIPLock(String aipId, User user) {
    boolean lockEnabled = RodaCoreFactory.getRodaConfiguration().getBoolean("core.aip.lockToEdit", false);

    if (lockEnabled) {
      PluginHelper.releaseObjectLock(aipId, user.getUUID());
    }
  }

  public static boolean requestAIPLock(String aipId, User user) {
    boolean lockEnabled = RodaCoreFactory.getRodaConfiguration().getBoolean("core.aip.lockToEdit", false);

    if (!lockEnabled) {
      return true;
    }


    try {
      PluginHelper.tryLock(Collections.singletonList(aipId), user.getUUID());
    } catch (LockingException e) {
      return false;
    }
    return true;
  }

  public static Job updateAIPPermissions(User user, SelectedItems<IndexedAIP> aips, Permissions permissions,
                                         String details, boolean recursive)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    controllerAssistant.checkObjectPermissions(user, aips);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return BrowserHelper.updateAIPPermissions(user, aips, permissions, details, recursive);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_AIPS_PARAM, aips,
        RodaConstants.CONTROLLER_PERMISSIONS_PARAM, permissions, RodaConstants.CONTROLLER_DETAILS_PARAM, details);
    }
  }

  public static Pair<Boolean, List<String>> retrieveAIPTypeOptions(String locale, User user) {
    List<String> types = new ArrayList<>();
    boolean isControlled = RodaCoreFactory.getRodaConfiguration().getBoolean("core.aip_type.controlled_vocabulary",
      false);

    if (isControlled) {
      types = RodaCoreFactory.getRodaConfigurationAsList("core.aip_type.value");
    } else {
      try {
        Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.AIP_TYPE));
        IndexResult<IndexedAIP> result = Browser.find(IndexedAIP.class.getName(), Filter.ALL, Sorter.NONE, user, Sublist.NONE, facets,
          locale, false, new ArrayList<>());

        List<FacetFieldResult> facetResults = result.getFacetResults();
        for (FacetValue facetValue : facetResults.get(0).getValues()) {
          types.add(facetValue.getValue());
        }
      } catch (GenericException | AuthorizationDeniedException | RequestNotValidException e) {
        LOGGER.error("Could not execute find request on AIPs", e);
      }
    }

    return Pair.of(isControlled, types);
  }

  public static AIP updateAIP(User user, AIP aip)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      IndexedAIP indexedAip = BrowserHelper.retrieve(IndexedAIP.class, aip.getId(),
        RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
      controllerAssistant.checkObjectPermissions(user, indexedAip);

      // check state
      controllerAssistant.checkAIPstate(indexedAip);

      // check if AIP is in a disposal confirmation
      controllerAssistant.checkIfAIPInConfirmation(indexedAip);

      // delegate
      return BrowserHelper.updateAIP(user, aip);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, aip.getId(), state, RodaConstants.CONTROLLER_AIP_PARAM, aip);
    }
  }

  public static AIP createAIP(User user, String parentId, String type) throws AuthorizationDeniedException,
    GenericException, NotFoundException, RequestNotValidException, AlreadyExistsException {
    if (parentId == null) {
      return createAIPTop(user, type);
    } else {
      return createAIPBelow(user, parentId, type);
    }
  }

  private static AIP createAIPTop(User user, String type) throws AuthorizationDeniedException, GenericException,
    NotFoundException, RequestNotValidException, AlreadyExistsException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {
    };

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      Permissions permissions = new Permissions();

      // delegate
      return BrowserHelper.createAIP(user, null, type, permissions);

    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_TYPE_PARAM, type);
    }
  }

  private static AIP createAIPBelow(User user, String parentId, String type) throws AuthorizationDeniedException,
    GenericException, NotFoundException, RequestNotValidException, AlreadyExistsException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {
    };

    // check user permissions
    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      Permissions permissions = new Permissions();

      if (parentId != null) {
        IndexedAIP parentSDO = BrowserHelper.retrieve(IndexedAIP.class, parentId,
          RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
        controllerAssistant.checkObjectPermissions(user, parentSDO);

        // check state
        controllerAssistant.checkAIPstate(parentSDO);

        // check if AIP is in a disposal confirmation
        controllerAssistant.checkIfAIPInConfirmation(parentSDO);

        Permissions parentPermissions = parentSDO.getPermissions();

        for (String name : parentPermissions.getUsernames()) {
          permissions.setUserPermissions(name, parentPermissions.getUserPermissions(name));
        }

        for (String name : parentPermissions.getGroupnames()) {
          permissions.setGroupPermissions(name, parentPermissions.getGroupPermissions(name));
        }
      } else {
        throw new RequestNotValidException("Creating AIP that should be below another with a null parentId");
      }

      // delegate
      return BrowserHelper.createAIP(user, parentId, type, permissions);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_PARENT_ID_PARAM, parentId,
        RodaConstants.CONTROLLER_TYPE_PARAM, type);
    }
  }

  public static Job deleteAIP(User user, SelectedItems<IndexedAIP> aips, String details)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {
    };

    // check user permissions
    controllerAssistant.checkRoles(user);
    controllerAssistant.checkObjectPermissions(user, aips);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return BrowserHelper.deleteAIP(user, aips, details);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, aips,
        RodaConstants.CONTROLLER_DETAILS_PARAM, details);
    }
  }

  public static Job changeAIPType(User user, SelectedItems<IndexedAIP> selected, String newType, String details)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);
    controllerAssistant.checkObjectPermissions(user, selected);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return BrowserHelper.changeAIPType(user, selected, newType, details);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selected,
        RodaConstants.CONTROLLER_TYPE_PARAM, newType, RodaConstants.CONTROLLER_DETAILS_PARAM, details);
    }
  }

}
