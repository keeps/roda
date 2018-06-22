/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management;

import java.util.List;

import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.RodaMemberList;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.ClientMessages;

public class MemberManagement extends Composite {
  private static final String EDIT_GROUP_HISTORY_TOKEN = "edit_group";
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
  SearchPanel searchPanel;

  @UiField(provided = true)
  RodaMemberList list;

  @UiField
  Button buttonAddUser;

  @UiField
  Button buttonAddGroup;

  @UiField(provided = true)
  FlowPanel facetsPanel;

  private static final Filter DEFAULT_FILTER = SearchFilters.defaultFilter(RODAMember.class.getName());
  private static final String ALL_FILTER = SearchFilters.allFilter(RODAMember.class.getName());

  public MemberManagement() {
    list = new RodaMemberList("MemberManagement_rodaMembers", DEFAULT_FILTER, messages.usersAndGroupsTitle(), false);

    searchPanel = new SearchPanel(DEFAULT_FILTER, ALL_FILTER, true, messages.usersAndGroupsSearchPlaceHolder(), false,
      false, false);
    searchPanel.setList(list);

    facetsPanel = new FlowPanel();
    FacetUtils.bindFacets(list, facetsPanel);

    initWidget(uiBinder.createAndBindUi(this));
    memberManagementDescription.add(new HTMLWidgetWrapper("MemberManagementDescription.html"));

    list.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        RODAMember selected = list.getSelectionModel().getSelectedObject();
        if (selected != null) {
          HistoryUtils.newHistory(RESOLVER,
            (selected.isUser() ? EditUser.RESOLVER.getHistoryToken() : EDIT_GROUP_HISTORY_TOKEN), selected.getId());
        }
      }
    });
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  private void refresh() {
    list.refresh();
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
      if (historyTokens.get(0).equals(EditUser.RESOLVER.getHistoryToken())) {
        EditUser.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(EDIT_GROUP_HISTORY_TOKEN)) {
        EditGroup.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else {
        HistoryUtils.newHistory(RESOLVER);
        callback.onSuccess(null);
      }
    } else {
      HistoryUtils.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

  @UiHandler("buttonAddUser")
  void buttonAddUserHandler(ClickEvent e) {
    HistoryUtils.newHistory(RESOLVER, "create_user");
  }

  @UiHandler("buttonAddGroup")
  void buttonAddGroupHandler(ClickEvent e) {
    HistoryUtils.newHistory(RESOLVER, "create_group");
  }
}
