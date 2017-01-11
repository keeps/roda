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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.browse.bundle.BrowseFileBundle;
import org.roda.wui.client.browse.bundle.BrowseRepresentationBundle;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.PreservationEventList;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.planning.Planning;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class PreservationEvents extends Composite {

  public static final HistoryResolver BROWSE_RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        final String aipId = historyTokens.get(0);
        PreservationEvents preservationEvents = new PreservationEvents(aipId);
        callback.onSuccess(preservationEvents);
      } else if (historyTokens.size() > 1
        && historyTokens.get(0).equals(ShowPreservationEvent.RESOLVER.getHistoryToken())) {
        ShowPreservationEvent.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.size() == 2) {
        final String aipId = historyTokens.get(0);
        final String representationUUID = historyTokens.get(1);
        PreservationEvents preservationEvents = new PreservationEvents(aipId, representationUUID);
        callback.onSuccess(preservationEvents);
      } else if (historyTokens.size() == 3) {
        final String aipId = historyTokens.get(0);
        final String representationUUID = historyTokens.get(1);
        final String fileUUID = historyTokens.get(2);
        PreservationEvents preservationEvents = new PreservationEvents(aipId, representationUUID, fileUUID);
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
      return ListUtils.concat(BrowseAIP.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "events";
    }
  };

  public static final HistoryResolver PLANNING_RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 0) {
        PreservationEvents preservationEvents = new PreservationEvents();
        callback.onSuccess(preservationEvents);
      } else {
        HistoryUtils.newHistory(PLANNING_RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    public List<String> getHistoryPath() {
      return ListUtils.concat(Planning.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "events";
    }
  };

  public static final List<String> getViewItemHistoryToken(String id) {
    return ListUtils.concat(BROWSE_RESOLVER.getHistoryPath(), id);
  }

  interface MyUiBinder extends UiBinder<Widget, PreservationEvents> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  @UiField(provided = true)
  FlowPanel facetClasses;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  SimplePanel itemIcon;

  @UiField
  Label itemTitle;

  @UiField(provided = true)
  SearchPanel eventSearch;

  @UiField(provided = true)
  PreservationEventList eventList;

  @UiField
  FlowPanel actionsPanel;

  @UiField
  Button downloadButton;

  @UiField
  Button backButton;

  private String aipId;
  private String representationUUID;
  private String fileUUID;

  /**
   * Create a new panel to edit a user
   * 
   * @param itemBundle
   * 
   */
  public PreservationEvents() {
    this(null);
  }

  public PreservationEvents(final String aipId) {
    this(aipId, null);
  }

  public PreservationEvents(final String aipId, final String representationUUID) {
    this(aipId, representationUUID, null);
  }

  public PreservationEvents(final String aipId, final String representationUUID, final String fileUUID) {
    this.aipId = aipId;
    this.representationUUID = representationUUID;
    this.fileUUID = fileUUID;

    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS));
    Filter filter = new Filter();

    if (aipId != null) {
      filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aipId));
    }

    if (representationUUID != null) {
      filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_REPRESENTATION_UUID, representationUUID));
    }

    if (fileUUID != null) {
      filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_FILE_UUID, fileUUID));
    }

    eventList = new PreservationEventList(filter, facets, messages.preservationEventsTitle(), false);

    facetClasses = new FlowPanel();
    Map<String, FlowPanel> facetPanels = new HashMap<String, FlowPanel>();
    facetPanels.put(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS, facetClasses);
    FacetUtils.bindFacets(eventList, facetPanels);

    eventList.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        IndexedPreservationEvent selected = eventList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          if (fileUUID != null) {
            HistoryUtils.newHistory(ShowPreservationEvent.RESOLVER, aipId, representationUUID, fileUUID,
              selected.getId());
          } else if (representationUUID != null) {
            HistoryUtils.newHistory(ShowPreservationEvent.RESOLVER, aipId, representationUUID, selected.getId());
          } else if (aipId != null) {
            HistoryUtils.newHistory(ShowPreservationEvent.RESOLVER, aipId, selected.getId());
          } else {
            HistoryUtils.newHistory(ShowPreservationEvent.RESOLVER, selected.getId());
          }
        }
      }
    });

    eventSearch = new SearchPanel(filter, RodaConstants.PRESERVATION_EVENT_SEARCH, true, messages.searchPlaceHolder(),
      false, false, true);
    eventSearch.setList(eventList);

    initWidget(uiBinder.createAndBindUi(this));
    actionsPanel.setVisible(aipId != null);

    // create breadcrumbs
    if (fileUUID != null) {
      getFileBreadCrumbs();
    } else if (representationUUID != null) {
      getRepresentationBreadCrumbs();
    } else if (aipId != null) {
      getAIPBreadCrumbs();
    } else {
      breadcrumb.setVisible(false);
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  private void getAIPBreadCrumbs() {
    BrowserService.Util.getInstance().retrieveBrowseAIPBundle(aipId, LocaleInfo.getCurrentLocale().getLocaleName(),
      new AsyncCallback<BrowseAIPBundle>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
          HistoryUtils.newHistory(BrowseAIP.RESOLVER);
        }

        @Override
        public void onSuccess(BrowseAIPBundle itemBundle) {
          breadcrumb
            .updatePath(BreadcrumbUtils.getAipBreadcrumbs(itemBundle.getAIPAncestors(), itemBundle.getAip(), true));
          breadcrumb.setVisible(true);
        }
      });
  }

  private void getRepresentationBreadCrumbs() {
    BrowserService.Util.getInstance().retrieve(IndexedRepresentation.class.getName(), representationUUID,
      new AsyncCallback<IndexedRepresentation>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(IndexedRepresentation representation) {
          BrowserService.Util.getInstance().retrieveBrowseRepresentationBundle(representation.getAipId(),
            representation.getId(), LocaleInfo.getCurrentLocale().getLocaleName(),
            new AsyncCallback<BrowseRepresentationBundle>() {

              @Override
              public void onFailure(Throwable caught) {
                AsyncCallbackUtils.defaultFailureTreatment(caught);
              }

              @Override
              public void onSuccess(BrowseRepresentationBundle repBundle) {
                breadcrumb.updatePath(BreadcrumbUtils.getRepresentationBreadcrumbs(repBundle));
                breadcrumb.setVisible(true);
              }
            });
        }
      });
  }

  private void getFileBreadCrumbs() {
    BrowserService.Util.getInstance().retrieve(IndexedFile.class.getName(), fileUUID, new AsyncCallback<IndexedFile>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(IndexedFile file) {
        BrowserService.Util.getInstance().retrieveBrowseFileBundle(file.getAipId(), file.getRepresentationId(),
          file.getPath(), file.getId(), LocaleInfo.getCurrentLocale().getLocaleName(),
          new AsyncCallback<BrowseFileBundle>() {

            @Override
            public void onFailure(Throwable caught) {
              AsyncCallbackUtils.defaultFailureTreatment(caught);
            }

            @Override
            public void onSuccess(BrowseFileBundle fileBundle) {
              breadcrumb.updatePath(BreadcrumbUtils.getFileBreadcrumbs(fileBundle));
              breadcrumb.setVisible(true);
            }
          });
      }
    });
  }

  @UiHandler("downloadButton")
  void buttonDownloadHandler(ClickEvent e) {
    if (aipId != null) {
      SafeUri downloadUri = RestUtils.createPreservationMetadataDownloadUri(aipId);
      if (downloadUri != null) {
        Window.Location.assign(downloadUri.asString());
      }
    }
  }

  @UiHandler("backButton")
  void buttonBackHandler(ClickEvent e) {
    if (fileUUID != null) {
      HistoryUtils.newHistory(HistoryUtils.getHistoryUuidResolver(IndexedFile.class.getName(), fileUUID));
    } else if (representationUUID != null) {
      HistoryUtils
        .newHistory(HistoryUtils.getHistoryUuidResolver(IndexedRepresentation.class.getName(), representationUUID));
    } else if (aipId != null) {
      HistoryUtils.newHistory(HistoryUtils.getHistoryBrowse(aipId));
    } else {
      // button not visible
    }
  }
}
