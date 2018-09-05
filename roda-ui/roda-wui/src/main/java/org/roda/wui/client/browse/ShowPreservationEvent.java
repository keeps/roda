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

import java.util.ArrayList;
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
import org.roda.wui.client.common.actions.PreservationEventActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.client.planning.ShowPreservationAgent;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestErrorOverlayType;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
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
      if (historyTokens.size() == 1) {
        final String eventId = historyTokens.get(0);
        ShowPreservationEvent preservationEvents = new ShowPreservationEvent(eventId);
        callback.onSuccess(preservationEvents);
      } else if (historyTokens.size() == 2) {
        final String aipId = historyTokens.get(0);
        final String eventId = historyTokens.get(1);
        ShowPreservationEvent preservationEvents = new ShowPreservationEvent(aipId, eventId);
        callback.onSuccess(preservationEvents);
      } else if (historyTokens.size() == 3) {
        final String aipId = historyTokens.get(0);
        final String representationUUID = historyTokens.get(1);
        final String eventId = historyTokens.get(2);
        ShowPreservationEvent preservationEvents = new ShowPreservationEvent(aipId, representationUUID, eventId);
        callback.onSuccess(preservationEvents);
      } else if (historyTokens.size() == 4) {
        final String aipId = historyTokens.get(0);
        final String representationUUID = historyTokens.get(1);
        final String fileUUID = historyTokens.get(2);
        final String eventId = historyTokens.get(3);
        ShowPreservationEvent preservationEvents = new ShowPreservationEvent(aipId, representationUUID, fileUUID,
          eventId);
        callback.onSuccess(preservationEvents);
      } else {
        HistoryUtils.newHistory(BrowseTop.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {BrowseTop.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(PreservationEvents.BROWSE_RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
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
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

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
  HTML eventOutcomeDetails;

  @UiField
  SimplePanel actionsSidebar;
  private ActionableWidgetBuilder<IndexedPreservationEvent> actionableWidgetBuilder;

  private String aipId;
  private String representationUUID;
  private String fileUUID;
  private String eventId;

  private PreservationEventViewBundle bundle;

  public ShowPreservationEvent(final String eventId) {
    this(null, eventId);
  }

  public ShowPreservationEvent(final String aipId, final String eventId) {
    this(aipId, null, eventId);
  }

  public ShowPreservationEvent(final String aipId, final String representationUUID, final String eventId) {
    this(aipId, representationUUID, null, eventId);
  }

  public ShowPreservationEvent(final String aipId, final String representationUUID, final String fileUUID,
    final String eventId) {
    this.aipId = aipId;
    this.representationUUID = representationUUID;
    this.fileUUID = fileUUID;
    this.eventId = eventId;

    initWidget(uiBinder.createAndBindUi(this));

    actionableWidgetBuilder = new ActionableWidgetBuilder<>(PreservationEventActions.get());

    BrowserService.Util.getInstance().retrievePreservationEventViewBundle(eventId,
      new AsyncCallback<PreservationEventViewBundle>() {

        @Override
        public void onFailure(Throwable caught) {
          if (caught instanceof NotFoundException) {
            Toast.showError(messages.notFoundError(), messages.couldNotFindPreservationEvent());
            HistoryUtils.newHistory(ListUtils.concat(PreservationEvents.PLANNING_RESOLVER.getHistoryPath()));
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
    eventDatetimeLabel.setText(Humanize.formatDateTime(event.getEventDateTime()));

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
        && sourceObjectId.getRoles().contains(RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE)
        && (RodaConstants.URN_TYPE.equalsIgnoreCase(sourceObjectId.getType())
          || RodaConstants.URI_TYPE.equalsIgnoreCase(sourceObjectId.getType()))) {
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
        && outcomeObjectId.getRoles().contains(RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME)
        && (RodaConstants.URN_TYPE.equalsIgnoreCase(outcomeObjectId.getType())
          || RodaConstants.URI_TYPE.equalsIgnoreCase(outcomeObjectId.getType()))) {
        addObjectPanel(outcomeObjectId, bundle, outcomeObjectsPanel);
        showOutcomeObjects = true;
      }
    }

    outcomeObjectsHeader.setVisible(showOutcomeObjects);
    outcomeObjectsPanel.setVisible(showOutcomeObjects);

    // OUTCOME DETAIL

    PluginState eventOutcome = PluginState.valueOf(event.getEventOutcome());
    eventOutcomeLabel.setText(messages.pluginStateMessage(eventOutcome));
    if (PluginState.SUCCESS.equals(eventOutcome)) {
      eventOutcomeLabel.setStyleName("label-success");
    } else if (PluginState.FAILURE.equals(eventOutcome)) {
      eventOutcomeLabel.setStyleName("label-danger");
    } else if (PluginState.PARTIAL_SUCCESS.equals(eventOutcome)) {
      eventOutcomeLabel.setStyleName("label-warning");
    }

    getEventDetailsHTML(new AsyncCallback<SafeHtml>() {

      @Override
      public void onFailure(Throwable caught) {
        if (!AsyncCallbackUtils.treatCommonFailures(caught)) {
          Toast.showError(messages.errorLoadingPreservationEventDetails(caught.getMessage()));
        }
      }

      @Override
      public void onSuccess(SafeHtml result) {
        eventOutcomeDetails.setHTML(result);
        outcomeDetailHeader.setVisible(result.asString().length() > 0);
      }
    });

    actionsSidebar.setWidget(actionableWidgetBuilder.buildListWithObjects(new ActionableObject<>(event)));
  }

  private void addObjectPanel(LinkingIdentifier object, PreservationEventViewBundle bundle, FlowPanel objectsPanel) {
    FlowPanel layout = new FlowPanel();
    layout.addStyleName("panel");
    String idValue = object.getValue();

    if (RodaConstants.URN_TYPE.equalsIgnoreCase(object.getType())) {
      RODA_TYPE type = LinkingObjectUtils.getLinkingIdentifierType(idValue);

      if (type == RODA_TYPE.TRANSFERRED_RESOURCE) {
        addTransferredResourcePanel(bundle, layout, idValue);
      } else if (type == RODA_TYPE.FILE) {
        addFilePanel(bundle, layout, idValue);
      } else if (type == RODA_TYPE.REPRESENTATION) {
        addRepresentationPanel(bundle, layout, idValue);
      } else if (type == RODA_TYPE.AIP) {
        addAipPanel(bundle, layout, idValue);
      }
    } else if (RodaConstants.URI_TYPE.equalsIgnoreCase(object.getType())) {
      addUriPanel(layout, idValue);
    }

    objectsPanel.add(layout);
  }

  private void addUriPanel(FlowPanel layout, String idValue) {
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

    FlowPanel footer = new FlowPanel();
    footer.addStyleName("panel-footer");
    layout.add(footer);

    Anchor link = new Anchor(messages.inspectPreservationAgent(),
      HistoryUtils.createHistoryHashLink(ShowPreservationAgent.RESOLVER, eventId, agent.getId()));

    link.addStyleName("btn");
    footer.add(link);
    return layout;
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
        HistoryUtils.createHistoryHashLink(HistoryUtils.getHistoryBrowse(iAIP.getId())));
      footer.add(link);

      link.addStyleName("btn");

    } else {
      Label idLabel = new Label(messages.identifierNotFound());
      idLabel.addStyleName("label");
      String path = LinkingObjectUtils.getLinkingObjectPath(idValue);
      Label identValue = new Label(path);
      identValue.addStyleName("value");

      body.add(idLabel);
      body.add(identValue);
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

      List<String> translatedStates = new ArrayList<>();
      for (String state : irep.getRepresentationStates()) {
        translatedStates.add(messages.statusLabel(state));
      }

      Label originalValue = new Label(StringUtils.prettyPrint(translatedStates));
      originalValue.addStyleName("value");

      body.add(originalLabel);
      body.add(originalValue);

      Anchor link = new Anchor(messages.inspectRepresentation(),
        HistoryUtils.createHistoryHashLink(HistoryUtils.getHistoryBrowse(irep.getAipId(), irep.getId())));

      link.addStyleName("btn");

      FlowPanel footer = new FlowPanel();
      footer.addStyleName("panel-footer");
      layout.add(footer);

      footer.add(link);
    } else {
      Label idLabel = new Label(messages.identifierNotFound());
      idLabel.addStyleName("label");
      Label identValue = new Label(idValue);
      identValue.addStyleName("value");

      body.add(idLabel);
      body.add(identValue);
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
      List<String> filePath = ifile.getPath();
      if (filePath != null && !filePath.isEmpty()) {
        pathLabel = new Label(messages.filePath());
        pathLabel.addStyleName("label");
        pathValue = new Label(StringUtils.join(filePath, "/"));
        pathValue.addStyleName("value");
      }

      Label formatLabel = new Label(messages.fileFormat());
      formatLabel.addStyleName("label");
      FileFormat fileFormat = ifile.getFileFormat();

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
        HistoryUtils.getHistoryBrowse(ifile.getAipId(), ifile.getRepresentationId(), filePath, ifile.getId())));

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
      Label identValue = new Label(path);
      identValue.addStyleName("value");

      body.add(idLabel);
      body.add(identValue);
    }
  }

  private void getEventDetailsHTML(final AsyncCallback<SafeHtml> callback) {
    IndexedPreservationEvent event = bundle.getEvent();
    SafeUri uri = RestUtils.createPreservationEventDetailsUri(eventId, event.getAipID(), event.getRepresentationUUID(),
      event.getFileUUID(), true, RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_HTML);

    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uri.asString());
    try {
      requestBuilder.sendRequest(null, new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (200 == response.getStatusCode()) {
            String html = response.getText();

            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='eventHTML'>"));
            b.append(SafeHtmlUtils.fromTrustedString(html));
            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
            SafeHtml safeHtml = b.toSafeHtml();

            callback.onSuccess(safeHtml);
          } else {
            String text = response.getText();
            String message;
            try {
              RestErrorOverlayType error = (RestErrorOverlayType) JsonUtils.safeEval(text);
              message = error.getMessage();
            } catch (IllegalArgumentException e) {
              message = text;
            }

            SafeHtmlBuilder b = new SafeHtmlBuilder();

            // error message
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='error'>"));
            b.append(messages.preservationEventDetailsTransformToHTMLError());
            b.append(SafeHtmlUtils.fromSafeConstant("<pre><code>"));
            b.append(SafeHtmlUtils.fromString(message));
            b.append(SafeHtmlUtils.fromSafeConstant("</core></pre>"));
            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));

            callback.onSuccess(b.toSafeHtml());
          }
        }

        @Override
        public void onError(Request request, Throwable exception) {
          callback.onFailure(exception);
        }
      });
    } catch (RequestException e) {
      callback.onFailure(e);
    }
  }
}
