package org.roda.wui.client.disposal.rule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedules;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.filter.AllFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.wui.client.common.CreateOrUpdateDisposalRuleActionsToolbar;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.services.DisposalScheduleRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ListUtils;

import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class CreateDisposalRule extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      Services services = new Services("List disposal schedules", "get");
      CountRequest request = new CountRequest();
      Filter filter = new Filter();

      filter.add(new AllFilterParameter());
      request.setFilter(filter);
      services.disposalScheduleResource(DisposalScheduleRestService::listDisposalSchedules)
        .thenCompose(disposalSchedules -> services.disposalRuleResource(s -> s.count(request))
          .whenComplete((disposalRulesCount, throwable) -> {
            if (throwable != null) {
              AsyncCallbackUtils.defaultFailureTreatment(throwable);
            } else {
              CreateDisposalRule createDisposalRule = new CreateDisposalRule(new DisposalRule(),
                disposalSchedules, disposalRulesCount);
              callback.onSuccess(createDisposalRule);
            }
          }));
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {DisposalPolicy.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(DisposalPolicy.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "create_disposal_rule";
    }
  };

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FocusPanel keyboardFocus;
  @UiField
  NavigationToolbar<DisposalRule> navigationToolbar;
  @UiField
  CreateOrUpdateDisposalRuleActionsToolbar actionsToolbar;
  @UiField
  TitlePanel title;
  @UiField
  FlowPanel disposalRuleDataPanel;

  public CreateDisposalRule(DisposalRule disposalRule, DisposalSchedules disposalSchedules,
                            LongResponse disposalRulesCount) {

    initWidget(uiBinder.createAndBindUi(this));
    disposalRule.setOrder(disposalRulesCount.getResult().intValue());

    // 1. Create the panel and keep a reference
    DisposalRuleDataPanel dataPanel = new DisposalRuleDataPanel(disposalRule, disposalSchedules, false);
    disposalRuleDataPanel.add(dataPanel);

    navigationToolbar.withoutButtons().build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getCreateDisposalRuleBreadcrumbs());

    actionsToolbar.setLabel(messages.showDisposalRuleTitle());

    // 2. Give the toolbar the panel so it can check validity
    actionsToolbar.setDataPanel(dataPanel);
    actionsToolbar.setIsCreate(true);

    // 3. Pass the shared object
    actionsToolbar.setObjectAndBuild(disposalRule, null, new AsyncCallback<Actionable.ActionImpact>() {
      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(Actionable.ActionImpact result) {
        // Redirect user or show success message if result == ActionImpact.UPDATED
      }
    });

    title.setText(messages.newDisposalRuleTitle());
    title.setIconClass("DisposalRule");

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");
  }

  interface MyUiBinder extends UiBinder<Widget, CreateDisposalRule> {
  }
}
