/**
 * 
 */
package pt.gov.dgarq.roda.wui.ingest.list.client;

import java.util.List;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.IngestListConstants;
import config.i18n.client.IngestListMessages;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.LikeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.v2.SIPReport;
import pt.gov.dgarq.roda.wui.common.client.AuthenticatedUser;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.widgets.SIPReportList;
import pt.gov.dgarq.roda.wui.ingest.client.Ingest;

/**
 * @author Luis Faria
 * 
 */
public class IngestList extends Composite {

	public static final HistoryResolver RESOLVER = new HistoryResolver() {

		@Override
		public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
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
		public String getHistoryPath() {
			return Ingest.RESOLVER.getHistoryPath() + "." + getHistoryToken();
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

	@UiField
	SIPReportList sipList;

	@UiField
	CheckBox checkProcessed;
	@UiField
	CheckBox checkProcessing;
	@UiField
	CheckBox checkAccepted;
	@UiField
	CheckBox checkRejected;
	@UiField
	CheckBox checkAll;

	@UiField
	Button report;
	@UiField
	Button view;
	@UiField
	Button accept;
	@UiField
	Button reject;

	private String filter_username;
	private String[] filter_states;
	private Boolean filter_complete;

	private IngestList() {
		filter_username = null;
		filter_states = new String[] { READY_STATE };
		filter_complete = Boolean.FALSE;

		initWidget(uiBinder.createAndBindUi(this));
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
//	public void init() {

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

//	}

	public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
		if (historyTokens.length == 0) {
			// init();
			sipList.refresh();
			callback.onSuccess(this);
		} else {
			History.newItem(RESOLVER.getHistoryPath());
			callback.onSuccess(null);
		}
	}

	protected void updateVisibles() {
		// ElementPanel<SIPState> selected = lazySIPList.getSelected();
		SIPReport selected = sipList.getSelectionModel().getSelectedObject();
		// report.setEnabled(selected != null);
		// view.setEnabled(selected != null && selected.getIngestedID() !=
		// null);

		UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<AuthenticatedUser>() {

			public void onFailure(Throwable caught) {
				logger.error("Error updating visibles", caught);
			}

			public void onSuccess(AuthenticatedUser user) {
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

		if (filter_states != null && filter_states.length > 0) {
			parameters.add(new OneOfManyFilterParameter("state", filter_states));
		}

		if (filter_complete != null) {
			parameters.add(new SimpleFilterParameter("complete", filter_complete.toString()));
		}

		filter.setParameters((FilterParameter[]) parameters.toArray(new FilterParameter[] {}));

		return filter;
	}

	/**
	 * Update ingest list
	 */
	public void update() {
		// lazySIPList.update();
	}
}
