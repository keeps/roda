package org.roda.wui.client.browse.tabs;

import com.google.gwt.user.client.ui.Widget;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfo;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.wui.client.browse.DescriptiveMetadataHistory;
import org.roda.wui.client.browse.EditDescriptiveMetadata;
import org.roda.wui.client.common.ActionsToolbar;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.RestErrorOverlayType;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class AIPDescriptiveMetadataTabs extends Tabs {

  public void init(IndexedAIP aip, DescriptiveMetadataInfos descriptiveMetadataInfos) {
    for (DescriptiveMetadataInfo metadataInfo : descriptiveMetadataInfos.getDescriptiveMetadataInfoList()) {
      // Tab button
      SafeHtml buttonTitle = SafeHtmlUtils.fromString(metadataInfo.getLabel());
      // Content container
      FlowPanel content = new FlowPanel();
      content.addStyleName("descriptiveMetadataTabContainer roda6CardWithHeader");
      // Create Toolbar
      FlowPanel cardHeader = new FlowPanel();
      cardHeader.setStyleName("cardHeader");
      String metadataID = SafeHtmlUtils.htmlEscape(metadataInfo.getId());
      ActionsToolbar descriptiveMetadataToolbar = new ActionsToolbar();
      descriptiveMetadataToolbar.setLabelVisible(false);
      descriptiveMetadataToolbar.setTagsVisible(false);
      cardHeader.add(descriptiveMetadataToolbar);
      content.add(cardHeader);
      // Get metadata and populate widget
      FlowPanel cardBody = new FlowPanel();
      content.add(cardBody);
      cardBody.setStyleName("cardBody");
      HTML metadataHTML = new HTML();
      SafeUri uri = RestUtils.createDescriptiveMetadataHTMLUri(aip.getId(), metadataID);
      RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uri.asString());
      try {
        requestBuilder.sendRequest(null, new RequestCallback() {
          @Override
          public void onResponseReceived(Request request, Response response) {
            // Edit button
            if (!AIPState.DESTROYED.equals(aip.getState()) && !aip.isOnHold() && aip.getDisposalConfirmationId() == null
              && PermissionClientUtils.hasPermissions(aip.getPermissions(),
                RodaConstants.PERMISSION_METHOD_UPDATE_AIP_DESCRIPTIVE_METADATA_FILE)) {
              descriptiveMetadataToolbar.addAction(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                  HistoryUtils.newHistory(EditDescriptiveMetadata.RESOLVER, aip.getId(), metadataID);
                }
              }, messages.editButton(), "btn-edit");
            }

            // History button
            if (metadataInfo.isHasHistory() && PermissionClientUtils.hasPermissions(aip.getPermissions(),
              RodaConstants.PERMISSION_METHOD_RETRIEVE_AIP_DESCRIPTIVE_METADATA_VERSIONS) && !aip.isOnHold()) {
              descriptiveMetadataToolbar.addAction(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                  HistoryUtils.newHistory(DescriptiveMetadataHistory.RESOLVER, aip.getId(), metadataID);
                }
              }, messages.historyDescriptiveMetadataTitle(), "btn-history");
            }

            SafeHtml safeHtml;
            if (200 == response.getStatusCode()) {
              // Download button
              descriptiveMetadataToolbar.addAction(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                  Window.Location
                    .assign(RestUtils.createDescriptiveMetadataDownloadUri(aip.getId(), metadataID).asString());
                }
              }, messages.downloadButton(), "btn-download");
              // HTML
              String html = response.getText();
              SafeHtmlBuilder b = new SafeHtmlBuilder();
              b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataHTML'>"));
              b.append(SafeHtmlUtils.fromTrustedString(html));
              b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
              safeHtml = b.toSafeHtml();
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
              safeHtml = b.toSafeHtml();
            }
            metadataHTML.setHTML(safeHtml);
          }

          @Override
          public void onError(Request request, Throwable e) {
            if (!AsyncCallbackUtils.treatCommonFailures(e)) {
              Toast.showError(messages.errorLoadingDescriptiveMetadata(e.getMessage()));
            }
          }
        });
      } catch (RequestException e) {
        if (!AsyncCallbackUtils.treatCommonFailures(e)) {
          Toast.showError(messages.errorLoadingDescriptiveMetadata(e.getMessage()));
        }
      }
      cardBody.add(metadataHTML);
      // Create and add tab
      // This descriptive metadata content is NOT lazy loading!
      createAndAddTab(buttonTitle, new TabContentBuilder() {
        @Override
        public Widget buildTabWidget() {
          return content;
        }
      });
    }
  }
}
