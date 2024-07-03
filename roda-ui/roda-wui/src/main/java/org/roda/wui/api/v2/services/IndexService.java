package org.roda.wui.api.v2.services;

import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.v2.stream.FacetsCSVOutputStream;
import org.roda.wui.api.v2.stream.ResultsCSVOutputStream;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.I18nUtility;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.server.RodaStreamingOutput;
import org.springframework.stereotype.Service;

@Service
public class IndexService {

  public <T extends IsIndexed> List<String> suggest(SuggestRequest suggestRequest, Class<T> classToReturn,
    RequestContext requestContext) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser(), classToReturn);

      boolean justActive = true;
      return RodaCoreFactory.getIndexService().suggest(classToReturn, suggestRequest.getField(),
        suggestRequest.getQuery(), requestContext.getUser(), suggestRequest.isAllowPartial(), justActive);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_CLASS_PARAM, classToReturn,
        RodaConstants.CONTROLLER_FIELD_PARAM, suggestRequest.getField(), RodaConstants.CONTROLLER_QUERY_PARAM,
        suggestRequest.getQuery());
    }
  }

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

  public <T extends IsIndexed> StreamResponse exportToCSV(User user, String findRequestString, Class<T> returnClass,
    RequestContext requestContext) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    LogEntryState state = LogEntryState.SUCCESS;

    FindRequest findRequest;
    try {
      findRequest = JsonUtils.getObjectFromJson(findRequestString, FindRequest.class);
    } catch (GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state);
    }

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser(), returnClass);
      String csvDelimiter = RodaCoreFactory.getRodaConfiguration().getString("csv.delimiter");

      if (StringUtils.isBlank(csvDelimiter)) {
        csvDelimiter = CSVFormat.DEFAULT.getDelimiterString();
      }

      if (findRequest.isExportFacets()) {
        IndexResult<T> result = RodaCoreFactory.getIndexService().find(returnClass, findRequest.getFilter(),
          Sorter.NONE, Sublist.NONE, findRequest.getFacets(), user, findRequest.isOnlyActive(),
          findRequest.getFieldsToReturn());

        return new RodaStreamingOutput(
          new FacetsCSVOutputStream(result.getFacetResults(), findRequest.getFilename(), csvDelimiter))
          .toStreamResponse();
      } else {
        IndexResult<T> result = RodaCoreFactory.getIndexService().find(returnClass, findRequest.getFilter(),
          findRequest.getSorter(), findRequest.getSublist(), findRequest.getFacets(), user, findRequest.isOnlyActive(),
          findRequest.getFieldsToReturn());

        return new RodaStreamingOutput(new ResultsCSVOutputStream<>(result, findRequest.getFilename(), csvDelimiter))
          .toStreamResponse();
      }
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RequestNotValidException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_CLASS_PARAM,
        returnClass.getName(), RodaConstants.CONTROLLER_FILTER_PARAM, findRequest.getFilter(),
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
    } catch (GenericException | NotFoundException e) {
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
      return RodaCoreFactory.getIndexService().count(returnClass, request.getFilter(), context.getUser(),
        request.isOnlyActive());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(context, state, RodaConstants.CONTROLLER_CLASS_PARAM,
        returnClass.getSimpleName(), RodaConstants.CONTROLLER_FILTER_PARAM, request.getFilter().toString());
    }
  }
}
