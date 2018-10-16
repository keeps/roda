/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 *
 */
package org.roda.wui.client.planning;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.RepresentationInformationActions;
import org.roda.wui.client.common.lists.RepresentationInformationList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira
 *
 */
public class RepresentationInformationAssociations extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.size() >= 2 && RodaConstants.REPRESENTATION_INFORMATION_FILTERS.equals(historyTokens.get(0))) {
        String[] filterParts = RepresentationInformationUtils.breakFilterIntoParts(historyTokens.get(1));
        Filter filter = SearchFilters.createFilterFromHistoryTokens(historyTokens);
        callback.onSuccess(new RepresentationInformationAssociations(filter, filterParts));
      } else {
        HistoryUtils.newHistory(RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(RepresentationInformationNetwork.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "representation_information_associations";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, RepresentationInformationAssociations> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  HTML description;

  @UiField
  FlowPanel resultsPanel;

  @UiField
  HTML resultsPanelTitle;

  @UiField(provided = true)
  SearchWrapper searchWrapper;

  public RepresentationInformationAssociations(Filter filter, String[] filterParts) {
    ListBuilder<RepresentationInformation> representationInformationAssociationsListBuilder = new ListBuilder<>(
      () -> new RepresentationInformationList(),
      new AsyncTableCellOptions<>(RepresentationInformation.class, "RepresentationInformationAssociations_RI")
        .bindOpener().withActionable(RepresentationInformationActions.getForAssociation(filter)).withFilter(filter)
        .withSearchPlaceholder(messages.representationInformationRegisterSearchPlaceHolder()));

    searchWrapper = new SearchWrapper(false).createListAndSearchPanel(representationInformationAssociationsListBuilder);

    initWidget(uiBinder.createAndBindUi(this));

    resultsPanelTitle
      .setHTML(messages.representationInformationAssociatedWith(filterParts[0], filterParts[1], filterParts[2]));
    description.setHTML(
      messages.representationInformationAssociatedWithDescription(filterParts[0], filterParts[1], filterParts[2]));
  }

  public static SafeHtml getAssociateWithExistingDialogTitle() {
    List<String> historyTokens = HistoryUtils.getCurrentHistoryPath();
    if (historyTokens.size() >= 2) {
      String[] parts = RepresentationInformationUtils.breakFilterIntoParts(historyTokens.get(1));
      return messages.representationInformationAssociatedWith(parts[0], parts[1], parts[2]);
    } else {
      return SafeHtmlUtils.EMPTY_SAFE_HTML;
    }
  }
}
