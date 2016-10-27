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

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.SearchFileList;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria <lfaria@keep.pt>
 * 
 */
public class BrowseFolder extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 3) {
        final String aipId = historyTokens.get(0);
        final String representationUUID = historyTokens.get(1);
        final String fileUUID = historyTokens.get(2);

        BrowserService.Util.getInstance().retrieveItemBundle(aipId, LocaleInfo.getCurrentLocale().getLocaleName(),
          new AsyncCallback<BrowseItemBundle>() {

          @Override
          public void onFailure(Throwable caught) {
            errorRedirect(callback);
          }

          @Override
          public void onSuccess(final BrowseItemBundle itemBundle) {
            if (itemBundle != null && verifyRepresentation(itemBundle.getRepresentations(), representationUUID)) {
              BrowserService.Util.getInstance().retrieve(IndexedFile.class.getName(), fileUUID,
                new AsyncCallback<IndexedFile>() {

                @Override
                public void onSuccess(IndexedFile simpleFile) {
                  if (simpleFile.isDirectory()) {
                    BrowseFolder folder = new BrowseFolder(itemBundle, simpleFile);
                    callback.onSuccess(folder);
                  } else {
                    errorRedirect(callback);
                  }
                }

                @Override
                public void onFailure(Throwable caught) {
                  Toast.showError(caught.getClass().getSimpleName(), caught.getMessage());
                  errorRedirect(callback);
                }
              });
            } else {
              errorRedirect(callback);
            }
          }
        });
      } else {
        errorRedirect(callback);
      }
    }

    private boolean verifyRepresentation(List<IndexedRepresentation> representations, String representationUUID) {
      boolean exist = false;
      for (IndexedRepresentation representation : representations) {
        if (representation.getUUID().equals(representationUUID)) {
          exist = true;
        }
      }
      return exist;
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public String getHistoryToken() {
      return "folder";
    }

    @Override
    public List<String> getHistoryPath() {
      return Tools.concat(Browse.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    private void errorRedirect(AsyncCallback<Widget> callback) {
      Tools.newHistory(Browse.RESOLVER);
      callback.onSuccess(null);
    }
  };

  public static final SafeHtml FOLDER_ICON = SafeHtmlUtils.fromSafeConstant("<i class='fa fa-folder-o'></i>");

  interface MyUiBinder extends UiBinder<Widget, BrowseFolder> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  @UiField
  SimplePanel folderIcon;

  @UiField
  Label folderName;

  @UiField
  Label folderId;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField(provided = true)
  SearchFileList filesList;

  // private BrowseItemBundle itemBundle;
  // private IndexedFile folder;
  private String aipId;
  private String repId;
  // private String folderUUID;

  private static final String ALL_FILTER = SearchFilters.allFilter(IndexedFile.class.getName());

  private BrowseFolder(BrowseItemBundle itemBundle, IndexedFile folder) {
    // this.itemBundle = itemBundle;
    // this.folder = folder;
    this.aipId = folder.getAipId();
    this.repId = folder.getRepresentationUUID();
    // this.folderUUID = folder.getUUID();

    String summary = messages.representationListOfFiles();
    boolean selectable = true;

    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.FILE_PARENT_UUID, folder.getUUID()));
    filesList = new SearchFileList(filter, true, Facets.NONE, summary, selectable);

    filesList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        IndexedFile selected = filesList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          if (selected.isDirectory()) {
            Tools.newHistory(Browse.RESOLVER, BrowseFolder.RESOLVER.getHistoryToken(), aipId, repId,
              selected.getUUID());
          } else {
            Tools.newHistory(Browse.RESOLVER, BrowseFile.RESOLVER.getHistoryToken(), aipId, repId, selected.getUUID());
          }
        }
      }
    });

    searchPanel = new SearchPanel(filter, ALL_FILTER, messages.searchPlaceHolder(), false, false, false);
    searchPanel.setDefaultFilterIncremental(true);
    searchPanel.setList(filesList);

    initWidget(uiBinder.createAndBindUi(this));

    String folderLabel = folder.getOriginalName() != null ? folder.getOriginalName() : folder.getId();

    HTMLPanel itemIconHtmlPanel = new HTMLPanel(
      DescriptionLevelUtils.getElementLevelIconSafeHtml(RodaConstants.VIEW_REPRESENTATION_FOLDER, false));
    itemIconHtmlPanel.addStyleName("browseItemIcon-other");

    folderIcon.setWidget(itemIconHtmlPanel);
    folderName.setText(folderLabel);
    folderId.setText(folder.getUUID());

    breadcrumb.updatePath(BreadcrumbUtils.getFileBreadcrumbs(itemBundle, aipId, repId, folder));
    breadcrumb.setVisible(true);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  // TODO
  @UiHandler("refresh")
  void buttonRefreshHandler(ClickEvent e) {

  }

  // TODO
  @UiHandler("rename")
  void buttonRenameHandler(ClickEvent e) {

  }

  // TODO
  @UiHandler("move")
  void buttonMoveHandler(ClickEvent e) {

  }

  // TODO
  @UiHandler("uploadFiles")
  void buttonUploadFilesHandler(ClickEvent e) {

  }

  // TODO
  @UiHandler("createFolder")
  void buttonCreateFolderHandler(ClickEvent e) {

  }

  // TODO
  @UiHandler("remove")
  void buttonRemoveHandler(ClickEvent e) {

  }
}
