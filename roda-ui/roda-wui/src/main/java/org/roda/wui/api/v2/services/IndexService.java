package org.roda.wui.api.v2.services;

import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.I18nUtility;
import org.roda.wui.common.model.RequestContext;
import org.springframework.stereotype.Service;

@Service
public class IndexService {

  public <T extends IsIndexed> IndexResult<T> find(final Class<T> classToReturn, final FindRequest findRequest,
    RequestContext context) {
    return find(classToReturn, findRequest, null, context);
  }


  public <T extends IsIndexed> IndexResult<T> find(final Class<T> classToReturn, final FindRequest findRequest,
    String locale, RequestContext context) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(context.getUser(), classToReturn);

      if (findRequest.getFilter() == null || findRequest.getFilter().getParameters().isEmpty()) {
        return new IndexResult<>();
      }

      // delegate
      IndexResult<T> result = RodaCoreFactory.getIndexService().find(classToReturn, findRequest, context.getUser());

      if (locale == null) {
        return result;
      }

      return I18nUtility.translate(result, classToReturn, locale);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {

      // register action
      controllerAssistant.registerAction(context, state, RodaConstants.CONTROLLER_CLASS_PARAM,
        classToReturn.getSimpleName(), RodaConstants.CONTROLLER_FILTER_PARAM, findRequest.getFilter(),
        RodaConstants.CONTROLLER_SORTER_PARAM, findRequest.getSorter(), RodaConstants.CONTROLLER_SUBLIST_PARAM,
        findRequest.getSublist());
    }
  }

  public <T extends IsIndexed> T retrieve(RequestContext context, Class<T> returnClass, String id,
    List<String> fieldsToReturn, boolean appendChildren) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(context.getUser(), returnClass);
      // delegate
      final T ret = RodaCoreFactory.getIndexService().retrieve(returnClass, id, fieldsToReturn, appendChildren);

      // checking object permissions
      controllerAssistant.checkObjectPermissions(context.getUser(), ret, returnClass);

      return ret;
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(context, id, state, RodaConstants.CONTROLLER_CLASS_PARAM,
        returnClass.getSimpleName());
    }
  }

  public <T extends IsIndexed> T retrieve(RequestContext context, Class<T> returnClass, String id,
    List<String> fieldsToReturn) {
    return retrieve(context, returnClass, id, fieldsToReturn, false);
  }

  public <T extends IsIndexed> Long count(Class<T> returnClass, CountRequest request, RequestContext context) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    LogEntryState state = LogEntryState.SUCCESS;
    try {
      // check user permissions
      controllerAssistant.checkRoles(context.getUser(), returnClass);
      return RodaCoreFactory.getIndexService().count(returnClass, request.getFilter(), context.getUser(), request.isOnlyActive());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(context, state, RodaConstants.CONTROLLER_CLASS_PARAM, returnClass.getSimpleName(),
        RodaConstants.CONTROLLER_FILTER_PARAM, request.getFilter().toString());
    }
  }
}
