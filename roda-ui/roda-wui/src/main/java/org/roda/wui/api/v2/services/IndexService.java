package org.roda.wui.api.v2.services;

import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.v2.controller.RequestHandler;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.stream.FacetsCSVOutputStream;
import org.roda.wui.api.v2.stream.ResultsCSVOutputStream;
import org.roda.wui.common.I18nUtility;
import org.roda.wui.common.RequestControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.server.RodaStreamingOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IndexService {

  @Autowired
  private RequestHandler requestHandler;

  public <T extends IsIndexed> List<String> suggest(SuggestRequest suggestRequest, Class<T> classToReturn) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<List<String>>() {
      @Override
      public List<String> process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_CLASS_PARAM, classToReturn,
          RodaConstants.CONTROLLER_FIELD_PARAM, suggestRequest.getField(), RodaConstants.CONTROLLER_QUERY_PARAM,
          suggestRequest.getQuery());
        boolean justActive = true;
        return requestContext.getIndexService().suggest(classToReturn, suggestRequest.getField(),
          suggestRequest.getQuery(), requestContext.getUser(), suggestRequest.isAllowPartial(), justActive);
      }
    }, classToReturn);
  }

  public <T extends IsIndexed> IndexResult<T> find(final Class<T> classToReturn, final FindRequest findRequest) {
    return find(classToReturn, findRequest, null);
  }

  public <T extends IsIndexed> IndexResult<T> find(final Class<T> classToReturn, final FindRequest findRequest,
    String locale) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<IndexResult<T>>() {
      @Override
      public IndexResult<T> process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_CLASS_PARAM, classToReturn.getSimpleName(),
          RodaConstants.CONTROLLER_FILTER_PARAM, findRequest.getFilter(), RodaConstants.CONTROLLER_SORTER_PARAM,
          findRequest.getSorter(), RodaConstants.CONTROLLER_SUBLIST_PARAM, findRequest.getSublist());

        if (findRequest.getFilter() == null || findRequest.getFilter().getParameters().isEmpty()) {
          return new IndexResult<>();
        }

        // delegate
        IndexResult<T> result = requestContext.getIndexService().find(classToReturn, findRequest,
          requestContext.getUser());

        if (locale == null) {
          return result;
        }

        return I18nUtility.translate(result, classToReturn, locale);
      }
    }, classToReturn);
  }

  public <T extends IsIndexed> StreamResponse exportToCSV(User user, String findRequestString, Class<T> returnClass) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<StreamResponse>() {
      @Override
      public StreamResponse process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        FindRequest findRequest = JsonUtils.getObjectFromJson(findRequestString, FindRequest.class);

        controllerAssistant.setParameters(RodaConstants.CONTROLLER_CLASS_PARAM, returnClass.getName(),
          RodaConstants.CONTROLLER_FILTER_PARAM, findRequest.getFilter(), RodaConstants.CONTROLLER_SORTER_PARAM,
          findRequest.getSorter(), RodaConstants.CONTROLLER_SUBLIST_PARAM, findRequest.getSublist());

        String csvDelimiter = RodaCoreFactory.getRodaConfiguration().getString("csv.delimiter");

        if (StringUtils.isBlank(csvDelimiter)) {
          csvDelimiter = CSVFormat.DEFAULT.getDelimiterString();
        }

        if (findRequest.isExportFacets()) {
          IndexResult<T> result = requestContext.getIndexService().find(returnClass, findRequest.getFilter(),
            Sorter.NONE, Sublist.NONE, findRequest.getFacets(), user, findRequest.isOnlyActive(),
            findRequest.getFieldsToReturn());

          return new RodaStreamingOutput(
            new FacetsCSVOutputStream(result.getFacetResults(), findRequest.getFilename(), csvDelimiter))
            .toStreamResponse();
        } else {
          IndexResult<T> result = requestContext.getIndexService().find(returnClass, findRequest.getFilter(),
            findRequest.getSorter(), findRequest.getSublist(), findRequest.getFacets(), user,
            findRequest.isOnlyActive(), findRequest.getFieldsToReturn());

          return new RodaStreamingOutput(new ResultsCSVOutputStream<>(result, findRequest.getFilename(), csvDelimiter))
            .toStreamResponse();
        }
      }
    });
  }

  public <T extends IsIndexed> T retrieve(Class<T> returnClass, String id, List<String> fieldsToReturn,
    boolean appendChildren) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<T>() {
      @Override
      public T process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setRelatedObjectId(id);
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_CLASS_PARAM, returnClass.getSimpleName());
        // delegate
        final T ret = requestContext.getIndexService().retrieve(returnClass, id, fieldsToReturn, appendChildren);

        // checking object permissions
        controllerAssistant.checkObjectPermissions(requestContext.getUser(), ret, returnClass);

        return ret;
      }
    }, returnClass);
  }

  public <T extends IsIndexed> T retrieve(Class<T> returnClass, String id, List<String> fieldsToReturn) {
    return retrieve(returnClass, id, fieldsToReturn, false);
  }

  public <T extends IsIndexed> Long count(Class<T> returnClass, CountRequest request) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<Long>() {
      @Override
      public Long process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_CLASS_PARAM, returnClass.getSimpleName(),
          RodaConstants.CONTROLLER_FILTER_PARAM, request.getFilter().toString());
        return requestContext.getIndexService().count(returnClass, request.getFilter(), requestContext.getUser(),
          request.isOnlyActive());
      }
    }, returnClass);
  }
}
