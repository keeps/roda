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

import java.util.Date;
import java.util.List;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.DateRangeFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.BasicSearch;
import org.roda.wui.client.common.Dialogs;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.AgentList;
import org.roda.wui.client.common.lists.AsyncTableCell.CheckboxSelectionListener;
import org.roda.wui.client.common.lists.SelectedItemsUtils;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.AgentMessages;
import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria
 *
 */
public class AgentRegister extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {AgentRegister.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(Planning.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "agentregister";
    }
  };

  private static AgentRegister instance = null;

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static AgentRegister getInstance() {
    if (instance == null) {
      instance = new AgentRegister();
    }
    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, AgentRegister> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static final BrowseMessages defaultMessages = GWT.create(BrowseMessages.class);
  private static final AgentMessages messages = GWT.create(AgentMessages.class);

  @UiField
  FlowPanel agentRegisterDescription;

  @UiField(provided = true)
  BasicSearch basicSearch;

  @UiField(provided = true)
  AgentList agentList;

  @UiField
  DateBox inputDateInitial;

  @UiField
  DateBox inputDateFinal;

  @UiField
  Button buttonAdd;

  @UiField
  Button buttonRemove;

  @UiField
  Button startProcess;

  private static final Filter DEFAULT_FILTER = new Filter(new BasicSearchFilterParameter(RodaConstants.AGENT_SEARCH,
    "*"));

  /**
   * Create a agent register page
   *
   * @param user
   */
  public AgentRegister() {
    Filter filter = null;
    Facets facets = null;

    agentList = new AgentList(filter, facets, "Agents", true);

    basicSearch = new BasicSearch(DEFAULT_FILTER, RodaConstants.AGENT_SEARCH,
      messages.agentRegisterSearchPlaceHolder(), false, false);
    basicSearch.setList(agentList);

    agentList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        Agent selected = agentList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          Tools.newHistory(RESOLVER, ShowAgent.RESOLVER.getHistoryToken(), selected.getId());
        }
      }
    });

    agentList.addCheckboxSelectionListener(new CheckboxSelectionListener() {

      @Override
      public void onSelectionChange(SelectedItems selected) {
        boolean empty = SelectedItemsUtils.isEmpty(selected);
        if (empty) {
          buttonRemove.setEnabled(false);
        } else {
          buttonRemove.setEnabled(true);
        }
      }

    });

    initWidget(uiBinder.createAndBindUi(this));

    agentRegisterDescription.add(new HTMLWidgetWrapper("AgentRegisterDescription.html"));

    buttonRemove.setEnabled(false);

    DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
    ValueChangeHandler<Date> valueChangeHandler = new ValueChangeHandler<Date>() {

      @Override
      public void onValueChange(ValueChangeEvent<Date> event) {
        updateDateFilter();
      }

    };

    inputDateInitial.setFormat(dateFormat);
    inputDateInitial.getDatePicker().setYearArrowsVisible(true);
    inputDateInitial.setFireNullValues(true);
    inputDateInitial.addValueChangeHandler(valueChangeHandler);

    inputDateFinal.setFormat(dateFormat);
    inputDateFinal.getDatePicker().setYearArrowsVisible(true);
    inputDateFinal.setFireNullValues(true);
    inputDateFinal.addValueChangeHandler(valueChangeHandler);

    inputDateInitial.getElement().setPropertyString("placeholder", defaultMessages.sidebarFilterFromDatePlaceHolder());
    inputDateFinal.getElement().setPropertyString("placeholder", defaultMessages.sidebarFilterToDatePlaceHolder());
  }

  private void updateDateFilter() {
    Date dateInitial = inputDateInitial.getDatePicker().getValue();
    Date dateFinal = inputDateFinal.getDatePicker().getValue();

    DateRangeFilterParameter filterParameter = new DateRangeFilterParameter(RodaConstants.AGENT_INITIAL_RELEASE,
      dateInitial, dateFinal, RodaConstants.DateGranularity.DAY);

    agentList.setFilter(new Filter(filterParameter));
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      agentList.refresh();
      callback.onSuccess(this);
    } else if (historyTokens.size() == 2 && historyTokens.get(0).equals(ShowAgent.RESOLVER.getHistoryToken())) {
      ShowAgent.RESOLVER.resolve(Tools.tail(historyTokens), callback);
    } else if (historyTokens.size() == 1 && historyTokens.get(0).equals(CreateAgent.RESOLVER.getHistoryToken())) {
      CreateAgent.RESOLVER.resolve(Tools.tail(historyTokens), callback);
    } else if (historyTokens.size() == 2 && historyTokens.get(0).equals(EditAgent.RESOLVER.getHistoryToken())) {
      EditAgent.RESOLVER.resolve(Tools.tail(historyTokens), callback);
    } else {
      Tools.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

  @UiHandler("buttonAdd")
  void buttonAddAgentHandler(ClickEvent e) {
    Tools.newHistory(RESOLVER, CreateAgent.RESOLVER.getHistoryToken());
  }

  @UiHandler("buttonRemove")
  void buttonRemoveAgentHandler(ClickEvent e) {

    final SelectedItems selected = agentList.getSelected();

    SelectedItemsUtils.size(TransferredResource.class, selected, new AsyncCallback<Long>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.agentRemoveFolderConfirmDialogTitle(),
          messages.agentRemoveSelectedConfirmDialogMessage(size), messages.agentRemoveFolderConfirmDialogCancel(),
          messages.agentRemoveFolderConfirmDialogOk(), new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable caught) {
              AsyncCallbackUtils.defaultFailureTreatment(caught);
            }

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                BrowserService.Util.getInstance().removeAgent(selected, new AsyncCallback<Void>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    AsyncCallbackUtils.defaultFailureTreatment(caught);
                    agentList.refresh();
                  }

                  @Override
                  public void onSuccess(Void result) {
                    Toast.showInfo(messages.agentRemoveSuccessTitle(), messages.agentRemoveSuccessMessage(size));
                    agentList.refresh();
                  }
                });
              }
            }
          });
      }
    });
  }
}
