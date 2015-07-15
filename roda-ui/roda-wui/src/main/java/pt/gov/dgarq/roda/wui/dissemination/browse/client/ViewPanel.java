/**
 *
 */
package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import java.util.List;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.BrowseConstants;
import config.i18n.client.BrowseMessages;
import pt.gov.dgarq.roda.core.common.InvalidDescriptionLevel;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.RODAObjectUserPermissions;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.AuthenticatedUser;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.LoginStatusListener;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.widgets.LoadingPopup;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.dissemination.client.DescriptiveMetadataPanel;
import pt.gov.dgarq.roda.wui.main.client.Main;
import pt.gov.dgarq.roda.wui.management.editor.client.EditObjectPermissionsPanel;
import pt.gov.dgarq.roda.wui.management.editor.client.EditProducersPanel;
import pt.gov.dgarq.roda.wui.management.editor.client.EditorService;
import pt.gov.dgarq.roda.wui.management.editor.client.MoveChooseDestinationPanel;
import pt.gov.dgarq.roda.wui.management.editor.client.MoveChooseDestinationPanel.MoveListener;

/**
 * @author Luis Faria <lfaria@keep.pt>
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public class ViewPanel extends Composite {

	private static final boolean SHOW_OPTIONAL_FIELDS_DEFAULT = false;
	private ClientLogger logger = new ClientLogger(getClass().getName());
	private static BrowseConstants constants = (BrowseConstants) GWT.create(BrowseConstants.class);
	private static BrowseMessages messages = (BrowseMessages) GWT.create(BrowseMessages.class);
	private static int selectedTab = 0;
	private static boolean editmode = false;

	/**
	 * Set edit mode
	 * 
	 * @param editmode
	 */
	public static void setEditMode(boolean editmode) {
		ViewPanel.editmode = editmode;
	}

	/**
	 * Get edit mode
	 * 
	 * @return true if in edit mode
	 */
	public static boolean getEditMode() {
		return editmode;
	}

	private static boolean modifyGlobalPermission = false;
	private static boolean listUsersGlobalPermission = false;
	private final String pid;
	private SimpleDescriptionObject sdo;
	private final DockPanel layout;
	private final Label optionalFieldsToggle;
	private boolean showOptionalFields;
	private final HorizontalPanel bottomToolbar;
	private final WUIButton edit;
	private final WUIButton save;
	private final WUIButton createChild;
	private final WUIButton move;
	// private final WUIButton clone;
	private final WUIButton remove;
	private final WUIButton downloadEADC;
	private final WUIButton downloadPremis;
	private final WUIButton close;
	private final TabPanel tabs;
	private DescriptiveMetadataPanel descriptiveMetadata;
	private RepresentationsPanel disseminations;
	private PreservationMetadataPanel preservationMetadata;
	private EditProducersPanel editProducersPanel = null;
	private EditObjectPermissionsPanel objPermissionsPanel = null;
	private int descriptiveMetadataTabIndex = -1;
	private int disseminationsTabIndex = -1;
	private int preservationMetadataTabIndex = -1;
	private int editProducersTabIndex = -1;
	private int objPermissionsTabIndex = -1;
	private boolean modifyObjectPermission;
	private boolean removeObjectPermission;
	private boolean grantObjectPermission;
	private VerticalPanel descriptiveMetadataLayout;
	private ScrollPanel descriptiveMetadataScroll;
	private final LoadingPopup loading;

	/**
	 * Listener for View actions
	 */
	public interface ViewListener {

		/**
		 * Called when panel is closed and all data is saved
		 * 
		 * @param thisPid
		 */
		public void onClose(String thisPid);

		/**
		 * Called when panel is closed but data was canceled for saving
		 * 
		 * @param thisPid
		 */
		public void onCancel(String thisPid);

		/**
		 * Called when user asks for edit mode
		 * 
		 * @param thisPid
		 */
		public void onEdit(String thisPid);

		/**
		 * Called when edition is saved
		 * 
		 * @param dobj
		 * 
		 */
		public void onSave(DescriptionObject dobj);

		/**
		 * Called when a child is created
		 * 
		 * @param thisPid
		 * 
		 * @param childPid
		 */
		public void onCreateChild(String thisPid, String childPid);

		/**
		 * Called when clone is created
		 * 
		 * @param thisPid
		 * 
		 * @param clonePid
		 */
		public void onClone(String thisPid, String clonePid);

		/**
		 * Called when object is moved in the hierarchy
		 * 
		 * @param thisPid
		 *            the PID of the object being moved
		 * @param oldParentPid
		 *            the old object parent, where the object was moved from
		 * @param newParentPid
		 *            the new object parent, where the object was moved to
		 */
		public void onMove(String thisPid, String oldParentPid, String newParentPid);

		/**
		 * Called when object is removed
		 * 
		 * @param thisPid
		 * 
		 * @param parentPid
		 */
		public void onRemove(String thisPid, String parentPid);
	}

	private final List<ViewListener> listeners;

	/**
	 * Create a new view panel
	 * 
	 * @param pid
	 * @param callback
	 */
	public ViewPanel(String pid, AsyncCallback<DescriptionObject> callback) {
		this.pid = pid;
		sdo = null;
		layout = new DockPanel();

		initWidget(layout);

		loading = new LoadingPopup(this);

		showOptionalFields = SHOW_OPTIONAL_FIELDS_DEFAULT;

		optionalFieldsToggle = new Label();

		optionalFieldsToggle
				.setText(showOptionalFields ? constants.hideOptionalFields() : constants.showOptionalFields());

		optionalFieldsToggle.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				showOptionalFields = !showOptionalFields;
				optionalFieldsToggle
						.setText(showOptionalFields ? constants.hideOptionalFields() : constants.showOptionalFields());
				descriptiveMetadata.setOptionalVisible(showOptionalFields);
			}
		});

		bottomToolbar = new HorizontalPanel();

		edit = new WUIButton(constants.editDescriptiveMetadata(), WUIButton.Left.ROUND, WUIButton.Right.ARROW_FORWARD);

		edit.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				editmode = !editmode;
				updateVisibility();
				tabs.selectTab(0);
				onEdit();
			}
		});

		save = new WUIButton(constants.saveDescriptiveMetadata(), WUIButton.Left.ROUND, WUIButton.Right.REC);

		save.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (save.isEnabled()) {
					descriptiveMetadata.commit(new AsyncCallback<DescriptionObject>() {
						public void onFailure(Throwable caught) {
							Window.alert(messages.editSaveError(caught.getMessage()));
						}

						public void onSuccess(DescriptionObject obj) {
							save.setEnabled(false);
							// TODO fix this
							// sdo = obj;
							updateCreateChildStatus();
							onSave(obj);
						}
					});
				}
			}
		});

		save.setEnabled(false);

		createChild = new WUIButton(constants.createElementChild(), WUIButton.Left.ROUND, WUIButton.Right.PLUS);

		createChild.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				loading.show();
				createChild(new AsyncCallback<String>() {
					public void onFailure(Throwable caught) {
						loading.hide();
						if (caught instanceof InvalidDescriptionLevel) {
							Window.alert("Cannot create child of item level, " + "this button should be disabled");
						} else {
							logger.error("Error creating child", caught);
						}
					}

					public void onSuccess(final String childPID) {
						loading.hide();
						onCreateChild(childPID);
						Browse.getInstance().update(ViewPanel.this.pid, false, true,
								new AsyncCallback<CollectionsTreeItem>() {
							public void onFailure(Throwable caught) {
								logger.error("Error updating tree", caught);
							}

							public void onSuccess(CollectionsTreeItem treeItem) {
								editmode = true;
							}
						});
					}
				});
			}
		});

		createChild.setEnabled(false);

		move = new WUIButton(constants.moveElement(), WUIButton.Left.ROUND, WUIButton.Right.ARROW_FORWARD);

		move.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				getSDO(new AsyncCallback<SimpleDescriptionObject>() {
					public void onFailure(Throwable caught) {
						logger.error("Error getting SDO of " + ViewPanel.this.pid, caught);
					}

					public void onSuccess(SimpleDescriptionObject source) {
						MoveChooseDestinationPanel movePanel = new MoveChooseDestinationPanel(source);
						movePanel.addMoveListener(new MoveListener() {
							public void onCancel() {
								// nothing to do
							}

							public void onMove(String oldParent, String newParentPid) {
								ViewPanel.this.onMove(oldParent, newParentPid);

							}
						});
						movePanel.show();
					}
				});

			}
		});

		// clone = new WUIButton(constants.cloneElement(), WUIButton.Left.ROUND,
		// WUIButton.Right.PLUS);
		//
		// clone.addClickListener(new ClickListener() {
		// public void onClick(Widget sender) {
		// loading.show();
		// cloneElement(new AsyncCallback<String>() {
		//
		// public void onFailure(Throwable caught) {
		// loading.hide();
		// logger.error("Error cloning " + ViewPanel.this.pid,
		// caught);
		// }
		//
		// public void onSuccess(String clonePID) {
		// loading.hide();
		// }
		//
		// });
		// }
		//
		// });

		remove = new WUIButton(constants.removeElement(), WUIButton.Left.ROUND, WUIButton.Right.MINUS);

		remove.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (Window.confirm(constants.confirmElementRemove())) {
					getSDO(new AsyncCallback<SimpleDescriptionObject>() {
						public void onFailure(Throwable caught) {
							logger.error("Error get SDO", caught);

						}

						public void onSuccess(SimpleDescriptionObject sdo) {
							if (sdo.getSubElementsCount() == 0 || Window.confirm(constants.confirmRecursiveRemove())) {
								loading.show();
								remove(new AsyncCallback<Object>() {
									public void onFailure(Throwable caught) {
										loading.hide();
										if (caught instanceof NoSuchRODAObjectException) {
											Window.alert(messages.noSuchRODAObject(ViewPanel.this.pid));
										} else {
											logger.error("Error getting parent of " + ViewPanel.this.pid, caught);
										}
									}

									public void onSuccess(Object result) {
										loading.hide();
									}
								});
							}

						}
					});

				}
			}
		});

		downloadEADC = new WUIButton(constants.downloadEadC(), WUIButton.Left.ROUND, WUIButton.Right.ARROW_DOWN);

		downloadEADC.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				Window.open(GWT.getModuleBaseURL() + "MetadataDownload?type=EAD&pid=" + sdo.getId(), "_blank", "");

			}
		});

		downloadPremis = new WUIButton(constants.downloadPremis(), WUIButton.Left.ROUND, WUIButton.Right.ARROW_DOWN);

		downloadPremis.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				Window.open(GWT.getModuleBaseURL() + "MetadataDownload?type=PREMIS&pid=" + sdo.getId(), "_blank", "");
			}
		});

		close = new WUIButton(constants.close(), WUIButton.Left.ROUND, WUIButton.Right.CROSS);

		close.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				editmode = false;
				close();
			}
		});

		bottomToolbar.add(edit);
		bottomToolbar.add(save);
		bottomToolbar.add(createChild);
		// bottomToolbar.add(clone);
		bottomToolbar.add(move);
		bottomToolbar.add(remove);
		bottomToolbar.add(downloadEADC);
		bottomToolbar.add(downloadPremis);
		bottomToolbar.add(close);

		tabs = new TabPanel();

		listeners = new Vector<ViewListener>();

		init(callback);

		bottomToolbar.setCellWidth(close, "100%");
		bottomToolbar.setCellHorizontalAlignment(close, HasAlignment.ALIGN_RIGHT);

		this.addStyleName("wui-view-panel");
		tabs.addStyleName("viewPanel-tabs");
		bottomToolbar.addStyleName("viewPanel-bottom");
		edit.addStyleName("viewPanel-action");
		save.addStyleName("viewPanel-action");
		createChild.addStyleName("viewPanel-action");
		// clone.addStyleName("viewPanel-action");
		move.addStyleName("viewPanel-action");
		remove.addStyleName("viewPanel-action");
		downloadEADC.addStyleName("viewPanel-action");
		downloadPremis.addStyleName("viewPanel-action");
		close.addStyleName("viewPanel-close");
		optionalFieldsToggle.addStyleName("viewPanel-optionalFieldsToggle");

	}

	private void init(final AsyncCallback<DescriptionObject> callback) {
		descriptiveMetadata = new DescriptiveMetadataPanel(pid, new AsyncCallback<DescriptionObject>() {
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			public void onSuccess(DescriptionObject obj) {
				postInit(obj);
				callback.onSuccess(obj);
			}
		});

	}

	private void postInit(final DescriptionObject obj) {
		// TODO fix this
		// sdo = obj;
		descriptiveMetadata.setOptionalVisible(SHOW_OPTIONAL_FIELDS_DEFAULT);
		updateCreateChildStatus();

		descriptiveMetadataLayout = new VerticalPanel();
		descriptiveMetadataScroll = new ScrollPanel(descriptiveMetadataLayout);
		descriptiveMetadataLayout.add(optionalFieldsToggle);
		descriptiveMetadataLayout.add(descriptiveMetadata);

		if (Main.REPRESENTATION_DESCRIPTION_LEVELS.contains(obj.getLevel())) {
			disseminations = new RepresentationsPanel(obj);
			preservationMetadata = new PreservationMetadataPanel(obj);
		}

		UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<AuthenticatedUser>() {
			public void onFailure(Throwable caught) {
				logger.error("Error getting authenticated user", caught);
			}

			public void onSuccess(AuthenticatedUser user) {
				onUserUpdated(user);
			}
		});

		UserLogin.getInstance().addLoginStatusListener(new LoginStatusListener() {
			public void onLoginStatusChanged(AuthenticatedUser user) {
				onUserUpdated(user);
			}
		});

		descriptiveMetadata.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				updateVisibility();
			}
		});

		tabs.addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {

			public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
				// do nothing
			}
		});
		tabs.addSelectionHandler(new SelectionHandler<Integer>() {

			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				selectedTab = event.getSelectedItem();
				initializeSelectedTab();
				updateVisibility();
			}
		});

		layout.add(tabs, DockPanel.CENTER);
		layout.add(bottomToolbar, DockPanel.SOUTH);

		descriptiveMetadataScroll.addStyleName("viewPanel-description-scroll");
		descriptiveMetadataLayout.addStyleName("viewPanel-description-layout");
	}

	private void onUserUpdated(AuthenticatedUser user) {
		modifyGlobalPermission = user.hasRole("administration.metadata_editor");
		listUsersGlobalPermission = user.hasRole("misc.browse_users");
		EditorService.Util.getInstance().getSelfObjectPermissions(sdo.getId(),
				new AsyncCallback<RODAObjectUserPermissions>() {
					public void onFailure(Throwable caught) {
						if (caught instanceof NoSuchRODAObjectException) {
							// No read permissions
							onCancel();
						} else {
							logger.error("Error getting authenticated user object permissions", caught);
						}

					}

					public void onSuccess(RODAObjectUserPermissions permissions) {
						modifyObjectPermission = permissions.getModify();
						removeObjectPermission = permissions.getRemove();
						grantObjectPermission = permissions.getGrant();

						updateTabs();
						updateVisibility();

					}
				});
	}

	private EditProducersPanel getEditProducersPanel() {
		if (editProducersPanel == null) {
			editProducersPanel = new EditProducersPanel(sdo);
			for (WUIButton action : editProducersPanel.getActionButtons()) {
				action.setVisible(false);
				bottomToolbar.insert(action, bottomToolbar.getWidgetCount() - 2);
				action.addStyleName("viewPanel-action");
			}
		}
		return editProducersPanel;
	}

	private EditObjectPermissionsPanel getObjPermissionsPanel() {
		if (objPermissionsPanel == null) {
			objPermissionsPanel = new EditObjectPermissionsPanel(sdo);
			for (WUIButton action : objPermissionsPanel.getActionButtons()) {
				action.setVisible(false);
				bottomToolbar.insert(action, bottomToolbar.getWidgetCount() - 2);
				action.addStyleName("viewPanel-action");
			}
		}
		return objPermissionsPanel;
	}

	private void updateTabs() {
		tabs.clear();

		if (Main.ROOT_DESCRIPTION_LEVELS.contains(sdo.getLevel())) {
			if (grantObjectPermission && modifyGlobalPermission && listUsersGlobalPermission) {
				// Description
				tabs.add(descriptiveMetadataScroll, constants.descriptiveMetadata());
				// Producers
				tabs.add(getEditProducersPanel(), constants.producers());
				// Permissions
				tabs.add(getObjPermissionsPanel(), constants.objPermissions());

				descriptiveMetadataTabIndex = 0;
				disseminationsTabIndex = -1;
				preservationMetadataTabIndex = -1;
				editProducersTabIndex = 1;
				objPermissionsTabIndex = 2;
			} else if (modifyGlobalPermission && listUsersGlobalPermission) {
				// Description
				tabs.add(descriptiveMetadataScroll, constants.descriptiveMetadata());
				// Producers
				tabs.add(getEditProducersPanel(), constants.producers());

				descriptiveMetadataTabIndex = 0;
				disseminationsTabIndex = -1;
				preservationMetadataTabIndex = -1;
				editProducersTabIndex = 1;
				objPermissionsTabIndex = -1;

			} else {
				// Description
				tabs.add(descriptiveMetadataScroll, constants.descriptiveMetadata());

				descriptiveMetadataTabIndex = 0;
				disseminationsTabIndex = -1;
				preservationMetadataTabIndex = -1;
				editProducersTabIndex = -1;
				objPermissionsTabIndex = -1;
			}

		} else if (!Main.REPRESENTATION_DESCRIPTION_LEVELS.contains(sdo.getLevel())) {
			if (grantObjectPermission && modifyGlobalPermission && listUsersGlobalPermission) {
				// Description
				tabs.add(descriptiveMetadataScroll, constants.descriptiveMetadata());
				// Permissions
				tabs.add(getObjPermissionsPanel(), constants.objPermissions());

				descriptiveMetadataTabIndex = 0;
				disseminationsTabIndex = -1;
				preservationMetadataTabIndex = -1;
				editProducersTabIndex = -1;
				objPermissionsTabIndex = 1;

			} else {
				// Description
				tabs.add(descriptiveMetadataScroll, constants.descriptiveMetadata());

				descriptiveMetadataTabIndex = 0;
				disseminationsTabIndex = -1;
				preservationMetadataTabIndex = -1;
				editProducersTabIndex = -1;
				objPermissionsTabIndex = -1;
			}

		} else {
			if (grantObjectPermission && modifyGlobalPermission && listUsersGlobalPermission) {
				// Description
				tabs.add(descriptiveMetadataScroll, constants.descriptiveMetadata());
				// Disseminations
				tabs.add(disseminations, constants.representations());
				// Preservation Metadata
				tabs.add(preservationMetadata, constants.preservationMetadata());
				// Permissions
				tabs.add(getObjPermissionsPanel(), constants.objPermissions());

				descriptiveMetadataTabIndex = 0;
				disseminationsTabIndex = 1;
				preservationMetadataTabIndex = 2;
				editProducersTabIndex = -1;
				objPermissionsTabIndex = 3;

			} else {
				if (grantObjectPermission && modifyGlobalPermission && listUsersGlobalPermission) {
					// Description
					tabs.add(descriptiveMetadataScroll, constants.descriptiveMetadata());
					// Disseminations
					tabs.add(disseminations, constants.representations());
					// Preservation Metadata
					tabs.add(preservationMetadata, constants.preservationMetadata());
					// Permissions
					tabs.add(getObjPermissionsPanel(), constants.objPermissions());

					descriptiveMetadataTabIndex = 0;
					disseminationsTabIndex = 1;
					preservationMetadataTabIndex = 2;
					editProducersTabIndex = -1;
					objPermissionsTabIndex = 3;

				} else {
					// Description
					tabs.add(descriptiveMetadataScroll, constants.descriptiveMetadata());
					// Disseminations
					tabs.add(disseminations, constants.representations());
					// Preservation Metadata
					tabs.add(preservationMetadata, constants.preservationMetadata());

					descriptiveMetadataTabIndex = 0;
					disseminationsTabIndex = 1;
					preservationMetadataTabIndex = 2;
					editProducersTabIndex = -1;
					objPermissionsTabIndex = -1;
				}

			}
		}

		if (tabs.getTabBar().getTabCount() <= selectedTab) {
			selectedTab = tabs.getTabBar().getTabCount() - 1;
		}
		tabs.selectTab(selectedTab);
		initializeSelectedTab();
	}

	private void initializeSelectedTab() {
		Scheduler.get().scheduleDeferred(new Command() {
			public void execute() {
				if (selectedTab == editProducersTabIndex) {
					getEditProducersPanel().init();
				} else if (selectedTab == disseminationsTabIndex) {
					disseminations.init();
				} else if (selectedTab == preservationMetadataTabIndex) {
					preservationMetadata.init();
				} else if (selectedTab == objPermissionsTabIndex) {
					getObjPermissionsPanel().init();
				}
			}
		});
	}

	/**
	 * Update visible and enabled buttons
	 */
	private void updateVisibility() {
		// General visibility
		edit.setVisible(false);
		save.setVisible(false);
		createChild.setVisible(false);
		// clone.setVisible(false);
		move.setVisible(false);
		remove.setVisible(false);
		downloadEADC.setVisible(false);
		downloadPremis.setVisible(false);
		optionalFieldsToggle.setVisible(false);
		if (editProducersPanel != null) {
			for (WUIButton action : editProducersPanel.getActionButtons()) {
				action.setVisible(false);
			}
		}

		if (objPermissionsPanel != null) {
			for (WUIButton action : objPermissionsPanel.getActionButtons()) {
				action.setVisible(false);
			}
		}

		// Description metadata tab selected
		if (selectedTab == descriptiveMetadataTabIndex) {
			downloadEADC.setVisible(true);
			if (modifyGlobalPermission && modifyObjectPermission && editmode) {
				descriptiveMetadata.setReadonly(false);
				edit.setVisible(false);
				save.setVisible(true);
				descriptiveMetadata.isValid(new AsyncCallback<Boolean>() {
					public void onFailure(Throwable caught) {
						logger.error("Error validating descriptive metadata", caught);
						save.setEnabled(false);
					}

					public void onSuccess(Boolean isValid) {
						if (!isValid) {
							save.setEnabled(false);
						} else {
							descriptiveMetadata.isChanged(new AsyncCallback<Boolean>() {
								public void onFailure(Throwable caught) {
									logger.error("Error checking descriptive " + "metadata for changes", caught);
								}

								public void onSuccess(Boolean isChanged) {
									save.setEnabled(isChanged);
								}
							});
						}
					}
				});

				createChild.setVisible(true);
				// clone.setVisible(true);
				move.setVisible(true);
				// TODO make the option of hide remove button when there are
				// children configurable
				// remove.setVisible(removeObjectPermission
				// && sdo.getSubElementsCount() == 0);
				remove.setVisible(removeObjectPermission);
				optionalFieldsToggle.setVisible(true);
			} else if (modifyGlobalPermission && modifyObjectPermission) {
				if (!descriptiveMetadata.isReadonly()) {
					descriptiveMetadata.cancel();
					save.setEnabled(false);
				}
				descriptiveMetadata.setReadonly(true);
				edit.setVisible(true);
				save.setVisible(false);
				createChild.setVisible(true);
				// clone.setVisible(true);
				move.setVisible(true);

				// TODO make the option of hide remove button when there are
				// children configurable
				remove.setVisible(removeObjectPermission);

				optionalFieldsToggle.setVisible(false);
			}
		} // Edit producer tab selected
		else if (selectedTab == editProducersTabIndex) {
			if (modifyGlobalPermission && listUsersGlobalPermission) {
				for (WUIButton action : getEditProducersPanel().getActionButtons()) {
					action.setVisible(true);
				}
			}
		} // Object Permissions tab selected
		else if (selectedTab == objPermissionsTabIndex) {
			if (grantObjectPermission && modifyGlobalPermission && listUsersGlobalPermission) {
				for (WUIButton action : getObjPermissionsPanel().getActionButtons()) {
					action.setVisible(true);
				}
			}
		} // Preservation Metadata tab selected
		else if (selectedTab == preservationMetadataTabIndex) {
			downloadPremis.setVisible(true);
		}
	}

	private void updateCreateChildStatus() {
		getSDO(new AsyncCallback<SimpleDescriptionObject>() {
			public void onFailure(Throwable caught) {
				logger.error("Error getting SDO of " + ViewPanel.this.pid, caught);
			}

			public void onSuccess(SimpleDescriptionObject sdo) {
				createChild.setEnabled(!Main.LEAF_DESCRIPTION_LEVELS.contains(sdo.getLevel()));
			}
		});
	}

	/**
	 * Close view panel
	 */
	public void close() {
		descriptiveMetadata.isChanged(new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				logger.error("Error checking for changes", caught);
			}

			public void onSuccess(Boolean isChanged) {
				if (isChanged) {
					if (Window.confirm(constants.saveBeforeClosing())) {
						descriptiveMetadata.commit(new AsyncCallback<DescriptionObject>() {
							public void onFailure(Throwable caught) {
								Window.alert(messages.editSaveError(caught.getMessage()));
							}

							public void onSuccess(DescriptionObject obj) {
								ViewPanel.this.onClose();
							}
						});
					} else {
						descriptiveMetadata.cancel();
						onCancel();
					}
				} else {
					onCancel();
				}
			}
		});

	}

	protected void onClose() {
		for (ViewListener listener : listeners) {
			listener.onClose(pid);
		}
	}

	protected void onCancel() {
		for (ViewListener listener : listeners) {
			listener.onCancel(pid);
		}
	}

	protected void onEdit() {
		for (ViewListener listener : listeners) {
			listener.onEdit(pid);
		}
	}

	protected void onSave(DescriptionObject dobj) {
		for (ViewListener listener : listeners) {
			listener.onSave(dobj);
		}
	}

	protected void onCreateChild(String childPid) {
		for (ViewListener listener : listeners) {
			listener.onCreateChild(pid, childPid);
		}
	}

	protected void onClone(String clonePid) {
		for (ViewListener listener : listeners) {
			listener.onClone(pid, clonePid);
		}
	}

	protected void onMove(String oldParentPid, String newParentPid) {
		for (ViewListener listener : listeners) {
			listener.onMove(pid, oldParentPid, newParentPid);
		}
	}

	protected void onRemove(String parentPid) {
		for (ViewListener listener : listeners) {
			listener.onRemove(pid, parentPid);
		}
	}

	/**
	 * Add a new view listener
	 * 
	 * @param listener
	 */
	public void addViewListener(ViewListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a view listener
	 * 
	 * @param listener
	 */
	public void removeViewListener(ViewListener listener) {
		listeners.remove(listener);
	}

	private void createChild(final AsyncCallback<String> callback) {
		getSDO(new AsyncCallback<SimpleDescriptionObject>() {
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			public void onSuccess(SimpleDescriptionObject sdo) {
				String level = sdo.getLevel();
				EditorService.Util.getInstance().createChild(ViewPanel.this.pid, level, new AsyncCallback<String>() {
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}

					public void onSuccess(String childPid) {
						onCreateChild(childPid);
						callback.onSuccess(childPid);

					}
				});
			}
		});
	}

	@SuppressWarnings("unused")
	private void cloneElement(final AsyncCallback<String> callback) {
		EditorService.Util.getInstance().clone(ViewPanel.this.pid, new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			public void onSuccess(final String clonePID) {
				onClone(clonePID);
				callback.onSuccess(clonePID);
			}
		});
	}

	/**
	 * Remove action
	 * 
	 * @param callback
	 */
	public void remove(final AsyncCallback<?> callback) {
		BrowserService.Util.getInstance().getParent(ViewPanel.this.pid, new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			public void onSuccess(final String parentPID) {
				EditorService.Util.getInstance().removeElement(ViewPanel.this.pid, new AsyncCallback<Void>() {
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}

					public void onSuccess(Void result) {
						onRemove(parentPID);
						callback.onSuccess(null);
					}
				});
			}
		});
	}

	/**
	 * Get simple description object
	 * 
	 * @param callback
	 */
	public void getSDO(AsyncCallback<SimpleDescriptionObject> callback) {
		if (sdo != null) {
			callback.onSuccess(sdo);
		} else {
			BrowserService.Util.getInstance().getSimpleDescriptionObject(pid, callback);
		}

	}

	/**
	 * Get object PID
	 * 
	 * @return the PID
	 */
	public String getPID() {
		return pid;
	}

}
