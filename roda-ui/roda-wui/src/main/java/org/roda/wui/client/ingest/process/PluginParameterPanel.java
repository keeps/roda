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
import java.util.List;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.RepresentationInformationUtils;
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
import org.roda.core.data.v2.risks.Risk.SEVERITY_LEVEL;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.bundle.RepresentationInformationFilterBundle;
import org.roda.wui.client.common.IncrementalAssociativeList;
import org.roda.wui.client.common.dialogs.SelectAipDialog;
import org.roda.wui.client.common.utils.PluginUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
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
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final ClientLogger LOGGER = new ClientLogger(PluginParameterPanel.class.getName());
  private static final String ADD_TYPE = "#__ADDNEW__#";

  private final PluginParameter parameter;
  private final FlowPanel layout;

  private String value;

  public PluginParameterPanel(PluginParameter parameter) {
    super();
    this.parameter = parameter;

    layout = new FlowPanel();
    initWidget(layout);

    updateLayout();

    layout.addStyleName("plugin-options-parameter");
  }

  private void updateLayout() {
    if (PluginParameterType.BOOLEAN.equals(parameter.getType())) {
      createBooleanLayout();
    } else if (PluginParameterType.STRING.equals(parameter.getType())) {
      createStringLayout();
    } else if (PluginParameterType.PLUGIN_SIP_TO_AIP.equals(parameter.getType())) {
      createPluginSipToAipLayout();
    } else if (PluginParameterType.AIP_ID.equals(parameter.getType())) {
      createSelectAipLayout();
    } else if (PluginParameterType.RISK_ID.equals(parameter.getType())) {
      createSelectRiskLayout();
    } else if (PluginParameterType.SEVERITY.equals(parameter.getType())) {
      createSelectSeverityLayout();
    } else if (PluginParameterType.REPRESENTATION_TYPE.equals(parameter.getType())) {
      createRepresentationTypeLayout();
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
    } else {
      LOGGER
        .warn("Unsupported plugin parameter type: " + parameter.getType() + ". Reverting to default parameter editor.");
      createStringLayout();
    }
  }

  private void createRepresentationTypeLayout() {
    Label parameterName = new Label(parameter.getName());

    final ListBox selectBox = new ListBox();
    selectBox.addStyleName("form-selectbox");
    selectBox.addStyleName("form-textbox-small");

    final TextBox newTypeBox = new TextBox();
    final Label newTypeLabel = new Label(messages.entityTypeNewLabel() + ": ");

    newTypeBox.getElement().setPropertyString("placeholder", messages.entityTypeNewLabel());
    newTypeBox.addStyleName("form-textbox wui-dialog-message plugin-representation-type-box");
    newTypeLabel.addStyleName("plugin-representation-type-label");

    newTypeLabel.setVisible(false);
    newTypeBox.setVisible(false);

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

          if (!result.getFirst()) {
            selectBox.addItem(messages.entityTypeAddNew(), ADD_TYPE);
          }

          selectBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
              value = selectBox.getSelectedValue();
              newTypeLabel.setVisible(value.equals(ADD_TYPE));
              newTypeBox.setVisible(value.equals(ADD_TYPE));
            }
          });

          newTypeBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
              value = newTypeBox.getText();
            }
          });
        }
      });

    value = selectBox.getSelectedValue();
    selectBox.setTitle("representation type box");

    layout.add(parameterName);
    layout.add(selectBox);
    layout.add(newTypeLabel);
    layout.add(newTypeBox);
    addHelp();
  }

  private void createSelectSeverityLayout() {
    Label parameterName = new Label(parameter.getName());
    final ListBox severityBox = new ListBox();
    severityBox.addStyleName("form-selectbox");
    severityBox.addStyleName("form-textbox-small");

    for (SEVERITY_LEVEL severity : SEVERITY_LEVEL.values()) {
      severityBox.addItem(messages.severityLevel(severity), severity.toString());
    }

    value = severityBox.getSelectedValue();

    severityBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        value = severityBox.getSelectedValue();
      }
    });

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
    if (parameter.getRenderingHings() != null
      && parameter.getRenderingHings() instanceof AipIdPluginParameterRenderingHints) {
      renderingHints = (AipIdPluginParameterRenderingHints) parameter.getRenderingHings();
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
    ClickHandler editClickHandler = new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
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
          selectAipDialog.addValueChangeHandler(new ValueChangeHandler<IndexedAIP>() {

            @Override
            public void onValueChange(ValueChangeEvent<IndexedAIP> event) {
              IndexedAIP aip = event.getValue();

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
            }
          });
        }
      }
    };
    if (finalRenderingHints == null || !finalRenderingHints.isDisableSelection()) {
      ClickHandler removeClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          editPanel.setVisible(false);
          button.setVisible(true);

          value = null;
        }
      };
      removeButton.addClickHandler(removeClickHandler);
    }
    button.addClickHandler(editClickHandler);

    layout.add(parameterName);
    layout.add(button);
    layout.add(editPanel);

    parameterName.addStyleName("form-label");
    aipPanel.addStyleName("itemPanel");
    button.addStyleName("form-button btn btn-play");
    buttonsPanel.addStyleName("itemButtonsPanel");
    editButton.addStyleName("toolbarLink toolbarLinkSmall");
    removeButton.addStyleName("toolbarLink toolbarLinkSmall");
  }

  private void createSelectRodaObjectLayout() {
    Label parameterName = new Label(parameter.getName());
    final ListBox objectBox = new ListBox();
    objectBox.addStyleName("form-selectbox");
    objectBox.addStyleName("form-textbox-small");

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

    objectBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        value = objectBox.getSelectedValue();
      }
    });

    objectBox.setTitle("object box");
    layout.add(parameterName);
    layout.add(objectBox);
    addHelp();
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
            box.addStyleName("form-radiobutton");

            box.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
              @Override
              public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                  selectedFields.add(field);
                } else {
                  selectedFields.remove(field);
                }
                value = StringUtils.join(selectedFields, ",");
              }
            });
          }

          if (File.class.getSimpleName().equals(className)) {
            CheckBox box = new CheckBox(messages.atLeastOneOfAbove());
            group.add(box);
            box.addStyleName("form-radiobutton");

            if (defaultValues.contains(RodaConstants.ONE_OF_FORMAT_FIELDS)) {
              box.setValue(true);
              selectedFields.add(RodaConstants.ONE_OF_FORMAT_FIELDS);
            }

            box.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
              @Override
              public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                  selectedFields.add(RodaConstants.ONE_OF_FORMAT_FIELDS);
                } else {
                  selectedFields.remove(RodaConstants.ONE_OF_FORMAT_FIELDS);
                }
                value = StringUtils.join(selectedFields, ",");
              }
            });
          }

          value = StringUtils.join(selectedFields, ",");
          layout.add(group);
          group.addStyleName("form-radiogroup");
          parameterName.addStyleName("form-label");
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
      box.addStyleName("form-radiobutton");

      box.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          if (event.getValue()) {
            selectedTypes.add(permissionType.toString());
          } else {
            selectedTypes.remove(permissionType.toString());
          }
          value = StringUtils.join(selectedTypes, ",");
        }
      });
    }

    value = StringUtils.join(selectedTypes, ",");
    layout.add(group);
    group.addStyleName("form-radiogroup");
    parameterName.addStyleName("form-label");
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

            pRadio.addStyleName("form-radiobutton");
            pHelp.addStyleName("form-help");

            pRadio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

              @Override
              public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                  value = pluginInfo.getId();
                }
              }
            });
          }
        }

        layout.add(radioGroup);

        radioGroup.addStyleName("form-radiogroup");
        parameterName.addStyleName("form-label");
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

    parameterName.addStyleName("form-label");
    parameterBox.addStyleName("form-textbox");

    // binding change
    parameterBox.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        value = ((IntegerBox) event.getSource()).getValue().toString();
      }
    });
  }

  private void createStringLayout() {
    Label parameterName = new Label(parameter.getName());
    TextBox parameterBox = new TextBox();
    if (parameter.getDefaultValue() != null) {
      parameterBox.setText(parameter.getDefaultValue());
      value = parameter.getDefaultValue();
    }

    parameterBox.setEnabled(!parameter.isReadonly());
    parameterBox.setTitle("parameter box");
    layout.add(parameterName);
    layout.add(parameterBox);
    addHelp();

    parameterName.addStyleName("form-label");
    parameterBox.addStyleName("form-textbox");

    // binding change
    parameterBox.addValueChangeHandler(new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        value = event.getValue();
      }
    });
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
    checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        value = event.getValue() ? "true" : "false";
      }
    });
  }

  private void addHelp() {
    String pDescription = parameter.getDescription();
    if (pDescription != null && pDescription.length() > 0) {
      Label pHelp = new Label(pDescription);
      layout.add(pHelp);
      pHelp.addStyleName("form-help");
    }
  }

  public String getValue() {
    return value;
  }

  public PluginParameter getParameter() {
    return parameter;
  }

}
