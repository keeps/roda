/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.user.client;

import pt.gov.dgarq.roda.core.data.Group;
import pt.gov.dgarq.roda.core.data.RODAMember;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.wui.common.client.BadHistoryTokenException;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.SuccessListener;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.widgets.AlphabetListItem;
import pt.gov.dgarq.roda.wui.common.client.widgets.ControlPanel;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.common.client.widgets.AlphabetSortedList.AlphabetSortedListListener;
import pt.gov.dgarq.roda.wui.common.client.widgets.ControlPanel.ControlPanelListener;
import pt.gov.dgarq.roda.wui.management.client.Management;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.UserManagementConstants;
import config.i18n.client.UserManagementMessages;

/**
 * @author Luis Faria
 * 
 */
public class WUIUserManagement extends DockPanel implements HistoryResolver {

	private static WUIUserManagement instance = null;

	/**
	 * Get the singleton instance
	 * 
	 * @return the instance
	 */
	public static WUIUserManagement getInstance() {
		if (instance == null) {
			instance = new WUIUserManagement();
		}
		return instance;
	}

	private ClientLogger logger = new ClientLogger(getClass().getName());

	/**
	 * WUI user management constants class
	 */
	public static class WUIUserManagementConstant {
		private WUIUserManagementConstant() {
		}
	}

	private static final WUIUserManagementConstant LIST_USERS = new WUIUserManagementConstant();

	private static final WUIUserManagementConstant LIST_GROUPS = new WUIUserManagementConstant();

	private static UserManagementConstants constants = (UserManagementConstants) GWT
			.create(UserManagementConstants.class);

	private static UserManagementMessages messages = (UserManagementMessages) GWT.create(UserManagementMessages.class);

	private boolean initialized;

	private UserAlphabetList userList;

	private GroupAlphabetList groupList;

	private ControlPanel controlPanel;

	private WUIUserManagementConstant listType;

	private Label controlActionUserLabel;

	private WUIButton controlActionReport;

	private WUIButton controlActionCreateUser;

	private WUIButton controlActionEditUser;

	private WUIButton controlActionSetActive;

	private WUIButton controlActionSetInactive;

	private WUIButton controlActionRemoveUser;

	private Label controlActionGroupLabel;

	private WUIButton controlActionCreateGroup;

	private WUIButton controlActionEditGroup;

	private WUIButton controlActionRemoveGroup;

	private WUIUserManagement() {
		initialized = false;
	}

	private void init() {
		if (!initialized) {
			initialized = true;

			userList = new UserAlphabetList();
			groupList = new GroupAlphabetList();

			userList.addAlphabetSortedListListener(new AlphabetSortedListListener() {

				public void onItemSelect(AlphabetListItem item) {
					if (item == null) {
						updateVisibility(null);
					} else if (listType == LIST_USERS && item instanceof UserDisclosurePanel) {
						UserDisclosurePanel userPanel = (UserDisclosurePanel) item;
						userPanel.getSelected(new AsyncCallback<RODAMember>() {

							public void onFailure(Throwable caught) {
								logger.error("Error getting selected member", caught);
							}

							public void onSuccess(RODAMember selected) {
								updateVisibility(selected);
							}

						});
					}

				}

			});

			groupList.addAlphabetSortedListListener(new AlphabetSortedListListener() {

				public void onItemSelect(AlphabetListItem item) {
					if (item == null) {
						updateVisibility(null);
					} else if (listType == LIST_GROUPS && item instanceof GroupDisclosurePanel) {
						GroupDisclosurePanel groupPanel = (GroupDisclosurePanel) item;
						groupPanel.getSelected(new AsyncCallback<RODAMember>() {

							public void onFailure(Throwable caught) {
								logger.error("Error getting selected member", caught);
							}

							public void onSuccess(RODAMember selected) {
								updateVisibility(selected);
							}

						});
					}

				}

			});

			controlPanel = new ControlPanel();

			this.initControlPanel();
			updateVisibility(null);

			this.add(userList, CENTER);
			this.setCellWidth(userList, "100%");

			this.add(controlPanel.getWidget(), EAST);
			this.addStyleName("UserManagement");
		}
	}

