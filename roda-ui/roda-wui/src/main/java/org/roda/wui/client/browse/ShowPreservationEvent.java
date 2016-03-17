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
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.LinkingObjectUtils;
import org.roda.core.data.v2.LinkingObjectUtils.LinkingObjectType;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncRequestUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria
 * 
 */
public class ShowPreservationEvent extends Composite {

  @SuppressWarnings("unused")
  private static final String TOP_ICON = "<span class='roda-logo'></span>";

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 2) {
        final String aipId = historyTokens.get(0);
        final String eventId = historyTokens.get(1);
        ShowPreservationEvent preservationEvents = new ShowPreservationEvent(aipId, eventId);
        callback.onSuccess(preservationEvents);
      } else {
        Tools.newHistory(Browse.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {Browse.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(PreservationEvents.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "event";
    }
  };

  public static final List<String> getViewItemHistoryToken(String id) {
    return Tools.concat(RESOLVER.getHistoryPath(), id);
  }

  interface MyUiBinder extends UiBinder<Widget, ShowPreservationEvent> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @SuppressWarnings("unused")
  private static BrowseMessages messages = (BrowseMessages) GWT.create(BrowseMessages.class);

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
  Label eventOutcomeDetailNoteLabel, eventOutcomeDetailNoteValue;
  @UiField
  Label eventOutcomeDetailExtensionLabel, eventOutcomeDetailExtensionValue;

  @UiField
  Button backButton;

  private String aipId;
  @SuppressWarnings("unused")
  private String eventId;

  private PreservationEventViewBundle bundle;

  /**
   * Create a new panel to edit a user
   * 
   * @param eventId
   * 
   * @param itemBundle
   * 
   */
  public ShowPreservationEvent(final String aipId, final String eventId) {
    this.aipId = aipId;
    this.eventId = eventId;

    initWidget(uiBinder.createAndBindUi(this));

    BrowserService.Util.getInstance().retrievePreservationEventViewBundle(eventId,
      new AsyncCallback<PreservationEventViewBundle>() {

        @Override
        public void onFailure(Throwable caught) {
          if (caught instanceof NotFoundException) {
            Toast.showError("Not found", "Could not find preservation event");
            Tools.newHistory(Tools.concat(PreservationEvents.RESOLVER.getHistoryPath(), aipId));
          } else {
            AsyncRequestUtils.defaultFailureTreatment(caught);
          }
        }

        @Override
        public void onSuccess(PreservationEventViewBundle eventBundle) {
          ShowPreservationEvent.this.bundle = eventBundle;
          viewAction();
        }
      });
  }

  public void viewAction() {
    IndexedPreservationEvent event = bundle.getEvent();

    eventIdValue.setText(event.getId());

    eventTypeLabel.setText(event.getEventType());
    eventDetailLabel.setText(event.getEventDetail());
    eventDatetimeLabel
      .setText(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL).format(event.getEventDateTime()));

    // AGENTS
    Map<String, IndexedPreservationAgent> agents = bundle.getAgents();

    for (LinkingIdentifier agentId : event.getLinkingAgentIds()) {
      IndexedPreservationAgent agent = agents.get(agentId.getValue());
      FlowPanel layout = createAgentPanel(agentId, agent);
      agentsPanel.add(layout);
    }

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

    String eventOutcome = event.getEventOutcome();
    eventOutcomeLabel.setText(eventOutcome);
    if (PluginState.SUCCESS.toString().equalsIgnoreCase(eventOutcome)) {
      eventOutcomeLabel.setStyleName("label-success");
    } else if (PluginState.FAILURE.toString().equalsIgnoreCase(eventOutcome)) {
      eventOutcomeLabel.setStyleName("label-danger");
    }

    if (StringUtils.isNotBlank(event.getEventOutcomeDetailNote())) {
      eventOutcomeDetailNoteValue.setText(event.getEventOutcomeDetailNote());
    } else {
      eventOutcomeDetailNoteLabel.setVisible(false);
      eventOutcomeDetailNoteValue.setVisible(false);
    }

    if (StringUtils.isNotBlank(event.getEventOutcomeDetailExtension())) {
      eventOutcomeDetailExtensionValue.setText(event.getEventOutcomeDetailExtension());
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

    if (!agentId.getRoles().isEmpty()) {
      Label typeLabel = new Label("Roles");
      typeLabel.addStyleName("label");
      // TODO humanize list
      Label typeValue = new Label(Tools.join(agentId.getRoles(), ", "));
      body.add(typeLabel);
      body.add(typeValue);
    }

    if (StringUtils.isNotBlank(agent.getType())) {
      Label typeLabel = new Label("Type");
      typeLabel.addStyleName("label");
      Label typeValue = new Label(agent.getType());
      body.add(typeLabel);
      body.add(typeValue);
    }

    if (StringUtils.isNotBlank(agent.getVersion())) {
      Label versionLabel = new Label("Version");
      versionLabel.addStyleName("label");
      Label versionValue = new Label(agent.getVersion());
      body.add(versionLabel);
      body.add(versionValue);
    }

    if (StringUtils.isNotBlank(agent.getNote())) {
      Label noteLabel = new Label("Note");
      noteLabel.addStyleName("label");
      Label noteValue = new Label(agent.getNote());
      body.add(noteLabel);
      body.add(noteValue);
    }

    if (StringUtils.isNotBlank(agent.getExtension())) {
      Label extensionLabel = new Label("Extension");
      extensionLabel.addStyleName("label");
      Label extensionValue = new Label(agent.getExtension());
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
      LinkingObjectType type = LinkingObjectUtils.getLinkingIdentifierType(idValue);

      if (type == LinkingObjectType.TRANSFERRED_RESOURCE) {
        addTransferredResourcePanel(bundle, layout, idValue);
      } else if (type == LinkingObjectType.FILE) {
        addFilePanel(bundle, layout, idValue);
      } else if (type == LinkingObjectType.REPRESENTATION) {
        addRepresentationPanel(bundle, layout, idValue);
      } else if (type == LinkingObjectType.AIP) {
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

    Label header = new Label("Intellectual entity");
    header.addStyleName("panel-title");
    header.addStyleName("h5");
    heading.add(header);

    IndexedAIP iAIP = bundle.getAips().get(idValue);

    if (iAIP != null) {

      Label titleLabel = new Label("Title");
      titleLabel.addStyleName("label");
      Label titleValue = new Label(iAIP.getTitle());

      body.add(titleLabel);
      body.add(titleValue);

      FlowPanel footer = new FlowPanel();
      footer.addStyleName("panel-footer");
      layout.add(footer);

      Anchor link = new Anchor("open", Tools.createHistoryHashLink(Browse.RESOLVER, iAIP.getId()));
      footer.add(link);

    } else {
      Label idLabel = new Label("Identifier (not found)");
      idLabel.addStyleName("label");
      String path = LinkingObjectUtils.getLinkingObjectPath(idValue);
      Label id_Value = new Label(path);

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

    Label header = new Label("Representation");
    header.addStyleName("panel-title");
    header.addStyleName("h5");
    heading.add(header);

    IndexedRepresentation irep = bundle.getRepresentations().get(idValue);

    if (irep != null) {

      Label originalLabel = new Label("Status");
      originalLabel.addStyleName("label");
      Label originalValue = new Label(irep.isOriginal() ? "original" : "alternative");

      body.add(originalLabel);
      body.add(originalValue);

      Anchor link = new Anchor("open",
        Tools.createHistoryHashLink(ViewRepresentation.RESOLVER, irep.getAipId(), irep.getId()));

      FlowPanel footer = new FlowPanel();
      footer.addStyleName("panel-footer");
      layout.add(footer);

      footer.add(link);
    } else {
      Label idLabel = new Label("Identifier (not found)");
      idLabel.addStyleName("label");
      Label id_Value = new Label(idValue);

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

    Label header = new Label("File");
    header.addStyleName("panel-title");
    header.addStyleName("h5");
    heading.add(header);

    IndexedFile ifile = bundle.getFiles().get(idValue);

    if (ifile != null) {
      Label nameLabel = new Label("Name");
      nameLabel.addStyleName("label");
      Label nameValue = new Label(
        StringUtils.isNotBlank(ifile.getOriginalName()) ? ifile.getOriginalName() : ifile.getId());

      Label pathLabel = null;
      Label pathValue = null;
      if (ifile.getPath() != null && !ifile.getPath().isEmpty()) {
        pathLabel = new Label("Path");
        pathLabel.addStyleName("label");
        pathValue = new Label(Tools.join(ifile.getPath(), "/"));
      }

      Label formatLabel = new Label("Format");
      formatLabel.addStyleName("label");
      FileFormat fileFormat = ifile.getFileFormat();
      // TODO guard nulls
      Label formatValue = new Label(
        fileFormat.getFormatDesignationName() + " " + fileFormat.getFormatDesignationVersion());

      // TODO add pronom and mime type

      Label sizeLabel = new Label("Size");
      sizeLabel.addStyleName("label");
      Label sizeValue = new Label(Humanize.readableFileSize(ifile.getSize()));

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

      Anchor link = new Anchor("open", Tools.createHistoryHashLink(ViewRepresentation.RESOLVER, ifile.getAipId(),
        ifile.getRepresentationUUID(), ifile.getUuid()));
      footer.add(link);

    } else {
      Label idLabel = new Label("Identifier (not found)");
      idLabel.addStyleName("label");
      String path = LinkingObjectUtils.getLinkingObjectPath(idValue);
      Label id_Value = new Label(path);

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

    Label header = new Label("Transferred resource");
    header.addStyleName("panel-title");
    header.addStyleName("h5");
    heading.add(header);

    TransferredResource tr = bundle.getTransferredResources().get(idValue);

    if (tr != null) {
      Label nameLabel = new Label("Name");
      nameLabel.addStyleName("label");
      Label nameValue = new Label(tr.getName());

      Label pathLabel = new Label("Path");
      pathLabel.addStyleName("label");
      Label pathValue = new Label(tr.getFullPath());

      body.add(nameLabel);
      body.add(nameValue);
      body.add(pathLabel);
      body.add(pathValue);

      FlowPanel footer = new FlowPanel();
      footer.addStyleName("panel-footer");
      layout.add(footer);

      Anchor link = new Anchor("open", Tools.createHistoryHashLink(IngestTransfer.RESOLVER, tr.getId()));
      footer.add(link);

    } else {
      Label idLabel = new Label("Identifier (not found)");
      idLabel.addStyleName("label");
      String path = LinkingObjectUtils.getLinkingObjectPath(idValue);
      Label id_Value = new Label(path);

      body.add(idLabel);
      body.add(id_Value);
    }
  }

  @UiHandler("backButton")
  void buttonBackHandler(ClickEvent e) {
    Tools.newHistory(Tools.concat(PreservationEvents.RESOLVER.getHistoryPath(), aipId));
  }
}
