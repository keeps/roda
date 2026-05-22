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

import java.util.Collections;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.v2.generics.DeleteRequest;
import org.roda.core.data.v2.generics.select.SelectedItemsListRequest;
import org.roda.core.data.v2.index.IndexedRepresentationRequest;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.CreateDescriptiveMetadataRequest;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoActionsToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.process.InternalProcess;
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
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class CreateDescriptiveMetadata extends Composite {
  public static final String NEW = "new";

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      boolean isAIP = historyTokens.get(0).equals(RodaConstants.RODA_OBJECT_AIP);
      boolean isRepresentation = historyTokens.get(0).equals(RodaConstants.RODA_OBJECT_REPRESENTATION);

      if ((isAIP && (historyTokens.size() == 2 || historyTokens.size() == 3))
        || (isRepresentation && (historyTokens.size() == 3 || historyTokens.size() == 4))) {

        final String aipId = historyTokens.get(1);
        final Services service = new Services("Retrieve create descriptive metadata context", "get");

        service.rodaEntityRestService(s -> s.findByUuid(aipId, LocaleInfo.getCurrentLocale().getLocaleName()),
          IndexedAIP.class).whenComplete((aip, throwable) -> {
            if (throwable != null) {
              callback.onFailure(throwable);
            } else if (isAIP) {
              boolean isNew = historyTokens.size() == 3 && historyTokens.get(2).equals(NEW);
              callback.onSuccess(new CreateDescriptiveMetadata(aip, isNew));
            } else {
              final String representationId = historyTokens.get(2);
              boolean isNew = historyTokens.size() == 4 && historyTokens.get(3).equals(NEW);

              service
                .representationResource(s -> s
                  .retrieveIndexedRepresentationViaRequest(new IndexedRepresentationRequest(aipId, representationId)))
                .whenComplete((representation, error) -> {
                  if (error != null) {
                    callback.onFailure(error);
                  } else {
                    callback.onSuccess(new CreateDescriptiveMetadata(aip, representation, isNew));
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
      // TODO check for edit metadata permission
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {BrowseTop.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(BrowseTop.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "create_metadata";
    }
  };
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private final IndexedAIP aip;
  private final IndexedRepresentation representation;
  private final boolean isNew;

  @UiField
  FocusPanel keyboardFocus;
  @UiField
  NavigationToolbar<IndexedAIP> navigationToolbar;
  @UiField
  NoActionsToolbar actionsToolbar;
  @UiField
  TitlePanel title;
  @UiField
  FlowPanel descriptiveMetadataPanel;

  public CreateDescriptiveMetadata(IndexedAIP aip, boolean isNew) {
    this(aip, null, isNew);
  }

  public CreateDescriptiveMetadata(IndexedAIP aip, IndexedRepresentation representation, boolean isNew) {
    this.aip = aip;
    this.representation = representation;
    this.isNew = isNew;

    initWidget(uiBinder.createAndBindUi(this));
    navigationToolbar.withoutButtons().build();

    if (representation == null) {
      navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getCreateDescriptiveMetadataBreadcrumbs(aip, isNew));
    } else {
      navigationToolbar.updateBreadcrumbPath(
        BreadcrumbUtils.getCreateRepresentationDescriptiveMetadataBreadcrumbs(aip, representation));
    }

    actionsToolbar.setLabel(isNew ? messages.newArchivalPackage() : messages.newDescriptiveMetadataTitle());
    actionsToolbar.build();

    initTitle(aip, title, isNew);

    DescriptiveMetadataPanel dataPanel = new DescriptiveMetadataPanel(aip.getId(),
      representation != null ? representation.getId() : null, null, null, null, false);
    descriptiveMetadataPanel.add(dataPanel);

    dataPanel.setSaveHandler(() -> dataPanel.getValue(new AsyncCallback<CreateDescriptiveMetadataRequest>() {
      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
        dataPanel.setSaveEnabled(true);
      }

      @Override
      public void onSuccess(CreateDescriptiveMetadataRequest request) {
        createMetadata(dataPanel, request);
      }
    }));

    dataPanel.setCancelHandler(this::cancel);

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");
  }

  protected static void initTitle(IndexedAIP aip, TitlePanel title, boolean isNew) {
    if (isNew) {
      title.setIcon(DescriptionLevelUtils.getTopIconSafeHtml());
      title.setText(messages.newArchivalPackage());
      return;
    }

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
  }

  protected static void initTitle(String aipId, TitlePanel title) {
    Services service = new Services("Get AIP", "get");
    service
      .rodaEntityRestService(s -> s.findByUuid(aipId, LocaleInfo.getCurrentLocale().getLocaleName()), IndexedAIP.class)
      .whenComplete((aip, error) -> {
        if (error != null) {
          AsyncCallbackUtils.defaultFailureTreatment(error);
        } else {
          initTitle(aip, title, false);
          title.addStyleName("mb-16");
        }
      });
  }

  private void createMetadata(DescriptiveMetadataPanel dataPanel, CreateDescriptiveMetadataRequest request) {
    Services service = new Services("Create Descriptive metadata", "create");

    if (isAipMetadata()) {
      service.aipResource(s -> s.createAIPDescriptiveMetadata(aip.getId(), request))
        .whenComplete((value, error) -> handleCreateResult(dataPanel, error));
    } else {
      service.aipResource(s -> s.createRepresentationDescriptiveMetadata(aip.getId(), representation.getId(), request))
        .whenComplete((value, error) -> handleCreateResult(dataPanel, error));
    }
  }

  private void handleCreateResult(DescriptiveMetadataPanel dataPanel, Throwable error) {
    if (error != null) {
      if (error instanceof ValidationException) {
        dataPanel.setErrors((ValidationException) error);
      } else if (error instanceof AlreadyExistsException) {
        dataPanel.setAlreadyExistsError();
      } else {
        AsyncCallbackUtils.defaultFailureTreatment(error);
      }
      dataPanel.setSaveEnabled(true);
    } else {
      dataPanel.clearErrors();
      Toast.showInfo(messages.dialogSuccess(), messages.metadataFileCreated());

      if (isAipMetadata()) {
        HistoryUtils.newHistory(BrowseTop.RESOLVER, aip.getId());
      } else {
        HistoryUtils.newHistory(BrowseRepresentation.RESOLVER, aip.getId(), representation.getId());
      }
    }
  }

  private void cancel() {
    if (isNew) {
      if (isAipMetadata()) {
        Services service = new Services("Delete AIP", "deletion");

        DeleteRequest request = new DeleteRequest();
        request.setItemsToDelete(new SelectedItemsListRequest(Collections.singletonList(aip.getId())));
        request.setDetails("");

        service.aipResource(s -> s.deleteAIPs(request)).whenComplete((value, error) -> {
          if (error != null) {
            HistoryUtils.newHistory(InternalProcess.RESOLVER);
          } else {
            HistoryUtils.newHistory(LastSelectedItemsSingleton.getInstance().getLastHistory());
          }
        });
      } else {
        HistoryUtils.newHistory(BrowseRepresentation.RESOLVER, aip.getId(), representation.getId());
      }
    } else {
      if (isAipMetadata()) {
        HistoryUtils.newHistory(BrowseTop.RESOLVER, aip.getId());
      } else {
        HistoryUtils.newHistory(BrowseRepresentation.RESOLVER, aip.getId(), representation.getId());
      }
    }
  }

  private boolean isAipMetadata() {
    return representation == null;
  }

  interface MyUiBinder extends UiBinder<Widget, CreateDescriptiveMetadata> {
  }

}
