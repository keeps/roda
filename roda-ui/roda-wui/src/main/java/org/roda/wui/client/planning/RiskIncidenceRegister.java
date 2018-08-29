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
package org.roda.wui.client.planning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.BrowseTop;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.browse.bundle.BrowseFileBundle;
import org.roda.wui.client.browse.bundle.BrowseRepresentationBundle;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.RiskIncidenceActions;
import org.roda.wui.client.common.lists.RiskIncidenceList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class RiskIncidenceRegister extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.isEmpty()) {
        RiskIncidenceRegister riskIncidences = new RiskIncidenceRegister(null, null, null, null,
          SearchFilters.allFilter());
        callback.onSuccess(riskIncidences);
      } else if (historyTokens.size() == 2
        && historyTokens.get(0).equals(ShowRiskIncidence.RESOLVER.getHistoryToken())) {
        ShowRiskIncidence.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.size() == 2
        && historyTokens.get(0).equals(EditRiskIncidence.RESOLVER.getHistoryToken())) {
        EditRiskIncidence.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.size() == 1) {
        final String aipId = historyTokens.get(0);
        Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_AIP_ID, aipId));
        RiskIncidenceRegister riskIncidences = new RiskIncidenceRegister(aipId, null, null, null, filter);
        callback.onSuccess(riskIncidences);
      } else if (historyTokens.size() == 2) {
        final String aipId = historyTokens.get(0);
        final String representationId = historyTokens.get(1);
        Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_AIP_ID, aipId),
          new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID, representationId));
        RiskIncidenceRegister riskIncidences = new RiskIncidenceRegister(aipId, representationId, null, null, filter);
        callback.onSuccess(riskIncidences);
      } else if (historyTokens.size() >= 3) {
        List<String> filePath = new ArrayList<>(historyTokens);
        final String aipId = filePath.remove(0);
        final String representationId = filePath.remove(0);
        final String fileId = filePath.remove(filePath.size() - 1);

        Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_AIP_ID, aipId),
          new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID, representationId),
          new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_FILE_ID, fileId));

        if (!filePath.isEmpty()) {
          filter.add(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_FILE_PATH_COMPUTED,
            StringUtils.join(filePath, RodaConstants.RISK_INCIDENCE_FILE_PATH_COMPUTED_SEPARATOR)));
        }

        RiskIncidenceRegister riskIncidences = new RiskIncidenceRegister(aipId, representationId, filePath, fileId,
          filter);
        callback.onSuccess(riskIncidences);
      } else {
        HistoryUtils.newHistory(RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {RiskIncidenceRegister.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Planning.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "riskincidenceregister";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, RiskIncidenceRegister> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final List<String> aipFieldsToReturn = new ArrayList<>(
    Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_GHOST, RodaConstants.AIP_TITLE, RodaConstants.AIP_LEVEL));

  private static final List<String> representationFieldsToReturn = new ArrayList<>(
    Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.REPRESENTATION_AIP_ID, RodaConstants.REPRESENTATION_ID,
      RodaConstants.REPRESENTATION_TYPE));

  private static final List<String> fileFieldsToReturn = new ArrayList<>(
    Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.FILE_PARENT_UUID, RodaConstants.FILE_PATH,
      RodaConstants.FILE_ANCESTORS_PATH, RodaConstants.FILE_ORIGINALNAME, RodaConstants.INDEX_ID,
      RodaConstants.FILE_AIP_ID, RodaConstants.FILE_REPRESENTATION_ID, RodaConstants.FILE_ISDIRECTORY));

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  Label riskIncidenceRegisterTitle;

  @UiField
  FlowPanel riskIncidenceRegisterDescription;

  @UiField(provided = true)
  SearchWrapper searchWrapper;

  public RiskIncidenceRegister(String aipId, String representationId, List<String> filePath, String fileId,
    Filter filter) {

    ListBuilder<RiskIncidence> riskIncidenceListBuilder = new ListBuilder<>(RiskIncidenceList::new,
      new AsyncTableCellOptions<>(RiskIncidence.class, "RiskIncidenceRegister_risks").withFilter(filter)
        .withSummary(messages.riskIncidencesTitle()).bindOpener());

    searchWrapper = new SearchWrapper(false).createListAndSearchPanel(riskIncidenceListBuilder,
      RiskIncidenceActions.getForMultipleEdit(), messages.riskIncidenceRegisterSearchPlaceHolder());

    initWidget(uiBinder.createAndBindUi(this));
    riskIncidenceRegisterDescription.add(new HTMLWidgetWrapper("RiskIncidenceRegisterDescription.html"));

    // create breadcrumbs
    breadcrumb.setVisible(true);
    if (fileId != null) {
      getFileBreadCrumbs(aipId, representationId, filePath, fileId);
    } else if (representationId != null) {
      getRepresentationBreadCrumbs(aipId, representationId);
    } else if (aipId != null) {
      getAIPBreadCrumbs(aipId);
    } else {
      breadcrumb.setVisible(false);
    }
  }

  private void getAIPBreadCrumbs(String aipId) {
    BrowserService.Util.getInstance().retrieveBrowseAIPBundle(aipId, LocaleInfo.getCurrentLocale().getLocaleName(),
      aipFieldsToReturn, new AsyncCallback<BrowseAIPBundle>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
          HistoryUtils.newHistory(BrowseTop.RESOLVER);
        }

        @Override
        public void onSuccess(BrowseAIPBundle itemBundle) {
          breadcrumb
            .updatePath(BreadcrumbUtils.getAipBreadcrumbs(itemBundle.getAIPAncestors(), itemBundle.getAip(), true));
          breadcrumb.setVisible(true);
        }
      });
  }

  private void getRepresentationBreadCrumbs(String aipId, String representationId) {
    BrowserService.Util.getInstance().retrieveBrowseRepresentationBundle(aipId, representationId,
      LocaleInfo.getCurrentLocale().getLocaleName(), representationFieldsToReturn,
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

  private void getFileBreadCrumbs(String aipId, String representationId, List<String> filePath, String fileId) {
    BrowserService.Util.getInstance().retrieveBrowseFileBundle(aipId, representationId, filePath, fileId,
      fileFieldsToReturn, new AsyncCallback<BrowseFileBundle>() {

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

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }
}
