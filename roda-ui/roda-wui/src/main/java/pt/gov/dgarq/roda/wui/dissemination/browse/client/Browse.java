/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.BrowseConstants;
import config.i18n.client.BrowseMessages;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.AuthenticatedUser;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.tools.DescriptionLevelUtils;
import pt.gov.dgarq.roda.wui.common.client.widgets.CollectionsTable;
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

	@UiField
	CollectionsTable fondsPanel;

	private SimplePanel viewPanelContainer;

	@UiField
	SimplePanel itemIcon;

	@UiField
	Label itemTitle;

	@UiField
	Button createFonds;

	private Browse() {
		initWidget(uiBinder.createAndBindUi(this));

		fondsPanel.getSelectionModel().addSelectionChangeHandler(new Handler() {

			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				SimpleDescriptionObject sdo = fondsPanel.getSelectionModel().getSelectedObject();
				if (sdo != null) {
					view(sdo);
				}
			}
		});
	}

	protected void onPermissionsUpdate(AuthenticatedUser user) {
		if (user.hasRole("administration.metadata_editor")) {
			createFonds.setVisible(true);
			// refresh.setVisible(true);
		} else {
			createFonds.setVisible(false);
			// refresh.setVisible(false);
		}
	}

	public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
		if (historyTokens.length == 0) {
			viewAction();
			callback.onSuccess(this);
		} else if (historyTokens.length == 1) {
			viewAction(historyTokens[0]);
			callback.onSuccess(this);
		} else {
			History.newItem(RESOLVER.getHistoryPath());
			callback.onSuccess(null);
		}
	}

	/**
	 * Call the view action by the history token
	 * 
	 * @param id
	 *            the pid of the object to view. if pid is null, then the base
	 *            state will be called
	 */
	public void view(final String id) {
		boolean historyUpdated = updateHistory(id);

		if (!historyUpdated) {
			viewAction(id);
		}
	}

	public void view(final SimpleDescriptionObject sdo) {
		logger.debug("view: " + sdo);
		boolean historyUpdated = updateHistory(sdo != null ? sdo.getId() : null);

		if (!historyUpdated) {
			viewAction(sdo);
		}
	}

	protected void viewAction(final String id) {
		if (id == null) {
			viewAction();
		} else {
			BrowserService.Util.getInstance().getSimpleDescriptionObject(id,
					new AsyncCallback<SimpleDescriptionObject>() {

						@Override
						public void onFailure(Throwable caught) {
							logger.error("Could not view id=" + id, caught);
						}

						@Override
						public void onSuccess(SimpleDescriptionObject sdo) {
							viewAction(sdo);
						}
					});
		}
	}

	protected void viewAction(SimpleDescriptionObject sdo) {
		logger.debug("viewAction: " + sdo);
		if (sdo != null) {
			itemIcon.setWidget(DescriptionLevelUtils.getElementLevelIconImage(sdo.getLevel()));
			itemTitle.setText(sdo.getTitle());
			fondsPanel.setParentId(sdo.getId());
		} else {
			viewAction();
		}
	}

	protected void viewAction() {
		itemIcon.setWidget(
				new HTMLPanel(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-home' style='font-size: 20px;'></i>")));
		itemTitle.setText("All collections");
		fondsPanel.setParentId(null);
	}

	private boolean updateHistory(String id) {
		boolean historyUpdated;
		String token;
		if (id == null) {
			token = "dissemination.browse";
		} else {
			token = "dissemination.browse." + id;
		}

		if (token.equals(History.getToken())) {
			historyUpdated = false;
		} else {
			logger.debug("calling new history token");
			History.newItem(token);
			historyUpdated = true;
		}
		return historyUpdated;
	}

	protected ViewListener createViewListener(String pid) {
		return new ViewListener() {

			public void onCancel(String thisPid) {
				viewPanelContainer.clear();
				// viewPanel = null;
				// updateStyle();
			}

			public void onClone(String thisPid, String clonePid) {
				Browse.this.onClone(clonePid);
			}

			public void onClose(String thisPid) {
				viewPanelContainer.clear();
				// viewPanel = null;
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
