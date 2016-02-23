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
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
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

        BrowserService.Util.getInstance().getDescriptiveMetadataVersionsBundle(aipId, descriptiveMetadataId,
          LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<DescriptiveMetadataVersionsBundle>() {

          @Override
          public void onFailure(Throwable caught) {
            Toast.showError(caught);
          }

          @Override
          public void onSuccess(DescriptiveMetadataVersionsBundle bundle) {
            DescriptiveMetadataHistory widget = new DescriptiveMetadataHistory(aipId, descriptiveMetadataId, bundle);
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
  private final DescriptiveMetadataVersionsBundle bundle;

  private boolean inHTML = true;
  private String selectedVersion = null;

  @UiField
  ListBox list;

  @UiField
  Label descriptiveMetadataType;

  @UiField
  HTML preview;

  @UiField
  FocusPanel showXml;

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
    DescriptiveMetadataVersionsBundle bundle) {
    this.aipId = aipId;
    this.descriptiveMetadataId = descriptiveMetadataId;
    this.bundle = bundle;

    initWidget(uiBinder.createAndBindUi(this));

    // sort
    List<Entry<String, Date>> versionList = new ArrayList<>(bundle.getVersions().entrySet());
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

      list.addItem(messages.descriptiveMetadataHistoryLabel(versionKey, createdDate), versionKey);

    }

    list.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        String versionKey = list.getSelectedValue();
        selectedVersion = versionKey;
        updatePreview();
      }
    });

    descriptiveMetadataType.setText(bundle.getDescriptiveMetadata().getLabel());

    if (versionList.size() > 0) {
      list.setSelectedIndex(0);
      selectedVersion = versionList.get(0).getKey();
      updatePreview();
    }

  }

  protected void updatePreview() {
    getDescriptiveMetadata(aipId, descriptiveMetadataId, selectedVersion, inHTML, new AsyncCallback<SafeHtml>() {

      @Override
      public void onFailure(Throwable caught) {
        Toast.showError(caught);
      }

      @Override
      public void onSuccess(SafeHtml html) {
        preview.setHTML(html);
        if (inHTML) {
          preview.removeStyleName("code-pre");
        } else {
          preview.addStyleName("code-pre");
        }
      }
    });
  }

  private void getDescriptiveMetadata(final String aipId, final String descId, final String versionKey,
    final boolean inHTML, final AsyncCallback<SafeHtml> callback) {

    SafeUri uri;
    if (inHTML) {
      uri = RestUtils.createDescriptiveMetadataHTMLUri(aipId, descId, versionKey);
    } else {
      uri = RestUtils.createDescriptiveMetadataDownloadUri(aipId, descId, versionKey);
    }
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uri.asString());
    requestBuilder.setHeader("Authorization", "Custom");
    try {
      requestBuilder.sendRequest(null, new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (200 == response.getStatusCode()) {
            String text = response.getText();

            SafeHtmlBuilder b = new SafeHtmlBuilder();
            if (inHTML) {
              b.append(SafeHtmlUtils.fromTrustedString(text));
            } else {
              b.append(SafeHtmlUtils.fromString(text));
            }
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
    } catch (

    RequestException e)

    {
      callback.onFailure(e);
    }

  }

  public boolean isInHTML() {
    return inHTML;
  }

  public void setInHTML(boolean inHTML) {
    this.inHTML = inHTML;
    if (inHTML) {
      showXml.removeStyleName("descriptiveMetadataLink-selected");
    } else {
      showXml.addStyleName("descriptiveMetadataLink-selected");
    }
  }

  @UiHandler("showXml")
  void buttonShowXmlHandler(ClickEvent e) {
    setInHTML(!isInHTML());
    updatePreview();
  }

  @UiHandler("buttonRevert")
  void buttonRevertHandler(ClickEvent e) {
    // TODO
    Toast.showInfo("Sorry", "Feature not yet implemented");
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    // TODO
    Toast.showInfo("Sorry", "Feature not yet implemented");
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    Tools.newHistory(Browse.RESOLVER, aipId);
  }

}
