package org.roda.wui.api.v2.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationCreateRequest;
import org.roda.core.data.v2.ri.RepresentationInformationCustomForm;
import org.roda.core.data.v2.ri.RepresentationInformationFamily;
import org.roda.core.data.v2.ri.RepresentationInformationFamilyOptions;
import org.roda.core.data.v2.ri.RepresentationInformationFilterRequest;
import org.roda.core.data.v2.ri.RepresentationInformationRelationOptions;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.RepresentationInformationService;
import org.roda.wui.api.v2.services.TranslationService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.client.services.RepresentationInformationRestService;
import org.roda.wui.common.RequestControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.google.json.JsonSanitizer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/representation-information")
public class RepresentationInformationController implements RepresentationInformationRestService, Exportable {
  @Autowired
  HttpServletRequest request;

  @Autowired
  IndexService indexService;

  @Autowired
  RepresentationInformationService representationInformationService;

  @Autowired
  TranslationService translationService;

  @Autowired
  RequestHandler requestHandler;

  @Override
  public RepresentationInformation findByUuid(String uuid, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    RepresentationInformation retrieve = indexService.retrieve(RepresentationInformation.class, uuid, new ArrayList<>(),
      true);

    retrieve.setFamilyI18n(
      translationService.getTranslation(localeString, "ri.family." + retrieve.getFamily(), retrieve.getFamily()));

    representationInformationService.setIndexService(indexService);

    return representationInformationService.enrichRepresentationInformationRelations(retrieve, localeString,
      requestContext);
  }

  @Override
  public IndexResult<RepresentationInformation> find(@RequestBody FindRequest findRequest, String localeString) {
    IndexResult<RepresentationInformation> representationInformationIndexResult = indexService
      .find(RepresentationInformation.class, findRequest, localeString);

    for (RepresentationInformation representationInformation : representationInformationIndexResult.getResults()) {
      representationInformation.setFamilyI18n(translationService.getTranslation(localeString,
        "ri.family." + representationInformation.getFamily(), representationInformation.getFamily()));
    }

    return representationInformationIndexResult;
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (UserUtility.hasPermissions(requestContext.getUser(),
      RodaConstants.PERMISSION_METHOD_FIND_REPRESENTATION_INFORMATION)) {
      return new LongResponse(indexService.count(RepresentationInformation.class, countRequest));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(SuggestRequest suggestRequest) {
    return indexService.suggest(suggestRequest, RepresentationInformation.class);
  }

  @Override
  public RepresentationInformation createRepresentationInformation(
    @RequestBody RepresentationInformationCreateRequest createRequest) {

    return requestHandler
      .processRequestWithTransaction(new RequestHandler.RequestProcessor<RepresentationInformation>() {
        @Override
        public RepresentationInformation process(RequestContext requestContext,
          RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
          controllerAssistant.setParameters("representation_information_creation_request", createRequest);
          // sanitize the input
          String riSanitized = JsonSanitizer
            .sanitize(JsonUtils.getJsonFromObject(createRequest.getRepresentationInformation()));
          RepresentationInformation ri = JsonUtils.getObjectFromJson(riSanitized, RepresentationInformation.class);

          String formSanitized = JsonSanitizer.sanitize(JsonUtils.getJsonFromObject(createRequest.getForm()));
          RepresentationInformationCustomForm form = JsonUtils.getObjectFromJson(formSanitized,
            RepresentationInformationCustomForm.class);

          // validate representation information
          representationInformationService.validateRepresentationInformation(ri);

          // delegate
          return representationInformationService.createRepresentationInformation(ri, form, requestContext, true);
        }
      });
  }

  @Override
  public RepresentationInformation updateRepresentationInformation(
    @RequestBody RepresentationInformationCreateRequest updateRequest) {

    return requestHandler
      .processRequestWithTransaction(new RequestHandler.RequestProcessor<RepresentationInformation>() {
        @Override
        public RepresentationInformation process(RequestContext requestContext,
          RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
          controllerAssistant.setParameters("representation_information_update_request", updateRequest);
          // sanitize the input
          String riSanitized = JsonSanitizer
            .sanitize(JsonUtils.getJsonFromObject(updateRequest.getRepresentationInformation()));
          RepresentationInformation ri = JsonUtils.getObjectFromJson(riSanitized, RepresentationInformation.class);

          String formSanitized = JsonSanitizer.sanitize(JsonUtils.getJsonFromObject(updateRequest.getForm()));
          RepresentationInformationCustomForm form = JsonUtils.getObjectFromJson(formSanitized,
            RepresentationInformationCustomForm.class);

          // validate representation information
          representationInformationService.validateRepresentationInformation(ri);

          // delegate
          return representationInformationService.updateRepresentationInformation(ri, form, requestContext, true);
        }
      });
  }

  @Override
  public RepresentationInformationFamily retrieveRepresentationInformationFamily(String id, String localeString) {

    return requestHandler.processRequest(new RequestHandler.RequestProcessor<RepresentationInformationFamily>() {
      @Override
      public RepresentationInformationFamily process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_REPRESENTATION_INFORMATION_ID_PARAM, id);
        return representationInformationService
          .retrieveRepresentationInformationFamily(requestContext.getModelService(), id, localeString);
      }
    });
  }

