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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.core.data.v2.common.ConversionProfile;
import org.roda.core.data.v2.common.Pair;
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
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.SeverityLevel;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.bundle.RepresentationInformationFilterBundle;
import org.roda.wui.client.common.IncrementalAssociativeList;
import org.roda.wui.client.common.dialogs.SelectAipDialog;
import org.roda.wui.client.common.utils.PluginUtils;
import org.roda.wui.client.ingest.process.model.DisseminationParameter;
import org.roda.wui.client.ingest.process.model.RepresentationParameter;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
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
  public static final String FORM_SELECTBOX = "form-selectbox";
  public static final String FORM_TEXTBOX_SMALL = "form-textbox-small";
  public static final String FORM_RADIOBUTTON = "form-radiobutton";
  public static final String FORM_RADIOGROUP = "form-radiogroup";
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

  public PluginParameterPanel(PluginParameter parameter) {
    super();
    this.parameter = parameter;

    this.pluginId = null;

    layout = new FlowPanel();

    initWidget(layout);

    updateLayout();

    layout.addStyleName("plugin-options-parameter");
  }

  public PluginParameterPanel(PluginParameter parameter, String pluginId) {
    super();
    this.parameter = parameter;

    this.pluginId = pluginId;

    layout = new FlowPanel();

    initWidget(layout);

    updateLayout();

    layout.addStyleName("plugin-options-parameter");
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
    final ListBox dropdown = new ListBox();
    dropdown.addStyleName(FORM_SELECTBOX);
    dropdown.addStyleName(FORM_TEXTBOX_SMALL);

    dropdown.addItem("Representation", RodaConstants.PLUGIN_PARAMS_CONVERSION_REPRESENTATION);
    dropdown.addItem("Dissemination", RodaConstants.PLUGIN_PARAMS_CONVERSION_DISSEMINATION);

    value = dropdown.getSelectedValue();
    FlowPanel innerPanel = new FlowPanel();
    dropdown.addChangeHandler(event -> {
      value = dropdown.getSelectedValue();
      innerPanel.clear();
      FlowPanel profiles = createConversionProfileLayout(value, pluginId);
      innerPanel.add(profiles);

      if (value.equals(RodaConstants.PLUGIN_PARAMS_CONVERSION_REPRESENTATION)) {
        // Add fields
        ValueChangeHandler<String> typeChanged = typeChangedEvent -> representationParameter
          .setValue(typeChangedEvent.getValue());

        innerPanel.add(createRepresentationType(messages.representationTypeTitle(),
          messages.representationTypeDescription(), typeChanged));
        ValueChangeHandler<Boolean> preservationStatusChanged = preservationStatusChangedEvent -> representationParameter
          .setMarkAsPreservation(preservationStatusChangedEvent.getValue());

        innerPanel.add(createBooleanLayout(messages.changeRepresentationStatusToPreservationTitle(), Boolean.toString(true),
          messages.changeRepresentationStatusToPreservationDescription(), false, preservationStatusChanged));

        value = RodaConstants.PLUGIN_PARAMS_CONVERSION_REPRESENTATION;
      } else {
        // Add fields
        ValueChangeHandler<String> titleChanged = titleChangedEvent -> disseminationParameter
          .setTitle(titleChangedEvent.getValue());
        ValueChangeHandler<String> descriptionChanged = descriptionChangedEvent -> disseminationParameter
          .setDescription(descriptionChangedEvent.getValue());

        innerPanel.add(createTextBoxLayout(messages.disseminationTitle(), disseminationParameter.getTitle(),
          messages.disseminationTitleDescription(), false, titleChanged));
        innerPanel.add(createTextBoxLayout(messages.disseminationDescriptionTitle(), disseminationParameter.getDescription(),
          messages.disseminationDescriptionDescription(), false, descriptionChanged));

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

  private FlowPanel createRepresentationType(String name, String description,
    ValueChangeHandler<String> changeHandler) {
    FlowPanel panel = new FlowPanel();
    Label parameterName = new Label(name);

    final ListBox selectBox = new ListBox();
    selectBox.addStyleName(FORM_SELECTBOX);
    selectBox.addStyleName(FORM_TEXTBOX_SMALL);
    selectBox.setTitle("representation type box");

    BrowserService.Util.getInstance().retrieveRepresentationTypeOptions(LocaleInfo.getCurrentLocale().getLocaleName(),
      new AsyncCallback<Pair<Boolean, List<String>>>() {

        @Override
        public void onFailure(Throwable caught) {
          selectBox.setVisible(false);
        }

        @Override
        public void onSuccess(Pair<Boolean, List<String>> result) {
          for (String option : result.getSecond()) {
            selectBox.addItem(option);
          }

          if (Boolean.FALSE.equals(result.getFirst())) {
            selectBox.addItem(messages.entityTypeAddNew(), ADD_TYPE);
          }
          selectBox.setSelectedIndex(0);
          value = selectBox.getSelectedValue();
          representationParameter.setValue(selectBox.getSelectedValue());
        }
      });

    final TextBox newTypeBox = new TextBox();
    final Label newTypeLabel = new Label(messages.entityTypeNewLabel() + ": ");
    newTypeBox.getElement().setPropertyString("placeholder", messages.entityTypeNewLabel());
    newTypeBox.addStyleName("form-textbox wui-dialog-message plugin-representation-type-box");
    newTypeLabel.addStyleName("plugin-representation-type-label");
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
    addHelp(panel, description);
    panel.add(selectBox);
    panel.add(newTypeLabel);
    panel.add(newTypeBox);

    return panel;
  }

  private void createRepresentationTypeLayout(PluginParameter parameter) {
    ValueChangeHandler<String> handler = event -> value = event.getValue();

    FlowPanel panel = createRepresentationType(parameter.getName(), parameter.getDescription(), handler);
    layout.add(panel);
  }

  private void createSelectSeverityLayout() {
    Label parameterName = new Label(parameter.getName());
    final ListBox severityBox = new ListBox();
    severityBox.addStyleName(FORM_SELECTBOX);
    severityBox.addStyleName(FORM_TEXTBOX_SMALL);

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

    parameterName.addStyleName(FORM_LABEL);
    aipPanel.addStyleName("itemPanel");
    button.addStyleName("form-button btn btn-play");
    buttonsPanel.addStyleName("itemButtonsPanel");
    editButton.addStyleName("toolbarLink toolbarLinkSmall");
    removeButton.addStyleName("toolbarLink toolbarLinkSmall");
  }

  private void createSelectRodaObjectLayout() {
    Label parameterName = new Label(parameter.getName());
    final ListBox objectBox = new ListBox();
    objectBox.addStyleName(FORM_SELECTBOX);
    objectBox.addStyleName(FORM_TEXTBOX_SMALL);

    BrowserService.Util.getInstance()
      .retrieveReindexPluginObjectClasses(new AsyncCallback<Set<Pair<String, String>>>() {

        @Override
        public void onFailure(Throwable caught) {
          // do nothing
        }

        @Override
        public void onSuccess(Set<Pair<String, String>> result) {
          for (Pair<String, String> classNames : result) {
            objectBox.addItem(classNames.getFirst(), classNames.getSecond());
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
    final ListBox dropdown = new ListBox();
    dropdown.addStyleName(FORM_SELECTBOX);
    dropdown.addStyleName(FORM_TEXTBOX_SMALL);

    BrowserService.Util.getInstance().retrieveDropdownPluginItems(parameter.getId(),
      LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<Set<Pair<String, String>>>() {

        @Override
        public void onFailure(Throwable caught) {
          // do nothing
        }

        @Override
        public void onSuccess(Set<Pair<String, String>> result) {
          Set<Pair<String, String>> treeSet = new TreeSet<>(
            (p1, p2) -> p1.getFirst().compareToIgnoreCase(p2.getFirst()));

          treeSet.addAll(result);
          for (Pair<String, String> item : treeSet) {
            dropdown.addItem(item.getFirst(), item.getSecond());
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

  private FlowPanel createConversionProfileLayout(String repOrDip, String pluginId) {
    Set<ConversionProfile> treeSet = new HashSet<>();
    Label parameterName = new Label(messages.conversionProfileTitle());
    final Label description = new Label();
    final ListBox dropdown = new ListBox();
    dropdown.addStyleName(FORM_SELECTBOX);
    dropdown.addStyleName(FORM_TEXTBOX_SMALL);

    FlowPanel result = new FlowPanel();
    FlowPanel panel = new FlowPanel();
    FlowPanel descriptionPanel = new FlowPanel();

    BrowserService.Util.getInstance().retrieveConversionProfilePluginItems(pluginId, repOrDip,
      LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<Set<ConversionProfile>>() {

        @Override
        public void onFailure(Throwable caught) {
          // do nothing
        }

        @Override
        public void onSuccess(Set<ConversionProfile> result) {
          treeSet.addAll(result);
          for (ConversionProfile item : treeSet) {
            dropdown.addItem(item.getTitle(), item.getProfile());
            description.setText(item.getDescription());
            description.addStyleName(FORM_HELP);
          }

          profile = dropdown.getSelectedValue();
          for (ConversionProfile conversionProfile : treeSet) {
            if (conversionProfile.getProfile().equals(profile)) {
              description.setText(conversionProfile.getDescription());
              break;
            }
          }
        }
      });

    dropdown.addChangeHandler(event -> {
      profile = dropdown.getSelectedValue();
      for (ConversionProfile conversionProfile : treeSet) {
        if (conversionProfile.getProfile().equals(profile)) {
          description.setText(conversionProfile.getDescription());
          break;
        }
      }
    });

    panel.add(dropdown);
    descriptionPanel.add(description);
    panel.addStyleName("conversion-profile");

    dropdown.setTitle(OBJECT_BOX);
    result.add(parameterName);
    addHelp(result, messages.conversionProfileDescription());
    result.add(panel);
    result.add(descriptionPanel);
    return result;
  }

  private void createPluginObjectFieldsLayout(final String className) {
    List<String> defaultValues = Arrays.asList(parameter.getDefaultValue().split(","));

    BrowserService.Util.getInstance().retrieveObjectClassFields(LocaleInfo.getCurrentLocale().getLocaleName(),
      new AsyncCallback<RepresentationInformationFilterBundle>() {

        @Override
        public void onFailure(Throwable caught) {
          // do nothing
        }

        @Override
        public void onSuccess(RepresentationInformationFilterBundle bundle) {
          final List<String> selectedFields = new ArrayList<>();
          FlowPanel group = new FlowPanel();
          Label parameterName = new Label(parameter.getName());
          layout.add(parameterName);
          addHelp();

          for (String field : bundle.getObjectClassFields().get(className)) {
            final String classField = className
              + RepresentationInformationUtils.REPRESENTATION_INFORMATION_FILTER_SEPARATOR + field;
            CheckBox box = new CheckBox(bundle.getTranslations().get(classField));

            if (defaultValues.contains(field)) {
              box.setValue(true);
              selectedFields.add(field);
            }

            group.add(box);
            box.addStyleName(FORM_RADIOBUTTON);

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
            box.addStyleName(FORM_RADIOBUTTON);

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
          group.addStyleName(FORM_RADIOGROUP);
          parameterName.addStyleName(FORM_LABEL);
        }
      });
  }

  private void createPermissionTypesLayout() {
    final List<String> selectedTypes = new ArrayList<>();
    FlowPanel group = new FlowPanel();
    Label parameterName = new Label(parameter.getName());
    layout.add(parameterName);
    addHelp();

    for (PermissionType permissionType : PermissionType.values()) {
      CheckBox box = new CheckBox(permissionType.toString());
      box.setValue(true);
      selectedTypes.add(permissionType.toString());

      group.add(box);
      box.addStyleName(FORM_RADIOBUTTON);

      box.addValueChangeHandler(event -> {
        if (Boolean.TRUE.equals(event.getValue())) {
          selectedTypes.add(permissionType.toString());
        } else {
          selectedTypes.remove(permissionType.toString());
        }
        value = StringUtils.join(selectedTypes, ",");
      });
    }

    value = StringUtils.join(selectedTypes, ",");
    layout.add(group);
    group.addStyleName(FORM_RADIOGROUP);
    parameterName.addStyleName(FORM_LABEL);
  }

  private void createPluginSipToAipLayout() {
    List<PluginType> plugins = Arrays.asList(PluginType.SIP_TO_AIP);
    BrowserService.Util.getInstance().retrievePluginsInfo(plugins, new AsyncCallback<List<PluginInfo>>() {

      @Override
      public void onFailure(Throwable caught) {
        // do nothing
      }

      @Override
      public void onSuccess(List<PluginInfo> pluginsInfo) {
        Label parameterName = new Label(parameter.getName());
        layout.add(parameterName);
        addHelp();

        FlowPanel radioGroup = new FlowPanel();
        PluginUtils.sortByName(pluginsInfo);

        for (final PluginInfo pluginInfo : pluginsInfo) {
          if (pluginInfo != null) {
            RadioButton pRadio = new RadioButton(parameter.getName(),
              messages.pluginLabelWithVersion(pluginInfo.getName(), pluginInfo.getVersion()));

            if (pluginInfo.getId().equals(parameter.getDefaultValue())) {
              pRadio.setValue(true);
              value = pluginInfo.getId();
            }

            Label pHelp = new Label(pluginInfo.getDescription());
            pRadio.setTitle("radio button");

            radioGroup.add(pRadio);
            radioGroup.add(pHelp);

            pRadio.addStyleName(FORM_RADIOBUTTON);
            pHelp.addStyleName(FORM_HELP);

            pRadio.addValueChangeHandler(event -> {
              if (Boolean.TRUE.equals(event.getValue())) {
                value = pluginInfo.getId();
              }
            });
          }
        }

        layout.add(radioGroup);

        radioGroup.addStyleName(FORM_RADIOGROUP);
        parameterName.addStyleName(FORM_LABEL);
      }
    });
  }

  private void createIntegerLayout() {
    Label parameterName = new Label(parameter.getName());
    IntegerBox parameterBox = new IntegerBox();
    if (parameter.getDefaultValue() != null) {
      parameterBox.setText(parameter.getDefaultValue());
      value = parameter.getDefaultValue();
    }

    parameterBox.setTitle("parameter box");
    layout.add(parameterName);
    layout.add(parameterBox);
    addHelp();

    parameterName.addStyleName(FORM_LABEL);
    parameterBox.addStyleName(FORM_TEXTBOX);

    // binding change
    parameterBox.addChangeHandler(event -> value = ((IntegerBox) event.getSource()).getValue().toString());
  }

  private void createStringLayout(PluginParameter parameter) {
    ValueChangeHandler<String> changeHandler = event -> value = event.getValue();

    FlowPanel textBoxLayout = createTextBoxLayout(parameter.getName(), parameter.getDefaultValue(),
      parameter.getDescription(), parameter.isReadonly(), changeHandler);
    layout.add(textBoxLayout);
  }

  private FlowPanel createTextBoxLayout(String name, String defaultValue, String description, boolean isReadOnly,
    ValueChangeHandler<String> valueChangeEvent) {

    FlowPanel panel = new FlowPanel();

    Label parameterName = new Label(name);
    TextBox parameterBox = new TextBox();

    if (defaultValue != null) {
      parameterBox.setText(defaultValue);
      value = defaultValue;
    }

    parameterName.addStyleName(FORM_LABEL);
    parameterBox.addStyleName(FORM_TEXTBOX);

    parameterBox.setEnabled(!isReadOnly);
    parameterBox.setTitle("parameter box");

    panel.add(parameterName);
    addHelp(panel, description);
    panel.add(parameterBox);

    parameterBox.addValueChangeHandler(valueChangeEvent);

    return panel;
  }

  private void createBooleanLayout(PluginParameter parameter) {
    ValueChangeHandler<Boolean> changeHandler = event -> value = Boolean.TRUE.equals(event.getValue()) ? "true"
      : "false";
    layout.add(createBooleanLayout(parameter.getName(), parameter.getDefaultValue(), parameter.getDescription(),
      parameter.isReadonly(), changeHandler));
  }

  private FlowPanel createBooleanLayout(String name, String defaultValue, String description, boolean isReadOnly,
    ValueChangeHandler<Boolean> valueChangeHandler) {
    FlowPanel panel = new FlowPanel();

    CheckBox checkBox = new CheckBox(name);
    checkBox.setValue("true".equals(defaultValue));
    value = "true".equals(defaultValue) ? "true" : "false";
    checkBox.setEnabled(!isReadOnly);
    checkBox.getElement().setTitle("checkbox");

    checkBox.addStyleName("form-checkbox");
    checkBox.addValueChangeHandler(valueChangeHandler);

    panel.add(checkBox);
    addHelp(panel, description);

    return panel;
  }

  private void createBooleanLayout() {
    CheckBox checkBox = new CheckBox(parameter.getName());
    checkBox.setValue("true".equals(parameter.getDefaultValue()));
    value = "true".equals(parameter.getDefaultValue()) ? "true" : "false";
    checkBox.setEnabled(!parameter.isReadonly());
    checkBox.getElement().setTitle("checkbox");

    layout.add(checkBox);
    addHelp();

    checkBox.addStyleName("form-checkbox");
    checkBox.addValueChangeHandler(event -> value = Boolean.TRUE.equals(event.getValue()) ? "true" : "false");
  }

  private void addHelp() {
    addHelp(layout, parameter.getDescription());
  }

  private void addHelp(FlowPanel panel, String description) {
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
