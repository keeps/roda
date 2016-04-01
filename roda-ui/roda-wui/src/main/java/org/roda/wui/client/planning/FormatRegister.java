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

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.common.RodaConstants;
import org.roda.wui.client.common.BasicSearch;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.FormatList;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria
 *
 */
public class FormatRegister extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {FormatRegister.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(Planning.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "formatregister";
    }
  };

  private static FormatRegister instance = null;

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static FormatRegister getInstance() {
    if (instance == null) {
      instance = new FormatRegister();
    }
    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, FormatRegister> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  @UiField
  FlowPanel formatRegisterDescription;

  @UiField(provided = true)
  BasicSearch basicSearch;

  @UiField(provided = true)
  FormatList formatList;

  @UiField
  Button buttonAdd;

  @UiField
  Button buttonEdit;

  @UiField
  Button buttonRemove;

  @UiField
  Button startProcess;

  private static final Filter DEFAULT_FILTER = new Filter(
    new BasicSearchFilterParameter(RodaConstants.FORMAT_SEARCH, "*"));

  /**
   * Create a format register page
   *
   * @param user
   */
  public FormatRegister() {
    Filter filter = null;
    Facets facets = null;

    formatList = new FormatList(filter, facets, "Formats", true);

    basicSearch = new BasicSearch(DEFAULT_FILTER, RodaConstants.FORMAT_SEARCH,
      messages.formatRegisterSearchPlaceHolder(), false, false);
    basicSearch.setList(formatList);

    formatList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {

      }
    });

    initWidget(uiBinder.createAndBindUi(this));

    formatRegisterDescription.add(new HTMLWidgetWrapper("FormatRegisterDescription.html"));

    buttonEdit.setEnabled(false);
    buttonRemove.setEnabled(false);
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      formatList.refresh();
      callback.onSuccess(this);
    } else {
      Tools.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

  protected void updateVisibles() {
    // TODO selection control
    buttonEdit.setEnabled(true);
    buttonRemove.setEnabled(true);
  }
}
