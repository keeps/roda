/**
 * 
 */
package pt.gov.dgarq.roda.wui.ingest.list.client;

import java.util.List;
import java.util.Vector;

import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.LikeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.IngestListConstants;
import config.i18n.client.IngestListMessages;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.wui.common.client.AuthenticatedUser;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.LoginStatusListener;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.widgets.ControlPanel;
import pt.gov.dgarq.roda.wui.common.client.widgets.ControlPanel.ControlPanelListener;
import pt.gov.dgarq.roda.wui.common.client.widgets.ElementPanel;
import pt.gov.dgarq.roda.wui.common.client.widgets.LazyVerticalList;
import pt.gov.dgarq.roda.wui.common.client.widgets.LazyVerticalList.ContentSource;
import pt.gov.dgarq.roda.wui.common.client.widgets.LazyVerticalList.LazyVerticalListListener;
import pt.gov.dgarq.roda.wui.common.client.widgets.ListHeaderPanel;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.ingest.client.Ingest;

/**
 * @author Luis Faria
 * 
 */
public class IngestList implements HistoryResolver {
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

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private StateFilter currentStateFilter;

	private final DockPanel layout;

	private LazyVerticalList<SIPState> lazySIPList;

	private final ControlPanel controlPanel;

	private final WUIButton report;

	private final WUIButton view;

	private final WUIButton accept;

	private final WUIButton reject;

	private boolean initialized;

	private String filter_username;
	private String[] filter_states;
	private Boolean filter_complete;

	private IngestList() {
		layout = new DockPanel();

		controlPanel = new ControlPanel(constants.ingestListControlTitle(), constants.ingestListControlSearchTitle());

		report = new WUIButton(constants.ingestReport(), WUIButton.Left.SQUARE, WUIButton.Right.REPORT);
		view = new WUIButton(constants.ingestView(), WUIButton.Left.SQUARE, WUIButton.Right.ARROW_FORWARD);
		accept = new WUIButton(constants.ingestAccept(), WUIButton.Left.SQUARE, WUIButton.Right.CHECK);
		reject = new WUIButton(constants.ingestReject(), WUIButton.Left.SQUARE, WUIButton.Right.CROSS);

		initialized = false;

	}

	private void addSIPListHeader() {
		ListHeaderPanel lazySIPListHeader = lazySIPList.getHeader();
		lazySIPListHeader.addHeader(constants.headerFilename(), "ingest-list-header-filename",
				new SortParameter[] { new SortParameter("originalFilename", false), new SortParameter("id", false) },
				true);

		lazySIPListHeader.addHeader(constants.headerStartDate(), "ingest-list-header-date",
				new SortParameter[] { new SortParameter("datetime", false), new SortParameter("id", false) }, false);

		lazySIPListHeader.addHeader(constants.headerState(), "ingest-list-header-state",
				new SortParameter[] { new SortParameter("state", false), new SortParameter("id", false) }, true);

		lazySIPListHeader.addHeader(constants.headerPercentage(), "ingest-list-header-percentage",
				new SortParameter[] { new SortParameter("completePercentage", false), new SortParameter("id", false) },
				true);

		lazySIPListHeader.addHeader(constants.headerProducer(), "ingest-list-header-producer",
				new SortParameter[] { new SortParameter("username", false), new SortParameter("id", false) }, true);

		lazySIPListHeader.addHeader("", "ingest-list-header-toolbar", new SortParameter[] {}, true);

		lazySIPListHeader.setFillerHeader(5);
		lazySIPListHeader.setSelectedHeader(1);

	}

