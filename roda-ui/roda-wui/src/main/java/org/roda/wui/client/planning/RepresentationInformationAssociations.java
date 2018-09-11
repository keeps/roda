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

import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.RepresentationInformationActions;
import org.roda.wui.client.common.dialogs.RepresentationInformationDialogs;
import org.roda.wui.client.common.lists.RepresentationInformationList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
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
      getInstance().resolve(historyTokens, callback);
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

  private static RepresentationInformationAssociations instance = null;

  interface MyUiBinder extends UiBinder<Widget, RepresentationInformationAssociations> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel title;

  @UiField
  HTML description;

  @UiField
  FlowPanel createPanel;

  @UiField
  Button buttonAddToExistingRI;

  @UiField
  Button buttonCreateNewRI;

  @UiField
  FlowPanel resultsPanel;

  @UiField
  HTML createPanelTitle;

  @UiField
  HTML resultsPanelTitle;

  @UiField(provided = true)
  SearchWrapper searchWrapper;

  private SafeHtml addWithAssociationDialogTitle;
  private boolean isInitializing = true;

  /**
   * Create a representation information page
   */
  public RepresentationInformationAssociations() {

    ValueChangeHandler<IndexResult<RepresentationInformation>> valueChangeHandler = event -> {
      boolean associating = isInitializing && event.getValue().getTotalCount() == 0;
      resultsPanel.setVisible(!associating);
      createPanel.setVisible(associating);
      isInitializing = false;
    };

    ListBuilder<RepresentationInformation> representationInformationAssociationsListBuilder = new ListBuilder<>(
      () -> new RepresentationInformationList(),
      new AsyncTableCellOptions<>(RepresentationInformation.class, "RepresentationInformationAssociations_RI")
        .bindOpener().addValueChangedHandler(valueChangeHandler).withActionable(RepresentationInformationActions.get())
        .withSearchPlaceholder(messages.representationInformationRegisterSearchPlaceHolder()));

    searchWrapper = new SearchWrapper(false).createListAndSearchPanel(representationInformationAssociationsListBuilder);
    searchWrapper.setVisible(false);

    initWidget(uiBinder.createAndBindUi(this));

    resultsPanel.setVisible(false);
    createPanel.setVisible(false);
    searchWrapper.setVisible(true);

    Label titleLabel = new Label(messages.representationInformationAssociationsTitle());
    titleLabel.addStyleName("h1 browseItemText");
    title.add(titleLabel);

    InlineHTML badge = new InlineHTML("<span class='label-warning browseRepresentationOriginalIcon'>Beta</span>");
    title.add(badge);

    buttonAddToExistingRI.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        RepresentationInformationDialogs.showPromptAddRepresentationInformationWithAssociation(
          addWithAssociationDialogTitle, messages.cancelButton(), messages.addToExistingRepresentationInformation(),
          messages.createNewRepresentationInformation(),
          new NoAsyncCallback<SelectedItems<RepresentationInformation>>() {
            @Override
            public void onSuccess(final SelectedItems<RepresentationInformation> selectedItems) {
              if (selectedItems != null) {
                String filtertoAdd = HistoryUtils.getCurrentHistoryPath()
                  .get(HistoryUtils.getCurrentHistoryPath().size() - 1);

                BrowserService.Util.getInstance().updateRepresentationInformationListWithFilter(selectedItems,
                  filtertoAdd, new NoAsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                      searchWrapper.refreshCurrentList();
                      createPanel.setVisible(false);
                      resultsPanel.setVisible(true);
                    }
                  });
              } else {
                addToNewClickHandler();
              }
            }
          });
      }
    });

    buttonCreateNewRI.addClickHandler(clickEvent -> addToNewClickHandler());
  }

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static RepresentationInformationAssociations getInstance() {
    if (instance == null) {
      instance = new RepresentationInformationAssociations();
    }
    return instance;
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    isInitializing = true;
    createPanel.setVisible(false);
    resultsPanel.setVisible(false);

    if (historyTokens.size() >= 2) {
      String[] parts = RepresentationInformationUtils.breakFilterIntoParts(historyTokens.get(1));
      createPanelTitle.setHTML(messages.representationInformationNoAssociations(parts[0], parts[1], parts[2]));
      resultsPanelTitle.setHTML(messages.representationInformationAssociatedWith(parts[0], parts[1], parts[2]));
      description.setHTML(messages.representationInformationAssociatedWithDescription(parts[0], parts[1], parts[2]));
      addWithAssociationDialogTitle = messages.representationInformationAssociateWith(parts[0], parts[1], parts[2]);

      searchWrapper.setFilter(RepresentationInformation.class,
        SearchFilters.createFilterFromHistoryTokens(historyTokens));

      callback.onSuccess(this);
    } else {
      HistoryUtils.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

  private void addToNewClickHandler() {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateRepresentationInformation.RESOLVER);
  }
}
