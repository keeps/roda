/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import java.util.List;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.common.BasicSearch;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.SimpleRodaMemberList;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

public class EditPermissions extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        EditPermissions edit = new EditPermissions();
        callback.onSuccess(edit);
      } else {
        Tools.newHistory(Browse.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {EditPermissions.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(Browse.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "edit_permissions";
    }
  };

  private static EditPermissions instance = null;

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static EditPermissions getInstance() {
    if (instance == null) {
      instance = new EditPermissions();
    }
    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, EditPermissions> {
  }
  
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  
  @UiField(provided = true)
  BasicSearch basicSearch;
  
  @UiField(provided = true)
  SimpleRodaMemberList list;

  public EditPermissions() {
    Filter filter = null;
    list = new SimpleRodaMemberList(filter, null, "Users and groups", false);
    
    basicSearch = new BasicSearch(null, null, "", false, false);
    basicSearch.setList(list);

    initWidget(uiBinder.createAndBindUi(this));

    list.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        RODAMember selected = list.getSelectionModel().getSelectedObject();
        if (selected != null) {
          
        }
      }
    });
  }
}
