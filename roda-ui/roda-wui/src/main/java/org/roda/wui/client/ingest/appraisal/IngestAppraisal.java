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
package org.roda.wui.client.ingest.appraisal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.facet.FacetParameter;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.Dialogs;
import org.roda.wui.client.common.LoadingAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.search.MainSearch;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.ingest.Ingest;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class IngestAppraisal extends Composite {

  private static final Filter BASE_FILTER = new Filter(
    new SimpleFilterParameter(RodaConstants.STATE, AIPState.UNDER_APPRAISAL.toString()));

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return Tools.concat(Ingest.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "appraisal";
    }
  };
  
  private static IngestAppraisal instance = null;

  public static IngestAppraisal getInstance() {
    if (instance == null) {
      instance = new IngestAppraisal();
    }
    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, IngestAppraisal> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  @UiField
  FlowPanel ingestAppraisalDescription;

  @UiField(provided = true)
  MainSearch mainSearch;

  // FILTERS
  @UiField(provided = true)
  FlowPanel itemsFacets;
  @UiField(provided = true)
  FlowPanel facetDescriptionLevels;
  @UiField(provided = true)
  FlowPanel facetHasRepresentations;

  @UiField(provided = true)
  FlowPanel filesFacets;
  @UiField(provided = true)
  FlowPanel facetFormats;
  @UiField(provided = true)
  FlowPanel facetPronoms;
  @UiField(provided = true)
  FlowPanel facetMimetypes;

  @UiField(provided = true)
  Button acceptButton, rejectButton;

  // cannot let reps and files to be selectable for now
  // boolean selectable = true;
  boolean itemsSelectable = true;
  boolean representationsSelectable = false;
  boolean filesSelectable = false;
  boolean justActive = false;

  private IngestAppraisal() {
    // Variables
    itemsFacets = new FlowPanel();
    facetDescriptionLevels = new FlowPanel();
    facetHasRepresentations = new FlowPanel();

    filesFacets = new FlowPanel();
    facetFormats = new FlowPanel();
    facetPronoms = new FlowPanel();
    facetMimetypes = new FlowPanel();

    acceptButton = new Button();
    rejectButton = new Button();

    // Define facets and facets panels
    Map<FacetParameter, FlowPanel> itemsFacetsMap = new HashMap<FacetParameter, FlowPanel>();
    Map<FacetParameter, FlowPanel> representationsFacetsMap = new HashMap<FacetParameter, FlowPanel>();
    Map<FacetParameter, FlowPanel> filesFacetsMap = new HashMap<FacetParameter, FlowPanel>();

    itemsFacetsMap.put(new SimpleFacetParameter(RodaConstants.AIP_LEVEL), facetDescriptionLevels);
    itemsFacetsMap.put(new SimpleFacetParameter(RodaConstants.AIP_HAS_REPRESENTATIONS), facetHasRepresentations);

    filesFacetsMap.put(new SimpleFacetParameter(RodaConstants.FILE_FILEFORMAT), facetFormats);
    filesFacetsMap.put(new SimpleFacetParameter(RodaConstants.FILE_PRONOM), facetPronoms);
    filesFacetsMap.put(new SimpleFacetParameter(RodaConstants.FILE_FORMAT_MIMETYPE), facetMimetypes);

    // Define hide/visible buttons
    Map<Button, Boolean> itemsButtons = new HashMap<Button, Boolean>();
    Map<Button, Boolean> representationsButtons = new HashMap<Button, Boolean>();
    Map<Button, Boolean> filesButtons = new HashMap<Button, Boolean>();

    // Define active buttons
    List<Button> itemsSelectionButtons = new ArrayList<>();
    List<Button> representationsSelectionButtons = new ArrayList<>();
    List<Button> filesSelectionButtons = new ArrayList<>();

    itemsSelectionButtons.add(acceptButton);
    itemsSelectionButtons.add(rejectButton);
    representationsSelectionButtons.add(acceptButton);
    representationsSelectionButtons.add(rejectButton);
    filesSelectionButtons.add(acceptButton);
    filesSelectionButtons.add(rejectButton);

    // Create main search
    mainSearch = new MainSearch(justActive, itemsSelectable, representationsSelectable, filesSelectable, itemsFacets,
      itemsFacetsMap, itemsButtons, itemsSelectionButtons, new FlowPanel(), representationsFacetsMap,
      representationsButtons, representationsSelectionButtons, filesFacets, filesFacetsMap, filesButtons,
      filesSelectionButtons);

    initWidget(uiBinder.createAndBindUi(this));

    ingestAppraisalDescription.add(new HTMLWidgetWrapper("IngestAppraisalDescription.html"));

    acceptButton.setEnabled(false);
    rejectButton.setEnabled(false);
  }
  
  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }
  
  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    mainSearch.setDefaultFilters(BASE_FILTER);
    if (historyTokens.isEmpty()) {
      mainSearch.search();
      callback.onSuccess(this);
    } else {
      // #search/TYPE/key/value/key/value
      boolean successful = mainSearch.setSearch(historyTokens);
      if (successful) {
        mainSearch.search();
        callback.onSuccess(this);
      } else {
        Tools.newHistory(RESOLVER);
        callback.onSuccess(null);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @UiHandler("acceptButton")
  void buttonAcceptHandler(ClickEvent e) {
    boolean accept = true;
    SelectedItems<?> selected = mainSearch.getSelected();
    String rejectReason = null;
    // not supporting accept of reps and files for now
    BrowserService.Util.getInstance().appraisal((SelectedItems<IndexedAIP>) selected, accept, rejectReason,
      new LoadingAsyncCallback<Void>() {

        @Override
        public void onSuccessImpl(Void result) {
          Toast.showInfo(messages.dialogDone(), messages.allItemsWereAccepted());
          mainSearch.refresh();
        }
      });
  }

  @UiHandler("rejectButton")
  void buttonRejectHandler(ClickEvent e) {
    final boolean accept = false;
    final SelectedItems<?> selected = mainSearch.getSelected();
    Dialogs.showPromptDialog(messages.rejectMessage(), messages.rejectSIPQuestion(), null, RegExp.compile(".+"),
      messages.dialogCancel(), messages.dialogOk(), new AsyncCallback<String>() {

        @Override
        public void onFailure(Throwable caught) {
          // nothing to do
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onSuccess(final String rejectReason) {
          // TODO support accept of reps and files
          BrowserService.Util.getInstance().appraisal((SelectedItems<IndexedAIP>) selected, accept, rejectReason,
            new LoadingAsyncCallback<Void>() {

            @Override
            public void onSuccessImpl(Void result) {
              Toast.showInfo(messages.dialogDone(), messages.allItemsWereAccepted());
              mainSearch.refresh();
            }
          });
        }
      });
  }
}
