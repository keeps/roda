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
import java.util.Date;
import java.util.List;

import org.roda.core.data.v2.IndexedAIP;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.RestErrorOverlayType;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria
 * 
 */
public class PreservationEvents extends Composite {

  private static final String TOP_ICON = "<span class='roda-logo'></span>";

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        final String aipId = historyTokens.get(0);
        PreservationEvents preservationEvents = new PreservationEvents(aipId);
        callback.onSuccess(preservationEvents);
      } else {
        Tools.newHistory(Browse.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {Browse.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(Browse.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "events";
    }
  };

  public static final List<String> getViewItemHistoryToken(String id) {
    return Tools.concat(RESOLVER.getHistoryPath(), id);
  }

  interface MyUiBinder extends UiBinder<Widget, PreservationEvents> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static BrowseMessages messages = (BrowseMessages) GWT.create(BrowseMessages.class);

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  SimplePanel itemIcon;

  @UiField
  Label itemTitle;

  @UiField
  Label itemDates;

  @UiField
  FlowPanel premisContainer;

  @UiField
  Button downloadButton;

  @UiField
  Button backButton;

  private String aipId;

  private BrowseItemBundle itemBundle;

  /**
   * Create a new panel to edit a user
   * 
   * @param itemBundle
   * 
   */
  public PreservationEvents(String aipId) {
    this.aipId = aipId;

    initWidget(uiBinder.createAndBindUi(this));

    downloadButton.setEnabled(false);

    BrowserService.Util.getInstance().getItemBundle(aipId, LocaleInfo.getCurrentLocale().getLocaleName(),
      new AsyncCallback<BrowseItemBundle>() {

        @Override
        public void onFailure(Throwable caught) {
          Tools.newHistory(Browse.RESOLVER);
        }

        @Override
        public void onSuccess(BrowseItemBundle itemBundle) {
          PreservationEvents.this.itemBundle = itemBundle;
          viewAction();
        }
      });
  }

  public void viewAction() {
    IndexedAIP aip = itemBundle.getAip();
    final PreservationMetadataBundle preservationMetadata = itemBundle.getPreservationMetadata();

    breadcrumb.updatePath(getBreadcrumbsFromAncestors(itemBundle.getAIPAncestors(), aip));
    breadcrumb.setVisible(true);

    HTMLPanel itemIconHtmlPanel = DescriptionLevelUtils.getElementLevelIconHTMLPanel(aip.getLevel());
    itemIconHtmlPanel.addStyleName("browseItemIcon-other");
    itemIcon.setWidget(itemIconHtmlPanel);
    itemTitle.setText(aip.getTitle() != null ? aip.getTitle() : aip.getId());
    itemTitle.removeStyleName("browseTitle-allCollections");
    itemDates.setText(getDatesText(aip));

    if (!preservationMetadata.getRepresentationsMetadata().isEmpty()) {
      downloadButton.setEnabled(true);
      
      for (RepresentationPreservationMetadataBundle bundle : preservationMetadata.getRepresentationsMetadata()) {
        String repId = bundle.getRepresentationID();
        getPreservationMetadataHTML(aipId, repId, new AsyncCallback<SafeHtml>() {

          @Override
          public void onFailure(Throwable caught) {
            Toast.showError(messages.errorLoadingPreservationMetadata(caught.getMessage()));
          }

          @Override
          public void onSuccess(SafeHtml result) {
            HTML html = new HTML(result);
            premisContainer.add(html);
            JavascriptUtils.runHighlighter(html.getElement());
            JavascriptUtils.slideToggle(html.getElement(), ".toggle-next");
            JavascriptUtils.smoothScroll(html.getElement());
          }
        });
      }

      premisContainer.addStyleName("preservationMetadata");
      premisContainer.addStyleName("metadataContent");
    }
  }

  private List<BreadcrumbItem> getBreadcrumbsFromAncestors(List<IndexedAIP> aipAncestors, IndexedAIP aip) {
    List<BreadcrumbItem> ret = new ArrayList<>();
    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(TOP_ICON), RESOLVER.getHistoryPath()));
    for (IndexedAIP ancestor : aipAncestors) {
      SafeHtml breadcrumbLabel = getBreadcrumbLabel(ancestor);
      BreadcrumbItem ancestorBreadcrumb = new BreadcrumbItem(breadcrumbLabel,
        Tools.concat(Browse.RESOLVER.getHistoryPath(), ancestor.getId()));
      ret.add(1, ancestorBreadcrumb);
    }

    ret.add(new BreadcrumbItem(getBreadcrumbLabel(aip), Tools.concat(Browse.RESOLVER.getHistoryPath(), aip.getId())));
    return ret;
  }

  private SafeHtml getBreadcrumbLabel(IndexedAIP ancestor) {
    SafeHtml elementLevelIconSafeHtml = DescriptionLevelUtils.getElementLevelIconSafeHtml(ancestor.getLevel());
    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    String label = ancestor.getTitle() != null ? ancestor.getTitle() : ancestor.getId();
    builder.append(elementLevelIconSafeHtml).append(SafeHtmlUtils.fromString(label));
    SafeHtml breadcrumbLabel = builder.toSafeHtml();
    return breadcrumbLabel;
  }

  private String getDatesText(IndexedAIP aip) {
    String ret;

    Date dateInitial = aip.getDateInitial();
    Date dateFinal = aip.getDateFinal();

    if (dateInitial == null && dateFinal == null) {
      ret = messages.titleDatesEmpty();
    } else if (dateInitial != null && dateFinal == null) {
      ret = messages.titleDatesNoFinal(dateInitial);
    } else if (dateInitial == null && dateFinal != null) {
      ret = messages.titleDatesNoInitial(dateFinal);
    } else {
      ret = messages.titleDates(dateInitial, dateFinal);
    }

    return ret;
  }

  private void getPreservationMetadataHTML(final String aipId, final String repId,
    final AsyncCallback<SafeHtml> callback) {
    String uri = RestUtils.createPreservationMetadataHTMLUri(aipId, repId, 0, 10, 0, 10, 0, 10);
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uri);
    requestBuilder.setHeader("Authorization", "Custom");
    try {
      requestBuilder.sendRequest(null, new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (200 == response.getStatusCode()) {
            String html = response.getText();
            SafeHtml safeHtml = SafeHtmlUtils.fromTrustedString(html);

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

            b.append(SafeHtmlUtils.fromSafeConstant("<span class='error'>"));
            b.append(messages.preservationMetadataTranformToHTMLError());
            b.append(SafeHtmlUtils.fromSafeConstant("<pre><code>"));
            b.append(SafeHtmlUtils.fromString(message));
            b.append(SafeHtmlUtils.fromSafeConstant("</core></pre>"));
            b.append(SafeHtmlUtils.fromSafeConstant("</span>"));

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

  @UiHandler("downloadButton")
  void buttonDownloadHandler(ClickEvent e) {
    SafeUri downloadUri = RestUtils.createPreservationMetadataDownloadUri(aipId);
    if (downloadUri != null) {
      Window.Location.assign(downloadUri.asString());
    }
  }

  @UiHandler("backButton")
  void buttonBackHandler(ClickEvent e) {
    Tools.newHistory(Tools.concat(Browse.RESOLVER.getHistoryPath(), aipId));
  }
}
