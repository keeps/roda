package org.roda.wui.client.common.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.PreservationEvents;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.LoadingAsyncCallback;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.search.SearchSuggestBox;
import org.roda.wui.client.planning.Planning;
import org.roda.wui.client.planning.RiskIncidenceRegister;
import org.roda.wui.client.process.CreateJob;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class RepresentationActions extends AbstractActionable<IndexedRepresentation> {

  private static final RepresentationActions GENERAL_INSTANCE = new RepresentationActions(null);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<RepresentationAction> POSSIBLE_ACTIONS_ON_SINGLE_REPRESENTATION = new HashSet<>(
    Arrays.asList(RepresentationAction.values()));

  private static final Set<RepresentationAction> POSSIBLE_ACTIONS_ON_MULTIPLE_REPRESENTATIONS = new HashSet<>(
    Arrays.asList(RepresentationAction.CHANGE_TYPE, RepresentationAction.REMOVE, RepresentationAction.NEW_PROCESS,
      RepresentationAction.IDENTIFY_FORMATS));

  private final String aipId;

  private RepresentationActions(String aipId) {
    this.aipId = aipId;
  }

  public enum RepresentationAction implements Actionable.Action<IndexedRepresentation> {
    DOWNLOAD, CHANGE_TYPE, REMOVE, NEW_PROCESS, IDENTIFY_FORMATS, SHOW_EVENTS, SHOW_RISKS, UPLOAD_FILES, CREATE_FOLDER;
  }

  public static RepresentationActions get() {
    return GENERAL_INSTANCE;
  }

  public static RepresentationActions get(String aipId) {
    return new RepresentationActions(aipId);
  }

  @Override
  public boolean canAct(Actionable.Action<IndexedRepresentation> action, IndexedRepresentation representation) {
    return POSSIBLE_ACTIONS_ON_SINGLE_REPRESENTATION.contains(action);
  }

  @Override
  public boolean canAct(Actionable.Action<IndexedRepresentation> action,
    SelectedItems<IndexedRepresentation> selectedItems) {
    return POSSIBLE_ACTIONS_ON_MULTIPLE_REPRESENTATIONS.contains(action);
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
    } else {
      callback.onFailure(new RequestNotValidException("Unsupported action in this context: " + action));
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
      callback.onFailure(new RequestNotValidException("Unsupported action in this context: " + action));
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
    remove(objectToSelectedItems(representation), callback);
  }

  public void remove(final SelectedItems<IndexedRepresentation> selectedList,
    final AsyncCallback<ActionImpact> callback) {

    Dialogs.showConfirmDialog(messages.representationRemoveTitle(), messages.representationRemoveMessage(),
      messages.dialogCancel(), messages.dialogYes(), new AsyncCallback<Boolean>() {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

                @Override
                public void onFailure(Throwable caught) {
                  // do nothing
                }

                @Override
                public void onSuccess(String details) {
                  BrowserService.Util.getInstance().deleteRepresentation(selectedList, details,
                    new AsyncCallback<Void>() {

                      @Override
                      public void onSuccess(Void result) {
                        if (aipId != null) {
                          HistoryUtils.openBrowse(aipId);
                        }

                        callback.onSuccess(ActionImpact.DESTROYED);
                      }

                      @Override
                      public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                      }
                    });
                }
              });
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          // nothing to do
        }
      });
  }

  public void changeType(final IndexedRepresentation representations, final AsyncCallback<ActionImpact> callback) {
    changeType(objectToSelectedItems(representations), callback);
  }

  public void changeType(final SelectedItems<IndexedRepresentation> representations,
    final AsyncCallback<ActionImpact> callback) {
    SearchSuggestBox<IndexedRepresentation> suggestBox = new SearchSuggestBox<IndexedRepresentation>(
      IndexedRepresentation.class, RodaConstants.REPRESENTATION_TYPE, true);

    Dialogs.showPromptDialogSuggest(messages.changeTypeTitle(), null, messages.changeTypePlaceHolder(),
      messages.cancelButton(), messages.confirmButton(), suggestBox, new AsyncCallback<String>() {

        @Override
        public void onFailure(Throwable caught) {
          // do nothing
        }

        @Override
        public void onSuccess(final String newType) {
          Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
            RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

              @Override
              public void onFailure(Throwable caught) {
                // do nothing
              }

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

  public void newProcess(IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    newProcess(objectToSelectedItems(representation), callback);
  }

  public void newProcess(SelectedItems<IndexedRepresentation> selected, final AsyncCallback<ActionImpact> callback) {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setSelectedItems(selected);
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateJob.RESOLVER, "action");
    callback.onSuccess(ActionImpact.UPDATED);
  }

  public void identifyFormats(IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    identifyFormats(objectToSelectedItems(representation), callback);
  }

  public void identifyFormats(SelectedItems<IndexedRepresentation> selected,
    final AsyncCallback<ActionImpact> callback) {
    BrowserService.Util.getInstance().createFormatIdentificationJob(selected, new AsyncCallback<Void>() {
      @Override
      public void onSuccess(Void object) {
        Toast.showInfo(messages.identifyingFormatsTitle(), messages.identifyingFormatsDescription());
        callback.onSuccess(ActionImpact.UPDATED);
      }

      @Override
      public void onFailure(Throwable caught) {
        if (caught instanceof NotFoundException) {
          Toast.showError(messages.moveNoSuchObject(caught.getMessage()));
        } else {
          callback.onFailure(caught);
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

    Dialogs.showPromptDialog(messages.createFolderTitle(), null, messages.createFolderPlaceholder(),
      RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {
        @Override
        public void onFailure(Throwable caught) {
          Toast.showInfo(messages.dialogFailure(), messages.renameFailed());
        }

        @Override
        public void onSuccess(final String newName) {
          Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
            RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

              @Override
              public void onFailure(Throwable caught) {
                // do nothing
              }

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
    Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
      RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

        @Override
        public void onFailure(Throwable caught) {
          // do nothing
        }

        @Override
        public void onSuccess(String details) {
          LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
          selectedItems.setDetailsMessage(details);
          HistoryUtils.openUpload(representation);
          callback.onSuccess(ActionImpact.UPDATED);
        }
      });
  }

  @Override
  public Widget createActionsLayout(IndexedRepresentation representation, AsyncCallback<ActionImpact> callback) {
    FlowPanel layout = createLayout();

    // MANAGEMENT
    addTitle(layout, messages.representation());

    // DOWNLOAD, RENAME, MOVE, REMOVE, UPLOAD_FILES, CREATE_FOLDER
    addButton(layout, messages.downloadButton(), RepresentationAction.DOWNLOAD, representation, ActionImpact.NONE,
      callback, "btn-download");

    addButton(layout, messages.changeTypeButton(), RepresentationAction.CHANGE_TYPE, representation,
      ActionImpact.UPDATED, callback, "btn-edit");
    addButton(layout, messages.removeButton(), RepresentationAction.REMOVE, representation, ActionImpact.DESTROYED,
      callback, "btn-ban");

    // PRESERVATION
    addTitle(layout, messages.preservationTitle());

    // NEW_PROCESS, IDENTIFY_FORMATS, SHOW_EVENTS, SHOW_RISKS

    addButton(layout, messages.newProcessPreservation(), RepresentationAction.NEW_PROCESS, representation,
      ActionImpact.UPDATED, callback, "btn-play");
    addButton(layout, messages.identifyFormatsButton(), RepresentationAction.IDENTIFY_FORMATS, representation,
      ActionImpact.UPDATED, callback, "btn-play");
    addButton(layout, messages.preservationEvents(), RepresentationAction.SHOW_EVENTS, representation,
      ActionImpact.NONE, callback, "btn-play");
    addButton(layout, messages.preservationRisks(), RepresentationAction.SHOW_RISKS, representation, ActionImpact.NONE,
      callback, "btn-play");

    // Files and folders
    addTitle(layout, messages.sidebarFoldersFilesTitle());

    // UPLOAD_FILES, CREATE_FOLDER
    addButton(layout, messages.uploadFilesButton(), RepresentationAction.UPLOAD_FILES, representation,
      ActionImpact.UPDATED, callback, "btn-upload");
    addButton(layout, messages.createFolderButton(), RepresentationAction.CREATE_FOLDER, representation,
      ActionImpact.UPDATED, callback, "btn-plus");

    return layout;
  }

  @Override
  public Widget createActionsLayout(SelectedItems<IndexedRepresentation> representations,
    AsyncCallback<ActionImpact> callback) {
    FlowPanel layout = createLayout();

    // MANAGEMENT
    addTitle(layout, messages.representation());

    // DOWNLOAD, RENAME, MOVE, REMOVE, UPLOAD_FILES, CREATE_FOLDER
    addButton(layout, messages.downloadButton(), RepresentationAction.DOWNLOAD, representations, ActionImpact.NONE,
      callback, "btn-download");

    addButton(layout, messages.changeTypeButton(), RepresentationAction.CHANGE_TYPE, representations,
      ActionImpact.UPDATED, callback, "btn-edit");
    addButton(layout, messages.removeButton(), RepresentationAction.REMOVE, representations, ActionImpact.DESTROYED,
      callback, "btn-ban");

    // PRESERVATION
    addTitle(layout, messages.preservationTitle());

    // NEW_PROCESS, IDENTIFY_FORMATS, SHOW_EVENTS, SHOW_RISKS

    addButton(layout, messages.newProcessPreservation(), RepresentationAction.NEW_PROCESS, representations,
      ActionImpact.UPDATED, callback, "btn-play");
    addButton(layout, messages.identifyFormatsButton(), RepresentationAction.IDENTIFY_FORMATS, representations,
      ActionImpact.UPDATED, callback, "btn-play");
    addButton(layout, messages.preservationEvents(), RepresentationAction.SHOW_EVENTS, representations,
      ActionImpact.NONE, callback, "btn-play");
    addButton(layout, messages.preservationRisks(), RepresentationAction.SHOW_RISKS, representations, ActionImpact.NONE,
      callback, "btn-play");

    // Files and folders
    addTitle(layout, messages.sidebarFoldersFilesTitle());

    // UPLOAD_FILES, CREATE_FOLDER
    addButton(layout, messages.uploadFilesButton(), RepresentationAction.UPLOAD_FILES, representations,
      ActionImpact.UPDATED, callback, "btn-upload");
    addButton(layout, messages.createFolderButton(), RepresentationAction.CREATE_FOLDER, representations,
      ActionImpact.UPDATED, callback, "btn-plus");

    return layout;
  }

}
