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

import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.ActionableObject;
import org.roda.wui.client.common.actions.ActionableWidgetBuilder;
import org.roda.wui.client.common.actions.FormatActions;
import org.roda.wui.client.common.lists.FormatList;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.JavascriptUtils;
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
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.ClientMessages;

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
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Planning.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "formatregister";
    }
  };

  private static FormatRegister instance = null;

  interface MyUiBinder extends UiBinder<Widget, FormatRegister> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel title;

  @UiField
  FlowPanel formatRegisterDescription;

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField(provided = true)
  FormatList formatList;

  @UiField
  SimplePanel actionsSidebar;
  private ActionableWidgetBuilder<Format> actionableWidgetBuilder;

  private static final Filter DEFAULT_FILTER = SearchFilters.defaultFilter(Format.class.getName());
  private static final String ALL_FILTER = SearchFilters.allFilter(Format.class.getName());

  /**
   * Create a format register page
   */
  public FormatRegister() {
    actionableWidgetBuilder = new ActionableWidgetBuilder<>(FormatActions.get());
    formatList = new FormatList("FormatRegister_formats", Filter.ALL, messages.formatsTitle(), true);
    formatList.setActionable(FormatActions.get());

    searchPanel = new SearchPanel(DEFAULT_FILTER, ALL_FILTER, true, messages.formatRegisterSearchPlaceHolder(), false,
      false, true);
    searchPanel.setList(formatList);

    formatList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        Format selected = formatList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          HistoryUtils.newHistory(ShowFormat.RESOLVER, selected.getId());
        }
      }
    });

    initWidget(uiBinder.createAndBindUi(this));
    actionsSidebar.setWidget(actionableWidgetBuilder.buildListWithObjects(new ActionableObject<>(Format.class)));

    Label titleLabel = new Label(messages.formatRegisterTitle());
    titleLabel.addStyleName("h1 browseItemText");
    title.add(titleLabel);

    InlineHTML badge = new InlineHTML("<span class='label-warning browseRepresentationOriginalIcon'>Deprecated</span>");
    title.add(badge);

    formatRegisterDescription.add(new HTMLWidgetWrapper("FormatRegisterDescription.html"));
  }

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

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      formatList.refresh();
      formatList.setFilter(Filter.ALL);
      callback.onSuccess(this);
    } else if (historyTokens.size() == 2 && historyTokens.get(0).equals(ShowFormat.RESOLVER.getHistoryToken())) {
      ShowFormat.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() == 1 && historyTokens.get(0).equals(CreateFormat.RESOLVER.getHistoryToken())) {
      CreateFormat.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() == 2 && historyTokens.get(0).equals(EditFormat.RESOLVER.getHistoryToken())) {
      EditFormat.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else {
      HistoryUtils.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }
}
