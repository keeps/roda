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
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class EditRepresentationInformation extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        Services services = new Services("Retrieve representation information", "get");
        services
          .representationInformationResource(
            s -> s.findByUuid(historyTokens.get(0), LocaleInfo.getCurrentLocale().getLocaleName()))
          .whenComplete((representationInformation, throwable) -> {
            if (throwable == null) {
              EditRepresentationInformation editRepresentationInformation = new EditRepresentationInformation(
                representationInformation);
              callback.onSuccess(editRepresentationInformation);
            }
          });
      } else {
        HistoryUtils.newHistory(RepresentationInformationNetwork.RESOLVER);
        callback.onSuccess(null);
      }
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
      return "edit_representation_information";
    }
  };
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private final RepresentationInformation ri;
  // Modern layout components to match CreateRepresentationInformation
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

  public EditRepresentationInformation(RepresentationInformation ri) {
    this.ri = ri;
    initWidget(uiBinder.createAndBindUi(this));

    RepresentationInformationDataPanel dataPanel = new RepresentationInformationDataPanel(true, ri);
    representationInformationDataPanel.add(dataPanel);

    dataPanel.setSaveHandler(() -> {
      if (dataPanel.isChanged()) {
        String formatId = ri.getId();
        RepresentationInformation updatedRi = dataPanel.getRepresentationInformation();
        updatedRi.setId(formatId);

        Services services = new Services("Update representation information", "update");
        RepresentationInformationCreateRequest updateRequest = new RepresentationInformationCreateRequest();
        updateRequest.setRepresentationInformation(updatedRi);
        updateRequest.setForm(dataPanel.getCustomForm());

        services.representationInformationResource(s -> s.updateRepresentationInformation(updateRequest))
          .whenComplete((representationInformation, throwable) -> {
            if (throwable == null) {
              HistoryUtils.newHistory(ShowRepresentationInformation.RESOLVER, representationInformation.getId());
            }
          });
      } else {
        cancel();
      }
    });

    dataPanel.setCancelHandler(this::cancel);

    navigationToolbar.withoutButtons().build();

    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getEditRepresentationInformationBreadcrumbs(ri));

    actionsToolbar.setLabel(messages.editRepresentationInformationTitle()); // Or equivalent edit message
    actionsToolbar.build();

    title.setText(messages.editRepresentationInformationTitle());
    title.setIconClass("RepresentationInformation");
    title.addStyleName("mb-16");

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");
  }

  private void cancel() {
    HistoryUtils.newHistory(ShowRepresentationInformation.RESOLVER, ri.getId());
  }

  interface MyUiBinder extends UiBinder<Widget, EditRepresentationInformation> {
  }
}
