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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataViewBundle;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.actions.AipActions;
import org.roda.wui.client.common.actions.DisseminationActions;
import org.roda.wui.client.common.actions.RepresentationActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.DIPList;
import org.roda.wui.client.common.lists.RepresentationList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.slider.Sliders;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.PermissionUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.management.UserLog;
import org.roda.wui.client.planning.RiskIncidenceRegister;
import org.roda.wui.client.welcome.Welcome;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.RestErrorOverlayType;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;
import org.roda.wui.common.client.widgets.wcag.WCAGUtilities;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
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
public class BrowseAIP extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.isEmpty()) {
        BrowseTop.RESOLVER.resolve(historyTokens, callback);
      } else if (historyTokens.size() == 1
        && !historyTokens.get(0).equals(EditPermissions.AIP_RESOLVER.getHistoryToken())) {
        String id = historyTokens.get(0);
        if (id == null) {
          HistoryUtils.newHistory(BrowseTop.RESOLVER);
        } else {
          List<String> fieldsToReturn = new ArrayList<>(RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
          fieldsToReturn.addAll(new ArrayList<>(Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_STATE,
            RodaConstants.AIP_TITLE, RodaConstants.AIP_LEVEL, RodaConstants.INGEST_SIP_IDS, RodaConstants.INGEST_JOB_ID,
            RodaConstants.INGEST_UPDATE_JOB_IDS)));

          BrowserService.Util.getInstance().retrieveBrowseAIPBundle(id, LocaleInfo.getCurrentLocale().getLocaleName(),
            fieldsToReturn, new AsyncCallback<BrowseAIPBundle>() {

              @Override
              public void onFailure(Throwable caught) {
                AsyncCallbackUtils.defaultFailureTreatment(caught, Welcome.RESOLVER.getHistoryPath());
              }

              @Override
              public void onSuccess(BrowseAIPBundle bundle) {
                if (bundle != null) {
                  callback.onSuccess(new BrowseAIP(bundle));
                } else {
                  HistoryUtils.newHistory(BrowseTop.RESOLVER);
                }
              }
            });
        }
      } else {
        HistoryUtils.newHistory(RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      BrowseTop.RESOLVER.isCurrentUserPermitted(callback);
    }

    @Override
    public String getHistoryToken() {
      return BrowseTop.RESOLVER.getHistoryToken();
    }

    @Override
    public List<String> getHistoryPath() {
      return BrowseTop.RESOLVER.getHistoryPath();
    }
  };

  interface MyUiBinder extends UiBinder<Widget, BrowseAIP> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private String aipId;
  private IndexedAIP aip;

  // Focus
  @UiField
  FocusPanel keyboardFocus;

  // HEADER

  @UiField
  NavigationToolbar<IndexedAIP> navigationToolbar;

  // STATUS

  @UiField
  HTML aipState;

  // IDENTIFICATION
  @UiField
  Label itemTitle;
  @UiField
  HTML itemIcon;

  @UiField
  FlowPanel identificationPanel;

  // DESCRIPTIVE METADATA

  @UiField
  TabPanel descriptiveMetadata;

  @UiField
  Button newDescriptiveMetadata;

  private SimplePanel descriptiveMetadataButtons;
  private Map<Integer, HTMLPanel> descriptiveMetadataSavedButtons;

  // REPRESENTATIONS
  @UiField
  SimplePanel addRepresentation;

  @UiField(provided = true)
  SearchWrapper representationsSearchWrapper;

  // DISSEMINATIONS

  @UiField(provided = true)
  SearchWrapper disseminationsSearchWrapper;

  // AIP CHILDREN
  @UiField
  SimplePanel addChildAip;

  @UiField(provided = true)
  SearchWrapper aipChildrenSearchWrapper;

  @UiField
  FlowPanel risksEventsLogs;

  @UiField
  FlowPanel center;
  @UiField
  Label dateCreatedAndModified;

  private BrowseAIP(BrowseAIPBundle bundle) {
    aip = bundle.getAip();
    aipId = aip.getId();
    boolean justActive = AIPState.ACTIVE.equals(aip.getState());

    RepresentationActions representationActions = RepresentationActions.get(aip.getId(),
      aip.getPermissions());
    DisseminationActions disseminationActions = DisseminationActions.get();
    AipActions aipActions = AipActions.get(aip.getId(), aip.getState(), aip.getPermissions());

    // REPRESENTATIONS

    ListBuilder<IndexedRepresentation> representationsListBuilder = new ListBuilder<>(RepresentationList::new,
      new AsyncTableCellOptions<>(IndexedRepresentation.class, "BrowseAIP_representations")
        .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aip.getId())))
        .withJustActive(justActive).withSummary(messages.listOfRepresentations()).bindOpener());

    representationsSearchWrapper = new SearchWrapper(false).createListAndSearchPanel(representationsListBuilder,
      representationActions);

    // DISSEMINATIONS

    ListBuilder<IndexedDIP> disseminationsListBuilder = new ListBuilder<>(DIPList::new,
      new AsyncTableCellOptions<>(IndexedDIP.class, "BrowseAIP_disseminations")
        .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.DIP_AIP_UUIDS, aip.getId())))
        .withJustActive(justActive).withSummary(messages.listOfDisseminations()).bindOpener());

    disseminationsSearchWrapper = new SearchWrapper(false).createListAndSearchPanel(disseminationsListBuilder,
      disseminationActions);

    // AIP CHILDREN

    ListBuilder<IndexedAIP> aipChildrenListBuilder = new ListBuilder<>(AIPList::new,
      new AsyncTableCellOptions<>(IndexedAIP.class, "BrowseAIP_aipChildren")
        .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, aip.getId())))
        .withJustActive(justActive).withSummary(messages.listOfAIPs()).bindOpener());

    aipChildrenSearchWrapper = new SearchWrapper(false).createListAndSearchPanel(aipChildrenListBuilder, aipActions);

    // INIT
    initWidget(uiBinder.createAndBindUi(this));

    PermissionUtils.bindPermission(newDescriptiveMetadata, aip.getPermissions(),
      "org.roda.wui.api.controllers.Browser.createDescriptiveMetadataFile");

    // CSS
    newDescriptiveMetadata.getElement().setId("aipNewDescriptiveMetadata");
    addStyleName("browse browse_aip");

    // make FocusPanel comply with WCAG
    Element firstElement = this.getElement().getFirstChildElement();
    if ("input".equalsIgnoreCase(firstElement.getTagName())) {
      firstElement.setAttribute("title", "browse input");
    }

    // STATE
    this.addStyleName(aip.getState().toString().toLowerCase());
    aipState.setHTML(HtmlSnippetUtils.getAIPStateHTML(aip.getState()));
    aipState.setVisible(!justActive);

    // NAVIGATION TOOLBAR
    navigationToolbar.setObject(aip);
    navigationToolbar.show();

    // IDENTIFICATION
    updateSectionIdentification(bundle);

    // DESCRIPTIVE METADATA
    updateSectionDescriptiveMetadata(bundle);

    // REPRESENTATIONS
    if (bundle.getRepresentationCount() == 0) {
      addRepresentation.setWidget(new ActionableWidgetBuilder<>(representationActions).buildListWithObjects(
        new ActionableObject<>(IndexedRepresentation.class),
        Collections.singletonList(RepresentationActions.RepresentationAction.NEW)));
    }

    addRepresentation.setVisible(bundle.getRepresentationCount() == 0);
    representationsSearchWrapper.setVisible(bundle.getRepresentationCount() > 0);
    representationsSearchWrapper.getParent().setVisible(bundle.getRepresentationCount() > 0);

    // DISSEMINATIONS
    disseminationsSearchWrapper.setVisible(bundle.getDipCount() > 0);
    disseminationsSearchWrapper.getParent().setVisible(bundle.getDipCount() > 0);

    // AIP CHILDREN
    if (bundle.getChildAIPCount() > 0) {
      LastSelectedItemsSingleton.getInstance().setSelectedJustActive(justActive);
    } else {
      addChildAip.setWidget(new ActionableWidgetBuilder<>(aipActions).buildListWithObjects(
        new ActionableObject<>(IndexedAIP.class), Collections.singletonList(AipActions.AipAction.NEW_CHILD_AIP_BELOW)));
    }

    addChildAip.setVisible(bundle.getChildAIPCount() == 0);
    aipChildrenSearchWrapper.setVisible(bundle.getChildAIPCount() > 0);
    aipChildrenSearchWrapper.getParent().setVisible(bundle.getChildAIPCount() > 0);

    keyboardFocus.setFocus(true);
  }

  private void updateSectionDescriptiveMetadata(BrowseAIPBundle bundle) {
    final List<Pair<String, HTML>> descriptiveMetadataContainers = new ArrayList<>();
    final Map<String, DescriptiveMetadataViewBundle> bundles = new HashMap<>();

    List<DescriptiveMetadataViewBundle> descMetadata = bundle.getDescriptiveMetadata();
    if (descMetadata != null) {
      for (DescriptiveMetadataViewBundle descMetadatum : descMetadata) {
        String title = descMetadatum.getLabel() != null ? descMetadatum.getLabel() : descMetadatum.getId();
        HTML container = new HTML();
        container.addStyleName("metadataContent");
        descriptiveMetadata.add(container, title);
        descriptiveMetadataContainers.add(Pair.of(descMetadatum.getId(), container));
        bundles.put(descMetadatum.getId(), descMetadatum);
      }
    }

    descriptiveMetadata.addSelectionHandler(event -> {
      if (event.getSelectedItem() < descriptiveMetadataContainers.size()) {
        Pair<String, HTML> pair = descriptiveMetadataContainers.get(event.getSelectedItem());
        String descId = pair.getFirst();
        final HTML html = pair.getSecond();
        final DescriptiveMetadataViewBundle descBundle = bundles.get(descId);
        if (html.getText().length() == 0) {
          getDescriptiveMetadataHTML(aipId, descId, descBundle, event.getSelectedItem(), new AsyncCallback<SafeHtml>() {

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
    });

    if (PermissionUtils.hasPermissions(aip.getPermissions(),
      "org.roda.wui.api.controllers.Browser.createDescriptiveMetadataFile")) {
      final int addTabIndex = descriptiveMetadata.getWidgetCount();
      FlowPanel addTab = new FlowPanel();
      addTab.add(new HTML(SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-plus-circle\"></i>")));
      descriptiveMetadata.add(new Label(), addTab);
      descriptiveMetadata.addSelectionHandler(event -> {
        if (event.getSelectedItem() == addTabIndex) {
          newDescriptiveMetadataRedirect();
        }
      });

      addTab.addStyleName("addTab");
      addTab.getElement().setId("aipNewDescriptiveMetadata");
      addTab.getParent().addStyleName("addTabWrapper");
    }

    descriptiveMetadataSavedButtons = new HashMap<>();
    descriptiveMetadataButtons = new SimplePanel();
    descriptiveMetadataButtons.addStyleName("descriptiveMetadataTabButtons");
    descriptiveMetadata.getTabBar().getElement().getStyle().clearProperty("width");
    descriptiveMetadata.getTabBar().getElement().getParentElement()
      .insertFirst(descriptiveMetadataButtons.getElement());
    descriptiveMetadata.addSelectionHandler(event -> {
      if (descriptiveMetadataSavedButtons.containsKey(event.getSelectedItem())) {
        descriptiveMetadataButtons.setWidget(descriptiveMetadataSavedButtons.get(event.getSelectedItem()));
      } else {
        descriptiveMetadataButtons.clear();
      }
    });

    if (descMetadata != null && !descMetadata.isEmpty()) {
      descriptiveMetadata.setVisible(true);
      descriptiveMetadata.selectTab(0);
      newDescriptiveMetadata.setVisible(false);
    } else {
      descriptiveMetadata.setVisible(false);
      newDescriptiveMetadata.setVisible(true);
    }

    WCAGUtilities.getInstance().makeAccessible(descriptiveMetadata.getElement());
  }

  private void updateSectionIdentification(BrowseAIPBundle bundle) {
    IndexedAIP aip = bundle.getAip();
    itemIcon.setHTML(DescriptionLevelUtils.getElementLevelIconSafeHtml(aip.getLevel(), false));
    itemTitle.setText(aip.getTitle() != null ? aip.getTitle() : aip.getId());

    Sliders.createAipInfoSlider(center, navigationToolbar.getSidebarButton(), bundle);

    Anchor risksLink = new Anchor(messages.aipRiskIncidences(bundle.getRiskIncidenceCount()),
      HistoryUtils.createHistoryHashLink(RiskIncidenceRegister.RESOLVER, aip.getId()));
    Anchor eventsLink = new Anchor(messages.aipEvents(bundle.getPreservationEventCount()),
      HistoryUtils.createHistoryHashLink(PreservationEvents.BROWSE_RESOLVER, aip.getId()));
    Anchor logsLink = new Anchor(messages.aipLogs(bundle.getLogCount()),
      HistoryUtils.createHistoryHashLink(UserLog.RESOLVER, aip.getId()));

    risksEventsLogs.clear();
    risksEventsLogs.add(risksLink);
    risksEventsLogs.add(new Label(", "));
    risksEventsLogs.add(eventsLink);
    risksEventsLogs.add(new Label(" " + messages.and() + " "));
    risksEventsLogs.add(logsLink);

    navigationToolbar.updateBreadcrumb(bundle);

    if (aip.getCreatedOn() != null && StringUtils.isNotBlank(aip.getCreatedBy()) && aip.getUpdatedOn() != null
      && StringUtils.isNotBlank(aip.getUpdatedBy())) {
      dateCreatedAndModified.setText(messages.dateCreatedAndUpdated(Humanize.formatDate(aip.getCreatedOn()),
        aip.getCreatedBy(), Humanize.formatDate(aip.getUpdatedOn()), aip.getUpdatedBy()));
    } else if (aip.getCreatedOn() != null && StringUtils.isNotBlank(aip.getCreatedBy())) {
      dateCreatedAndModified
        .setText(messages.dateCreated(Humanize.formatDateTime(aip.getCreatedOn()), aip.getCreatedBy()));
    } else if (aip.getUpdatedOn() != null && StringUtils.isNotBlank(aip.getUpdatedBy())) {
      dateCreatedAndModified
        .setText(messages.dateUpdated(Humanize.formatDateTime(aip.getUpdatedOn()), aip.getUpdatedBy()));
    } else {
      dateCreatedAndModified.setText("");
    }
  }

  private void getDescriptiveMetadataHTML(final String aipId, final String descId,
    final DescriptiveMetadataViewBundle bundle, final Integer selectedIndex, final AsyncCallback<SafeHtml> callback) {
    SafeUri uri = RestUtils.createDescriptiveMetadataHTMLUri(aipId, descId);
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uri.asString());
    try {
      requestBuilder.sendRequest(null, new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
          String escapedDescId = SafeHtmlUtils.htmlEscape(descId);

          if (200 == response.getStatusCode()) {
            String html = response.getText();

            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataLinks'>"));

            if (bundle.hasHistory() && PermissionUtils.hasPermissions(aip.getPermissions(),
              "org.roda.wui.api.controllers.Browser.retrieveDescriptiveMetadataVersionsBundle")) {
              // History link
              String historyLink = HistoryUtils.createHistoryHashLink(DescriptiveMetadataHistory.RESOLVER, aipId,
                escapedDescId);
              String historyLinkHtml = "<a href='" + historyLink
                + "' class='toolbarLink'><i class='fa fa-history'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(historyLinkHtml));
            }

            // Edit link
            if (PermissionUtils.hasPermissions(aip.getPermissions(),
              "org.roda.wui.api.controllers.Browser.updateDescriptiveMetadataFile")) {
              String editLink = HistoryUtils.createHistoryHashLink(EditDescriptiveMetadata.RESOLVER, aipId,
                escapedDescId);
              String editLinkHtml = "<a href='" + editLink
                + "' class='toolbarLink' id='aipEditDescriptiveMetadata'><i class='fa fa-edit'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(editLinkHtml));
            }

            // Download link
            SafeUri downloadUri = RestUtils.createDescriptiveMetadataDownloadUri(aipId, escapedDescId);
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
              String historyLink = HistoryUtils.createHistoryHashLink(DescriptiveMetadataHistory.RESOLVER, aipId,
                escapedDescId);
              String historyLinkHtml = "<a href='" + historyLink
                + "' class='toolbarLink'><i class='fa fa-history'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(historyLinkHtml));
            }

            // Edit link
            if (PermissionUtils.hasPermissions(aip.getPermissions(),
              "org.roda.wui.api.controllers.Browser.updateDescriptiveMetadataFile")) {
              String editLink = HistoryUtils.createHistoryHashLink(EditDescriptiveMetadata.RESOLVER, aipId,
                escapedDescId);
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

  @UiHandler("newDescriptiveMetadata")
  void buttonNewDescriptiveMetadataHandler(ClickEvent e) {
    newDescriptiveMetadataRedirect();
  }

  private void newDescriptiveMetadataRedirect() {
    if (aipId != null) {
      HistoryUtils.newHistory(RESOLVER, CreateDescriptiveMetadata.RESOLVER.getHistoryToken(),
        RodaConstants.RODA_OBJECT_AIP, aipId);
    }
  }
}
