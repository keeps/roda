/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.SelectedItemsUtils;
import org.roda.core.data.v2.aip.AssessmentRequest;
import org.roda.core.data.v2.aip.MoveRequest;
import org.roda.core.data.v2.disposal.hold.DisposalHoldState;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.disposal.schedule.DisposalScheduleState;
import org.roda.core.data.v2.generics.DeleteRequest;
import org.roda.core.data.v2.generics.select.SelectedItemsListRequest;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.disposalhold.DisassociateDisposalHoldRequest;
import org.roda.core.data.v2.representation.ChangeTypeRequest;
import org.roda.wui.client.browse.BrowseRepresentation;
import org.roda.wui.client.browse.BrowseTop;
import org.roda.wui.client.browse.CreateDescriptiveMetadata;
import org.roda.wui.client.browse.EditPermissions;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.DisposalDialogs;
import org.roda.wui.client.common.dialogs.RepresentationDialogs;
import org.roda.wui.client.common.dialogs.SelectAipDialog;
import org.roda.wui.client.common.dialogs.utils.DisposalHoldDialogResult;
import org.roda.wui.client.common.dialogs.utils.DisposalScheduleDialogResult;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.ingest.appraisal.IngestAppraisal;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.client.search.Search;
import org.roda.wui.client.services.DisposalHoldRestService;
import org.roda.wui.client.services.DisposalScheduleRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class AipToolbarActions extends AbstractActionable<IndexedAIP> {

  public static final IndexedAIP NO_AIP_OBJECT = null;
  public static final String NO_AIP_PARENT = null;
  public static final AIPState NO_AIP_STATE = null;
  public static final String BTN_EDIT = "btn-edit";
  private static final AipToolbarActions GENERAL_INSTANCE = new AipToolbarActions(NO_AIP_PARENT, NO_AIP_STATE, null);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final Set<AIPAction> POSSIBLE_ACTIONS_ON_NO_AIP_TOP = new HashSet<>(
    List.of(AIPAction.NEW_CHILD_AIP_TOP));
  private static final Set<AIPAction> POSSIBLE_ACTIONS_ON_NO_AIP_BELOW = new HashSet<>(
    List.of(AIPAction.NEW_CHILD_AIP_BELOW));
  private static final Set<AIPAction> POSSIBLE_ACTIONS_ON_SINGLE_AIP = new HashSet<>(
    Arrays.asList(AIPAction.DOWNLOAD, AIPAction.MOVE_IN_HIERARCHY, AIPAction.UPDATE_PERMISSIONS, AIPAction.REMOVE,
      AIPAction.NEW_PROCESS, AIPAction.DOWNLOAD_EVENTS, AIPAction.DOWNLOAD_DOCUMENTATION,
      AIPAction.DOWNLOAD_SUBMISSIONS, AIPAction.CHANGE_TYPE, AIPAction.ASSOCIATE_DISPOSAL_SCHEDULE,
      AIPAction.ASSOCIATE_DISPOSAL_HOLD, AIPAction.SEARCH_DESCENDANTS, AIPAction.SEARCH_PACKAGE,
      AIPAction.NEW_CHILD_AIP_BELOW, AIPAction.NEW_REPRESENTATION, AIPAction.CREATE_DESCRIPTIVE_METADATA));
  private static final Set<AIPAction> POSSIBLE_ACTIONS_ON_MULTIPLE_AIPS = new HashSet<>(
    Arrays.asList(AIPAction.MOVE_IN_HIERARCHY, AIPAction.UPDATE_PERMISSIONS, AIPAction.REMOVE, AIPAction.NEW_PROCESS,
      AIPAction.CHANGE_TYPE, AIPAction.ASSOCIATE_DISPOSAL_SCHEDULE, AIPAction.ASSOCIATE_DISPOSAL_HOLD));
  private static final Set<AIPAction> APPRAISAL_ACTIONS = new HashSet<>(
    Arrays.asList(AIPAction.APPRAISAL_ACCEPT, AIPAction.APPRAISAL_REJECT));
  private static final Set<Action<IndexedAIP>> DOWNLOAD_ACTIONS = new HashSet<>(Arrays.asList(AIPAction.DOWNLOAD_EVENTS,
    AIPAction.DOWNLOAD_DOCUMENTATION, AIPAction.DOWNLOAD, AIPAction.DOWNLOAD_SUBMISSIONS));
  private static final Set<Action<IndexedAIP>> SEARCH_WITHIN_ACTIONS = new HashSet<>(
    Arrays.asList(AIPAction.SEARCH_PACKAGE, AIPAction.SEARCH_DESCENDANTS));
  private final String parentAipId;
  private final AIPState parentAipState;
  private final Permissions permissions;

  private AipToolbarActions(String parentAipId, AIPState parentAipState, Permissions permissions) {
    this.parentAipId = parentAipId;
    this.parentAipState = parentAipState;
    this.permissions = permissions;
  }

  public static AipToolbarActions get() {
    return GENERAL_INSTANCE;
  }

  public static AipToolbarActions get(String parentAipId, AIPState parentAipState, Permissions permissions) {
    return new AipToolbarActions(parentAipId, parentAipState, permissions);
  }

  public static AipToolbarActions getWithoutNoAipActions(String parentAipId, AIPState parentAipState,
    Permissions permissions) {
    return new AipToolbarActions(parentAipId, parentAipState, permissions) {
      @Override
      public CanActResult contextCanAct(Action<IndexedAIP> action) {
        return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonNoObjectSelected());
      }
    };
  }

  @Override
  public AIPAction[] getActions() {
    return AIPAction.values();
  }

  @Override
  public AIPAction actionForName(String name) {
    return AIPAction.valueOf(name);
  }

  @Override
  public CanActResult userCanAct(Action<IndexedAIP> action) {
    return new CanActResult(hasPermissions(action, permissions), CanActResult.Reason.USER,
      messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<IndexedAIP> action) {
    if (!AIPState.UNDER_APPRAISAL.equals(parentAipState)) {
      if (Objects.equals(parentAipId, NO_AIP_PARENT)) {
        return new CanActResult(POSSIBLE_ACTIONS_ON_NO_AIP_TOP.contains(action), CanActResult.Reason.CONTEXT,
          messages.reasonNoParentObject());
      } else {
        return new CanActResult(POSSIBLE_ACTIONS_ON_NO_AIP_BELOW.contains(action), CanActResult.Reason.CONTEXT,
          messages.reasonNoObjectSelected());
      }
    } else {
      return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonAffectedAIPUnderAppraisal());
    }
  }

  @Override
  public CanActResult userCanAct(Action<IndexedAIP> action, IndexedAIP aip) {
    if (aip == NO_AIP_OBJECT) {
      return new CanActResult(hasPermissions(action, permissions), CanActResult.Reason.USER,
        messages.reasonUserLacksPermission());
    } else if ((AIPAction.REMOVE.equals(action) || AIPAction.NEW_CHILD_AIP_BELOW.equals(action)
      || AIPAction.MOVE_IN_HIERARCHY.equals(action) || AIPAction.CHANGE_TYPE.equals(action)
      || AIPAction.NEW_REPRESENTATION.equals(action) || AIPAction.CREATE_DESCRIPTIVE_METADATA.equals(action))
      && (aip.isOnHold() || StringUtils.isNotBlank(aip.getDisposalConfirmationId()))) {
      return new CanActResult(false, CanActResult.Reason.USER, messages.reasonAIPProtectedByDisposalPolicy());
    } else if (AIPState.UNDER_APPRAISAL.equals(aip.getState()) && (AIPAction.CREATE_DESCRIPTIVE_METADATA.equals(action)
      || AIPAction.NEW_CHILD_AIP_BELOW.equals(action) || SEARCH_WITHIN_ACTIONS.contains(action)
      || DOWNLOAD_ACTIONS.contains(action) || AIPAction.NEW_PROCESS.equals(action)
      || AIPAction.CHANGE_TYPE.equals(action) || AIPAction.MOVE_IN_HIERARCHY.equals(action)
      || AIPAction.REMOVE.equals(action) || AIPAction.UPDATE_PERMISSIONS.equals(action)
      || AIPAction.ASSOCIATE_DISPOSAL_HOLD.equals(action) || AIPAction.ASSOCIATE_DISPOSAL_SCHEDULE.equals(action))) {
      return new CanActResult(false, CanActResult.Reason.USER, messages.reasonAIPUnderAppraisal());
    } else {
      return new CanActResult(hasPermissions(action, aip.getPermissions()), CanActResult.Reason.USER,
        messages.reasonUserLacksPermission());
    }
  }

  @Override
  public CanActResult contextCanAct(Action<IndexedAIP> action, IndexedAIP aip) {
    if (aip == NO_AIP_OBJECT) {
      return new CanActResult(POSSIBLE_ACTIONS_ON_NO_AIP_BELOW.contains(action), CanActResult.Reason.CONTEXT,
        messages.reasonNoObjectSelected());
    } else if (AIPState.DESTROYED.equals(parentAipState)) {
      return new CanActResult(false, CanActResult.Reason.CONTEXT, "");
    } else if (AIPState.UNDER_APPRAISAL.equals(aip.getState()) && AIPState.UNDER_APPRAISAL.equals(parentAipState)
      && Objects.equals(parentAipId, NO_AIP_PARENT)) {
      return new CanActResult(APPRAISAL_ACTIONS.contains(action), CanActResult.Reason.CONTEXT,
        messages.reasonAffectedAIPUnderAppraisal());
    } else if (AIPState.UNDER_APPRAISAL.equals(aip.getState())) {
      return new CanActResult(APPRAISAL_ACTIONS.contains(action) || POSSIBLE_ACTIONS_ON_SINGLE_AIP.contains(action),
        CanActResult.Reason.CONTEXT, messages.reasonAIPUnderAppraisal());
    } else if (action.equals(AIPAction.REMOVE)
      && (aip.isOnHold() || StringUtils.isNotBlank(aip.getDisposalScheduleId()))) {
      return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonAIPProtectedByDisposalPolicy());
    } else if (StringUtils.isNotBlank(aip.getDisposalConfirmationId()) && (action.equals(AIPAction.MOVE_IN_HIERARCHY)
      || action.equals(AIPAction.ASSOCIATE_DISPOSAL_SCHEDULE) || action.equals(AIPAction.ASSOCIATE_DISPOSAL_HOLD))) {
      return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonAIPProtectedByDisposalPolicy());
    } else if (action.equals(AIPAction.MOVE_IN_HIERARCHY) && aip.isOnHold()) {
      return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonAIPProtectedByDisposalPolicy());
    } else {
      return new CanActResult(POSSIBLE_ACTIONS_ON_SINGLE_AIP.contains(action), CanActResult.Reason.CONTEXT,
        messages.reasonCantActOnSingleObject());
    }
  }

  @Override
  public CanActResult userCanAct(Action<IndexedAIP> action, SelectedItems<IndexedAIP> objects) {
    return new CanActResult(hasPermissions(action, permissions), CanActResult.Reason.USER,
      messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<IndexedAIP> action, SelectedItems<IndexedAIP> objects) {
    if (AIPState.UNDER_APPRAISAL.equals(parentAipState)) {
      return new CanActResult(
        (!Objects.equals(parentAipId, NO_AIP_PARENT) && POSSIBLE_ACTIONS_ON_MULTIPLE_AIPS.contains(action))
          || APPRAISAL_ACTIONS.contains(action),
        CanActResult.Reason.CONTEXT, messages.reasonAffectedAIPUnderAppraisal());
    } else {
      return new CanActResult(POSSIBLE_ACTIONS_ON_MULTIPLE_AIPS.contains(action), CanActResult.Reason.CONTEXT,
        messages.reasonCantActOnMultipleObjects());
    }
  }

  @Override
  public void act(Action<IndexedAIP> action, AsyncCallback<ActionImpact> callback) {
    if (AIPAction.NEW_CHILD_AIP_TOP.equals(action) || AIPAction.NEW_CHILD_AIP_BELOW.equals(action)) {
      newChildAip(callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<IndexedAIP> action, IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    if (AIPAction.SEARCH_DESCENDANTS.equals(action)) {
      searchDescendants(aip);
    } else if (AIPAction.SEARCH_PACKAGE.equals(action)) {
      searchPackage(aip);
    } else if (AIPAction.DOWNLOAD.equals(action)) {
      download(aip, callback);
    } else if (AIPAction.MOVE_IN_HIERARCHY.equals(action)) {
      move(aip, callback);
    } else if (AIPAction.UPDATE_PERMISSIONS.equals(action)) {
      updatePermissions(aip, callback);
    } else if (AIPAction.REMOVE.equals(action)) {
      remove(aip, callback);
    } else if (AIPAction.NEW_PROCESS.equals(action)) {
      newProcess(aip, callback);
    } else if (AIPAction.DOWNLOAD_EVENTS.equals(action)) {
      downloadEvents(aip, callback);
    } else if (AIPAction.DOWNLOAD_SUBMISSIONS.equals(action)) {
      downloadSubmissions(aip, callback);
    } else if (AIPAction.APPRAISAL_ACCEPT.equals(action)) {
      appraisalAccept(aip, callback);
    } else if (AIPAction.APPRAISAL_REJECT.equals(action)) {
      appraisalReject(aip, callback);
    } else if (AIPAction.DOWNLOAD_DOCUMENTATION.equals(action)) {
      downloadDocumentation(aip, callback);
    } else if (AIPAction.CHANGE_TYPE.equals(action)) {
      changeType(aip, callback);
    } else if (AIPAction.ASSOCIATE_DISPOSAL_SCHEDULE.equals(action)) {
      associateDisposalSchedule(aip, callback);
    } else if (AIPAction.ASSOCIATE_DISPOSAL_HOLD.equals(action)) {
      manageDisposalHold(aip, callback);
    } else if (AIPAction.NEW_CHILD_AIP_TOP.equals(action) || AIPAction.NEW_CHILD_AIP_BELOW.equals(action)) {
      newChildAip(callback);
    } else if (AIPAction.NEW_REPRESENTATION.equals(action)) {
      newRepresentation(callback);
    } else if (AIPAction.CREATE_DESCRIPTIVE_METADATA.equals(action)) {
      createDescriptiveMetadata(aip, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<IndexedAIP> action, SelectedItems<IndexedAIP> aips, AsyncCallback<ActionImpact> callback) {
    if (AIPAction.MOVE_IN_HIERARCHY.equals(action)) {
      move(aips, callback);
    } else if (AIPAction.UPDATE_PERMISSIONS.equals(action)) {
      updatePermissions(aips, callback);
    } else if (AIPAction.REMOVE.equals(action)) {
      remove(aips, callback);
    } else if (AIPAction.NEW_PROCESS.equals(action)) {
      newProcess(aips, callback);
    } else if (AIPAction.APPRAISAL_ACCEPT.equals(action)) {
      appraisalAccept(aips, callback);
    } else if (AIPAction.APPRAISAL_REJECT.equals(action)) {
      appraisalReject(aips, callback);
    } else if (AIPAction.CHANGE_TYPE.equals(action)) {
      changeType(aips, callback);
    } else if (AIPAction.ASSOCIATE_DISPOSAL_SCHEDULE.equals(action)) {
      associateDisposalSchedule(aips, callback);
    } else if (AIPAction.ASSOCIATE_DISPOSAL_HOLD.equals(action)) {
      manageDisposalHold(aips, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  // ACTIONS
  private void searchDescendants(final IndexedAIP aip) {
    List<String> searchFilters = new ArrayList<>();

    searchFilters.add(RodaConstants.SEARCH_WITH_PREFILTER_HANDLER);
    searchFilters.add("title");
    searchFilters.add(messages.searchPrefilterDescendantsOf(aip.getTitle()));

    searchFilters.add(SearchFilters.classesToHistoryTokens(IndexedAIP.class));
    searchFilters.add(RodaConstants.AIP_ANCESTORS);
    searchFilters.add(aip.getId());

    searchFilters.add(SearchFilters.classesToHistoryTokens(IndexedRepresentation.class));
    searchFilters.add(RodaConstants.REPRESENTATION_ANCESTORS);
    searchFilters.add(aip.getId());

    searchFilters.add(SearchFilters.classesToHistoryTokens(IndexedFile.class));
    searchFilters.add(RodaConstants.FILE_ANCESTORS);
    searchFilters.add(aip.getId());

    HistoryUtils.newHistory(Search.RESOLVER, searchFilters);
  }

  private void searchPackage(final IndexedAIP aip) {
    List<String> searchFilters = new ArrayList<>();

    searchFilters.add(RodaConstants.SEARCH_WITH_PREFILTER_HANDLER);
    searchFilters.add("title");
    searchFilters.add(messages.searchPreFilterInThisPackage(aip.getTitle()));

    searchFilters.add(SearchFilters.classesToHistoryTokens(IndexedRepresentation.class));
    searchFilters.add(RodaConstants.REPRESENTATION_AIP_ID);
    searchFilters.add(aip.getId());

    searchFilters.add(SearchFilters.classesToHistoryTokens(IndexedFile.class));
    searchFilters.add(RodaConstants.FILE_AIP_ID);
    searchFilters.add(aip.getId());

    HistoryUtils.newHistory(Search.RESOLVER, searchFilters);
  }

  private void newChildAip(final AsyncCallback<ActionImpact> callback) {
    String aipType = RodaConstants.AIP_TYPE_MIXED;

    Services service = new Services("Create Child AIP", "create");

    service.aipResource(s -> s.createAIP(parentAipId, aipType)).whenComplete((value, error) -> {
      if (value != null) {
        LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
        callback.onSuccess(ActionImpact.NONE);
        HistoryUtils.newHistory(CreateDescriptiveMetadata.RESOLVER, RodaConstants.RODA_OBJECT_AIP, value.getId(),
          CreateDescriptiveMetadata.NEW);
      }
    });
  }

  private void newRepresentation(final AsyncCallback<ActionImpact> callback) {
    Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
      RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
      new ActionNoAsyncCallback<String>(callback) {
        @Override
        public void onSuccess(String details) {
          Services services = new Services("Create representation", "create");
          services.representationResource(s -> s.createRepresentation(parentAipId, "MIXED", details))
            .whenComplete((representation, error) -> {
              if (representation != null) {
                HistoryUtils.newHistory(BrowseRepresentation.RESOLVER, parentAipId, representation.getId());
                callback.onSuccess(ActionImpact.UPDATED);
              }
            });
        }
      });
  }

  public void createDescriptiveMetadata(IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    if (aip != null) {
      HistoryUtils.newHistory(BrowseTop.RESOLVER, CreateDescriptiveMetadata.RESOLVER.getHistoryToken(),
        RodaConstants.RODA_OBJECT_AIP, aip.getId());
    }
    callback.onSuccess(ActionImpact.NONE);
  }

  private void download(IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    SafeUri downloadUri = RestUtils.createAIPDownloadUri(aip.getId());
    callback.onSuccess(ActionImpact.NONE);
    Window.Location.assign(downloadUri.asString());
  }

  private void move(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.moveConfirmDialogTitle(),
      messages.moveAllConfirmDialogMessageSingle(StringUtils.isNotBlank(aip.getTitle()) ? aip.getTitle() : aip.getId()),
      messages.dialogNo(), messages.dialogYes(), new ActionAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            final String aipId = aip.getId();
            boolean justActive = AIPState.ACTIVE.equals(aip.getState());

            Filter filter = new Filter(new NotSimpleFilterParameter(RodaConstants.INDEX_UUID, aipId));
            SelectAipDialog selectAipDialog = new SelectAipDialog(
              messages.moveItemTitle() + " " + (StringUtils.isNotBlank(aip.getTitle()) ? aip.getTitle() : aip.getId()),
              filter, justActive);
            selectAipDialog.setEmptyParentButtonVisible(true);
            selectAipDialog.setSingleSelectionMode();
            selectAipDialog.showAndCenter();
            selectAipDialog.addCloseHandler(e -> callback.onSuccess(ActionImpact.NONE));
            selectAipDialog.addValueChangeHandler(new ValueChangeHandler<IndexedAIP>() {

              @Override
              public void onValueChange(ValueChangeEvent<IndexedAIP> event) {
                final IndexedAIP parentAIP = event.getValue();
                final String parentId = (parentAIP != null) ? parentAIP.getId() : null;
                final SelectedItemsList<IndexedAIP> selected = new SelectedItemsList<>(Collections.singletonList(aipId),
                  IndexedAIP.class.getName());

                Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
                  RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
                  new ActionNoAsyncCallback<String>(callback) {
                    @Override
                    public void onSuccess(String details) {

                      Services service = new Services("Move AIP", "move");

                      MoveRequest request = new MoveRequest();
                      request.setParentId(parentId);
                      request.setDetails(details);
                      request.setItemsToMove(SelectedItemsUtils.convertToRESTRequest(selected));

                      service.aipResource(s -> s.moveAIPInHierarchy(request)).whenComplete((value, error) -> {
                        if (error == null) {
                          Toast.showInfo(messages.moveItemTitle(), messages.movingAIP());

                          Dialogs.showJobRedirectDialog(messages.moveJobCreatedMessage(), new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                              doActionCallbackNone();
                            }

                            @Override
                            public void onSuccess(final Void nothing) {
                              doActionCallbackNone();
                              HistoryUtils.newHistory(ShowJob.RESOLVER, value.getId());
                            }
                          });
                        }
                      });
                    }
                  });
              }
            });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  private void move(final SelectedItems<IndexedAIP> selected, final AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(IndexedAIP.class, selected, new ActionNoAsyncCallback<Long>(callback) {

      @Override
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.moveConfirmDialogTitle(), messages.moveSelectedConfirmDialogMessage(size),
          messages.dialogNo(), messages.dialogYes(), new ActionNoAsyncCallback<Boolean>(callback) {

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                int counter = 0;
                boolean justActive = parentAipState == null || AIPState.ACTIVE.equals(parentAipState);
                Filter filter = new Filter();

                if (selected instanceof SelectedItemsList) {
                  SelectedItemsList<IndexedAIP> list = (SelectedItemsList<IndexedAIP>) selected;
                  counter = list.getIds().size();
                  if (counter <= RodaConstants.DIALOG_FILTER_LIMIT_NUMBER) {
                    for (String id : list.getIds()) {
                      filter.add(new NotSimpleFilterParameter(RodaConstants.AIP_ANCESTORS, id));
                      filter.add(new NotSimpleFilterParameter(RodaConstants.INDEX_UUID, id));
                    }
                  }

                  if (parentAipId != null) {
                    filter.add(new NotSimpleFilterParameter(RodaConstants.INDEX_UUID, parentAipId));
                  }
                } else if (selected instanceof SelectedItemsFilter && parentAipId != null) {
                  filter.add(new NotSimpleFilterParameter(RodaConstants.INDEX_UUID, parentAipId));
                }

                SelectAipDialog selectAipDialog = new SelectAipDialog(messages.moveItemTitle(), filter, justActive);
                selectAipDialog.setEmptyParentButtonVisible(parentAipId != null);
                selectAipDialog.showAndCenter();
                if (counter > 0 && counter <= RodaConstants.DIALOG_FILTER_LIMIT_NUMBER) {
                  selectAipDialog.addStyleName("object-dialog");
                }

                selectAipDialog.addCloseHandler(e -> callback.onSuccess(ActionImpact.NONE));
                selectAipDialog.addValueChangeHandler(new ValueChangeHandler<IndexedAIP>() {

                  @Override
                  public void onValueChange(ValueChangeEvent<IndexedAIP> event) {
                    final IndexedAIP parentAIP = event.getValue();
                    final String parentId = (parentAIP != null) ? parentAIP.getId() : null;

                    Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null,
                      messages.outcomeDetailPlaceholder(), RegExp.compile(".*"), messages.cancelButton(),
                      messages.confirmButton(), false, false, new ActionNoAsyncCallback<String>(callback) {

                        @Override
                        public void onSuccess(String details) {
                          Services service = new Services("Move AIP", "move");

                          MoveRequest request = new MoveRequest();
                          request.setParentId(parentId);
                          request.setDetails(details);
                          request.setItemsToMove(SelectedItemsUtils.convertToRESTRequest(selected));

                          service.aipResource(s -> s.moveAIPInHierarchy(request)).whenComplete((value, error) -> {
                            if (error == null) {
                              Toast.showInfo(messages.runningInBackgroundTitle(),
                                messages.runningInBackgroundDescription());

                              doActionCallbackNone();
                              if (value != null) {
                                HistoryUtils.newHistory(ShowJob.RESOLVER, value.getId());
                              } else {
                                HistoryUtils.newHistory(InternalProcess.RESOLVER);
                              }
                            }
                          });
                        }
                      });
                  }
                });
              }
            }
          });
      }
    });
  }

  private void updatePermissions(IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(EditPermissions.AIP_RESOLVER, aip.getId());
  }

  private void updatePermissions(SelectedItems<IndexedAIP> aips, AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    LastSelectedItemsSingleton.getInstance().setSelectedItems(aips);
    LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(EditPermissions.AIP_RESOLVER);
  }

  private void remove(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.removeConfirmDialogTitle(),
      messages
        .removeAllConfirmDialogMessageSingle(StringUtils.isNotBlank(aip.getTitle()) ? aip.getTitle() : aip.getId()),
      messages.dialogNo(), messages.dialogYes(), new ActionNoAsyncCallback<Boolean>(callback) {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Services services = new Services("Delete AIP", "deletion");
            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
              new ActionNoAsyncCallback<String>(callback) {

                @Override
                public void onSuccess(final String details) {
                  DeleteRequest request = new DeleteRequest();
                  request.setDetails(details);
                  request.setItemsToDelete(new SelectedItemsListRequest(Collections.singletonList(aip.getUUID())));
                  services.aipResource(s -> s.deleteAIPs(request)).whenComplete((value, error) -> {
                    if (error == null) {
                      Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(), new AsyncCallback<Void>() {

                        @Override
                        public void onFailure(Throwable caught) {
                          Toast.showInfo(messages.removingSuccessTitle(), messages.removingSuccessMessage(1L));
                          doActionCallbackDestroyed();
                        }

                        @Override
                        public void onSuccess(final Void nothing) {
                          doActionCallbackNone();
                          HistoryUtils.newHistory(ShowJob.RESOLVER, value.getId());
                        }
                      });
                    }
                  });
                }
              });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  private void remove(final SelectedItems<IndexedAIP> selected, final AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(IndexedAIP.class, selected, new ActionNoAsyncCallback<Long>(callback) {

      @Override
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.removeConfirmDialogTitle(),
          messages.removeSelectedConfirmDialogMessage(size), messages.dialogNo(), messages.dialogYes(),
          new ActionNoAsyncCallback<Boolean>(callback) {

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                Services service = new Services("Delete AIPs", "deletion");
                Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
                  RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
                  new ActionNoAsyncCallback<String>(callback) {

                    @Override
                    public void onSuccess(final String details) {
                      DeleteRequest request = new DeleteRequest();
                      request.setDetails(details);
                      request.setSelectedItemsToDelete(selected);

                      service.aipResource(s -> s.deleteAIPs(request)).whenComplete((value, error) -> {
                        if (error == null) {
                          Toast.showInfo(messages.runningInBackgroundTitle(),
                            messages.runningInBackgroundDescription());

                          Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(), new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {

                              doActionCallbackDestroyed();
                            }

                            @Override
                            public void onSuccess(final Void nothing) {
                              doActionCallbackNone();
                              HistoryUtils.newHistory(ShowJob.RESOLVER, value.getId());
                            }
                          });
                        }
                      });

                    }
                  });
              } else {
                doActionCallbackNone();
              }
            }
          });
      }
    });
  }

  private void newProcess(IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    LastSelectedItemsSingleton.getInstance().setSelectedItems(objectToSelectedItems(aip, IndexedAIP.class));
    LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
  }

  private void newProcess(SelectedItems<IndexedAIP> aips, AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    LastSelectedItemsSingleton.getInstance().setSelectedItems(aips);
    LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
  }

  private void downloadEvents(IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    SafeUri downloadUri = RestUtils.createPreservationMetadataDownloadUri(aip.getId());
    callback.onSuccess(ActionImpact.NONE);
    Window.Location.assign(downloadUri.asString());
  }

  private void appraisalAccept(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    appraisalAccept(objectToSelectedItems(aip, IndexedAIP.class), callback);
  }

  private void appraisalAccept(final SelectedItems<IndexedAIP> aips, final AsyncCallback<ActionImpact> callback) {

    Services service = new Services("Accept assessment", "post");

    AssessmentRequest request = new AssessmentRequest();
    request.setItems(SelectedItemsUtils.convertToRESTRequest(aips));
    request.setAccept(true);

    service.aipResource(s -> s.appraisal(request)).whenComplete((value, error) -> {
      if (error == null) {
        Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

        Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

          @Override
          public void onFailure(Throwable caught) {
            callback.onSuccess(ActionImpact.UPDATED);
          }

          @Override
          public void onSuccess(final Void nothing) {
            callback.onSuccess(ActionImpact.NONE);
            HistoryUtils.newHistory(ShowJob.RESOLVER, value.getId());
          }
        });
      }
    });
  }

  private void appraisalReject(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    appraisalReject(objectToSelectedItems(aip, IndexedAIP.class), callback);
  }

  private void appraisalReject(final SelectedItems<IndexedAIP> aips, final AsyncCallback<ActionImpact> callback) {
    Dialogs.showPromptDialog(messages.rejectMessage(), messages.rejectQuestion(), null, null, RegExp.compile(".+"),
      messages.dialogCancel(), messages.dialogOk(), true, false, new ActionNoAsyncCallback<String>(callback) {

        @Override
        public void onSuccess(final String rejectReason) {
          Services service = new Services("Reject Assessment", "post");

          AssessmentRequest request = new AssessmentRequest();
          request.setItems(SelectedItemsUtils.convertToRESTRequest(aips));
          request.setAccept(false);
          request.setRejectReason(rejectReason);

          service.aipResource(s -> s.appraisal(request)).whenComplete((value, error) -> {
            if (error == null) {
              Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

              Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                @Override
                public void onFailure(Throwable caught) {
                  doActionCallbackNone();
                  HistoryUtils.newHistory(IngestAppraisal.RESOLVER);
                }

                @Override
                public void onSuccess(final Void nothing) {
                  doActionCallbackNone();
                  HistoryUtils.newHistory(ShowJob.RESOLVER, value.getId());
                }
              });
            }
          });

        }
      });
  }

  private void downloadDocumentation(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    Services service = new Services("Download documentation", "get");

    service.aipResource(s -> s.getDocumentation(aip.getId())).whenComplete((value, throwable) -> {
      if (throwable == null) {
        if (Boolean.TRUE.equals(value)) {
          SafeUri downloadUri = RestUtils.createAIPPartDownloadUri(aip.getId(),
            RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION);
          callback.onSuccess(ActionImpact.NONE);
          Window.Location.assign(downloadUri.asString());
        } else {
          Toast.showInfo(messages.downloadNoDocumentationTitle(), messages.downloadNoDocumentationDescription());
          callback.onSuccess(ActionImpact.NONE);
        }
      } else {
        callback.onFailure(throwable);
      }
    });
  }

  private void downloadSubmissions(IndexedAIP aip, AsyncCallback<ActionImpact> callback) {

    Services service = new Services("Download submission", "get");

    service.aipResource(s -> s.getSubmissions(aip.getId())).whenComplete((value, throwable) -> {
      if (throwable == null) {
        if (value) {
          SafeUri downloadUri = RestUtils.createAIPPartDownloadUri(aip.getId(),
            RodaConstants.STORAGE_DIRECTORY_SUBMISSION);
          callback.onSuccess(ActionImpact.NONE);
          Window.Location.assign(downloadUri.asString());
        } else {
          Toast.showInfo(messages.downloadNoSubmissionsTitle(), messages.downloadNoSubmissionsDescription());
          callback.onSuccess(ActionImpact.NONE);
        }
      } else {
        callback.onFailure(throwable);
      }
    });
  }

  private void changeType(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    changeType(objectToSelectedItems(aip, IndexedAIP.class), callback);
  }

  private void changeType(final SelectedItems<IndexedAIP> aips, final AsyncCallback<ActionImpact> callback) {
    Services service = new Services("Change AIP type", "update");
    service.aipResource(s -> s.getTypeOptions(LocaleInfo.getCurrentLocale().getLocaleName()))
      .whenComplete((value, error) -> {
        if (error == null) {
          RepresentationDialogs.showPromptDialogRepresentationTypes(messages.changeTypeTitle(), null,
            messages.cancelButton(), messages.confirmButton(), value.getTypes(), value.isControlled(),
            new ActionNoAsyncCallback<String>(callback) {

              @Override
              public void onSuccess(final String newType) {
                Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
                  RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
                  new ActionNoAsyncCallback<String>(callback) {

                    @Override
                    public void onSuccess(String details) {
                      ChangeTypeRequest request = new ChangeTypeRequest();
                      request.setType(newType);
                      request.setDetails(details);
                      request.setItems(SelectedItemsUtils.convertToRESTRequest(aips));
                      service.aipResource(s -> s.changeAIPType(request)).whenComplete((value, error) -> {

                        if (error == null) {
                          Toast.showInfo(messages.runningInBackgroundTitle(),
                            messages.runningInBackgroundDescription());

                          Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                              doActionCallbackUpdated();
                            }

                            @Override
                            public void onSuccess(final Void nothing) {
                              doActionCallbackNone();
                              HistoryUtils.newHistory(ShowJob.RESOLVER, value.getId());
                            }
                          });
                        }
                      });
                    }
                  });
              }
            });
        }
      });
  }

  private void associateDisposalSchedule(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    associateDisposalSchedule(objectToSelectedItems(aip, IndexedAIP.class), callback);
  }

  private void associateDisposalSchedule(final SelectedItems<IndexedAIP> aips,
    final AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(IndexedAIP.class, aips, new ActionNoAsyncCallback<Long>(callback) {
      @Override
      public void onSuccess(final Long size) {
        Services services = new Services("List disposal schedules", "get");
        services.disposalScheduleResource(DisposalScheduleRestService::listDisposalSchedules)
          .whenComplete((disposalSchedules, caught) -> {
            if (caught != null) {
              AsyncCallbackUtils.defaultFailureTreatment(caught);
              callback.onFailure(caught);
            } else {
              // Show the active disposal schedules only
              disposalSchedules.getObjects()
                .removeIf(schedule -> DisposalScheduleState.INACTIVE.equals(schedule.getState()));
              DisposalDialogs.showDisposalScheduleSelection(messages.disposalScheduleSelectionDialogTitle(),
                disposalSchedules, new ActionNoAsyncCallback<DisposalScheduleDialogResult>(callback) {
                  @Override
                  public void onFailure(Throwable caught) {
                    doActionCallbackNone();
                  }

                  @Override
                  public void onSuccess(DisposalScheduleDialogResult result) {
                    if (DisposalScheduleDialogResult.ActionType.ASSOCIATE.equals(result.getActionType())) {
                      associateDisposalSchedule(aips, size, result, callback);
                    } else if (DisposalScheduleDialogResult.ActionType.CLEAR.equals(result.getActionType())) {
                      disassociateDisposalSchedule(aips, size, callback);
                    }
                  }
                });
            }
          });
      }
    });
  }

  private void disassociateDisposalSchedule(SelectedItems<IndexedAIP> aips, Long size,
    AsyncCallback<ActionImpact> callback) {

    Dialogs.showConfirmDialog(messages.dissociateDisposalScheduleDialogTitle(),
      messages.dissociateDisposalScheduleDialogMessage(size), messages.dialogNo(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            Services services = new Services("Disassociate disposal schedule from AIP", "job");
            services
              .disposalScheduleResource(
                s -> s.disassociatedDisposalSchedule(SelectedItemsUtils.convertToRESTRequest(aips)))
              .whenComplete((job, throwable) -> {
                if (throwable != null) {
                  callback.onFailure(throwable);
                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
                } else {
                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

                      Timer timer = new Timer() {
                        @Override
                        public void run() {
                          doActionCallbackUpdated();
                        }
                      };

                      timer.schedule(RodaConstants.ACTION_TIMEOUT);
                    }

                    @Override
                    public void onSuccess(final Void nothing) {
                      doActionCallbackNone();
                      HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
                    }
                  });
                }
              });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  private void associateDisposalSchedule(SelectedItems<IndexedAIP> aips, Long size,
    DisposalScheduleDialogResult dialogResult, AsyncCallback<ActionImpact> callback) {
    DisposalSchedule disposalSchedule = dialogResult.getDisposalSchedule();

    Dialogs.showConfirmDialog(messages.associateDisposalScheduleDialogTitle(),
      messages.associateDisposalScheduleDialogMessage(size), messages.dialogNo(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            Services services = new Services("Associate disposal schedule", "job");
            services.disposalScheduleResource(s -> s
              .associatedDisposalSchedule(SelectedItemsUtils.convertToRESTRequest(aips), disposalSchedule.getId()))
              .whenComplete((job, throwable) -> {
                if (throwable != null) {
                  callback.onFailure(throwable);
                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
                } else {
                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

                      Timer timer = new Timer() {
                        @Override
                        public void run() {
                          doActionCallbackUpdated();
                        }
                      };

                      timer.schedule(RodaConstants.ACTION_TIMEOUT);
                    }

                    @Override
                    public void onSuccess(final Void nothing) {
                      doActionCallbackNone();
                      HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
                    }
                  });
                }
              });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  private void manageDisposalHold(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    manageDisposalHold(objectToSelectedItems(aip, IndexedAIP.class), callback);
  }

  private void manageDisposalHold(final SelectedItems<IndexedAIP> aips, final AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(IndexedAIP.class, aips, new ActionNoAsyncCallback<Long>(callback) {
      @Override
      public void onSuccess(final Long size) {
        Services services = new Services("Get disposal holds", "get");
        services.disposalHoldResource(DisposalHoldRestService::listDisposalHolds)
          .whenComplete((disposalHolds, throwable) -> {
            if (throwable != null) {
              AsyncCallbackUtils.defaultFailureTreatment(throwable);
              callback.onFailure(throwable);
            } else {
              disposalHolds.getObjects().removeIf(p -> DisposalHoldState.LIFTED.equals(p.getState()));
              DisposalDialogs.showDisposalHoldSelection(messages.disposalHoldSelectionDialogTitle(), disposalHolds,
                new ActionNoAsyncCallback<DisposalHoldDialogResult>(callback) {
                  @Override
                  public void onFailure(Throwable caught) {
                    doActionCallbackNone();
                  }

                  @Override
                  public void onSuccess(DisposalHoldDialogResult result) {
                    if (DisposalHoldDialogResult.ActionType.CLEAR.equals(result.getActionType())) {
                      clearDisposalHolds(aips, size, callback);
                    } else if (DisposalHoldDialogResult.ActionType.ASSOCIATE.equals(result.getActionType())) {
                      applyDisposalHold(aips, size, result, false, callback);
                    } else if (DisposalHoldDialogResult.ActionType.OVERRIDE.equals(result.getActionType())) {
                      applyDisposalHold(aips, size, result, true, callback);
                    }
                  }
                });
            }
          });
      }
    });
  }

  private void applyDisposalHold(final SelectedItems<IndexedAIP> aips, final Long size,
    DisposalHoldDialogResult holdDialogResult, boolean override, final AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.applyDisposalHoldDialogTitle(),
      messages.applyDisposalHoldDialogMessage(size.intValue()), messages.dialogNo(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            Services services = new Services("Apply disposal hold", "job");
            services.disposalHoldResource(s -> s.applyDisposalHold(SelectedItemsUtils.convertToRESTRequest(aips),
              holdDialogResult.getDisposalHold().getId(), override)).whenComplete((job, throwable) -> {
                if (throwable != null) {
                  callback.onFailure(null);
                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
                } else {
                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                      Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

                      Timer timer = new Timer() {
                        @Override
                        public void run() {
                          doActionCallbackUpdated();
                        }
                      };
                      timer.schedule(RodaConstants.ACTION_TIMEOUT);
                    }

                    @Override
                    public void onSuccess(final Void nothing) {
                      doActionCallbackNone();
                      HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
                    }
                  });
                }
              });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  private void clearDisposalHolds(final SelectedItems<IndexedAIP> aips, final Long size,
    final AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.clearDisposalHoldDialogTitle(),
      messages.clearDisposalHoldDialogMessage(size.intValue()), messages.dialogNo(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
              new ActionNoAsyncCallback<String>(callback) {
                @Override
                public void onFailure(Throwable caught) {
                  // do nothing
                }

                @Override
                public void onSuccess(String details) {
                  DisassociateDisposalHoldRequest request = new DisassociateDisposalHoldRequest();
                  request.setSelectedItems(SelectedItemsUtils.convertToRESTRequest(aips));
                  request.setClear(true);
                  request.setDetails(details);
                  Services services = new Services("Disassociate disposal holds", "job");
                  services.disposalHoldResource(s -> s.disassociateDisposalHold(request, null))
                    .whenComplete((job, throwable) -> {
                      if (throwable != null) {
                        callback.onFailure(throwable);
                        HistoryUtils.newHistory(InternalProcess.RESOLVER);
                      } else {
                        Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                          @Override
                          public void onFailure(Throwable caught) {
                            Toast.showInfo(messages.runningInBackgroundTitle(),
                              messages.runningInBackgroundDescription());

                            Timer timer = new Timer() {
                              @Override
                              public void run() {
                                doActionCallbackUpdated();
                              }
                            };

                            timer.schedule(RodaConstants.ACTION_TIMEOUT);
                          }

                          @Override
                          public void onSuccess(final Void nothing) {
                            doActionCallbackNone();
                            HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
                          }
                        });
                      }
                    });
                }
              });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  @Override
  public ActionableBundle<IndexedAIP> createActionsBundle() {
    ActionableBundle<IndexedAIP> aipActionableBundle = new ActionableBundle<>();

    // SEARCH
    ActionableGroup<IndexedAIP> searchGroup = new ActionableGroup<>(messages.searchWithin(), "btn-search");
    searchGroup.addButton(messages.searchDescendants(), AIPAction.SEARCH_DESCENDANTS, ActionImpact.NONE, "btn-sitemap");
    searchGroup.addButton(messages.searchPackage(), AIPAction.SEARCH_PACKAGE, ActionImpact.NONE, "btn-archive");

    // DOWNLOAD
    ActionableGroup<IndexedAIP> downloadGroup = new ActionableGroup<>(messages.downloadButton(), "btn-download");
    downloadGroup.addButton(messages.downloadButton() + " " + messages.oneOfAObject(AIP.class.getName()),
      AIPAction.DOWNLOAD, ActionImpact.NONE, "btn-download");
    downloadGroup.addButton(messages.preservationEventsDownloadButton(), AIPAction.DOWNLOAD_EVENTS, ActionImpact.NONE,
      "btn-download");
    downloadGroup.addButton(messages.downloadDocumentation(), AIPAction.DOWNLOAD_DOCUMENTATION, ActionImpact.NONE,
      "btn-download");
    downloadGroup.addButton(messages.downloadSubmissions(), AIPAction.DOWNLOAD_SUBMISSIONS, ActionImpact.NONE,
      "btn-download");

    // MANAGEMENT
    ActionableGroup<IndexedAIP> managementGroup = new ActionableGroup<>(messages.manage(), "btn-edit");
    managementGroup.addButton(messages.newDescriptiveMetadataTitle(), AIPAction.CREATE_DESCRIPTIVE_METADATA,
      ActionImpact.UPDATED, "btn-plus-circle");
    managementGroup.addButton(messages.newArchivalPackage(), AIPAction.NEW_CHILD_AIP_TOP, ActionImpact.UPDATED,
      "btn-plus-circle");
    managementGroup.addButton(messages.newSublevel(), AIPAction.NEW_CHILD_AIP_BELOW, ActionImpact.UPDATED,
      "btn-plus-circle");
    managementGroup.addButton(messages.newRepresentationButton(), AIPAction.NEW_REPRESENTATION, ActionImpact.UPDATED,
      "btn-plus-circle");
    managementGroup.addButton(messages.changeTypeButton(), AIPAction.CHANGE_TYPE, ActionImpact.UPDATED, BTN_EDIT);
    managementGroup.addButton(messages.moveArchivalPackage(), AIPAction.MOVE_IN_HIERARCHY, ActionImpact.UPDATED,
      BTN_EDIT);
    managementGroup.addButton(messages.editArchivalPackagePermissions(), AIPAction.UPDATE_PERMISSIONS,
      ActionImpact.UPDATED, BTN_EDIT);
    managementGroup.addButton(messages.removeArchivalPackage(), AIPAction.REMOVE, ActionImpact.DESTROYED, "btn-ban");

    // PRESERVATION
    ActionableGroup<IndexedAIP> preservationGroup = new ActionableGroup<>(messages.preservationTitle(),
      "btn-play-circle");
    preservationGroup.addButton(messages.runAction(), AIPAction.NEW_PROCESS, ActionImpact.UPDATED, "btn-play");

    // APPRAISAL
    ActionableGroup<IndexedAIP> appraisalGroup = new ActionableGroup<>(messages.appraisalTitle(), "btn-assessment");
    appraisalGroup.addButton(messages.appraisalAccept(), AIPAction.APPRAISAL_ACCEPT, ActionImpact.UPDATED, "btn-play");
    appraisalGroup.addButton(messages.appraisalReject(), AIPAction.APPRAISAL_REJECT, ActionImpact.DESTROYED, "btn-ban");

    // Disposal
    ActionableGroup<IndexedAIP> disposalGroup = new ActionableGroup<>(messages.disposalTitle(), "btn-calendar");
    disposalGroup.addButton(messages.associateDisposalScheduleButton(), AIPAction.ASSOCIATE_DISPOSAL_SCHEDULE,
      ActionImpact.NONE, "btn-calendar");
    disposalGroup.addButton(messages.associateDisposalHoldButton(), AIPAction.ASSOCIATE_DISPOSAL_HOLD,
      ActionImpact.NONE, "btn-lock");

    aipActionableBundle.addGroup(searchGroup).addGroup(downloadGroup).addGroup(managementGroup).addGroup(disposalGroup)
      .addGroup(appraisalGroup).addGroup(preservationGroup);
    return aipActionableBundle;
  }

  public enum AIPAction implements Action<IndexedAIP> {
    NEW_CHILD_AIP_BELOW(RodaConstants.PERMISSION_METHOD_CREATE_AIP_BELOW),
    NEW_CHILD_AIP_TOP(RodaConstants.PERMISSION_METHOD_CREATE_AIP_TOP), DOWNLOAD(),
    MOVE_IN_HIERARCHY(RodaConstants.PERMISSION_METHOD_MOVE_AIP_IN_HIERARCHY),
    UPDATE_PERMISSIONS(RodaConstants.PERMISSION_METHOD_UPDATE_AIP_PERMISSIONS),
    REMOVE(RodaConstants.PERMISSION_METHOD_DELETE_AIP), NEW_PROCESS(RodaConstants.PERMISSION_METHOD_CREATE_JOB),
    DOWNLOAD_EVENTS(RodaConstants.PERMISSION_METHOD_FIND_PRESERVATION_EVENT), DOWNLOAD_SUBMISSIONS(),
    APPRAISAL_ACCEPT(RodaConstants.PERMISSION_METHOD_APPRAISAL),
    APPRAISAL_REJECT(RodaConstants.PERMISSION_METHOD_APPRAISAL), DOWNLOAD_DOCUMENTATION(),
    CHANGE_TYPE(RodaConstants.PERMISSION_METHOD_CHANGE_AIP_TYPE),
    ASSOCIATE_DISPOSAL_SCHEDULE(RodaConstants.PERMISSION_METHOD_ASSOCIATE_DISPOSAL_SCHEDULE),
    ASSOCIATE_DISPOSAL_HOLD(RodaConstants.PERMISSION_METHOD_ASSOCIATE_DISPOSAL_HOLD), SEARCH_DESCENDANTS(),
    SEARCH_PACKAGE(), NEW_REPRESENTATION(RodaConstants.PERMISSION_METHOD_CREATE_REPRESENTATION),
    CREATE_DESCRIPTIVE_METADATA(RodaConstants.PERMISSION_METHOD_UPDATE_AIP_DESCRIPTIVE_METADATA_FILE);

    private final List<String> methods;

    AIPAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }
}
