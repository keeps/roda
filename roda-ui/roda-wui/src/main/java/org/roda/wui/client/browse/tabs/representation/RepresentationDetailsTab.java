package org.roda.wui.client.browse.tabs.representation;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.browse.RepresentationInformationHelper;
import org.roda.wui.client.common.model.BrowseRepresentationResponse;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class RepresentationDetailsTab extends GenericMetadataCardPanel<BrowseRepresentationResponse> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public RepresentationDetailsTab(BrowseRepresentationResponse response) {
    setData(response);
  }

  @Override
  protected FlowPanel createHeaderWidget(BrowseRepresentationResponse data) {
    return null;
  }

  @Override
  protected void buildFields(BrowseRepresentationResponse data) {
    IndexedRepresentation representation = data.getIndexedRepresentation();

    addSeparator(messages.detailsRepresentation());
    buildField(messages.representationId()).withWidget(createIdHTML(data)).build();
    if (representation.getCreatedOn() != null && StringUtils.isNotBlank(representation.getCreatedBy())) {
      buildField(messages.aipCreated()).withValue(messages
        .dateCreatedOrUpdated(Humanize.formatDateTime(representation.getCreatedOn()), representation.getCreatedBy()))
        .build();
    }

    if (representation.getUpdatedOn() != null && StringUtils.isNotBlank(representation.getUpdatedBy())) {
      buildField(messages.aipUpdated()).withValue(messages
        .dateCreatedOrUpdated(Humanize.formatDateTime(representation.getUpdatedOn()), representation.getUpdatedBy()))
        .build();
    }

    buildField(messages.representationType()).withWidget(createRepresentationTypeHTML(data)).build();
  }

  private FlowPanel createIdHTML(BrowseRepresentationResponse response) {
    IndexedRepresentation representation = response.getIndexedRepresentation();
    FlowPanel panel = new FlowPanel();

    final String riFilter = RepresentationInformationUtils.createRepresentationInformationFilter(
      RodaConstants.INDEX_REPRESENTATION, RodaConstants.INDEX_UUID, representation.getUUID());
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(
      SafeHtmlUtils.fromString(representation.getId()), riFilter, panel,
      response.getRiRules().contains(RodaConstants.INDEX_UUID));

    return panel;
  }

  private FlowPanel createRepresentationTypeHTML(BrowseRepresentationResponse response) {
    IndexedRepresentation representation = response.getIndexedRepresentation();

    if (representation.getType() == null) {
      return null;
    }

    FlowPanel panel = new FlowPanel();

    final String riFilter = RepresentationInformationUtils.createRepresentationInformationFilter(
      RodaConstants.INDEX_REPRESENTATION, RodaConstants.REPRESENTATION_TYPE, representation.getType());
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(
      SafeHtmlUtils.fromString(representation.getType()), riFilter, panel,
      response.getRiRules().contains(RodaConstants.REPRESENTATION_TYPE));

    return panel;
  }

}
