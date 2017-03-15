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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.DateRangeFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.NotificationList;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

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

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class NotificationRegister extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {NotificationRegister.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Management.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "notifications";
    }
  };

  private static NotificationRegister instance = null;

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static NotificationRegister getInstance() {
    if (instance == null) {
      instance = new NotificationRegister();
    }
    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, NotificationRegister> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel notificationDescription;

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField
  DateBox inputDateInitial;

  @UiField
  DateBox inputDateFinal;

  @UiField(provided = true)
  NotificationList notificationList;

  @UiField(provided = true)
  FlowPanel facetRecipientUsers;

  @UiField(provided = true)
  FlowPanel facetState;

  @UiField(provided = true)
  FlowPanel facetAcknowledged;

  private static final Filter DEFAULT_FILTER = SearchFilters.defaultFilter(Notification.class.getName());
  private static final String ALL_FILTER = SearchFilters.allFilter(Notification.class.getName());

  /**
   * Create a new notification
   *
   * @param user
   */
  public NotificationRegister() {
    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.NOTIFICATION_RECIPIENT_USERS),
      new SimpleFacetParameter(RodaConstants.NOTIFICATION_IS_ACKNOWLEDGED),
      new SimpleFacetParameter(RodaConstants.NOTIFICATION_STATE));
    notificationList = new NotificationList(Filter.NULL, facets, messages.notificationsTitle(), false);

    searchPanel = new SearchPanel(DEFAULT_FILTER, ALL_FILTER, true, messages.messageSearchPlaceHolder(), false, false,
      false);
    searchPanel.setList(notificationList);

    facetRecipientUsers = new FlowPanel();
    facetAcknowledged = new FlowPanel();
    facetState = new FlowPanel();

    Map<String, FlowPanel> facetPanels = new HashMap<>();
    facetPanels.put(RodaConstants.NOTIFICATION_STATE, facetState);
    facetPanels.put(RodaConstants.NOTIFICATION_IS_ACKNOWLEDGED, facetAcknowledged);
    facetPanels.put(RodaConstants.NOTIFICATION_RECIPIENT_USERS, facetRecipientUsers);
    FacetUtils.bindFacets(notificationList, facetPanels);

    notificationList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        Notification selected = notificationList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          HistoryUtils.newHistory(ShowNotification.RESOLVER, selected.getId());
        }
      }
    });

    initWidget(uiBinder.createAndBindUi(this));
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
    inputDateInitial.setTitle(messages.dateIntervalLabelInitial());

    inputDateFinal.setFormat(dateFormat);
    inputDateFinal.getDatePicker().setYearArrowsVisible(true);
    inputDateFinal.setFireNullValues(true);
    inputDateFinal.addValueChangeHandler(valueChangeHandler);
    inputDateFinal.setTitle(messages.dateIntervalLabelFinal());

    inputDateInitial.getElement().setPropertyString("placeholder", messages.sidebarFilterFromDatePlaceHolder());
    inputDateFinal.getElement().setPropertyString("placeholder", messages.sidebarFilterToDatePlaceHolder());
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  private void updateDateFilter() {
    Date dateInitial = inputDateInitial.getDatePicker().getValue();
    Date dateFinal = inputDateFinal.getDatePicker().getValue();

    DateRangeFilterParameter filterParameter = new DateRangeFilterParameter(RodaConstants.NOTIFICATION_SENT_ON,
      dateInitial, dateFinal, RodaConstants.DateGranularity.DAY);

    notificationList.setFilter(new Filter(filterParameter));
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      notificationList.refresh();
      notificationList.setFilter(Filter.ALL);
      callback.onSuccess(this);
    } else if (historyTokens.size() > 1 && ShowNotification.RESOLVER.getHistoryToken().equals(historyTokens.get(0))) {
      ShowNotification.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else {
      HistoryUtils.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

}
