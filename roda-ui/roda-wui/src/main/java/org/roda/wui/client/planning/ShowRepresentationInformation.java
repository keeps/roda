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

import java.util.List;

import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.wui.client.browse.tabs.BrowseRepresentationInformationTabs;
import org.roda.wui.client.common.BrowseRepresentationInformationActionsToolbar;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
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
public class ShowRepresentationInformation extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static ShowRepresentationInformation instance = null;
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(RepresentationInformationNetwork.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "representation_information";
    }
  };

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  NavigationToolbar<RepresentationInformation> navigationToolbar;

  @UiField
  BrowseRepresentationInformationActionsToolbar actionsToolbar;

  @UiField
  FocusPanel keyboardFocus;

  @UiField
  TitlePanel title;

  @UiField
  BrowseRepresentationInformationTabs browseRepresentationInformationTabs;

  public ShowRepresentationInformation() {
  }

  public ShowRepresentationInformation(final RepresentationInformation ri) {
    instance = this;

    initWidget(uiBinder.createAndBindUi(this));

    navigationToolbar.withObject(ri).build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getRepresentationInformationBreadCrumbs(ri));

    title.setText(ri.getName());

    actionsToolbar.setObjectAndBuild(ri, null, null);
    browseRepresentationInformationTabs.init(ri);

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse browse-file browse_main_panel");
  }

  public static ShowRepresentationInformation getInstance() {
    if (instance == null) {
      instance = new ShowRepresentationInformation();
    }
    return instance;
  }

  void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {
      Services services = new Services("Retrieve representation information", "get");
      services
        .rodaEntityRestService(s -> s.findByUuid(historyTokens.get(0), LocaleInfo.getCurrentLocale().getLocaleName()),
          RepresentationInformation.class)
        .whenComplete((representationInformation, throwable) -> {
          if (throwable != null) {
            callback.onFailure(throwable);
          } else {

            ShowRepresentationInformation panel = new ShowRepresentationInformation(representationInformation);
            callback.onSuccess(panel);
          }
        });
    } else {
      HistoryUtils.newHistory(RepresentationInformationNetwork.RESOLVER);
      callback.onSuccess(null);
    }
  }

  interface MyUiBinder extends UiBinder<Widget, ShowRepresentationInformation> {
  }
}
