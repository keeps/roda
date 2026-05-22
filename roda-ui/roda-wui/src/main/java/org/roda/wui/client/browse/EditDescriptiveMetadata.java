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

import java.util.List;

import org.roda.core.data.v2.index.IndexedRepresentationRequest;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.CreateDescriptiveMetadataRequest;
import org.roda.core.data.v2.ip.metadata.SupportedMetadataValue;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoActionsToolbar;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class EditDescriptiveMetadata extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  public static final HistoryResolver RESOLVER = new HistoryResolver() {
    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 2 || historyTokens.size() == 3) {
        final String aipId = historyTokens.get(0);
        final String representationId = historyTokens.size() == 3 ? historyTokens.get(1) : null;
        final String filename = new HTML(historyTokens.get(historyTokens.size() - 1)).getText();

        Services service = new Services("Get aip lock", "get");
        service.aipResource(s -> s.requestAIPLock(aipId)).whenComplete((value, error) -> {
          if (error == null) {
            if (value) {
              if (representationId == null) {
                service
                  .rodaEntityRestService(s -> s.findByUuid(aipId, LocaleInfo.getCurrentLocale().getLocaleName()),
                    IndexedAIP.class)
                  .thenCompose(aip -> service.aipResource(s -> s.retrieveAIPSupportedMetadata(aip.getId(),
                    filename.replace(".xml", ""), LocaleInfo.getCurrentLocale().getLocaleName()))
                    .whenComplete((result, throwable) -> {
                      if (throwable != null) {
                        callback.onFailure(throwable);
                      } else {
                        callback.onSuccess(new EditDescriptiveMetadata(aip, null, filename, result));
                      }
                    }));
              } else {
                service
                  .rodaEntityRestService(s -> s.findByUuid(aipId, LocaleInfo.getCurrentLocale().getLocaleName()),
                    IndexedAIP.class)
                  .thenCompose(aip -> service
                    .representationResource(s -> s.retrieveIndexedRepresentationViaRequest(
                      new IndexedRepresentationRequest(aipId, representationId)))
                    .thenCompose(representation -> service
                      .aipResource(s -> s.retrieveRepresentationSupportedMetadata(aip.getId(), representation.getId(),
                        filename.replace(".xml", ""), LocaleInfo.getCurrentLocale().getLocaleName()))
                      .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                          callback.onFailure(throwable);
                        } else {
                          callback.onSuccess(new EditDescriptiveMetadata(aip, representation, filename, result));
                        }
                      })));
              }
            } else {
              HistoryUtils.newHistory(BrowseTop.RESOLVER, aipId);
              Toast.showInfo(messages.editDescMetadataLockedTitle(), messages.editDescMetadataLockedText());
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
      // TODO check for edit metadata permission
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {BrowseTop.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(BrowseTop.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "edit_metadata";
    }
  };
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private final IndexedAIP aip;
  private final IndexedRepresentation representation;
  @UiField
  FocusPanel keyboardFocus;
  @UiField
  NavigationToolbar<IndexedAIP> navigationToolbar;
  @UiField
  NoActionsToolbar actionsToolbar;
  @UiField
  FlowPanel descriptiveMetadataPanel;
  @UiField
  TitlePanel title;
  private boolean aipLocked;

  /**
   * Create a new panel to edit a descriptive metadata
   *
   * @param aip
   * @param representation
   *          the user to edit
   * @param filename
   */
  public EditDescriptiveMetadata(IndexedAIP aip, IndexedRepresentation representation, String filename,
    final SupportedMetadataValue responseParams) {
    this.aip = aip;
    this.representation = representation;
    aipLocked = true;
    initWidget(uiBinder.createAndBindUi(this));

    navigationToolbar.withoutButtons().build();

    initTitle(aip, title);
    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");

    if (representation == null) {
      navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getEditDescriptiveMetadataBreadcrumbs(aip, filename));
    } else {
      navigationToolbar.updateBreadcrumbPath(
        BreadcrumbUtils.getEditRepresentationDescriptiveMetadataBreadcrumbs(aip, representation, filename));
    }

    actionsToolbar.setLabel(messages.editDescriptiveMetadataTitle());
    actionsToolbar.build();

    DescriptiveMetadataPanel dataPanel = new DescriptiveMetadataPanel(aip.getId(),
      representation != null ? representation.getId() : null, filename, responseParams, aip.getPermissions(), true);
    descriptiveMetadataPanel.add(dataPanel);

    dataPanel.setSaveHandler(() -> dataPanel.getValue(new AsyncCallback<CreateDescriptiveMetadataRequest>() {
      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
        dataPanel.setSaveEnabled(true);
      }

      @Override
      public void onSuccess(CreateDescriptiveMetadataRequest request) {
        updateMetadataOnServer(dataPanel, request);
      }
    }));

    dataPanel.setCancelHandler(this::back);
  }

  protected static void initTitle(IndexedAIP aip, TitlePanel title) {
    if (aip.getLevel() != null) {
      title.setIcon(DescriptionLevelUtils.getElementLevelIconSafeHtml(aip.getLevel(), false));
    } else {
      title.setIcon(DescriptionLevelUtils.getTopIconSafeHtml());
    }

    if (aip.getTitle() != null) {
      title.setText(aip.getTitle());
    } else {
      title.setText(aip.getId());
    }
    title.addStyleName("mb-16");
  }

  @Override
  protected void onDetach() {
    if (aipLocked) {
      Services services = new Services("Release AIP lock", "lock");
      services.aipResource(s -> s.releaseAIPLock(this.aip.getId())).whenComplete((s, throwable) -> aipLocked = false);
    }
    super.onDetach();
  }

  private void updateMetadataOnServer(DescriptiveMetadataPanel dataPanel, CreateDescriptiveMetadataRequest request) {
    Dialogs.showConfirmDialog(messages.updateMetadataFileTitle(), messages.updateMetadataFileLabel(),
      messages.cancelButton(), messages.confirmButton(), new NoAsyncCallback<Boolean>() {

        @Override
        public void onSuccess(Boolean confirm) {
          if (!confirm) {
            dataPanel.setSaveEnabled(true);
            return;
          }

          Services service = new Services("Update descriptive metadata", "update");

          if (representation == null) {
            service.aipResource(s -> s.updateAIPDescriptiveMetadataFile(aip.getId(), request))
              .whenComplete((value, error) -> handleUpdateResult(dataPanel, error));
          } else {
            service
              .aipResource(
                s -> s.updateRepresentationDescriptiveMetadataFile(aip.getId(), representation.getId(), request))
              .whenComplete((value, error) -> handleUpdateResult(dataPanel, error));
          }
        }
      });
  }

  private void handleUpdateResult(DescriptiveMetadataPanel dataPanel, Throwable error) {
    if (error != null) {
      if (error instanceof ValidationException) {
        dataPanel.setErrors((ValidationException) error);
      } else {
        AsyncCallbackUtils.defaultFailureTreatment(error);
      }
      dataPanel.setSaveEnabled(true);
    } else {
      dataPanel.clearErrors();
      Toast.showInfo(messages.dialogSuccess(), messages.metadataFileSaved());
      back();
    }
  }

  private void back() {
    if (representation == null) {
      HistoryUtils.openBrowse(aip.getId());
    } else {
      HistoryUtils.openBrowse(aip.getId(), representation.getId());
    }
  }

  interface MyUiBinder extends UiBinder<Widget, EditDescriptiveMetadata> {
  }
}
