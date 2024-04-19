package org.roda.wui.api.v2.services;

import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.common.ControllerAssistant;
import org.springframework.stereotype.Service;

@Service
public class IndexService {

  public <T extends IsIndexed> IndexResult<T> find(final Class<T> classToReturn, final Filter filter,
    final Sorter sorter, final Sublist sublist, final Facets facets, final User user, final boolean justActive,
    final List<String> fieldsToReturn) {

    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(user, classToReturn);

      // delegate
      return RodaCoreFactory.getIndexService().find(classToReturn, filter, sorter, sublist, facets, user, justActive,
        fieldsToReturn);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_CLASS_PARAM,
        classToReturn.getSimpleName(), RodaConstants.CONTROLLER_FILTER_PARAM, filter,
        RodaConstants.CONTROLLER_SORTER_PARAM, sorter, RodaConstants.CONTROLLER_SUBLIST_PARAM, sublist);
    }
  }

  public <T extends IsIndexed> T retrieve(User user, Class<T> returnClass, String id, List<String> fieldsToReturn) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(user, returnClass);
      // delegate
      final T ret = RodaCoreFactory.getIndexService().retrieve(returnClass, id, fieldsToReturn);

      // checking object permissions
      controllerAssistant.checkObjectPermissions(user, ret, returnClass);

      return ret;
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, id, state, RodaConstants.CONTROLLER_CLASS_PARAM,
        returnClass.getSimpleName());
    }
  }

  public <T extends IsIndexed> Long count(Class<T> returnClass, Filter filter, boolean justActive, User user) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    LogEntryState state = LogEntryState.SUCCESS;
    try {
      // check user permissions
      controllerAssistant.checkRoles(user, returnClass);
      return RodaCoreFactory.getIndexService().count(returnClass, filter, user, justActive);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_CLASS_PARAM, returnClass.getSimpleName(),
        RodaConstants.CONTROLLER_FILTER_PARAM, filter.toString());
    }
  }
}
