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

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.RiskActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class ShowRisk extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
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
      return "risk";
    }
  };

  private static ShowRisk instance = null;
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, ShowRisk> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.RISK_ID,
    RodaConstants.RISK_NAME, RodaConstants.RISK_DESCRIPTION, RodaConstants.RISK_IDENTIFIED_ON,
    RodaConstants.RISK_IDENTIFIED_BY, RodaConstants.RISK_CATEGORIES, RodaConstants.RISK_NOTES,
    RodaConstants.RISK_PRE_MITIGATION_PROBABILITY, RodaConstants.RISK_PRE_MITIGATION_IMPACT,
    RodaConstants.RISK_PRE_MITIGATION_SEVERITY, RodaConstants.RISK_POST_MITIGATION_PROBABILITY,
    RodaConstants.RISK_POST_MITIGATION_IMPACT, RodaConstants.RISK_POST_MITIGATION_SEVERITY,
    RodaConstants.RISK_PRE_MITIGATION_NOTES, RodaConstants.RISK_POST_MITIGATION_NOTES,
    RodaConstants.RISK_MITIGATION_STRATEGY, RodaConstants.RISK_MITIGATION_OWNER,
    RodaConstants.RISK_MITIGATION_OWNER_TYPE, RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_TYPE,
    RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_VALUE);

  private static final AsyncCallback<Actionable.ActionImpact> actionCallback = new NoAsyncCallback<Actionable.ActionImpact>() {
    @Override
    public void onSuccess(Actionable.ActionImpact result) {
      if (result.equals(Actionable.ActionImpact.DESTROYED)) {
        HistoryUtils.newHistory(RiskRegister.RESOLVER);
      }
    }
  };

  @UiField(provided = true)
  RiskShowPanel riskShowPanel;

  @UiField
  SimplePanel actionsSidebar;

  /**
   * Create a new panel to view a risk
   *
   *
   */
  public ShowRisk() {
    this.riskShowPanel = new RiskShowPanel("RiskShowPanel_riskIncidences");
    initWidget(uiBinder.createAndBindUi(this));
  }

  public ShowRisk(IndexedRisk risk) {
    this.riskShowPanel = new RiskShowPanel(risk, "RiskShowPanel_riskIncidences", true);
    initWidget(uiBinder.createAndBindUi(this));
    // actionsSidebar is set in the resolve callback
  }

  public static ShowRisk getInstance() {
    if (instance == null) {
      instance = new ShowRisk();
    }
    return instance;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {
      String riskId = historyTokens.get(0);
      BrowserService.Util.getInstance().retrieve(IndexedRisk.class.getName(), riskId, fieldsToReturn,
        new AsyncCallback<IndexedRisk>() {

          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          @Override
          public void onSuccess(IndexedRisk result) {
            instance = new ShowRisk(result);

            BrowserService.Util.getInstance().hasRiskVersions(result.getId(), new AsyncCallback<Boolean>() {

              @Override
              public void onFailure(Throwable caught) {
                instance.actionsSidebar.setWidget(new ActionableWidgetBuilder<>(RiskActions.get())
                  .withCallback(actionCallback).buildListWithObjects(new ActionableObject<>(result)));
                callback.onSuccess(instance);
              }

              @Override
              public void onSuccess(Boolean hasHistory) {
                if (hasHistory) {
                  instance.actionsSidebar.setWidget(new ActionableWidgetBuilder<>(RiskActions.getWithHistory())
                    .withCallback(actionCallback).buildListWithObjects(new ActionableObject<>(result)));
                } else {
                  instance.actionsSidebar.setWidget(new ActionableWidgetBuilder<>(RiskActions.get())
                    .withCallback(actionCallback).buildListWithObjects(new ActionableObject<>(result)));
                }

                callback.onSuccess(instance);
              }
            });
          }
        });
    } else {
      HistoryUtils.newHistory(RiskRegister.RESOLVER);
      callback.onSuccess(null);
    }
  }
}
