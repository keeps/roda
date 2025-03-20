/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.planning;

import java.util.List;

import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationCreateRequest;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class CreateRepresentationInformation extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      CreateRepresentationInformation createRepresentationInformation = new CreateRepresentationInformation(
        new RepresentationInformation());
      callback.onSuccess(createRepresentationInformation);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(RepresentationInformationNetwork.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "create_representation_information";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, CreateRepresentationInformation> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private RepresentationInformation ri;

  @UiField
  Button buttonApply;

  @UiField
  Button buttonCancel;

  @UiField(provided = true)
  RepresentationInformationDataPanel representationInformationDataPanel;

  public CreateRepresentationInformation(RepresentationInformation ri) {
    this.ri = ri;
    this.representationInformationDataPanel = new RepresentationInformationDataPanel(true, false, ri);
    initWidget(uiBinder.createAndBindUi(this));
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    if (representationInformationDataPanel.isValid()) {
      ri = representationInformationDataPanel.getRepresentationInformation();
      Services services = new Services("Create representation information", "create");
      RepresentationInformationCreateRequest request = new RepresentationInformationCreateRequest();
      request.setRepresentationInformation(ri);
      request.setForm(representationInformationDataPanel.getCustomForm());
      services.representationInformationResource(s -> s.createRepresentationInformation(request))
        .whenComplete((representationInformation, throwable) -> {
          if (throwable == null) {
            HistoryUtils.newHistory(ShowRepresentationInformation.RESOLVER, representationInformation.getId());
          }
        });
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    History.back();
  }
}
