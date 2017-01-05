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
package org.roda.wui.client.browse;

import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.RODA_TYPE;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.LinkingObjectUtils;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.wui.client.browse.bundle.PreservationEventViewBundle;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class ShowPreservationEvent extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      int size = historyTokens.size();
      if (size == 2 || size == 3) {
        final String aipId = historyTokens.get(0);
        final String representationId = size == 3 ? historyTokens.get(1) : null;
        final String eventId = historyTokens.get(historyTokens.size() - 1);
        ShowPreservationEvent preservationEvents = new ShowPreservationEvent(aipId, representationId, eventId);
        callback.onSuccess(preservationEvents);
      } else {
        HistoryUtils.newHistory(BrowseAIP.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {BrowseAIP.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return ListUtils.concat(PreservationEvents.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "event";
    }
  };

  public static final List<String> getViewItemHistoryToken(String id) {
    return ListUtils.concat(RESOLVER.getHistoryPath(), id);
  }

  interface MyUiBinder extends UiBinder<Widget, ShowPreservationEvent> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  @UiField
  SimplePanel itemIcon;

  @UiField
  Label itemTitle;

  @UiField
  Label eventIdValue;

  @UiField
  Label eventDatetimeLabel;

  @UiField
  Label eventTypeLabel;

  @UiField
  Label eventDetailLabel;

  @UiField
  Label agentsHeader;
  @UiField
  FlowPanel agentsPanel;

  @UiField
  Label sourceObjectsHeader;
  @UiField
  FlowPanel sourceObjectsPanel;

  @UiField
  Label outcomeObjectsHeader;
  @UiField
  FlowPanel outcomeObjectsPanel;

  @UiField
  Label eventOutcomeLabel;

  @UiField
  Label outcomeDetailHeader;
  @UiField
  Label eventOutcomeDetailNoteLabel;
  @UiField
  HTML eventOutcomeDetailNoteValue;
  @UiField
  Label eventOutcomeDetailExtensionLabel;
  @UiField
  HTML eventOutcomeDetailExtensionValue;

  @UiField
  Button backButton;

  private String aipId;
  private String representationId;

  private PreservationEventViewBundle bundle;

  /**
   * Create a new panel to edit a user
   * 
   * @param eventId
   * 
   * @param itemBundle
   * 
   */
  public ShowPreservationEvent(final String aipId, final String representationId, final String eventId) {
    this.aipId = aipId;
    this.representationId = representationId;

    initWidget(uiBinder.createAndBindUi(this));

    BrowserService.Util.getInstance().retrievePreservationEventViewBundle(eventId,
      new AsyncCallback<PreservationEventViewBundle>() {

        @Override
        public void onFailure(Throwable caught) {
          if (caught instanceof NotFoundException) {
            Toast.showError(messages.notFoundError(), messages.couldNotFindPreservationEvent());
            HistoryUtils.newHistory(ListUtils.concat(PreservationEvents.RESOLVER.getHistoryPath(), aipId));
          } else {
            AsyncCallbackUtils.defaultFailureTreatment(caught);
          }
        }

        @Override
        public void onSuccess(PreservationEventViewBundle eventBundle) {
          ShowPreservationEvent.this.bundle = eventBundle;
          viewAction();
        }
      });
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  public void viewAction() {
    IndexedPreservationEvent event = bundle.getEvent();

    eventIdValue.setText(event.getId());

    eventTypeLabel.setText(event.getEventType());
    eventDetailLabel.setText(event.getEventDetail());
    eventDatetimeLabel
      .setText(DateTimeFormat.getFormat(RodaConstants.DEFAULT_DATETIME_FORMAT).format(event.getEventDateTime()));

    // AGENTS
    Map<String, IndexedPreservationAgent> agents = bundle.getAgents();
    boolean hasAgents = false;

    for (LinkingIdentifier agentId : event.getLinkingAgentIds()) {
      IndexedPreservationAgent agent = agents.get(agentId.getValue());
      if (agent != null) {
        FlowPanel layout = createAgentPanel(agentId, agent);
        agentsPanel.add(layout);
        hasAgents = true;
      }
    }

    agentsHeader.setVisible(hasAgents);

    // Source objects
    boolean showSourceObjects = false;
    for (LinkingIdentifier sourceObjectId : event.getSourcesObjectIds()) {
      if (sourceObjectId.getRoles() != null
        && sourceObjectId.getRoles().contains(RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE)) {
        GWT.log("Event source id: " + sourceObjectId);
        addObjectPanel(sourceObjectId, bundle, sourceObjectsPanel);
        showSourceObjects = true;
      }
    }
    sourceObjectsHeader.setVisible(showSourceObjects);
    sourceObjectsPanel.setVisible(showSourceObjects);

    // Outcome objects
    boolean showOutcomeObjects = false;
    for (LinkingIdentifier outcomeObjectId : event.getOutcomeObjectIds()) {
      if (outcomeObjectId.getRoles() != null
        && outcomeObjectId.getRoles().contains(RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME)) {
        addObjectPanel(outcomeObjectId, bundle, outcomeObjectsPanel);
        showOutcomeObjects = true;
      }
    }

    outcomeObjectsHeader.setVisible(showOutcomeObjects);
    outcomeObjectsPanel.setVisible(showOutcomeObjects);

    // OUTCOME DETAIL

    outcomeDetailHeader.setVisible(StringUtils.isNotBlank(event.getEventOutcomeDetailNote())
      || StringUtils.isNotBlank(event.getEventOutcomeDetailExtension()));

    PluginState eventOutcome = PluginState.valueOf(event.getEventOutcome());
    eventOutcomeLabel.setText(messages.pluginStateMessage(eventOutcome));
    if (PluginState.SUCCESS.equals(eventOutcome)) {
      eventOutcomeLabel.setStyleName("label-success");
    } else if (PluginState.FAILURE.equals(eventOutcome)) {
      eventOutcomeLabel.setStyleName("label-danger");
    } else if (PluginState.PARTIAL_SUCCESS.equals(eventOutcome)) {
      eventOutcomeLabel.setStyleName("label-warning");
    }

    if (StringUtils.isNotBlank(event.getEventOutcomeDetailNote())) {
      eventOutcomeDetailNoteValue.setHTML(event.getEventOutcomeDetailNote());
    } else {
      eventOutcomeDetailNoteLabel.setVisible(false);
      eventOutcomeDetailNoteValue.setVisible(false);
    }

    if (StringUtils.isNotBlank(event.getEventOutcomeDetailExtension())) {
      eventOutcomeDetailExtensionValue.setHTML(event.getEventOutcomeDetailExtension());
    } else {
      eventOutcomeDetailExtensionLabel.setVisible(false);
      eventOutcomeDetailExtensionValue.setVisible(false);
    }

  }

  private FlowPanel createAgentPanel(LinkingIdentifier agentId, IndexedPreservationAgent agent) {
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

    if (!agentId.getRoles().isEmpty()) {
      Label rolesLabel = new Label(messages.preservationEventAgentRoles());
      rolesLabel.addStyleName("label");
      // TODO humanize list
      Label rolesValue = new Label(StringUtils.join(agentId.getRoles(), ", "));
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
    return layout;
  }

  private void addObjectPanel(LinkingIdentifier object, PreservationEventViewBundle bundle, FlowPanel objectsPanel) {

    FlowPanel layout = new FlowPanel();
    layout.addStyleName("panel");

    if (object.getType().equalsIgnoreCase("URN")) {
      String idValue = object.getValue();
      RODA_TYPE type = LinkingObjectUtils.getLinkingIdentifierType(idValue);

      if (type == RODA_TYPE.TRANSFERRED_RESOURCE) {
        addTransferredResourcePanel(bundle, layout, idValue);
      } else if (type == RODA_TYPE.FILE) {
        addFilePanel(bundle, layout, idValue);
      } else if (type == RODA_TYPE.REPRESENTATION) {
        addRepresentationPanel(bundle, layout, idValue);
      } else if (type == RODA_TYPE.AIP) {
        addAipPanel(bundle, layout, idValue);
      } else {
        // TODO send warning
      }

      objectsPanel.add(layout);
    }
  }

  private void addAipPanel(PreservationEventViewBundle bundle, FlowPanel layout, String idValue) {

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

    IndexedAIP iAIP = bundle.getAips().get(idValue);

    if (iAIP != null) {

      Label titleLabel = new Label(messages.genericTitle());
      titleLabel.addStyleName("label");
      Label titleValue = new Label(iAIP.getTitle());
      titleValue.addStyleName("value");

      body.add(titleLabel);
      body.add(titleValue);

      FlowPanel footer = new FlowPanel();
      footer.addStyleName("panel-footer");
      layout.add(footer);

      Anchor link = new Anchor(messages.inspectIntellectualEntity(),
        HistoryUtils.createHistoryHashLink(BrowseAIP.RESOLVER, iAIP.getId()));
      footer.add(link);

      link.addStyleName("btn");

    } else {
      Label idLabel = new Label(messages.identifierNotFound());
      idLabel.addStyleName("label");
      String path = LinkingObjectUtils.getLinkingObjectPath(idValue);
      Label id_Value = new Label(path);
      id_Value.addStyleName("value");

      body.add(idLabel);
      body.add(id_Value);
    }
  }

  private void addRepresentationPanel(PreservationEventViewBundle bundle, FlowPanel layout, String idValue) {

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

    IndexedRepresentation irep = bundle.getRepresentations().get(idValue);

    if (irep != null) {
      Label originalLabel = new Label(messages.representationStatus());
      originalLabel.addStyleName("label");
      Label originalValue = new Label(
        irep.isOriginal() ? messages.originalRepresentation() : messages.alternativeRepresentation());
      originalValue.addStyleName("value");

      body.add(originalLabel);
      body.add(originalValue);

      Anchor link = new Anchor(messages.inspectRepresentation(),
        HistoryUtils.createHistoryHashLink(BrowseRepresentation.RESOLVER, irep.getAipId(), irep.getId()));

      link.addStyleName("btn");

      FlowPanel footer = new FlowPanel();
      footer.addStyleName("panel-footer");
      layout.add(footer);

      footer.add(link);
    } else {
      Label idLabel = new Label(messages.identifierNotFound());
      idLabel.addStyleName("label");
      Label id_Value = new Label(idValue);
      id_Value.addStyleName("value");

      body.add(idLabel);
      body.add(id_Value);
    }
  }

  private void addFilePanel(PreservationEventViewBundle bundle, FlowPanel layout, String idValue) {

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

    IndexedFile ifile = bundle.getFiles().get(idValue);

    if (ifile != null) {
      Label nameLabel = new Label(messages.fileName());
      nameLabel.addStyleName("label");
      Label nameValue = new Label(
        StringUtils.isNotBlank(ifile.getOriginalName()) ? ifile.getOriginalName() : ifile.getId());
      nameValue.addStyleName("value");

      Label pathLabel = null;
      Label pathValue = null;
      if (ifile.getPath() != null && !ifile.getPath().isEmpty()) {
        pathLabel = new Label(messages.filePath());
        pathLabel.addStyleName("label");
        pathValue = new Label(StringUtils.join(ifile.getPath(), "/"));
        pathValue.addStyleName("value");
      }

      Label formatLabel = new Label(messages.fileFormat());
      formatLabel.addStyleName("label");
      FileFormat fileFormat = ifile.getFileFormat();

      String version = fileFormat.getFormatDesignationVersion() != null ? fileFormat.getFormatDesignationVersion() : "";
      String name = fileFormat.getFormatDesignationName() != null ? fileFormat.getFormatDesignationName() : "Unknown";
      Label formatValue = new Label(name + " " + version);
      formatValue.addStyleName("value");

      // TODO add pronom and mime type

      Label sizeLabel = new Label(messages.fileSize());
      sizeLabel.addStyleName("label");
      Label sizeValue = new Label(Humanize.readableFileSize(ifile.getSize()));
      sizeValue.addStyleName("value");

      body.add(nameLabel);
      body.add(nameValue);
      if (pathValue != null) {
        body.add(pathLabel);
        body.add(pathValue);
      }
      body.add(formatLabel);
      body.add(formatValue);
      body.add(sizeLabel);
      body.add(sizeValue);

      FlowPanel footer = new FlowPanel();
      footer.addStyleName("panel-footer");
      layout.add(footer);

      Anchor link = new Anchor(messages.inspectFile(), HistoryUtils.createHistoryHashLink(BrowseFile.RESOLVER,
        ifile.getAipId(), ifile.getRepresentationUUID(), ifile.getUUID()));

      link.addStyleName("btn");
      footer.add(link);

    } else {
      Label idLabel = new Label(messages.identifierNotFound());
      idLabel.addStyleName("label");
      String path = LinkingObjectUtils.getLinkingObjectPath(idValue);
      Label id_Value = new Label(path);
      id_Value.addStyleName("value");

      body.add(idLabel);
      body.add(id_Value);
    }
  }

  private void addTransferredResourcePanel(PreservationEventViewBundle bundle, FlowPanel layout, String idValue) {
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

    TransferredResource tr = bundle.getTransferredResources().get(idValue);

    if (tr != null) {
      Label nameLabel = new Label(messages.transferredResourceName());
      nameLabel.addStyleName("label");
      Label nameValue = new Label(tr.getName());
      nameValue.addStyleName("value");

      Label pathLabel = new Label(messages.transferredResourcePath());
      pathLabel.addStyleName("label");
      Label pathValue = new Label(tr.getFullPath());
      pathValue.addStyleName("value");

      body.add(nameLabel);
      body.add(nameValue);
      body.add(pathLabel);
      body.add(pathValue);

      FlowPanel footer = new FlowPanel();
      footer.addStyleName("panel-footer");
      layout.add(footer);

      Anchor link = new Anchor(messages.inspectTransferredResource(),
        HistoryUtils.createHistoryHashLink(IngestTransfer.RESOLVER, tr.getUUID()));
      link.addStyleName("btn");

      footer.add(link);

    } else {
      Label idLabel = new Label(messages.identifierNotFound());
      idLabel.addStyleName("label");
      String path = LinkingObjectUtils.getLinkingObjectPath(idValue);
      Label id_Value = new Label(path);
      id_Value.addStyleName("value");

      body.add(idLabel);
      body.add(id_Value);
    }
  }

  @UiHandler("backButton")
  void buttonBackHandler(ClickEvent e) {
    if (representationId == null) {
      HistoryUtils.newHistory(PreservationEvents.RESOLVER, aipId);
    } else {
      HistoryUtils.newHistory(PreservationEvents.RESOLVER, aipId, representationId);
    }
  }
}
