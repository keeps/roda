package pt.gov.dgarq.roda.wui.management.user.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.facet.SimpleFacetParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.v2.RODAMember;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.tools.FacetUtils;
import pt.gov.dgarq.roda.wui.common.client.widgets.RodaMemberList;
import pt.gov.dgarq.roda.wui.management.client.Management;

public class MemberManagement extends Composite {
	public static final HistoryResolver RESOLVER = new HistoryResolver() {

		@Override
		public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
			getInstance().resolve(historyTokens, callback);
		}

		@Override
		public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
			UserLogin.getInstance().checkRoles(new HistoryResolver[] { MemberManagement.RESOLVER }, false, callback);
		}

		public String getHistoryPath() {
			return Management.RESOLVER.getHistoryPath() + "." + getHistoryToken();
		}

		public String getHistoryToken() {
			return "user";
		}
	};

	private static MemberManagement instance = null;

	/**
	 * Get the singleton instance
	 * 
	 * @return the instance
	 */
	public static MemberManagement getInstance() {
		if (instance == null) {
			instance = new MemberManagement();
		}
		return instance;
	}

	interface MyUiBinder extends UiBinder<Widget, MemberManagement> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	// private UserManagementConstants constants = (UserManagementConstants)
	// GWT.create(UserManagementConstants.class);

	// private UserManagementMessages messages = (UserManagementMessages)
	// GWT.create(UserManagementMessages.class);

	@UiField(provided = true)
	RodaMemberList list;

	@UiField(provided = true)
	FlowPanel facetIsActive;

	@UiField(provided = true)
	FlowPanel facetIsUser;

	@UiField(provided = true)
	FlowPanel facetGroups;

	@UiField
	Button buttonEdit;

	@UiField
	Button buttonRemove;

	@UiField
	Button buttonDeActivate;

	public MemberManagement() {
		Filter filter = null;
		Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.MEMBER_IS_ACTIVE),
				new SimpleFacetParameter(RodaConstants.MEMBER_IS_USER),
				new SimpleFacetParameter(RodaConstants.MEMBER_GROUPS_ALL));
		list = new RodaMemberList(filter, facets);
		facetIsActive = new FlowPanel();
		facetIsUser = new FlowPanel();
		facetGroups = new FlowPanel();

		Map<String, FlowPanel> facetPanels = new HashMap<String, FlowPanel>();
		facetPanels.put(RodaConstants.MEMBER_IS_ACTIVE, facetIsActive);
		facetPanels.put(RodaConstants.MEMBER_IS_USER, facetIsUser);
		facetPanels.put(RodaConstants.MEMBER_GROUPS_ALL, facetGroups);

		FacetUtils.bindFacets(list, facetPanels);

		initWidget(uiBinder.createAndBindUi(this));

		list.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				RODAMember selected = list.getSelectionModel().getSelectedObject();
				updateVisibles(selected);
			}
		});
	}

	protected void updateVisibles(RODAMember selected) {
		buttonEdit.setEnabled(selected != null);
		buttonRemove.setEnabled(selected != null);
		buttonDeActivate.setEnabled(selected != null);
	}

	public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
		if (historyTokens.length == 0) {
			// TODO ?list.refresh();
			callback.onSuccess(this);
		} else {
			History.newItem(RESOLVER.getHistoryPath());
			callback.onSuccess(null);
		}
	}
}
