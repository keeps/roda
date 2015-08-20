/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.user.client;

import pt.gov.dgarq.roda.core.data.v2.Group;
import pt.gov.dgarq.roda.core.data.v2.RODAMember;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.widgets.AlphabetListItem;
import pt.gov.dgarq.roda.wui.common.client.widgets.AlphabetSortedList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.UserManagementMessages;

/**
 * @author Luis Faria
 * 
 */
public class GroupAlphabetList extends AlphabetSortedList {
	private ClientLogger logger = new ClientLogger(getClass().getName());

	private final UserManagementServiceAsync userManagementService;

	private static UserManagementMessages messages = (UserManagementMessages) GWT
			.create(UserManagementMessages.class);

	private String filter;

	private Character letter;

	private boolean initialized;

	/**
	 * Create a new group alphabet list
	 * 
	 */
	public GroupAlphabetList() {
		super();
		this.userManagementService = UserManagementService.Util.getInstance();
		filter = "";
		letter = null;
		initialized = false;
	}

	/**
	 * Refresh this alphabet list content
	 * 
	 */
	public void update() {
		this.clear();

		userManagementService.getGroupCount(letter, filter,
				new AsyncCallback<Integer>() {
					public void onFailure(Throwable caught) {
						Window.alert("Internal Error Occured");
						logger.error("Error while updating user list", caught);
					}

					public void onSuccess(Integer groupCount) {
						if (!initialized) {
							init(groupCount);
							initialized = true;
						} else {
							update(groupCount);
						}
					}
				});
	}

	/**
	 * Get selected group name.
	 * 
	 * @param callback
	 *            handle the selected group or user, or null if none selected.
	 */
	public void getSelected(AsyncCallback<RODAMember> callback) {
		AlphabetListItem selected = super.getSelectedItem();
		GroupDisclosurePanel disclosure = (GroupDisclosurePanel) selected;
		if (disclosure != null) {
			disclosure.getSelected(callback);
		} else {
			callback.onSuccess(null);
		}
	}

	protected String getSizeCountMessage(int size) {
		return messages.groupCount(size);
	}

	public void getItems(int firstItem, int limit, final AsyncCallback<AlphabetListItem[]> callback) {
		userManagementService.getGroups(letter, filter, firstItem, limit,
				new AsyncCallback<Group[]>() {

					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}

					public void onSuccess(Group[] groups) {
						AlphabetListItem[] items = new AlphabetListItem[groups.length];
						for (int i = 0; i < groups.length; i++) {
							final GroupDisclosurePanel groupDisclosure = new GroupDisclosurePanel(
									groups[i]);
							groupDisclosure
									.addChangeListener(new ChangeListener() {

										public void onChange(Widget sender) {
											if (groupDisclosure.isSelected()) {
												setSelectedItem(groupDisclosure);
												onItemSelect(groupDisclosure);
											} else {
												setSelectedItem(null);
												onItemSelect(null);
											}
										}

									});
							items[i] = groupDisclosure;
						}
						callback.onSuccess(items);
					}

				});
	}

	public void getLetterList(AsyncCallback<Character[]> callback) {
		userManagementService.getGroupLetterList(filter, callback);
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
