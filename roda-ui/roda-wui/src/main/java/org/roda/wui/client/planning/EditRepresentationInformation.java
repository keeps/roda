/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.planning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
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
        String formatId = historyTokens.get(0);
        BrowserService.Util.getInstance().retrieve(RepresentationInformation.class.getName(), formatId, fieldsToReturn,
          new AsyncCallback<RepresentationInformation>() {

            @Override
            public void onFailure(Throwable caught) {
              callback.onFailure(caught);
            }

            @Override
            public void onSuccess(RepresentationInformation ri) {
              EditRepresentationInformation editRepresentationInformation = new EditRepresentationInformation(ri);
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

  private static final List<String> fieldsToReturn = new ArrayList<>();

  @UiField
  Button buttonApply;

  @UiField
  Button buttonRemove;

  @UiField
  Button buttonCancel;

  @UiField(provided = true)
  RepresentationInformationDataPanel representationInformationDataPanel;

  /**
   * Create a new panel to create a user
   *
   * @param user
   *          the user to create
   */
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
      BrowserService.Util.getInstance().updateRepresentationInformation(ri,
        this.representationInformationDataPanel.getExtras(), new AsyncCallback<Void>() {

          @Override
          public void onFailure(Throwable caught) {
            errorMessage(caught);
          }

          @Override
          public void onSuccess(Void result) {
            HistoryUtils.newHistory(ShowRepresentationInformation.RESOLVER, ri.getId());
          }

        });
    } else {
      HistoryUtils.newHistory(ShowRepresentationInformation.RESOLVER, ri.getId());
    }
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    BrowserService.Util.getInstance().deleteRepresentationInformation(
      new SelectedItemsList<>(Arrays.asList(ri.getUUID()), RepresentationInformation.class.getName()),
      new AsyncCallback<Job>() {
        @Override
        public void onFailure(Throwable caught) {
          HistoryUtils.newHistory(InternalProcess.RESOLVER);
        }

        @Override
        public void onSuccess(Job result) {
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
              HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
            }
          });
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
