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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.facet.SimpleFacetParameter;
import org.roda.core.data.adapter.filter.DateRangeFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.risks.Risk;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.RiskList;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
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
public class RiskRegister extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {RiskRegister.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(Planning.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "riskregister";
    }
  };

  private static RiskRegister instance = null;

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

  interface MyUiBinder extends UiBinder<Widget, RiskRegister> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  @UiField
  FlowPanel riskRegisterDescription;

  @UiField(provided = true)
  RiskList riskList;

  @UiField(provided = true)
  FlowPanel facetCategories;

  @UiField(provided = true)
  FlowPanel facetSeverities;

  @UiField
  DateBox inputDateInitial;

  @UiField
  DateBox inputDateFinal;

  @UiField
  Button buttonAdd;

  @UiField
  Button buttonEdit;

  @UiField
  Button buttonRemove;

  @UiField
  Button startProcess;

  /**
   * Create a risk register page
   *
   * @param user
   */
  public RiskRegister() {
    Filter filter = null;
    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.RISK_CATEGORY), new SimpleFacetParameter(
      RodaConstants.RISK_PRE_MITIGATION_SEVERITY));

    riskList = new RiskList(filter, facets, "Risks", false);

    facetCategories = new FlowPanel();
    facetSeverities = new FlowPanel();

    Map<String, FlowPanel> facetPanels = new HashMap<String, FlowPanel>();
    facetPanels.put(RodaConstants.RISK_CATEGORY, facetCategories);
    facetPanels.put(RodaConstants.RISK_PRE_MITIGATION_SEVERITY, facetSeverities);
    FacetUtils.bindFacets(riskList, facetPanels);

    riskList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        Risk selected = riskList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          Tools.newHistory(RESOLVER, ShowRisk.RESOLVER.getHistoryToken(), selected.getId());
        }
      }
    });

    initWidget(uiBinder.createAndBindUi(this));

    riskRegisterDescription.add(new HTMLWidgetWrapper("RiskRegisterDescription.html"));

    buttonEdit.setEnabled(false);
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

    inputDateInitial.getElement().setPropertyString("placeholder", messages.sidebarFilterFromDatePlaceHolder());
    inputDateFinal.getElement().setPropertyString("placeholder", messages.sidebarFilterToDatePlaceHolder());
  }

  private void updateDateFilter() {
    Date dateInitial = inputDateInitial.getDatePicker().getValue();
    Date dateFinal = inputDateFinal.getDatePicker().getValue();

    DateRangeFilterParameter filterParameter = new DateRangeFilterParameter(RodaConstants.RISK_IDENTIFIED_ON,
      dateInitial, dateFinal, RodaConstants.DateGranularity.DAY);

    riskList.setFilter(new Filter(filterParameter));
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      riskList.refresh();
      callback.onSuccess(this);
    } else if (historyTokens.size() == 2 && historyTokens.get(0).equals(ShowRisk.RESOLVER.getHistoryToken())) {
      ShowRisk.RESOLVER.resolve(Tools.tail(historyTokens), callback);
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
