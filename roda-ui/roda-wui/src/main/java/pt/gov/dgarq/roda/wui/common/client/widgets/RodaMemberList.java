package pt.gov.dgarq.roda.wui.common.client.widgets;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ProvidesKey;

import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.RODAMember;
import pt.gov.dgarq.roda.wui.management.user.client.UserManagementService;

public class RodaMemberList extends AsyncTableCell<RODAMember> {

	private static final int PAGE_SIZE = 20;

	// private final ClientLogger logger = new
	// ClientLogger(getClass().getName());

	private final Column<RODAMember, SafeHtml> activeColumn;
	private final Column<RODAMember, SafeHtml> typeColumn;
	private final TextColumn<RODAMember> idColumn;
	private final TextColumn<RODAMember> nameColumn;
	private final TextColumn<RODAMember> groupsColumn;
	private final TextColumn<RODAMember> rolesColumn;

	public RodaMemberList() {
		this(null, null);
	}

	public RodaMemberList(Filter filter, Facets facets) {
		super(filter, facets,"MEMBERS");

		activeColumn = new Column<RODAMember, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(RODAMember member) {
				return SafeHtmlUtils.fromSafeConstant(member != null
						? (member.isActive() ? "<i class='fa fa-check-circle'></i>" : "<i class='fa fa-ban'></i>")
						: "");

			}
		};

		typeColumn = new Column<RODAMember, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(RODAMember member) {
				return SafeHtmlUtils.fromSafeConstant(member != null
						? (member.isUser() ? "<i class='fa fa-user'></i>" : "<i class='fa fa-users'></i>") : "");

			}
		};

		idColumn = new TextColumn<RODAMember>() {

			@Override
			public String getValue(RODAMember member) {
				return member != null ? member.getId() : null;
			}
		};
		nameColumn = new TextColumn<RODAMember>() {

			@Override
			public String getValue(RODAMember member) {
				return member != null ? member.getName() : null;
			}
		};
		groupsColumn = new TextColumn<RODAMember>() {

			@Override
			public String getValue(RODAMember member) {
				return member != null ? member.getAllGroups().toString() : null;
			}
		};
		rolesColumn = new TextColumn<RODAMember>() {

			@Override
			public String getValue(RODAMember member) {
				return member != null ? member.getAllRoles().toString() : null;
			}
		};

		// idColumn.setSortable(true);
		// nameColumn.setSortable(true);
		// groupsColumn.setSortable(true);
		// rolesColumn.setSortable(true);

		// TODO externalize strings into constants
		getDisplay().addColumn(activeColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-check-circle'></i>"));
		getDisplay().addColumn(typeColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-user'></i>"));
		getDisplay().addColumn(idColumn, "Identifier");
		getDisplay().addColumn(nameColumn, "Full name");
		getDisplay().addColumn(groupsColumn, "Groups");
		getDisplay().addColumn(rolesColumn, "Roles");

		// getDisplay().setAutoHeaderRefreshDisabled(true);
		Label emptyInfo = new Label("No items to display");
		getDisplay().setEmptyTableWidget(emptyInfo);
		// getDisplay().setColumnWidth(nameColumn, "100%");

		getDisplay().setColumnWidth(activeColumn, "15px");
		getDisplay().setColumnWidth(typeColumn, "15px");

		addStyleName("my-list-rodamember");
		emptyInfo.addStyleName("my-list-rodamember-empty-info");

		idColumn.setCellStyleNames("nowrap");

	}

	@Override
	protected void getData(int start, int length, ColumnSortList columnSortList,
			AsyncCallback<IndexResult<RODAMember>> callback) {

		Filter filter = getFilter();

		// calculate sorter
		Sorter sorter = new Sorter();
		// TODO set sorters
		// for (int i = 0; i < columnSortList.size(); i++) {
		// ColumnSortInfo columnSortInfo = columnSortList.get(i);
		// String sortParameterKey;

		// if (columnSortInfo.getColumn().equals(idColumn)) {
		// sortParameterKey = RodaConstants.SIP_REPORT_ID;
		// } else {
		// sortParameterKey = null;
		// }

		// if (sortParameterKey != null) {
		// sorter.add(new SortParameter(sortParameterKey,
		// !columnSortInfo.isAscending()));
		// } else {
		// logger.warn("Selecting a sorter that is not mapped");
		// }
		// }

		// define sublist
		Sublist sublist = new Sublist(start, length);

		// IngestListService.Util.getInstance().findSipReports(filter, sorter,
		// sublist, getFacets(), callback);
		UserManagementService.Util.getInstance().findMember(filter, sorter, sublist, getFacets(), callback);
	}

	@Override
	protected ProvidesKey<RODAMember> getKeyProvider() {
		return new ProvidesKey<RODAMember>() {

			@Override
			public Object getKey(RODAMember item) {
				return item.getId();
			}
		};
	}

	@Override
	protected int getInitialPageSize() {
		return PAGE_SIZE;
	}

}
