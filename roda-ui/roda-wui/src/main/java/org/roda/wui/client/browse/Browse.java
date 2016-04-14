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
import java.util.Map;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.NotSimpleFilterParameter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.client.common.Dialogs;
import org.roda.wui.client.common.LoadingAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.SelectAipDialog;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.SelectedItemsUtils;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.RestErrorOverlayType;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria
 * 
 */
public class Browse extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public String getHistoryToken() {
      return "browse";
    }

    @Override
    public List<String> getHistoryPath() {
      return Arrays.asList(getHistoryToken());
    }
  };

  public static final List<String> getViewItemHistoryToken(String id) {
    return Tools.concat(RESOLVER.getHistoryPath(), id);
  }

  interface MyUiBinder extends UiBinder<Widget, Browse> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static Browse instance = null;

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static Browse getInstance() {
    if (instance == null) {
      instance = new Browse();
    }
    return instance;
  }

  private static Filter COLLECTIONS_FILTER = new Filter(new EmptyKeyFilterParameter(RodaConstants.AIP_PARENT_ID));

  private static BrowseMessages messages = (BrowseMessages) GWT.create(BrowseMessages.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private String aipId;

  private BrowseItemBundle itemBundle;

  @UiField
  Label browseTitle;

  @UiField
  FlowPanel browseDescription;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  SimplePanel itemIcon;

  @UiField
  Label itemTitle;

  @UiField
  Label itemId;

  @UiField
  TabPanel itemMetadata;

  @UiField
  Button newDescriptiveMetadata;

  @UiField
  Label fondsPanelTitle;

  @UiField(provided = true)
  AIPList aipList;

  @UiField
  FlowPanel sidebarData;

  @UiField
  FlowPanel downloadList;

  @UiField
  FlowPanel preservationSidebar;

  @UiField
  FlowPanel permissionsSidebar;

  @UiField
  FlowPanel actionsSidebar;

  @UiField
  Button preservationEvents;

  @UiField
  Button createItem;

  @UiField
  Button moveItem;

  @UiField
  Button remove;

  @UiField
  Button newProcess;

  @UiField
  Button editPermissions;

  private boolean viewingTop;

  private List<HandlerRegistration> handlers;

  private Browse() {
    viewingTop = true;
    handlers = new ArrayList<HandlerRegistration>();

    Filter filter = null;
    Facets facets = null;
    String summary = "List of items";
    boolean selectable = true;
    aipList = new AIPList(filter, facets, summary, selectable);
    initWidget(uiBinder.createAndBindUi(this));

    browseDescription.add(new HTMLWidgetWrapper("BrowseDescription.html"));

    aipList.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        IndexedAIP aip = aipList.getSelectionModel().getSelectedObject();
        if (aip != null) {
          view(aip.getId());
        }
      }
    });

    aipList.addValueChangeHandler(new ValueChangeHandler<IndexResult<IndexedAIP>>() {

      @Override
      public void onValueChange(ValueChangeEvent<IndexResult<IndexedAIP>> event) {
        fondsPanelTitle.setVisible(!viewingTop && event.getValue().getTotalCount() > 0);
        aipList.setVisible(viewingTop || event.getValue().getTotalCount() > 0);
      }
    });
  }

  protected void onPermissionsUpdate(RodaUser user) {
    // FIXME
    if (user.hasRole(RodaConstants.REPOSITORY_PERMISSIONS_METADATA_EDITOR)) {
      createItem.setVisible(true);
    } else {
      createItem.setVisible(false);
    }
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    clear();
    if (historyTokens.size() == 0) {
      viewAction();
      callback.onSuccess(this);
    } else if (historyTokens.size() == 1) {
      viewAction(historyTokens.get(0));
      callback.onSuccess(this);
    } else if (historyTokens.size() > 1
      && historyTokens.get(0).equals(EditDescriptiveMetadata.RESOLVER.getHistoryToken())) {
      EditDescriptiveMetadata.RESOLVER.resolve(Tools.tail(historyTokens), callback);
    } else if (historyTokens.size() > 1
      && historyTokens.get(0).equals(CreateDescriptiveMetadata.RESOLVER.getHistoryToken())) {
      CreateDescriptiveMetadata.RESOLVER.resolve(Tools.tail(historyTokens), callback);
    } else if (historyTokens.size() > 1 && historyTokens.get(0).equals(ViewRepresentation.RESOLVER.getHistoryToken())) {
      ViewRepresentation.RESOLVER.resolve(Tools.tail(historyTokens), callback);
    } else if (historyTokens.size() > 1 && historyTokens.get(0).equals(PreservationEvents.RESOLVER.getHistoryToken())) {
      PreservationEvents.RESOLVER.resolve(Tools.tail(historyTokens), callback);
    } else if (historyTokens.size() > 1
      && historyTokens.get(0).equals(DescriptiveMetadataHistory.RESOLVER.getHistoryToken())) {
      DescriptiveMetadataHistory.RESOLVER.resolve(Tools.tail(historyTokens), callback);
    } else if (historyTokens.size() > 1 && historyTokens.get(0).equals(EditPermissions.RESOLVER.getHistoryToken())) {
      EditPermissions.RESOLVER.resolve(Tools.tail(historyTokens), callback);
    } else {
      Tools.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

  /**
   * Call the view action by the history token
   * 
   * @param id
   *          the pid of the object to view. if pid is null, then the base state
   *          will be called
   */
  public void view(final String id) {
    boolean historyUpdated = updateHistory(id);

    if (!historyUpdated) {
      viewAction(id);
    }
  }

  protected void viewAction(final String id) {
    if (id == null) {
      viewAction();
    } else {
      aipId = id;
      BrowserService.Util.getInstance().getItemBundle(id, LocaleInfo.getCurrentLocale().getLocaleName(),
        new AsyncCallback<BrowseItemBundle>() {

          @Override
          public void onFailure(Throwable caught) {
            if (!AsyncCallbackUtils.treatCommonFailures(caught)) {
              showError(id, caught);
            }
          }

          @Override
          public void onSuccess(BrowseItemBundle itemBundle) {
            viewAction(itemBundle);
          }
        });
    }
  }

  private void clear() {
    browseTitle.setVisible(false);
    browseDescription.setVisible(false);

    HTMLPanel itemIconHtmlPanel = DescriptionLevelUtils.getTopIconHTMLPanel();
    itemIconHtmlPanel.addStyleName("browseItemIcon-all");
    itemIcon.setWidget(itemIconHtmlPanel);
    itemTitle.setText(messages.browseLoading());
    itemTitle.removeStyleName("browseTitle-allCollections");
    itemIcon.getParent().removeStyleName("browseTitle-allCollections-wrapper");
    itemId.setText("");

    breadcrumb.setVisible(false);

    itemMetadata.setVisible(false);
    itemMetadata.clear();
    removeHandlerRegistrations();

    newDescriptiveMetadata.setVisible(false);

    viewingTop = false;
    fondsPanelTitle.setVisible(false);
    aipList.setVisible(false);

    downloadList.clear();
    sidebarData.setVisible(false);

    preservationSidebar.setVisible(false);
    actionsSidebar.setVisible(false);
    permissionsSidebar.setVisible(false);

    // Set button visibility
    createItem.setVisible(false);
    moveItem.setVisible(false);
    editPermissions.setVisible(false);
    remove.setVisible(false);
  }

  protected void showError(String id, Throwable caught) {
    breadcrumb.updatePath(new ArrayList<BreadcrumbItem>());

    HTMLPanel itemIconHtmlPanel = DescriptionLevelUtils.getElementLevelIconHTMLPanel(null);
    itemIconHtmlPanel.addStyleName("browseItemIcon-other");
    itemIcon.setWidget(itemIconHtmlPanel);
    itemTitle.setText(id);

    SafeHtml title;
    SafeHtml message;
    if (caught instanceof NotFoundException) {
      title = messages.notFoundErrorTitle();
      message = messages.notFoundErrorMessage(aipId);
      GWT.log("Not found", caught);
    } else {
      title = messages.genericErrorTitle();
      message = messages.genericErrorMessage(caught.getMessage());
    }

    HTML messageHTML = new HTML(message);
    messageHTML.addStyleName("error");
    itemMetadata.add(messageHTML, title.asString(), true);
    itemMetadata.selectTab(0);
    itemMetadata.setVisible(true);
  }

  protected void viewAction(BrowseItemBundle itemBundle) {
    if (itemBundle != null) {
      this.itemBundle = itemBundle;
      viewingTop = false;

      IndexedAIP aip = itemBundle.getAip();
      List<DescriptiveMetadataViewBundle> descMetadata = itemBundle.getDescriptiveMetadata();
      List<IndexedRepresentation> representations = itemBundle.getRepresentations();

      breadcrumb.updatePath(getBreadcrumbsFromAncestors(itemBundle.getAIPAncestors(), aip));
      breadcrumb.setVisible(true);

      HTMLPanel itemIconHtmlPanel = DescriptionLevelUtils.getElementLevelIconHTMLPanel(aip.getLevel());
      itemIconHtmlPanel.addStyleName("browseItemIcon-other");
      itemIcon.setWidget(itemIconHtmlPanel);
      itemTitle.setText(aip.getTitle() != null ? aip.getTitle() : aip.getId());
      itemTitle.removeStyleName("browseTitle-allCollections");
      itemIcon.getParent().removeStyleName("browseTitle-allCollections-wrapper");
      itemId.setText(aip.getId());

      final List<Pair<String, HTML>> descriptiveMetadataContainers = new ArrayList<Pair<String, HTML>>();
      final Map<String, DescriptiveMetadataViewBundle> bundles = new HashMap<>();
      for (DescriptiveMetadataViewBundle bundle : descMetadata) {
        String title = bundle.getLabel() != null ? bundle.getLabel() : bundle.getId();
        HTML container = new HTML();
        container.addStyleName("metadataContent");
        itemMetadata.add(container, title);
        descriptiveMetadataContainers.add(Pair.create(bundle.getId(), container));
        bundles.put(bundle.getId(), bundle);
      }

      HandlerRegistration tabHandler = itemMetadata.addSelectionHandler(new SelectionHandler<Integer>() {

        @Override
        public void onSelection(SelectionEvent<Integer> event) {
          if (event.getSelectedItem() < descriptiveMetadataContainers.size()) {
            Pair<String, HTML> pair = descriptiveMetadataContainers.get(event.getSelectedItem());
            String descId = pair.getFirst();
            final HTML html = pair.getSecond();
            final DescriptiveMetadataViewBundle bundle = bundles.get(descId);
            if (html.getText().length() == 0) {
              getDescriptiveMetadataHTML(aipId, descId, bundle, new AsyncCallback<SafeHtml>() {

                @Override
                public void onFailure(Throwable caught) {
                  if (!AsyncCallbackUtils.treatCommonFailures(caught)) {
                    Toast.showError(messages.errorLoadingDescriptiveMetadata(caught.getMessage()));
                  }
                }

                @Override
                public void onSuccess(SafeHtml result) {
                  html.setHTML(result);
                }
              });
            }
          }
        }
      });

      final int addTabIndex = itemMetadata.getWidgetCount();
      FlowPanel addTab = new FlowPanel();
      addTab.add(new HTML(SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-plus-circle\"></i>")));
      itemMetadata.add(new Label(), addTab);
      HandlerRegistration addTabHandler = itemMetadata.addSelectionHandler(new SelectionHandler<Integer>() {
        @Override
        public void onSelection(SelectionEvent<Integer> event) {
          if (event.getSelectedItem() == addTabIndex) {
            newDescriptiveMetadataRedirect();
          }
        }
      });
      addTab.addStyleName("addTab");
      addTab.getParent().addStyleName("addTabWrapper");

      handlers.add(tabHandler);
      handlers.add(addTabHandler);

      if (!descMetadata.isEmpty()) {
        itemMetadata.setVisible(true);
        itemMetadata.selectTab(0);
      } else {
        newDescriptiveMetadata.setVisible(true);
      }

      Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, aip.getId()));
      aipList.setFilter(filter);

      sidebarData.setVisible(representations.size() > 0);
      preservationSidebar.setVisible(true);
      actionsSidebar.setVisible(true);
      permissionsSidebar.setVisible(true);

      for (IndexedRepresentation rep : representations) {
        downloadList.add(createRepresentationDownloadButton(rep));
      }

      // Set button visibility
      createItem.setVisible(true);
      moveItem.setVisible(true);
      editPermissions.setVisible(true);
      remove.setVisible(true);
    } else {
      viewAction();
    }
  }

  protected void viewAction() {
    aipId = null;
    viewingTop = true;

    browseTitle.setVisible(true);
    browseDescription.setVisible(true);

    breadcrumb.updatePath(
      Arrays.asList(new BreadcrumbItem(DescriptionLevelUtils.getTopIconSafeHtml(), RESOLVER.getHistoryPath())));

    HTMLPanel topIcon = DescriptionLevelUtils.getTopIconHTMLPanel();
    topIcon.addStyleName("browseItemIcon-all");
    itemIcon.setWidget(topIcon);
    itemTitle.setText(messages.allCollectionsTitle());
    itemTitle.addStyleName("browseTitle-allCollections");
    itemIcon.getParent().addStyleName("browseTitle-allCollections-wrapper");

    aipList.setFilter(COLLECTIONS_FILTER);

    actionsSidebar.setVisible(true);

    // Set button visibility
    createItem.setVisible(true);
    moveItem.setVisible(true);
    editPermissions.setVisible(true);
    remove.setVisible(true);
  }

  private void removeHandlerRegistrations() {
    for (HandlerRegistration handlerRegistration : handlers) {
      handlerRegistration.removeHandler();
    }
    handlers.clear();
  }

  private List<BreadcrumbItem> getBreadcrumbsFromAncestors(List<IndexedAIP> aipAncestors, IndexedAIP aip) {
    List<BreadcrumbItem> ret = new ArrayList<>();
    ret.add(new BreadcrumbItem(DescriptionLevelUtils.getTopIconSafeHtml(), RESOLVER.getHistoryPath()));
    if (aipAncestors != null) {
      for (IndexedAIP ancestor : aipAncestors) {
        if (ancestor != null) {
          SafeHtml breadcrumbLabel = getBreadcrumbLabel(ancestor);
          BreadcrumbItem ancestorBreadcrumb = new BreadcrumbItem(breadcrumbLabel,
            getViewItemHistoryToken(ancestor.getId()));
          ret.add(1, ancestorBreadcrumb);
        } else {
          SafeHtml breadcrumbLabel = SafeHtmlUtils.fromSafeConstant("<i class='fa fa-question-circle'></i>");
          BreadcrumbItem unknownAncestorBreadcrumb = new BreadcrumbItem(breadcrumbLabel, new Command() {

            @Override
            public void execute() {
              // TODO find better error message
              Toast.showError("Unknown ancestor");
            }
          });
          ret.add(unknownAncestorBreadcrumb);
        }
      }
    }

    ret.add(new BreadcrumbItem(getBreadcrumbLabel(aip), getViewItemHistoryToken(aip.getId())));
    return ret;
  }

  private SafeHtml getBreadcrumbLabel(IndexedAIP ancestor) {
    SafeHtml elementLevelIconSafeHtml = DescriptionLevelUtils.getElementLevelIconSafeHtml(ancestor.getLevel(), false);
    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    String label = ancestor.getTitle() != null ? ancestor.getTitle() : ancestor.getId();
    builder.append(elementLevelIconSafeHtml).append(SafeHtmlUtils.fromString(label));
    SafeHtml breadcrumbLabel = builder.toSafeHtml();
    return breadcrumbLabel;
  }

  private Widget createRepresentationDownloadButton(IndexedRepresentation rep) {
    Button downloadButton = new Button();
    final String aipId = rep.getAipId();
    final String repUUID = rep.getUUID();

    SafeHtml labelText;

    if (rep.isOriginal()) {
      labelText = messages.downloadTitleOriginal();
    } else {
      labelText = messages.downloadTitleDefault();
    }

    downloadButton.setText(labelText.asString());

    downloadButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        Tools.newHistory(Tools.concat(ViewRepresentation.RESOLVER.getHistoryPath(), aipId, repUUID));
      }
    });

    downloadButton.addStyleName("btn btn-view");

    return downloadButton;
  }

  private void getDescriptiveMetadataHTML(final String aipId, final String descId,
    final DescriptiveMetadataViewBundle bundle, final AsyncCallback<SafeHtml> callback) {
    SafeUri uri = RestUtils.createDescriptiveMetadataHTMLUri(aipId, descId);
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uri.asString());
    requestBuilder.setHeader("Authorization", "Custom");
    try {
      requestBuilder.sendRequest(null, new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (200 == response.getStatusCode()) {
            String html = response.getText();

            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataLinks'>"));

            if (bundle.hasHistory()) {
              // History link
              String historyLink = Tools.createHistoryHashLink(DescriptiveMetadataHistory.RESOLVER, aipId, descId);
              String historyLinkHtml = "<a href='" + historyLink
                + "' class='toolbarLink'><i class='fa fa-history'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(historyLinkHtml));
            }
            // Edit link
            String editLink = Tools.createHistoryHashLink(EditDescriptiveMetadata.RESOLVER, aipId, descId);
            String editLinkHtml = "<a href='" + editLink + "' class='toolbarLink'><i class='fa fa-edit'></i></a>";
            b.append(SafeHtmlUtils.fromSafeConstant(editLinkHtml));

            // Download link
            SafeUri downloadUri = RestUtils.createDescriptiveMetadataDownloadUri(aipId, descId);
            String downloadLinkHtml = "<a href='" + downloadUri.asString()
              + "' class='toolbarLink'><i class='fa fa-download'></i></a>";
            b.append(SafeHtmlUtils.fromSafeConstant(downloadLinkHtml));

            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));

            b.append(SafeHtmlUtils.fromTrustedString(html));
            SafeHtml safeHtml = b.toSafeHtml();

            callback.onSuccess(safeHtml);
          } else {
            String text = response.getText();
            String message;
            try {
              RestErrorOverlayType error = (RestErrorOverlayType) JsonUtils.safeEval(text);
              message = error.getMessage();
            } catch (IllegalArgumentException e) {
              message = text;
            }

            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataLinks'>"));

            if (bundle.hasHistory()) {
              // History link
              String historyLink = Tools.createHistoryHashLink(DescriptiveMetadataHistory.RESOLVER, aipId, descId);
              String historyLinkHtml = "<a href='" + historyLink
                + "' class='toolbarLink'><i class='fa fa-history'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(historyLinkHtml));
            }

            // Edit link
            String editLink = Tools.createHistoryHashLink(EditDescriptiveMetadata.RESOLVER, aipId, descId);
            String editLinkHtml = "<a href='" + editLink + "' class='toolbarLink'><i class='fa fa-edit'></i></a>";
            b.append(SafeHtmlUtils.fromSafeConstant(editLinkHtml));

            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));

            // error message
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='error'>"));
            b.append(messages.descriptiveMetadataTranformToHTMLError());
            b.append(SafeHtmlUtils.fromSafeConstant("<pre><code>"));
            b.append(SafeHtmlUtils.fromString(message));
            b.append(SafeHtmlUtils.fromSafeConstant("</core></pre>"));
            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));

            callback.onSuccess(b.toSafeHtml());
          }
        }

        @Override
        public void onError(Request request, Throwable exception) {
          callback.onFailure(exception);
        }
      });
    } catch (RequestException e) {
      callback.onFailure(e);
    }
  }

  @SuppressWarnings("unused")
  private String getDatesText(IndexedAIP aip) {
    return Humanize.getDatesText(aip.getDateInitial(), aip.getDateFinal(), true);
  }

  private boolean updateHistory(String id) {
    boolean historyUpdated;
    List<String> path;
    if (id == null) {
      path = RESOLVER.getHistoryPath();
    } else {
      path = getViewItemHistoryToken(id);
    }

    if (path.equals(History.getToken())) {
      historyUpdated = false;
    } else {
      Tools.newHistory(path);
      historyUpdated = true;
    }
    return historyUpdated;
  }

  @UiHandler("preservationEvents")
  void buttonPreservationEventsHandler(ClickEvent e) {
    if (aipId != null) {
      Tools.newHistory(RESOLVER, PreservationEvents.RESOLVER.getHistoryToken(), aipId);
    }
  }

  @UiHandler("createItem")
  void buttonCreateItemHandler(ClickEvent e) {
    BrowserService.Util.getInstance().createAIP(aipId, new AsyncCallback<String>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(String itemAIPId) {
        view(itemAIPId);
        Tools.newHistory(CreateDescriptiveMetadata.RESOLVER, itemAIPId, CreateDescriptiveMetadata.NEW);
      }
    });
  }

  @UiHandler("remove")
  void buttonRemoveHandler(ClickEvent e) {

    final SelectedItems selected = aipList.getSelected();

    if (SelectedItemsUtils.isEmpty(selected)) {
      // Remove the whole folder

      if (aipId != null) {
        Dialogs.showConfirmDialog(messages.browseRemoveConfirmDialogTitle(),
          messages.browseRemoveConfirmDialogMessage(), messages.dialogCancel(), messages.dialogOk(),
          new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable caught) {
              // nothing to do
            }

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                SelectedItemsList selected = new SelectedItemsList(Arrays.asList(aipId));
                BrowserService.Util.getInstance().removeAIP(selected, new AsyncCallback<String>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    AsyncCallbackUtils.defaultFailureTreatment(caught);
                  }

                  @Override
                  public void onSuccess(String parentId) {
                    if (parentId != null) {
                      Tools.newHistory(Browse.RESOLVER, parentId);
                    } else {
                      Tools.newHistory(Browse.RESOLVER);
                    }
                  }
                });
              }
            }
          });

      } else {
        Dialogs.showInformationDialog("Select an item", "Please select one or more items to remove", "OK");
      }
    } else {
      // Remove all selected

      SelectedItemsUtils.size(IndexedAIP.class, selected, new AsyncCallback<Long>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(final Long size) {
          // TODO update messages
          Dialogs.showConfirmDialog(messages.ingestTransferRemoveFolderConfirmDialogTitle(),
            messages.ingestTransferRemoveSelectedConfirmDialogMessage(size),
            messages.ingestTransferRemoveFolderConfirmDialogCancel(),
            messages.ingestTransferRemoveFolderConfirmDialogOk(), new AsyncCallback<Boolean>() {

              @Override
              public void onSuccess(Boolean confirmed) {
                if (confirmed) {
                  BrowserService.Util.getInstance().removeAIP(selected, new LoadingAsyncCallback<String>() {

                    @Override
                    public void onFailureImpl(Throwable caught) {
                      AsyncCallbackUtils.defaultFailureTreatment(caught);
                      aipList.refresh();
                    }

                    @Override
                    public void onSuccessImpl(String parentId) {
                      Toast.showInfo(messages.ingestTransferRemoveSuccessTitle(),
                        messages.ingestTransferRemoveSuccessMessage(size));
                      aipList.refresh();
                    }
                  });
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

  @UiHandler("newDescriptiveMetadata")
  void buttonNewDescriptiveMetadataHandler(ClickEvent e) {
    newDescriptiveMetadataRedirect();
  }

  private void newDescriptiveMetadataRedirect() {
    if (aipId != null) {
      Tools.newHistory(RESOLVER, CreateDescriptiveMetadata.RESOLVER.getHistoryToken(), aipId);
    }
  }

  @UiHandler("moveItem")
  void buttonMoveItemHandler(ClickEvent e) {
    if (aipId != null && itemBundle != null) {
      Filter filter = new Filter(new NotSimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aipId),
        new NotSimpleFilterParameter(RodaConstants.AIP_ID, aipId));
      SelectAipDialog selectAipDialog = new SelectAipDialog(messages.moveItemTitle(), filter);
      if (itemBundle.getAip().getParentID() != null) {
        selectAipDialog.setEmptyParentButtonVisible();
      }
      selectAipDialog.showAndCenter();
      selectAipDialog.addValueChangeHandler(new ValueChangeHandler<IsIndexed>() {

        @Override
        public void onValueChange(ValueChangeEvent<IsIndexed> event) {
          final IndexedAIP parentAIP = (IndexedAIP) event.getValue();
          final String parentId = (parentAIP != null) ? parentAIP.getId() : null;

          BrowserService.Util.getInstance().moveInHierarchy(aipId, parentId, new AsyncCallback<AIP>() {

            @Override
            public void onSuccess(AIP result) {
              if (result != null) {
                clear();
                viewAction(result.getId());
              }
            }

            @Override
            public void onFailure(Throwable caught) {
              if (caught instanceof NotFoundException) {
                Toast.showError(messages.moveNoSuchObject(caught.getMessage()));
              } else {
                Toast.showError(messages.moveIllegalOperation(caught.getMessage()));
              }
            }
          });
        }
      });
    }
  }

  @UiHandler("newProcess")
  void buttonNewProcessHandler(ClickEvent e) {
    Toast.showInfo("Sorry", "Feature not yet implemented");
  }

  @UiHandler("editPermissions")
  void buttonEditPermissionsHandler(ClickEvent e) {
    if (aipId != null) {
      Tools.newHistory(RESOLVER, EditPermissions.RESOLVER.getHistoryToken(), aipId);
    }
  }
}
