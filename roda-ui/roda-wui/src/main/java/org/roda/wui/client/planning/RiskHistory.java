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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.risks.Risk;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.bundle.BinaryVersionBundle;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
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
        final String riskId = historyTokens.get(0);

        BrowserService.Util.getInstance().retrieveRiskVersions(riskId, new AsyncCallback<RiskVersionsBundle>() {

          @Override
          public void onFailure(Throwable caught) {
            AsyncCallbackUtils.defaultFailureTreatment(caught);
          }

          @Override
          public void onSuccess(RiskVersionsBundle bundle) {
            RiskHistory widget = new RiskHistory(riskId, bundle);
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

  interface MyUiBinder extends UiBinder<Widget, RiskHistory> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final String riskId;
  private RiskVersionsBundle bundle;
  private String selectedVersion = null;

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

  /**
   * Create a new panel to edit a user
   * 
   * @param user
   *          the user to edit
   */
  public RiskHistory(final String riskId, final RiskVersionsBundle bundle) {
    this.riskId = riskId;
    this.bundle = bundle;

    oldRisk = new RiskShowPanel(bundle.getLastRisk(), "RiskHistory_riskIncidences");

    initWidget(uiBinder.createAndBindUi(this));
    init();

    list.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        String versionKey = list.getSelectedValue();
        selectedVersion = versionKey;

        BrowserService.Util.getInstance().retrieveRiskVersion(riskId, selectedVersion, new AsyncCallback<Risk>() {

          @Override
          public void onFailure(Throwable caught) {
            AsyncCallbackUtils.defaultFailureTreatment(caught);
          }

          @Override
          public void onSuccess(Risk oldRisk) {
            RiskHistory.this.oldRisk.clear();
            RiskHistory.this.oldRisk.init(oldRisk);
          }
        });
      }
    });

  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  private void init() {
    // sort
    List<BinaryVersionBundle> versionList = new ArrayList<>(bundle.getVersions());
    Collections.sort(versionList, new Comparator<BinaryVersionBundle>() {

      @Override
      public int compare(BinaryVersionBundle v1, BinaryVersionBundle v2) {
        return (int) (v2.getCreatedDate().getTime() - v1.getCreatedDate().getTime());
      }
    });

    // create list layout
    for (BinaryVersionBundle version : versionList) {
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
    BrowserService.Util.getInstance().revertRiskVersion(riskId, selectedVersion, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(Void result) {
        Toast.showInfo(messages.dialogDone(), messages.versionReverted());
        HistoryUtils.newHistory(ShowRisk.RESOLVER, riskId);
      }
    });
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    BrowserService.Util.getInstance().deleteRiskVersion(riskId, selectedVersion, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(Void result) {
        Toast.showInfo(messages.dialogDone(), messages.versionDeleted());
        refresh();
        if (bundle.getVersions().isEmpty()) {
          HistoryUtils.newHistory(ShowRisk.RESOLVER, riskId);
        }
      }
    });
  }

  protected void refresh() {
    BrowserService.Util.getInstance().retrieveRiskVersions(riskId, new AsyncCallback<RiskVersionsBundle>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(RiskVersionsBundle bundle) {
        RiskHistory.this.bundle = bundle;
        clean();
        init();

        RiskHistory.this.oldRisk.clear();
        RiskHistory.this.oldRisk.init(bundle.getLastRisk());
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

}
