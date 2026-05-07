/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.ingest.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Panel;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.AipIdPluginParameterRenderingHints;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.properties.ConversionProfile;
import org.roda.core.data.v2.properties.ConversionProfileOutcomeType;
import org.roda.core.data.v2.properties.DropdownPluginParameterItem;
import org.roda.core.data.v2.properties.ReindexPluginObject;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.SeverityLevel;
import org.roda.wui.client.common.IncrementalAssociativeList;
import org.roda.wui.client.common.dialogs.SelectAipDialog;
import org.roda.wui.client.common.utils.PluginUtils;
import org.roda.wui.client.ingest.process.model.DisseminationParameter;
import org.roda.wui.client.ingest.process.model.RepresentationParameter;
import org.roda.wui.client.services.ConfigurationRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;

import config.i18n.client.ClientMessages;

public class PluginParameterPanel extends Composite {
  public static final String FORM_SELECTBOX = "form-listbox";
  public static final String FORM_LABEL = "form-label";
  public static final String FORM_TEXTBOX = "form-textbox";
  public static final String OBJECT_BOX = "object box";
  public static final String FORM_HELP = "form-help";
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final ClientLogger LOGGER = new ClientLogger(PluginParameterPanel.class.getName());
  private static final String ADD_TYPE = "#__ADDNEW__#";
  private final PluginParameter parameter;
  private final FlowPanel layout;
  private final RepresentationParameter representationParameter = new RepresentationParameter();
  private final DisseminationParameter disseminationParameter = new DisseminationParameter();
  private final String pluginId;
  private String value;
  private String profile;
  private String aipTitle;
  private boolean conversionPanel = false;

  public PluginParameterPanel(PluginParameter parameter, String pluginId) {
    super();
    this.parameter = parameter;
    this.pluginId = pluginId;
    layout = new FlowPanel();

    initWidget(layout);

    updateLayout();
    layout.addStyleName("plugin-option-parameter");
  }

  private void updateLayout() {
    if (PluginParameterType.BOOLEAN.equals(parameter.getType())) {
      createBooleanLayout(parameter);
    } else if (PluginParameterType.STRING.equals(parameter.getType())) {
      createStringLayout(parameter);
    } else if (PluginParameterType.PLUGIN_SIP_TO_AIP.equals(parameter.getType())) {
      createPluginSipToAipLayout();
    } else if (PluginParameterType.AIP_ID.equals(parameter.getType())) {
      createSelectAipLayout();
    } else if (PluginParameterType.RISK_ID.equals(parameter.getType())) {
      createSelectRiskLayout();
    } else if (PluginParameterType.SEVERITY.equals(parameter.getType())) {
      createSelectSeverityLayout();
    } else if (PluginParameterType.REPRESENTATION_TYPE.equals(parameter.getType())) {
      createRepresentationTypeLayout(parameter);
    } else if (PluginParameterType.RODA_OBJECT.equals(parameter.getType())) {
      createSelectRodaObjectLayout();
    } else if (PluginParameterType.INTEGER.equals(parameter.getType())) {
      createIntegerLayout();
    } else if (PluginParameterType.AIP_FIELDS.equals(parameter.getType())) {
      createPluginObjectFieldsLayout(AIP.class.getSimpleName());
    } else if (PluginParameterType.REPRESENTATION_FIELDS.equals(parameter.getType())) {
      createPluginObjectFieldsLayout(Representation.class.getSimpleName());
    } else if (PluginParameterType.FILE_FIELDS.equals(parameter.getType())) {
      createPluginObjectFieldsLayout(File.class.getSimpleName());
    } else if (PluginParameterType.PERMISSION_TYPES.equals(parameter.getType())) {
      createPermissionTypesLayout();
    } else if (PluginParameterType.DROPDOWN.equals(parameter.getType())) {
      createDropdownLayout();
    } else if (PluginParameterType.CONVERSION.equals(parameter.getType())) {
      createConversionLayout();
    } else {
      LOGGER
        .warn("Unsupported plugin parameter type: " + parameter.getType() + ". Reverting to default parameter editor.");
      createStringLayout(parameter);
    }
  }

