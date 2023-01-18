/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management;

import java.util.List;

import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.RODAMemberActions;
import org.roda.wui.client.common.lists.RodaMemberList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.management.access.CreateAccessKey;
import org.roda.wui.client.management.access.EditAccessKey;
import org.roda.wui.client.management.access.ShowAccessKey;
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

public class MemberManagement extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

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
      return ListUtils.concat(Management.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "user";
    }
  };

  private static MemberManagement instance = null;

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static MemberManagement getInstance() {
    if (instance == null) {
      instance = new MemberManagement();
    } else {
      instance.refresh();
    }
    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, MemberManagement> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FlowPanel memberManagementDescription;

  @UiField(provided = true)
  SearchWrapper searchWrapper;

  public MemberManagement() {

    ListBuilder<RODAMember> rodaMemberListBuilder = new ListBuilder<>(() -> new RodaMemberList(),
      new AsyncTableCellOptions<>(RODAMember.class, "MemberManagement_rodaMembers")
        .withSummary(messages.usersAndGroupsTitle()).bindOpener()
        .withSearchPlaceholder(messages.usersAndGroupsSearchPlaceHolder()).withActionable(RODAMemberActions.get()));

    searchWrapper = new SearchWrapper(false).createListAndSearchPanel(rodaMemberListBuilder);

    initWidget(uiBinder.createAndBindUi(this));

    memberManagementDescription.add(new HTMLWidgetWrapper("MemberManagementDescription.html"));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  private void refresh() {
    searchWrapper.refreshCurrentList();
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      callback.onSuccess(this);
    } else if (historyTokens.size() == 1) {
      if (historyTokens.get(0).equals(CreateUser.RESOLVER.getHistoryToken())) {
        CreateUser.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(CreateGroup.RESOLVER.getHistoryToken())) {
        CreateGroup.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else {
        HistoryUtils.newHistory(RESOLVER);
        callback.onSuccess(null);
      }
    } else if (historyTokens.size() == 2) {
      if (historyTokens.get(0).equals(ShowUser.RESOLVER.getHistoryToken())) {
        ShowUser.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(EditUser.RESOLVER.getHistoryToken())) {
        EditUser.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(ShowGroup.RESOLVER.getHistoryToken())) {
        ShowGroup.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(EditGroup.RESOLVER.getHistoryToken())) {
        EditGroup.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(ShowAccessKey.RESOLVER.getHistoryToken())) {
        ShowAccessKey.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(EditAccessKey.RESOLVER.getHistoryToken())) {
        EditAccessKey.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(CreateAccessKey.RESOLVER.getHistoryToken())) {
        CreateAccessKey.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else {
        HistoryUtils.newHistory(RESOLVER);
        callback.onSuccess(null);
      }
    } else {
      HistoryUtils.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }
}
