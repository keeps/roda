/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.user.client;

import pt.gov.dgarq.roda.core.data.v2.RODAMember;
import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.widgets.AlphabetListItem;
import pt.gov.dgarq.roda.wui.common.client.widgets.AlphabetSortedList;
import pt.gov.dgarq.roda.wui.common.client.widgets.LoadingPopup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.UserManagementMessages;

/**
 * @author Luis Faria
 * 
 */
public class UserAlphabetList extends AlphabetSortedList {

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private final UserManagementServiceAsync userManagementService;

	private static UserManagementMessages messages = (UserManagementMessages) GWT
			.create(UserManagementMessages.class);

	private String filter;

	private Character letter;

	private boolean initialized;

	/**
	 * Create a new alphabet list of users
	 * 
	 */
	public UserAlphabetList() {
		super();
		this.userManagementService = UserManagementService.Util.getInstance();
		filter = "";
		letter = null;
		initialized = false;
	}

	/**
	 * Refresh the user alphabet list, reloading all user info
	 * 
	 */
	public void update() {
		final LoadingPopup loading = new LoadingPopup(this);
		loading.show();
		this.clear();
		this.userManagementService.getUserCount(letter, filter,
				new AsyncCallback<Integer>() {
					public void onFailure(Throwable caught) {
						logger.error("Error while updating user list", caught);
						loading.hide();
					}

					public void onSuccess(Integer userCount) {
						if (!initialized) {
							init(userCount);
							initialized = true;
						} else {
							update(userCount);
						}
						loading.hide();
					}
				});

	}

	public void getItems(int firstItem, int limit,
			final AsyncCallback<AlphabetListItem[]> callback) {
		userManagementService.getUsers(letter, filter, firstItem, limit,
				new AsyncCallback<User[]>() {

					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}

					public void onSuccess(User[] members) {
						AlphabetListItem[] items = new AlphabetListItem[members.length];
						for (int i = 0; i < members.length; i++) {
							final UserDisclosurePanel userDisclosurePanel = new UserDisclosurePanel(
									members[i]);
							userDisclosurePanel
									.addChangeListener(new ChangeListener() {

										public void onChange(Widget sender) {
											if (userDisclosurePanel
													.isSelected()) {
												setSelectedItem(userDisclosurePanel);
												onItemSelect(userDisclosurePanel);
											} else {
												setSelectedItem(null);
												onItemSelect(null);
											}
										}

									});
							items[i] = userDisclosurePanel;
						}
						callback.onSuccess(items);
					}

				});
	}

	/**
	 * Get the selected member
	 * 
	 * @param callback
	 *            handle the selected RODA member or null if none selected
	 */
	public void getSelected(AsyncCallback<RODAMember> callback) {
		AlphabetListItem selected = super.getSelectedItem();
		UserDisclosurePanel disclosure = (UserDisclosurePanel) selected;
		if (disclosure != null) {
			disclosure.getSelected(callback);
		} else {
			callback.onSuccess(null);
		}
	}

	protected String getSizeCountMessage(int size) {
		return messages.userCount(size);
	}

	public void getLetterList(AsyncCallback<Character[]> callback) {
		userManagementService.getUserLetterList(filter, callback);
	}

	public void setFilter(String filter) {
		if (!this.filter.equals(filter)) {
			this.filter = filter;
			super.setAllLetters();
			this.letter = null;
			update();
		}
	}

	public void setLetter(int letterIndex) {
		super.setLetter(letterIndex);
		char letter = getAlphabet()[letterIndex];
		if (this.letter == null || this.letter.charValue() != letter) {
			this.letter = new Character(letter);
			update();
		}
	}

	public void setAllLetters() {
		super.setAllLetters();
		if (this.letter != null) {
			this.letter = null;
			update();
		}
	}
}
