/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.association;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmation;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationState;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.disposal.confirmations.ShowDisposalConfirmation;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalConfirmationPanel extends Composite {
  private static final List<String> fieldsToReturn = new ArrayList<>(
    Arrays.asList(RodaConstants.DISPOSAL_CONFIRMATION_ID, RodaConstants.DISPOSAL_CONFIRMATION_TITLE));

  interface MyUiBinder extends UiBinder<Widget, DisposalConfirmationPanel> {
  }

  private static DisposalConfirmationPanel.MyUiBinder uiBinder = GWT.create(DisposalConfirmationPanel.MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel disposalConfirmationPanel;

  @UiField
  FlowPanel confirmationInfo;

  @UiField
  Label disposalConfirmationCreationDate;

  @UiField
  FlowPanel content;

  private DisposalConfirmation disposalConfirmation;

  public DisposalConfirmationPanel(String disposalConfirmationId) {
    initWidget(uiBinder.createAndBindUi(this));

    if (disposalConfirmationId == null) {
      disposalConfirmationPanel.clear();
    } else {

      BrowserService.Util.getInstance().retrieve(DisposalConfirmation.class.getName(), disposalConfirmationId,
        fieldsToReturn, new NoAsyncCallback<IsIndexed>() {
          @Override
          public void onSuccess(IsIndexed isIndexed) {
            disposalConfirmation = (DisposalConfirmation) isIndexed;
            init();
          }
        });
    }
  }

  private void init() {
    Anchor confirmationLink = new Anchor(disposalConfirmation.getTitle(),
      HistoryUtils.createHistoryHashLink(ShowDisposalConfirmation.RESOLVER, disposalConfirmation.getId()));

    confirmationInfo.add(confirmationLink);

    disposalConfirmationCreationDate.setText(Humanize.formatDate(disposalConfirmation.getCreatedOn()));

    if (DisposalConfirmationState.APPROVED.equals(disposalConfirmation.getState())
      || DisposalConfirmationState.PERMANENTLY_DELETED.equals(disposalConfirmation.getState())) {
      content.add(getFlowPanelWithLabel("Executed on", Humanize.formatDate(disposalConfirmation.getExecutedOn())));
      content.add(getFlowPanelWithLabel("Executed by", disposalConfirmation.getExecutedBy()));
    } else if (DisposalConfirmationState.RESTORED.equals(disposalConfirmation.getState())) {
      content.add(getFlowPanelWithLabel("Restored on", Humanize.formatDate(disposalConfirmation.getRestoredOn())));
      content.add(getFlowPanelWithLabel("Restored by", disposalConfirmation.getRestoredBy()));
    }

    content.add(getFlowPanelWithHTML(messages.disposalConfirmationStatus(),
      HtmlSnippetUtils.getDisposalConfirmationStateHTML(disposalConfirmation.getState())));
  }

  private FlowPanel getFlowPanelWithLabel(String labelText, String messageText) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("field");

    Label label = new Label(labelText);
    label.addStyleName("label");

    Label value = new Label(messageText);
    value.addStyleName("value");

    panel.add(label);
    panel.add(value);

    return panel;
  }

  private FlowPanel getFlowPanelWithHTML(String labelText, SafeHtml message) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("field");

    Label label = new Label(labelText);
    label.addStyleName("label");

    HTML value = new HTML();
    value.setHTML(message);
    value.addStyleName("value");

    panel.add(label);
    panel.add(value);

    return panel;
  }
}