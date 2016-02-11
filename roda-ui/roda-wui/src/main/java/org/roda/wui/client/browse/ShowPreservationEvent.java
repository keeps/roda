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

import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncRequestUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.common.client.HistoryResolver;
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
  Label eventObjectIdValue;

  @UiField
  Label eventObjectRoleLabel, eventObjectRoleValue;

  @UiField
  Label eventOutcomeLabel;
  @UiField
  Label eventOutcomeDetailNoteLabel, eventOutcomeDetailNoteValue;
  @UiField
  Label eventOutcomeDetailExtensionLabel, eventOutcomeDetailExtensionValue;

  @UiField
  Button backButton;

  private String aipId;
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
    List<IndexedPreservationAgent> agents = bundle.getAgents();
    for (IndexedPreservationAgent agent : agents) {
      FlowPanel layout = new FlowPanel();

      Label idLabel = new Label(messages.preservationEventAgentId());
      idLabel.addStyleName("label");
      Label idValue = new Label(
        messages.preservationEventAgentIdValue(agent.getIdentifierValue(), agent.getIdentifierType()));

      layout.add(idLabel);
      layout.add(idValue);

      agentsPanel.add(layout);

      // <g:Label styleName="label">
      // <ui:text from='{messages.preservationEventAgentId}' />
      // </g:Label>
      // <g:Label ui:field="eventAgentIdLabel" />
      //
      // <g:Label ui:field="eventAgentRoleLabel" styleName="label">
      // <ui:text from='{messages.preservationEventAgentRole}' />
      // </g:Label>
      // <g:Label ui:field="eventAgentRoleValue" />
    }

    // OBJECTS
    // TODO set links
    // TODO... update with new structure (list of objects instead of single
    // agent...)
    /*
     * eventObjectIdValue.setText(event.getObjectIdentifierValue());
     * eventObjectRoleLabel.setVisible(StringUtils.isNotBlank(event.
     * getObjectRole()));
     * eventObjectRoleValue.setVisible(StringUtils.isNotBlank(event.
     * getObjectRole())); eventObjectRoleValue.setText(event.getObjectRole());
     */
    // OUTCOME
    eventOutcomeLabel.setText(event.getEventOutcome());

    // TODO add event outcome detail note when available
    eventOutcomeDetailNoteLabel.setVisible(false);
    eventOutcomeDetailNoteValue.setVisible(false);
    // eventOutcomeDetailNoteValue.setText(event.getEventOutcomeDetailNote());

    eventOutcomeDetailExtensionLabel.setVisible(StringUtils.isNotBlank(event.getEventOutcomeDetailExtension()));
    eventOutcomeDetailExtensionValue.setVisible(StringUtils.isNotBlank(event.getEventOutcomeDetailExtension()));
    eventOutcomeDetailExtensionValue.setText(event.getEventOutcomeDetailExtension());
  }

  @UiHandler("backButton")
  void buttonBackHandler(ClickEvent e) {
    Tools.newHistory(Tools.concat(PreservationEvents.RESOLVER.getHistoryPath(), aipId));
  }
}
