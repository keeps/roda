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
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.OrFiltersParameters;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ri.RelationObjectType;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationRelation;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.RepresentationInformationDialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
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
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
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
      return ListUtils.concat(RepresentationInformationRegister.RESOLVER.getHistoryPath(), getHistoryToken());
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
  Label representationInformationDescriptionKey, representationInformationDescriptionValue;

  @UiField
  Label representationInformationFamilyKey, representationInformationFamilyValue;

  @UiField
  Label representationInformationCategoryKey;

  @UiField
  FlowPanel representationInformationCategoryValue;

  @UiField
  Label representationInformationExtrasKey, representationInformationExtrasValue;

  @UiField
  Label representationInformationSupportKey, representationInformationSupportValue;

  @UiField
  FlowPanel representationInformationRelationsValue;

  @UiField
  FlowPanel objectPanel;

  @UiField
  FlowPanel additionalSeparator;

  @UiField
  Button buttonEdit;

  @UiField
  Button buttonCancel;

  private List<FilterParameter> aipParams = new ArrayList<>();
  private List<FilterParameter> representationParams = new ArrayList<>();
  private List<FilterParameter> fileParams = new ArrayList<>();

  public ShowRepresentationInformation() {
    this.ri = new RepresentationInformation();
    initWidget(uiBinder.createAndBindUi(this));
    initElements();
  }

  public ShowRepresentationInformation(final RepresentationInformation ri) {
    instance = this;
    this.ri = ri;

    initWidget(uiBinder.createAndBindUi(this));
    initEntityFilters(ri);
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

    representationInformationDescriptionValue.setText(ri.getDescription());
    representationInformationDescriptionKey.setVisible(StringUtils.isNotBlank(ri.getDescription()));

    representationInformationFamilyValue.setText(ri.getFamily());
    representationInformationFamilyKey.setVisible(StringUtils.isNotBlank(ri.getFamily()));

    List<String> categoryList = ri.getCategories();
    representationInformationCategoryValue.setVisible(categoryList != null && !categoryList.isEmpty());
    representationInformationCategoryKey.setVisible(categoryList != null && !categoryList.isEmpty());

    if (categoryList != null) {
      for (final String category : categoryList) {
        InlineHTML parPanel = new InlineHTML();
        parPanel.setHTML("<span class='label label-info btn-separator-right ri-category'>"
          + messages.representationInformationListItems(category) + "</span>");
        parPanel.addClickHandler(new ClickHandler() {

          @Override
          public void onClick(ClickEvent event) {
            List<String> history = new ArrayList<>();
            history.addAll(RepresentationInformationRegister.RESOLVER.getHistoryPath());
            history.add(Search.RESOLVER.getHistoryToken());
            history.add(RodaConstants.REPRESENTATION_INFORMATION_CATEGORIES);
            history.add(category);
            HistoryUtils.newHistory(history);
          }
        });
        representationInformationCategoryValue.add(parPanel);
      }
    }

    representationInformationExtrasValue.setText(ri.getExtras());
    representationInformationExtrasKey.setVisible(StringUtils.isNotBlank(ri.getExtras()));

    if (ri.getSupport() != null) {
      representationInformationSupportValue
        .setText(messages.representationInformationSupportValue(ri.getSupport().toString()));
      representationInformationSupportKey.setVisible(true);
    } else {
      representationInformationSupportKey.setVisible(false);
    }

    initRelations(ri);
  }

  public void updateLists(final RepresentationInformation ri) {
    aipParams.clear();
    representationParams.clear();
    fileParams.clear();

    initEntityFilters(ri);
    initRelations(ri);
  }

  private void initEntityFilters(final RepresentationInformation ri) {
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

      BrowserService.Util.getInstance().count(IndexedAIP.class.getName(), aipFilter, false, new AsyncCallback<Long>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(Long result) {
          objectPanel.clear();

          String url = HistoryUtils.getSearchHistoryByRepresentationInformationFilter(ri.getFilters(),
            RodaConstants.SEARCH_ITEMS);
          InlineHTML label = new InlineHTML(
            messages.representationInformationIntellectualEntities(result.intValue(), url));
          label.addStyleName("ri-form-label-inline");
          objectPanel.add(label);

          InlineHTML edit = new InlineHTML("<i class='fa fa-pencil' aria-hidden='true'></i>");
          edit.setTitle("Edit relation rules");
          edit.addStyleName("ri-category link-color");

          edit.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
              RepresentationInformationDialogs.showPromptDialogRepresentationInformation(
                messages.representationInformationAddNewRelation(), messages.cancelButton(), messages.confirmButton(),
                ShowRepresentationInformation.this.ri, new AsyncCallback<String>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    // do nothing
                  }

                  @Override
                  public void onSuccess(final String newType) {
                    // do nothing
                  }
                });
            }
          });

          objectPanel.add(edit);
        }
      });
    } else if (!representationParams.isEmpty()) {
      Filter representationFilter = new Filter();
      representationFilter.add(new OrFiltersParameters(representationParams));

      BrowserService.Util.getInstance().count(IndexedRepresentation.class.getName(), representationFilter, false,
        new AsyncCallback<Long>() {

          @Override
          public void onFailure(Throwable caught) {
            AsyncCallbackUtils.defaultFailureTreatment(caught);
          }

          @Override
          public void onSuccess(Long result) {
            objectPanel.clear();
            String url = HistoryUtils.getSearchHistoryByRepresentationInformationFilter(ri.getFilters(),
              RodaConstants.SEARCH_REPRESENTATIONS);
            InlineHTML label = new InlineHTML(
              messages.representationInformationRepresentations(result.intValue(), url));
            label.addStyleName("ri-form-label-inline");
            objectPanel.add(label);

            InlineHTML edit = new InlineHTML("<i class='fa fa-pencil' aria-hidden='true'></i>");
            edit.setTitle("Edit relation rules");
            edit.addStyleName("ri-category link-color");

            edit.addClickHandler(new ClickHandler() {

              @Override
              public void onClick(ClickEvent event) {
                RepresentationInformationDialogs.showPromptDialogRepresentationInformation(
                  messages.representationInformationAddNewRelation(), messages.cancelButton(), messages.confirmButton(),
                  ShowRepresentationInformation.this.ri, new AsyncCallback<String>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      // do nothing
                    }

                    @Override
                    public void onSuccess(final String newType) {
                      // do nothing
                    }
                  });
              }
            });

            objectPanel.add(edit);
          }
        });
    } else if (!fileParams.isEmpty()) {
      Filter fileFilter = new Filter();
      fileFilter.add(new OrFiltersParameters(fileParams));

      BrowserService.Util.getInstance().count(IndexedFile.class.getName(), fileFilter, false,
        new AsyncCallback<Long>() {

          @Override
          public void onFailure(Throwable caught) {
            AsyncCallbackUtils.defaultFailureTreatment(caught);
          }

          @Override
          public void onSuccess(Long result) {
            objectPanel.clear();
            String url = HistoryUtils.getSearchHistoryByRepresentationInformationFilter(ri.getFilters(),
              RodaConstants.SEARCH_FILES);
            InlineHTML label = new InlineHTML(messages.representationInformationFiles(result.intValue(), url));
            label.addStyleName("ri-form-label-inline");
            objectPanel.add(label);

            InlineHTML edit = new InlineHTML("<i class='fa fa-pencil' aria-hidden='true'></i>");
            edit.setTitle("Edit relation rules");
            edit.addStyleName("ri-category link-color");

            edit.addClickHandler(new ClickHandler() {

              @Override
              public void onClick(ClickEvent event) {
                RepresentationInformationDialogs.showPromptDialogRepresentationInformation(
                  messages.representationInformationAddNewRelation(), messages.cancelButton(), messages.confirmButton(),
                  ShowRepresentationInformation.this.ri, new AsyncCallback<String>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      // do nothing
                    }

                    @Override
                    public void onSuccess(final String newType) {
                      // do nothing
                    }
                  });
              }
            });

            objectPanel.add(edit);
          }
        });
    } else {
      objectPanel.clear();

      String url = HistoryUtils.getSearchHistoryByRepresentationInformationFilter(ri.getFilters(),
        RodaConstants.SEARCH_ITEMS);
      InlineHTML label = new InlineHTML(messages.representationInformationIntellectualEntities(0, url));
      label.addStyleName("ri-form-label-inline");
      objectPanel.add(label);

      InlineHTML edit = new InlineHTML("<i class='fa fa-pencil' aria-hidden='true'></i>");
      edit.setTitle("Edit relation rules");
      edit.addStyleName("ri-category link-color");

      edit.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          RepresentationInformationDialogs.showPromptDialogRepresentationInformation(
            messages.representationInformationAddNewRelation(), messages.cancelButton(), messages.confirmButton(),
            ShowRepresentationInformation.this.ri, new AsyncCallback<String>() {

              @Override
              public void onFailure(Throwable caught) {
                // do nothing
              }

              @Override
              public void onSuccess(final String newType) {
                // do nothing
              }
            });
        }
      });

      objectPanel.add(edit);
    }
  }

  private void initRelations(final RepresentationInformation ri) {
    additionalSeparator.setVisible(false);

    if (ri.getRelations() != null && !ri.getRelations().isEmpty()) {
      BrowserService.Util.getInstance().retrieveRelationTypeTranslations(LocaleInfo.getCurrentLocale().getLocaleName(),
        new AsyncCallback<Map<String, String>>() {

          @Override
          public void onFailure(Throwable caught) {
            AsyncCallbackUtils.defaultFailureTreatment(caught);
          }

          @Override
          public void onSuccess(Map<String, String> translations) {
            additionalSeparator.setVisible(true);
            representationInformationRelationsValue.clear();
            Map<String, List<RepresentationInformationRelation>> relationTypeToLink = new TreeMap<>();

            FlowPanel allPanel = new FlowPanel();
            representationInformationRelationsValue.add(allPanel);

            for (RepresentationInformationRelation relation : ri.getRelations()) {
              String relationType = translations.get(relation.getRelationType());
              if (relationTypeToLink.containsKey(relationType)) {
                relationTypeToLink.get(relationType).add(relation);
              } else {
                List<RepresentationInformationRelation> newRelations = new ArrayList<>();
                newRelations.add(relation);
                relationTypeToLink.put(relationType, newRelations);
              }
            }

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

              SafeHtmlBuilder b = new SafeHtmlBuilder();
              b.append(SafeHtmlUtils.fromSafeConstant("<ul>"));
              for (RepresentationInformationRelation relation : entry.getValue()) {
                b.append(createRelationViewer(relation));
              }
              b.append(SafeHtmlUtils.fromSafeConstant("</ul>"));
              allPanel.add(new InlineHTML(b.toSafeHtml().asString()));
            }
          }
        });
    }
  }

  private SafeHtml createRelationViewer(RepresentationInformationRelation relation) {
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    b.append(SafeHtmlUtils.fromSafeConstant("<li>"));

    if (relation.getObjectType().equals(RelationObjectType.AIP)) {
      Anchor a = new Anchor(relation.getTitle(),
        HistoryUtils.createHistoryHashLink(HistoryUtils.getHistoryBrowse(relation.getLink())));
      b.append(SafeHtmlUtils.fromSafeConstant("<a href='"));
      b.append(SafeHtmlUtils.fromString(a.getHref()));
      b.append(SafeHtmlUtils.fromSafeConstant("'>"));
      b.append(SafeHtmlUtils.fromString(a.getText()));
      b.append(SafeHtmlUtils.fromSafeConstant("</a>"));
    } else if (relation.getObjectType().equals(RelationObjectType.REPRESENTATION_INFORMATION)) {
      List<String> history = new ArrayList<>();
      history.addAll(ShowRepresentationInformation.RESOLVER.getHistoryPath());
      history.add(relation.getLink());

      Anchor a = new Anchor(relation.getTitle(), HistoryUtils.createHistoryHashLink(history));
      b.append(SafeHtmlUtils.fromSafeConstant("<a href='"));
      b.append(SafeHtmlUtils.fromString(a.getHref()));
      b.append(SafeHtmlUtils.fromSafeConstant("'>"));
      b.append(SafeHtmlUtils.fromString(a.getText()));
      b.append(SafeHtmlUtils.fromSafeConstant("</a>"));
    } else if (relation.getObjectType().equals(RelationObjectType.WEB)) {
      Anchor a = new Anchor(relation.getTitle(), relation.getLink());
      b.append(SafeHtmlUtils.fromSafeConstant("<a href='"));
      b.append(SafeHtmlUtils.fromString(a.getHref()));
      b.append(SafeHtmlUtils.fromSafeConstant("'>"));
      b.append(SafeHtmlUtils.fromString(a.getText()));
      b.append(SafeHtmlUtils.fromSafeConstant("</a>"));
    } else if (relation.getObjectType().equals(RelationObjectType.TEXT)) {
      b.append(SafeHtmlUtils.fromString(relation.getTitle()));
    }

    b.append(SafeHtmlUtils.fromSafeConstant("</li>"));
    return b.toSafeHtml();
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
      HistoryUtils.newHistory(RepresentationInformationRegister.RESOLVER);
      callback.onSuccess(null);
    }
  }

  @UiHandler("buttonEdit")
  void handleButtonEdit(ClickEvent e) {
    HistoryUtils.newHistory(RepresentationInformationRegister.RESOLVER,
      EditRepresentationInformation.RESOLVER.getHistoryToken(), ri.getId());
  }

  @UiHandler("buttonCancel")
  void handleButtonCancel(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    HistoryUtils.newHistory(selectedItems.getLastHistory());
  }

}
