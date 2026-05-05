package org.roda.wui.client.browse.tabs;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.LinkingObjectUtils;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.*;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.client.planning.ShowPreservationAgent;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.*;

public class BrowsePreservationEventTabs extends Tabs {

  private IndexedPreservationEvent event;
  private List<IndexedPreservationAgent> agents;
  private PreservationEventsLinkingObjects linkingObjects;
  private String outcomeDetails;

  public void init(IndexedPreservationEvent event, List<IndexedPreservationAgent> agents,
    PreservationEventsLinkingObjects linkingObjects, String outcomeDetails) {

    this.event = event;
    this.agents = agents;
    this.linkingObjects = linkingObjects;
    this.outcomeDetails = outcomeDetails;

    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.detailsTab()), new TabContentBuilder() {
      ;
      @Override
      public Widget buildTabWidget() {
        return new DetailsTab(event, outcomeDetails);
      }
    });

    if (agents != null && !agents.isEmpty()) {
      createAndAddTab(SafeHtmlUtils.fromString(messages.preservationEventAgentsHeader()), new TabContentBuilder() {
        @Override
        public Widget buildTabWidget() {
          return buildAgentsTab();
        }
      });
    }

    if (hasRenderableObjects(linkingObjects != null ? linkingObjects.getSourceObjectIds() : null,
      RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE)) {
      createAndAddTab(SafeHtmlUtils.fromString(messages.preservationEventSourceObjectsHeader()),
        new TabContentBuilder() {
          @Override
          public Widget buildTabWidget() {
            return buildSourceObjectsTab();
          }
        });
    }

    if (hasRenderableObjects(linkingObjects != null ? linkingObjects.getOutcomeObjectIds() : null,
      RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME)) {
      createAndAddTab(SafeHtmlUtils.fromString(messages.preservationEventOutcomeObjectsHeader()),
        new TabContentBuilder() {
          @Override
          public Widget buildTabWidget() {
            return buildOutcomeObjectsTab();
          }
        });
    }
  }

  private Widget buildAgentsTab() {
    FlowPanel container = new FlowPanel();
    container.addStyleName("details-panel-sections");

    for (IndexedPreservationAgent agent : agents) {
      container.add(buildAgentPanel(agent));
    }

    return container;
  }

  private Widget buildSourceObjectsTab() {
    FlowPanel container = new FlowPanel();
    container.addStyleName("details-panel-sections");

    for (LinkingIdentifier sourceObjectId : linkingObjects.getSourceObjectIds()) {
      if (isRenderableObject(sourceObjectId, RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE)) {
        container.add(buildObjectPanel(sourceObjectId));
      }
    }

    return container;
  }

  private Widget buildOutcomeObjectsTab() {
    FlowPanel container = new FlowPanel();
    container.addStyleName("details-panel-sections");

    for (LinkingIdentifier outcomeObjectId : linkingObjects.getOutcomeObjectIds()) {
      if (isRenderableObject(outcomeObjectId, RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME)) {
        container.add(buildObjectPanel(outcomeObjectId));
      }
    }

    return container;
  }

  private Widget buildAgentPanel(IndexedPreservationAgent agent) {
    String title = StringUtils.isNotBlank(agent.getName()) ? agent.getName() : agent.getId();
    String href = HistoryUtils.createHistoryHashLink(ShowPreservationAgent.RESOLVER, agent.getId());

    FlowPanel card = buildCard();
    card.add(buildCardHeader(title, href, false));

    FlowPanel body = buildCardBody();
    addIfNotBlank(body, messages.preservationEventAgentIdentifier(), agent.getId());

    if (agent.getRoles() != null && !agent.getRoles().isEmpty()) {
      addIfNotBlank(body, messages.preservationEventAgentRoles(), StringUtils.join(agent.getRoles(), ", "));
    }

    addIfNotBlank(body, messages.preservationEventAgentType(), agent.getType());
    addIfNotBlank(body, messages.preservationEventAgentVersion(), agent.getVersion());
    addIfNotBlank(body, messages.preservationEventAgentNote(), agent.getNote());
    addIfNotBlank(body, messages.preservationEventAgentExtension(), agent.getExtension());
    card.add(body);
    return card;
  }

  private Widget buildObjectPanel(LinkingIdentifier object) {
    String idValue = object.getValue();

    if (RodaConstants.URN_TYPE.equalsIgnoreCase(object.getType())) {
      RodaConstants.RODA_TYPE type = LinkingObjectUtils.getLinkingIdentifierType(idValue);

      if (type == RodaConstants.RODA_TYPE.TRANSFERRED_RESOURCE) {
        return buildTransferredResourcePanel(idValue);
      } else if (type == RodaConstants.RODA_TYPE.FILE) {
        return buildFilePanel(idValue);
      } else if (type == RodaConstants.RODA_TYPE.REPRESENTATION) {
        return buildRepresentationPanel(idValue);
      } else if (type == RodaConstants.RODA_TYPE.AIP) {
        return buildAipPanel(idValue);
      }
    } else if (RodaConstants.URI_TYPE.equalsIgnoreCase(object.getType())) {
      return buildUriPanel(idValue);
    }

    return new FlowPanel();
  }

  private Widget buildUriPanel(String idValue) {
    FlowPanel card = buildCard();
    card.add(buildCardHeader(messages.uriLinkingIdentifierTitle(), idValue, true));

    FlowPanel body = buildCardBody();
    Anchor link = new Anchor(idValue, UriUtils.fromString(idValue).asString());
    link.getElement().setAttribute("target", "_blank");
    addField(body, messages.genericTitle(), link);

    card.add(body);
    return card;
  }

  private Widget buildAipPanel(String idValue) {
    IndexedAIP aip = linkingObjects.getAips().get(idValue);
    String href = aip != null ? HistoryUtils.createHistoryHashLink(HistoryUtils.getHistoryBrowse(aip.getId())) : null;

    FlowPanel card = buildCard();
    card.add(buildCardHeader(messages.intellectualEntity(), href, false));

    FlowPanel body = buildCardBody();
    if (aip != null) {
      if (StringUtils.isNotBlank(aip.getTitle())) {
        addIfNotBlank(body, messages.genericTitle(), aip.getTitle());
      } else
        addEmptyInnerMessage(body, messages.noTitleMessage());
    } else {
      addIdentifierNotFound(body, LinkingObjectUtils.getLinkingObjectPath(idValue));
    }
    card.add(body);
    return card;
  }

  private Widget buildRepresentationPanel(String idValue) {
    IndexedRepresentation representation = linkingObjects.getRepresentations().get(idValue);
    String href = representation != null
      ? HistoryUtils
        .createHistoryHashLink(HistoryUtils.getHistoryBrowse(representation.getAipId(), representation.getId()))
      : null;

    FlowPanel card = buildCard();
    card.add(buildCardHeader(messages.showRepresentationExtended(), href, false));

    FlowPanel body = buildCardBody();

    if (representation != null) {
      List<String> translatedStates = new ArrayList<>();
      for (String state : representation.getRepresentationStates()) {
        translatedStates.add(messages.statusLabel(state));
      }
      addIfNotBlank(body, messages.representationStatus(), StringUtils.prettyPrint(translatedStates));
    } else {
      addIdentifierNotFound(body, idValue);
    }
    card.add(body);
    return card;
  }

  private Widget buildFilePanel(String idValue) {
    IndexedFile file = linkingObjects.getFiles().get(idValue);
    String href = file != null
      ? HistoryUtils.createHistoryHashLink(
        HistoryUtils.getHistoryBrowse(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId()))
      : null;

    FlowPanel card = buildCard();
    card.add(buildCardHeader(messages.showFileExtended(), href, false));

    FlowPanel body = buildCardBody();

    if (file != null) {
      addIfNotBlank(body, messages.fileName(),
        StringUtils.isNotBlank(file.getOriginalName()) ? file.getOriginalName() : file.getId());

      List<String> filePath = file.getPath();
      if (filePath != null && !filePath.isEmpty()) {
        addIfNotBlank(body, messages.filePath(), StringUtils.join(filePath, "/"));
      }

      FileFormat fileFormat = file.getFileFormat();
      if (fileFormat != null) {
        String version = fileFormat.getFormatDesignationVersion() != null ? fileFormat.getFormatDesignationVersion()
          : "";
        String name = fileFormat.getFormatDesignationName() != null ? fileFormat.getFormatDesignationName() : "Unknown";
        addIfNotBlank(body, messages.fileFormat(), (name + " " + version).trim());

        addIfNotBlank(body, messages.fileMimetype(), fileFormat.getMimeType());
        addIfNotBlank(body, messages.filePronom(), fileFormat.getPronom());
      }
      addIfNotBlank(body, messages.fileSize(), Humanize.readableFileSize(file.getSize()));
    } else {
      addIdentifierNotFound(body, LinkingObjectUtils.getLinkingObjectPath(idValue));

    }
    card.add(body);
    return card;
  }

  private Widget buildTransferredResourcePanel(String idValue) {
    TransferredResource resource = linkingObjects.getTransferredResources().get(idValue);
    String href = resource != null ? HistoryUtils.createHistoryHashLink(IngestTransfer.RESOLVER, resource.getUUID())
      : null;

    FlowPanel card = buildCard();
    card.add(buildCardHeader(messages.showTransferredResourceExtended(), href, false));

    FlowPanel body = buildCardBody();
    if (resource != null) {
      addIfNotBlank(body, messages.transferredResourceName(), resource.getName());
      addIfNotBlank(body, messages.transferredResourcePath(), resource.getFullPath());

    } else {
      addIdentifierNotFound(body, LinkingObjectUtils.getLinkingObjectPath(idValue));
    }
    card.add(body);

    return card;
  }

  private boolean hasRenderableObjects(List<LinkingIdentifier> objects, String role) {
    if (objects == null || objects.isEmpty()) {
      return false;
    }

    for (LinkingIdentifier object : objects) {
      if (isRenderableObject(object, role)) {
        return true;
      }
    }

    return false;
  }

  private boolean isRenderableObject(LinkingIdentifier object, String role) {
    return object != null && object.getRoles() != null && object.getRoles().contains(role)
      && (RodaConstants.URN_TYPE.equalsIgnoreCase(object.getType())
        || RodaConstants.URI_TYPE.equalsIgnoreCase(object.getType()));
  }

  private void addIfNotBlank(FlowPanel panel, String label, String value) {
    if (StringUtils.isNotBlank(value)) {
      panel.add(buildField(label, new Label(value)));
    }
  }

  private void addField(FlowPanel panel, String label, Widget valueWidget) {
    if (valueWidget != null) {
      panel.add(buildField(label, valueWidget));
    }
  }

  private void addIdentifierNotFound(FlowPanel body, String value) {
    addIfNotBlank(body, messages.identifierNotFound(), value);
  }

  private void addEmptyInnerMessage(FlowPanel body, String messageHtml) {
    SimplePanel info = new SimplePanel();
    info.addStyleName("table-empty-inner");

    HTML label = new HTML(messageHtml);
    label.addStyleName("table-empty-inner-label");
    info.setWidget(label);

    body.add(info);
  }

  // common widget constructors, css to be changed
  private FlowPanel buildField(String labelText, Widget valueWidget) {
    FlowPanel field = new FlowPanel();
    field.setStyleName("field");

    Label label = new Label(labelText);
    label.setStyleName("label");

    FlowPanel value = new FlowPanel();
    value.setStyleName("value");
    value.add(valueWidget);

    field.add(label);
    field.add(value);
    return field;
  }

  private FlowPanel buildCard() {
    FlowPanel card = new FlowPanel();
    card.addStyleName("roda6Card");
    return card;
  }

  private FlowPanel buildCardHeader(String title, String href, boolean external) {
    FlowPanel header = new FlowPanel();
    header.addStyleName("preservation-event-tab-card-header");

    Label titleLabel = new Label(title);
    titleLabel.addStyleName("preservation-event-tab-card-title");
    header.add(titleLabel);

    if (StringUtils.isNotBlank(href)) {
      Anchor link = new Anchor();
      link.setHref(href);
      link.setTitle(title);
      link.addStyleName("preservation-event-tab-card-action");
      link.addStyleName("preservation-event-tab-card-action-open");

      if (external) {
        link.getElement().setAttribute("target", "_blank");
        link.getElement().setAttribute("rel", "noopener noreferrer");
      }

      header.add(link);
    }

    return header;
  }

  private FlowPanel buildCardBody() {
    FlowPanel body = new FlowPanel();
    body.addStyleName("cardBody");
    body.addStyleName("descriptiveMetadata");
    return body;
  }
}
