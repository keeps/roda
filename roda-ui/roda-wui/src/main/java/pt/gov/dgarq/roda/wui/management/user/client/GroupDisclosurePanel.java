/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.user.client;

import java.util.HashMap;
import java.util.Map;

import pt.gov.dgarq.roda.core.data.Group;
import pt.gov.dgarq.roda.core.data.RODAMember;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.widgets.AlphabetListItem;
import pt.gov.dgarq.roda.wui.management.user.client.images.UserManagementImageBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.DisclosureEvent;
import com.google.gwt.user.client.ui.DisclosureHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class GroupDisclosurePanel extends HorizontalPanel implements
		AlphabetListItem {

	private static UserManagementImageBundle userManagementImageBundle = (UserManagementImageBundle) GWT
			.create(UserManagementImageBundle.class);

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private final Group group;

	private final DisclosurePanel disclosurePanel;

	private final HorizontalPanel header;

	private Image groupIcon;

	private Label groupNameLabel;

	private Label fullNameLabel;

	private final VerticalPanel memberList;

	private UserMiniPanel selectedUserPanel;

	private GroupMiniPanel selectedGroupPanel;

	private Map<ChangeListener, DisclosureHandler> changeToDisclosureEvents;

	/**
	 * Create a new group disclosure panel
	 * 
	 * @param group
	 */
	public GroupDisclosurePanel(Group group) {
		this.group = group;

		this.header = createHeader();
		this.disclosurePanel = new DisclosurePanel(header);
		this.memberList = new VerticalPanel();
		selectedUserPanel = null;
		selectedGroupPanel = null;

		changeToDisclosureEvents = new HashMap<ChangeListener, DisclosureHandler>();

		this.add(disclosurePanel);
		this.disclosurePanel.setContent(memberList);
		this.disclosurePanel.addEventHandler(new DisclosureHandler() {
			private boolean loaded = false;

			public void onClose(DisclosureEvent arg0) {
				if (selectedGroupPanel != null) {
					selectedGroupPanel.setSelected(false);
					selectedGroupPanel = null;
				}
				if (selectedUserPanel != null) {
					selectedUserPanel.setSelected(false);
					selectedUserPanel = null;
				}
			}

			public void onOpen(DisclosureEvent arg0) {
				if (!loaded) {
					loaded = true;
					addMembers();

				}
			}

		});

		this.addStyleName("wui-GroupDisclosurePanel");
		memberList.addStyleName("wui-UserList");
	}

	private HorizontalPanel createHeader() {
		HorizontalPanel layout = new HorizontalPanel();
		groupIcon = userManagementImageBundle.group().createImage();
		groupNameLabel = new Label(group.getName());
		fullNameLabel = new Label(group.getFullName());

		layout.add(groupIcon);
		layout.add(groupNameLabel);
		layout.add(fullNameLabel);

		layout.addStyleName("group-header");
		groupIcon.addStyleName("group-icon");
		groupNameLabel.addStyleName("group-name");
		fullNameLabel.addStyleName("group-fullname");

		return layout;
	}

	/**
	 * Get the group defined by this panel
	 * 
	 * @return the group
	 */
	public Group getGroup() {
		return group;
	}

	protected void addMembers() {
		String[] subgroups = group.getMemberGroupNames();
		for (int i = 0; i < subgroups.length; i++) {
			String groupname = subgroups[i];
			final GroupMiniPanel groupMiniPanel = new GroupMiniPanel(groupname);
			groupMiniPanel.addChangeListener(new ChangeListener() {

				public void onChange(Widget sender) {
					if (selectedGroupPanel != null) {
						selectedGroupPanel.setSelected(false);
					}
					if (selectedUserPanel != null) {
						selectedUserPanel.setSelected(false);
						selectedUserPanel = null;
					}
					if (groupMiniPanel.isSelected()) {
						selectedGroupPanel = groupMiniPanel;
					} else {
						selectedGroupPanel = null;
					}
					GroupDisclosurePanel.this.onChange();
				}

			});
			memberList.add(groupMiniPanel.getWidget());
		}
		UserManagementService.Util.getInstance().getGroupUsers(group.getName(),
				new AsyncCallback<User[]>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting group users", caught);
					}

					public void onSuccess(User[] users) {
						logger.debug("Adding " + users.length + " users to "
								+ group.getName());
						for (int i = 0; i < users.length; i++) {
							User user = users[i];
							final UserMiniPanel userPanel = new UserMiniPanel(
									user);
							userPanel.addChangeListener(new ChangeListener() {

								public void onChange(Widget sender) {
									if (selectedGroupPanel != null) {
										selectedGroupPanel.setSelected(false);
										selectedGroupPanel = null;
									}
									if (selectedUserPanel != null) {
										selectedUserPanel.setSelected(false);
									}
									if (userPanel.isSelected()) {
										selectedUserPanel = userPanel;
									} else {
										selectedUserPanel = null;
									}
									GroupDisclosurePanel.this.onChange();
								}

							});
							memberList.add(userPanel.getWidget());
						}
					}

				});
	}

	public Widget getWidget() {
		return this;
	}

	public boolean isSelected() {
		return disclosurePanel.isOpen();
	}

	public void setSelected(boolean selected) {
		disclosurePanel.setOpen(selected);
	}

	/**
	 * Get the selected RODA member
	 * 
	 * @param callback
	 *            handle the group if disclosure opened, the user or group if
	 *            any selected, or null if disclosure closed
	 */
	public void getSelected(final AsyncCallback<RODAMember> callback) {
		if (isSelected() && selectedUserPanel == null
				&& selectedGroupPanel == null) {
			callback.onSuccess(group);
		} else if (isSelected() && selectedGroupPanel == null) {
			callback.onSuccess(selectedUserPanel.getUser());
		} else if (isSelected() && selectedUserPanel == null) {
			selectedGroupPanel.getGroup(new AsyncCallback<Group>() {

				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}

				public void onSuccess(Group group) {
					callback.onSuccess(group);
				}
				
			});
		} else {
			callback.onSuccess(null);
		}
	}

	public void addChangeListener(final ChangeListener listener) {
		DisclosureHandler disclosureHandler = new DisclosureHandler() {

			public void onClose(DisclosureEvent event) {
				listener.onChange(GroupDisclosurePanel.this);
			}

			public void onOpen(DisclosureEvent event) {
				listener.onChange(GroupDisclosurePanel.this);
			}

		};

		this.disclosurePanel.addEventHandler(disclosureHandler);
		this.changeToDisclosureEvents.put(listener, disclosureHandler);
	}

	public void removeChangeListener(ChangeListener listener) {
		DisclosureHandler disclosureHandler = (DisclosureHandler) changeToDisclosureEvents
				.get(listener);
		this.disclosurePanel.removeEventHandler(disclosureHandler);
		this.changeToDisclosureEvents.remove(listener);

	}

	protected void onChange() {
		for(ChangeListener listener : changeToDisclosureEvents.keySet()) {
			listener.onChange(getWidget());
		}
	}

	public String getKeyword() {
		return group.getFullName();
	}

	public boolean matches(String regex) {
		return group.getName().matches(regex)
				|| group.getFullName().matches(regex);
	}

}