  private void createConversionLayout() {
    conversionPanel = true;
    Label parameterName = new Label(parameter.getName());
    parameterName.addStyleName(FORM_LABEL);
    final ListBox dropdown = new ListBox();
    dropdown.addStyleName(FORM_SELECTBOX);

    dropdown.addItem(messages.conversionProfileRepresentationParameter(),
      ConversionProfileOutcomeType.REPRESENTATION.toString());
    dropdown.addItem(messages.conversionProfileDisseminationParameter(),
      ConversionProfileOutcomeType.DISSEMINATION.toString());

    value = dropdown.getSelectedValue();
    FlowPanel innerPanel = new FlowPanel();
    innerPanel.addStyleName("plugin-option-nested-panel");
    dropdown.addChangeHandler(event -> {
      value = dropdown.getSelectedValue();
      innerPanel.clear();

      FlowPanel profiles = new FlowPanel();
      profiles.addStyleName("plugin-option-nested-panel");
      createConversionProfileLayout(profiles, value, pluginId);
      innerPanel.add(profiles);

      if (value.equals(ConversionProfileOutcomeType.REPRESENTATION.toString())) {
        // Add fields
        ValueChangeHandler<String> typeChanged = typeChangedEvent -> representationParameter
          .setValue(typeChangedEvent.getValue());

        createRepresentationType(innerPanel, messages.representationTypeTitle(),
          messages.representationTypeDescription(), typeChanged);
        ValueChangeHandler<Boolean> preservationStatusChanged = preservationStatusChangedEvent -> representationParameter
          .setMarkAsPreservation(preservationStatusChangedEvent.getValue());

        createBooleanLayout(innerPanel, messages.changeRepresentationStatusToPreservationTitle(),
          Boolean.toString(true), messages.changeRepresentationStatusToPreservationDescription(), false,
          preservationStatusChanged);

        value = RodaConstants.PLUGIN_PARAMS_CONVERSION_REPRESENTATION;
      } else {
        // Add fields
        ValueChangeHandler<String> titleChanged = titleChangedEvent -> disseminationParameter
          .setTitle(titleChangedEvent.getValue());
        ValueChangeHandler<String> descriptionChanged = descriptionChangedEvent -> disseminationParameter
          .setDescription(descriptionChangedEvent.getValue());

        createTextBoxLayout(innerPanel, messages.disseminationTitle(), disseminationParameter.getTitle(),
          messages.disseminationTitleDescription(), false, titleChanged);

        createTextBoxLayout(innerPanel, messages.disseminationDescriptionTitle(),
          disseminationParameter.getDescription(), messages.disseminationDescriptionDescription(), false,
          descriptionChanged);

        value = RodaConstants.PLUGIN_PARAMS_CONVERSION_DISSEMINATION;
      }
    });

    setSelectedIndexAndFireEvent(dropdown, 0);

    dropdown.setTitle(OBJECT_BOX);
    layout.add(parameterName);
    addHelp();
    layout.add(dropdown);
    layout.add(innerPanel);
  }

