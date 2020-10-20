package org.roda.wui.client.disposal.confirmations;

import java.util.List;

import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.disposal.DisposalConfirmations;
import org.roda.wui.client.planning.CreateRepresentationInformation;
import org.roda.wui.client.planning.RepresentationInformationDataPanel;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CreateDisposalConfirmation extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {
    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      CreateDisposalConfirmation createDisposalConfirmation = new CreateDisposalConfirmation();
      callback.onSuccess(createDisposalConfirmation);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(DisposalConfirmations.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "create_confirmation";
    }
  };

  private static CreateDisposalConfirmation instance = null;
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, CreateDisposalConfirmation> {
  }

  private static CreateDisposalConfirmation.MyUiBinder uiBinder = GWT
    .create(CreateDisposalConfirmation.MyUiBinder.class);

  /**
   * Create a new panel to create a disposal confirmation
   */
  public CreateDisposalConfirmation() {
    initWidget(uiBinder.createAndBindUi(this));
  }
}
