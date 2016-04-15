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

import org.roda.wui.client.browse.BinaryVersionBundle;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.RiskVersionsBundle;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.RiskMessages;

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
        Tools.newHistory(RiskRegister.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(RiskRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "risk_history";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, RiskHistory> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  // private ClientLogger logger = new ClientLogger(getClass().getName());
  private static final RiskMessages messages = GWT.create(RiskMessages.class);

  private final String riskId;
  private RiskVersionsBundle bundle;
  private String selectedVersion = null;

  @UiField
  ListBox list;

  @UiField
  Label riskLabel;

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

    initWidget(uiBinder.createAndBindUi(this));

    Toast.showInfo("Risk History", "This feature is not fully implemented. Risk version content is missing.");
    init();

    list.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        String versionKey = list.getSelectedValue();
        selectedVersion = versionKey;
      }
    });

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
      String message = version.getMessage();
      Date createdDate = version.getCreatedDate();

      list.addItem(messages.riskHistoryLabel(message, createdDate), versionKey);
    }

    riskLabel.setText(bundle.getRisk().getName());

    if (versionList.size() > 0) {
      list.setSelectedIndex(0);
      selectedVersion = versionList.get(0).getId();
    }
  }

  @UiHandler("buttonRevert")
  void buttonRevertHandler(ClickEvent e) {
    BrowserService.Util.getInstance().revertRiskVersion(riskId, selectedVersion, messages.modifyRiskMessage(),
      new AsyncCallback<Void>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(Void result) {
          Toast.showInfo("Done", "Version reverted");
          Tools.newHistory(ShowRisk.RESOLVER, riskId);
        }
      });
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    BrowserService.Util.getInstance().removeRiskVersion(riskId, selectedVersion, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(Void result) {
        Toast.showInfo("Done", "Version deleted");
        refresh();
        if (bundle.getVersions().isEmpty()) {
          Tools.newHistory(ShowRisk.RESOLVER, riskId);
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
      }
    });
  }

  protected void clean() {
    list.clear();
    riskLabel.setText("");
    selectedVersion = null;
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    Tools.newHistory(ShowRisk.RESOLVER, riskId);
  }

}
