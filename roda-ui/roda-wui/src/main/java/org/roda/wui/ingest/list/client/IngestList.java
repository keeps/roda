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
package org.roda.wui.ingest.list.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.roda.core.common.RodaConstants;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.facet.SimpleFacetParameter;
import org.roda.core.data.adapter.filter.DateRangeFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.FilterParameter;
import org.roda.core.data.adapter.filter.LikeFilterParameter;
import org.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.v2.RodaUser;
import org.roda.core.data.v2.SIPReport;
import org.roda.wui.client.common.SIPReportList;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.ingest.Ingest;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.Tools;

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
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.IngestListConstants;
import config.i18n.client.IngestListMessages;

/**
 * @author Luis Faria
 * 
 */
public class IngestList extends Composite {

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
    public String getHistoryToken() {
      return "list";
    }

    @Override
    public List<String> getHistoryPath() {
      return Tools.concat(Ingest.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  private static IngestList instance = null;
  private static String READY_STATE = "SIP_NORMALIZED";

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static IngestList getInstance() {
    if (instance == null) {
      instance = new IngestList();
    }
    return instance;
  }

  private static IngestListConstants constants = GWT.create(IngestListConstants.class);

  private static IngestListMessages messages = GWT.create(IngestListMessages.class);

  /**
   * Filter state options
   * 
   */
  public static enum StateFilter {
    /**
     * SIPs which are in processing state, i.e. before ready state
     */
    PROCESSING, /**
                 * Ready SIPs, waiting for acceptance
                 */
    READY, /**
            * Quarantined SIPs
            */
    QUARANTINE, /**
                 * Accepted SIPs
                 */
    ACCEPTED, /**
               * All SIPs
               */
    ALL

  }

  interface MyUiBinder extends UiBinder<Widget, IngestList> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private StateFilter currentStateFilter;

  @UiField(provided = true)
  SIPReportList sipList;

  @UiField(provided = true)
  FlowPanel stateFacets;

  @UiField(provided = true)
  FlowPanel producerFacets;

  @UiField
  DateBox inputDateInitial;

  @UiField
  DateBox inputDateFinal;

  @UiField
  Button report;
  @UiField
  Button view;
  @UiField
  Button accept;
  @UiField
  Button reject;

  private String filter_username;
  private List<String> filter_states;
  private Boolean filter_complete;

  private boolean init = true;

  private IngestList() {
    filter_username = null;
    filter_states = new ArrayList<String>();
    filter_states.add(READY_STATE);
    filter_complete = Boolean.FALSE;

    Filter filter = null;

    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.SIP_REPORT_STATE),
      new SimpleFacetParameter(RodaConstants.SIP_REPORT_USERNAME));

    // TODO externalise strings
    sipList = new SIPReportList(filter, facets, "Ingest list");
    producerFacets = new FlowPanel();
    stateFacets = new FlowPanel();
    Map<String, FlowPanel> facetPanels = new HashMap<String, FlowPanel>();
    facetPanels.put(RodaConstants.SIP_REPORT_STATE, stateFacets);
    facetPanels.put(RodaConstants.SIP_REPORT_USERNAME, producerFacets);
    FacetUtils.bindFacets(sipList, facetPanels);

    initWidget(uiBinder.createAndBindUi(this));

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

    sipList.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        updateVisibles(sipList.getSelectionModel().getSelectedObject());
      }
    });

    report.setEnabled(false);
    view.setEnabled(false);
    accept.setEnabled(false);
    reject.setEnabled(false);
  }

  protected void updateVisibles(SIPReport selected) {
    report.setEnabled(selected != null);
    view.setEnabled(selected != null && selected.getIngestedID() != null);
    accept.setEnabled(selected != null && !selected.isProcessing() && !selected.isComplete());
    reject.setEnabled(selected != null && !selected.isProcessing() && !selected.isComplete());
  }

  private void updateDateFilter() {
    Date dateInitial = inputDateInitial.getDatePicker().getValue();
    Date dateFinal = inputDateFinal.getDatePicker().getValue();

    DateRangeFilterParameter filterParameter = new DateRangeFilterParameter(RodaConstants.SIP_REPORT_DATETIME,
      dateInitial, dateFinal, RodaConstants.DateGranularity.DAY);

    sipList.setFilter(new Filter(filterParameter));
  }

  @UiHandler("report")
  void handleReportAction(ClickEvent e) {
    SIPReport sipReport = sipList.getSelectionModel().getSelectedObject();
    if (sipReport != null) {
      // TODO
    }
  }

  @UiHandler("view")
  void handleViewAction(ClickEvent e) {
    SIPReport sipReport = sipList.getSelectionModel().getSelectedObject();
    if (sipReport != null) {
      // TODO
    }
  }

  @UiHandler("accept")
  void handleAcceptAction(ClickEvent e) {
    SIPReport sipReport = sipList.getSelectionModel().getSelectedObject();
    if (sipReport != null) {
      // TODO
    }
  }

  @UiHandler("reject")
  void handleRejectAction(ClickEvent e) {
    SIPReport sipReport = sipList.getSelectionModel().getSelectedObject();
    if (sipReport != null) {
      // TODO
    }
  }

  /**
   * Initialize
   */
  // public void init() {

  // controlPanel.clear();
  //
  // controlPanel.addOption(constants.optionWaitingPublishing());
  // controlPanel.addOption(constants.optionAllActive());
  // controlPanel.addOption(constants.optionAccepted());
  // controlPanel.addOption(constants.optionQuarantine());
  // controlPanel.addOption(constants.optionAll());
  //
  // controlPanel.setSelectedOptionIndex(0);
  // currentStateFilter = StateFilter.READY;
  //
  // controlPanel.addControlPanelListener(new ControlPanelListener() {
  //
  // public void onOptionSelected(int option) {
  // switch (option) {
  // case 0:
  // setStateFilter(StateFilter.READY);
  // break;
  // case 1:
  // setStateFilter(StateFilter.PROCESSING);
  // break;
  // case 2:
  // setStateFilter(StateFilter.ACCEPTED);
  // break;
  // case 3:
  // setStateFilter(StateFilter.QUARANTINE);
  // break;
  // case 4:
  // default:
  // setStateFilter(StateFilter.ALL);
  // break;
  // }
  // }
  //
  // public void onSearch(String keywords) {
  // if (keywords.length() > 0) {
  // filter_username = keywords;
  // } else {
  // filter_username = null;
  // }
  // sipList.setFilter(getFilter());
  //
  // }
  //
  // });
  //
  // report.addClickListener(new ClickListener() {
  //
  // public void onClick(Widget sender) {
  // // ElementPanel<SIPState> elementPanel =
  // // lazySIPList.getSelected();
  // // if (elementPanel != null && elementPanel instanceof
  // // SIPPanel) {
  // // ((SIPPanel) elementPanel).setReportVisible(true);
  // // }
  //
  // }
  //
  // });
  //
  // view.addClickListener(new ClickListener() {
  //
  // public void onClick(Widget sender) {
  // // ElementPanel<SIPState> elementPanel =
  // // lazySIPList.getSelected();
  // // if (elementPanel != null && elementPanel instanceof
  // // SIPPanel) {
  // // ((SIPPanel) elementPanel).view();
  // // }
  // }
  //
  // });
  //
  // accept.addClickListener(new ClickListener() {
  //
  // public void onClick(Widget sender) {
  // // ElementPanel<SIPState> elementPanel =
  // // lazySIPList.getSelected();
  // // if (elementPanel != null && elementPanel instanceof
  // // SIPPanel) {
  // // lazySIPList.setUserMessage(constants.acceptingSIP());
  // // SIPPanel selected = (SIPPanel) elementPanel;
  // // accept.setEnabled(false);
  // // reject.setEnabled(false);
  // // selected.accept(new AsyncCallback<Object>() {
  // //
  // // public void onFailure(Throwable caught) {
  // // lazySIPList.removeUserMessage();
  // // logger.error("Error publishing sip", caught);
  // // }
  // //
  // // public void onSuccess(Object result) {
  // // lazySIPList.removeUserMessage();
  // // lazySIPList.update();
  // // }
  // //
  // // });
  // // }
  // }
  //
  // });
  //
  // reject.addClickListener(new ClickListener() {
  //
  // public void onClick(Widget sender) {
  // ElementPanel<SIPState> elementPanel =
  // lazySIPList.getSelected();
  // if (elementPanel != null && elementPanel instanceof
  // SIPPanel) {
  // lazySIPList.setUserMessage(constants.rejectingSIP());
  // SIPPanel selected = (SIPPanel) elementPanel;
  // accept.setEnabled(false);
  // reject.setEnabled(false);
  // selected.reject(new AsyncCallback<Object>() {
  //
  // public void onFailure(Throwable caught) {
  // lazySIPList.removeUserMessage();
  // logger.error("Error rejecting sip", caught);
  // }
  //
  // public void onSuccess(Object result) {
  // lazySIPList.removeUserMessage();
  // lazySIPList.update();
  // }
  // //
  // // });
  // // }
  // }
  //
  // });
  //
  // controlPanel.addActionButton(report);
  // controlPanel.addActionButton(view);
  // controlPanel.addActionButton(accept);
  // controlPanel.addActionButton(reject);
  //
  // accept.setVisible(false);
  // reject.setVisible(false);
  // updateVisibles();
  //
  // layout.add(sipList, DockPanel.CENTER);
  // layout.add(controlPanel.getWidget(), DockPanel.EAST);
  //
  // layout.setCellWidth(sipList, "100%");
  //
  // layout.addStyleName("wui-ingest-list");
  // sipList.addStyleName("ingest-lazy-list");

  // UserLogin.getInstance().addLoginStatusListener(new
  // LoginStatusListener() {
  //
  // public void onLoginStatusChanged(AuthenticatedUser user) {
  // RESOLVER.isCurrentUserPermitted(new AsyncCallback<Boolean>() {
  //
  // public void onFailure(Throwable caught) {
  // logger.error("Error getting permissions", caught);
  // // lazySIPList.setAutoUpdate(false);
  // }
  //
  // public void onSuccess(Boolean permitted) {
  // // lazySIPList.setAutoUpdate(permitted);
  // }
  //
  // });
  // }
  //
  // });

  // }

  // }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      if (init) {
        init = false;
      } else {
        sipList.refresh();
      }

      callback.onSuccess(this);
    } else {
      Tools.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

  protected void updateVisibles() {
    // ElementPanel<SIPState> selected = lazySIPList.getSelected();
    SIPReport selected = sipList.getSelectionModel().getSelectedObject();
    // report.setEnabled(selected != null);
    // view.setEnabled(selected != null && selected.getIngestedID() !=
    // null);

    UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<RodaUser>() {

      public void onFailure(Throwable caught) {
        logger.error("Error updating visibles", caught);
      }

      public void onSuccess(RodaUser user) {
        boolean accept_reject_role = user.hasRole("ingest.accept_reject_sip");
        // accept.setVisible(accept_reject_role);
        // reject.setVisible(accept_reject_role);
      }

    });
    // accept.setEnabled(selected != null &&
    // selected.getState().equals(READY_STATE));
    // reject.setEnabled(selected != null &&
    // selected.getState().equals(READY_STATE));
  }

  /**
   * Get state filter
   * 
   * @return state filter
   */
  public StateFilter getStateFilter() {
    return currentStateFilter;
  }

  /**
   * Set state filter
   * 
   * @param filter
   */
  // public void setStateFilter(StateFilter filter) {
  // if (currentStateFilter != filter) {
  // currentStateFilter = filter;
  // if (filter == StateFilter.PROCESSING) {
  // filter_states = new String[] { "DROPED_FTP", "DROPED_UPLOAD_SERVICE",
  // "DROPED_LOCAL", "UNPACKED",
  // "VIRUS_FREE", "SIP_VALID", "AUTHORIZED", "REPRESENTATION_PRESENT",
  // "SIP_INGESTED" };
  // filter_complete = Boolean.FALSE;
  // controlPanel.setSelectedOptionIndex(1);
  // } else if (filter == StateFilter.READY) {
  // filter_states = new String[] { READY_STATE };
  // filter_complete = Boolean.FALSE;
  // controlPanel.setSelectedOptionIndex(0);
  // } else if (filter == StateFilter.QUARANTINE) {
  // filter_states = new String[] { "QUARANTINE" };
  // filter_complete = Boolean.TRUE;
  // controlPanel.setSelectedOptionIndex(3);
  // } else if (filter == StateFilter.ACCEPTED) {
  // filter_states = new String[] { "ACCEPTED" };
  // filter_complete = Boolean.TRUE;
  // controlPanel.setSelectedOptionIndex(2);
  // } else if (filter == StateFilter.ALL) {
  // filter_states = null;
  // filter_complete = null;
  // controlPanel.setSelectedOptionIndex(4);
  // } else {
  // logger.error("State filter not recognized: " + filter);
  // }
  // sipList.setFilter(getFilter());
  // }

  // }

  protected Filter getFilter() {

    Filter filter = new Filter();
    List<FilterParameter> parameters = new Vector<FilterParameter>();

    if (filter_username != null && filter_username.length() > 0) {
      parameters.add(new LikeFilterParameter("username", "%" + filter_username + "%"));
    }

    if (filter_states != null && filter_states.size() > 0) {
      parameters.add(new OneOfManyFilterParameter("state", filter_states));
    }

    if (filter_complete != null) {
      parameters.add(new SimpleFilterParameter("complete", filter_complete.toString()));
    }

    filter.setParameters(parameters);

    return filter;
  }

  /**
   * Update ingest list
   */
  public void update() {
    // lazySIPList.update();
  }
}
