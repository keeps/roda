package org.roda.wui.api.v2.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.StreamResponse;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmationCreateRequest;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmationForm;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.services.DisposalConfirmationService;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.client.services.DisposalConfirmationRestService;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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
@RequestMapping(path = "/api/v2/disposal/confirmations")
public class DisposalConfirmationController implements DisposalConfirmationRestService, Exportable {

  @Autowired
  HttpServletRequest request;

  @Autowired
  DisposalConfirmationService disposalConfirmationService;

  @Autowired
  IndexService indexService;

  @Override
  public DisposalConfirmation findByUuid(String uuid, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.retrieve(requestContext, DisposalConfirmation.class, uuid, new ArrayList<>());
  }

  @Override
  public IndexResult<DisposalConfirmation> find(@RequestBody FindRequest findRequest, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.find(DisposalConfirmation.class, findRequest, localeString, requestContext);
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (UserUtility.hasPermissions(requestContext.getUser(), RodaConstants.PERMISSION_METHOD_FIND_DIP)) {
      return new LongResponse(indexService.count(DisposalConfirmation.class, countRequest, requestContext));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(SuggestRequest suggestRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.suggest(suggestRequest, DisposalConfirmation.class, requestContext);
  }

  @GetMapping(path = "/{id}/report/html", produces = MediaType.TEXT_HTML_VALUE)
  @Operation(summary = "Retrieves the disposal confirmation report", responses = {
    @ApiResponse(responseCode = "200", description = "Returns the disposal confirmation report", content = @Content(schema = @Schema(implementation = String.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  public ResponseEntity<StreamingResponseBody> retrieveDisposalConfirmationReport(
    @Parameter(description = "The ID of the disposal confirmation", required = true) @PathVariable(name = "id") String disposalConfirmationId,
    @Parameter(description = "Use a print-friendly layout", schema = @Schema(defaultValue = "false", implementation = Boolean.class)) @RequestParam(name = "to-print", defaultValue = "false", required = false) boolean toPrint) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      StreamResponse disposalConfirmationReport = disposalConfirmationService
        .createDisposalConfirmationReport(disposalConfirmationId, toPrint);

      return ApiUtils.okResponse(disposalConfirmationReport, null);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RODAException | IOException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_DISPOSAL_CONFIRMATION_ID_PARAM,
        disposalConfirmationId, "printLayout", toPrint);
    }
  }

  @Override
  public Job destroyRecordsInDisposalConfirmation(@RequestBody SelectedItemsRequest selectedItems) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      return disposalConfirmationService.destroyRecordsInDisposalConfirmation(requestContext.getUser(),
        CommonServicesUtils.convertSelectedItems(selectedItems, DisposalConfirmation.class));
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selectedItems);
    }
  }

  @Override
  public Job permanentlyDeleteRecordsInDisposalConfirmation(@RequestBody SelectedItemsRequest selectedItems) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      return disposalConfirmationService.permanentlyDeleteRecordsInDisposalConfirmation(requestContext.getUser(),
        CommonServicesUtils.convertSelectedItems(selectedItems, DisposalConfirmation.class));
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selectedItems);
    }
  }

  @Override
  public Job restoreDisposalConfirmation(@RequestBody SelectedItemsRequest selectedItems) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      return disposalConfirmationService.restoreRecordsInDisposalConfirmation(requestContext.getUser(),
          CommonServicesUtils.convertSelectedItems(selectedItems, DisposalConfirmation.class));
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selectedItems);
    }
  }

  @Override
  public Job recoverDisposalConfirmation(@RequestBody SelectedItemsRequest selectedItems) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      return disposalConfirmationService.recoverDisposalConfirmationExecutionFailed(requestContext.getUser(),
          CommonServicesUtils.convertSelectedItems(selectedItems, DisposalConfirmation.class));
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selectedItems);
    }
  }

  @Override
  public Job deleteDisposalConfirmation(@RequestBody SelectedItemsRequest selectedItems, String details) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      return disposalConfirmationService.deleteDisposalConfirmation(requestContext.getUser(),
        CommonServicesUtils.convertSelectedItems(selectedItems, DisposalConfirmation.class), details);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_PARAM, selectedItems);
    }
  }

  @Override
  public Job createDisposalConfirmation(@RequestBody DisposalConfirmationCreateRequest createRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      return disposalConfirmationService.createDisposalConfirmation(requestContext.getUser(), createRequest);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state,
        RodaConstants.CONTROLLER_DISPOSAL_CONFIRMATION_METADATA_PARAM, createRequest.getForm(),
        RodaConstants.CONTROLLER_SELECTED_ITEMS_PARAM, createRequest.getSelectedItems(), "title",
        createRequest.getTitle());
    }
  }

  @Override
  public DisposalConfirmationForm retrieveDisposalConfirmationForm() {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      return disposalConfirmationService.retrieveDisposalConfirmationExtraBundle();
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, state);
    }
  }

  @Override
  public ResponseEntity<StreamingResponseBody> exportToCSV(String findRequestString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    // delegate
    return ApiUtils.okResponse(indexService.exportToCSV(requestContext.getUser(), findRequestString,
      DisposalConfirmation.class, requestContext));
  }
}
