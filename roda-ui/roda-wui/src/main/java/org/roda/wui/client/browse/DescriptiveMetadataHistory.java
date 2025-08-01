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
import java.util.Date;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataVersions;
import org.roda.core.data.v2.ip.metadata.ResourceVersion;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestErrorOverlayType;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DomEvent;
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

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class DescriptiveMetadataHistory extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 2 || historyTokens.size() == 3) {
        final String aipId = historyTokens.get(0);
        final String representationId = historyTokens.size() == 3 ? historyTokens.get(1) : null;
        final String descriptiveMetadataId = new HTML(historyTokens.get(historyTokens.size() - 1)).getText();

        Services service = new Services("History resolver", "get");

        service.aipResource(s -> s.requestAIPLock(aipId)).whenComplete((value, error) -> {
          if (value) {
            if (representationId == null) {
              service.aipResource(s -> s.retrieveAIPDescriptiveMetadataVersions(aipId, descriptiveMetadataId,
                LocaleInfo.getCurrentLocale().getLocaleName())).whenComplete((result, throwable) -> {
                  if (throwable != null) {
                    AsyncCallbackUtils.defaultFailureTreatment(throwable);
                  } else {
                    DescriptiveMetadataHistory widget = new DescriptiveMetadataHistory(aipId, null,
                      descriptiveMetadataId, result);
                    callback.onSuccess(widget);
                  }
                });
            } else {
              service
                .aipResource(s -> s.retrieveRepresentationDescriptiveMetadataVersions(aipId, representationId,
                  descriptiveMetadataId, LocaleInfo.getCurrentLocale().getLocaleName()))
                .whenComplete((result, throwable) -> {
                  if (throwable != null) {
                    AsyncCallbackUtils.defaultFailureTreatment(throwable);
                  } else {
                    DescriptiveMetadataHistory widget = new DescriptiveMetadataHistory(aipId, representationId,
                      descriptiveMetadataId, result);
                    callback.onSuccess(widget);
                  }
                });
            }
          }
        });
      } else {
        HistoryUtils.newHistory(BrowseTop.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      // TODO check for browse metadata history permission
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {BrowseTop.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(BrowseTop.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "history";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, DescriptiveMetadataHistory> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final String aipId;
  private final String representationId;
  private final String descriptiveMetadataId;
  private DescriptiveMetadataVersions descriptiveMetadataVersions;

  private boolean inHTML = true;
  private String selectedVersion = null;
  private boolean aipLocked;

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

  @UiField
  TitlePanel title;

  /**
   * Create a new panel to select descriptive metadata history
   *
   * @param aipId
   *          the AIP identifier.
   * @param representationId
   *          the representation identifier.
   * @param descriptiveMetadataId
   *          the descriptive metadata identifier.
   * @param versions
   *          the descriptive metadata versions
   *          bundle @{DescriptiveMetadataVersionsBundle}
   *
   */
  public DescriptiveMetadataHistory(final String aipId, final String representationId,
    final String descriptiveMetadataId, final DescriptiveMetadataVersions versions) {
    this.aipId = aipId;
    this.representationId = representationId;
    this.descriptiveMetadataId = descriptiveMetadataId;
    this.descriptiveMetadataVersions = versions;
    aipLocked = true;

    initWidget(uiBinder.createAndBindUi(this));
    CreateDescriptiveMetadata.initTitle(aipId, title);
    init();

    list.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        String versionKey = list.getSelectedValue();
        selectedVersion = versionKey;
        updatePreview();
      }
    });

    PermissionClientUtils.bindPermission(buttonRevert, descriptiveMetadataVersions.getPermissions(),
      RodaConstants.PERMISSION_METHOD_REVERT_DESCRIPTIVE_METADATA_VERSION);
    PermissionClientUtils.bindPermission(buttonRemove, descriptiveMetadataVersions.getPermissions(),
      RodaConstants.PERMISSION_METHOD_DELETE_DESCRIPTIVE_METADATA_VERSION);

    Element firstElement = showXml.getElement().getFirstChildElement();
    if ("input".equalsIgnoreCase(firstElement.getTagName())) {
      firstElement.setAttribute("title", "browse input");
    }

    // Set the selected index
    list.setSelectedIndex(0);
    // Manually trigger a ValueChangeEvent
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), list);

  }

  private void init() {
    // sort
    List<ResourceVersion> versionList = new ArrayList<>(descriptiveMetadataVersions.getVersions());
    versionList.sort((v1, v2) -> (int) (v2.getCreatedDate().getTime() - v1.getCreatedDate().getTime()));

    // create list layout
    for (ResourceVersion version : versionList) {
      String versionKey = version.getId();
      String message = "";
      if (version.getProperties() != null) {
        message = messages.versionAction(version.getProperties().get(RodaConstants.VERSION_ACTION));

        if (version.getProperties().get(RodaConstants.VERSION_USER) != null) {
          message = messages.versionActionBy(message, version.getProperties().get(RodaConstants.VERSION_USER));
        }
      }
      Date createdDate = version.getCreatedDate();

      list.addItem(messages.descriptiveMetadataHistoryLabel(message, createdDate), versionKey);
    }

    if (!versionList.isEmpty()) {
      list.setSelectedIndex(0);
      selectedVersion = versionList.get(0).getId();
    }

    // Manually trigger a ValueChangeEvent
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), list);

  }

  protected void updatePreview() {
    getDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, selectedVersion, inHTML,
      new AsyncCallback<SafeHtml>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(SafeHtml html) {
          preview.setHTML(html);
          if (inHTML) {
            preview.removeStyleName("code-pre");
          } else {
            preview.addStyleName("code-pre");
            JavascriptUtils.runHighlighterOn(preview.getElement());
          }
        }
      });
  }

  private void getDescriptiveMetadata(final String aipId, final String representationId, final String descId,
    final String versionKey, final boolean inHTML, final AsyncCallback<SafeHtml> callback) {

    SafeUri uri;
    if (inHTML) {
      if (representationId != null) {
        uri = RestUtils.createRepresentationDescriptiveMetadataHTMLUri(aipId, representationId, descId, versionKey);
      } else {
        uri = RestUtils.createDescriptiveMetadataHTMLUri(aipId, descId, versionKey);
      }
    } else {
      if (representationId != null) {
        uri = RestUtils.createRepresentationDescriptiveMetadataDownloadUri(aipId, representationId, descId, versionKey);
      } else {
        uri = RestUtils.createDescriptiveMetadataDownloadUri(aipId, descId, versionKey);
      }
    }
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uri.asString());
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
            b.append(messages.descriptiveMetadataTransformToHTMLError());
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
      showXml.removeStyleName("toolbarLink-selected");
    } else {
      showXml.addStyleName("toolbarLink-selected");
    }
  }

  @UiHandler("showXml")
  void buttonShowXmlHandler(ClickEvent e) {
    setInHTML(!isInHTML());
    updatePreview();
  }

  @UiHandler("buttonRevert")
  void buttonRevertHandler(ClickEvent e) {
    Dialogs.showConfirmDialog(messages.descriptiveHistoryRevertConfirmDialogTitle(),
      messages.descriptiveHistoryRevertConfirmDialogMessage(), messages.dialogNo(), messages.dialogYes(),
      new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            Services service = new Services("Revert descriptive metadata version", "put");

            if (representationId == null) {

              service
                .aipResource(s -> s.revertAIPDescriptiveMetadataVersion(aipId, descriptiveMetadataId, selectedVersion))
                .whenComplete((value, error) -> {
                  if (error != null) {
                    AsyncCallbackUtils.defaultFailureTreatment(error);
                  } else {
                    Toast.showInfo(messages.dialogDone(), messages.versionReverted());
                    HistoryUtils.newHistory(BrowseTop.RESOLVER, aipId);
                  }
                });

            } else {
              service.aipResource(s -> s.revertRepresentationDescriptiveMetadataVersion(aipId, representationId,
                descriptiveMetadataId, selectedVersion)).whenComplete((value, error) -> {
                  if (error != null) {
                    AsyncCallbackUtils.defaultFailureTreatment(error);
                  } else {
                    Toast.showInfo(messages.dialogDone(), messages.versionReverted());
                    HistoryUtils.newHistory(BrowseTop.RESOLVER, RodaConstants.RODA_OBJECT_REPRESENTATION, aipId,
                      representationId);
                  }
                });
            }
          }
        }
      });
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {

    Dialogs.showConfirmDialog(messages.descriptiveHistoryRemoveConfirmDialogTitle(),
      messages.descriptiveHistoryRemoveConfirmDialogMessage(), messages.dialogNo(), messages.removeButton(),
      new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {

            Services service = new Services("Delete descriptive metadata version", "delete");

            if (representationId == null) {
              service
                .aipResource(s -> s.deleteDescriptiveMetadataVersion(aipId, descriptiveMetadataId, selectedVersion))
                .thenCompose(unused -> service.aipResource(s -> s.retrieveAIPDescriptiveMetadataVersions(aipId,
                  descriptiveMetadataId, LocaleInfo.getCurrentLocale().getLocaleName())))
                .whenComplete((value, error) -> {
                  if (error != null) {
                    AsyncCallbackUtils.defaultFailureTreatment(error);
                  } else {
                    if (value.getVersions().isEmpty()) {
                      HistoryUtils.newHistory(BrowseTop.RESOLVER, aipId);
                    } else {
                      descriptiveMetadataVersions = value;
                      clean();
                      init();
                    }
                  }
                });
            } else {
              service
                .aipResource(s -> s.deleteRepresentationDescriptiveMetadataVersion(aipId, representationId,
                  descriptiveMetadataId, selectedVersion))
                .thenCompose(unused -> service
                  .aipResource(s -> s.retrieveRepresentationDescriptiveMetadataVersions(aipId, representationId,
                    descriptiveMetadataId, LocaleInfo.getCurrentLocale().getLocaleName()))
                  .whenComplete((value, error) -> {
                    if (error != null) {
                      AsyncCallbackUtils.defaultFailureTreatment(error);
                    } else {
                      if (value.getVersions().isEmpty()) {
                        HistoryUtils.newHistory(BrowseRepresentation.RESOLVER, aipId, representationId);
                      } else {
                        descriptiveMetadataVersions = value;
                        clean();
                        init();
                      }
                    }
                  }));
            }
          }
        }
      });
  }

  protected void refresh() {

    Services service = new Services("Get descriptive metadata versions", "get");

    if (representationId == null) {
      service.aipResource(s -> s.retrieveAIPDescriptiveMetadataVersions(aipId, descriptiveMetadataId,
        LocaleInfo.getCurrentLocale().getLocaleName())).whenComplete((value, error) -> {
          if (error != null) {
            AsyncCallbackUtils.defaultFailureTreatment(error);
          } else {
            DescriptiveMetadataHistory.this.descriptiveMetadataVersions = value;
            clean();
            init();
          }
        });
    } else {
      service.aipResource(s -> s.retrieveRepresentationDescriptiveMetadataVersions(aipId, representationId,
        descriptiveMetadataId, LocaleInfo.getCurrentLocale().getLocaleName())).whenComplete((result, throwable) -> {
          if (throwable != null) {
            AsyncCallbackUtils.defaultFailureTreatment(throwable);
          } else {
            DescriptiveMetadataHistory.this.descriptiveMetadataVersions = result;
            clean();
            init();
          }
        });
    }
  }

  protected void clean() {
    list.clear();
    descriptiveMetadataType.setText("");
    selectedVersion = null;
    preview.setHTML("");
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(BrowseTop.RESOLVER, aipId);
  }

  @Override
  protected void onDetach() {
    if (aipLocked) {
      Services services = new Services("Release AIP lock", "lock");
      services.aipResource(s -> s.releaseAIPLock(this.aipId)).whenComplete((s, throwable) -> aipLocked = false);
    }
    super.onDetach();
  }
}
