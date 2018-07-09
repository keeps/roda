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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.OrFiltersParameters;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ri.RelationObjectType;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationRelation;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.bundle.RepresentationInformationExtraBundle;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.ActionableObject;
import org.roda.wui.client.common.actions.ActionableWidgetBuilder;
import org.roda.wui.client.common.actions.RepresentationInformationActions;
import org.roda.wui.client.common.dialogs.RepresentationInformationDialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.search.Search;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
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

  private static ShowRepresentationInformation instance = null;

  interface MyUiBinder extends UiBinder<Widget, ShowRepresentationInformation> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static ClientMessages messages = GWT.create(ClientMessages.class);

  private static final List<String> fieldsToReturn = new ArrayList<>();

  private RepresentationInformation ri;

  @UiField
  Label representationInformationId;

  @UiField
  Label dateCreated, dateUpdated;

  @UiField
  Label representationInformationTitle;

  @UiField
  SimplePanel representationInformationIcon;

  @UiField
  Label representationInformationDescriptionKey;

  @UiField
  HTML representationInformationDescriptionValue;

  @UiField
  Label representationInformationFamilyKey, representationInformationFamilyValue;

  @UiField
  Label representationInformationTagKey;

  @UiField
  FlowPanel representationInformationTagValue;

  @UiField
  Label representationInformationSupportKey, representationInformationSupportValue;

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

  public void initElements() {
    HTMLPanel itemIconHtmlPanel = new HTMLPanel("<i class='fa fa-info-circle'></i>");
    itemIconHtmlPanel.addStyleName("browseItemIcon-other");
    representationInformationIcon.setWidget(itemIconHtmlPanel);
    representationInformationTitle.setText(ri.getName());
    representationInformationTitle.removeStyleName("browseTitle-allCollections");
    representationInformationIcon.getParent().removeStyleName("browseTitle-allCollections-wrapper");

    representationInformationId.setText(messages.representationInformationIdentifier() + ": " + ri.getId());

    if (ri.getCreatedOn() != null && StringUtils.isNotBlank(ri.getCreatedBy())) {
      dateCreated.setText(messages.dateCreated(Humanize.formatDateTime(ri.getCreatedOn()), ri.getCreatedBy()));
    }

    if (ri.getUpdatedOn() != null && StringUtils.isNotBlank(ri.getUpdatedBy())) {
      dateUpdated.setText(messages.dateUpdated(Humanize.formatDateTime(ri.getUpdatedOn()), ri.getUpdatedBy()));
    }

    representationInformationDescriptionValue.setHTML(ri.getDescription());
    representationInformationDescriptionKey.setVisible(StringUtils.isNotBlank(ri.getDescription()));

    representationInformationFamilyKey.setVisible(StringUtils.isNotBlank(ri.getFamily()));
    BrowserService.Util.getInstance().retrieveRepresentationInformationFamilyOptions(ri.getFamily(),
      LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<String>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(String familyTranslation) {
          representationInformationFamilyValue.setText(familyTranslation);
        }
      });

    List<String> tagsList = ri.getTags();
    representationInformationTagValue.setVisible(tagsList != null && !tagsList.isEmpty());
    representationInformationTagKey.setVisible(tagsList != null && !tagsList.isEmpty());

    if (tagsList != null) {
      for (final String category : tagsList) {
        InlineHTML parPanel = new InlineHTML();
        parPanel.setHTML("<span class='label label-info btn-separator-right ri-category'>"
          + messages.representationInformationListItems(category) + "</span>");
        parPanel.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            JavascriptUtils.scrollToTop();
            List<String> history = new ArrayList<>();
            history.addAll(RepresentationInformationNetwork.RESOLVER.getHistoryPath());
            history.add(Search.RESOLVER.getHistoryToken());
            history.add(RodaConstants.REPRESENTATION_INFORMATION_TAGS);
            history.add(category);
            HistoryUtils.newHistory(history);
          }
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

    BrowserService.Util.getInstance().retrieveRepresentationInformationExtraBundle(ri.getId(),
      LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<RepresentationInformationExtraBundle>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(RepresentationInformationExtraBundle extra) {
          HtmlSnippetUtils.createExtraShow(extras, extra.getFamilyValues().get(ri.getFamily()), false);
        }
      });

    actionableWidgetBuilder = new ActionableWidgetBuilder<>(RepresentationInformationActions.get())
      .withCallback(new NoAsyncCallback<Actionable.ActionImpact>() {
        @Override
        public void onSuccess(Actionable.ActionImpact result) {
          if (result.equals(Actionable.ActionImpact.DESTROYED)) {
            HistoryUtils.newHistory(RepresentationInformationNetwork.RESOLVER);
          }
        }
      });
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

      BrowserService.Util.getInstance().count(IndexedAIP.class.getName(), aipFilter, true,
        initEntityFiltersObjectPanel(RodaConstants.SEARCH_ITEMS));
    } else if (!representationParams.isEmpty()) {
      Filter representationFilter = new Filter();
      representationFilter.add(new OrFiltersParameters(representationParams));

      BrowserService.Util.getInstance().count(IndexedRepresentation.class.getName(), representationFilter, true,
        initEntityFiltersObjectPanel(RodaConstants.SEARCH_REPRESENTATIONS));
    } else if (!fileParams.isEmpty()) {
      Filter fileFilter = new Filter();
      fileFilter.add(new OrFiltersParameters(fileParams));

      BrowserService.Util.getInstance().count(IndexedFile.class.getName(), fileFilter, true,
        initEntityFiltersObjectPanel(RodaConstants.SEARCH_FILES));
    } else {
      initEntityFiltersObjectPanel(RodaConstants.SEARCH_ITEMS).onSuccess(0L);
    }
  }

  private AsyncCallback<Long> initEntityFiltersObjectPanel(final String searchType) {
    return new AsyncCallback<Long>() {
      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(Long size) {
        ShowRepresentationInformation.this.objectPanel.clear();

        String url = HistoryUtils.getSearchHistoryByRepresentationInformationFilter(
          ShowRepresentationInformation.this.ri.getFilters(), searchType);

        InlineHTML label = new InlineHTML();
        label.addStyleName("ri-form-label-inline");

        if (RodaConstants.SEARCH_ITEMS.equals(searchType)) {
          label.setHTML(messages.representationInformationIntellectualEntities(size.intValue(), url));
        } else if (RodaConstants.SEARCH_REPRESENTATIONS.equals(searchType)) {
          label.setHTML(messages.representationInformationRepresentations(size.intValue(), url));
        } else if (RodaConstants.SEARCH_FILES.equals(searchType)) {
          label.setHTML(messages.representationInformationFiles(size.intValue(), url));
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
                  BrowserService.Util.getInstance().updateRepresentationInformation(result, null,
                    new AsyncCallback<Void>() {
                      @Override
                      public void onFailure(Throwable caught) {
                        AsyncCallbackUtils.defaultFailureTreatment(caught);
                      }

                      @Override
                      public void onSuccess(Void result) {
                        ShowRepresentationInformation.getInstance().updateLists();
                      }
                    });
                }
              });
          }
        });

        ShowRepresentationInformation.this.objectPanel.add(edit);
      }
    };
  }

  private void initRelations() {
    additionalSeparator.setVisible(false);
    final RepresentationInformation ri = ShowRepresentationInformation.this.ri;

    BrowserService.Util.getInstance().retrieveRelationTypeTranslations(LocaleInfo.getCurrentLocale().getLocaleName(),
      new AsyncCallback<RelationTypeTranslationsBundle>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(final RelationTypeTranslationsBundle bundle) {
          representationInformationRelationsValue.clear();
          final Map<String, List<RepresentationInformationRelation>> relationTypeToLink = new TreeMap<>();

          final FlowPanel allPanel = new FlowPanel();
          representationInformationRelationsValue.add(allPanel);

          if (ri.getRelations() != null) {
            for (RepresentationInformationRelation relation : ri.getRelations()) {
              String relationType = bundle.getTranslations().get(relation.getObjectType())
                .get(relation.getRelationType());
              if (relationType != null) {
                if (relationTypeToLink.containsKey(relationType)) {
                  relationTypeToLink.get(relationType).add(relation);
                } else {
                  List<RepresentationInformationRelation> newRelations = new ArrayList<>();
                  newRelations.add(relation);
                  relationTypeToLink.put(relationType, newRelations);
                }
              }
            }
          }

          if (StringUtils.isNotBlank(ri.getId())) {
            Filter filter = new Filter(
              new SimpleFilterParameter(RodaConstants.REPRESENTATION_INFORMATION_RELATIONS_WITH_RI, ri.getId()));

            BrowserService.Util.getInstance().find(RepresentationInformation.class.getName(), filter, Sorter.NONE,
              new Sublist(0, 1000), Facets.NONE, LocaleInfo.getCurrentLocale().toString(), true,
              new ArrayList<>(), new NoAsyncCallback<IndexResult<RepresentationInformation>>() {
                @Override
                public void onSuccess(IndexResult<RepresentationInformation> result) {
                  for (RepresentationInformation r : result.getResults()) {
                    if (r.getRelations() != null) {
                      for (RepresentationInformationRelation relation : r.getRelations()) {
                        if (relation.getLink() != null && relation.getLink().equals(ri.getId())) {
                          String inverse = bundle.getInverseTranslations()
                            .get(bundle.getInverses().get(relation.getRelationType()));

                          if (StringUtils.isNotBlank(inverse)) {
                            List<RepresentationInformationRelation> existingRelations = relationTypeToLink.get(inverse);
                            if (existingRelations == null) {
                              existingRelations = new ArrayList<>();
                              relationTypeToLink.put(inverse, existingRelations);
                            }

                            // add new value to the list
                            RepresentationInformationRelation newRelation = new RepresentationInformationRelation(
                              inverse, relation.getObjectType(), r.getId(), r.getName());
                            existingRelations.add(newRelation);
                          }
                        }
                      }
                    }
                  }

                  additionalSeparator.setVisible(relationTypeToLink.size() > 0);
                  createRelationsLayout(relationTypeToLink, allPanel);
                }
              });
          }
        }
      });
  }

  private void createRelationsLayout(Map<String, List<RepresentationInformationRelation>> relationTypeToLink,
    FlowPanel allPanel) {
    for (Entry<String, List<RepresentationInformationRelation>> entry : relationTypeToLink.entrySet()) {
      Label entryLabel = new Label(entry.getKey());
      entryLabel.setStyleName("label");
      allPanel.add(entryLabel);

      Collections.sort(entry.getValue(), new Comparator<RepresentationInformationRelation>() {

        @Override
        public int compare(RepresentationInformationRelation o1, RepresentationInformationRelation o2) {
          return o1.getObjectType().getWeight() - o2.getObjectType().getWeight();
        }
      });

      FlowPanel linksPanel = new FlowPanel();
      allPanel.add(linksPanel);
      for (RepresentationInformationRelation relation : entry.getValue()) {
        Widget w = createRelationViewer(relation);
        if (w != null) {
          w.addStyleName("ri-links-panel");
          linksPanel.add(w);
        }
      }
    }
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
        anchor.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            if (!relation.getObjectType().equals(RelationObjectType.WEB)) {
              JavascriptUtils.scrollToTop();
            }
          }
        });

        widgetToAdd = anchor;
      }
    }

    return widgetToAdd;
  }

  public static ShowRepresentationInformation getInstance() {
    if (instance == null) {
      instance = new ShowRepresentationInformation();
    }
    return instance;
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
      BrowserService.Util.getInstance().retrieve(RepresentationInformation.class.getName(), historyTokens.get(0),
        fieldsToReturn, new AsyncCallback<RepresentationInformation>() {

          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          @Override
          public void onSuccess(RepresentationInformation result) {
            ShowRepresentationInformation panel = new ShowRepresentationInformation(result);
            callback.onSuccess(panel);
          }
        });
    } else {
      HistoryUtils.newHistory(RepresentationInformationNetwork.RESOLVER);
      callback.onSuccess(null);
    }
  }
}