  private void setSelectedIndexAndFireEvent(final ListBox listBox, final int index) {
    // Set the selected index
    listBox.setSelectedIndex(index);

    // Manually trigger a ValueChangeEvent
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), listBox);
  }

  private void createRepresentationType(FlowPanel panel, String name, String description,
    ValueChangeHandler<String> changeHandler) {
    Label parameterName = new Label(name);
    parameterName.addStyleName(FORM_LABEL);

    final ListBox selectBox = new ListBox();
    selectBox.addStyleName(FORM_SELECTBOX);
    selectBox.setTitle("representation type box");

    Services services = new Services("Retrieve representation type options", "get");
    services.representationResource(s -> s.getRepresentationTypeOptions(LocaleInfo.getCurrentLocale().getLocaleName()))
      .whenComplete((options, throwable) -> {
        if (throwable != null) {
          selectBox.setVisible(false);
        } else {
          for (String option : options.getTypes()) {
            selectBox.addItem(option);
          }

          selectBox.addItem(messages.entityTypeAddNew(), ADD_TYPE);
          selectBox.setSelectedIndex(0);
          value = selectBox.getSelectedValue();
          representationParameter.setValue(value);
        }
      });

    final TextBox newTypeBox = new TextBox();
    final Label newTypeLabel = new Label(messages.entityTypeNewLabel() + ": ");
    newTypeBox.getElement().setPropertyString("placeholder", messages.entityTypeNewLabel());
    newTypeBox.addStyleName(FORM_TEXTBOX);
    newTypeLabel.addStyleName(FORM_LABEL);
    newTypeLabel.setVisible(false);
    newTypeBox.setVisible(false);
    newTypeBox.addValueChangeHandler(changeHandler);

    selectBox.addChangeHandler(event -> {
      value = selectBox.getSelectedValue();
      representationParameter.setValue(value);
      newTypeLabel.setVisible(value.equals(ADD_TYPE));
      newTypeBox.setVisible(value.equals(ADD_TYPE));
    });

    panel.add(parameterName);
    panel.add(selectBox);
    panel.add(newTypeLabel);
    panel.add(newTypeBox);
    addHelp(panel, description);
  }

  private void createRepresentationTypeLayout(PluginParameter parameter) {
    ValueChangeHandler<String> handler = event -> value = event.getValue();

    createRepresentationType(layout, parameter.getName(), parameter.getDescription(), handler);
  }

  private void createSelectSeverityLayout() {
    Label parameterName = new Label(parameter.getName());
    parameterName.addStyleName(FORM_LABEL);
    final ListBox severityBox = new ListBox();
    severityBox.addStyleName(FORM_SELECTBOX);

    for (SeverityLevel severity : SeverityLevel.values()) {
      severityBox.addItem(messages.severityLevel(severity), severity.toString());
    }

    value = severityBox.getSelectedValue();

    severityBox.addChangeHandler(event -> value = severityBox.getSelectedValue());

    severityBox.setTitle("severity box");
    layout.add(parameterName);
    layout.add(severityBox);
    addHelp();
  }

  private void createSelectRiskLayout() {
    Label parameterName = new Label(parameter.getName());
    parameterName.addStyleName(FORM_LABEL);
    IncrementalAssociativeList list = new IncrementalAssociativeList(IndexedRisk.class, RodaConstants.RISK_ID,
      RodaConstants.INDEX_SEARCH, messages.getRisksDialogName());

    list.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        IncrementalAssociativeList sourceList = (IncrementalAssociativeList) event.getSource();
        List<String> values = sourceList.getTextBoxesValue();
        if (!values.isEmpty()) {
          value = getValuesString(values);
        }
      }

      private String getValuesString(List<String> values) {
        StringBuilder builder = new StringBuilder();

        for (String stringValue : values) {
          builder.append(stringValue).append(",");
        }

        return builder.substring(0, builder.length() - 1);
      }

    });

    layout.add(parameterName);
    layout.add(list);
    addHelp();
  }

  private void createSelectAipLayout() {
    AipIdPluginParameterRenderingHints renderingHints = null;
    if (parameter.getRenderingHints() instanceof AipIdPluginParameterRenderingHints) {
      renderingHints = (AipIdPluginParameterRenderingHints) parameter.getRenderingHints();
    }
    Label parameterName = new Label(parameter.getName());
    final HorizontalPanel editPanel = new HorizontalPanel();
    final FlowPanel aipPanel = new FlowPanel();
    final Button button = new Button(renderingHints != null && renderingHints.getCustomizedButtonLabel() != null
      ? renderingHints.getCustomizedButtonLabel()
      : messages.pluginAipIdButton());
    final FlowPanel buttonsPanel = new FlowPanel();
    final Anchor editButton = new Anchor(SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-edit\"></i>"));
    final Anchor removeButton = new Anchor(SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-remove\"></i>"));

    buttonsPanel.add(editButton);
    buttonsPanel.add(removeButton);

    final AipIdPluginParameterRenderingHints finalRenderingHints = renderingHints;
    ClickHandler editClickHandler = event -> {
      SelectAipDialog selectAipDialog;
      if (finalRenderingHints != null) {

        selectAipDialog = new SelectAipDialog(parameter.getName(), finalRenderingHints.getFilter(),
          finalRenderingHints.isJustActive(), finalRenderingHints.isExportCsvVisible());

      } else {
        selectAipDialog = new SelectAipDialog(parameter.getName());
      }
      selectAipDialog.showAndCenter();
      // default behaviour of selectAipDialog enabled
      if (finalRenderingHints == null || !finalRenderingHints.isDisableSelection()) {
        selectAipDialog.setSingleSelectionMode();
        selectAipDialog.addValueChangeHandler(event1 -> {
          IndexedAIP aip = event1.getValue();

          Label itemTitle = new Label();
          HTMLPanel itemIconHtmlPanel = DescriptionLevelUtils.getElementLevelIconHTMLPanel(aip.getLevel());
          itemIconHtmlPanel.addStyleName("itemIcon");
          itemTitle.setText(aip.getTitle() != null ? aip.getTitle() : aip.getId());
          itemTitle.addStyleName("itemText");

          aipPanel.clear();
          aipPanel.add(itemIconHtmlPanel);
          aipPanel.add(itemTitle);

          editPanel.add(aipPanel);
          editPanel.add(buttonsPanel);

          editPanel.setCellWidth(aipPanel, "100%");

          editPanel.setVisible(true);
          button.setVisible(false);

          value = aip.getId();
          aipTitle = aip.getTitle();
        });
      }
    };
    if (finalRenderingHints == null || !finalRenderingHints.isDisableSelection()) {
      ClickHandler removeClickHandler = event -> {
        editPanel.setVisible(false);
        button.setVisible(true);

        value = null;
      };
      removeButton.addClickHandler(removeClickHandler);
    }
    button.addClickHandler(editClickHandler);
    editButton.addClickHandler(editClickHandler);

    layout.add(parameterName);
    layout.add(button);
    layout.add(editPanel);
    addHelp();

    parameterName.addStyleName(FORM_LABEL);
    aipPanel.addStyleName("itemPanel");
    button.addStyleName("btn btn-play");
    buttonsPanel.addStyleName("itemButtonsPanel");
    editButton.addStyleName("toolbarLink toolbarLinkSmall");
    removeButton.addStyleName("toolbarLink toolbarLinkSmall");
  }

  private void createSelectRodaObjectLayout() {
    Label parameterName = new Label(parameter.getName());
    parameterName.addStyleName(FORM_LABEL);

    final ListBox objectBox = new ListBox();
    objectBox.addStyleName(FORM_SELECTBOX);

    Services services = new Services("Retrieve indexed plugin object classes", "get");
    services.configurationsResource(ConfigurationRestService::retrieveReindexPluginObjectClasses)
      .whenComplete((reindexPluginObjects, throwable) -> {
        if (throwable == null) {
          for (ReindexPluginObject object : reindexPluginObjects.getPluginsObjects()) {
            objectBox.addItem(object.getSimpleName(), object.getName());
          }

          objectBox.addItem(RodaConstants.PLUGIN_SELECT_ALL_RODA_OBJECTS, RodaConstants.PLUGIN_SELECT_ALL_RODA_OBJECTS);
          objectBox.setSelectedIndex(objectBox.getItemCount() - 1);
          value = objectBox.getSelectedValue();
        }
      });

    objectBox.addChangeHandler(event -> value = objectBox.getSelectedValue());

    objectBox.setTitle(OBJECT_BOX);
    layout.add(parameterName);
    layout.add(objectBox);
    addHelp();
  }

  private void createDropdownLayout() {
    Label parameterName = new Label(parameter.getName());
    parameterName.addStyleName(FORM_LABEL);
    final ListBox dropdown = new ListBox();
    dropdown.addStyleName(FORM_SELECTBOX);

    Services services = new Services("Retrieve dropdown plugin parameter items", "get");
    services
      .configurationsResource(
        s -> s.retrieveDropdownPluginItems(parameter.getId(), LocaleInfo.getCurrentLocale().getLocaleName()))
      .whenComplete((dropdownPluginParameterItems, throwable) -> {
        if (throwable == null) {

          Set<DropdownPluginParameterItem> items = new TreeSet<>(
            (i1, i2) -> i1.getLabel().compareToIgnoreCase(i2.getLabel()));
          items.addAll(dropdownPluginParameterItems.getItems());

          for (DropdownPluginParameterItem item : items) {
            dropdown.addItem(item.getLabel(), item.getId());
          }

          value = dropdown.getSelectedValue();
        }
      });

    dropdown.addChangeHandler(event -> value = dropdown.getSelectedValue());

    dropdown.setTitle(OBJECT_BOX);
    layout.add(parameterName);
    layout.add(dropdown);
    addHelp();
  }

  private void createConversionProfileLayout(FlowPanel result, String repOrDip, String pluginId) {
    Set<ConversionProfile> treeSet = new HashSet<>();
    Label parameterName = new Label(messages.conversionProfileTitle());
    parameterName.addStyleName(FORM_LABEL);
    final Label descriptionHelper = new Label();
    final ListBox dropdown = new ListBox();
    dropdown.addStyleName(FORM_SELECTBOX);

    Services services = new Services("Retrieve conversion profiles", "get");
    services
      .configurationsResource(s -> s.retrieveConversionProfiles(pluginId,
        ConversionProfileOutcomeType.valueOf(repOrDip), LocaleInfo.getCurrentLocale().getLocaleName()))
      .whenComplete((conversionProfiles, throwable) -> {
        if (throwable == null) {
          treeSet.addAll(conversionProfiles.getConversionProfileSet());

          for (ConversionProfile item : treeSet) {
            dropdown.addItem(item.getTitle(), item.getProfile());
            descriptionHelper.setText(item.getDescription());
            descriptionHelper.addStyleName(FORM_HELP);
          }

          profile = dropdown.getSelectedValue();
          for (ConversionProfile conversionProfile : treeSet) {
            if (conversionProfile.getProfile().equals(profile)) {
              descriptionHelper.setText(conversionProfile.getDescription());
              break;
            }
          }

          dropdown.addChangeHandler(event -> {
            profile = dropdown.getSelectedValue();
            for (ConversionProfile conversionProfile : treeSet) {
              if (conversionProfile.getProfile().equals(profile)) {
                descriptionHelper.setText(conversionProfile.getDescription());
                break;
              }
            }
          });

          dropdown.setTitle(OBJECT_BOX);
          result.add(parameterName);
          result.add(dropdown);
          result.add(descriptionHelper);
        }
      });
  }

  private void createPluginObjectFieldsLayout(final String className) {
    List<String> defaultValues = Arrays.asList(parameter.getDefaultValue().split(","));

    Services services = new Services("Retrieves object class fields from configurations", "get");
    services.configurationsResource(s -> s.retrieveObjectClassFields(LocaleInfo.getCurrentLocale().getLocaleName()))
      .whenComplete((objectClassFields, throwable) -> {
        if (throwable == null) {
          final List<String> selectedFields = new ArrayList<>();

          // 1. Create the wrapper for your checkboxes
          FlowPanel group = new FlowPanel();
          // Apply the custom flexbox container class to get the 0.4rem gap
          group.addStyleName("checkbox-group");

          Label parameterName = new Label(parameter.getName());
          parameterName.addStyleName(FORM_LABEL);
          layout.add(parameterName);

          for (String field : objectClassFields.getObjectClassFields().get(className)) {
            final String classField = className
              + RepresentationInformationUtils.REPRESENTATION_INFORMATION_FILTER_SEPARATOR + field;
            CheckBox box = new CheckBox(objectClassFields.getTranslations().get(classField));

            if (defaultValues.contains(field)) {
              box.setValue(true);
              selectedFields.add(field);
            }

            group.add(box);
            // 2. Apply your custom material icon checkbox style
            box.addStyleName("my-custom-checkbox");

            box.addValueChangeHandler(event -> {
              if (Boolean.TRUE.equals(event.getValue())) {
                selectedFields.add(field);
              } else {
                selectedFields.remove(field);
              }
              value = StringUtils.join(selectedFields, ",");
            });
          }

          if (File.class.getSimpleName().equals(className)) {
            CheckBox box = new CheckBox(messages.atLeastOneOfAbove());
            group.add(box);
            // 2. Apply your custom material icon checkbox style here as well
            box.addStyleName("my-custom-checkbox");

            if (defaultValues.contains(RodaConstants.ONE_OF_FORMAT_FIELDS)) {
              box.setValue(true);
              selectedFields.add(RodaConstants.ONE_OF_FORMAT_FIELDS);
            }

            box.addValueChangeHandler(event -> {
              if (Boolean.TRUE.equals(event.getValue())) {
                selectedFields.add(RodaConstants.ONE_OF_FORMAT_FIELDS);
              } else {
                selectedFields.remove(RodaConstants.ONE_OF_FORMAT_FIELDS);
              }
              value = StringUtils.join(selectedFields, ",");
            });
          }

          value = StringUtils.join(selectedFields, ",");
          layout.add(group);
          addHelp();
        }
      });
  }

  private void createPermissionTypesLayout() {
    final List<String> selectedTypes = new ArrayList<>();
    Label parameterName = new Label(parameter.getName());
    parameterName.addStyleName(FORM_LABEL);
    layout.add(parameterName);

    // Create a wrapper for the checkboxes
    FlowPanel checkboxContainer = new FlowPanel();
    checkboxContainer.addStyleName("checkbox-group");

    for (PermissionType permissionType : PermissionType.values()) {
      CheckBox box = new CheckBox(permissionType.toString());
      box.setValue(true);
      selectedTypes.add(permissionType.toString());

      // Add the checkbox to the inner container instead of the main layout
      checkboxContainer.add(box);
      box.addStyleName("my-custom-checkbox");

      box.addValueChangeHandler(event -> {
        if (Boolean.TRUE.equals(event.getValue())) {
          selectedTypes.add(permissionType.toString());
        } else {
          selectedTypes.remove(permissionType.toString());
        }
        value = StringUtils.join(selectedTypes, ",");
      });
    }

    // Add the grouped container to the main layout
    layout.add(checkboxContainer);

    value = StringUtils.join(selectedTypes, ",");
    addHelp();
  }

  private void createPluginSipToAipLayout() {
    List<PluginType> plugins = List.of(PluginType.SIP_TO_AIP);
    Services services = new Services("Retrieve plugin information", "get");
    services
      .configurationsResource(s -> s.retrievePluginsInfo(plugins, false, LocaleInfo.getCurrentLocale().getLocaleName()))
      .whenComplete((pluginInfoList, throwable) -> {
        if (throwable == null) {
          Label parameterName = new Label(parameter.getName());
          parameterName.addStyleName(FORM_LABEL);
          layout.add(parameterName);

          FlowPanel radioGroup = new FlowPanel();
          // Use a custom class for the outer group
          radioGroup.addStyleName("radio-group-container");
          PluginUtils.sortByName(pluginInfoList.getPluginInfoList());

          for (final PluginInfo pluginInfo : pluginInfoList.getPluginInfoList()) {
            if (pluginInfo != null) {
              // Create a wrapper for this specific option
              FlowPanel optionWrapper = new FlowPanel();
              optionWrapper.addStyleName("radio-option-wrapper");

              RadioButton pRadio = new RadioButton(parameter.getName(),
                messages.pluginLabelWithVersion(pluginInfo.getName(), pluginInfo.getVersion()));

              if (pluginInfo.getId().equals(parameter.getDefaultValue())) {
                pRadio.setValue(true);
                value = pluginInfo.getId();
              }

              Label pHelp = new Label(pluginInfo.getDescription());
              pRadio.setTitle("radio button");

              pRadio.addStyleName("my-custom-radio"); // Consistent with your checkboxes
              pHelp.addStyleName("radio-option-help");

              // Add elements to the wrapper, NOT directly to the group
              optionWrapper.add(pRadio);
              optionWrapper.add(pHelp);

              // Add the wrapper to the main group
              radioGroup.add(optionWrapper);

              pRadio.addValueChangeHandler(event -> {
                if (Boolean.TRUE.equals(event.getValue())) {
                  value = pluginInfo.getId();
                }
              });
            }
          }

          layout.add(radioGroup);
          addHelp();
        }
      });
  }

  private void createIntegerLayout() {
    Label parameterName = new Label(parameter.getName());
    parameterName.addStyleName(FORM_LABEL);
    IntegerBox parameterBox = new IntegerBox();
    if (parameter.getDefaultValue() != null) {
      parameterBox.setText(parameter.getDefaultValue());
      value = parameter.getDefaultValue();
    }

    parameterBox.setTitle("parameter box");
    parameterBox.addStyleName(FORM_TEXTBOX);
    parameterBox.addChangeHandler(event -> value = ((IntegerBox) event.getSource()).getValue().toString());

    layout.add(parameterName);
    layout.add(parameterBox);
    addHelp();
  }

  private void createStringLayout(PluginParameter parameter) {
    ValueChangeHandler<String> changeHandler = event -> value = event.getValue();

    createTextBoxLayout(layout, parameter.getName(), parameter.getDefaultValue(), parameter.getDescription(),
      parameter.isReadonly(), changeHandler);
  }

  private void createTextBoxLayout(FlowPanel targetPanel, String name, String defaultValue, String description,
    boolean isReadOnly, ValueChangeHandler<String> valueChangeEvent) {

    if (defaultValue != null) {
      value = defaultValue;
    }

    Label parameterName = new Label(name);
    parameterName.addStyleName(FORM_LABEL);
    targetPanel.add(parameterName);

    if (isReadOnly) {
      InlineHTML html = new InlineHTML();
      html.setText(value);
      targetPanel.add(html);
    } else {
      TextBox parameterBox = new TextBox();
      parameterBox.addStyleName(FORM_TEXTBOX);
      parameterBox.setText(value);
      parameterBox.setTitle("parameter box");
      parameterBox.addValueChangeHandler(valueChangeEvent);
      targetPanel.add(parameterBox);
    }

    addHelp(targetPanel, description);
  }

  private void createBooleanLayout(PluginParameter parameter) {
    ValueChangeHandler<Boolean> changeHandler = event -> value = Boolean
      .toString(Boolean.TRUE.equals(event.getValue()));
    createBooleanLayout(layout, parameter.getName(), parameter.getDefaultValue(), parameter.getDescription(),
      parameter.isReadonly(), changeHandler);
  }

  private void createBooleanLayout(FlowPanel panel, String name, String defaultValue, String description,
    boolean isReadOnly, ValueChangeHandler<Boolean> valueChangeHandler) {

    CheckBox checkBox = new CheckBox(name);
    checkBox.setValue("true".equals(defaultValue));
    value = Boolean.toString("true".equals(defaultValue));
    checkBox.setEnabled(!isReadOnly);
    checkBox.getElement().setTitle("checkbox");

    checkBox.addStyleName("my-custom-checkbox");
    checkBox.addValueChangeHandler(valueChangeHandler);

    panel.add(checkBox);
    addHelp(panel, description);
  }

  private void addHelp() {
    addHelp(layout, parameter.getDescription());
  }

  private void addHelp(Panel panel, String description) {
    if (StringUtils.isNotBlank(description)) {
      Label pHelp = new Label(description);
      panel.add(pHelp);
      pHelp.addStyleName(FORM_HELP);
    }
  }

  public String getProfile() {
    return profile;
  }

  public String getValue() {
    return value;
  }

  public boolean isConversionPanel() {
    return conversionPanel;
  }

  public RepresentationParameter getRepresentationParameter() {
    return representationParameter;
  }

  public DisseminationParameter getDisseminationParameter() {
    return disseminationParameter;
  }

  public String getAipTitle() {
    return aipTitle;
  }

  public PluginParameter getParameter() {
    return parameter;
  }

  public FlowPanel getLayout() {
    return layout;
  }

}
