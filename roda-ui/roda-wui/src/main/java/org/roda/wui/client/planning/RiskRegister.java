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

import java.util.List;

import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.RiskActions;
import org.roda.wui.client.common.actions.RiskIncidenceActions;
import org.roda.wui.client.common.lists.RiskIncidenceList;
import org.roda.wui.client.common.lists.RiskList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.process.CreateActionJob;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class RiskRegister extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Planning.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "riskregister";
    }
  };

  private static RiskRegister instance = null;

  interface MyUiBinder extends UiBinder<Widget, RiskRegister> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel riskRegisterDescription;

  @UiField(provided = true)
  SearchWrapper riskSearch;

  @UiField
  FlowPanel contentFlowPanel;

  /**
   * Create a risk register page
   */
  public RiskRegister() {

    ListBuilder<IndexedRisk> riskListBuilder = new ListBuilder<>(() -> new RiskList(),
      new AsyncTableCellOptions<>(IndexedRisk.class, "RiskRegister_risks").withActionable(RiskActions.get())
        .withActionableCallback(new NoAsyncCallback<Actionable.ActionImpact>() {
          @Override
          public void onSuccess(Actionable.ActionImpact result) {
            if (Actionable.ActionImpact.DESTROYED.equals(result) || Actionable.ActionImpact.UPDATED.equals(result)) {
              riskSearch.refreshAllLists();
            }
          }
        }).bindOpener());

    ListBuilder<RiskIncidence> riskIncidenceListBuilder = new ListBuilder<>(() -> new RiskIncidenceList(),
      new AsyncTableCellOptions<>(RiskIncidence.class, "RiskRegister_riskIncidences")
        .withActionable(RiskIncidenceActions.getForMultipleEdit()).bindOpener());

    riskSearch = new SearchWrapper(true).createListAndSearchPanel(riskListBuilder)
      .createListAndSearchPanel(riskIncidenceListBuilder);

    initWidget(uiBinder.createAndBindUi(this));
    riskRegisterDescription.add(new HTMLWidgetWrapper("RiskRegisterDescription.html"));
  }

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static RiskRegister getInstance() {
    if (instance == null) {
      instance = new RiskRegister();
    }
    return instance;
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      callback.onSuccess(this);
    } else if (historyTokens.size() == 2 && historyTokens.get(0).equals(ShowRisk.RESOLVER.getHistoryToken())) {
      ShowRisk.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() == 1 && historyTokens.get(0).equals(CreateRisk.RESOLVER.getHistoryToken())) {
      CreateRisk.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() == 2 && historyTokens.get(0).equals(EditRisk.RESOLVER.getHistoryToken())) {
      EditRisk.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() == 2 && historyTokens.get(0).equals(RiskHistory.RESOLVER.getHistoryToken())) {
      RiskHistory.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() == 1 && historyTokens.get(0).equals(CreateActionJob.RESOLVER.getHistoryToken())) {
      CreateActionJob.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else {
      HistoryUtils.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }
}
