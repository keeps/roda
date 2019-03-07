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
package org.roda.wui.client.portal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.DipFilePreview;
import org.roda.wui.client.browse.Viewers;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataViewBundle;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.RestErrorOverlayType;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;
import org.roda.wui.common.client.widgets.wcag.WCAGUtilities;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Element;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class BrowseAIPPortal extends Composite {
  private static SimplePanel container;

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        BrowseAIPPortal.getAndRefresh(historyTokens.get(0), callback);
      } else {
        HistoryUtils.newHistory(RESOLVER);
        callback.onSuccess(null);
      }
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

  public static void getAndRefresh(String id, AsyncCallback<Widget> callback) {
    container = new SimplePanel();
    refresh(id, new AsyncCallback<BrowseAIPBundle>() {
      @Override
      public void onFailure(Throwable caught) {
        callback.onFailure(caught);
      }

      @Override
      public void onSuccess(BrowseAIPBundle result) {
        callback.onSuccess(container);
      }
    });
  }

  private static void refresh(String id, AsyncCallback<BrowseAIPBundle> callback) {
    BrowserService.Util.getInstance().retrieveBrowseAIPBundle(id, LocaleInfo.getCurrentLocale().getLocaleName(),
      fieldsToReturn, new AsyncCallback<BrowseAIPBundle>() {

        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(BrowseAIPBundle bundle) {
          container.setWidget(new BrowseAIPPortal(bundle));
          callback.onSuccess(bundle);
        }
      });
  }

  private static final List<String> fieldsToReturn = new ArrayList<>(RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
  static {
    fieldsToReturn.addAll(
      Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_STATE, RodaConstants.AIP_TITLE, RodaConstants.AIP_LEVEL,
        RodaConstants.INGEST_SIP_IDS, RodaConstants.INGEST_JOB_ID, RodaConstants.INGEST_UPDATE_JOB_IDS));
  }

  interface MyUiBinder extends UiBinder<Widget, BrowseAIPPortal> {
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
  TitlePanel title;

  // DESCRIPTIVE METADATA

  @UiField
  HTML descriptiveMetadata;

  @UiField
  FlowPanel preMetadata;

  // DISSEMINATIONS
  @UiField
  SimplePanel disseminationsCard;

  @UiField
  FlowPanel preDisseminations;

  // AIP CHILDREN
  @UiField
  SimplePanel aipChildrenCard;

  @UiField
  FlowPanel preChildren;

  @UiField
  FlowPanel center;

  @UiField
  Label dateCreatedAndModified;

  private BrowseAIPPortal(BrowseAIPBundle bundle) {
    aip = bundle.getAip();
    aipId = aip.getId();
    boolean justActive = AIPState.ACTIVE.equals(aip.getState());

    // INIT
    initWidget(uiBinder.createAndBindUi(this));
    preMetadata.add(new HTMLWidgetWrapper("PreMetadataPortal.html"));
    preDisseminations.add(new HTMLWidgetWrapper("PreDisseminationsPortal.html"));
    preChildren.add(new HTMLWidgetWrapper("PreChildrenPortal.html"));
    preMetadata.addStyleName("preSectionTitle preMetadataTitle");
    preDisseminations.addStyleName("preSectionTitle preDisseminationsTitle");
    preChildren.addStyleName("preSectionTitle preChildrenTitle");

    AsyncCallback<Actionable.ActionImpact> listActionableCallback = new NoAsyncCallback<Actionable.ActionImpact>() {
      @Override
      public void onSuccess(Actionable.ActionImpact impact) {
        if (!Actionable.ActionImpact.NONE.equals(impact)) {
          refresh(aipId, new NoAsyncCallback<>());
        }
      }
    };

    // DISSEMINATIONS
    disseminationsCard.setVisible(false);
    preDisseminations.setVisible(false);

    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_FIND_DIP)) {
      BrowserService.Util.getInstance().retrieveViewersProperties(new AsyncCallback<Viewers>() {

        @Override
        public void onSuccess(Viewers viewers) {
          Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_AIP_UUIDS, aip.getId()));
          Sorter sorter = new Sorter(new SortParameter(RodaConstants.DIP_DATE_CREATED, true));

          BrowserService.Util.getInstance().find(IndexedDIP.class.getName(), filter, sorter, new Sublist(0, 1),
            Facets.NONE, LocaleInfo.getCurrentLocale().getLocaleName(), true,
            Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.DIP_ID),
            new AsyncCallback<IndexResult<IndexedDIP>>() {

              @Override
              public void onFailure(Throwable caught) {
                AsyncCallbackUtils.defaultFailureTreatment(caught);
              }

              @Override
              public void onSuccess(IndexResult<IndexedDIP> result) {
                if (result.getTotalCount() > 0) {
                  String dipId = result.getResults().get(0).getId();
                  Filter fileFilter = new Filter(new SimpleFilterParameter(RodaConstants.DIPFILE_DIP_ID, dipId));
                  BrowserService.Util.getInstance().find(
                    DIPFile.class.getName(), fileFilter, Sorter.NONE, new Sublist(0, 1), Facets.NONE,
                    LocaleInfo.getCurrentLocale().getLocaleName(), true, Arrays.asList(RodaConstants.INDEX_UUID,
                      RodaConstants.DIPFILE_ID, RodaConstants.DIPFILE_SIZE, RodaConstants.DIPFILE_IS_DIRECTORY),
                    new AsyncCallback<IndexResult<DIPFile>>() {

                      @Override
                      public void onFailure(Throwable caught) {
                        AsyncCallbackUtils.defaultFailureTreatment(caught);
                      }

                      @Override
                      public void onSuccess(IndexResult<DIPFile> result) {
                        if (result.getTotalCount() > 0) {
                          disseminationsCard.setVisible(true);
                          preDisseminations.setVisible(true);
                          disseminationsCard.add(new DipFilePreview(viewers, result.getResults().get(0)));
                        }
                      }
                    });
                }
              }
            });
        }

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.treatCommonFailures(caught);
        }
      });
    }

    // AIP CHILDREN
    preChildren.setVisible(false);

    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_FIND_AIP)) {
      ListBuilder<IndexedAIP> aipChildrenListBuilder = new ListBuilder<>(() -> new AIPList(),
        new AsyncTableCellOptions<>(IndexedAIP.class, "BrowseAIPPortal_aipChildren")
          .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, aip.getId())))
          .withJustActive(justActive).withSummary(messages.listOfAIPs()).bindOpener()
          .withActionableCallback(listActionableCallback));

      SearchWrapper aipChildrenSearchWrapper = new SearchWrapper(false)
        .createListAndSearchPanel(aipChildrenListBuilder);
      aipChildrenCard.setWidget(aipChildrenSearchWrapper);
      aipChildrenCard.setVisible(bundle.getChildAIPCount() > 0);
      preChildren.setVisible(bundle.getChildAIPCount() > 0);
    }
    // CSS
    addStyleName("browse browse_aip");

    // make FocusPanel comply with WCAG
    Element firstElement = this.getElement().getFirstChildElement();
    if ("input".equalsIgnoreCase(firstElement.getTagName())) {
      firstElement.setAttribute("title", "browse input");
    }

    IndexedAIP aip = bundle.getAip();
    title.setIcon(DescriptionLevelUtils.getElementLevelIconSafeHtml(aip.getLevel(), false));
    title.setText(aip.getTitle() != null ? aip.getTitle() : aip.getId());

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

    // STATE
    this.addStyleName(aip.getState().toString().toLowerCase());
    aipState.setHTML(HtmlSnippetUtils.getAIPStateHTML(aip.getState()));
    aipState.setVisible(!justActive);

    // NAVIGATION TOOLBAR
    navigationToolbar.withObject(aip);
    navigationToolbar.withPermissions(aip.getPermissions());
    navigationToolbar.withActionImpactHandler(Actionable.ActionImpact.UPDATED,
      () -> refresh(aipId, new NoAsyncCallback<>()));
    navigationToolbar.build();
    navigationToolbar.setActionsButtonVisibility(false);
    navigationToolbar.setSearchButtonVisibility(false);

    // DESCRIPTIVE METADATA
    updateSectionDescriptiveMetadata();

    // AIP CHILDREN
    if (bundle.getChildAIPCount() > 0) {
      LastSelectedItemsSingleton.getInstance().setSelectedJustActive(justActive);
    }

    keyboardFocus.setFocus(true);
  }

  private void updateSectionDescriptiveMetadata() {
    getDescriptiveMetadataHTML(aipId, new AsyncCallback<SafeHtml>() {

      @Override
      public void onFailure(Throwable caught) {
        if (!AsyncCallbackUtils.treatCommonFailures(caught)) {
          Toast.showError(messages.errorLoadingDescriptiveMetadata(caught.getMessage()));
        }
      }

      @Override
      public void onSuccess(SafeHtml result) {
        descriptiveMetadata.setHTML(result);
      }
    });

    WCAGUtilities.getInstance().makeAccessible(descriptiveMetadata.getElement());
  }

  private void getDescriptiveMetadataHTML(final String aipId, final AsyncCallback<SafeHtml> callback) {
    try {
      SafeUri uri = RestUtils.createDescriptiveMetadataHTMLUri(aipId, null);
      RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uri.asString());

      requestBuilder.sendRequest(null, new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (200 == response.getStatusCode()) {
            String html = response.getText();

            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataHTML'>"));
            b.append(SafeHtmlUtils.fromTrustedString(html));
            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
            b.append(SafeHtmlUtils.fromSafeConstant("<div style=\"clear: both\"/>"));
            callback.onSuccess(b.toSafeHtml());
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
}
