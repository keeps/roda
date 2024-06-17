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

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.metadata.CreateDescriptiveMetadataRequest;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataPreviewRequest;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataRequestXML;
import org.roda.core.data.v2.ip.metadata.SupportedMetadata;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationIssue;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.FormUtilities;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

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
        final String descriptiveMetadataId = new HTML(historyTokens.get(historyTokens.size() - 1)).getText();
        GWT.log(descriptiveMetadataId);

        Services service = new Services("Get aip lock", "get");
        service.aipResource(s -> s.requestAIPLock(aipId)).whenComplete((value, error) -> {
          if (error == null) {
            if (value) {

              if (representationId == null) {
                service.aipResource(s -> s.retrieveSpecificDescriptiveMetadata(aipId, descriptiveMetadataId,
                  LocaleInfo.getCurrentLocale().getLocaleName())).whenComplete((result, throwable) -> {
                    if (throwable != null) {
                      callback.onFailure(throwable);
                    } else {
                      callback.onSuccess(new EditDescriptiveMetadata(aipId, representationId, result));
                    }
                  });
              } else {
                // representation method to do
              }

              GWT.log("teste");
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

  private final String aipId;
  private final String representationId;
  @UiField
  TextBox id;
  @UiField
  ListBox type;
  @UiField
  Label formSimilarDanger;
  @UiField
  FocusPanel showXml;
  @UiField
  HTML showXmlIconXML;
  @UiField
  HTML showXmlIconForm;
  @UiField
  FlowPanel formOrXML;
  @UiField
  Button buttonApply;
  @UiField
  Button buttonRemove;
  @UiField
  Button buttonCancel;
  @UiField
  HTML errors;
  @UiField
  TitlePanel title;
  private final boolean isSimilar;
  private Set<MetadataValue> values = null;
  private Set<MetadataValue> supportedMetadataValues = null;
  private String template = "";
  private String supportedTemplate = "";
  private String packageId = "";
  private Permissions permissions = null;

  private boolean inXML = false;
  private TextArea metadataXML;
  private String metadataTextFromForm = null;
  private boolean aipLocked;

  /**
   * Create a new panel to edit a descriptive metadata
   *
   * @param aipId
   * @param representationId
   *          the user to edit
   */
  public EditDescriptiveMetadata(final String aipId, final String representationId,
    final CreateDescriptiveMetadataRequest bundleParam) {
    GWT.log("vou comecar a editar");
    this.aipId = aipId;
    this.representationId = representationId;
    this.values = bundleParam.getValues();
    this.template = bundleParam.getRawTemplate();
    this.permissions = bundleParam.getPermissions();
    this.isSimilar = bundleParam.isSimilar();
    this.packageId = bundleParam.getId();

    aipLocked = true;

    // Create new Set of MetadataValues so we can keep the original
    Set<MetadataValue> newValues = null;
    if (values != null) {
      newValues = new TreeSet<>();
      for (MetadataValue mv : values) {
        newValues.add(mv.copy());
      }
    }

    supportedMetadataValues = newValues;
    supportedTemplate = template;

    initWidget(uiBinder.createAndBindUi(this));

    CreateDescriptiveMetadata.initTitle(aipId, title);

    metadataXML = new TextArea();
    metadataXML.addStyleName("form-textbox metadata-edit-area metadata-form-textbox");
    metadataXML.setTitle("Metadata edit area");

    id.setText(bundleParam.getId());
    id.setEnabled(false);

    type.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent changeEvent) {
        setInXML(false);
        String typeString = null;
        String version = null;
        String value = type.getSelectedValue();
        if (value.contains(RodaConstants.METADATA_VERSION_SEPARATOR)) {
          typeString = value.substring(0, value.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR));
          version = value.substring(value.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR) + 1, value.length());
        }

        if (typeString == null) {
          typeString = value;
        }

        Services service = new Services("Retrieve descripitive metadta", "get");

        service
          .aipResource(s -> s.retrieveAIPSupportedMetadata(aipId, value, LocaleInfo.getCurrentLocale().getLocaleName()))
          .whenComplete((result, error) -> {
            if (error == null) {
              values = result.getValue();
              template = result.getTemplate();
              supportedTemplate = result.getTemplate();
              supportedMetadataValues = result.getValue();
              GWT.log(values.toString());
              updateFormOrXML();
            }
          });
      }
    });

    Services service = new Services("Retrieve supported metadata", "get");
    service.aipResource(s -> s.retrieveSupportedMetadataTypes(LocaleInfo.getCurrentLocale().getLocaleName()))
      .whenComplete((value, error) -> {
        if (error != null) {
          AsyncCallbackUtils.defaultFailureTreatment(error);
        } else {
          for (SupportedMetadata sm : value) {
            type.addItem(sm.getLabel(), sm.getId());
          }
          type.addItem(messages.otherItem(), "");

          service.aipResource(s -> s.retrieveAIPSupportedMetadata(aipId, type.getSelectedValue(),
            LocaleInfo.getCurrentLocale().getLocaleName())).whenComplete((result, caught) -> {
              if (caught == null) {
                GWT.log("result -> " + result.toString());
                template = result.getTemplate();
                values = result.getValue();
                updateFormOrXML();
              }
            });

          id.setText(type.getSelectedValue() + RodaConstants.PREMIS_SUFFIX);

        }
      });

    PermissionClientUtils.bindPermission(buttonRemove, permissions,
      RodaConstants.PERMISSION_METHOD_DELETE_DESCRIPTIVE_METADATA_FILE);

    Element firstElement = showXml.getElement().getFirstChildElement();
    if ("input".equalsIgnoreCase(firstElement.getTagName())) {
      firstElement.setAttribute("title", "browse input");
    }
  }

  @Override
  protected void onDetach() {
    if (aipLocked) {
      BrowserService.Util.getInstance().releaseAIPLock(this.aipId, new NoAsyncCallback<Void>() {
        @Override
        public void onSuccess(Void result) {
          aipLocked = false;
        }
      });
    }
    super.onDetach();
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  private void createForm() {
    formOrXML.clear();
    metadataXML.setText(template);
    FormUtilities.create(formOrXML, values, true);
  }

  public void setInXML(boolean inXML) {
    this.inXML = inXML;
    showXmlIconXML.setVisible(!inXML);
    showXmlIconForm.setVisible(inXML);
  }

  @UiHandler("showXml")
  void buttonShowXmlHandler(ClickEvent e) {
    setInXML(!inXML);
    updateFormOrXML();
  }

  private void updateFormOrXML() {
    if (values != null && !values.isEmpty()) {
      if (isSimilar) {
        formSimilarDanger.setVisible(true);
      } else {
        formSimilarDanger.setVisible(false);
      }
      showXml.setVisible(true);

      if (inXML) {
        updateMetadataXML();
      } else {
        // if the user changed the metadata text
        if (metadataTextFromForm != null && !metadataXML.getText().equals(metadataTextFromForm)) {
          Dialogs.showConfirmDialog(messages.confirmChangeToFormTitle(), messages.confirmChangeToFormMessage(),
            messages.dialogCancel(), messages.dialogYes(), new AsyncCallback<Boolean>() {
              @Override
              public void onFailure(Throwable caught) {
                AsyncCallbackUtils.defaultFailureTreatment(caught);
              }

              @Override
              public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                  formOrXML.clear();
                  createForm();
                } else {
                  setInXML(!inXML);
                }
              }
            });
        } else {
          formOrXML.clear();
          createForm();
        }
      }
    } else {
      setInXML(true);
      formSimilarDanger.setVisible(false);
      formOrXML.clear();
      if (!template.equals("")) {
        metadataXML.setText(template);
      } else {
        metadataXML.setText("");
      }
      formOrXML.add(metadataXML);
      showXml.setVisible(false);
      metadataTextFromForm = null;
    }
  }

  private boolean hasModifiedForm() {
    HashMap<String, MetadataValue> formMap = new HashMap<>();
    for (MetadataValue mv : supportedMetadataValues) {
      formMap.put(mv.getId(), mv);
    }

    HashMap<String, MetadataValue> bundleMap = new HashMap<>();
    for (MetadataValue mv : values) {
      bundleMap.put(mv.getId(), mv);
    }

    for (Entry<String, MetadataValue> entry : formMap.entrySet()) {
      String key = entry.getKey();
      MetadataValue mvForm = entry.getValue();
      String formValue = mvForm != null ? mvForm.get("value") : "";
      MetadataValue mvBundle = bundleMap.get(key);
      String bundleValue = mvBundle != null ? bundleMap.get(key).get("value") : "";

      if ((formValue != null && !formValue.equals(bundleValue)) || (formValue == null && bundleValue != null)) {
        return true;
      }
    }
    return false;
  }

  private void updateMetadataXML() {
    if (hasModifiedForm()) {
      // Apply the form values to the template (server)
      Services service = new Services("Update Descriptive metadata", "get");

      DescriptiveMetadataPreviewRequest previewRequest = new DescriptiveMetadataPreviewRequest(type.getSelectedValue(),
        values);
      GWT.log("request -> " + previewRequest);
      service.aipResource(s -> s.retrieveDescriptiveMetadataPreview(previewRequest)).whenComplete((value, error) -> {
        if (error != null) {
          AsyncCallbackUtils.defaultFailureTreatment(error);
        } else {
          formOrXML.clear();
          metadataXML.setText(value.getPreview());
          formOrXML.add(metadataXML);
          metadataTextFromForm = value.getPreview();
        }
      });
    } else {
      formOrXML.clear();
      metadataXML.setText(template);
      formOrXML.add(metadataXML);
      metadataTextFromForm = template;
    }
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    String xmlText = metadataXML.getText();
    if (inXML) {
      updateMetadataOnServer(xmlText);
    } else {

      Services service = new Services("Update Descriptive metadata", "get");

      DescriptiveMetadataPreviewRequest previewRequest = new DescriptiveMetadataPreviewRequest(type.getSelectedValue(),
        values);
      service.aipResource(s -> s.retrieveDescriptiveMetadataPreview(previewRequest)).whenComplete((value, error) -> {
        if (error != null) {
          AsyncCallbackUtils.defaultFailureTreatment(error);
        } else {
          updateMetadataOnServer(value.getPreview());
        }
      });
    }
  }

  private void updateMetadataOnServer(String content) {
    String typeText = type.getSelectedValue();
    String version = null;

    if (typeText.contains(RodaConstants.METADATA_VERSION_SEPARATOR)) {
      version = typeText.substring(typeText.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR) + 1);
      typeText = typeText.substring(0, typeText.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR));
    }

    CreateDescriptiveMetadataRequest request = new DescriptiveMetadataRequestXML(packageId, "", typeText, version,
      template, isSimilar, permissions, content);

    Dialogs.showConfirmDialog(messages.updateMetadataFileTitle(), messages.updateMetadataFileLabel(),
      messages.cancelButton(), messages.confirmButton(), new NoAsyncCallback<Boolean>() {

        public void onSuccess(Boolean confirm) {
          if (confirm) {

            Services service = new Services("Update descriptive metadata", "update");

            service.aipResource(s -> s.updateDescriptiveMetadataFile(aipId, request)).whenComplete((value, error) -> {
              if (error != null) {
                if (error instanceof ValidationException) {
                  ValidationException e = (ValidationException) error;
                  updateErrors(e);
                } else {
                  AsyncCallbackUtils.defaultFailureTreatment(error);
                }
              } else {
                errors.setText("");
                errors.setVisible(false);
                Toast.showInfo(messages.dialogSuccess(), messages.metadataFileSaved());
                back();
              }
            });

          }
        }
      });

  }

  protected void updateErrors(ValidationException e) {
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    for (ValidationIssue issue : e.getReport().getIssues()) {
      b.append(SafeHtmlUtils.fromSafeConstant("<span class='error'>"));
      b.append(messages.metadataParseError(issue.getLineNumber(), issue.getColumnNumber(), issue.getMessage()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
    }

    errors.setHTML(b.toSafeHtml());
    errors.setVisible(true);
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    Dialogs.showConfirmDialog(messages.removeMetadataFileTitle(), messages.removeMetadataFileLabel(),
      messages.cancelButton(), messages.confirmButton(), new NoAsyncCallback<Boolean>() {

        public void onSuccess(Boolean confirm) {
          if (confirm) {
            Services service = new Services("Delete Metadata File", "delete");

            service.aipResource(s -> s.deleteDescriptiveMetadataFile(aipId, type.getSelectedValue()))
              .whenComplete((value, error) -> {
                if (error == null) {
                  Toast.showInfo(messages.dialogSuccess(), messages.metadataFileRemoved());
                  back();
                } else {
                  AsyncCallbackUtils.defaultFailureTreatment(error);
                }
              });

          }
        }

      });
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    back();
  }

  private void back() {
    if (representationId == null) {
      HistoryUtils.openBrowse(aipId);
    } else {
      HistoryUtils.openBrowse(aipId, representationId);
    }
  }

  interface MyUiBinder extends UiBinder<Widget, EditDescriptiveMetadata> {
  }
}
