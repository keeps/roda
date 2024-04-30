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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.OrFiltersParameters;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ri.RelationObjectType;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationCreateRequest;
import org.roda.core.data.v2.ri.RepresentationInformationRelation;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.RepresentationInformationActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.dialogs.RepresentationInformationDialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.SidebarUtils;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.search.Search;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class ShowRepresentationInformation extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static ShowRepresentationInformation instance = null;
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(RepresentationInformationNetwork.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "representation_information";
    }
  };

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  Label representationInformationId;
  @UiField
  Label dateCreated;
  @UiField
  Label dateUpdated;
  @UiField
  TitlePanel title;
  @UiField
  Label representationInformationDescriptionKey;
  @UiField
  HTML representationInformationDescriptionValue;
  @UiField
  Label representationInformationFamilyKey;
  @UiField
  Label representationInformationFamilyValue;
  @UiField
  Label representationInformationTagKey;
  @UiField
  FlowPanel representationInformationTagValue;
  @UiField
  Label representationInformationSupportKey;
  @UiField
  Label representationInformationSupportValue;
  @UiField
  FlowPanel representationInformationRelationsValue;
  @UiField
  FlowPanel objectPanel;
  @UiField
  FlowPanel additionalSeparator;
  @UiField
  FlowPanel extras;
  @UiField
  SimplePanel actionsSidebar;
  @UiField
  FlowPanel contentFlowPanel;
  @UiField
  FlowPanel sidebarFlowPanel;

  private RepresentationInformation ri;
  private ActionableWidgetBuilder<RepresentationInformation> actionableWidgetBuilder;
  private List<FilterParameter> aipParams = new ArrayList<>();
  private List<FilterParameter> representationParams = new ArrayList<>();
  private List<FilterParameter> fileParams = new ArrayList<>();

  public ShowRepresentationInformation() {
    this.ri = new RepresentationInformation();
  }

  public ShowRepresentationInformation(final RepresentationInformation ri) {
    instance = this;
    this.ri = ri;

    initWidget(uiBinder.createAndBindUi(this));
    initEntityFilters();
    objectPanel.addStyleName("ri-entity-relation-section");
    initElements();
  }

  public static ShowRepresentationInformation getInstance() {
    if (instance == null) {
      instance = new ShowRepresentationInformation();
    }
    return instance;
  }

  public void initElements() {
    title.setText(ri.getName());

    representationInformationId.setText(messages.representationInformationIdentifier() + ": " + ri.getId());

    if (ri.getCreatedOn() != null && StringUtils.isNotBlank(ri.getCreatedBy())) {
      dateCreated.setText(messages.dateCreated(Humanize.formatDateTime(ri.getCreatedOn()), ri.getCreatedBy()));
    }

    if (ri.getUpdatedOn() != null && StringUtils.isNotBlank(ri.getUpdatedBy())) {
      dateUpdated.setText(messages.dateUpdated(Humanize.formatDateTime(ri.getUpdatedOn()), ri.getUpdatedBy()));
    }

    String description = (ri.getDescription() == null) ? "" : ri.getDescription();
    representationInformationDescriptionValue.setHTML(SafeHtmlUtils.fromString(description));
    representationInformationDescriptionKey.setVisible(StringUtils.isNotBlank(ri.getDescription()));

    representationInformationFamilyKey.setVisible(StringUtils.isNotBlank(ri.getFamily()));
    representationInformationFamilyValue.setText(ri.getFamilyI18n());

    List<String> tagsList = ri.getTags();
    representationInformationTagValue.setVisible(tagsList != null && !tagsList.isEmpty());
    representationInformationTagKey.setVisible(tagsList != null && !tagsList.isEmpty());

    if (tagsList != null) {
      for (final String category : tagsList) {
        InlineHTML parPanel = new InlineHTML();
        parPanel.setHTML("<span class='label label-info btn-separator-right ri-category'>"
          + messages.representationInformationListItems(SafeHtmlUtils.htmlEscape(category)) + "</span>");
        parPanel.addClickHandler(event -> {
          List<String> history = new ArrayList<>(RepresentationInformationNetwork.RESOLVER.getHistoryPath());
          history.add(Search.RESOLVER.getHistoryToken());
          history.add(RodaConstants.REPRESENTATION_INFORMATION_TAGS);
          history.add(category);
          HistoryUtils.newHistory(history);
        });
        representationInformationTagValue.add(parPanel);
      }
    }

    if (ri.getSupport() != null) {
      representationInformationSupportValue
        .setText(messages.representationInformationSupportValue(ri.getSupport().toString()));
      representationInformationSupportKey.setVisible(true);
    } else {
      representationInformationSupportKey.setVisible(false);
    }

    Services services = new Services("Retrieve representation information family metadata", "get");
    services.representationInformationResource(s -> s.retrieveRepresentationInformationFamily(ri.getId(),
      ri.getFamily(), LocaleInfo.getCurrentLocale().getLocaleName()))
      .whenComplete((representationInformationFamily, throwable) -> {
        if (throwable == null) {
          HtmlSnippetUtils.createExtraShow(extras, representationInformationFamily.getFamilyValues(), false);
        }
      });

    RepresentationInformationActions representationInformationActions = RepresentationInformationActions.get();

    actionableWidgetBuilder = new ActionableWidgetBuilder<>(representationInformationActions)
      .withActionCallback(new NoAsyncCallback<Actionable.ActionImpact>() {
        @Override
        public void onSuccess(Actionable.ActionImpact result) {
          if (result.equals(Actionable.ActionImpact.DESTROYED)) {
            HistoryUtils.newHistory(RepresentationInformationNetwork.RESOLVER);
          }
        }
      });

    SidebarUtils.toggleSidebar(contentFlowPanel, sidebarFlowPanel, representationInformationActions.hasAnyRoles());
    actionsSidebar.setWidget(actionableWidgetBuilder.buildListWithObjects(new ActionableObject<>(ri)));

    initRelations();
  }

  public void updateLists() {
    aipParams.clear();
    representationParams.clear();
    fileParams.clear();

    initEntityFilters();
    initRelations();
  }

  private void initEntityFilters() {
    for (String filter : ri.getFilters()) {
      String[] splittedFilter = filter
        .split(RepresentationInformationUtils.REPRESENTATION_INFORMATION_FILTER_SEPARATOR);

      if (splittedFilter[0].equals(RodaConstants.INDEX_AIP)) {
        aipParams.add(new SimpleFilterParameter(splittedFilter[1], splittedFilter[2]));
      } else if (splittedFilter[0].equals(RodaConstants.INDEX_REPRESENTATION)) {
        representationParams.add(new SimpleFilterParameter(splittedFilter[1], splittedFilter[2]));
      } else if (splittedFilter[0].equals(RodaConstants.INDEX_FILE)) {
        fileParams.add(new SimpleFilterParameter(splittedFilter[1], splittedFilter[2]));
      }
    }

    if (!aipParams.isEmpty()) {
      Filter aipFilter = new Filter();
      aipFilter.add(new OrFiltersParameters(aipParams));
      Services services = new Services("Count AIPs associated with representation information", "count");
      CountRequest countRequest = new CountRequest(IndexedAIP.class.getName(), aipFilter, true);
      services.rodaEntityRestService(s -> s.count(countRequest), IndexedAIP.class).whenComplete((count,
        throwable) -> initEntityFiltersObjectPanel(count.getResult(), throwable, IndexedAIP.class.getSimpleName()));
    } else if (!representationParams.isEmpty()) {
      Filter representationFilter = new Filter();
      representationFilter.add(new OrFiltersParameters(representationParams));

      Services services = new Services("Count Representations associated with representation information", "count");
      CountRequest countRequest = new CountRequest(IndexedRepresentation.class.getName(), representationFilter, true);
      services.rodaEntityRestService(s -> s.count(countRequest), IndexedRepresentation.class)
        .whenComplete((count, throwable) -> initEntityFiltersObjectPanel(count.getResult(), throwable,
          IndexedRepresentation.class.getSimpleName()));
    } else if (!fileParams.isEmpty()) {
      Filter fileFilter = new Filter();
      fileFilter.add(new OrFiltersParameters(fileParams));

      Services services = new Services("Count Files associated with representation information", "count");
      CountRequest countRequest = new CountRequest(IndexedFile.class.getName(), fileFilter, true);
      services.rodaEntityRestService(s -> s.count(countRequest), IndexedFile.class).whenComplete((count,
        throwable) -> initEntityFiltersObjectPanel(count.getResult(), throwable, IndexedFile.class.getSimpleName()));
    } else {
      initEntityFiltersObjectPanel(0L, null, IndexedAIP.class.getSimpleName());
    }
  }

  private void initEntityFiltersObjectPanel(final Long count, final Throwable throwable, final String searchType) {
    if (throwable != null) {
      AsyncCallbackUtils.defaultFailureTreatment(throwable);
    } else {
      ShowRepresentationInformation.this.objectPanel.clear();
      String url = HistoryUtils.getSearchHistoryByRepresentationInformationFilter(
        ShowRepresentationInformation.this.ri.getFilters(), searchType);

      InlineHTML label = new InlineHTML();
      label.addStyleName("ri-form-label-inline");
      if (IndexedAIP.class.getSimpleName().equals(searchType)) {
        label.setHTML(messages.representationInformationIntellectualEntities(count.intValue(), url));
      } else if (IndexedRepresentation.class.getSimpleName().equals(searchType)) {
        label.setHTML(messages.representationInformationRepresentations(count.intValue(), url));
      } else if (IndexedFile.class.getSimpleName().equals(searchType)) {
        label.setHTML(messages.representationInformationFiles(count.intValue(), url));
      }

      ShowRepresentationInformation.this.objectPanel.add(label);

      InlineHTML edit = new InlineHTML("<i class='fa fa-pencil' aria-hidden='true'></i>");
      edit.setTitle("Edit association rules");
      edit.addStyleName("ri-category link-color");

      edit.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          RepresentationInformationDialogs.showPromptDialogRepresentationInformation(
            messages.representationInformationEditAssociations(), messages.cancelButton(), messages.confirmButton(),
            messages.searchButton(), ShowRepresentationInformation.this.ri,
            new AsyncCallback<RepresentationInformation>() {
              @Override
              public void onFailure(Throwable caught) {
                // do nothing
              }

              @Override
              public void onSuccess(RepresentationInformation result) {
                // result is ri with updated filters
                Services services = new Services("Update representation information", "update");
                RepresentationInformationCreateRequest createRequest = new RepresentationInformationCreateRequest();
                createRequest.setRepresentationInformation(result);
                services.representationInformationResource(s -> s.updateRepresentationInformation(createRequest))
                  .whenComplete((representationInformation, throwable1) -> {
                    if (throwable1 == null) {
                      ShowRepresentationInformation.getInstance().updateLists();
                    }
                  });
              }
            });
        }
      });

      ShowRepresentationInformation.this.objectPanel.add(edit);
    }
  }

  private void initRelations() {
    additionalSeparator.setVisible(!ri.getRelations().isEmpty());

    ri.getRelations().sort(Comparator.comparingInt(o -> o.getObjectType().getWeight()));

    for (RepresentationInformationRelation relation : ri.getRelations()) {
      representationInformationRelationsValue.add(createRelationsLayout(relation));
    }
  }

  private FlowPanel createRelationsLayout(RepresentationInformationRelation relation) {
    FlowPanel panel = new FlowPanel();
    FlowPanel linksPanel = new FlowPanel();

    Label entryLabel = new Label(relation.getRelationTypeI18n());
    entryLabel.setStyleName("label");
    panel.add(entryLabel);

    Widget w = createRelationViewer(relation);
    if (w != null) {
      w.addStyleName("ri-links-panel");
      linksPanel.add(w);
    }

    panel.add(w);

    return panel;
  }

  private Widget createRelationViewer(RepresentationInformationRelation relation) {
    Widget widgetToAdd = null;
    String title = StringUtils.isNotBlank(relation.getTitle()) ? relation.getTitle() : relation.getLink();

    if (relation.getObjectType().equals(RelationObjectType.TEXT)) {
      widgetToAdd = new Label(title);
    } else {
      Anchor anchor = null;

      if (relation.getObjectType().equals(RelationObjectType.AIP)) {
        anchor = new Anchor(title,
          HistoryUtils.createHistoryHashLink(HistoryUtils.getHistoryBrowse(relation.getLink())));
      } else if (relation.getObjectType().equals(RelationObjectType.REPRESENTATION_INFORMATION)) {
        List<String> history = new ArrayList<>();
        history.addAll(ShowRepresentationInformation.RESOLVER.getHistoryPath());
        history.add(relation.getLink());
        anchor = new Anchor(title, HistoryUtils.createHistoryHashLink(history));
      } else if (relation.getObjectType().equals(RelationObjectType.WEB)) {
        anchor = new Anchor(title, relation.getLink());
        anchor.getElement().setAttribute("target", "_blank");
      }

      if (anchor != null) {
        widgetToAdd = anchor;
      }
    }

    return widgetToAdd;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  // Java method
  public native boolean isValidUrl(String url) /*-{
		var pattern = /(http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
		return pattern.test(url);
  }-*/;

  void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {
      Services services = new Services("Retrieve representation information", "get");
      services
        .rodaEntityRestService(s -> s.findByUuid(historyTokens.get(0), LocaleInfo.getCurrentLocale().getLocaleName()),
          RepresentationInformation.class)
        .whenComplete((representationInformation, throwable) -> {
          if (throwable != null) {
            callback.onFailure(throwable);
          } else {

            ShowRepresentationInformation panel = new ShowRepresentationInformation(representationInformation);
            callback.onSuccess(panel);
          }
        });
    } else {
      HistoryUtils.newHistory(RepresentationInformationNetwork.RESOLVER);
      callback.onSuccess(null);
    }
  }

  interface MyUiBinder extends UiBinder<Widget, ShowRepresentationInformation> {
  }
}
