/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.user.client;

import pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.NoSuchUserException;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIWindow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.UserManagementConstants;
import config.i18n.client.UserManagementMessages;

/**
 * @author Luis Faria
 * 
 */
public class EditUser extends WUIWindow {

	private final User user;

	private static UserManagementConstants constants = (UserManagementConstants) GWT
			.create(UserManagementConstants.class);

	private static UserManagementMessages messages = (UserManagementMessages) GWT
			.create(UserManagementMessages.class);

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private final WUIButton apply;

	private final WUIButton cancel;

	private final UserDataPanel userDataPanel;

	private final PermissionsPanel permissionsPanel;

	/**
	 * Create a new panel to edit a user
	 * 
	 * @param user
	 *            the user to edit
	 */
	public EditUser(User user) {
		super(constants.editUserTitle(), 699, 630);
		this.user = user;
		apply = new WUIButton(constants.editUserApply(),
				WUIButton.Left.ROUND,
				WUIButton.Right.REC);

		cancel = new WUIButton(constants.editUserCancel(),
				WUIButton.Left.ROUND,
				WUIButton.Right.CROSS);

		apply.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				final User user = userDataPanel.getUser();
				final String password = userDataPanel.getPassword();

				final String[] specialroles = permissionsPanel
						.getDirectRoles();
				user.setDirectRoles(specialroles);

				UserManagementService.Util.getInstance().editUser(user,
						password, new AsyncCallback() {

							public void onFailure(Throwable caught) {
								if (caught instanceof NoSuchUserException) {
									Window.alert(messages.editUserNotFound(user
											.getName()));
									EditUser.this.cancel();
								} else if (caught instanceof EmailAlreadyExistsException) {
									Window.alert(messages
											.editUserEmailAlreadyExists(user
													.getEmail()));
									EditUser.this.cancel();
								} else {
									Window.alert(messages.editUserFailure(
											EditUser.this.user.getName(),
											caught.getMessage()));
								}
							}

							public void onSuccess(Object result) {
								EditUser.this.hide();
								EditUser.this.onSuccess();

							}

						});
			}

		});

		cancel.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				EditUser.this.cancel();
			}

		});

		this.addToBottom(apply);
		this.addToBottom(cancel);

		this.userDataPanel = new UserDataPanel(false, true, true);
		this.permissionsPanel = new PermissionsPanel();

		apply.setEnabled(false);

		userDataPanel.setUsernameReadOnly(true);
		userDataPanel.addChangeListener(new ChangeListener() {

			public void onChange(Widget sender) {
				apply.setEnabled(userDataPanel.isValid());
			}

		});

		permissionsPanel.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				apply.setEnabled(userDataPanel.isValid());
			}
		});

		this.addTab(userDataPanel, constants.dataTabTitle());
		this.addTab(permissionsPanel, constants.permissionsTabTitle());

		this.getTabPanel().addTabListener(new TabListener() {

			public boolean onBeforeTabSelected(SourcesTabEvents sender,
					int tabIndex) {
				if (tabIndex == 1) {
					permissionsPanel.updateLockedPermissions(userDataPanel
							.getMemberGroups());
				}
				return true;
			}

			public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
			}

		});

		this.selectTab(0);

		this.init();

		getTabPanel().addStyleName("office-edit-user-tabpanel");
	}

	private void init() {
		userDataPanel.setUser(user);
		userDataPanel.setVisible(true);

		UserManagementService.Util.getInstance().getUserDirectRoles(
				user.getName(), new AsyncCallback<String[]>() {

					public void onFailure(Throwable caught) {
						logger.error("Error while getting "
								+ EditUser.this.user.getName() + " roles",
								caught);
					}

					public void onSuccess(String[] directRoles) {
						permissionsPanel.checkPermissions(directRoles, false);
						permissionsPanel.setEnabled(true);
					}

				});

	}

	private void cancel() {
		this.hide();
		super.onCancel();
	}

}
