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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.browse.bundle.BrowseRepresentationBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataViewBundle;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.DisseminationActions;
import org.roda.wui.client.common.actions.FileActions;
import org.roda.wui.client.common.actions.RepresentationActions;
import org.roda.wui.client.common.lists.DIPList;
import org.roda.wui.client.common.lists.SearchFileList;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestErrorOverlayType;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;
import org.roda.wui.common.client.widgets.wcag.WCAGUtilities;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class BrowseRepresentation extends Composite {

  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 2) {
        final String historyAipId = historyTokens.get(0);
        final String histortyRepresentationId = historyTokens.get(1);

        BrowserService.Util.getInstance().retrieveBrowseRepresentationBundle(historyAipId, histortyRepresentationId,
          LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<BrowseRepresentationBundle>() {

            @Override
            public void onFailure(Throwable caught) {
              Toast.showError(caught.getClass().getSimpleName(), caught.getMessage());
              errorRedirect(callback);
            }

            @Override
            public void onSuccess(final BrowseRepresentationBundle representationBundle) {
              callback.onSuccess(new BrowseRepresentation(representationBundle));
            }
          });

      } else {
        errorRedirect(callback);
      }
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
      return "representation";
    }

    private void errorRedirect(AsyncCallback<Widget> callback) {
      HistoryUtils.newHistory(BrowseAIP.RESOLVER);
      callback.onSuccess(null);
    }
  };

  interface MyUiBinder extends UiBinder<Widget, BrowseRepresentation> {
  }

  // Focus
  @UiField
  FocusPanel keyboardFocus;

  // STATUS

  @UiField
  HTML aipState;

  // IDENTIFICATION

  @UiField
  SimplePanel representationIcon;

  @UiField
  Label representationType;

  @UiField
  Label representationId;

  @UiField
  BreadcrumbPanel breadcrumb;

  // DESCRIPTIVE METADATA

  @UiField
  TabPanel itemMetadata;

  @UiField
  Button newDescriptiveMetadata;

  // FILES

  @UiField(provided = true)
  SearchPanel filesSearch;

  @UiField(provided = true)
  SearchFileList filesList;

  // DISSEMINATIONS

  @UiField
  Label disseminationsTitle;

  @UiField(provided = true)
  SearchPanel disseminationsSearch;

  @UiField(provided = true)
  DIPList disseminationsList;

  // SIDEBAR

  @UiField
  SimplePanel actionsSidebar;

  @UiField
  FlowPanel searchSection;

  @UiField
  Button searchPrevious, searchNext;

  private List<HandlerRegistration> handlers;
  private IndexedRepresentation representation;
  private String aipId;
  private String repId;
  private String repUUID;

  private static final String ALL_FILTER = SearchFilters.allFilter(IndexedFile.class.getName());

  public BrowseRepresentation(BrowseRepresentationBundle bundle) {
    this.representation = bundle.getRepresentation();

    this.aipId = representation.getAipId();
    this.repId = representation.getId();
    this.repUUID = representation.getUUID();

    handlers = new ArrayList<HandlerRegistration>();
    String summary = messages.representationListOfFiles();

    boolean justActive = AIPState.ACTIVE.equals(bundle.getAip().getState());
    boolean selectable = true;
    boolean showFilesPath = false;

    // FILES

    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.FILE_REPRESENTATION_UUID, repUUID),
      new EmptyKeyFilterParameter(RodaConstants.FILE_PARENT_UUID));

    filesList = new SearchFileList(filter, justActive, Facets.NONE, summary, selectable, showFilesPath);
    filesList.setActionable(FileActions.get(aipId, repId));

    ListSelectionUtils.bindBrowseOpener(filesList);

    filesSearch = new SearchPanel(filter, ALL_FILTER, true, messages.searchPlaceHolder(), false, false, true);
    filesSearch.setList(filesList);

    // DISSEMINATIONS
    disseminationsList = new DIPList(Filter.NULL, Facets.NONE, messages.listOfDisseminations(), true);
    disseminationsList.setActionable(DisseminationActions.get());
    ListSelectionUtils.bindBrowseOpener(disseminationsList);

    disseminationsSearch = new SearchPanel(Filter.NULL, RodaConstants.DIP_SEARCH, true, messages.searchPlaceHolder(),
      false, false, true);
    disseminationsSearch.setList(disseminationsList);

    // INIT
    initWidget(uiBinder.createAndBindUi(this));

    // STATUS
    aipState.setHTML(HtmlSnippetUtils.getAIPStateHTML(bundle.getAip().getState()));
    aipState.setVisible(AIPState.ACTIVE != bundle.getAip().getState());

    // IDENTIFICATION

    HTMLPanel representationIconHtmlPanel = new HTMLPanel(
      DescriptionLevelUtils.getRepresentationTypeIcon(representation.getType(), false));
    representationIconHtmlPanel.addStyleName("browseItemIcon-other");
    representationIcon.setWidget(representationIconHtmlPanel);
    representationType.setText(representation.getType() != null ? representation.getType() : representation.getId());
    representationId.setText(representation.getId());

    breadcrumb.updatePath(BreadcrumbUtils.getRepresentationBreadcrumbs(bundle));
    breadcrumb.setVisible(true);

    // DESCRIPTIVE METADATA

    final List<Pair<String, HTML>> descriptiveMetadataContainers = new ArrayList<Pair<String, HTML>>();
    final Map<String, DescriptiveMetadataViewBundle> bundles = new HashMap<>();
    for (DescriptiveMetadataViewBundle descMetadataBundle : bundle.getRepresentationDescriptiveMetadata()) {
      String title = descMetadataBundle.getLabel() != null ? descMetadataBundle.getLabel() : descMetadataBundle.getId();
      HTML container = new HTML();
      container.addStyleName("metadataContent");
      itemMetadata.add(container, title);
      descriptiveMetadataContainers.add(Pair.create(descMetadataBundle.getId(), container));
      bundles.put(descMetadataBundle.getId(), descMetadataBundle);
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
            getDescriptiveMetadataHTML(descId, bundle, new AsyncCallback<SafeHtml>() {

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
          newRepresentationDescriptiveMetadata();
        }
      }
    });
    addTab.addStyleName("addTab");
    addTab.getParent().addStyleName("addTabWrapper");

    handlers.add(tabHandler);
    handlers.add(addTabHandler);

    if (!bundle.getRepresentationDescriptiveMetadata().isEmpty()) {
      newDescriptiveMetadata.setVisible(false);
      itemMetadata.setVisible(true);
      itemMetadata.selectTab(0);
    } else {
      newDescriptiveMetadata.setVisible(true);
      itemMetadata.setVisible(false);
    }

    // DISSEMINATIONS (POST-INIT)
    if (bundle.getDipCount() > 0) {
      Filter disseminationsFilter = new Filter(
        new SimpleFilterParameter(RodaConstants.DIP_REPRESENTATION_UUIDS, repUUID));
      disseminationsList.set(disseminationsFilter, bundle.getAip().getState().equals(AIPState.ACTIVE), Facets.NONE);
      disseminationsSearch.setDefaultFilter(disseminationsFilter, true);
      disseminationsSearch.clearSearchInputBox();
    }
    disseminationsList.getParent().setVisible(bundle.getDipCount() > 0);

    // SIDEBAR
    actionsSidebar.setWidget(RepresentationActions.get(aipId).createActionsLayout(representation,
      new AsyncCallback<Actionable.ActionImpact>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(Actionable.ActionImpact impact) {
          // TODO update interface
        }
      }));

    ListSelectionUtils.bindLayout(representation, searchPrevious, searchNext, keyboardFocus, true, false, false,
      searchSection);

    // CSS
    this.addStyleName("browse");
    this.addStyleName("browse-representation");
    this.addStyleName(bundle.getAip().getState().toString().toLowerCase());

    Element firstElement = this.getElement().getFirstChildElement();
    if (firstElement.getTagName().equalsIgnoreCase("input")) {
      firstElement.setAttribute("title", "browse input");
    }

    WCAGUtilities.getInstance().makeAccessible(itemMetadata.getElement());
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  private void getDescriptiveMetadataHTML(final String descId, final DescriptiveMetadataViewBundle bundle,
    final AsyncCallback<SafeHtml> callback) {
    SafeUri uri = RestUtils.createRepresentationDescriptiveMetadataHTMLUri(aipId, repId, descId);
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
              String historyLink = HistoryUtils.createHistoryHashLink(DescriptiveMetadataHistory.RESOLVER, aipId, repId,
                descId);
              String historyLinkHtml = "<a href='" + historyLink
                + "' class='toolbarLink'><i class='fa fa-history'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(historyLinkHtml));
            }
            // Edit link
            String editLink = HistoryUtils.createHistoryHashLink(EditDescriptiveMetadata.RESOLVER, aipId, repId,
              descId);
            String editLinkHtml = "<a href='" + editLink + "' class='toolbarLink'><i class='fa fa-edit'></i></a>";
            b.append(SafeHtmlUtils.fromSafeConstant(editLinkHtml));

            // Download link
            SafeUri downloadUri = RestUtils.createRepresentationDescriptiveMetadataDownloadUri(aipId, repId, descId);
            String downloadLinkHtml = "<a href='" + downloadUri.asString()
              + "' class='toolbarLink'><i class='fa fa-download'></i></a>";
            b.append(SafeHtmlUtils.fromSafeConstant(downloadLinkHtml));

            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));

            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataHTML'>"));
            b.append(SafeHtmlUtils.fromTrustedString(html));
            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
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
              String historyLink = HistoryUtils.createHistoryHashLink(DescriptiveMetadataHistory.RESOLVER, aipId, repId,
                descId);
              String historyLinkHtml = "<a href='" + historyLink
                + "' class='toolbarLink'><i class='fa fa-history'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(historyLinkHtml));
            }

            // Edit link
            String editLink = HistoryUtils.createHistoryHashLink(EditDescriptiveMetadata.RESOLVER, aipId, repId,
              descId);
            String editLinkHtml = "<a href='" + editLink + "' class='toolbarLink'><i class='fa fa-edit'></i></a>";
            b.append(SafeHtmlUtils.fromSafeConstant(editLinkHtml));

            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));

            // error message
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='error'>"));
            b.append(messages.descriptiveMetadataTransformToHTMLError());
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

  private void newRepresentationDescriptiveMetadata() {
    HistoryUtils.newHistory(CreateDescriptiveMetadata.RESOLVER, CreateDescriptiveMetadata.REPRESENTATION, aipId, repId);
  }

  @UiHandler("newDescriptiveMetadata")
  void buttonNewDescriptiveMetadataEventsHandler(ClickEvent e) {
    newRepresentationDescriptiveMetadata();
  }
}
