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

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.browse.bundle.DipBundle;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.DisseminationFileActions;
import org.roda.wui.client.common.lists.DIPFileList;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.slider.Sliders;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class BrowseDIP extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(final List<String> historyTokens, final AsyncCallback<Widget> callback) {
      BrowserService.Util.getInstance().retrieveViewersProperties(new AsyncCallback<Viewers>() {

        @Override
        public void onSuccess(Viewers viewers) {
          load(viewers, historyTokens, callback);
        }

        @Override
        public void onFailure(Throwable caught) {
          errorRedirect(callback);
        }
      });
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(BrowseTop.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "dip";
    }

    private void load(final Viewers viewers, final List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (!historyTokens.isEmpty()) {
        final String historyDipUUID = historyTokens.get(0);
        final String historyDipFileUUID = historyTokens.size() > 1 ? historyTokens.get(1) : null;

        BrowserService.Util.getInstance().retrieveDipBundle(historyDipUUID, historyDipFileUUID,
          new AsyncCallback<DipBundle>() {

            @Override
            public void onFailure(Throwable caught) {
              AsyncCallbackUtils.defaultFailureTreatment(caught);
            }

            @Override
            public void onSuccess(DipBundle dipBundle) {
              callback.onSuccess(new BrowseDIP(viewers, dipBundle));
            }
          });

      } else {
        errorRedirect(callback);
      }
    }

    private void errorRedirect(AsyncCallback<Widget> callback) {
      HistoryUtils.newHistory(BrowseTop.RESOLVER);
      callback.onSuccess(null);
    }

  };

  interface MyUiBinder extends UiBinder<Widget, BrowseDIP> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  public static final Sorter DEFAULT_DIPFILE_SORTER = new Sorter(new SortParameter(RodaConstants.DIPFILE_ID, false));

  private ClientLogger logger = new ClientLogger(getClass().getName());

  // interface

  @UiField
  AccessibleFocusPanel keyboardFocus;

  @UiField
  FlowPanel center;

  @UiField
  FlowPanel container;

  public BrowseDIP(Viewers viewers, DipBundle bundle) {
    // source
    IndexedAIP aip = bundle.getAip();
    IndexedRepresentation representation = bundle.getRepresentation();
    IndexedFile file = bundle.getFile();

    // target
    IndexedDIP dip = bundle.getDip();
    DIPFile dipFile = bundle.getDipFile();

    initWidget(uiBinder.createAndBindUi(this));

    if (dipFile != null) {
      center.add(new DipFilePreview(viewers, dipFile));
    } else if (dip.getOpenExternalURL() != null) {
      center.add(new DipUrlPreview(viewers, dip));
    } else {
      final Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIPFILE_DIP_ID, dip.getId()),
        new EmptyKeyFilterParameter(RodaConstants.DIPFILE_PARENT_UUID));

      ListBuilder<DIPFile> dipFileListBuilder = new ListBuilder<>(() -> new DIPFileList(),
        new AsyncTableCellOptions<>(DIPFile.class, "BrowseDIP_dipFiles").withFilter(filter)
          .withSummary(messages.allOfAObject(DIPFile.class.getName())).bindOpener());

      SearchWrapper search = new SearchWrapper(false).createListAndSearchPanel(dipFileListBuilder,
        DisseminationFileActions.get(dip.getPermissions()));

      SimplePanel layout = new SimplePanel();
      layout.add(search);
      center.add(layout);
      layout.addStyleName("browseDip-topList");
    }

    NavigationToolbar<IsIndexed> bottomNavigationToolbar = new NavigationToolbar<>();
    bottomNavigationToolbar.withAlternativeStyle(true);
    bottomNavigationToolbar.withObject(dipFile != null ? dipFile : dip);
    bottomNavigationToolbar.withPermissions(dip.getPermissions());
    bottomNavigationToolbar.updateBreadcrumb(bundle);
    bottomNavigationToolbar.setHeader(messages.catalogueDIPTitle());
    bottomNavigationToolbar.addStyleDependentName("alt");
    bottomNavigationToolbar.build();
    container.insert(bottomNavigationToolbar, 0);

    if (aip != null) {
      NavigationToolbar<IsIndexed> topNavigationToolbar = new NavigationToolbar<>();
      ListSelectionUtils.ProcessRelativeItem<IsIndexed> processor;
      IsIndexed referrer;
      String title;

      if (file != null) {
        referrer = file;
        processor = referredObject -> openReferred(referredObject,
          new Filter(new SimpleFilterParameter(RodaConstants.DIP_FILE_UUIDS, referrer.getUUID())));
        title = messages.catalogueFileTitle();
      } else if (representation != null) {
        referrer = representation;
        processor = referredObject -> openReferred(referredObject,
          new Filter(new SimpleFilterParameter(RodaConstants.DIP_REPRESENTATION_UUIDS, referredObject.getUUID())));
        title = messages.catalogueRepresentationTitle();
      } else {
        referrer = aip;
        processor = referredObject -> openReferred(referredObject,
          new Filter(new SimpleFilterParameter(RodaConstants.DIP_AIP_UUIDS, referredObject.getUUID())));
        title = messages.catalogueItemTitle();
      }

      topNavigationToolbar.setHeader(title);
      topNavigationToolbar.withObject(referrer);
      topNavigationToolbar.withProcessor(processor);
      topNavigationToolbar.withModifierKeys(true, true, false);
      topNavigationToolbar.withPermissions(bundle.getReferrerPermissions());
      topNavigationToolbar.updateReferrerBreadcrumb(bundle);
      topNavigationToolbar.build();
      Sliders.createDisseminationsSlider(center, topNavigationToolbar.getDisseminationsButton(), referrer);
      Sliders.createInfoSlider(center, topNavigationToolbar.getInfoSidebarButton(), referrer);

      container.insert(topNavigationToolbar, 0);
    }

    keyboardFocus.setFocus(true);
  }

  private static <T extends IsIndexed> void openReferred(final T object, Filter filter) {
    BrowserService.Util.getInstance().find(IndexedDIP.class.getName(), filter, DEFAULT_DIPFILE_SORTER,
      new Sublist(0, 1), Facets.NONE, LocaleInfo.getCurrentLocale().getLocaleName(), true,
      Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.DIP_ID), new AsyncCallback<IndexResult<IndexedDIP>>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(IndexResult<IndexedDIP> result) {
          if (result.getTotalCount() > 0) {
            // open DIP
            HistoryUtils.openBrowse(result.getResults().get(0));
          } else {
            // open object
            HistoryUtils.resolve(object);
          }
        }
      });
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.smoothScroll(keyboardFocus.getElement());
  }
}
