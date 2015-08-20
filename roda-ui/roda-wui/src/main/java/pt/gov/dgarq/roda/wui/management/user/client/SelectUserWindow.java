/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.user.client;

import pt.gov.dgarq.roda.core.data.v2.RODAMember;
import pt.gov.dgarq.roda.wui.common.client.widgets.AlphabetListItem;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIWindow;
import pt.gov.dgarq.roda.wui.common.client.widgets.AlphabetSortedList.AlphabetSortedListListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.UserManagementConstants;

/**
 * @author Luis Faria
 * 
 */
public class SelectUserWindow extends WUIWindow {

	private static UserManagementConstants constants = (UserManagementConstants) GWT
			.create(UserManagementConstants.class);

	private UserAlphabetList userList;
	private WUIButton select;
	private WUIButton cancel;

	/**
	 * Create a new select user window
	 */
	public SelectUserWindow() {
		super(constants.selectUserWindowTitle(), 650, 400);

		userList = new UserAlphabetList();

		select = new WUIButton(constants.selectUserWindowSelect(),
				WUIButton.Left.ROUND, WUIButton.Right.ARROW_FORWARD);
		cancel = new WUIButton(constants.selectUserWindowCancel(),
				WUIButton.Left.ROUND, WUIButton.Right.CROSS);

		select.setEnabled(false);

		userList
				.addAlphabetSortedListListener(new AlphabetSortedListListener() {

					public void onItemSelect(AlphabetListItem item) {
						select.setEnabled(true);
					}

				});

		select.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				hide();
				onSuccess();
			}

		});

		cancel.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				hide();
				onCancel();
			}

		});

		setWidget(userList);
		addToBottom(cancel);
		addToBottom(select);

		userList.update();

	}

	/**
	 * Get selected user
	 * 
	 * @param callback
	 */
	public void getSelected(final AsyncCallback<RODAMember> callback) {
		userList.getSelected(callback);
	}

}
