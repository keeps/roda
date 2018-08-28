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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.browse.bundle.BrowseRepresentationBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataViewBundle;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.DisseminationActions;
import org.roda.wui.client.common.actions.FileActions;
import org.roda.wui.client.common.lists.DIPList;
import org.roda.wui.client.common.lists.SearchFileList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.slider.Sliders;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.PermissionUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.planning.RiskIncidenceRegister;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
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
import com.google.gwt.user.client.ui.Anchor;
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
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 2) {
        final String historyAipId = historyTokens.get(0);
        final String histortyRepresentationId = historyTokens.get(1);

        BrowserService.Util.getInstance().retrieveBrowseRepresentationBundle(historyAipId, histortyRepresentationId,
          LocaleInfo.getCurrentLocale().getLocaleName(), representationFields,
          new AsyncCallback<BrowseRepresentationBundle>() {

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
      UserLogin.getInstance().checkRole(BrowseTop.RESOLVER, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(BrowseTop.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "representation";
    }

    private void errorRedirect(AsyncCallback<Widget> callback) {
      HistoryUtils.newHistory(BrowseTop.RESOLVER);
      callback.onSuccess(null);
    }
  };

  interface MyUiBinder extends UiBinder<Widget, BrowseRepresentation> {
  }

  private List<HandlerRegistration> handlers;
  private IndexedAIP aip;
  private IndexedRepresentation representation;
  private String aipId;
  private String repId;
  private String repUUID;

  private static final List<String> representationFields = new ArrayList<>(Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.REPRESENTATION_AIP_ID, RodaConstants.REPRESENTATION_ID, RodaConstants.REPRESENTATION_TYPE));

  // Focus
  @UiField
  FocusPanel keyboardFocus;

  // STATUS

  @UiField
  HTML aipState;

  // IDENTIFICATION

  // DESCRIPTIVE METADATA

  @UiField
  TabPanel itemMetadata;

  @UiField
  Button newDescriptiveMetadata;

  private SimplePanel descriptiveMetadataButtons;
  private Map<Integer, HTMLPanel> descriptiveMetadataSavedButtons;

  // FILES

  @UiField(provided = true)
  SearchWrapper filesSearch;

  // DISSEMINATIONS

  @UiField(provided = true)
  SearchWrapper disseminationsSearch;

  @UiField
  FlowPanel center;

  @UiField
  NavigationToolbar<IndexedRepresentation> navigationToolbar;

  @UiField
  HTML representationIcon;

  @UiField
  FlowPanel representationTitle;

  @UiField
  FlowPanel risksEventsLogs;

  @UiField
  Label dateCreatedAndModified;

  public BrowseRepresentation(BrowseRepresentationBundle bundle) {
    this.representation = bundle.getRepresentation();
    this.aip = bundle.getAip();

    this.aipId = representation.getAipId();
    this.repId = representation.getId();
    this.repUUID = representation.getUUID();

    handlers = new ArrayList<>();
    String summary = messages.representationListOfFiles();

    final AIPState state = aip.getState();
    final boolean justActive = AIPState.ACTIVE.equals(state);
    boolean showFilesPath = false;

    LastSelectedItemsSingleton.getInstance().setSelectedJustActive(justActive);

    // FILES

    Filter filesFilter = new Filter(new SimpleFilterParameter(RodaConstants.FILE_REPRESENTATION_UUID, repUUID),
      new EmptyKeyFilterParameter(RodaConstants.FILE_PARENT_UUID));

    ListBuilder<IndexedFile> fileListBuilder = new ListBuilder<>(() -> new SearchFileList(showFilesPath),
      new AsyncTableCell.Options<>(IndexedFile.class, "BrowseRepresentation_files").withFilter(filesFilter)
        .withJustActive(justActive).withSummary(summary).bindOpener());

    filesSearch = new SearchWrapper(false).createListAndSearchPanel(fileListBuilder,
      FileActions.get(aipId, repId, aip.getPermissions()));

    // DISSEMINATIONS

    Filter disseminationsFilter = new Filter(
      new SimpleFilterParameter(RodaConstants.DIP_REPRESENTATION_UUIDS, repUUID));

    ListBuilder<IndexedDIP> disseminationsListBuilder = new ListBuilder<>(DIPList::new,
      new AsyncTableCell.Options<>(IndexedDIP.class, "BrowseRepresentation_disseminations")
        .withFilter(disseminationsFilter).withSummary(messages.listOfDisseminations()).bindOpener()
        .withJustActive(justActive));

    disseminationsSearch = new SearchWrapper(false).createListAndSearchPanel(disseminationsListBuilder,
      DisseminationActions.get());

    // INIT
    initWidget(uiBinder.createAndBindUi(this));

    updateLayout(bundle, state, justActive);

    // NAVIGATION TOOLBAR
    navigationToolbar.setObject(representation);
    navigationToolbar.setPermissions(aip.getPermissions());
    navigationToolbar.show();

    // DESCRIPTIVE METADATA

    final List<Pair<String, HTML>> descriptiveMetadataContainers = new ArrayList<>();
    final Map<String, DescriptiveMetadataViewBundle> bundles = new HashMap<>();
    for (DescriptiveMetadataViewBundle descMetadataBundle : bundle.getRepresentationDescriptiveMetadata()) {
      String title = descMetadataBundle.getLabel() != null ? descMetadataBundle.getLabel() : descMetadataBundle.getId();
      HTML container = new HTML();
      container.addStyleName("metadataContent");
      itemMetadata.add(container, title);
      descriptiveMetadataContainers.add(Pair.of(descMetadataBundle.getId(), container));
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
            getDescriptiveMetadataHTML(descId, bundle, event.getSelectedItem(), new AsyncCallback<SafeHtml>() {

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
    addTab.getElement().setId("representationNewDescriptiveMetadata");
    addTab.getParent().addStyleName("addTabWrapper");

    PermissionUtils.bindPermission(newDescriptiveMetadata, aip.getPermissions(),
      "org.roda.wui.api.controllers.Browser.createDescriptiveMetadataFile");

    handlers.add(tabHandler);
    handlers.add(addTabHandler);

    descriptiveMetadataSavedButtons = new HashMap<>();
    descriptiveMetadataButtons = new SimplePanel();
    descriptiveMetadataButtons.addStyleName("descriptiveMetadataTabButtons");
    itemMetadata.getTabBar().getElement().getStyle().clearProperty("width");
    itemMetadata.getTabBar().getElement().getParentElement().insertFirst(descriptiveMetadataButtons.getElement());
    itemMetadata.addSelectionHandler(event -> {
      if (descriptiveMetadataSavedButtons.containsKey(event.getSelectedItem())) {
        descriptiveMetadataButtons.setWidget(descriptiveMetadataSavedButtons.get(event.getSelectedItem()));
      } else {
        descriptiveMetadataButtons.clear();
      }
    });

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
      disseminationsSearch.setFilter(IndexedDIP.class, disseminationsFilter);
    }
    disseminationsSearch.setVisible(bundle.getDipCount() > 0);
    disseminationsSearch.getParent().setVisible(bundle.getDipCount() > 0);

    // CSS
    this.addStyleName("browse");
    this.addStyleName("browse-representation");
    this.addStyleName(state.toString().toLowerCase());
    newDescriptiveMetadata.getElement().setId("representationNewDescriptiveMetadata");

    Element firstElement = this.getElement().getFirstChildElement();
    if ("input".equalsIgnoreCase(firstElement.getTagName())) {
      firstElement.setAttribute("title", "browse input");
    }

    WCAGUtilities.getInstance().makeAccessible(itemMetadata.getElement());
  }

  private void updateLayout(final BrowseRepresentationBundle bundle, final AIPState state, final boolean justActive) {
    // STATUS
    aipState.setHTML(HtmlSnippetUtils.getAIPStateHTML(state));
    aipState.setVisible(!justActive);

    // IDENTIFICATION
    representationIcon.setHTML(DescriptionLevelUtils.getRepresentationTypeIcon(representation.getType(), false));
    Sliders.createRepresentationInfoSlider(center, navigationToolbar.getSidebarButton(), bundle);

    Anchor risksLink = new Anchor(messages.aipRiskIncidences(bundle.getRiskIncidenceCount()), HistoryUtils
      .createHistoryHashLink(RiskIncidenceRegister.RESOLVER, representation.getAipId(), representation.getId()));
    Anchor eventsLink = new Anchor(messages.aipEvents(bundle.getPreservationEventCount()), HistoryUtils
      .createHistoryHashLink(PreservationEvents.BROWSE_RESOLVER, representation.getAipId(), representation.getUUID()));

    risksEventsLogs.clear();
    risksEventsLogs.add(risksLink);
    risksEventsLogs.add(new Label(" " + messages.and() + " "));
    risksEventsLogs.add(eventsLink);

    if (representation.getCreatedOn() != null && StringUtils.isNotBlank(representation.getCreatedBy())
      && representation.getUpdatedOn() != null && StringUtils.isNotBlank(representation.getUpdatedBy())) {
      dateCreatedAndModified.setText(messages.dateCreatedAndUpdated(Humanize.formatDate(representation.getCreatedOn()),
        representation.getCreatedBy(), Humanize.formatDate(representation.getUpdatedOn()),
        representation.getUpdatedBy()));
    } else if (representation.getCreatedOn() != null && StringUtils.isNotBlank(representation.getCreatedBy())) {
      dateCreatedAndModified.setText(
        messages.dateCreated(Humanize.formatDateTime(representation.getCreatedOn()), representation.getCreatedBy()));
    } else if (representation.getUpdatedOn() != null && StringUtils.isNotBlank(representation.getUpdatedBy())) {
      dateCreatedAndModified.setText(
        messages.dateUpdated(Humanize.formatDateTime(representation.getUpdatedOn()), representation.getUpdatedBy()));
    } else {
      dateCreatedAndModified.setText("");
    }

    String title = representation.getTitle() != null ? representation.getTitle() : representation.getType();
    title = title == null ? representation.getId() : title;
    representationTitle.clear();
    HtmlSnippetUtils.getRepresentationTypeHTML(representationTitle, title, representation.getRepresentationStates());

    navigationToolbar.updateBreadcrumb(bundle);
  }

  private void getDescriptiveMetadataHTML(final String descId, final DescriptiveMetadataViewBundle bundle,
    final Integer selectedIndex, final AsyncCallback<SafeHtml> callback) {
    SafeUri uri = RestUtils.createRepresentationDescriptiveMetadataHTMLUri(aipId, repId, descId);
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uri.asString());
    try {
      requestBuilder.sendRequest(null, new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (200 == response.getStatusCode()) {
            String html = response.getText();

            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataLinks'>"));

            if (bundle.hasHistory() && PermissionUtils.hasPermissions(aip.getPermissions(),
              "org.roda.wui.api.controllers.Browser.retrieveDescriptiveMetadataVersionsBundle")) {
              // History link
              String historyLink = HistoryUtils.createHistoryHashLink(DescriptiveMetadataHistory.RESOLVER, aipId, repId,
                descId);
              String historyLinkHtml = "<a href='" + historyLink
                + "' class='toolbarLink'><i class='fa fa-history'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(historyLinkHtml));
            }
            // Edit link
            if (PermissionUtils.hasPermissions(aip.getPermissions(),
              "org.roda.wui.api.controllers.Browser.updateDescriptiveMetadataFile")) {
              String editLink = HistoryUtils.createHistoryHashLink(EditDescriptiveMetadata.RESOLVER, aipId, repId,
                descId);
              String editLinkHtml = "<a href='" + editLink
                + "' class='toolbarLink' id='representationEditDescriptiveMetadata'><i class='fa fa-edit'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(editLinkHtml));
            }

            // Download link
            SafeUri downloadUri = RestUtils.createRepresentationDescriptiveMetadataDownloadUri(aipId, repId, descId);
            String downloadLinkHtml = "<a href='" + downloadUri.asString()
              + "' class='toolbarLink'><i class='fa fa-download'></i></a>";
            b.append(SafeHtmlUtils.fromSafeConstant(downloadLinkHtml));

            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));

            HTMLPanel buttons = new HTMLPanel(b.toSafeHtml());
            descriptiveMetadataSavedButtons.put(selectedIndex, buttons);
            descriptiveMetadataButtons.setWidget(buttons);

            b = new SafeHtmlBuilder();

            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataHTML'>"));
            b.append(SafeHtmlUtils.fromTrustedString(html));
            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
            SafeHtml safeHtml = b.toSafeHtml();

            callback.onSuccess(safeHtml);
          } else {
            String text = response.getText();
            String message;
            try {
              RestErrorOverlayType error = JsonUtils.safeEval(text);
              message = error.getMessage();
            } catch (IllegalArgumentException e) {
              message = text;
            }

            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataLinks'>"));

            if (bundle.hasHistory() && PermissionUtils.hasPermissions(aip.getPermissions(),
              "org.roda.wui.api.controllers.Browser.retrieveDescriptiveMetadataVersionsBundle")) {
              // History link
              String historyLink = HistoryUtils.createHistoryHashLink(DescriptiveMetadataHistory.RESOLVER, aipId, repId,
                descId);
              String historyLinkHtml = "<a href='" + historyLink
                + "' class='toolbarLink'><i class='fa fa-history'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(historyLinkHtml));
            }

            // Edit link
            if (PermissionUtils.hasPermissions(aip.getPermissions(),
              "org.roda.wui.api.controllers.Browser.updateDescriptiveMetadataFile")) {
              String editLink = HistoryUtils.createHistoryHashLink(EditDescriptiveMetadata.RESOLVER, aipId, repId,
                descId);
              String editLinkHtml = "<a href='" + editLink + "' class='toolbarLink'><i class='fa fa-edit'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(editLinkHtml));
            }

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
    HistoryUtils.newHistory(CreateDescriptiveMetadata.RESOLVER, RodaConstants.RODA_OBJECT_REPRESENTATION, aipId, repId);
  }

  @UiHandler("newDescriptiveMetadata")
  void buttonNewDescriptiveMetadataEventsHandler(ClickEvent e) {
    newRepresentationDescriptiveMetadata();
  }
}
