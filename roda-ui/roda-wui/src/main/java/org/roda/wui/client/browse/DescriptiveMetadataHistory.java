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
import org.roda.wui.common.client.tools.RestErrorOverlayType;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
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
  ListBox list;

  @UiField
  HTML preview;

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
    for (Entry<String, Date> version : versionList) {
      String versionKey = version.getKey();
      Date createdDate = version.getValue();
      list.addItem(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(createdDate), versionKey);

      // Tools.createHistoryHashLink(RESOLVER, aipId, descriptiveMetadataId,
      // versionKey))
    }

    list.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        String versionKey = list.getSelectedValue();
        updatePreview(versionKey);
      }
    });

    if (versionList.size() > 0) {
      list.setSelectedIndex(0);
      updatePreview(versionList.get(0).getKey());
    }

  }

  protected void updatePreview(String versionKey) {
    getDescriptiveMetadataHTML(aipId, descriptiveMetadataId, versionKey, new AsyncCallback<SafeHtml>() {

      @Override
      public void onFailure(Throwable caught) {
        Toast.showError(caught);
      }

      @Override
      public void onSuccess(SafeHtml html) {
        preview.setHTML(html);
      }
    });
  }

  private void getDescriptiveMetadataHTML(final String aipId, final String descId, final String versionKey,
    final AsyncCallback<SafeHtml> callback) {
    // TODO retrieve requested version
    String uri = RestUtils.createDescriptiveMetadataHTMLUri(aipId, descId, versionKey);
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uri);
    requestBuilder.setHeader("Authorization", "Custom");
    try {
      requestBuilder.sendRequest(null, new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (200 == response.getStatusCode()) {
            String html = response.getText();

            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.append(SafeHtmlUtils.fromTrustedString(html));
            SafeHtml safeHtml = b.toSafeHtml();

            callback.onSuccess(safeHtml);
          } else {
            String text = response.getText();
            String message;
            try {
              RestErrorOverlayType error = (RestErrorOverlayType) JsonUtils.safeEval(text);
              message = error.getMessage();
            } catch (IllegalArgumentException e) {
              message = text;
            }

            SafeHtmlBuilder b = new SafeHtmlBuilder();

            // error message
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='error'>"));
            b.append(messages.descriptiveMetadataTranformToHTMLError());
            b.append(SafeHtmlUtils.fromSafeConstant("<pre><code>"));
            b.append(SafeHtmlUtils.fromString(message));
            b.append(SafeHtmlUtils.fromSafeConstant("</core></pre>"));
            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));

            callback.onSuccess(b.toSafeHtml());
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