	private void initControlPanel() {
		controlPanel.setTitle(constants.list());
		controlPanel.setSearchTitle(constants.search());
		controlPanel.addOption(constants.users());
		controlPanel.addOption(constants.groups());
		controlPanel.setSelectedOptionIndex(0);

		controlPanel.addControlPanelListener(new ControlPanelListener() {

			public void onOptionSelected(int option) {
				switch (option) {
				case 1:
					History.newItem("administration.user.groups");
					break;
				case 0:
				default:
					History.newItem("administration.user.users");
					break;
				}

			}

			public void onSearch(String keywords) {
				if (listType == LIST_USERS) {
					userList.setFilter(keywords);
					logger.debug("Setting filter for users");
				} else if (listType == LIST_GROUPS) {
					groupList.setFilter(keywords);
					logger.debug("Setting filter for groups");
				}
			}

		});

		controlActionUserLabel = new Label(constants.userActions());

		controlActionReport = new WUIButton(constants.report(), WUIButton.Left.SQUARE, WUIButton.Right.REPORT);

		controlActionCreateUser = new WUIButton(constants.createUser(), WUIButton.Left.SQUARE, WUIButton.Right.PLUS);

		controlActionEditUser = new WUIButton(constants.editUser(), WUIButton.Left.SQUARE,
				WUIButton.Right.ARROW_FORWARD);

		controlActionSetActive = new WUIButton(constants.setActive(), WUIButton.Left.SQUARE,
				WUIButton.Right.ARROW_FORWARD);
		controlActionSetInactive = new WUIButton(constants.setInactive(), WUIButton.Left.SQUARE,
				WUIButton.Right.ARROW_FORWARD);
		controlActionRemoveUser = new WUIButton(constants.removeUser(), WUIButton.Left.SQUARE, WUIButton.Right.CROSS);

		controlActionGroupLabel = new Label(constants.groupActions());

		controlActionCreateGroup = new WUIButton(constants.createGroup(), WUIButton.Left.SQUARE, WUIButton.Right.PLUS);

		controlActionCreateGroup = new WUIButton(constants.createGroup(), WUIButton.Left.SQUARE, WUIButton.Right.PLUS);

		controlActionEditGroup = new WUIButton(constants.editGroup(), WUIButton.Left.SQUARE,
				WUIButton.Right.ARROW_FORWARD);

		controlActionRemoveGroup = new WUIButton(constants.removeGroup(), WUIButton.Left.SQUARE, WUIButton.Right.CROSS);

		controlPanel.addActionWidget(controlActionUserLabel);
		controlPanel.addActionButton(controlActionReport);
		controlPanel.addActionButton(controlActionCreateUser);
		controlPanel.addActionButton(controlActionEditUser);
		controlPanel.addActionButton(controlActionSetActive);
		controlPanel.addActionButton(controlActionSetInactive);
		controlPanel.addActionButton(controlActionRemoveUser);

		controlPanel.addActionWidget(controlActionGroupLabel);
		controlPanel.addActionButton(controlActionCreateGroup);
		controlPanel.addActionButton(controlActionEditGroup);
		controlPanel.addActionButton(controlActionRemoveGroup);

		controlActionReport.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				AsyncCallback<RODAMember> callback = new AsyncCallback<RODAMember>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting selected RODA member", caught);

					}

