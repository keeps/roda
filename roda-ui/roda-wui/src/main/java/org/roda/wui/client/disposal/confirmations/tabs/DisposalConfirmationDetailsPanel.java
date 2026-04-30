package org.roda.wui.client.disposal.confirmations.tabs;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.HTML;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.wui.client.common.ActionsToolbar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;
import org.roda.wui.client.common.actions.DisposalConfirmationToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.common.client.tools.RestUtils;

import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class DisposalConfirmationDetailsPanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  ActionsToolbar actionsToolbar;

  @UiField
  FlowPanel detailsPanel;

  public DisposalConfirmationDetailsPanel(DisposalConfirmation disposalConfirmation) {
    initWidget(uiBinder.createAndBindUi(this));

    actionsToolbar
      .setActionableMenu(new ActionableWidgetBuilder<DisposalConfirmation>(DisposalConfirmationToolbarActions.get())
        .buildGroupedListWithObjects(new ActionableObject<>(disposalConfirmation),
          List.of(DisposalConfirmationToolbarActions.DisposalConfirmationReportAction.PRINT),
          List.of(DisposalConfirmationToolbarActions.DisposalConfirmationReportAction.PRINT)),
        true);

    actionsToolbar.setLabelVisible(false);
    actionsToolbar.setTagsVisible(false);

    showDisposalConfirmationDetails(disposalConfirmation);
  }

  private void showDisposalConfirmationDetails(DisposalConfirmation disposalConfirmation) {
    SafeUri uri = RestUtils.createDisposalConfirmationHTMLUri(disposalConfirmation.getId(), false);
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uri.asString());
    try {
      requestBuilder.sendRequest(null, new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (200 == response.getStatusCode()) {
            HTML reportHtml = new HTML(SafeHtmlUtils.fromSafeConstant(response.getText()));
            detailsPanel.add(reportHtml);
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

  interface MyUiBinder extends UiBinder<Widget, DisposalConfirmationDetailsPanel> {
    Widget createAndBindUi(DisposalConfirmationDetailsPanel detailsPanel);
  }
}
