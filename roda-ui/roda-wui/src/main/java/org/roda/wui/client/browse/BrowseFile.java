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
import java.util.Collections;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.wui.client.browse.bundle.BrowseFileBundle;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.slider.SliderPanel;
import org.roda.wui.client.common.slider.Sliders;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;
import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;
import org.roda.wui.common.client.widgets.wcag.WCAGUtilities;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class BrowseFile extends Composite {

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
          Toast.showError(caught);
          errorRedirect(callback);
        }
      });
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(BrowseTop.RESOLVER, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(BrowseTop.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "file";
    }

    private void load(final Viewers viewers, final List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() > 2) {
        final String historyAipId = historyTokens.get(0);
        final String historyRepresentationId = historyTokens.get(1);
        final List<String> historyFilePath = new ArrayList<>(historyTokens.subList(2, historyTokens.size() - 1));
        final String historyFileId = historyTokens.get(historyTokens.size() - 1);

        BrowserService.Util.getInstance().retrieveBrowseFileBundle(historyAipId, historyRepresentationId,
          historyFilePath, historyFileId, Collections.emptyList(), new AsyncCallback<BrowseFileBundle>() {

            @Override
            public void onFailure(Throwable caught) {
              AsyncCallbackUtils.defaultFailureTreatment(caught);
            }

            @Override
            public void onSuccess(final BrowseFileBundle bundle) {
              instance = new BrowseFile(viewers, bundle);
              callback.onSuccess(instance);
            }
          });
      } else {
        errorRedirect(callback);
      }
    }

    private void errorRedirect(AsyncCallback<Widget> callback) {
      callback.onSuccess(null);
    }
  };

  interface MyUiBinder extends UiBinder<Widget, BrowseFile> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static BrowseFile instance = null;

  private final BrowseFileBundle bundle;
  private SliderPanel disseminationsSlider;

  private ClientLogger logger = new ClientLogger(getClass().getName());

  @UiField
  AccessibleFocusPanel keyboardFocus;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  HTML aipState;

  @UiField(provided = true)
  IndexedFilePreview filePreview;

  @UiField
  AccessibleFocusPanel optionsButton, infoFileButton, disseminationsButton, previousButton, nextButton;

  @UiField
  FlowPanel center;

  public BrowseFile(Viewers viewers, final BrowseFileBundle bundle) {
    this.bundle = bundle;
    final boolean justActive = AIPState.ACTIVE.equals(bundle.getAip().getState());

    // initialize preview
    filePreview = new IndexedFilePreview(viewers, bundle.getFile(), justActive, bundle.getAip().getPermissions(),
      new Command() {

        @Override
        public void execute() {
          Scheduler.get().scheduleDeferred(new Command() {
            @Override
            public void execute() {
              Filter filter = new Filter(
                new SimpleFilterParameter(RodaConstants.DIP_FILE_UUIDS, bundle.getFile().getUUID()));
              BrowserService.Util.getInstance().count(IndexedDIP.class.getName(), filter, justActive,
                new AsyncCallback<Long>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    AsyncCallbackUtils.defaultFailureTreatment(caught);
                  }

                  @Override
                  public void onSuccess(Long dipCount) {
                    if (dipCount > 0) {
                      disseminationsSlider.open();
                    }
                  }
                });
            }
          });
        }
      });

    // initialize widget
    initWidget(uiBinder.createAndBindUi(this));

    // breadcrumb
    breadcrumb.updatePath(getBreadcrumbs());
    breadcrumb.setVisible(true);

    // STATUS
    this.addStyleName(bundle.getAip().getState().toString().toLowerCase());
    aipState.setHTML(HtmlSnippetUtils.getAIPStateHTML(bundle.getAip().getState()));
    aipState.setVisible(AIPState.ACTIVE != bundle.getAip().getState());

    // set title
    infoFileButton.setTitle(messages.viewRepresentationInfoFileButton());

    // update visibles
    disseminationsButton.setVisible(bundle.getDipCount() > 0);

    // bind slider buttons
    disseminationsSlider = Sliders.createDisseminationsSlider(center, disseminationsButton, bundle.getFile());
    Sliders.createFileInfoSlider(center, infoFileButton, bundle);
    Sliders.createOptionsSlider(center, optionsButton, bundle.getFile());

    keyboardFocus.setFocus(true);

    // bind previous and next buttons
    ListSelectionUtils.bindLayout(bundle.getFile(), previousButton, nextButton, keyboardFocus, true, false, false);

    this.addStyleName("browse");
    this.addStyleName("browse-file");

    Element firstElement = this.getElement().getFirstChildElement();
    if ("input".equalsIgnoreCase(firstElement.getTagName())) {
      firstElement.setAttribute("title", "browse input");
    }

    WCAGUtilities.getInstance().makeAccessible(center.getElement());
  }

  private List<BreadcrumbItem> getBreadcrumbs() {
    return BreadcrumbUtils.getFileBreadcrumbs(getBundle());
  }

  public BrowseFileBundle getBundle() {
    return bundle;
  }

}