	/**
	 * Initialize
	 */
	public void init() {
		if (!initialized) {
			initialized = true;
			layout.clear();

			filter_username = null;
			filter_states = new String[] { READY_STATE };
			filter_complete = Boolean.FALSE;

			lazySIPList = new LazyVerticalList<SIPState>(new ContentSource<SIPState>() {

				public void getCount(Filter filter, AsyncCallback<Integer> callback) {
					IngestListService.Util.getInstance().getSIPCount(filter, callback);

				}

				public ElementPanel<SIPState> getElementPanel(SIPState element) {
					return new SIPPanel(element);
				}

				public void getElements(ContentAdapter adapter, AsyncCallback<SIPState[]> callback) {
					IngestListService.Util.getInstance().getSIPs(adapter, callback);
				}

				public String getTotalMessage(int total) {
					return messages.total(total);
				}

				public void setReportInfo(ContentAdapter adapter, String locale, AsyncCallback<Void> callback) {

					IngestListService.Util.getInstance().setSIPListReportInfo(adapter, locale, callback);
				}

			}, 15000, getFilter());

			lazySIPList.addLazyVerticalListListener(new LazyVerticalListListener<SIPState>() {

				public void onElementSelected(ElementPanel<SIPState> element) {
					updateVisibles();
				}

				public void onUpdateBegin() {
					controlPanel.setOptionsEnabled(false);
				}

				public void onUpdateFinish() {
					controlPanel.setOptionsEnabled(true);
				}

			});

			addSIPListHeader();

			controlPanel.clear();

			controlPanel.addOption(constants.optionWaitingPublishing());
			controlPanel.addOption(constants.optionAllActive());
			controlPanel.addOption(constants.optionAccepted());
			controlPanel.addOption(constants.optionQuarantine());
			controlPanel.addOption(constants.optionAll());

			controlPanel.setSelectedOptionIndex(0);
			currentStateFilter = StateFilter.READY;

			controlPanel.addControlPanelListener(new ControlPanelListener() {

				public void onOptionSelected(int option) {
					switch (option) {
					case 0:
						setStateFilter(StateFilter.READY);
						break;
					case 1:
						setStateFilter(StateFilter.PROCESSING);
						break;
					case 2:
						setStateFilter(StateFilter.ACCEPTED);
						break;
					case 3:
						setStateFilter(StateFilter.QUARANTINE);
						break;
					case 4:
					default:
						setStateFilter(StateFilter.ALL);
						break;
					}
				}

				public void onSearch(String keywords) {
					if (keywords.length() > 0) {
						filter_username = keywords;
					} else {
						filter_username = null;
					}
					lazySIPList.setFilter(getFilter());
					lazySIPList.reset();

				}

			});

			report.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					ElementPanel<SIPState> elementPanel = lazySIPList.getSelected();
					if (elementPanel != null && elementPanel instanceof SIPPanel) {
						((SIPPanel) elementPanel).setReportVisible(true);
					}

				}

			});

			view.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					ElementPanel<SIPState> elementPanel = lazySIPList.getSelected();
					if (elementPanel != null && elementPanel instanceof SIPPanel) {
						((SIPPanel) elementPanel).view();
					}
				}

			});

			accept.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					ElementPanel<SIPState> elementPanel = lazySIPList.getSelected();
					if (elementPanel != null && elementPanel instanceof SIPPanel) {
						lazySIPList.setUserMessage(constants.acceptingSIP());
						SIPPanel selected = (SIPPanel) elementPanel;
						accept.setEnabled(false);
						reject.setEnabled(false);
						selected.accept(new AsyncCallback<Object>() {

							public void onFailure(Throwable caught) {
								lazySIPList.removeUserMessage();
								logger.error("Error publishing sip", caught);
							}

							public void onSuccess(Object result) {
								lazySIPList.removeUserMessage();
								lazySIPList.update();
							}

						});
					}
				}

			});

			reject.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					ElementPanel<SIPState> elementPanel = lazySIPList.getSelected();
					if (elementPanel != null && elementPanel instanceof SIPPanel) {
						lazySIPList.setUserMessage(constants.rejectingSIP());
						SIPPanel selected = (SIPPanel) elementPanel;
						accept.setEnabled(false);
						reject.setEnabled(false);
						selected.reject(new AsyncCallback<Object>() {

							public void onFailure(Throwable caught) {
								lazySIPList.removeUserMessage();
								logger.error("Error rejecting sip", caught);
							}

							public void onSuccess(Object result) {
								lazySIPList.removeUserMessage();
								lazySIPList.update();
							}

						});
					}
				}

			});

			controlPanel.addActionButton(report);
			controlPanel.addActionButton(view);
			controlPanel.addActionButton(accept);
			controlPanel.addActionButton(reject);

			accept.setVisible(false);
			reject.setVisible(false);
			updateVisibles();

			layout.add(lazySIPList.getWidget(), DockPanel.CENTER);
			layout.add(controlPanel.getWidget(), DockPanel.EAST);

			layout.setCellWidth(lazySIPList.getWidget(), "100%");

			layout.addStyleName("wui-ingest-list");
			lazySIPList.getWidget().addStyleName("ingest-lazy-list");

			UserLogin.getInstance().addLoginStatusListener(new LoginStatusListener() {

				public void onLoginStatusChanged(AuthenticatedUser user) {
					isCurrentUserPermitted(new AsyncCallback<Boolean>() {

						public void onFailure(Throwable caught) {
							logger.error("Error getting permissions", caught);
							lazySIPList.setAutoUpdate(false);
						}

						public void onSuccess(Boolean permitted) {
							lazySIPList.setAutoUpdate(permitted);
						}

					});
				}

			});

		}
	}

	public String getHistoryPath() {
		return Ingest.getInstance().getHistoryPath() + "." + getHistoryToken();
	}

	public String getHistoryToken() {
		return "list";
	}

	public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
		if (historyTokens.length == 0) {
			init();
			callback.onSuccess(layout);
		} else {
			History.newItem(getHistoryPath());
			callback.onSuccess(null);
		}
	}

	protected void updateVisibles() {
		ElementPanel<SIPState> selected = lazySIPList.getSelected();
		report.setEnabled(selected != null);
		view.setEnabled(selected != null && selected.get().getIngestedPID() != null);

		UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<AuthenticatedUser>() {

			public void onFailure(Throwable caught) {
				logger.error("Error updating visibles", caught);
			}

			public void onSuccess(AuthenticatedUser user) {
				boolean accept_reject_role = user.hasRole("ingest.accept_reject_sip");
				accept.setVisible(accept_reject_role);
				reject.setVisible(accept_reject_role);
			}

		});
		accept.setEnabled(selected != null && selected.get().getState().equals(READY_STATE));
		reject.setEnabled(selected != null && selected.get().getState().equals(READY_STATE));
	}

	public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
		UserLogin.getInstance().checkRole(this, callback);

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
	public void setStateFilter(StateFilter filter) {
		if (currentStateFilter != filter) {
			currentStateFilter = filter;
			if (filter == StateFilter.PROCESSING) {
				filter_states = new String[] { "DROPED_FTP", "DROPED_UPLOAD_SERVICE", "DROPED_LOCAL", "UNPACKED",
						"VIRUS_FREE", "SIP_VALID", "AUTHORIZED", "REPRESENTATION_PRESENT", "SIP_INGESTED" };
				filter_complete = Boolean.FALSE;
				controlPanel.setSelectedOptionIndex(1);
			} else if (filter == StateFilter.READY) {
				filter_states = new String[] { READY_STATE };
				filter_complete = Boolean.FALSE;
				controlPanel.setSelectedOptionIndex(0);
			} else if (filter == StateFilter.QUARANTINE) {
				filter_states = new String[] { "QUARANTINE" };
				filter_complete = Boolean.TRUE;
				controlPanel.setSelectedOptionIndex(3);
			} else if (filter == StateFilter.ACCEPTED) {
				filter_states = new String[] { "ACCEPTED" };
				filter_complete = Boolean.TRUE;
				controlPanel.setSelectedOptionIndex(2);
			} else if (filter == StateFilter.ALL) {
				filter_states = null;
				filter_complete = null;
				controlPanel.setSelectedOptionIndex(4);
			} else {
				logger.error("State filter not recognized: " + filter);
			}
			lazySIPList.setFilter(getFilter());
			lazySIPList.reset();
		}

	}

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
		lazySIPList.update();
	}
}
