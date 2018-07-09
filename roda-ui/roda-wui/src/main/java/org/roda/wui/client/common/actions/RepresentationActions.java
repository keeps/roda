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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.browse.BrowseRepresentation;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.PreservationEvents;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.LoadingAsyncCallback;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.RepresentationDialogs;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.planning.Planning;
import org.roda.wui.client.planning.RiskIncidenceRegister;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class RepresentationActions extends AbstractActionable<IndexedRepresentation> {
  private static final RepresentationActions GENERAL_INSTANCE = new RepresentationActions(null);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<RepresentationAction> POSSIBLE_ACTIONS_WITHOUT_REPRESENTATION = new HashSet<>(
    Arrays.asList(RepresentationAction.NEW));

  private static final Set<RepresentationAction> POSSIBLE_ACTIONS_ON_SINGLE_REPRESENTATION = new HashSet<>(
    Arrays.asList(RepresentationAction.values()));

  private static final Set<RepresentationAction> POSSIBLE_ACTIONS_ON_MULTIPLE_REPRESENTATIONS = new HashSet<>(
    Arrays.asList(RepresentationAction.CHANGE_TYPE, RepresentationAction.REMOVE, RepresentationAction.NEW_PROCESS,
      RepresentationAction.IDENTIFY_FORMATS));

  private final IndexedAIP parentAip;

  private RepresentationActions(IndexedAIP parentAip) {
    this.parentAip = parentAip;
  }

  public enum RepresentationAction implements Actionable.Action<IndexedRepresentation> {
    NEW, DOWNLOAD, CHANGE_TYPE, REMOVE, NEW_PROCESS, IDENTIFY_FORMATS, SHOW_EVENTS, SHOW_RISKS, UPLOAD_FILES,
    CREATE_FOLDER, CHANGE_STATE
  }

  public static RepresentationActions get() {
    return GENERAL_INSTANCE;
  }

  public static RepresentationActions get(IndexedAIP aip) {
    return new RepresentationActions(aip);
  }

  @Override
  public boolean canAct(Action<IndexedRepresentation> action) {
    return POSSIBLE_ACTIONS_WITHOUT_REPRESENTATION.contains(action) && parentAip != null;
  }

  @Override
  public boolean canAct(Action<IndexedRepresentation> action, IndexedRepresentation representation) {
    return POSSIBLE_ACTIONS_ON_SINGLE_REPRESENTATION.contains(action);
  }

  @Override
  public boolean canAct(Action<IndexedRepresentation> action, SelectedItems<IndexedRepresentation> selectedItems) {
    return POSSIBLE_ACTIONS_ON_MULTIPLE_REPRESENTATIONS.contains(action);
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
      RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
      new NoAsyncCallback<String>() {
        @Override
        public void onSuccess(String details) {
          BrowserService.Util.getInstance().createRepresentation(parentAip.getId(), details,
            new LoadingAsyncCallback<String>() {
              @Override
              public void onSuccessImpl(String representationId) {
                HistoryUtils.newHistory(BrowseRepresentation.RESOLVER, parentAip.getId(), representationId);
                callback.onSuccess(ActionImpact.UPDATED);
              }
            });
        }
      });
  }

  @Override
  public void act(Actionable.Action<IndexedRepresentation> action, IndexedRepresentation representation,
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
    } else if (RepresentationAction.SHOW_EVENTS.equals(action)) {
      showEvents(representation, callback);
    } else if (RepresentationAction.SHOW_RISKS.equals(action)) {
      showRisks(representation, callback);
    } else if (RepresentationAction.UPLOAD_FILES.equals(action)) {
      uploadFiles(representation, callback);
    } else if (RepresentationAction.CREATE_FOLDER.equals(action)) {
      createFolder(representation, callback);
    } else if (RepresentationAction.CHANGE_STATE.equals(action)) {
      changeState(representation, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  /**
   * Act on multiple files from different representations
   */
  @Override
  public void act(Actionable.Action<IndexedRepresentation> action, SelectedItems<IndexedRepresentation> selectedItems,
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
  public void download(IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    SafeUri downloadUri = null;
    if (representation != null) {
      downloadUri = RestUtils.createRepresentationDownloadUri(representation.getAipId(), representation.getId());
    }
    if (downloadUri != null) {
      Window.Location.assign(downloadUri.asString());
    }
    callback.onSuccess(ActionImpact.NONE);
  }

  public void remove(final IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    remove(objectToSelectedItems(representation, IndexedRepresentation.class), callback);
  }

  public void remove(final SelectedItems<IndexedRepresentation> selectedList,
    final AsyncCallback<ActionImpact> callback) {

    Dialogs.showConfirmDialog(messages.representationRemoveTitle(), messages.representationRemoveMessage(),
      messages.dialogCancel(), messages.dialogYes(), new NoAsyncCallback<Boolean>() {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
              new NoAsyncCallback<String>() {

                @Override
                public void onSuccess(String details) {
                  BrowserService.Util.getInstance().deleteRepresentation(selectedList, details,
                    new AsyncCallback<Job>() {

                      @Override
                      public void onSuccess(Job result) {
                        Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(),
                          new ActionAsyncCallback<Void>(callback) {

                          @Override
                          public void onFailure(Throwable caught) {
                            Timer timer = new Timer() {
                              @Override
                              public void run() {
                                  if (parentAip != null) {
                                    HistoryUtils.openBrowse(parentAip);
                                }
                                  doActionCallbackDestroyed();
                              }
                            };

                            timer.schedule(RodaConstants.ACTION_TIMEOUT);
                          }

                          @Override
                          public void onSuccess(final Void nothing) {
                            HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                          }
                        });
                      }

                      @Override
                      public void onFailure(Throwable caught) {
                        HistoryUtils.newHistory(InternalProcess.RESOLVER);
                      }
                    });
                }
              });
          }
        }
      });
  }

  public void changeType(final IndexedRepresentation representations, final AsyncCallback<ActionImpact> callback) {
    changeType(objectToSelectedItems(representations, IndexedRepresentation.class), callback);
  }

  public void changeType(final SelectedItems<IndexedRepresentation> representations,
    final AsyncCallback<ActionImpact> callback) {

    BrowserService.Util.getInstance().retrieveRepresentationTypeOptions(LocaleInfo.getCurrentLocale().getLocaleName(),
      new NoAsyncCallback<Pair<Boolean, List<String>>>() {

        @Override
        public void onSuccess(Pair<Boolean, List<String>> result) {
          RepresentationDialogs.showPromptDialogRepresentationTypes(messages.changeTypeTitle(), null,
            messages.cancelButton(), messages.confirmButton(), result.getSecond(), result.getFirst(),
            new NoAsyncCallback<String>() {

              @Override
              public void onSuccess(final String newType) {
                Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
                  RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
                  new NoAsyncCallback<String>() {

                    @Override
                    public void onSuccess(String details) {
                      BrowserService.Util.getInstance().changeRepresentationType(representations, newType, details,
                        new LoadingAsyncCallback<Void>() {

                          @Override
                          public void onSuccessImpl(Void nothing) {
                            Toast.showInfo(messages.dialogSuccess(), messages.changeTypeSuccessful());
                            callback.onSuccess(ActionImpact.UPDATED);
                          }
                        });
                    }
                  });
              }
            });
        }
      });
  }

  public void newProcess(IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    newProcess(objectToSelectedItems(representation, IndexedRepresentation.class), callback);
  }

  public void newProcess(SelectedItems<IndexedRepresentation> selected, final AsyncCallback<ActionImpact> callback) {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setSelectedItems(selected);
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
    callback.onSuccess(ActionImpact.UPDATED);
  }

  public void identifyFormats(IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    identifyFormats(objectToSelectedItems(representation, IndexedRepresentation.class), callback);
  }

  public void identifyFormats(SelectedItems<IndexedRepresentation> selected,
    final AsyncCallback<ActionImpact> callback) {
    BrowserService.Util.getInstance().createFormatIdentificationJob(selected, new ActionAsyncCallback<Void>(callback) {
      @Override
      public void onSuccess(Void object) {
        Toast.showInfo(messages.identifyingFormatsTitle(), messages.identifyingFormatsDescription());
        doActionCallbackNone();
      }

      @Override
      public void onFailure(Throwable caught) {
        if (caught instanceof NotFoundException) {
          Toast.showError(messages.moveNoSuchObject(caught.getMessage()));
        } else {
          super.onFailure(caught);
        }
      }
    });
  }

  public void showEvents(IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    List<String> history = new ArrayList<>();
    history.add(representation.getAipId());
    history.add(representation.getUUID());
    HistoryUtils.newHistory(PreservationEvents.BROWSE_RESOLVER, history);
    callback.onSuccess(ActionImpact.NONE);
  }

  public void showRisks(IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    List<String> history = new ArrayList<>();
    history.add(RiskIncidenceRegister.RESOLVER.getHistoryToken());
    history.add(representation.getAipId());
    history.add(representation.getId());
    HistoryUtils.newHistory(Planning.RESOLVER, history);
    callback.onSuccess(ActionImpact.NONE);
  }

  public void createFolder(final IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    Dialogs.showPromptDialog(messages.createFolderTitle(), null, null, messages.createFolderPlaceholder(),
      RegExp.compile("^[^/]+$"), messages.cancelButton(), messages.confirmButton(), true, false,
      new NoAsyncCallback<String>() {

        @Override
        public void onSuccess(final String newName) {
          Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
            RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
            new NoAsyncCallback<String>() {

              @Override
              public void onSuccess(final String details) {
                BrowserService.Util.getInstance().createFolder(representation.getAipId(), representation.getId(), null,
                  newName, details, new LoadingAsyncCallback<IndexedFile>() {

                    @Override
                    public void onSuccessImpl(IndexedFile newFolder) {
                      HistoryUtils.openBrowse(newFolder);
                      callback.onSuccess(ActionImpact.UPDATED);
                    }

                    @Override
                    public void onFailureImpl(Throwable caught) {
                      if (caught instanceof NotFoundException) {
                        Toast.showError(messages.moveNoSuchObject(caught.getMessage()));
                      } else {
                        callback.onFailure(caught);
                      }
                    }

                  });
              }
            });
        }
      });
  }

  public void uploadFiles(final IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
      RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
      new NoAsyncCallback<String>() {

        @Override
        public void onSuccess(String details) {
          LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
          selectedItems.setDetailsMessage(details);
          HistoryUtils.openUpload(representation);
          callback.onSuccess(ActionImpact.UPDATED);
        }
      });
  }

  public void changeState(final IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    RepresentationDialogs.showPromptDialogRepresentationStates(messages.changeStatusTitle(), messages.cancelButton(),
      messages.confirmButton(), representation.getRepresentationStates(), new NoAsyncCallback<List<String>>() {

        @Override
        public void onSuccess(final List<String> newStates) {
          Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
            RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
            new NoAsyncCallback<String>() {

              @Override
              public void onSuccess(String details) {
                BrowserService.Util.getInstance().changeRepresentationStates(representation, newStates, details,
                  new LoadingAsyncCallback<Void>() {

                    @Override
                    public void onSuccessImpl(Void nothing) {
                      Toast.showInfo(messages.dialogSuccess(), messages.changeStatusSuccessful());
                      callback.onSuccess(ActionImpact.UPDATED);
                    }
                  });
              }
            });
        }
      });
  }


  @Override
  public ActionsBundle<IndexedRepresentation> createActionsBundle() {
    ActionsBundle<IndexedRepresentation> representationActionableBundle = new ActionsBundle<>();

    // MANAGEMENT
    ActionsGroup<IndexedRepresentation> managementGroup = new ActionsGroup<>(messages.representation());
    managementGroup.addButton(messages.newRepresentationButton(), RepresentationAction.NEW, ActionImpact.UPDATED,
      "btn-plus");
    managementGroup.addButton(messages.downloadButton(), RepresentationAction.DOWNLOAD, ActionImpact.NONE,
      "btn-download");
    managementGroup.addButton(messages.changeTypeButton(), RepresentationAction.CHANGE_TYPE, ActionImpact.UPDATED,
      "btn-edit");
    managementGroup.addButton(messages.changeStatusButton(), RepresentationAction.CHANGE_STATE, ActionImpact.UPDATED,
      "btn-edit");
    managementGroup.addButton(messages.removeButton(), RepresentationAction.REMOVE, ActionImpact.DESTROYED, "btn-ban");

    // PRESERVATION
    ActionsGroup<IndexedRepresentation> preservationGroup = new ActionsGroup<>(messages.preservationTitle());
    preservationGroup.addButton(messages.newProcessPreservation(), RepresentationAction.NEW_PROCESS,
      ActionImpact.UPDATED, "btn-play");
    preservationGroup.addButton(messages.identifyFormatsButton(), RepresentationAction.IDENTIFY_FORMATS,
      ActionImpact.UPDATED, "btn-play");
    preservationGroup.addButton(messages.preservationEvents(), RepresentationAction.SHOW_EVENTS, ActionImpact.NONE,
      "btn-play");
    preservationGroup.addButton(messages.preservationRisks(), RepresentationAction.SHOW_RISKS, ActionImpact.NONE,
      "btn-play");

    // FILES AND FOLDERS
    ActionsGroup<IndexedRepresentation> filesAndFoldersGroup = new ActionsGroup<>(messages.sidebarFoldersFilesTitle());
    filesAndFoldersGroup.addButton(messages.uploadFilesButton(), RepresentationAction.UPLOAD_FILES,
      ActionImpact.UPDATED, "btn-upload");
    filesAndFoldersGroup.addButton(messages.createFolderButton(), RepresentationAction.CREATE_FOLDER,
      ActionImpact.UPDATED, "btn-plus");

    representationActionableBundle.addGroup(managementGroup).addGroup(preservationGroup).addGroup(filesAndFoldersGroup);

    return representationActionableBundle;
  }
}
