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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.DateRangeFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.lists.RiskIncidenceList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell.CheckboxSelectionListener;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class RiskIncidenceRegister extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {RiskIncidenceRegister.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return ListUtils.concat(Planning.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "riskincidenceregister";
    }
  };

  private static RiskIncidenceRegister instance = null;

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static RiskIncidenceRegister getInstance() {
    if (instance == null) {
      instance = new RiskIncidenceRegister();
    }
    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, RiskIncidenceRegister> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  Label riskIncidenceRegisterTitle;

  @UiField
  FlowPanel riskIncidenceRegisterDescription;

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField(provided = true)
  RiskIncidenceList riskIncidenceList;

  @UiField(provided = true)
  FlowPanel facetDetectedBy, facetStatus;

  @UiField
  DateBox inputDateInitial;

  @UiField
  DateBox inputDateFinal;

  @UiField
  Button buttonRemove;

  private static final Filter DEFAULT_FILTER = SearchFilters.defaultFilter(RiskIncidence.class.getName());
  private static final String ALL_FILTER = SearchFilters.allFilter(RiskIncidence.class.getName());

  private String aipId = null;

  /**
   * Create a risk register page
   *
   * @param user
   */

  public RiskIncidenceRegister() {
    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.RISK_INCIDENCE_DETECTED_BY),
      new SimpleFacetParameter(RodaConstants.RISK_INCIDENCE_STATUS));

    riskIncidenceList = new RiskIncidenceList(Filter.NULL, facets, messages.riskIncidencesTitle(), true);

    searchPanel = new SearchPanel(DEFAULT_FILTER, ALL_FILTER, messages.riskIncidenceRegisterSearchPlaceHolder(), false,
      false, false);
    searchPanel.setList(riskIncidenceList);

    facetDetectedBy = new FlowPanel();
    facetStatus = new FlowPanel();

    Map<String, FlowPanel> facetPanels = new HashMap<String, FlowPanel>();
    facetPanels.put(RodaConstants.RISK_INCIDENCE_DETECTED_BY, facetDetectedBy);
    facetPanels.put(RodaConstants.RISK_INCIDENCE_STATUS, facetStatus);
    FacetUtils.bindFacets(riskIncidenceList, facetPanels);

    riskIncidenceList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        final RiskIncidence selected = riskIncidenceList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          HistoryUtils.newHistory(RiskIncidenceRegister.RESOLVER, ShowRiskIncidence.RESOLVER.getHistoryToken(),
            selected.getId());
        }
      }
    });

    riskIncidenceList.addCheckboxSelectionListener(new CheckboxSelectionListener<RiskIncidence>() {

      @Override
      public void onSelectionChange(SelectedItems<RiskIncidence> selected) {
        boolean empty = ClientSelectedItemsUtils.isEmpty(selected);
        if (empty) {
          buttonRemove.setEnabled(false);
        } else {
          buttonRemove.setEnabled(true);
        }
      }

    });

    initWidget(uiBinder.createAndBindUi(this));
    riskIncidenceRegisterDescription.add(new HTMLWidgetWrapper("RiskIncidenceRegisterDescription.html"));
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

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  private void updateDateFilter() {
    Date dateInitial = inputDateInitial.getDatePicker().getValue();
    Date dateFinal = inputDateFinal.getDatePicker().getValue();

    DateRangeFilterParameter filterParameter = new DateRangeFilterParameter(RodaConstants.RISK_INCIDENCE_DETECTED_ON,
      dateInitial, dateFinal, RodaConstants.DateGranularity.DAY);

    riskIncidenceList.setFilter(new Filter(filterParameter));
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      setAipId(null);
      riskIncidenceList.setFilter(Filter.ALL);
      riskIncidenceList.refresh();
      callback.onSuccess(this);
    } else if (historyTokens.size() == 2 && historyTokens.get(0).equals(ShowRiskIncidence.RESOLVER.getHistoryToken())) {
      ShowRiskIncidence.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() == 2 && historyTokens.get(0).equals(EditRiskIncidence.RESOLVER.getHistoryToken())) {
      EditRiskIncidence.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() == 1) {
      final String aipId = historyTokens.get(0);
      setAipId(aipId);
      riskIncidenceList.setFilter(new Filter(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_AIP_ID, aipId)));
      riskIncidenceList.refresh();
      callback.onSuccess(this);
    } else if (historyTokens.size() == 2) {
      final String aipId = historyTokens.get(0);
      final String repId = historyTokens.get(1);
      setAipId(aipId);
      riskIncidenceList.setFilter(new Filter(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_AIP_ID, aipId),
        new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID, repId)));
      riskIncidenceList.refresh();
      callback.onSuccess(this);
    } else {
      HistoryUtils.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

  public String getAipId() {
    return aipId;
  }

  public void setAipId(String id) {
    aipId = id;
  }

  @UiHandler("buttonRemove")
  void buttonRemoveRiskHandler(ClickEvent e) {

    final SelectedItems<RiskIncidence> selected = riskIncidenceList.getSelected();

    ClientSelectedItemsUtils.size(RiskIncidence.class, selected, new AsyncCallback<Long>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.riskRemoveFolderConfirmDialogTitle(),
          messages.riskRemoveSelectedConfirmDialogMessage(size), messages.riskRemoveFolderConfirmDialogCancel(),
          messages.riskRemoveFolderConfirmDialogOk(), new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable caught) {
              AsyncCallbackUtils.defaultFailureTreatment(caught);
            }

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                buttonRemove.setEnabled(false);
                BrowserService.Util.getInstance().deleteRiskIncidences(selected, new AsyncCallback<Void>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    AsyncCallbackUtils.defaultFailureTreatment(caught);
                    riskIncidenceList.refresh();
                  }

                  @Override
                  public void onSuccess(Void result) {
                    Toast.showInfo(messages.riskRemoveSuccessTitle(), messages.riskRemoveSuccessMessage(size));
                    riskIncidenceList.refresh();
                  }
                });
              }
            }
          });
      }
    });
  }

}
