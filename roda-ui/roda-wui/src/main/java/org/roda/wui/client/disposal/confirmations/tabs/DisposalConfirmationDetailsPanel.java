package org.roda.wui.client.disposal.confirmations.tabs;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.FlowPanel;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.client.common.actions.DisposalConfirmationToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.common.client.tools.RestUtils;

import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalConfirmationDetailsPanel extends GenericMetadataCardPanel<DisposalConfirmation> {

  public DisposalConfirmationDetailsPanel(DisposalConfirmation disposalConfirmation) {
    setData(disposalConfirmation);
  }

  @Override
  protected FlowPanel createHeaderWidget(DisposalConfirmation disposalConfirmation) {
    if (disposalConfirmation == null) {
      return null;
    }

    return new ActionableWidgetBuilder<DisposalConfirmation>(DisposalConfirmationToolbarActions.get())
      .buildGroupedListWithObjects(new ActionableObject<>(disposalConfirmation),
        List.of(DisposalConfirmationToolbarActions.DisposalConfirmationReportAction.PRINT),
        List.of(DisposalConfirmationToolbarActions.DisposalConfirmationReportAction.PRINT));
  }

  @Override
  protected void buildFields(DisposalConfirmation disposalConfirmation) {
    SafeUri uri = RestUtils.createDisposalConfirmationHTMLUri(disposalConfirmation.getId(), false);
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uri.asString());

    try {
      requestBuilder.sendRequest(null, new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (200 == response.getStatusCode()) {
            HTML reportHtml = new HTML(SafeHtmlUtils.fromSafeConstant(response.getText()));
            metadataContainer.add(reportHtml);
          }
        }

        @Override
        public void onError(Request request, Throwable throwable) {

        }

      });
    } catch (RequestException e) {
      throw new RuntimeException(e);
    }
  }
}