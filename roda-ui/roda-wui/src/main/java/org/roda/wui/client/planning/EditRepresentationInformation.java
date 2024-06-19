/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.planning;

import java.util.Collections;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.generics.select.SelectedItemsListRequest;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationCreateRequest;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
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
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
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

  interface MyUiBinder extends UiBinder<Widget, EditRepresentationInformation> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private RepresentationInformation ri;

  @UiField
  Button buttonApply;

  @UiField
  Button buttonRemove;

  @UiField
  Button buttonCancel;

  @UiField(provided = true)
  RepresentationInformationDataPanel representationInformationDataPanel;

  public EditRepresentationInformation(RepresentationInformation ri) {
    this.ri = ri;
    this.representationInformationDataPanel = new RepresentationInformationDataPanel(true, true, ri);
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    if (representationInformationDataPanel.isChanged() && representationInformationDataPanel.isValid()) {
      String formatId = ri.getId();
      ri = representationInformationDataPanel.getRepresentationInformation();
      ri.setId(formatId);
      Services services = new Services("Update representation information", "update");
      RepresentationInformationCreateRequest updateRequest = new RepresentationInformationCreateRequest();
      updateRequest.setRepresentationInformation(ri);
      updateRequest.setForm(representationInformationDataPanel.getCustomForm());
      services.representationInformationResource(s -> s.updateRepresentationInformation(updateRequest))
        .whenComplete((representationInformation, throwable) -> {
          if (throwable == null) {
            HistoryUtils.newHistory(ShowRepresentationInformation.RESOLVER, representationInformation.getId());
          }
        });
    } else {
      HistoryUtils.newHistory(ShowRepresentationInformation.RESOLVER, ri.getId());
    }
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    Services services = new Services("Delete representation information", "delete");
    services
      .representationInformationResource(s -> s
        .deleteMultipleRepresentationInformation(new SelectedItemsListRequest(Collections.singletonList(ri.getUUID()))))
      .whenComplete((job, throwable) -> {
        if (throwable == null) {
          Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(), new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
              Timer timer = new Timer() {
                @Override
                public void run() {
                  HistoryUtils.newHistory(RepresentationInformationNetwork.RESOLVER);
                }
              };

              timer.schedule(RodaConstants.ACTION_TIMEOUT);
            }

            @Override
            public void onSuccess(final Void nothing) {
              HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
            }
          });
        } else {
          HistoryUtils.newHistory(InternalProcess.RESOLVER);
        }
      });
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(ShowRepresentationInformation.RESOLVER, ri.getId());
  }

  private void errorMessage(Throwable caught) {
    if (caught instanceof NotFoundException) {
      Toast.showError(messages.editRepresentationInformationNotFound(ri.getName()));
      cancel();
    } else {
      AsyncCallbackUtils.defaultFailureTreatment(caught);
    }
  }
}
