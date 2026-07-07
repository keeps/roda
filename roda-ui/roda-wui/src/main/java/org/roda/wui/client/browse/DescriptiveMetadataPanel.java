package org.roda.wui.client.browse;

import java.util.Set;
import java.util.stream.IntStream;

import com.google.gwt.user.client.ui.TextBox;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.metadata.ConfiguredDescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.ConfiguredDescriptiveMetadataList;
import org.roda.core.data.v2.ip.metadata.CreateDescriptiveMetadataRequest;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfo;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataPreviewRequest;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataRequestForm;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataRequestXML;
import org.roda.core.data.v2.ip.metadata.SelectedType;
import org.roda.core.data.v2.ip.metadata.SupportedMetadataValue;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationIssue;
import org.roda.wui.client.browse.tabs.DescriptiveMetadataTabs;
import org.roda.wui.client.common.forms.GenericDataForm;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.FormUtilities;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class DescriptiveMetadataPanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final String aipId;
  private final String representationId;
  private final String filename;
  private final Permissions permissions;
  private final boolean editMode;

  private final GenericDataForm<DescriptiveMetadata> form;
  private final DescriptiveMetadataInfos descriptiveMetadataInfos;
  private TextBox filenameTextBox;
  private Label filenameLabel;
  private ListBox type;
  private HTML idError;
  private Label formSimilarDanger;
  private HTML errors;
  private FlowPanel actionsPanel;
  private Button saveButton;
  private Button cancelButton;
  private SimplePanel tabsContainer;
  private Set<MetadataValue> values;
  private String metadataId;
  private boolean inXML = false;
  private boolean isSimilar = true;
  private TextArea metadataXML;
  private String metadataTextFromForm;
  private ConfiguredDescriptiveMetadataList supportedDescriptiveMetadataList;

  public DescriptiveMetadataPanel(String aipId, String representationId, String filename,
    SupportedMetadataValue initialValues, Permissions permissions, boolean editMode,
    DescriptiveMetadataInfos descriptiveMetadataInfos) {
    this.aipId = aipId;
    this.representationId = representationId;
    this.filename = filename;
    this.permissions = permissions;
    this.editMode = editMode;
    this.form = new GenericDataForm<>();
    this.descriptiveMetadataInfos = descriptiveMetadataInfos;

    this.metadataId = editMode && filename != null ? filename.replace(".xml", "") : null;
    this.values = initialValues != null ? initialValues.getValue() : null;

    buildFields();
    initWidget(form);

    loadSupportedMetadataTypes();
  }

  private void buildFields() {
    form.addCustomWidget(createFilenameField());

    type = new ListBox();

    form.addListBox(messages.metadataType(), type,
      metadata -> metadata.getType() != null ? getTypeValue(metadata.getType(), metadata.getVersion()) : "",
      (metadata, value) -> {
        metadata.setType(getTypeText(value));
        metadata.setVersion(getTypeVersion(value));
      }, true);

    idError = new HTML();
    idError.addStyleName("error metadata-edit-errors");
    idError.setVisible(false);
    form.addCustomWidget(idError);

    formSimilarDanger = new Label(messages.editDescriptionMetadataWarning());
    formSimilarDanger.addStyleName("label-danger");
    formSimilarDanger.setVisible(false);
    form.addCustomWidget(formSimilarDanger);

    errors = new HTML();
    errors.addStyleName("metadata-edit-errors");
    errors.setVisible(false);
    form.addCustomWidget(errors);

    form.addCustomWidget(createMetadataEditor());
    saveButton = new Button(messages.saveButton());
    saveButton.addStyleName("btn btn-primary btn-play");

    cancelButton = new Button(messages.cancelButton());
    cancelButton.addStyleName("btn btn-link");

    actionsPanel = new FlowPanel();
    actionsPanel.addStyleName("alignButtonsPanel");
    actionsPanel.setVisible(false);
    actionsPanel.add(saveButton);
    actionsPanel.add(cancelButton);

    form.addCustomWidget(actionsPanel);

    type.addChangeHandler(event -> retrieveSelectedTypeMetadata());
  }

  private Widget createFilenameField() {
    FlowPanel field = new FlowPanel();
    field.addStyleName("generic-form-field");

    FlowPanel leftPanel = new FlowPanel();
    leftPanel.addStyleName("generic-form-field-left-panel");

    Label label = new Label(messages.metadataFilename());
    label.addStyleName("form-label");
    label.addStyleName("form-label-mandatory");

    FlowPanel inputPanel = new FlowPanel();
    inputPanel.addStyleName("generic-form-field-input-panel full_width");

    if (editMode) {
      filenameLabel = new Label();
      filenameLabel.addStyleName("form-readonly-value");
      inputPanel.add(filenameLabel);
    } else {
      filenameTextBox = new TextBox();
      filenameTextBox.addStyleName("form-textbox");
      inputPanel.add(filenameTextBox);
    }

    leftPanel.add(label);
    leftPanel.add(inputPanel);
    field.add(leftPanel);

    return field;
  }

  private DescriptiveMetadata createModel() {
    String selectedId = editMode ? metadataId : "";
    return new DescriptiveMetadata(selectedId, aipId, representationId, getTypeText(selectedId),
      getTypeVersion(selectedId));
  }

  private Widget createMetadataEditor() {
    metadataXML = new TextArea();
    metadataXML.addStyleName("form-textbox");
    metadataXML.addStyleName("metadata-edit-area");
    tabsContainer = new SimplePanel();

    FlowPanel metadataPanel = new FlowPanel();
    metadataPanel.add(tabsContainer);

    return metadataPanel;
  }

  public void setSaveHandler(Runnable onSave) {
    actionsPanel.setVisible(true);
    saveButton.addClickHandler(event -> {
      if (isValid()) {
        saveButton.setEnabled(false);
        onSave.run();
      } else {
        Toast.showError("Please fill the mandatory fields correctly");
      }
    });
  }

  public void setCancelHandler(Runnable onCancel) {
    actionsPanel.setVisible(true);
    cancelButton.addClickHandler(event -> onCancel.run());
  }

  public boolean isValid() {
    if (editMode) {
      return form.isValid() && StringUtils.isNotBlank(getSelectedTypeValue())
        && StringUtils.isNotBlank(filenameLabel.getText()) && filenameLabel.getText().endsWith(".xml");
    } else {
      return form.isValid() && StringUtils.isNotBlank(getSelectedTypeValue())
        && StringUtils.isNotBlank(filenameTextBox.getText()) && filenameTextBox.getText().endsWith(".xml");
    }
  }

  private String getSelectedTypeValue() {
    return type.getSelectedValue();
  }

  public void getValue(AsyncCallback<CreateDescriptiveMetadataRequest> callback) {
    if (editMode) {
      getEditValue(callback);
    } else {
      getCreateValue(callback);
    }
  }

  public void setSaveEnabled(boolean enabled) {
    saveButton.setEnabled(enabled);
  }

  public void clearErrors() {
    errors.setText("");
    errors.setVisible(false);
    idError.setText("");
    idError.setVisible(false);
  }

  public void setAlreadyExistsError() {
    idError.setVisible(true);
    idError.setHTML(SafeHtmlUtils.fromSafeConstant(messages.fileAlreadyExists()));
    errors.setVisible(false);
  }

  public void setErrors(ValidationException e) {
    idError.setVisible(false);
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    for (ValidationIssue issue : e.getReport().getIssues()) {
      b.append(SafeHtmlUtils.fromSafeConstant("<span class='error'>"));
      b.append(messages.metadataParseError(issue.getLineNumber(), issue.getColumnNumber(), issue.getMessage()));
      b.append(SafeHtmlUtils.fromSafeConstant("</span>"));
    }

    errors.setHTML(b.toSafeHtml());
    errors.setVisible(true);
  }

  private boolean isAipMetadata() {
    return representationId == null;
  }

  private void loadSupportedMetadataTypes() {
    Services service = new Services("Retrieve supported metadata", "get");
    service.aipResource(s -> s.retrieveSupportedMetadataTypes(LocaleInfo.getCurrentLocale().getLocaleName()))
      .whenComplete((value, error) -> {
        if (error != null) {
          AsyncCallbackUtils.defaultFailureTreatment(error);
        } else {
          supportedDescriptiveMetadataList = value;
          for (ConfiguredDescriptiveMetadata sm : value.getList()) {
            type.addItem(sm.getLabel(), sm.getId());
          }
          type.addItem(messages.otherItem(), "Other");

          if (editMode) {
            DescriptiveMetadataInfo metadataInfo = descriptiveMetadataInfos.getDescriptiveMetadataInfoList().stream()
              .filter(info -> info.getId().equals(filename)).findFirst().orElse(null);
            String id = metadataInfo.getVersion() != null ? metadataInfo.getType().toLowerCase() + "_" + metadataInfo.getVersion()
              : metadataInfo.getType();

            int targetIndex = IntStream.range(0, type.getItemCount()).filter(i -> type.getValue(i).equals(id))
              .findFirst().orElse(type.getItemCount() - 1);

            filenameLabel.setText(filename);
            form.setModel(createModel());
            type.setSelectedIndex(targetIndex);
            updateFormOrXML();
          } else {
            type.setSelectedIndex(0);

            String selectedType = type.getSelectedValue();
            filenameTextBox
              .setText(StringUtils.isNotBlank(selectedType) ? selectedType + RodaConstants.PREMIS_SUFFIX : "");

            form.setModel(createModel());
            retrieveSelectedTypeMetadata();
          }
        }
      });
  }

  private void retrieveSelectedTypeMetadata() {
    inXML = false;
    String selectedType = type.getSelectedValue();
    Services service = new Services("Retrieve descriptive metadata", "get");

    if (isAipMetadata()) {
      service
        .aipResource(
          s -> s.retrieveAIPSupportedMetadata(aipId, selectedType, LocaleInfo.getCurrentLocale().getLocaleName()))
        .whenComplete((result, error) -> {
          if (error == null) {
            values = result.getValue();
            if (editMode) {
              checkAipSimilarity(service, selectedType, result);
            } else {
              updateFormOrXML();
            }
          }
        });
    } else {
      service.aipResource(s -> s.retrieveRepresentationSupportedMetadata(aipId, representationId, selectedType,
        LocaleInfo.getCurrentLocale().getLocaleName())).whenComplete((result, error) -> {
          if (error == null) {
            values = result.getValue();
            if (editMode) {
              checkRepresentationSimilarity(service, selectedType, result);
            } else {
              updateFormOrXML();
            }
          }
        });
    }

    if (!editMode) {
      filenameTextBox.setText(StringUtils.isNotBlank(selectedType) ? selectedType + RodaConstants.PREMIS_SUFFIX : "");
    }
  }

  private void checkAipSimilarity(Services service, String selectedType, SupportedMetadataValue result) {
    service
      .aipResource(s -> s.isAIPMetadataSimilar(aipId, metadataId, new SelectedType(selectedType, result.getValue())))
      .whenComplete((similar, error) -> {
        if (error == null) {
          isSimilar = similar;
          updateFormOrXML();
        }
      });
  }

  private void checkRepresentationSimilarity(Services service, String selectedType, SupportedMetadataValue result) {
    service.aipResource(s -> s.isRepresentationMetadataSimilar(aipId, representationId, metadataId,
      new SelectedType(selectedType, result.getValue()))).whenComplete((similar, error) -> {
        if (error == null) {
          isSimilar = similar;
          updateFormOrXML();
        }
      });
  }

  private void updateFormOrXML() {
    formSimilarDanger.setVisible(editMode && !isSimilar);
    tabsContainer.clear();

    DescriptiveMetadataTabs tabs = new DescriptiveMetadataTabs();
    if (values != null && !values.isEmpty()) {
      tabs.init(this::createFormWidget, this::createXmlWidget);

    } else {
      tabs.init(this::createXmlWidget);
    }
    tabsContainer.setWidget(tabs);
  }

  private Widget createFormWidget() {
    inXML = false;

    FlowPanel formPanel = new FlowPanel();
    FormUtilities.create(formPanel, values, true);

    return formPanel;
  }

  private Widget createXmlWidget() {
    inXML = true;

    FlowPanel xmlPanel = new FlowPanel();
    xmlPanel.add(metadataXML);

    if (editMode || (values != null && !values.isEmpty())) {
      retrievePreview(new AsyncCallback<String>() {
        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(String preview) {
          metadataXML.setText(preview);
          metadataTextFromForm = preview;
        }
      });
    } else {
      metadataXML.setText("");
      metadataTextFromForm = null;
    }

    return xmlPanel;
  }

  private void retrievePreview(AsyncCallback<String> callback) {
    Services service = new Services("Preview descriptive metadata", "get");

    String descriptiveMetadataId = supportedDescriptiveMetadataList.getList().stream()
      .filter(p -> p.getId().equals(type.getSelectedValue())).findFirst().map(ConfiguredDescriptiveMetadata::getId)
      .orElse(metadataId);

    DescriptiveMetadataPreviewRequest previewRequest = new DescriptiveMetadataPreviewRequest(descriptiveMetadataId,
      values);

    if (representationId != null) {
      service
        .aipResource(s -> s.retrieveRepresentationDescriptiveMetadataPreview(aipId, representationId, previewRequest))
        .whenComplete((value, error) -> {
          if (error != null) {
            callback.onFailure(error);
          } else {
            callback.onSuccess(value.getPreview());
          }
        });
    } else {

      service.aipResource(s -> s.retrieveDescriptiveMetadataPreview(aipId, previewRequest))
        .whenComplete((value, error) -> {
          if (error != null) {
            callback.onFailure(error);
          } else {
            callback.onSuccess(value.getPreview());
          }
        });
    }
  }

  private void getCreateValue(AsyncCallback<CreateDescriptiveMetadataRequest> callback) {
    String idText = type.getSelectedValue();
    String requestFilename = filenameTextBox.getText();
    String typeText = getTypeText(idText);
    String typeVersion = getTypeVersion(idText);
    String xmlText = metadataXML.getText();
    boolean hasOverriddenTheForm = inXML && !xmlText.equals(metadataTextFromForm);

    CreateDescriptiveMetadataRequest request;
    if (!hasOverriddenTheForm && values != null && !values.isEmpty()) {
      request = new DescriptiveMetadataRequestForm(idText, requestFilename, typeText, typeVersion, true, null, values);
    } else {
      request = new DescriptiveMetadataRequestXML(idText, requestFilename, typeText, typeVersion, true, null, xmlText);
    }

    callback.onSuccess(request);
  }

  private void getEditValue(AsyncCallback<CreateDescriptiveMetadataRequest> callback) {
    if (inXML) {
      callback.onSuccess(buildEditXmlRequest(metadataXML.getText()));
    } else {
      retrievePreview(new AsyncCallback<String>() {
        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(String preview) {
          callback.onSuccess(buildEditXmlRequest(preview));
        }
      });
    }
  }

  private CreateDescriptiveMetadataRequest buildEditXmlRequest(String content) {
    String selectedType = type.getSelectedValue();
    String typeText = getTypeText(selectedType);
    String version = getTypeVersion(selectedType);

    return new DescriptiveMetadataRequestXML(metadataId, filename, typeText, version, false, permissions, content);
  }

  private String getTypeText(String selectedType) {
    if (selectedType != null && selectedType.contains(RodaConstants.METADATA_VERSION_SEPARATOR)) {
      return selectedType.substring(0, selectedType.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR));
    }
    return selectedType;
  }

  private String getTypeVersion(String selectedType) {
    if (selectedType != null && selectedType.contains(RodaConstants.METADATA_VERSION_SEPARATOR)) {
      return selectedType.substring(selectedType.lastIndexOf(RodaConstants.METADATA_VERSION_SEPARATOR) + 1);
    }
    return null;
  }

  private String getTypeValue(String type, String version) {
    if (StringUtils.isBlank(version)) {
      return type;
    }
    return type + RodaConstants.METADATA_VERSION_SEPARATOR + version;
  }
}
