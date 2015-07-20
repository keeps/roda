/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.BrowseConstants;
import config.i18n.client.BrowseMessages;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.AuthenticatedUser;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.tools.PIDTranslator;
import pt.gov.dgarq.roda.wui.common.client.widgets.CollectionsTable;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.ViewPanel.ViewListener;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.images.BrowseImageBundle;
import pt.gov.dgarq.roda.wui.dissemination.client.Dissemination;

/**
 * @author Luis Faria
 * 
 */
public class Browse extends Composite {

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
			return "browse";
		}

		@Override
		public String getHistoryPath() {
			return Dissemination.getInstance().getHistoryPath() + "." + getHistoryToken();
		}
	};

	interface MyUiBinder extends UiBinder<Widget, Browse> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	private static Browse instance = null;

	/**
	 * Get the singleton instance
	 * 
	 * @return the instance
	 */
	public static Browse getInstance() {
		if (instance == null) {
			instance = new Browse();
		}
		return instance;
	}

	private static BrowseConstants constants = (BrowseConstants) GWT.create(BrowseConstants.class);

	private static BrowseMessages messages = (BrowseMessages) GWT.create(BrowseMessages.class);

	private static BrowseImageBundle browseImageBundle = (BrowseImageBundle) GWT.create(BrowseImageBundle.class);

	private ClientLogger logger = new ClientLogger(getClass().getName());

	// private HorizontalSplitPanel split;

	// private CollectionsTreeVerticalScrollPanel fondsPanel;

	@UiField
	CollectionsTable fondsPanel;

	private SimplePanel viewPanelContainer;

	// private boolean init;

	private HorizontalPanel browserHeader;

	private ToggleButton viewToggle;

	private Label total;

	private WUIButton createFonds;

	private Image refresh;

	private ViewWindow viewWindow;

	private ViewPanel viewPanel;

	private Browse() {
		// this.fondsPanel = new CollectionsDataGrid();
		initWidget(uiBinder.createAndBindUi(this));

		// init = false;

	}

	// private void init_old() {
	// if (!init) {
	// logger.debug("Initializing browser");
	// init = true;
	//
	// browserHeader = new HorizontalPanel();
	//
	// Image viewPanelToggleImage =
	// browseImageBundle.browseViewPanel().createImage();
	// Image viewWindowToggleImage =
	// browseImageBundle.browseViewWindow().createImage();
	// viewPanelToggleImage.setTitle(constants.viewPanelToggleTitle());
	// viewWindowToggleImage.setTitle(constants.viewWindowToggleTitle());
	//
	// viewToggle = new ToggleButton(viewPanelToggleImage,
	// viewWindowToggleImage, new ClickHandler() {
	//
	// @Override
	// public void onClick(ClickEvent event) {
	// updateStyle();
	// }
	//
	// });
	//
	// total = new Label();
	// createFonds = new WUIButton(constants.addFond(), WUIButton.Left.ROUND,
	// WUIButton.Right.PLUS);
	// refresh = browseImageBundle.refresh().createImage();
	// createFonds.addClickHandler(new ClickHandler() {
	//
	// public void onClick(ClickEvent event) {
	// final LoadingPopup loading = new LoadingPopup(Browse.this);
	// loading.show();
	// EditorService.Util.getInstance().createCollection(new
	// AsyncCallback<String>() {
	//
	// public void onFailure(Throwable caught) {
	// loading.hide();
	// logger.error("Error creating fonds", caught);
	// }
	//
	// public void onSuccess(final String pid) {
	// loading.hide();
	// update(new AsyncCallback<CollectionsTreeItem>() {
	// public void onFailure(Throwable caught) {
	// logger.error("Error updating tree", caught);
	// }
	//
	// public void onSuccess(CollectionsTreeItem treeItem) {
	// ViewPanel.setEditMode(true);
	// view(pid);
	// }
	//
	// });
	// }
	//
	// });
	// }
	//
	// });
	// refresh.addClickHandler(new ClickHandler() {
	//
	// public void onClick(ClickEvent event) {
	// // fondsPanel.clear(new AsyncCallback<Integer>() {
	// //
	// // public void onFailure(Throwable caught) {
	// // logger.error("Error refreshing browser", caught);
	// // }
	// //
	// // public void onSuccess(Integer result) {
	// // // nothing to do
	// // }
	// //
	// // });
	//
	// }
	//
	// });
	// createFonds.setVisible(false);
	// refresh.setVisible(false);
	// browserHeader.add(total);
	// browserHeader.add(createFonds);
	// browserHeader.add(refresh);
	// browserHeader.add(viewToggle);
	// // add(browserHeader, NORTH);
	// split = new HorizontalSplitPanel();
	// viewPanelContainer = new SimplePanel();
	// split.setRightWidget(viewPanelContainer);
	// // add(split, CENTER);
	//
	// viewWindow = null;
	// viewPanel = null;
	//
	// browserHeader.setCellWidth(viewToggle, "100%");
	// browserHeader.setCellHorizontalAlignment(viewToggle,
	// HorizontalPanel.ALIGN_RIGHT);
	//
	// this.addStyleName("wui-browse");
	// browserHeader.addStyleName("browse-header");
	// total.addStyleName("browse-total");
	// createFonds.addStyleName("browse-createFonds");
	// refresh.addStyleName("browse-refresh");
	// split.setStylePrimaryName("wui-browse-split");
	// viewPanelContainer.addStyleName("viewPanel-container");
	// viewToggle.addStyleName("view-toggle");
	//
	// // this.fondsPanel = new CollectionsTreeVerticalScrollPanel(true);
	// // this.fondsPanel2 = new CollectionsDataGrid();
	// // this.fondsPanel2.setSize("100%", "600px");
	// // split.setLeftWidget(fondsPanel);
	// // split.setLeftWidget(fondsPanel2);
	// // add(fondsPanel2, CENTER);
	// updateTotal();
	//
	// // fondsPanel.addClickListener(new ClickListener() {
	// //
	// // public void onClick(Widget sender) {
	// // CollectionsTreeItem selected = fondsPanel.getSelected();
	// // Browse.getInstance().view(selected.getPid());
	// // }
	// //
	// // });
	//
	// split.setWidth("865px");
	// split.setHeight("460px");
	// updateStyle();
	//
	// UserLogin.getInstance().getAuthenticatedUser(new
	// AsyncCallback<AuthenticatedUser>() {
	//
	// public void onFailure(Throwable caught) {
	// logger.error("Error getting authenticated user", caught);
	// }
	//
	// public void onSuccess(AuthenticatedUser user) {
	// onPermissionsUpdate(user);
	//
	// }
	// });
	//
	// UserLogin.getInstance().addLoginStatusListener(new LoginStatusListener()
	// {
	//
	// public void onLoginStatusChanged(AuthenticatedUser user) {
	// onPermissionsUpdate(user);
	// }
	//
	// });
	//
	// }
	// }

	// /**
	// * Update the total count of collections
	// */
	// public void updateTotal() {
	// fondsPanel.getCount(new AsyncCallback<Integer>() {
	//
	// public void onFailure(Throwable caught) {
	// logger.error("Error getting total number of collections", caught);
	// }
	//
	// public void onSuccess(Integer count) {
	// if (count == 0) {
	// total.setText(constants.repositoryEmpty());
	// } else {
	// total.setText(messages.totalFondsNumber(count));
	// }
	// }
	//
	// });
	// }

	protected void onPermissionsUpdate(AuthenticatedUser user) {
		if (user.hasRole("administration.metadata_editor")) {
			createFonds.setVisible(true);
			refresh.setVisible(true);
		} else {
			createFonds.setVisible(false);
			refresh.setVisible(false);
		}

		// fondsPanel.clear(new AsyncCallback<Integer>() {
		//
		// public void onFailure(Throwable caught) {
		// logger.error("Error refreshing browser", caught);
		// }
		//
		// public void onSuccess(Integer result) {
		// // nothing to do
		// }
		//
		// });

		// updateTotal();
	}

	// protected void updateStyle() {
	// if (!viewToggle.isDown()) {
	// if (viewPanel == null) {
	// split.addStyleDependentName("hidden");
	// split.setSplitPosition("865px");
	// // fondsPanel.setShowInfo(true);
	// } else {
	// split.removeStyleDependentName("hidden");
	// split.setSplitPosition("210px");
	// // fondsPanel.setShowInfo(false);
	// }
	// } else {
	// if (viewPanel != null) {
	// viewPanelContainer.clear();
	// ViewWindow viewWindow = new ViewWindow(viewPanel);
	// viewWindow.show();
	// viewPanel = null;
	// }
	// split.addStyleDependentName("hidden");
	// split.setSplitPosition("865px");
	// // fondsPanel.setShowInfo(true);
	// }
	// }

	public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
		// init();
		if (historyTokens.length == 0) {
			if (viewPanel != null) {
				viewPanel.close();
				viewPanel = null;
			}
			// updateStyle();
			callback.onSuccess(this);
		} else if (historyTokens.length == 1) {
			// fondsPanel.setSelected(PIDTranslator.untranslatePID(historyTokens[0]));
			viewAction(PIDTranslator.untranslatePID(historyTokens[0]));
			callback.onSuccess(this);
		} else {
			History.newItem(RESOLVER.getHistoryPath());
			callback.onSuccess(null);
		}
	}

	private void viewAction(final String pid) {
		if (viewToggle.isDown()) {
			logger.debug("Opening viewWindow with " + pid);
			// updateStyle();
			if (viewWindow != null && !viewWindow.getPID().equals(pid)) {
				viewWindow.hide();
			}

			if (viewWindow == null || !viewWindow.getPID().equals(pid)) {
				viewWindow = new ViewWindow(pid, new AsyncCallback<DescriptionObject>() {

					public void onFailure(Throwable caught) {
						if (caught instanceof NoSuchRODAObjectException) {
							// onNoSuchObject(pid);
						} else {
							logger.error("Error creating view window", caught);
						}
					}

					public void onSuccess(DescriptionObject obj) {
						viewWindow.show();
					}

				});
				viewWindow.addViewListener(createViewListener(pid));
			}

		} else {
			if (viewPanel != null && !viewPanel.getPID().equals(pid)) {
				viewPanel.close();
			}

			if (viewPanel == null || !viewPanel.getPID().equals(pid)) {
				logger.debug("Opening viewPanel with " + pid);
				viewPanel = new ViewPanel(pid, new AsyncCallback<DescriptionObject>() {

					public void onFailure(Throwable caught) {
						if (caught instanceof NoSuchRODAObjectException) {
							// onNoSuchObject(pid);
						} else {
							logger.error("Error creating view window", caught);
						}

					}

					public void onSuccess(DescriptionObject obj) {
						// updateStyle();
						viewPanelContainer.setWidget(viewPanel);
					}

				});

				viewPanel.addViewListener(createViewListener(pid));

			}
		}
	}

	// protected void onNoSuchObject(final String pid) {
	// update(pid, true, true, new AsyncCallback<CollectionsTreeItem>() {
	//
	// public void onFailure(Throwable caught) {
	// logger.error("Error creating updating tree" + " after RODA object not
	// found", caught);
	// }
	//
	// public void onSuccess(CollectionsTreeItem treeItem) {
	// Window.alert(messages.noSuchRODAObject(pid));
	// }
	//
	// });
	// }

	/**
	 * Call the view action by the history token
	 * 
	 * @param pid
	 *            the pid of the object to view. if pid is null, then the base
	 *            state will be called
	 */
	public void view(String pid) {
		String token;
		logger.debug("viewing pid=" + pid);
		if (pid == null) {
			token = "dissemination.browse";
		} else {
			token = "dissemination.browse." + PIDTranslator.translatePID(pid);
		}
		if (token.equals(History.getToken())) {
			if (pid == null) {
				viewPanel.close();
				viewPanel = null;
			} else {
				viewAction(pid);
			}

		} else {
			logger.debug("calling new history token");
			History.newItem(token);
		}

	}

	/**
	 * Complete refresh the elements tree list
	 * 
	 * @param callback
	 *            interface to handle the finish of the refresh
	 */
	// public void update(AsyncCallback<CollectionsTreeItem> callback) {
	// update(null, false, true, callback);
	// }

	/**
	 * Refresh an element in the tree list
	 * 
	 * @param pid
	 *            the pid of the element
	 * @param info
	 *            refresh the information of the element (id, level, title,
	 *            initial or final date)
	 * @param hierarchy
	 *            refresh the children list of that element
	 * @param callback
	 *            interface to handle the finish of the refresh
	 */
	// public void update(String pid, boolean info, boolean hierarchy,
	// AsyncCallback<CollectionsTreeItem> callback) {
	// fondsPanel.update(pid, info, hierarchy, callback);
	// if (pid == null) {
	// updateTotal();
	// }
	// }

	protected ViewListener createViewListener(String pid) {
		return new ViewListener() {

			public void onCancel(String thisPid) {
				viewPanelContainer.clear();
				viewPanel = null;
				// updateStyle();
			}

			public void onClone(String thisPid, String clonePid) {
				Browse.this.onClone(clonePid);
			}

			public void onClose(String thisPid) {
				viewPanelContainer.clear();
				viewPanel = null;
				// updateStyle();
			}

			public void onCreateChild(String thisPid, final String childPid) {
				// update(thisPid, false, true, new
				// AsyncCallback<CollectionsTreeItem>() {
				//
				// public void onFailure(Throwable caught) {
				// logger.error("Error updating tree", caught);
				// }
				//
				// public void onSuccess(CollectionsTreeItem treeItem) {
				// view(childPid);
				// ViewPanel.setEditMode(true);
				// }
				//
				// });
			}

			public void onEdit(String thisPid) {
				// nothing to do
			}

			public void onMove(String thisPid, String oldParentPid, String newParentPid) {
				Browse.this.onMove(thisPid, oldParentPid, newParentPid);
			}

			public void onRemove(String thisPid, String parentPid) {
				Browse.this.onRemove(parentPid);
			}

			public void onSave(DescriptionObject obj) {
				// nothing to do?
			}

		};
	}

	protected void onMove(final String targetPid, final String oldParentPid, final String newParentPid) {

		// update(oldParentPid, false, true, new
		// AsyncCallback<CollectionsTreeItem>() {
		//
		// public void onFailure(Throwable caught) {
		// logger.error("Error on move event", caught);
		// }
		//
		// public void onSuccess(CollectionsTreeItem treeItem) {
		// update(newParentPid, false, true, new
		// AsyncCallback<CollectionsTreeItem>() {
		//
		// public void onFailure(Throwable caught) {
		// logger.error("Error on move event", caught);
		// }
		//
		// public void onSuccess(CollectionsTreeItem result) {
		// // fondsPanel.setSelected(null);
		// // fondsPanel.setSelected(targetPid);
		// }
		//
		// });
		//
		// }

		// });
	}

	protected void onClone(final String clonePID) {
		BrowserService.Util.getInstance().getParent(clonePID, new AsyncCallback<String>() {

			public void onFailure(Throwable caught) {
				logger.error("Error on cloning event", caught);
			}

			public void onSuccess(final String parentPID) {
				// update(parentPID, false, true, new
				// AsyncCallback<CollectionsTreeItem>() {
				//
				// public void onFailure(Throwable caught) {
				// logger.error("Error on cloning event", caught);
				// }
				//
				// public void onSuccess(CollectionsTreeItem treeItem) {
				// ViewPanel.setEditMode(true);
				// view(clonePID);
				//
				// }
				//
				// });
			}

		});
	}

	protected void onRemove(final String parentPID) {
		// update(parentPID, false, true, new
		// AsyncCallback<CollectionsTreeItem>() {
		//
		// public void onFailure(Throwable caught) {
		// logger.error("Error on remove event", caught);
		// }
		//
		// public void onSuccess(CollectionsTreeItem treeItem) {
		// view(null);
		// }
		//
		// });
	}
}