  @Override
  public RepresentationInformationFamily retrieveRepresentationInformationFamilyConfigurations(String familyType,
    String localeString) {

    return requestHandler.processRequest(new RequestHandler.RequestProcessor<RepresentationInformationFamily>() {
      @Override
      public RepresentationInformationFamily process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
        // delegate
        return representationInformationService.retrieveRepresentationInformationFamilyConfigurations(familyType,
          localeString);
      }
    });
  }

  @Override
  public RepresentationInformationFamilyOptions retrieveRepresentationInformationFamilyOptions(String localeString) {

    return requestHandler.processRequest(new RequestHandler.RequestProcessor<RepresentationInformationFamilyOptions>() {
      @Override
      public RepresentationInformationFamilyOptions process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
        List<String> families = RodaCoreFactory.getRodaConfigurationAsList("ui.ri.family");
        Map<String, String> familyAndTranslation = new HashMap<>();

        for (String family : families) {
          familyAndTranslation.put(family, translationService.getTranslation(localeString, "ri.family." + family));
        }

        RepresentationInformationFamilyOptions options = new RepresentationInformationFamilyOptions();
        options.setOptions(familyAndTranslation);

        return options;
      }
    });
  }

  @Override
  public RepresentationInformationRelationOptions retrieveRepresentationInformationRelationOptions(
    String localeString) {

    return requestHandler
      .processRequest(new RequestHandler.RequestProcessor<RepresentationInformationRelationOptions>() {
        @Override
        public RepresentationInformationRelationOptions process(RequestContext requestContext,
          RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
          return representationInformationService.retrieveRelationTypeTranslations(localeString);
        }
      });
  }

  @Override
  public Job deleteMultipleRepresentationInformation(@RequestBody SelectedItemsRequest selectedItems) {

    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_SELECTED_PARAM, selectedItems);
        SelectedItems<RepresentationInformation> representationInformationSelectedItems = CommonServicesUtils
          .convertSelectedItems(selectedItems, RepresentationInformation.class);
        return representationInformationService
          .deleteRepresentationInformationByJob(representationInformationSelectedItems, requestContext.getUser());
      }
    });
  }

  @Override
  public Job addFilterToRepresentationInformation(
    @RequestBody RepresentationInformationFilterRequest representationInformationFilterRequest) {

    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters("filter_request", representationInformationFilterRequest);
        return representationInformationService.updateRepresentationInformationListWithFilter(
          representationInformationFilterRequest, requestContext.getUser());
      }
    });
  }

  @GetMapping(path = "{id}/download", produces = MediaType.APPLICATION_XML_VALUE)
  @Operation(summary = "Downloads the representation information", description = "Downloads the representation information from the storage", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StreamResponse.class))),
    @ApiResponse(responseCode = "404", description = "Representation information not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> downloadRepresentationInformation(
    @Parameter(description = "The representation information identifier") @PathVariable(name = "id") String id) {

    return requestHandler.processRequest(new RequestHandler.RequestProcessor<ResponseEntity<StreamingResponseBody>>() {
      @Override
      public ResponseEntity<StreamingResponseBody> process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException {
        controllerAssistant.setRelatedObjectId(id);
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_REPRESENTATION_ID_PARAM, id);
        StreamResponse streamResponse = representationInformationService
          .downloadRepresentationInformation(requestContext.getModelService(), id);

        return ApiUtils.okResponse(streamResponse, null);
      }
    });
  }

  @Override
  public ResponseEntity<StreamingResponseBody> exportToCSV(String findRequestString) {
    // delegate
    return ApiUtils.okResponse(
      indexService.exportToCSV(findRequestString, RepresentationInformation.class));
  }
}
