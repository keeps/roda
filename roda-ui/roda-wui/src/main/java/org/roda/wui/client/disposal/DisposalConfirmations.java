package org.roda.wui.client.disposal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.RepresentationInformationActions;
import org.roda.wui.client.common.lists.RepresentationInformationList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.planning.CreateRepresentationInformation;
import org.roda.wui.client.planning.EditRepresentationInformation;
import org.roda.wui.client.planning.RepresentationInformationAssociations;
import org.roda.wui.client.planning.ShowRepresentationInformation;
import org.roda.wui.client.search.Search;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import java.util.List;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class DisposalConfirmations extends Composite {
    public static final HistoryResolver RESOLVER = new HistoryResolver() {

        @Override
        public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
            getInstance().resolve(historyTokens, callback);
        }

        @Override
        public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
            UserLogin.getInstance().checkRole(this, callback);
        }

        @Override
        public List<String> getHistoryPath() {
            return ListUtils.concat(Disposal.RESOLVER.getHistoryPath(), getHistoryToken());
        }

        @Override
        public String getHistoryToken() {
            return "disposalConfirmations";
        }
    };

    private static DisposalConfirmations instance = null;

    interface MyUiBinder extends UiBinder<Widget, DisposalConfirmations> {
    }

    private static DisposalConfirmations.MyUiBinder uiBinder = GWT.create(DisposalConfirmations.MyUiBinder.class);
    private static final ClientMessages messages = GWT.create(ClientMessages.class);

    @UiField
    FlowPanel registerDescription;

    @UiField(provided = true)
    SearchWrapper searchPanel;

    @UiField
    FlowPanel content;

    /**
     * Create a representation information page
     */
    public DisposalConfirmations() {
        ListBuilder<RepresentationInformation> representationInformationListBuilder = new ListBuilder<>(
                () -> new RepresentationInformationList(),
                new AsyncTableCellOptions<>(RepresentationInformation.class, "RepresentationInformationNetwork_RI")
                        .withSummary(messages.representationInformationTitle()).bindOpener()
                        .withSearchPlaceholder(messages.representationInformationRegisterSearchPlaceHolder())
                        .withActionable(RepresentationInformationActions.get()));

        searchPanel = new SearchWrapper(false).createListAndSearchPanel(representationInformationListBuilder);

        initWidget(uiBinder.createAndBindUi(this));

        registerDescription.add(new HTMLWidgetWrapper("RepresentationInformationNetworkDescription.html"));
    }

    /**
     * Get the singleton instance
     *
     * @return the instance
     */
    public static DisposalConfirmations getInstance() {
        if (instance == null) {
            instance = new DisposalConfirmations();
        }
        return instance;
    }

    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
        if (historyTokens.isEmpty()) {
            searchPanel.setFilter(RepresentationInformation.class, SearchFilters.allFilter());
            callback.onSuccess(this);
        } else {
            String basePage = historyTokens.remove(0);
            if (ShowRepresentationInformation.RESOLVER.getHistoryToken().equals(basePage)) {
                ShowRepresentationInformation.RESOLVER.resolve(historyTokens, callback);
            } else if (CreateRepresentationInformation.RESOLVER.getHistoryToken().equals(basePage)) {
                CreateRepresentationInformation.RESOLVER.resolve(historyTokens, callback);
            } else if (EditRepresentationInformation.RESOLVER.getHistoryToken().equals(basePage)) {
                EditRepresentationInformation.RESOLVER.resolve(historyTokens, callback);
            } else if (RepresentationInformationAssociations.RESOLVER.getHistoryToken().equals(basePage)) {
                RepresentationInformationAssociations.RESOLVER.resolve(historyTokens, callback);
            } else if (Search.RESOLVER.getHistoryToken().equals(basePage)) {
                searchPanel.setFilter(RepresentationInformation.class,
                        SearchFilters.createFilterFromHistoryTokens(historyTokens));
                callback.onSuccess(this);
            } else {
                HistoryUtils.newHistory(RESOLVER);
                callback.onSuccess(null);
            }
        }
    }
}
