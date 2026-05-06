package org.roda.wui.client.planning.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ri.RelationObjectType;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationRelation;
import org.roda.wui.client.common.actions.RepresentationInformationActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.planning.RepresentationInformationNetwork;
import org.roda.wui.client.planning.ShowRepresentationInformation;
import org.roda.wui.client.search.Search;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RepresentationInformationDetailsPanel extends GenericMetadataCardPanel<RepresentationInformation> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public RepresentationInformationDetailsPanel(RepresentationInformation data) {
    setData(data);
  }

  @Override
  protected FlowPanel createHeaderWidget(RepresentationInformation data) {
    return new ActionableWidgetBuilder<RepresentationInformation>(RepresentationInformationActions.get())
      .buildGroupedListWithObjects(new ActionableObject<>(data),
        List.of(RepresentationInformationActions.RepresentationInformationAction.EDIT),
        List.of(RepresentationInformationActions.RepresentationInformationAction.EDIT));
  }

  @Override
  protected void buildFields(RepresentationInformation data) {
    buildField(messages.representationInformationName()).withValue(data.getName()).build();
    buildField(messages.representationInformationDescription()).withValue(data.getDescription()).build();
    buildField(messages.representationInformationFamily()).withValue(data.getFamilyI18n()).build();

    if (data.getSupport() != null) {
      buildField(messages.representationInformationSupport())
        .withValue(messages.representationInformationSupportValue(data.getSupport().toString())).build();
    }
    initRelations(data);

    addTagsField(data);
    addExtrasField(data);

    addSeparator(messages.representationInformationAdditionalInformation());
    buildField(messages.representationInformationIdentifier()).withValue(data.getId()).build();

    if (data.getCreatedOn() != null && StringUtils.isNotBlank(data.getCreatedBy())) {
      buildField(messages.detailsCreatedOn()).withValue(Humanize.formatDateTime(data.getCreatedOn())).build();
      buildField(messages.detailsCreatedBy()).withValue(data.getCreatedBy()).build();
    }

    if (data.getUpdatedOn() != null && StringUtils.isNotBlank(data.getUpdatedBy())) {
      buildField(messages.detailsUpdatedOn()).withValue(Humanize.formatDateTime(data.getUpdatedOn())).build();
      buildField(messages.detailsUpdatedBy()).withValue(data.getUpdatedBy()).build();
    }
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

    buildField(messages.representationInformationTags()).withWidget(tagsPanel).build();
  }

  private void addExtrasField(RepresentationInformation ri) {
    // 1. Create a placeholder panel and add it to the container synchronously
    // to reserve its spot in the exact order defined in buildFields().
    FlowPanel extrasPlaceholder = new FlowPanel();
    metadataContainer.add(extrasPlaceholder);

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
            // 2. Add the loaded content to the placeholder instead of the main
            // metadataContainer.
            extrasPlaceholder.add(extrasContent);
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
        buildField(relation.getRelationTypeI18n()).withWidget(relationValue).build();
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
}
