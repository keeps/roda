package org.roda.wui.client.disposal.association;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmation;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.disposal.confirmations.ShowDisposalConfirmation;
import org.roda.wui.client.disposal.schedule.ShowDisposalSchedule;
import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalConfirmationPanel extends Composite {
  private static final List<String> fieldsToReturn = new ArrayList<>(
    Arrays.asList(RodaConstants.DISPOSAL_COFIRMATION_ID, RodaConstants.DISPOSAL_CONFIRMATION_TITLE));

  interface MyUiBinder extends UiBinder<Widget, DisposalConfirmationPanel> {
  }

  private static DisposalConfirmationPanel.MyUiBinder uiBinder = GWT.create(DisposalConfirmationPanel.MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel disposalConfirmationPanel;

  @UiField
  FlowPanel confirmationInfo;

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
  }
}