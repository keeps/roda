/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.user.client;

import pt.gov.dgarq.roda.core.data.RODAMember;
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
public class SelectGroupWindow extends WUIWindow {

	private static UserManagementConstants constants = (UserManagementConstants) GWT
			.create(UserManagementConstants.class);

	private GroupAlphabetList groupList;
	private WUIButton select;
	private WUIButton cancel;

	/**
	 * Create a new select user window
	 */
	public SelectGroupWindow() {
		super(constants.selectGroupWindowTitle(), 650, 400);

		groupList = new GroupAlphabetList();

		select = new WUIButton(constants.selectGroupWindowSelect(),
				WUIButton.Left.ROUND, WUIButton.Right.ARROW_FORWARD);
		cancel = new WUIButton(constants.selectGroupWindowCancel(),
				WUIButton.Left.ROUND, WUIButton.Right.CROSS);

		select.setEnabled(false);

		groupList
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

		setWidget(groupList);
		addToBottom(cancel);
		addToBottom(select);

		groupList.update();

	}

	/**
	 * Get selected user
	 * 
	 * @param callback
	 */
	public void getSelected(final AsyncCallback<RODAMember> callback) {
		groupList.getSelected(callback);
	}

}
