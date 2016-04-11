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
package org.roda.wui.client.management;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.facet.SimpleFacetParameter;
import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.DateRangeFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.messages.Message;
import org.roda.wui.client.common.BasicSearch;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.NotificationList;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.management.client.Management;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria
 *
 */
public class NotificationMessages extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {NotificationMessages.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(Management.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "notifications";
    }
  };

  private static NotificationMessages instance = null;

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static NotificationMessages getInstance() {
    if (instance == null) {
      instance = new NotificationMessages();
    }
    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, NotificationMessages> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  @UiField
  FlowPanel notificationDescription;

  @UiField(provided = true)
  BasicSearch basicSearch;

  @UiField
  DateBox inputDateInitial;

  @UiField
  DateBox inputDateFinal;

  @UiField(provided = true)
  NotificationList notificationList;

  @UiField(provided = true)
  FlowPanel facetRecipientUser;

  @UiField(provided = true)
  FlowPanel facetAcknowledged;

  private static final Filter DEFAULT_FILTER = new Filter(
    new BasicSearchFilterParameter(RodaConstants.MESSAGE_SEARCH, "*"));

  /**
   * Create a new notification
   *
   * @param user
   */
  public NotificationMessages() {
    Filter filter = null;
    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.MESSAGE_RECIPIENT_USER),
      new SimpleFacetParameter(RodaConstants.MESSAGE_IS_ACKNOWLEDGED));
    notificationList = new NotificationList(filter, facets, "Notifications", false);

    basicSearch = new BasicSearch(DEFAULT_FILTER, RodaConstants.MESSAGE_SEARCH, messages.messageSearchPlaceHolder(),
      false, false);
    basicSearch.setList(notificationList);

    facetRecipientUser = new FlowPanel();
    facetAcknowledged = new FlowPanel();

    Map<String, FlowPanel> facetPanels = new HashMap<String, FlowPanel>();
    facetPanels.put(RodaConstants.MESSAGE_RECIPIENT_USER, facetRecipientUser);
    facetPanels.put(RodaConstants.MESSAGE_IS_ACKNOWLEDGED, facetAcknowledged);
    FacetUtils.bindFacets(notificationList, facetPanels);

    notificationList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        Message selected = notificationList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          Tools.newHistory(ShowNotificationMessage.RESOLVER, selected.getId());
        }
      }
    });

    initWidget(uiBinder.createAndBindUi(this));

    // FIXME a revision should be done on the description
    notificationDescription.add(new HTMLWidgetWrapper("NotificationDescription.html"));

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

    inputDateInitial.getElement().setPropertyString("placeholder", messages.sidebarFilterFromDatePlaceHolder());
    inputDateFinal.getElement().setPropertyString("placeholder", messages.sidebarFilterToDatePlaceHolder());
  }

  private void updateDateFilter() {
    Date dateInitial = inputDateInitial.getDatePicker().getValue();
    Date dateFinal = inputDateFinal.getDatePicker().getValue();

    DateRangeFilterParameter filterParameter = new DateRangeFilterParameter(RodaConstants.MESSAGE_SENT_ON, dateInitial,
      dateFinal, RodaConstants.DateGranularity.DAY);

    notificationList.setFilter(new Filter(filterParameter));
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      notificationList.refresh();
      callback.onSuccess(this);
    } else if (historyTokens.size() > 1
      && ShowNotificationMessage.RESOLVER.getHistoryToken().equals(historyTokens.get(0))) {
      ShowNotificationMessage.RESOLVER.resolve(Tools.tail(historyTokens), callback);
    } else {
      Tools.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

}
