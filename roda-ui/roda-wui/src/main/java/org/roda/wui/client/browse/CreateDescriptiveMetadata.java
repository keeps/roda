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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.metadata.*;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationIssue;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.FormUtilities;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;
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
public class CreateDescriptiveMetadata extends Composite {
  public static final String NEW = "new";

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      boolean isAIP = historyTokens.get(0).equals(RodaConstants.RODA_OBJECT_AIP);

      if ((isAIP && (historyTokens.size() == 2 || historyTokens.size() == 3))
        || (historyTokens.get(0).equals(RodaConstants.RODA_OBJECT_REPRESENTATION)
          && (historyTokens.size() == 3 || historyTokens.size() == 4))) {
        final String aipId = historyTokens.get(1);
        boolean newAIP;
        CreateDescriptiveMetadata create;

        if (isAIP) {
          newAIP = historyTokens.size() == 3 && historyTokens.get(2).equals(NEW);
          create = new CreateDescriptiveMetadata(aipId, null, newAIP);
        } else {
          final String representationId = historyTokens.get(2);
          newAIP = historyTokens.size() == 4 && historyTokens.get(3).equals(NEW);
          create = new CreateDescriptiveMetadata(aipId, representationId, newAIP);
        }

        callback.onSuccess(create);
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

  interface MyUiBinder extends UiBinder<Widget, CreateDescriptiveMetadata> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final String aipId;
  private final String representationId;
  private final boolean isNew;

  private boolean inXML = false;

  private Set<MetadataValue> values = null;
  private String template = "";
  private TextArea metadataXML;
  private String metadataTextFromForm = null;

  @UiField
  TextBox id;

  @UiField
  ListBox type;

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
  Button buttonCancel;

  @UiField
  HTML errors;

  @UiField
  HTML idError;

  @UiField
  TitlePanel title;

  /**
   * Create a new panel to edit a user
   *
   * @param user
   *          the user to edit
   */
  public CreateDescriptiveMetadata(String aipId, String representationId, boolean isNew) {
    this.aipId = aipId;
    this.representationId = representationId;
    this.isNew = isNew;

    initWidget(uiBinder.createAndBindUi(this));
    metadataXML = new TextArea();
    metadataXML.addStyleName("form-textbox metadata-edit-area metadata-form-textbox");

    initTitle(aipId, title);

    type.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        String value = type.getSelectedValue();

        Services service = new Services("Retrieve descriptive metadata", "get");

        service
          .aipResource(s -> s.retrieveAIPSupportedMetadata(aipId, value, LocaleInfo.getCurrentLocale().getLocaleName()))
          .whenComplete((result, error) -> {
            if (error == null) {
              template = result.getTemplate();
              values = result.getValue();
              updateFormOrXML();
            }
          });

        if (StringUtils.isNotBlank(value)) {

          id.setText(value + RodaConstants.PREMIS_SUFFIX);
        } else {
          id.setText("");
        }

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
          type.addItem(messages.otherItem(), "Other");
          type.setSelectedIndex(0);

          service.aipResource(s -> s.retrieveAIPSupportedMetadata(aipId, type.getSelectedValue(),
            LocaleInfo.getCurrentLocale().getLocaleName())).whenComplete((result, caught) -> {
              if (caught == null) {
                template = result.getTemplate();
                values = result.getValue();
                updateFormOrXML();
              }
            });

          id.setText(type.getSelectedValue() + RodaConstants.PREMIS_SUFFIX);

        }
      });

    Element firstElement = showXml.getElement().getFirstChildElement();
    if ("input".equalsIgnoreCase(firstElement.getTagName())) {
      firstElement.setAttribute("title", "browse input");
    }
  }

  protected static void initTitle(String aipId, TitlePanel title) {
    Services service = new Services("Get AIP", "get");
    service.aipResource(s -> s.findByUuid(aipId, LocaleInfo.getCurrentLocale().getLocaleName())).thenCompose(
      aip -> service.aipResource(s -> s.getDescriptiveMetadata(aipId, LocaleInfo.getCurrentLocale().getLocaleName()))
        .whenComplete((value, error) -> {
          if (error == null) {
            if (aip.getLevel() != null) {
              title.setIcon(DescriptionLevelUtils.getElementLevelIconSafeHtml(aip.getLevel(), false));
            } else {
              title.setIcon(DescriptionLevelUtils.getTopIconSafeHtml());
            }

            if (aip.getTitle() != null) {
              title.setText(aip.getTitle());
            } else if (value.getDescriptiveMetadataInfoList().isEmpty()) {
              title.setText(messages.newArchivalPackage());
            } else {
              title.setText(aipId);
            }
          }
        }));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  private void createForm() {
    formOrXML.clear();
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
      formOrXML.clear();
      if (!template.equals("")) {
        metadataXML.setText(template);
      } else {
        metadataXML.setText("");
      }
      formOrXML.add(metadataXML);
      showXml.setVisible(false);
    }
  }

  private void updateMetadataXML() {

    Services service = new Services("Update Descriptive metadata", "get");

    DescriptiveMetadataPreviewRequest previewRequest = new DescriptiveMetadataPreviewRequest(type.getSelectedValue(),
      values);
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
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    buttonApply.setEnabled(false);
    String idText = type.getSelectedValue();
    String filename = id.getText();
    String typeText = idText.contains("_")  ? idText.substring(0, idText.lastIndexOf("_")) : idText;
    String typeVersion = idText.contains("_") ? idText.substring(idText.lastIndexOf("_") + 1) : null;
    String xmlText = metadataXML.getText();
    boolean hasOverridenTheForm = inXML && !xmlText.equals(metadataTextFromForm);

    if (idText.length() > 0 && filename.endsWith(".xml")) {
      // we only send the values map if the user hasn't overriden the form by
      // modifying the XML directly
      CreateDescriptiveMetadataRequest body;
      if (!hasOverridenTheForm && !values.isEmpty()) {
        body = new DescriptiveMetadataRequestForm(idText, filename, typeText, typeVersion, template, true, null,
          values);
      } else {
        body = new DescriptiveMetadataRequestXML(idText, filename, typeText, typeVersion, template, true, null,
          xmlText);
      }
      Services service = new Services("Create Descriptive metadata", "create");
      // GWT.
      if (representationId == null) {

        service.aipResource(s -> s.createAIPDescriptiveMetadata(aipId, body)).whenComplete((value, error) -> {
          if (error != null) {
            if (error instanceof ValidationException) {
              ValidationException o = (ValidationException) error;
              updateErrors(o);
              idError.setVisible(false);
            } else if (error instanceof AlreadyExistsException) {
              idError.setVisible(true);
              idError.setHTML(SafeHtmlUtils.fromSafeConstant(messages.fileAlreadyExists()));
              errors.setVisible(false);
            } else {
              idError.setVisible(false);
              AsyncCallbackUtils.defaultFailureTreatment(error);
            }
            buttonApply.setEnabled(true);
          } else {
            errors.setText("");
            errors.setVisible(false);
            Toast.showInfo(messages.dialogSuccess(), messages.metadataFileCreated());
            HistoryUtils.newHistory(BrowseTop.RESOLVER, aipId);
          }
        });
      } else {
        // route will be created on representations
      }

    } else {
      Toast.showError("Please fill the mandatory fields correctly");
      buttonApply.setEnabled(true);
    }

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

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    if (isNew) {
      if (representationId == null) {

        Services service = new Services("Delete AIP", "deletion");

        SelectedItemsList<IndexedAIP> selected = new SelectedItemsList<>(Arrays.asList(aipId),
          IndexedAIP.class.getName());

        service.aipResource(s -> s.deleteAIPs(selected, null)).whenComplete((value, error) -> {
          if (error != null) {
            HistoryUtils.newHistory(InternalProcess.RESOLVER);
          } else {
            HistoryUtils.newHistory(LastSelectedItemsSingleton.getInstance().getLastHistory());
          }
        });

      } else {
        HistoryUtils.newHistory(BrowseRepresentation.RESOLVER, aipId, representationId);
      }
    } else {
      if (representationId == null) {
        HistoryUtils.newHistory(BrowseTop.RESOLVER, aipId);
      } else {
        HistoryUtils.newHistory(BrowseRepresentation.RESOLVER, aipId, representationId);
      }
    }
  }

}