					public void onSuccess(RODAMember selected) {
						if (selected != null && selected instanceof User) {
							ActionReportWindow actionReportWindow = new ActionReportWindow((User) selected);
							actionReportWindow.show();
						} else {
							Window.alert(messages.actionReportNoUser());
						}
					}

				};
				if (listType == LIST_USERS) {
					userList.getSelected(callback);
				} else {
					groupList.getSelected(callback);
				}

			}

		});

		controlActionCreateUser.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				CreateUser createUserPanel = new CreateUser();
				createUserPanel.show();
				createUserPanel.addSuccessListener(new SuccessListener() {

					public void onCancel() {
						// do nothing
					}

					public void onSuccess() {
						if (listType == LIST_USERS) {
							userList.update();
						} else {
							groupList.update();
						}
					}

				});

			}

		});

		controlActionCreateGroup.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				CreateGroup createGroupPanel = new CreateGroup();
				createGroupPanel.show();
				createGroupPanel.addSuccessListener(new SuccessListener() {

					public void onCancel() {
						// do nothing

					}

					public void onSuccess() {
						if (listType == LIST_USERS) {
							userList.update();
						} else {
							groupList.update();
						}
					}

				});
			}

		});

		controlActionEditUser.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				AsyncCallback<RODAMember> callback = new AsyncCallback<RODAMember>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting selected RODA member", caught);
					}

					public void onSuccess(RODAMember selected) {
						if (selected != null && selected instanceof User) {
							editRodaMember(selected);
						} else {
							Window.alert(constants.selectNoUser());
						}
					}

				};
				if (listType == LIST_USERS) {
					userList.getSelected(callback);

				} else {
					groupList.getSelected(callback);
				}

			}

		});

		controlActionSetActive.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				AsyncCallback<RODAMember> callback = new AsyncCallback<RODAMember>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting selected RODA member", caught);
					}

					public void onSuccess(RODAMember selected) {
						if (selected != null && selected instanceof User && !((User) selected).isActive()) {
							setUserActive((User) selected, true);
						} else {
							Window.alert(constants.selectNoInactiveUser());
						}
					}
				};
				if (listType == LIST_USERS) {
					userList.getSelected(callback);

				} else {
					groupList.getSelected(callback);
				}

			}

		});

		controlActionSetInactive.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				AsyncCallback<RODAMember> callback = new AsyncCallback<RODAMember>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting selected RODA member", caught);
					}

					public void onSuccess(RODAMember selected) {
						if (selected != null && selected instanceof User && ((User) selected).isActive()) {
							setUserActive((User) selected, false);
						} else {
							Window.alert(constants.selectNoActiveUser());
						}
					}
				};
				if (listType == LIST_USERS) {
					userList.getSelected(callback);

				} else {
					groupList.getSelected(callback);
				}

			}

		});

		controlActionRemoveUser.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				AsyncCallback<RODAMember> callback = new AsyncCallback<RODAMember>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting selected RODA member", caught);
					}

					public void onSuccess(RODAMember selected) {
						if (selected != null && selected instanceof User) {
							removeMember(selected);
						} else {
							Window.alert(constants.selectNoUser());
						}
					}

				};
				if (listType == LIST_USERS) {
					userList.getSelected(callback);

				} else {
					groupList.getSelected(callback);
				}
			}

		});

		controlActionEditGroup.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				AsyncCallback<RODAMember> callback = new AsyncCallback<RODAMember>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting selected RODA member", caught);
					}

					public void onSuccess(RODAMember selected) {
						if (selected != null && selected instanceof Group) {
							editRodaMember(selected);
						} else {
							Window.alert(constants.selectNoGroup());
						}
					}

				};
				if (listType == LIST_USERS) {
					userList.getSelected(callback);

				} else {
					groupList.getSelected(callback);
				}
			}

		});

		controlActionRemoveGroup.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				AsyncCallback<RODAMember> callback = new AsyncCallback<RODAMember>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting selected RODA member", caught);
					}

					public void onSuccess(RODAMember selected) {
						if (selected != null && selected instanceof Group) {
							removeMember(selected);
						} else {
							Window.alert(constants.selectNoGroup());
						}
					}

				};
				if (listType == LIST_USERS) {
					userList.getSelected(callback);

				} else {
					groupList.getSelected(callback);
				}
			}

		});

		controlActionUserLabel.addStyleName("wui-management-user-actions-title");
		controlActionGroupLabel.addStyleName("wui-management-user-actions-title");
	}

	/**
	 * Set if the user management list is grouped by users or groups
	 * 
	 * @param type
	 *            the list type, i.e. grouped by users or groups
	 */
	public void setListType(WUIUserManagementConstant type) {
		if (this.listType != type) {
			if (type == LIST_USERS) {
				this.remove(groupList);
				this.add(userList, CENTER);
				this.setCellWidth(userList, "100%");
				listType = LIST_USERS;
				controlPanel.setSelectedOptionIndex(0);
				userList.update();
			} else /* if(type == LIST_GROUPS) */ {
				this.remove(userList);
				this.add(groupList, CENTER);
				this.setCellWidth(groupList, "100%");
				listType = LIST_GROUPS;
				controlPanel.setSelectedOptionIndex(1);
				groupList.update();
			}
			updateVisibility(null);
		}
	}

	protected void updateVisibility(RODAMember selected) {
		if (selected == null) {
			controlActionReport.setEnabled(false);
			controlActionEditUser.setEnabled(false);
			controlActionSetActive.setVisible(false);
			controlActionSetInactive.setVisible(true);
			controlActionSetInactive.setEnabled(false);
			controlActionRemoveUser.setEnabled(false);
			controlActionEditGroup.setEnabled(false);
			controlActionRemoveGroup.setEnabled(false);
		} else if (selected instanceof User) {
			controlActionReport.setEnabled(true);
			controlActionEditUser.setEnabled(true);
			controlActionRemoveUser.setEnabled(true);
			controlActionEditGroup.setEnabled(false);
			controlActionRemoveGroup.setEnabled(false);
			if (((User) selected).isActive()) {
				controlActionSetActive.setVisible(false);
				controlActionSetInactive.setVisible(true);
				controlActionSetInactive.setEnabled(true);
			} else {
				controlActionSetActive.setVisible(true);
				controlActionSetActive.setEnabled(true);
				controlActionSetInactive.setVisible(false);
			}
		} else if (selected instanceof Group) {
			controlActionReport.setEnabled(false);
			controlActionEditUser.setEnabled(false);
			controlActionRemoveUser.setEnabled(false);
			controlActionSetActive.setVisible(false);
			controlActionSetInactive.setVisible(true);
			controlActionSetInactive.setEnabled(false);
			controlActionEditGroup.setEnabled(true);
			controlActionRemoveGroup.setEnabled(true);
		}
	}

	/**
	 * Show panels that allow the RODA member to be edited
	 * 
	 * @param member
	 *            the RODA member to be edited
	 */
	public void editRodaMember(final RODAMember member) {
		if (member != null && member instanceof User) {
			final User user = (User) member;
			EditUser editUserPanel = new EditUser(user);
			editUserPanel.show();
			editUserPanel.addSuccessListener(new SuccessListener() {

				public void onCancel() {
					// do nothing
				}

				public void onSuccess() {
					if (listType == LIST_USERS) {
						userList.update();
					} else {
						groupList.update();
					}
					UserLogin.getInstance().permissionsChanged(member);
				}

			});
		} else if (member != null && member instanceof Group) {
			final Group group = (Group) member;
			EditGroup editGroupPanel = new EditGroup(group);
			editGroupPanel.show();
			editGroupPanel.addSuccessListener(new SuccessListener() {

				public void onCancel() {
					// do nothing
				}

				public void onSuccess() {
					if (listType == LIST_USERS) {
						userList.update();
					} else {
						groupList.update();
					}
					UserLogin.getInstance().permissionsChanged(member);
				}

			});
		} else {
			Window.alert(constants.selectNoUserOrGroup());
		}
	}

	/**
	 * Set a user active or inactive
	 * 
	 * @param user
	 *            the target user
	 * @param active
	 *            true to set the user active, false to set inactive
	 */
	public void setUserActive(final User user, final boolean active) {
		if (user.isActive() != active) {
			user.setActive(active);
			UserManagementService.Util.getInstance().editUser(user, null, new AsyncCallback<Void>() {

				public void onFailure(Throwable caught) {
					logger.error("Error setting user " + user.getName() + (active ? " active" : " inactive"), caught);
				}

				public void onSuccess(Void result) {
					if (listType == LIST_USERS) {
						userList.update();
					} else {
						groupList.update();
					}
				}

			});
		} else {
			logger.warn("Tryed to re-apply " + (active ? "active" : "inactive") + " status of user " + user.getName());
		}
	}

	/**
	 * Show panels that allow a RODAMember to be removed
	 * 
	 * @param member
	 *            the RODA member to be removed
	 */
	public void removeMember(final RODAMember member) {
		if (member instanceof User && Window.confirm(messages.removeUserConfirm(member.getName()))) {
			UserManagementService.Util.getInstance().removeUser(member.getName(), new AsyncCallback<Boolean>() {

				public void onFailure(Throwable caught) {
					Window.alert(messages.removeUserFailure(member.getName(), caught.getMessage()));
				}

				public void onSuccess(Boolean removed) {
					userList.update();
					if (!removed.booleanValue()) {
						Window.alert(messages.removeUserNotPossible(member.getName()));
					}

				}

			});
		} else if (member instanceof Group && Window.confirm(messages.removeGroupConfirm(member.getName()))) {
			UserManagementService.Util.getInstance().removeGroup(member.getName(), new AsyncCallback<Void>() {

				public void onFailure(Throwable caught) {
					Window.alert(messages.removeGroupFailure(member.getName(), caught.getMessage()));
				}

				public void onSuccess(Void result) {
					groupList.update();
				}

			});
		}
	}

	public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
		UserLogin.getInstance().checkRole(this, callback);

	}

	public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
		if (historyTokens.length == 0) {
			History.newItem(getHistoryPath() + ".users");
			callback.onSuccess(null);
		} else if (historyTokens.length == 1) {
			if (historyTokens[0].equals("users")) {
				callback.onSuccess(this);
				init();
				setListType(LIST_USERS);
			} else if (historyTokens[0].equals("groups")) {
				callback.onSuccess(this);
				init();
				setListType(LIST_GROUPS);
			} else {
				callback.onFailure(new BadHistoryTokenException(historyTokens[0]));
			}
		} else {
			History.newItem(getHistoryPath() + ".users");
			callback.onSuccess(null);
		}
	}

	public String getHistoryPath() {
		return Management.RESOLVER.getHistoryPath() + "." + getHistoryToken();
	}

	public String getHistoryToken() {
		return "user";
	}

}
