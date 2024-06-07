/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 *
 */
package org.roda.wui.client.planning;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.risks.RiskVersion;
import org.roda.core.data.v2.risks.RiskVersions;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class RiskHistory extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {
    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        Services services = new Services("Retrieve risk versioning", "get");
        services.riskResource(s -> s.retrieveRiskVersions(historyTokens.get(0))).whenComplete((result, throwable) -> {
          if (throwable != null) {
            AsyncCallbackUtils.defaultFailureTreatment(throwable);
          } else {
            RiskHistory widget = new RiskHistory(historyTokens.get(0), result);
            callback.onSuccess(widget);
          }
        });
      } else {
        HistoryUtils.newHistory(RiskRegister.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(RiskRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "risk_history";
    }
  };

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private final String riskId;
  @UiField
  ListBox list;

  @UiField(provided = true)
  RiskShowPanel oldRisk;
  @UiField
  Button buttonRevert;
  @UiField
  Button buttonRemove;
  @UiField
  Button buttonCancel;

  private RiskVersions riskVersions;
  private String selectedVersion = null;

  public RiskHistory(final String riskId, final RiskVersions versions) {
    this.riskId = riskId;
    this.riskVersions = versions;

    oldRisk = new RiskShowPanel("RiskHistory_riskIncidences");

    initWidget(uiBinder.createAndBindUi(this));
    init();

    list.addChangeHandler(event -> {
      selectedVersion = list.getSelectedValue();

      Services services = new Services("Retrieve risk version", "get");
      services.riskResource(s -> s.retrieveRiskVersion(riskId, selectedVersion)).whenComplete((result, throwable) -> {
        if (throwable != null) {
          AsyncCallbackUtils.defaultFailureTreatment(throwable);
        } else {
          RiskHistory.this.oldRisk.clear();
          RiskHistory.this.oldRisk.init(result);
        }
      });
    });

    // Set the selected index
    list.setSelectedIndex(0);
    // Manually trigger a ValueChangeEvent
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), list);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  private void init() {
    // sort
    List<RiskVersion> versionList = new ArrayList<>(riskVersions.getVersions());
    versionList.sort((v1, v2) -> (int) (v2.getCreatedDate().getTime() - v1.getCreatedDate().getTime()));

    // create list layout
    for (RiskVersion version : versionList) {
      String versionKey = version.getId();
      String message = messages.versionAction(version.getProperties().get(RodaConstants.VERSION_ACTION));
      Date createdDate = version.getCreatedDate();

      list.addItem(messages.riskHistoryLabel(message, createdDate), versionKey);
    }

    if (!versionList.isEmpty()) {
      list.setSelectedIndex(0);
      selectedVersion = versionList.get(0).getId();
    }
  }

  @UiHandler("buttonRevert")
  void buttonRevertHandler(ClickEvent e) {
    Services services = new Services("Revert risk", "revert");
    services.riskResource(s -> s.revertRiskVersion(riskId, selectedVersion)).whenComplete((risk, throwable) -> {
      if (throwable != null) {
        AsyncCallbackUtils.defaultFailureTreatment(throwable);
      } else {
        Toast.showInfo(messages.dialogDone(), messages.versionReverted());
        HistoryUtils.newHistory(ShowRisk.RESOLVER, riskId);
      }

    });
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    Services services = new Services("Delete risk version", "delete");

    services.riskResource(s -> s.deleteRiskVersion(riskId, selectedVersion))
      .thenCompose(unused -> services.riskResource(s -> s.retrieveRiskVersions(riskId)))
      .whenComplete((versions, throwable) -> {
        if (throwable != null) {
          AsyncCallbackUtils.defaultFailureTreatment(throwable);
        } else {
          Toast.showInfo(messages.dialogDone(), messages.versionDeleted());
          if (versions.getVersions().isEmpty()) {
            HistoryUtils.newHistory(ShowRisk.RESOLVER, riskId);
          } else {
            this.riskVersions = versions;
            clean();
            init();
          }
        }
      });
  }

  protected void clean() {
    list.clear();
    selectedVersion = null;
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(ShowRisk.RESOLVER, riskId);
  }

  interface MyUiBinder extends UiBinder<Widget, RiskHistory> {
  }
}
