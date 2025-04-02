/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.SelectedItemsUtils;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.representation.ChangeRepresentationStatesRequest;
import org.roda.core.data.v2.representation.ChangeTypeRequest;
import org.roda.wui.client.browse.BrowseRepresentation;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.RepresentationDialogs;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class RepresentationSearchWrapperActions extends AbstractActionable<IndexedRepresentation> {
  private static final RepresentationSearchWrapperActions GENERAL_INSTANCE = new RepresentationSearchWrapperActions(
    null, null);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<RepresentationSearchWrapperAction> POSSIBLE_ACTIONS_WITHOUT_REPRESENTATION = new HashSet<>(
    Arrays.asList(RepresentationSearchWrapperAction.NEW));

  private static final Set<RepresentationSearchWrapperAction> POSSIBLE_ACTIONS_ON_SINGLE_REPRESENTATION = new HashSet<>(
    Arrays.asList(RepresentationSearchWrapperAction.CHANGE_TYPE, RepresentationSearchWrapperAction.REMOVE, RepresentationSearchWrapperAction.NEW_PROCESS,
      RepresentationSearchWrapperAction.IDENTIFY_FORMATS, RepresentationSearchWrapperAction.CHANGE_STATE));

  private static final Set<RepresentationSearchWrapperAction> POSSIBLE_ACTIONS_ON_MULTIPLE_REPRESENTATIONS = new HashSet<>(
    Arrays.asList(RepresentationSearchWrapperAction.CHANGE_TYPE, RepresentationSearchWrapperAction.REMOVE, RepresentationSearchWrapperAction.NEW_PROCESS,
      RepresentationSearchWrapperAction.IDENTIFY_FORMATS));

  private final String parentAipId;
  private final Permissions permissions;

  private RepresentationSearchWrapperActions(String parentAipId, Permissions permissions) {
    this.parentAipId = parentAipId;
    this.permissions = permissions;
  }

  public static RepresentationSearchWrapperActions get() {
    return GENERAL_INSTANCE;
  }

  public static RepresentationSearchWrapperActions get(String parentAipId, Permissions permissions) {
    return new RepresentationSearchWrapperActions(parentAipId, permissions);
  }

  public static RepresentationSearchWrapperActions getWithoutNoRepresentationActions(String parentAipId,
    Permissions permissions) {
    return new RepresentationSearchWrapperActions(parentAipId, permissions) {
      @Override
      public CanActResult userCanAct(Action<IndexedRepresentation> action) {
        return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonNoObjectSelected());
      }

      @Override
      public CanActResult contextCanAct(Action<IndexedRepresentation> action) {
        return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonNoObjectSelected());
      }
    };
  }

  @Override
  public RepresentationSearchWrapperAction[] getActions() {
    return RepresentationSearchWrapperAction.values();
  }

  @Override
  public RepresentationSearchWrapperAction actionForName(String name) {
    return RepresentationSearchWrapperAction.valueOf(name);
  }

  @Override
  public CanActResult userCanAct(Action<IndexedRepresentation> action) {
    return new CanActResult(hasPermissions(action, permissions), CanActResult.Reason.USER,
      messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<IndexedRepresentation> action) {
    return new CanActResult(POSSIBLE_ACTIONS_WITHOUT_REPRESENTATION.contains(action) && parentAipId != null,
      CanActResult.Reason.CONTEXT, messages.reasonNoObjectSelected());
  }

  @Override
  public CanActResult userCanAct(Action<IndexedRepresentation> action, IndexedRepresentation representation) {
    return new CanActResult(hasPermissions(action, permissions), CanActResult.Reason.USER,
      messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<IndexedRepresentation> action, IndexedRepresentation representation) {
    return new CanActResult(POSSIBLE_ACTIONS_ON_SINGLE_REPRESENTATION.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonCantActOnSingleObject());
  }

  @Override
  public CanActResult userCanAct(Action<IndexedRepresentation> action,
    SelectedItems<IndexedRepresentation> selectedItems) {
    return new CanActResult(hasPermissions(action, permissions), CanActResult.Reason.USER,
      messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<IndexedRepresentation> action,
    SelectedItems<IndexedRepresentation> selectedItems) {
    return new CanActResult(POSSIBLE_ACTIONS_ON_MULTIPLE_REPRESENTATIONS.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonCantActOnMultipleObjects());
  }

  @Override
  public void act(Action<IndexedRepresentation> action, AsyncCallback<ActionImpact> callback) {
    if (RepresentationSearchWrapperAction.NEW.equals(action)) {
      create(callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void create(AsyncCallback<ActionImpact> callback) {
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

  @Override
  public void act(Action<IndexedRepresentation> action, IndexedRepresentation representation,
    AsyncCallback<ActionImpact> callback) {
    if (RepresentationSearchWrapperAction.CHANGE_TYPE.equals(action)) {
      changeType(representation, callback);
    } else if (RepresentationSearchWrapperAction.REMOVE.equals(action)) {
      remove(representation, callback);
    } else if (RepresentationSearchWrapperAction.NEW_PROCESS.equals(action)) {
      newProcess(representation, callback);
    } else if (RepresentationSearchWrapperAction.IDENTIFY_FORMATS.equals(action)) {
      identifyFormats(representation, callback);
    } else if (RepresentationSearchWrapperAction.CHANGE_STATE.equals(action)) {
      changeState(representation, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  /**
   * Act on multiple files from different representations
   */
  @Override
  public void act(Action<IndexedRepresentation> action, SelectedItems<IndexedRepresentation> selectedItems,
    AsyncCallback<ActionImpact> callback) {
    if (RepresentationSearchWrapperAction.REMOVE.equals(action)) {
      remove(selectedItems, callback);
    } else if (RepresentationSearchWrapperAction.CHANGE_TYPE.equals(action)) {
      changeType(selectedItems, callback);
    } else if (RepresentationSearchWrapperAction.NEW_PROCESS.equals(action)) {
      newProcess(selectedItems, callback);
    } else if (RepresentationSearchWrapperAction.IDENTIFY_FORMATS.equals(action)) {
      identifyFormats(selectedItems, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  // ACTIONS
  private void remove(final IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    remove(objectToSelectedItems(representation, IndexedRepresentation.class), callback);
  }

  private void remove(final SelectedItems<IndexedRepresentation> selectedList,
    final AsyncCallback<ActionImpact> callback) {

    Dialogs.showConfirmDialog(messages.representationRemoveTitle(), messages.representationRemoveMessage(),
      messages.dialogCancel(), messages.dialogYes(), new ActionNoAsyncCallback<Boolean>(callback) {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
              new ActionNoAsyncCallback<String>(callback) {

                @Override
                public void onSuccess(String details) {
                  Services services = new Services("Delete representation", "delete");
                  services
                    .representationResource(
                      s -> s.deleteRepresentation(SelectedItemsUtils.convertToRESTRequest(selectedList), details))
                    .whenComplete((result, error) -> {
                      if (result != null) {
                        Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(),
                          new ActionAsyncCallback<Void>(callback) {

                            @Override
                            public void onFailure(Throwable caught) {
                              doActionCallbackDestroyed();
                            }

                            @Override
                            public void onSuccess(final Void nothing) {
                              doActionCallbackNone();
                              HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                            }
                          });
                      } else if (error != null) {
                        doActionCallbackNone();
                        HistoryUtils.newHistory(InternalProcess.RESOLVER);
                      }
                    });
                }
              });
          }
        }
      });
  }

  private void changeType(final IndexedRepresentation representations, final AsyncCallback<ActionImpact> callback) {
    changeType(objectToSelectedItems(representations, IndexedRepresentation.class), callback);
  }

  private void changeType(final SelectedItems<IndexedRepresentation> representations,
    final AsyncCallback<ActionImpact> callback) {
    Services services = new Services("Get representation type options", "get");
    services.representationResource(s -> s.getRepresentationTypeOptions(LocaleInfo.getCurrentLocale().getLocaleName()))
      .whenComplete((representationTypeOptions, error) -> {
        if (representationTypeOptions != null) {
          RepresentationDialogs.showPromptDialogRepresentationTypes(messages.changeTypeTitle(), null,
            messages.cancelButton(), messages.confirmButton(), representationTypeOptions.getTypes(),
            representationTypeOptions.isControlledVocabulary(), new ActionNoAsyncCallback<String>(callback) {

              @Override
              public void onSuccess(final String newType) {
                Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
                  RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
                  new ActionNoAsyncCallback<String>(callback) {

                    @Override
                    public void onSuccess(String details) {
                      Services services = new Services("Change representation type", "update");
                      ChangeTypeRequest request = new ChangeTypeRequest(
                        SelectedItemsUtils.convertToRESTRequest(representations), newType, details);
                      services.representationResource(s -> s.changeRepresentationType(request))
                        .whenComplete((result, error) -> {
                          if (result != null) {
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
                                HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
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

  private void newProcess(IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    newProcess(objectToSelectedItems(representation, IndexedRepresentation.class), callback);
  }

  private void newProcess(SelectedItems<IndexedRepresentation> selected, final AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setSelectedItems(selected);
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
  }

  private void identifyFormats(IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    identifyFormats(objectToSelectedItems(representation, IndexedRepresentation.class), callback);
  }

  private void identifyFormats(SelectedItems<IndexedRepresentation> selected,
    final AsyncCallback<ActionImpact> callback) {
    Services services = new Services("Create format identification job", "create");
    services
      .representationResource(s -> s.createFormatIdentificationJob(SelectedItemsUtils.convertToRESTRequest(selected)))
      .whenComplete((result, error) -> {
        if (result != null) {
          Toast.showInfo(messages.identifyingFormatsTitle(), messages.identifyingFormatsDescription());

          Dialogs.showJobRedirectDialog(messages.identifyFormatsJobCreatedMessage(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
              callback.onSuccess(ActionImpact.UPDATED);
            }

            @Override
            public void onSuccess(final Void nothing) {
              callback.onSuccess(ActionImpact.NONE);
              HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
            }
          });
        }
      });
  }

  private void changeState(final IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    RepresentationDialogs.showPromptDialogRepresentationStates(messages.changeStatusTitle(), messages.cancelButton(),
      messages.confirmButton(), representation.getRepresentationStates(),
      new ActionNoAsyncCallback<List<String>>(callback) {

        @Override
        public void onSuccess(final List<String> newStates) {
          Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
            RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
            new ActionNoAsyncCallback<String>(callback) {

              @Override
              public void onSuccess(String details) {
                Services services = new Services("Change representation status", "update");
                ChangeRepresentationStatesRequest changeRepresentationStatesRequest = new ChangeRepresentationStatesRequest(
                  SelectedItemsUtils.convertToRESTRequest(
                    objectToSelectedItems(representation, IndexedRepresentation.class)),
                  newStates, details);
                services.representationResource(s -> s.changeRepresentationStatus(changeRepresentationStatesRequest))
                  .whenComplete((result, error) -> {
                    if (error == null) {
                      Toast.showInfo(messages.dialogSuccess(), messages.changeStatusSuccessful());

                      Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                        @Override
                        public void onFailure(Throwable caught) {
                          doActionCallbackUpdated();
                        }

                        @Override
                        public void onSuccess(final Void nothing) {
                          doActionCallbackNone();
                          HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                        }
                      });
                    }
                  });
              }
            });
        }
      });
  }

  @Override
  public ActionableBundle<IndexedRepresentation> createActionsBundle() {
    ActionableBundle<IndexedRepresentation> representationActionableBundle = new ActionableBundle<>();

    // MANAGEMENT
    ActionableGroup<IndexedRepresentation> managementGroup = new ActionableGroup<>(messages.representation());
    managementGroup.addButton(messages.newRepresentationButton(), RepresentationSearchWrapperAction.NEW, ActionImpact.UPDATED,
      "btn-plus");
    managementGroup.addButton(messages.changeTypeButton(), RepresentationSearchWrapperAction.CHANGE_TYPE, ActionImpact.UPDATED,
      "btn-edit");
    managementGroup.addButton(messages.changeStatusButton(), RepresentationSearchWrapperAction.CHANGE_STATE, ActionImpact.UPDATED,
      "btn-edit");
    managementGroup.addButton(messages.removeButton(), RepresentationSearchWrapperAction.REMOVE, ActionImpact.DESTROYED, "btn-ban");

    // PRESERVATION
    ActionableGroup<IndexedRepresentation> preservationGroup = new ActionableGroup<>(messages.preservationTitle());
    preservationGroup.addButton(messages.newProcessPreservation(), RepresentationSearchWrapperAction.NEW_PROCESS,
      ActionImpact.UPDATED, "btn-play");
    preservationGroup.addButton(messages.identifyFormatsButton(), RepresentationSearchWrapperAction.IDENTIFY_FORMATS,
      ActionImpact.UPDATED, "btn-play");

    representationActionableBundle.addGroup(managementGroup).addGroup(preservationGroup);
    return representationActionableBundle;
  }

  public enum RepresentationSearchWrapperAction implements Action<IndexedRepresentation> {
    NEW(RodaConstants.PERMISSION_METHOD_CREATE_REPRESENTATION),
    CHANGE_TYPE(RodaConstants.PERMISSION_METHOD_CHANGE_REPRESENTATION_TYPE),
    REMOVE(RodaConstants.PERMISSION_METHOD_DELETE_REPRESENTATION),
    NEW_PROCESS(RodaConstants.PERMISSION_METHOD_CREATE_JOB),
    IDENTIFY_FORMATS(RodaConstants.PERMISSION_METHOD_CREATE_JOB),
    CHANGE_STATE(RodaConstants.PERMISSION_METHOD_CHANGE_REPRESENTATION_STATES);

    private List<String> methods;

    RepresentationSearchWrapperAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }
}
