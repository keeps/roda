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
import org.roda.core.data.v2.ip.metadata.PreservationEventsLinkingObjects;
import org.roda.wui.client.browse.tabs.BrowsePreservationEventTabs;
import org.roda.wui.client.common.BrowsePreservationEventActionsToolbar;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.*;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
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
        ShowPreservationEvent preservationEvents = new ShowPreservationEvent(historyTokens.get(0));
        callback.onSuccess(preservationEvents);
      } else {
        HistoryUtils.newHistory(PreservationEvents.PLANNING_RESOLVER);
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
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private final String eventId;
  private IndexedPreservationEvent preservationEvent;
  private List<IndexedPreservationAgent> agents;
  private PreservationEventsLinkingObjects linkingObjects;
  private String eventOutcomeDetailText;

  @UiField
  NavigationToolbar<IndexedPreservationEvent> navigationToolbar;

  @UiField
  FocusPanel focusPanel;

  @UiField
  BrowsePreservationEventActionsToolbar actionsToolbar;

  @UiField
  TitlePanel title;

  @UiField
  BrowsePreservationEventTabs tabs;

  public ShowPreservationEvent(final String eventId) {
    this.eventId = eventId;

    initWidget(uiBinder.createAndBindUi(this));
    focusPanel.setFocus(true);
    focusPanel.addStyleName("browse browse-file browse_main_panel");
    loadPreservationEvent();
  }

  public static final List<String> getViewItemHistoryToken(String id) {
    return ListUtils.concat(RESOLVER.getHistoryPath(), id);
  }

  private void getEventDetails(final AsyncCallback<String> callback) {
    SafeUri uri = RestUtils.createPreservationEventDetailsJsonUri(eventId);

    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uri.asString());
    requestBuilder.setHeader("Accept", "application/json");

    try {
      requestBuilder.sendRequest(null, new RequestCallback() {
        @Override
        public void onResponseReceived(Request request, Response response) {
          if (200 == response.getStatusCode()) {
            try {
              JSONObject json = JSONParser.parseStrict(response.getText()).isObject();
              String outcomeDetailNote = "";
              if (json != null && json.get("outcomeDetailNote") != null
                && json.get("outcomeDetailNote").isString() != null) {
                outcomeDetailNote = json.get("outcomeDetailNote").isString().stringValue();
              }

              callback.onSuccess(outcomeDetailNote);
            } catch (Exception e) {
              callback.onFailure(e);
            }
          } else {
            callback.onFailure(new RuntimeException(response.getText()));
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

  private void loadPreservationEvent() {
    Services services = new Services("Retrieve preservation event", "get");

    services.rodaEntityRestService(s -> s.findByUuid(eventId, LocaleInfo.getCurrentLocale().getLocaleName()),
      IndexedPreservationEvent.class).thenCompose(event -> {
        this.preservationEvent = event;
        return services.preservationEventsResource(s -> s.getPreservationAgents(event.getId()));
      }).thenCompose(indexedPreservationAgents -> {
        this.agents = indexedPreservationAgents;
        return services.preservationEventsResource(s -> s.getLinkingIdentifierObjects(preservationEvent.getId()));
      }).whenComplete((linkingObjectsResult, throwable) -> {
        if (throwable != null) {
          handleLoadFailure(throwable);
        } else {
          this.linkingObjects = linkingObjectsResult;
          loadEventDetailsHtml();
        }
      });
  }

  private void loadEventDetailsHtml() {
    getEventDetails(new AsyncCallback<String>() {
      @Override
      public void onFailure(Throwable caught) {
        if (!AsyncCallbackUtils.treatCommonFailures(caught)) {
          Toast.showError(messages.errorLoadingPreservationEventDetails(caught.getMessage()));
        }
      }

      @Override
      public void onSuccess(String result) {
        eventOutcomeDetailText = result;
        initView();
      }
    });
  }

  private void initView() {
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getPreservationEventBreadCrumbs(preservationEvent));
    navigationToolbar.withObject(preservationEvent).build();
    title.setText(StringUtils.isNotBlank(preservationEvent.getEventType()) ? preservationEvent.getEventType()
      : preservationEvent.getId());

    tabs.init(preservationEvent, agents, linkingObjects, eventOutcomeDetailText);

    if (actionsToolbar != null) {
      actionsToolbar.setLabel(messages.preservationEventTitle());
      actionsToolbar.setObjectAndBuild(preservationEvent, null, null);
    }
  }

  private void handleLoadFailure(Throwable throwable) {
    if (throwable instanceof NotFoundException) {
      Toast.showError(messages.notFoundError(), messages.couldNotFindPreservationEvent());
      HistoryUtils.newHistory(ListUtils.concat(PreservationEvents.PLANNING_RESOLVER.getHistoryPath()));
    } else {
      AsyncCallbackUtils.defaultFailureTreatment(throwable);
    }
  }

  interface MyUiBinder extends UiBinder<Widget, ShowPreservationEvent> {
  }
}
