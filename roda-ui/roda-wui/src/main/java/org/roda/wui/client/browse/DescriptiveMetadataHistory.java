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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.roda.wui.client.common.UserLogin;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria
 * 
 */
public class DescriptiveMetadataHistory extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 2) {
        final String aipId = historyTokens.get(0);
        final String descriptiveMetadataId = historyTokens.get(1);

        BrowserService.Util.getInstance().listDescriptiveMetadataVersions(aipId, descriptiveMetadataId,
          new AsyncCallback<Map<String, Date>>() {

          @Override
          public void onFailure(Throwable caught) {
            Toast.showError(caught);
          }

          @Override
          public void onSuccess(Map<String, Date> versions) {
            DescriptiveMetadataHistory widget = new DescriptiveMetadataHistory(aipId, descriptiveMetadataId, versions);
            callback.onSuccess(widget);
          }
        });

      } else {
        Tools.newHistory(Browse.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      // TODO check for browse metadata history permission
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {Browse.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(Browse.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "history";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, DescriptiveMetadataHistory> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  // private ClientLogger logger = new ClientLogger(getClass().getName());
  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  private final String aipId;
  private final String descriptiveMetadataId;
  private final Map<String, Date> versions;

  @UiField
  FlowPanel layout;

  @UiField
  Button buttonRevert;

  @UiField
  Button buttonRemove;

  @UiField
  Button buttonCancel;

  /**
   * Create a new panel to edit a user
   * 
   * @param user
   *          the user to edit
   */
  public DescriptiveMetadataHistory(final String aipId, final String descriptiveMetadataId,
    Map<String, Date> versions) {
    this.aipId = aipId;
    this.descriptiveMetadataId = descriptiveMetadataId;
    this.versions = versions;

    initWidget(uiBinder.createAndBindUi(this));

    // sort
    List<Entry<String, Date>> versionList = new ArrayList<>(versions.entrySet());
    Collections.sort(versionList, new Comparator<Entry<String, Date>>() {

      @Override
      public int compare(Entry<String, Date> d1, Entry<String, Date> d2) {
        return (int) (d2.getValue().getTime() - d1.getValue().getTime());
      }
    });

    // create list layout
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    b.append(SafeHtmlUtils.fromSafeConstant("<ol>"));
    for (Entry<String, Date> version : versionList) {
      String versionKey = version.getKey();
      Date createdDate = version.getValue();
      b.append(SafeHtmlUtils.fromSafeConstant("<li>"));
      b.append(SafeHtmlUtils.fromSafeConstant("<a href='"));
      b.append(SafeHtmlUtils
        .fromSafeConstant(Tools.createHistoryHashLink(RESOLVER, aipId, descriptiveMetadataId, versionKey)));
      b.append(SafeHtmlUtils.fromSafeConstant("'>"));
      b.append(SafeHtmlUtils.fromString(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL).format(createdDate)));
      b.append(SafeHtmlUtils.fromSafeConstant("</a>"));
      b.append(SafeHtmlUtils.fromSafeConstant("</li>"));
    }
    b.append(SafeHtmlUtils.fromSafeConstant("</ol>"));

    layout.add(new HTML(b.toSafeHtml()));
  }

  @UiHandler("buttonRevert")
  void buttonRevertHandler(ClickEvent e) {
    // TODO

  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    // TODO
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    Tools.newHistory(Browse.RESOLVER, aipId);
  }

}
