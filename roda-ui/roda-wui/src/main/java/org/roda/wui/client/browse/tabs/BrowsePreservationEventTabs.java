package org.roda.wui.client.browse.tabs;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.safehtml.shared.UriUtils;
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

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class BrowsePreservationEventTabs extends Tabs {

  private IndexedPreservationEvent event;
  private List<IndexedPreservationAgent> agents;
  private PreservationEventsLinkingObjects linkingObjects;
  private SafeHtml outcomeDetails;

  public void init(IndexedPreservationEvent event, List<IndexedPreservationAgent> agents,
    PreservationEventsLinkingObjects linkingObjects, SafeHtml outcomeDetails) {

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

    for (IndexedPreservationAgent agent : agents) {
      container.add(buildAgentPanel(agent));
    }

    return container;
  }

  private Widget buildSourceObjectsTab() {
    FlowPanel container = new FlowPanel();

    for (LinkingIdentifier sourceObjectId : linkingObjects.getSourceObjectIds()) {
      if (isRenderableObject(sourceObjectId, RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE)) {
        container.add(buildObjectPanel(sourceObjectId));
      }
    }

    return container;
  }

  private Widget buildOutcomeObjectsTab() {
    FlowPanel container = new FlowPanel();

    for (LinkingIdentifier outcomeObjectId : linkingObjects.getOutcomeObjectIds()) {
      if (isRenderableObject(outcomeObjectId, RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME)) {
        container.add(buildObjectPanel(outcomeObjectId));
      }
    }

    return container;
  }

  private Widget buildAgentPanel(IndexedPreservationAgent agent) {
    FlowPanel layout = new FlowPanel();
    layout.addStyleName("panel");

    FlowPanel heading = new FlowPanel();
    heading.addStyleName("panel-heading");
    layout.add(heading);

    FlowPanel body = new FlowPanel();
    body.addStyleName("panel-body");
    layout.add(body);

    if (StringUtils.isNotBlank(agent.getName())) {
      Label nameValue = new Label(agent.getName());
      nameValue.addStyleName("panel-title");
      heading.add(nameValue);
    } else {
      Label idValue = new Label(agent.getId());
      idValue.addStyleName("panel-title");
      heading.add(idValue);
    }

    if (StringUtils.isNotBlank(agent.getId())) {
      Label idLabel = new Label(messages.preservationEventAgentIdentifier());
      idLabel.addStyleName("label");
      Label idValue = new Label(agent.getId());
      idValue.addStyleName("value");
      body.add(idLabel);
      body.add(idValue);
    }

    if (agent.getRoles() != null && !agent.getRoles().isEmpty()) {
      Label rolesLabel = new Label(messages.preservationEventAgentRoles());
      rolesLabel.addStyleName("label");
      Label rolesValue = new Label(StringUtils.join(agent.getRoles(), ", "));
      rolesValue.addStyleName("value");
      body.add(rolesLabel);
      body.add(rolesValue);
    }

    if (StringUtils.isNotBlank(agent.getType())) {
      Label typeLabel = new Label(messages.preservationEventAgentType());
      typeLabel.addStyleName("label");
      Label typeValue = new Label(agent.getType());
      typeValue.addStyleName("value");
      body.add(typeLabel);
      body.add(typeValue);
    }

    if (StringUtils.isNotBlank(agent.getVersion())) {
      Label versionLabel = new Label(messages.preservationEventAgentVersion());
      versionLabel.addStyleName("label");
      Label versionValue = new Label(agent.getVersion());
      versionValue.addStyleName("value");
      body.add(versionLabel);
      body.add(versionValue);
    }

    if (StringUtils.isNotBlank(agent.getNote())) {
      Label noteLabel = new Label(messages.preservationEventAgentNote());
      noteLabel.addStyleName("label");
      Label noteValue = new Label(agent.getNote());
      noteValue.addStyleName("value");
      body.add(noteLabel);
      body.add(noteValue);
    }

    if (StringUtils.isNotBlank(agent.getExtension())) {
      Label extensionLabel = new Label(messages.preservationEventAgentExtension());
      extensionLabel.addStyleName("label");
      Label extensionValue = new Label(agent.getExtension());
      extensionValue.addStyleName("value");
      body.add(extensionLabel);
      body.add(extensionValue);
    }

    FlowPanel footer = new FlowPanel();
    footer.addStyleName("panel-footer");
    layout.add(footer);

    Anchor link = new Anchor(messages.inspectPreservationAgent(),
      HistoryUtils.createHistoryHashLink(ShowPreservationAgent.RESOLVER, agent.getId()));
    link.addStyleName("btn");
    footer.add(link);

    return layout;
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
    FlowPanel layout = new FlowPanel();
    layout.addStyleName("panel");

    FlowPanel heading = new FlowPanel();
    heading.addStyleName("panel-heading");
    layout.add(heading);

    FlowPanel body = new FlowPanel();
    body.addStyleName("panel-body");
    layout.add(body);

    Label header = new Label(messages.uriLinkingIdentifierTitle());
    header.addStyleName("panel-title");
    header.addStyleName("h5");
    heading.add(header);

    Label titleLabel = new Label(messages.genericTitle());
    titleLabel.addStyleName("label");
    Anchor link = new Anchor(idValue, UriUtils.fromString(idValue).asString());
    link.getElement().setAttribute("target", "_blank");

    body.add(titleLabel);
    body.add(link);

    return layout;
  }

  private Widget buildAipPanel(String idValue) {
    FlowPanel layout = new FlowPanel();
    layout.addStyleName("panel");

    FlowPanel heading = new FlowPanel();
    heading.addStyleName("panel-heading");
    layout.add(heading);

    FlowPanel body = new FlowPanel();
    body.addStyleName("panel-body");
    layout.add(body);

    Label header = new Label(messages.intellectualEntity());
    header.addStyleName("panel-title");
    header.addStyleName("h5");
    heading.add(header);

    IndexedAIP aip = linkingObjects.getAips().get(idValue);

    if (aip != null) {
      Label titleLabel = new Label(messages.genericTitle());
      titleLabel.addStyleName("label");
      Label titleValue = new Label(aip.getTitle());
      titleValue.addStyleName("value");

      body.add(titleLabel);
      body.add(titleValue);

      FlowPanel footer = new FlowPanel();
      footer.addStyleName("panel-footer");
      layout.add(footer);

      Anchor link = new Anchor(messages.inspectIntellectualEntity(),
        HistoryUtils.createHistoryHashLink(HistoryUtils.getHistoryBrowse(aip.getId())));
      link.addStyleName("btn");
      footer.add(link);
    } else {
      Label idLabel = new Label(messages.identifierNotFound());
      idLabel.addStyleName("label");

      String path = LinkingObjectUtils.getLinkingObjectPath(idValue);
      Label identValue = new Label(path);
      identValue.addStyleName("value");

      body.add(idLabel);
      body.add(identValue);
    }

    return layout;
  }

  private Widget buildRepresentationPanel(String idValue) {
    FlowPanel layout = new FlowPanel();
    layout.addStyleName("panel");

    FlowPanel heading = new FlowPanel();
    heading.addStyleName("panel-heading");
    layout.add(heading);

    FlowPanel body = new FlowPanel();
    body.addStyleName("panel-body");
    layout.add(body);

    Label header = new Label(messages.showRepresentationExtended());
    header.addStyleName("panel-title");
    header.addStyleName("h5");
    heading.add(header);

    IndexedRepresentation representation = linkingObjects.getRepresentations().get(idValue);

    if (representation != null) {
      Label originalLabel = new Label(messages.representationStatus());
      originalLabel.addStyleName("label");

      List<String> translatedStates = new ArrayList<>();
      for (String state : representation.getRepresentationStates()) {
        translatedStates.add(messages.statusLabel(state));
      }

      Label originalValue = new Label(StringUtils.prettyPrint(translatedStates));
      originalValue.addStyleName("value");

      body.add(originalLabel);
      body.add(originalValue);

      FlowPanel footer = new FlowPanel();
      footer.addStyleName("panel-footer");
      layout.add(footer);

      Anchor link = new Anchor(messages.inspectRepresentation(), HistoryUtils
        .createHistoryHashLink(HistoryUtils.getHistoryBrowse(representation.getAipId(), representation.getId())));
      link.addStyleName("btn");
      footer.add(link);
    } else {
      Label idLabel = new Label(messages.identifierNotFound());
      idLabel.addStyleName("label");
      Label identValue = new Label(idValue);
      identValue.addStyleName("value");

      body.add(idLabel);
      body.add(identValue);
    }

    return layout;
  }

  private Widget buildFilePanel(String idValue) {
    FlowPanel layout = new FlowPanel();
    layout.addStyleName("panel");

    FlowPanel heading = new FlowPanel();
    heading.addStyleName("panel-heading");
    layout.add(heading);

    FlowPanel body = new FlowPanel();
    body.addStyleName("panel-body");
    layout.add(body);

    Label header = new Label(messages.showFileExtended());
    header.addStyleName("panel-title");
    header.addStyleName("h5");
    heading.add(header);

    IndexedFile file = linkingObjects.getFiles().get(idValue);

    if (file != null) {
      Label nameLabel = new Label(messages.fileName());
      nameLabel.addStyleName("label");
      Label nameValue = new Label(
        StringUtils.isNotBlank(file.getOriginalName()) ? file.getOriginalName() : file.getId());
      nameValue.addStyleName("value");

      Label pathLabel = null;
      Label pathValue = null;
      List<String> filePath = file.getPath();
      if (filePath != null && !filePath.isEmpty()) {
        pathLabel = new Label(messages.filePath());
        pathLabel.addStyleName("label");
        pathValue = new Label(StringUtils.join(filePath, "/"));
        pathValue.addStyleName("value");
      }

      Label formatLabel = new Label(messages.fileFormat());
      formatLabel.addStyleName("label");
      FileFormat fileFormat = file.getFileFormat();

      String version = fileFormat.getFormatDesignationVersion() != null ? fileFormat.getFormatDesignationVersion() : "";
      String name = fileFormat.getFormatDesignationName() != null ? fileFormat.getFormatDesignationName() : "Unknown";
      Label formatValue = new Label(name + " " + version);
      formatValue.addStyleName("value");

      Label mimetypeLabel = new Label(messages.fileMimetype());
      mimetypeLabel.addStyleName("label");
      String fileMimetype = fileFormat.getMimeType();

      String mimetype = fileMimetype != null ? fileMimetype : "";
      Label mimetypeValue = new Label(mimetype);
      mimetypeValue.addStyleName("value");

      Label pronomLabel = new Label(messages.filePronom());
      pronomLabel.addStyleName("label");
      String filePronom = fileFormat.getPronom();

      String pronom = filePronom != null ? filePronom : "";
      Label pronomValue = new Label(pronom);
      pronomValue.addStyleName("value");

      Label sizeLabel = new Label(messages.fileSize());
      sizeLabel.addStyleName("label");
      Label sizeValue = new Label(Humanize.readableFileSize(file.getSize()));
      sizeValue.addStyleName("value");

      body.add(nameLabel);
      body.add(nameValue);
      if (pathValue != null) {
        body.add(pathLabel);
        body.add(pathValue);
      }
      body.add(formatLabel);
      body.add(formatValue);
      if (StringUtils.isNotBlank(fileMimetype)) {
        body.add(mimetypeLabel);
        body.add(mimetypeValue);
      }
      if (StringUtils.isNotBlank(filePronom)) {
        body.add(pronomLabel);
        body.add(pronomValue);
      }
      body.add(sizeLabel);
      body.add(sizeValue);

      FlowPanel footer = new FlowPanel();
      footer.addStyleName("panel-footer");
      layout.add(footer);

      Anchor link = new Anchor(messages.inspectFile(), HistoryUtils.createHistoryHashLink(
        HistoryUtils.getHistoryBrowse(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId())));
      link.addStyleName("btn");
      footer.add(link);
    } else {
      Label idLabel = new Label(messages.identifierNotFound());
      idLabel.addStyleName("label");

      String path = LinkingObjectUtils.getLinkingObjectPath(idValue);
      Label identValue = new Label(path);
      identValue.addStyleName("value");

      body.add(idLabel);
      body.add(identValue);
    }

    return layout;
  }

  private Widget buildTransferredResourcePanel(String idValue) {
    FlowPanel layout = new FlowPanel();
    layout.addStyleName("panel");

    FlowPanel heading = new FlowPanel();
    heading.addStyleName("panel-heading");
    layout.add(heading);

    FlowPanel body = new FlowPanel();
    body.addStyleName("panel-body");
    layout.add(body);

    Label header = new Label(messages.showTransferredResourceExtended());
    header.addStyleName("panel-title");
    header.addStyleName("h5");
    heading.add(header);

    TransferredResource resource = linkingObjects.getTransferredResources().get(idValue);

    if (resource != null) {
      Label nameLabel = new Label(messages.transferredResourceName());
      nameLabel.addStyleName("label");
      Label nameValue = new Label(resource.getName());
      nameValue.addStyleName("value");

      Label pathLabel = new Label(messages.transferredResourcePath());
      pathLabel.addStyleName("label");
      Label pathValue = new Label(resource.getFullPath());
      pathValue.addStyleName("value");

      body.add(nameLabel);
      body.add(nameValue);
      body.add(pathLabel);
      body.add(pathValue);

      FlowPanel footer = new FlowPanel();
      footer.addStyleName("panel-footer");
      layout.add(footer);

      Anchor link = new Anchor(messages.inspectTransferredResource(),
        HistoryUtils.createHistoryHashLink(IngestTransfer.RESOLVER, resource.getUUID()));
      link.addStyleName("btn");
      footer.add(link);
    } else {
      Label idLabel = new Label(messages.identifierNotFound());
      idLabel.addStyleName("label");

      String path = LinkingObjectUtils.getLinkingObjectPath(idValue);
      Label identValue = new Label(path);
      identValue.addStyleName("value");

      body.add(idLabel);
      body.add(identValue);
    }

    return layout;
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
}
