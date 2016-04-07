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

import org.roda.core.data.v2.formats.Format;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.management.UserManagementService;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;

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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 *
 */
public class ShowFormat extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(FormatRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "format";
    }
  };

  private static ShowFormat instance = null;

  public static ShowFormat getInstance() {
    if (instance == null) {
      instance = new ShowFormat();
    }
    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, ShowFormat> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  // private static CommonMessages messages = GWT.create(CommonMessages.class);

  @UiField
  Label formatId;

  @UiField
  Label formatName;

  @UiField
  Label formatDefinition;

  @UiField
  Label formatCategory;

  @UiField
  Label formatLatestVersion;

  @UiField
  Label formatPopularity;

  @UiField
  Label formatDeveloper;

  @UiField
  Label formatInitialRelease;

  @UiField
  Label formatStandard;

  @UiField
  Label formatIsOpenFormat;

  @UiField
  Label formatWebsite;

  @UiField
  Label formatProvenanceInformation;

  @UiField
  Button buttonEdit;

  @UiField
  Button buttonCancel;

  private Format format;

  /**
   * Create a new panel to view a format
   *
   *
   */

  public ShowFormat() {
    this.format = new Format();
    initWidget(uiBinder.createAndBindUi(this));
  }

  public ShowFormat(Format format) {
    initWidget(uiBinder.createAndBindUi(this));
    this.format = format;

    formatId.setText(format.getId());
    formatName.setText(format.getName());
    formatDefinition.setText(format.getDefinition());
    formatCategory.setText(format.getCategory());
    formatLatestVersion.setText(format.getLatestVersion());
    formatPopularity.setText(Integer.toString(format.getPopularity()));
    formatDeveloper.setText(format.getDeveloper());
    formatInitialRelease.setText(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL).format(
      format.getInitialRelease()));
    formatStandard.setText(format.getStandard());
    formatIsOpenFormat.setText(Boolean.toString(format.isOpenFormat()));
    formatWebsite.setText(format.getWebsite());
    formatProvenanceInformation.setText(format.getProvenanceInformation());
  }

  void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {

    if (historyTokens.size() == 1) {
      String formatId = historyTokens.get(0);
      UserManagementService.Util.getInstance().retrieveFormat(formatId, new AsyncCallback<Format>() {

        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(Format result) {
          ShowFormat formatPanel = new ShowFormat(result);
          callback.onSuccess(formatPanel);
        }
      });
    } else {
      Tools.newHistory(FormatRegister.RESOLVER);
      callback.onSuccess(null);
    }
  }

  @UiHandler("buttonEdit")
  void handleButtonEdit(ClickEvent e) {
    Tools.newHistory(FormatRegister.RESOLVER, EditFormat.RESOLVER.getHistoryToken(), format.getId());
  }

  @UiHandler("buttonCancel")
  void handleButtonCancel(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    Tools.newHistory(FormatRegister.RESOLVER);
  }

}
