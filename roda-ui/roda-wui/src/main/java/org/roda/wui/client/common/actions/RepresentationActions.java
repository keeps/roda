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
import org.roda.core.data.v2.ip.Representation;
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
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class RepresentationActions extends AbstractActionable<IndexedRepresentation> {
  private static final RepresentationActions GENERAL_INSTANCE = new RepresentationActions(null, null);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<RepresentationAction> POSSIBLE_ACTIONS_WITHOUT_REPRESENTATION = new HashSet<>(
    Arrays.asList(RepresentationAction.NEW));

  private static final Set<RepresentationAction> POSSIBLE_ACTIONS_ON_SINGLE_REPRESENTATION = new HashSet<>(
    Arrays.asList(RepresentationAction.DOWNLOAD, RepresentationAction.CHANGE_TYPE, RepresentationAction.REMOVE,
      RepresentationAction.NEW_PROCESS, RepresentationAction.IDENTIFY_FORMATS, RepresentationAction.CHANGE_STATE,
      RepresentationAction.DOWNLOAD_METADATA));

  private static final Set<RepresentationAction> POSSIBLE_ACTIONS_ON_MULTIPLE_REPRESENTATIONS = new HashSet<>(
    Arrays.asList(RepresentationAction.CHANGE_TYPE, RepresentationAction.REMOVE, RepresentationAction.NEW_PROCESS,
      RepresentationAction.IDENTIFY_FORMATS));

  private final String parentAipId;
  private final Permissions permissions;

  private RepresentationActions(String parentAipId, Permissions permissions) {
    this.parentAipId = parentAipId;
    this.permissions = permissions;
  }

  public static RepresentationActions get() {
    return GENERAL_INSTANCE;
  }

  public static RepresentationActions get(String parentAipId, Permissions permissions) {
    return new RepresentationActions(parentAipId, permissions);
  }

  public static RepresentationActions getWithoutNoRepresentationActions(String parentAipId, Permissions permissions) {
    return new RepresentationActions(parentAipId, permissions) {
      @Override
      public CanActResult contextCanAct(Action<IndexedRepresentation> action) {
        return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonNoObjectSelected());
      }
    };
  }

  @Override
  public RepresentationAction[] getActions() {
    return RepresentationAction.values();
  }

  @Override
  public RepresentationAction actionForName(String name) {
    return RepresentationAction.valueOf(name);
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
    if (RepresentationAction.NEW.equals(action)) {
      create(callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void create(AsyncCallback<ActionImpact> callback) {
    Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
      RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, true,
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
    if (RepresentationAction.DOWNLOAD.equals(action)) {
      download(representation, callback);
    } else if (RepresentationAction.CHANGE_TYPE.equals(action)) {
      changeType(representation, callback);
    } else if (RepresentationAction.REMOVE.equals(action)) {
      remove(representation, callback);
    } else if (RepresentationAction.NEW_PROCESS.equals(action)) {
      newProcess(representation, callback);
    } else if (RepresentationAction.IDENTIFY_FORMATS.equals(action)) {
      identifyFormats(representation, callback);
    } else if (RepresentationAction.CHANGE_STATE.equals(action)) {
      changeState(representation, callback);
    } else if (RepresentationAction.DOWNLOAD_METADATA.equals(action)) {
      downloadOtherMetadata(representation, callback);
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
    if (RepresentationAction.REMOVE.equals(action)) {
      remove(selectedItems, callback);
    } else if (RepresentationAction.CHANGE_TYPE.equals(action)) {
      changeType(selectedItems, callback);
    } else if (RepresentationAction.NEW_PROCESS.equals(action)) {
      newProcess(selectedItems, callback);
    } else if (RepresentationAction.IDENTIFY_FORMATS.equals(action)) {
      identifyFormats(selectedItems, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  // ACTIONS
  private void download(IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    SafeUri downloadUri = null;
    if (representation != null) {
      downloadUri = RestUtils.createRepresentationDownloadUri(representation.getAipId(), representation.getId());
    }
    if (downloadUri != null) {
      Window.Location.assign(downloadUri.asString());
    }
    callback.onSuccess(ActionImpact.NONE);
  }

  private void downloadOtherMetadata(IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    SafeUri downloadUri = null;
    if (representation != null) {
      downloadUri = RestUtils.createRepresentationOtherMetadataDownloadUri(representation.getAipId(),
        representation.getId());
    }
    if (downloadUri != null) {
      Window.Location.assign(downloadUri.asString());
    }
    callback.onSuccess(ActionImpact.NONE);
  }

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
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, true,
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
                  RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, true,
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
              callback.onSuccess(Actionable.ActionImpact.UPDATED);
            }

            @Override
            public void onSuccess(final Void nothing) {
              callback.onSuccess(Actionable.ActionImpact.NONE);
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
            RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, true,
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
    managementGroup.addButton(messages.newRepresentationButton(), RepresentationAction.NEW, ActionImpact.UPDATED,
      "btn-plus-circle");
    managementGroup.addButton(messages.changeTypeButton(), RepresentationAction.CHANGE_TYPE, ActionImpact.UPDATED,
      "btn-edit");
    managementGroup.addButton(messages.changeStatusButton(), RepresentationAction.CHANGE_STATE, ActionImpact.UPDATED,
      "btn-edit");
    managementGroup.addButton(messages.removeButton(), RepresentationAction.REMOVE, ActionImpact.DESTROYED, "btn-ban");

    // PRESERVATION
    ActionableGroup<IndexedRepresentation> preservationGroup = new ActionableGroup<>(messages.preservationTitle());
    preservationGroup.addButton(messages.newProcessPreservation(), RepresentationAction.NEW_PROCESS,
      ActionImpact.UPDATED, "btn-play");
    preservationGroup.addButton(messages.identifyFormatsButton(), RepresentationAction.IDENTIFY_FORMATS,
      ActionImpact.UPDATED, "btn-play");

    // DOWNLOAD
    ActionableGroup<IndexedRepresentation> downloadGroup = new ActionableGroup<>(messages.downloadButton());
    downloadGroup.addButton(messages.downloadButton() + " " + messages.oneOfAObject(Representation.class.getName()),
      RepresentationAction.DOWNLOAD, ActionImpact.NONE, "btn-download");
    downloadGroup.addButton(messages.downloadButton() + " " + messages.otherMetadata(),
      RepresentationAction.DOWNLOAD_METADATA, ActionImpact.NONE, "btn-download");

    representationActionableBundle.addGroup(managementGroup).addGroup(preservationGroup).addGroup(downloadGroup);
    return representationActionableBundle;
  }

  public enum RepresentationAction implements Action<IndexedRepresentation> {
    NEW(RodaConstants.PERMISSION_METHOD_CREATE_REPRESENTATION), DOWNLOAD(),
    CHANGE_TYPE(RodaConstants.PERMISSION_METHOD_CHANGE_REPRESENTATION_TYPE),
    REMOVE(RodaConstants.PERMISSION_METHOD_DELETE_REPRESENTATION),
    NEW_PROCESS(RodaConstants.PERMISSION_METHOD_CREATE_JOB),
    IDENTIFY_FORMATS(RodaConstants.PERMISSION_METHOD_CREATE_JOB),
    CHANGE_STATE(RodaConstants.PERMISSION_METHOD_CHANGE_REPRESENTATION_STATES), DOWNLOAD_METADATA();

    private List<String> methods;

    RepresentationAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }
}
