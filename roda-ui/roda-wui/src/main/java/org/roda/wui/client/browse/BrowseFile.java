/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.client.browse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.wui.client.browse.bundle.BrowseFileBundle;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.LoadingAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.SelectFileDialog;
import org.roda.wui.client.common.lists.pagination.ListSelectionState;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.sidebar.SliderPanel;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.planning.Planning;
import org.roda.wui.client.planning.RiskIncidenceRegister;
import org.roda.wui.client.process.CreateJob;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class BrowseFile extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(final List<String> historyTokens, final AsyncCallback<Widget> callback) {
      BrowserService.Util.getInstance().retrieveViewersProperties(new AsyncCallback<Viewers>() {

        @Override
        public void onSuccess(Viewers viewers) {
          load(viewers, historyTokens, callback);
        }

        @Override
        public void onFailure(Throwable caught) {
          Toast.showError(caught);
          errorRedirect(callback);
        }
      });
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(BrowseAIP.RESOLVER, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(BrowseAIP.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "file";
    }

    private void load(final Viewers viewers, final List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() > 2) {
        final String historyAipId = historyTokens.get(0);
        final String historyRepresentationId = historyTokens.get(1);
        final List<String> historyFilePath = new ArrayList<String>(historyTokens.subList(2, historyTokens.size() - 1));
        final String historyFileId = historyTokens.get(historyTokens.size() - 1);

        BrowserService.Util.getInstance().retrieveBrowseFileBundle(historyAipId, historyRepresentationId,
          historyFilePath, historyFileId, LocaleInfo.getCurrentLocale().getLocaleName(),
          new AsyncCallback<BrowseFileBundle>() {

            @Override
            public void onFailure(Throwable caught) {
              AsyncCallbackUtils.defaultFailureTreatment(caught);
            }

            @Override
            public void onSuccess(final BrowseFileBundle bundle) {
              callback.onSuccess(new BrowseFile(viewers, bundle));
            }
          });
      } else {
        errorRedirect(callback);
      }
    }

    private void errorRedirect(AsyncCallback<Widget> callback) {
      // HistoryUtils.newHistory(Browse.RESOLVER);
      callback.onSuccess(null);
    }
  };

  interface MyUiBinder extends UiBinder<Widget, BrowseFile> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static final Sorter DEFAULT_FILE_SORTER = new Sorter(new SortParameter(RodaConstants.FILE_FILE_ID, false));

  public static final Integer DEFAULT_FILE_INDEX = -1;

  private BrowseFileBundle bundle;

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  @UiField
  FocusPanel keyboardFocus;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  IndexedFilePreview filePreview;

  @UiField
  FocusPanel optionsButton, infoFileButton, disseminationsButton, previousButton, nextButton;

  @UiField
  SliderPanel disseminationsSliderPanel, infoSliderPanel, optionsSliderPanel;

  @UiField
  Button optionDownload, optionRename, optionMove, optionUploadFiles, optionCreateFolder, optionRemove,
    optionNewProcess, optionRisk, optionIdentify, optionEvents;

  /**
   * Create a new panel to view a representation
   * 
   * @param viewers
   * @param index
   * @param sorter
   * @param aipId
   * @param itemBundle
   * @param representationUUID
   * @param fileUUID
   * @param file
   * 
   */
  public BrowseFile(Viewers viewers, final BrowseFileBundle bundle) {
    this.bundle = bundle;

    // initialize preview
    filePreview = new IndexedFilePreview(viewers, bundle.getFile(), new Command() {

      @Override
      public void execute() {
        Scheduler.get().scheduleDeferred(new Command() {
          @Override
          public void execute() {
            Filter filter = new Filter(
              new SimpleFilterParameter(RodaConstants.DIP_FILE_UUIDS, bundle.getFile().getUUID()));
            BrowserService.Util.getInstance().count(IndexedDIP.class.getName(), filter, new AsyncCallback<Long>() {

              @Override
              public void onFailure(Throwable caught) {
                AsyncCallbackUtils.defaultFailureTreatment(caught);
              }

              @Override
              public void onSuccess(Long dipCount) {
                if (dipCount > 0) {
                  // toggleDisseminationsPanel();
                  disseminationsSliderPanel.open();
                }
              }
            });

          }
        });
      }
    });

    // initialize widget
    initWidget(uiBinder.createAndBindUi(this));

    // breadcrumb
    breadcrumb.updatePath(getBreadcrumbs());
    breadcrumb.setVisible(true);

    // set title

    infoFileButton.setTitle(messages.viewRepresentationInfoFileButton());

    // update visibles
    optionDownload.setEnabled(!bundle.getFile().isDirectory());
    infoFileButton.setVisible(!bundle.getFile().isDirectory());
    // downloadDocumentationButton.setVisible(bundle.getRepresentation().getNumberOfDocumentationFiles()
    // > 0);
    // downloadSchemasButton.setVisible(bundle.getRepresentation().getNumberOfSchemaFiles()
    // > 0);
    disseminationsButton.setVisible(bundle.getDipCount() > 0);

    // bind slider buttons
    disseminationsSliderPanel.setToggleButton(disseminationsButton);
    updateDisseminations(bundle.getFile().getUUID(), disseminationsSliderPanel);

    infoSliderPanel.setToggleButton(infoFileButton);
    updateInfoSliderPanel();

    optionsSliderPanel.setToggleButton(optionsButton);

    keyboardFocus.setFocus(true);

    // bind previous and next buttons
    ListSelectionState.bindLayout(IndexedFile.class, previousButton, nextButton, keyboardFocus, true, false, false);

    // update visibility
    boolean directory = bundle.getFile().isDirectory();
    optionDownload.setVisible(!directory);
    optionRename.setVisible(directory);
    optionMove.setVisible(true);
    optionUploadFiles.setVisible(directory);
    optionCreateFolder.setVisible(directory);
    optionRemove.setVisible(true);
    optionNewProcess.setVisible(true);
    optionIdentify.setVisible(true);
    optionEvents.setVisible(true);
    optionRisk.setVisible(true);

  }

  @UiHandler("optionDownload")
  void buttonDownloadFileButtonHandler(ClickEvent e) {
    downloadFile();
  }

  private void downloadFile() {
    SafeUri downloadUri = null;
    if (bundle.getFile() != null) {
      downloadUri = RestUtils.createRepresentationFileDownloadUri(bundle.getFile().getUUID());
    }
    if (downloadUri != null) {
      Window.Location.assign(downloadUri.asString());
    }
  }

  @UiHandler("optionRename")
  void buttonRenameHandler(ClickEvent e) {
    final String selectedUUID;

    if (filePreview.getSelected() instanceof SelectedItemsList) {
      SelectedItemsList<IndexedFile> fileList = (SelectedItemsList<IndexedFile>) filePreview.getSelected();
      selectedUUID = (String) fileList.getIds().get(0);
    } else {
      return;
    }

    Dialogs.showPromptDialog(messages.renameItemTitle(), null, messages.renamePlaceholder(), RegExp.compile(".*"),
      messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

        @Override
        public void onFailure(Throwable caught) {
          // do nothing
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
              public void onSuccess(String details) {
                BrowserService.Util.getInstance().renameFolder(selectedUUID, newName, details,
                  new LoadingAsyncCallback<IndexedFile>() {

                    @Override
                    public void onSuccessImpl(IndexedFile newFolder) {
                      Toast.showInfo(messages.dialogSuccess(), messages.renameSuccessful());
                      HistoryUtils.openBrowse(newFolder);
                    }
                  });
              }
            });
        }
      });
  }

  @UiHandler("optionMove")
  void buttonMoveHandler(ClickEvent e) {
    // FIXME missing filter to remove the files themselves
    IndexedFile file = bundle.getFile();
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.FILE_AIP_ID, file.getAipId()),
      new SimpleFilterParameter(RodaConstants.FILE_REPRESENTATION_ID, file.getRepresentationId()),
      new SimpleFilterParameter(RodaConstants.FILE_ISDIRECTORY, Boolean.toString(true)));
    SelectFileDialog selectFileDialog = new SelectFileDialog(messages.moveItemTitle(), filter, true, false);
    selectFileDialog.setEmptyParentButtonVisible(true);
    selectFileDialog.setSingleSelectionMode();
    selectFileDialog.showAndCenter();
    selectFileDialog.addValueChangeHandler(new ValueChangeHandler<IndexedFile>() {

      @Override
      public void onValueChange(ValueChangeEvent<IndexedFile> event) {
        final IndexedFile toFolder = event.getValue();

        Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
          RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
              // do nothing
            }

            @Override
            public void onSuccess(String details) {
              SelectedItems<IndexedFile> selected = filePreview.getSelected();

              if (ClientSelectedItemsUtils.isEmpty(selected)) {
                selected = new SelectedItemsList<IndexedFile>(Arrays.asList(bundle.getFile().getUUID()),
                  IndexedFile.class.getName());
              }

              final SelectedItems<IndexedFile> selectedItems = selected;
              final String aipId = bundle.getFile().getAipId();
              final String repId = bundle.getFile().getRepresentationId();

              BrowserService.Util.getInstance().moveFiles(aipId, repId, selectedItems, toFolder, details,
                new LoadingAsyncCallback<Void>() {

                  @Override
                  public void onSuccessImpl(Void nothing) {
                    if (toFolder != null) {
                      HistoryUtils.openBrowse(toFolder);
                    } else {
                      HistoryUtils.openBrowse(aipId, repId);
                    }
                  }

                  @Override
                  public void onFailureImpl(Throwable caught) {
                    if (caught instanceof NotFoundException) {
                      Toast.showError(messages.moveNoSuchObject(caught.getMessage()));
                    } else {
                      AsyncCallbackUtils.defaultFailureTreatment(caught);
                    }
                  }

                });
            }
          });
      }
    });
  }

  @UiHandler("optionUploadFiles")
  void buttonUploadFilesHandler(ClickEvent e) {
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
          HistoryUtils.openUpload(bundle.getFile());
        }

      });
  }

  @UiHandler("optionCreateFolder")
  void buttonCreateFolderHandler(ClickEvent e) {
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
              public void onSuccess(String details) {
                IndexedFile folder = bundle.getFile();
                String aipId = folder.getAipId();
                String repId = folder.getRepresentationId();
                String folderUUID = folder.getUUID();
                BrowserService.Util.getInstance().createFolder(aipId, repId, folderUUID, newName, details,
                  new LoadingAsyncCallback<IndexedFile>() {

                    @Override
                    public void onSuccessImpl(IndexedFile newFolder) {
                      HistoryUtils.openBrowse(newFolder);
                    }

                    @Override
                    public void onFailureImpl(Throwable caught) {
                      if (caught instanceof NotFoundException) {
                        Toast.showError(messages.moveNoSuchObject(caught.getMessage()));
                      } else {
                        AsyncCallbackUtils.defaultFailureTreatment(caught);
                      }
                    }

                  });
              }
            });
        }
      });
  }

  @UiHandler("optionNewProcess")
  void buttonNewProcessButtonHandler(ClickEvent e) {
    SelectedItems<IndexedFile> selected = new SelectedItemsList<IndexedFile>(Arrays.asList(bundle.getFile().getUUID()),
      IndexedFile.class.getName());

    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setSelectedItems(selected);
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateJob.RESOLVER, "action");
  }

  @UiHandler("optionRemove")
  void buttonRemoveFileButtonHandler(ClickEvent e) {
    Dialogs.showConfirmDialog(messages.viewRepresentationRemoveFileTitle(),
      messages.viewRepresentationRemoveFileMessage(), messages.dialogCancel(), messages.dialogYes(),
      new AsyncCallback<Boolean>() {

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
                  BrowserService.Util.getInstance().deleteFile(bundle.getFile().getUUID(), details,
                    new AsyncCallback<Void>() {

                      @Override
                      public void onSuccess(Void result) {
                        IndexedFile file = bundle.getFile();
                        List<String> path = file.getPath();
                        if (path.isEmpty()) {
                          HistoryUtils.openBrowse(file.getAipId(), file.getRepresentationId());
                        } else {
                          int lastIndex = path.size() - 1;
                          List<String> parentPath = new ArrayList<>(path.subList(0, lastIndex));
                          String parentId = path.get(lastIndex);
                          HistoryUtils.openBrowse(file.getAipId(), file.getRepresentationId(), parentPath, parentId);
                        }
                      }

                      @Override
                      public void onFailure(Throwable caught) {
                        AsyncCallbackUtils.defaultFailureTreatment(caught);
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

  @UiHandler("optionEvents")
  void buttonPreservationEventsHandler(ClickEvent e) {
    List<String> history = new ArrayList<>();
    history.add(bundle.getAip().getId());
    history.add(bundle.getRepresentation().getUUID());
    history.add(bundle.getFile().getUUID());
    HistoryUtils.newHistory(PreservationEvents.BROWSE_RESOLVER, history);
  }

  @UiHandler("optionRisk")
  void buttonRisksButtonHandler(ClickEvent e) {
    List<String> history = new ArrayList<>();
    history.add(RiskIncidenceRegister.RESOLVER.getHistoryToken());
    history.add(bundle.getAip().getId());
    history.add(bundle.getRepresentation().getId());
    history.addAll(bundle.getFile().getPath());
    history.add(bundle.getFile().getId());
    HistoryUtils.newHistory(Planning.RESOLVER, history);
  }

  @UiHandler("optionIdentify")
  void buttonIdentifyFormatsButtonHandler(ClickEvent e) {
    SelectedItemsList<IndexedFile> selected = new SelectedItemsList<IndexedFile>(
      Arrays.asList(bundle.getFile().getUUID()), IndexedFile.class.getName());

    BrowserService.Util.getInstance().createFormatIdentificationJob(selected, new AsyncCallback<Void>() {
      @Override
      public void onSuccess(Void object) {
        Toast.showInfo(messages.identifyingFormatsTitle(), messages.identifyingFormatsDescription());
      }

      @Override
      public void onFailure(Throwable caught) {
        if (caught instanceof NotFoundException) {
          Toast.showError(messages.moveNoSuchObject(caught.getMessage()));
        } else {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }
      }
    });
  }

  private List<BreadcrumbItem> getBreadcrumbs() {
    return BreadcrumbUtils.getFileBreadcrumbs(bundle);
  }

  public void updateInfoSliderPanel() {
    HashMap<String, SafeHtml> values = new HashMap<String, SafeHtml>();
    IndexedFile file = bundle.getFile();

    if (file != null) {
      String fileName = file.getOriginalName() != null ? file.getOriginalName() : file.getId();
      values.put(messages.viewRepresentationInfoFilename(), SafeHtmlUtils.fromString(fileName));

      values.put(messages.viewRepresentationInfoSize(),
        SafeHtmlUtils.fromString(Humanize.readableFileSize(file.getSize())));

      if (file.getFileFormat() != null) {
        FileFormat fileFormat = file.getFileFormat();

        if (StringUtils.isNotBlank(fileFormat.getMimeType())) {
          values.put(messages.viewRepresentationInfoMimetype(), SafeHtmlUtils.fromString(fileFormat.getMimeType()));
        }

        if (StringUtils.isNotBlank(fileFormat.getFormatDesignationName())) {
          values.put(messages.viewRepresentationInfoFormat(),
            SafeHtmlUtils.fromString(fileFormat.getFormatDesignationName()));
        }

        if (StringUtils.isNotBlank(fileFormat.getFormatDesignationVersion())) {
          values.put(messages.viewRepresentationInfoFormatVersion(),
            SafeHtmlUtils.fromString(fileFormat.getFormatDesignationVersion()));
        }

        if (StringUtils.isNotBlank(fileFormat.getPronom())) {
          values.put(messages.viewRepresentationInfoPronom(), SafeHtmlUtils.fromString(fileFormat.getPronom()));
        }

      }

      if (StringUtils.isNotBlank(file.getCreatingApplicationName())) {
        values.put(messages.viewRepresentationInfoCreatingApplicationName(),
          SafeHtmlUtils.fromString(file.getCreatingApplicationName()));
      }

      if (StringUtils.isNotBlank(file.getCreatingApplicationVersion())) {
        values.put(messages.viewRepresentationInfoCreatingApplicationVersion(),
          SafeHtmlUtils.fromString(file.getCreatingApplicationVersion()));
      }

      if (StringUtils.isNotBlank(file.getDateCreatedByApplication())) {
        values.put(messages.viewRepresentationInfoDateCreatedByApplication(),
          SafeHtmlUtils.fromString(file.getDateCreatedByApplication()));
      }

      if (file.getHash() != null && !file.getHash().isEmpty()) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        boolean first = true;
        for (String hash : file.getHash()) {
          if (first) {
            first = false;
          } else {
            b.append(SafeHtmlUtils.fromSafeConstant("<br/>"));
          }
          b.append(SafeHtmlUtils.fromSafeConstant("<small>"));
          b.append(SafeHtmlUtils.fromString(hash));
          b.append(SafeHtmlUtils.fromSafeConstant("</small>"));
        }
        values.put(messages.viewRepresentationInfoHash(), b.toSafeHtml());
      }

      if (file.getStoragePath() != null) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        b.append(SafeHtmlUtils.fromSafeConstant("<small>"));
        b.append(SafeHtmlUtils.fromString(file.getStoragePath()));
        b.append(SafeHtmlUtils.fromSafeConstant("</small>"));

        values.put(messages.viewRepresentationInfoStoragePath(), b.toSafeHtml());
      }
    }

    for (String key : values.keySet()) {
      FlowPanel entry = new FlowPanel();

      Label keyLabel = new Label(key);
      HTML valueLabel = new HTML(values.get(key));

      entry.add(keyLabel);
      entry.add(valueLabel);

      infoSliderPanel.addContent(entry);

      keyLabel.addStyleName("infoFileEntryKey");
      valueLabel.addStyleName("infoFileEntryValue");
      entry.addStyleName("infoFileEntry");
    }
  }

  public static void updateDisseminations(String fileUUID, final SliderPanel disseminationsSliderPanel) {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_FILE_UUIDS, fileUUID));
    Sorter sorter = new Sorter(new SortParameter(RodaConstants.DIP_DATE_CREATED, true));
    Sublist sublist = new Sublist(0, 100);
    Facets facets = Facets.NONE;
    String localeString = LocaleInfo.getCurrentLocale().getLocaleName();
    boolean justActive = true;

    BrowserService.Util.getInstance().find(IndexedDIP.class.getName(), filter, sorter, sublist, facets, localeString,
      justActive, new AsyncCallback<IndexResult<IndexedDIP>>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(IndexResult<IndexedDIP> result) {
          updateDisseminations(result.getResults(), disseminationsSliderPanel);
        }
      });
  }

  private static void updateDisseminations(List<IndexedDIP> dips, SliderPanel disseminationsSliderPanel) {

    if (dips.isEmpty()) {
      Label dipEmpty = new Label(messages.browseFileDipEmpty());
      disseminationsSliderPanel.addContent(dipEmpty);
      dipEmpty.addStyleName("dip-empty");
    } else {
      for (final IndexedDIP dip : dips) {
        disseminationsSliderPanel.addContent(createDipPanel(dip));
      }
    }
  }

  private static FlowPanel createDipPanel(final IndexedDIP dip) {
    FlowPanel layout = new FlowPanel();

    // open layout
    FlowPanel leftLayout = new FlowPanel();
    Label titleLabel = new Label(dip.getTitle());
    Label descriptionLabel = new Label(dip.getDescription());

    leftLayout.add(titleLabel);
    leftLayout.add(descriptionLabel);

    FocusPanel openFocus = new FocusPanel(leftLayout);
    layout.add(openFocus);

    // delete
    HTML deleteIcon = new HTML(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-ban'></i>"));
    FocusPanel deleteButton = new FocusPanel(deleteIcon);
    deleteButton.addStyleName("lightbtn");
    deleteIcon.addStyleName("lightbtn-icon");
    deleteButton.setTitle(messages.browseFileDipDelete());

    deleteButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        deleteDIP(dip);
      }
    });

    layout.add(deleteButton);

    titleLabel.addStyleName("dipTitle");
    descriptionLabel.addStyleName("dipDescription");
    layout.addStyleName("dip");
    leftLayout.addStyleName("dip-left");
    openFocus.addStyleName("dip-focus");
    deleteButton.addStyleName("dip-delete");

    openFocus.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        if (StringUtils.isNotBlank(dip.getOpenExternalURL())) {
          Window.open(dip.getOpenExternalURL(), "_blank", "");
          Toast.showInfo(messages.browseFileDipOpenedExternalURL(), dip.getOpenExternalURL());
        } else {
          HistoryUtils.openBrowse(dip);
        }
      }
    });

    return layout;
  }

  private static void deleteDIP(final IndexedDIP dip) {
    Dialogs.showConfirmDialog(messages.browseFileDipRepresentationConfirmTitle(),
      messages.browseFileDipRepresentationConfirmMessage(), messages.dialogCancel(), messages.dialogYes(),
      new AsyncCallback<Boolean>() {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            BrowserService.Util.getInstance().deleteDIP(dip.getId(), new AsyncCallback<Void>() {
              @Override
              public void onSuccess(Void result) {
                // updateDisseminations();
              }

              @Override
              public void onFailure(Throwable caught) {
                AsyncCallbackUtils.defaultFailureTreatment(caught);
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
}
