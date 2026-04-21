package org.roda.wui.client.planning;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ri.RelationObjectType;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationRelation;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.search.Search;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 *
 * @author Eduardo Teixeira <eteixeira@keep.pt>
 */
public class DetailsPanelRepresentationInformation extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FlowPanel details;

  public DetailsPanelRepresentationInformation(RepresentationInformation ri) {
    initWidget(uiBinder.createAndBindUi(this));
    init(ri);
  }

  private void init(RepresentationInformation ri) {
    if (ri.getCreatedOn() != null && StringUtils.isNotBlank(ri.getCreatedBy())) {
      addIfNotBlank(messages.detailsCreatedOn(), Humanize.formatDateTime(ri.getCreatedOn()));
      addIfNotBlank(messages.detailsCreatedBy(), ri.getCreatedBy());
    }

    if (ri.getUpdatedOn() != null && StringUtils.isNotBlank(ri.getUpdatedBy())) {
      addIfNotBlank(messages.detailsUpdatedOn(), Humanize.formatDateTime(ri.getUpdatedOn()));
      addIfNotBlank(messages.detailsUpdatedBy(), ri.getUpdatedBy());
    }
    addIfNotBlank(messages.representationInformationIdentifier(), ri.getId());
    addIfNotBlank(messages.representationInformationName(), ri.getName());
    addIfNotBlank(messages.representationInformationDescription(), ri.getDescription());
    addIfNotBlank(messages.representationInformationFamily(), ri.getFamilyI18n());

    if (ri.getSupport() != null) {
      addIfNotBlank(messages.representationInformationSupport(),
        messages.representationInformationSupportValue(ri.getSupport().toString()));
    }

    addTagsField(ri);
    addExtrasField(ri);
    initRelations(ri);

  }

  private void addIfNotBlank(String label, String value) {
    if (StringUtils.isNotBlank(value)) {
      details.add(buildField(label, new InlineHTML(SafeHtmlUtils.htmlEscape(value))));
    }
  }

  private Widget buildField(String label, Widget valueWidget) {
    FlowPanel fieldPanel = new FlowPanel();
    fieldPanel.setStyleName("field");

    Label fieldLabel = new Label(label);
    fieldLabel.setStyleName("label");

    FlowPanel fieldValuePanel = new FlowPanel();
    fieldValuePanel.setStyleName("value");
    fieldValuePanel.add(valueWidget);

    fieldPanel.add(fieldLabel);
    fieldPanel.add(fieldValuePanel);

    return fieldPanel;
  }

  private void addTagsField(RepresentationInformation ri) {
    List<String> tags = ri.getTags();
    if (tags == null || tags.isEmpty())
      return;

    FlowPanel tagsPanel = new FlowPanel();
    tagsPanel.addStyleName("value");
    for (String category : tags) {
      InlineHTML tag = new InlineHTML("<span class='label-info btn-separator-right ri-category'>"
        + messages.representationInformationListItems(SafeHtmlUtils.htmlEscape(category)) + "</span>");
      tag.addClickHandler(event -> {
        List<String> history = new ArrayList<>(RepresentationInformationNetwork.RESOLVER.getHistoryPath());
        history.add(Search.RESOLVER.getHistoryToken());
        history.add(RodaConstants.REPRESENTATION_INFORMATION_TAGS);
        history.add(category);
        HistoryUtils.newHistory(history);
      });
      tagsPanel.add(tag);
    }

    details.add(buildField(messages.representationInformationTags(), tagsPanel));
  }

  private void addExtrasField(RepresentationInformation ri) {
    Services services = new Services("Retrieve representation information family metadata", "get");
    services
      .representationInformationResource(
        s -> s.retrieveRepresentationInformationFamily(ri.getId(), LocaleInfo.getCurrentLocale().getLocaleName()))
      .whenComplete((family, throwable) -> {
        if (throwable == null && family != null && family.getFamilyValues() != null
          && !family.getFamilyValues().isEmpty()) {

          FlowPanel extrasContent = new FlowPanel();
          extrasContent.addStyleName("ri-extras-panel");

          HtmlSnippetUtils.createExtraShow(extrasContent, family.getFamilyValues(), false);

          if (extrasContent.getWidgetCount() > 0) {
            details.add(extrasContent);
          }
        }
      });
  }

  private void initRelations(RepresentationInformation ri) {
    if (ri.getRelations() == null) {
      return;
    }

    ri.getRelations().sort(Comparator.comparingInt(o -> o.getObjectType().getWeight()));
    for (RepresentationInformationRelation relation : ri.getRelations()) {
      Widget relationValue = createRelationViewer(relation);
      if (relationValue != null) {
        relationValue.addStyleName("ri-links-panel");
        details.add(buildField(relation.getRelationTypeI18n(), relationValue));
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
        widgetToAdd = anchor;
      }
    }

    return widgetToAdd;
  }

  interface MyUiBinder extends UiBinder<Widget, DetailsPanelRepresentationInformation> {
    Widget createAndBindUi(DetailsPanelRepresentationInformation representationInformationPanel);
  }
}
