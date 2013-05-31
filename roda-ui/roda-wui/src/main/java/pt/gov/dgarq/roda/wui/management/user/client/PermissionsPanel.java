/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.user.client;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Vector;

import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.widgets.LoadingPopup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.google.gwt.user.client.ui.SourcesClickEvents;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.UserManagementConstants;

/**
 * @author Luis Faria
 * 
 */
public class PermissionsPanel extends VerticalPanel implements
		SourcesChangeEvents {

	private final List<ChangeListener> changelisteners;

	private class Permission extends HorizontalPanel implements
			SourcesClickEvents, Comparable<Permission> {

		// functional attributes
		private final String sortingkeyword;

		private final String role;

		private boolean locked;

		private boolean enabled;

		// UI attributes
		private final CheckBox checkbox;

		private final Label descriptionLabel;

		public Permission(String role, String description, String sortingkeyword) {
			this.role = role;
			this.checkbox = new CheckBox();
			this.descriptionLabel = new Label(description);
			this.sortingkeyword = sortingkeyword;
			this.add(checkbox);
			this.add(descriptionLabel);
			this.locked = false;
			this.enabled = true;

			this.descriptionLabel.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					if (enabled) {
						checkbox.setChecked(!checkbox.isChecked());
					}
				}

			});

			this.addStyleName("permission");
			checkbox.addStyleName("permission-checkbox");
			descriptionLabel.setStylePrimaryName("permission-description");
		}

		public boolean isLocked() {
			return locked;
		}

		public void setLocked(boolean locked) {
			this.locked = locked;
			checkbox.setEnabled(!locked);

		}

		public boolean isChecked() {
			return checkbox.isChecked();
		}

		public void setChecked(boolean checked) {
			checkbox.setChecked(checked);
		}

		public String getRole() {
			return role;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
			if (!locked) {
				this.checkbox.setEnabled(enabled);
				if (enabled) {
					this.descriptionLabel.removeStyleDependentName("off");
					this.descriptionLabel.addStyleDependentName("on");
				} else {
					this.descriptionLabel.removeStyleDependentName("on");
					this.descriptionLabel.addStyleDependentName("off");
				}
			}
		}

		public void addClickListener(ClickListener listener) {
			checkbox.addClickListener(listener);
			descriptionLabel.addClickListener(listener);
		}

		public void removeClickListener(ClickListener listener) {
			checkbox.removeClickListener(listener);
			descriptionLabel.removeClickListener(listener);

		}

		public int compareTo(Permission permission0) {
			return sortingkeyword.compareTo(permission0.sortingkeyword);
		}

		public String getSortingkeyword() {
			return sortingkeyword;
		}

	}

	private static UserManagementConstants constants = (UserManagementConstants) GWT
			.create(UserManagementConstants.class);

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private final List<Permission> permissions;

	private boolean enabled;

	private final LoadingPopup loading;

	/**
	 * 
	 */
	public PermissionsPanel() {

		this.changelisteners = new Vector<ChangeListener>();
		this.permissions = new Vector<Permission>();
		loading = new LoadingPopup(this);
		logger.debug("Getting permissions from RODA properties");
		loading.show();

		UserLogin.getRodaProperties(new AsyncCallback<Map<String, String>>() {

			public void onFailure(Throwable caught) {
				loading.hide();
				logger.fatal("Error getting RODA properties", caught);
			}

			public void onSuccess(Map<String, String> rodaProperties) {
				logger.debug("Creating permissions list");
				for (String key : rodaProperties.keySet()) {
					if (key.startsWith("role.")) {
						String role = (String) rodaProperties.get(key);
						String description;
						try {
							description = constants.getString(key.replace('.',
									'_'));
						} catch (MissingResourceException e) {
							description = role + " (needs translation)";
						}

						Permission permission = new Permission(role,
								description, (String) key);
						permissions.add(permission);
					}
				}

				logger.debug("Sorting permissions list");
				Collections.sort(permissions);

				logger.debug("Adding permissions to panel");
				for (final Permission permission : permissions) {
					PermissionsPanel.this.add(permission);
					permission.addClickListener(new ClickListener() {
						public void onClick(Widget sender) {
							if (permission.isEnabled()) {
								onChange();
							}
						}

					});
				}
				loading.hide();
			}

		});

		this.enabled = true;

		this.addStyleName("permissions");
	}

	/**
	 * Set all permissions defined by roles checked and set locked with
	 * parameters
	 * 
	 * @param roles
	 *            roles of the permissions to check
	 * @param lock
	 *            if permissions should also be locked
	 */
	public void checkPermissions(String[] roles, boolean lock) {
		for (int i = 0; i < roles.length; i++) {
			String role = roles[i];
			boolean foundit = false;
			for (Iterator<Permission> j = permissions.iterator(); j.hasNext()
					&& !foundit;) {
				Permission p = j.next();
				if (p.getRole().equals(role)) {
					foundit = true;
					p.setChecked(true);
					p.setLocked(lock);
				}
			}
		}
	}

	public void clear() {
		for (Permission p : permissions) {
			p.setChecked(false);
			p.setLocked(false);
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		for (Permission p : permissions) {
			p.setEnabled(enabled);
		}
	}

	public void updateLockedPermissions(String[] memberGroups) {
		if (memberGroups.length > 0) {
			logger.debug("Getting group permissions");
			this.setEnabled(false);
			loading.show();
			UserManagementService.Util.getInstance().getGroupsRoles(
					memberGroups, new AsyncCallback<String[]>() {

						public void onFailure(Throwable caught) {
							loading.hide();
							logger.error("Error while getting member"
									+ "groups permissions", caught);
						}

						public void onSuccess(String[] inheritedRoles) {
							logger.info("got " + inheritedRoles.length
									+ " permissions to add");

							// unlock all
							for (Permission p : permissions) {
								p.setLocked(false);

							}
							// Lock inherited roles
							checkPermissions(inheritedRoles, true);

							PermissionsPanel.this.setEnabled(true);
							loading.hide();
						}

					});
		}

	}

	/**
	 * Get roles that are directly defined, i.e. are not inherited
	 * 
	 * @return
	 */
	public String[] getDirectRoles() {
		List<Permission> checkedPermissions = new Vector<Permission>();
		for (Permission p : permissions) {
			if (p.isChecked() && !p.isLocked()) {
				checkedPermissions.add(p);
			}
		}
		String[] specialRoles = new String[checkedPermissions.size()];
		for (int i = 0; i < checkedPermissions.size(); i++) {
			specialRoles[i] = ((Permission) checkedPermissions.get(i))
					.getRole();
		}
		return specialRoles;
	}

	protected void onChange() {
		for (ChangeListener listener : changelisteners) {
			listener.onChange(this);
		}
	}

	public void addChangeListener(ChangeListener listener) {
		this.changelisteners.add(listener);

	}

	public void removeChangeListener(ChangeListener listener) {
		this.changelisteners.remove(listener);
	}

}
