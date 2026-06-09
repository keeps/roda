/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IndexedRepresentationRequest;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataVersions;
import org.roda.core.data.v2.ip.metadata.ResourceVersion;
import org.roda.wui.client.browse.tabs.DescriptiveMetadataTabs;
import org.roda.wui.client.common.ActionsToolbar;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestErrorOverlayType;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class DescriptiveMetadataHistory extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 2 || historyTokens.size() == 3) {
        final String aipId = historyTokens.get(0);
        final String representationId = historyTokens.size() == 3 ? historyTokens.get(1) : null;
        final String descriptiveMetadataId = new HTML(historyTokens.get(historyTokens.size() - 1)).getText();

        Services service = new Services("History resolver", "get");

        service.aipResource(s -> s.requestAIPLock(aipId)).whenComplete((locked, lockError) -> {
          if (lockError != null) {
            callback.onFailure(lockError);
          } else if (!Boolean.TRUE.equals(locked)) {
            GWT.log("DescriptiveMetadataHistory lock result: " + locked);
            HistoryUtils.newHistory(BrowseTop.RESOLVER, aipId);
            Toast.showInfo(messages.editDescMetadataLockedTitle(), messages.editDescMetadataLockedText());
            callback.onSuccess(null);
          } else {
            service.rodaEntityRestService(s -> s.findByUuid(aipId, LocaleInfo.getCurrentLocale().getLocaleName()),
              IndexedAIP.class).whenComplete((aip, aipError) -> {
                if (aipError != null) {
                  callback.onFailure(aipError);
                } else if (representationId == null) {
                  service.aipResource(s -> s.retrieveAIPDescriptiveMetadataVersions(aip.getId(), descriptiveMetadataId,
                    LocaleInfo.getCurrentLocale().getLocaleName())).whenComplete((versions, versionsError) -> {
                      if (versionsError != null) {
                        callback.onFailure(versionsError);
                      } else {
                        callback.onSuccess(new DescriptiveMetadataHistory(aip, null, descriptiveMetadataId, versions));
                      }
                    });
                } else {
                  service
                    .representationResource(s -> s.retrieveIndexedRepresentationViaRequest(
                      new IndexedRepresentationRequest(aipId, representationId)))
                    .whenComplete((representation, representationError) -> {
                      if (representationError != null) {
                        callback.onFailure(representationError);
                      } else {
                        service.aipResource(s -> s.retrieveRepresentationDescriptiveMetadataVersions(aip.getId(),
                          representation.getId(), descriptiveMetadataId, LocaleInfo.getCurrentLocale().getLocaleName()))
                          .whenComplete((versions, versionsError) -> {
                            if (versionsError != null) {
                              callback.onFailure(versionsError);
                            } else {
                              callback.onSuccess(
                                new DescriptiveMetadataHistory(aip, representation, descriptiveMetadataId, versions));
                            }
                          });
                      }
                    });
                }
              });
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
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private final String aipId;
  private final String representationId;
  private final String descriptiveMetadataId;

  @UiField
  ActionsToolbar actionsToolbar;
  @UiField
  NavigationToolbar<IndexedAIP> navigationToolbar;
  @UiField
  FlowPanel radioContainer;
  @UiField
  TitlePanel title;
  @UiField
  SimplePanel tabsContainer;
  @UiField
  FocusPanel keyboardFocus;
  @UiField
  FlowPanel versionActionsPanel;
  private DescriptiveMetadataVersions descriptiveMetadataVersions;
  private String selectedVersion = null;
  private boolean aipLocked;

  /**
   * Create a new panel to select descriptive metadata history
   *
   * @param aip
   *          the AIP.
   * @param representation
   *          the representation.
   * @param descriptiveMetadataId
   *          the descriptive metadata identifier.
   * @param versions
   *          the descriptive metadata versions
   *          bundle @{DescriptiveMetadataVersionsBundle}
   *
   */
  public DescriptiveMetadataHistory(final IndexedAIP aip, final IndexedRepresentation representation,
    final String descriptiveMetadataId, final DescriptiveMetadataVersions versions) {
    this.aipId = aip.getId();
    this.representationId = representation != null ? representation.getId() : null;
    this.descriptiveMetadataId = descriptiveMetadataId;
    this.descriptiveMetadataVersions = versions;
    aipLocked = true;

    initWidget(uiBinder.createAndBindUi(this));
    CreateDescriptiveMetadata.initTitle(aip, title, false);
    title.addStyleName("mb-16");

    navigationToolbar.withoutButtons().build();
    if (representation == null) {
      navigationToolbar
        .updateBreadcrumbPath(BreadcrumbUtils.getDescriptiveMetadataHistoryBreadcrumbs(aip, descriptiveMetadataId));
    } else {
      navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getRepresentationDescriptiveMetadataHistoryBreadcrumbs(aip,
        representation, descriptiveMetadataId));
    }

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");

    init();

    actionsToolbar.setLabel(messages.historyDescriptiveMetadataTitle());
    actionsToolbar.setTagsVisible(false);

    if (PermissionClientUtils.hasPermissions(descriptiveMetadataVersions.getPermissions(),
      RodaConstants.PERMISSION_METHOD_REVERT_DESCRIPTIVE_METADATA_VERSION)) {

      Button revertBtn = new Button(messages.revertButton());
      revertBtn.addStyleName("btn btn-play mr-10");
      revertBtn.addStyleName("btn-separator-right");
      revertBtn.addClickHandler(event -> revertSelectedVersion());

      versionActionsPanel.add(revertBtn);
    }

    if (PermissionClientUtils.hasPermissions(descriptiveMetadataVersions.getPermissions(),
      RodaConstants.PERMISSION_METHOD_DELETE_DESCRIPTIVE_METADATA_VERSION)) {

      Button removeBtn = new Button(messages.removeButton());
      removeBtn.addStyleName("btn btn-ban btn-danger");
      removeBtn.addStyleName("btn-separator-right");
      removeBtn.addClickHandler(event -> removeSelectedVersion());

      versionActionsPanel.add(removeBtn);
    }

    Button cancelBtn = new Button(messages.cancelButton());
    cancelBtn.addStyleName("btn");
    cancelBtn.addStyleName("btn-link");

    cancelBtn.addClickHandler(event -> cancel());
    versionActionsPanel.add(cancelBtn);
  }

  private void init() {
    radioContainer.clear();

    if (descriptiveMetadataVersions.getVersions() == null) {
      selectedVersion = null;
      tabsContainer.clear();
      return;
    }

    List<ResourceVersion> versionList = new ArrayList<>(descriptiveMetadataVersions.getVersions());
    versionList.sort((v1, v2) -> (int) (v2.getCreatedDate().getTime() - v1.getCreatedDate().getTime()));

    String radioGroupName = "versionsGroup_" + descriptiveMetadataId;
    boolean isFirst = true;

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
      String labelText = messages.descriptiveMetadataHistoryLabel(message, createdDate);
      RadioButton rb = new RadioButton(radioGroupName, labelText);

      rb.addStyleName("mb-5 display-block my-custom-radio");
      rb.addValueChangeHandler(event -> {
        if (event.getValue()) {
          selectedVersion = versionKey;
          updateTabs();
        }
      });

      if (isFirst) {
        rb.setValue(true);
        selectedVersion = versionKey;
        isFirst = false;
      }

      radioContainer.add(rb);
    }

    if (!versionList.isEmpty()) {
      updateTabs();
    }

  }

  private void cancel() {
    if (representationId == null) {
      HistoryUtils.newHistory(BrowseTop.RESOLVER, aipId);
    } else {
      HistoryUtils.newHistory(BrowseRepresentation.RESOLVER, aipId, representationId);
    }
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

  void revertSelectedVersion() {
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

  void removeSelectedVersion() {

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

  protected void clean() {
    radioContainer.clear();
    selectedVersion = null;
    tabsContainer.clear();
  }

  @Override
  protected void onDetach() {
    if (aipLocked) {
      Services services = new Services("Release AIP lock", "lock");
      services.aipResource(s -> s.releaseAIPLock(this.aipId)).whenComplete((s, throwable) -> aipLocked = false);
    }
    super.onDetach();
  }

  protected void updateTabs() {
    tabsContainer.clear();

    if (selectedVersion == null) {
      return;
    }

    DescriptiveMetadataTabs versionTabs = new DescriptiveMetadataTabs();
    versionTabs.init(() -> new DescriptiveMetadataViewPanel(selectedVersion, true),
      () -> new DescriptiveMetadataViewPanel(selectedVersion, false));

    tabsContainer.setWidget(versionTabs);
  }

  interface MyUiBinder extends UiBinder<Widget, DescriptiveMetadataHistory> {
  }

  private class DescriptiveMetadataViewPanel extends SimplePanel {
    public DescriptiveMetadataViewPanel(String versionKey, boolean isHtml) {
      final HTML content = new HTML();
      setWidget(content);

      getDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, versionKey, isHtml,
        new AsyncCallback<SafeHtml>() {
          @Override
          public void onFailure(Throwable caught) {
            AsyncCallbackUtils.defaultFailureTreatment(caught);
          }

          @Override
          public void onSuccess(SafeHtml html) {
            content.setHTML(html);
            if (!isHtml) {
              content.addStyleName("code-pre");
              JavascriptUtils.runHighlighterOn(content.getElement());
            }
          }
        });
    }
  }
}
