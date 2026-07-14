/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.planning.ri;

import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationCreateRequest;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoActionsToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class CreateRepresentationInformation extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      CreateRepresentationInformation createRepresentationInformation = new CreateRepresentationInformation();
      callback.onSuccess(createRepresentationInformation);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
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

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FocusPanel keyboardFocus;
  @UiField
  NavigationToolbar<RepresentationInformation> navigationToolbar;
  @UiField
  NoActionsToolbar actionsToolbar;
  @UiField
  TitlePanel title;
  @UiField
  FlowPanel representationInformationDataPanel;

  public CreateRepresentationInformation() {
    initWidget(uiBinder.createAndBindUi(this));

    RepresentationInformationDataPanel dataPanel = new RepresentationInformationDataPanel(false,
      new RepresentationInformation());
    representationInformationDataPanel.add(dataPanel);

    dataPanel.setSaveHandler(() -> {
      Services services = new Services("Create representation information", "create");
      RepresentationInformationCreateRequest request = new RepresentationInformationCreateRequest();
      request.setRepresentationInformation(dataPanel.getValue());
      request.setForm(dataPanel.getCustomForm());
      services.representationInformationResource(s -> s.createRepresentationInformation(request))
        .whenComplete((representationInformation, throwable) -> {
          if (throwable == null) {
            HistoryUtils.newHistory(ShowRepresentationInformation.RESOLVER, representationInformation.getId());
          }
        });
    });

    dataPanel.setCancelHandler(() -> HistoryUtils.newHistory(RepresentationInformationNetwork.RESOLVER));

    navigationToolbar.withoutButtons().build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getCreateRepresentationInformationBreadcrumbs());

    actionsToolbar.setLabel(messages.showRepresentationInformationTitle());
    actionsToolbar.build();

    title.setText(messages.createRepresentationInformationTitle());
    title.setIconClass("RepresentationInformation");
    title.addStyleName("mb-16");

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");
  }

  interface MyUiBinder extends UiBinder<Widget, CreateRepresentationInformation> {
  }
}
