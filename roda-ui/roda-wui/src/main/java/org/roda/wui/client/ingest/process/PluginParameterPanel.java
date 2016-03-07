/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.ingest.process;

import java.util.List;

import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.wui.client.common.SelectAipDialog;
import org.roda.wui.client.common.utils.PluginUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;

import config.i18n.client.BrowseMessages;

public class PluginParameterPanel extends Composite {
  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private final PluginParameter parameter;
  private final List<PluginInfo> sipToAipPlugins;
  private final FlowPanel layout;

  private String value;

  public PluginParameterPanel(PluginParameter parameter, List<PluginInfo> sipToAipPlugins) {
    super();
    this.parameter = parameter;
    this.sipToAipPlugins = sipToAipPlugins;

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
    } else {
      logger
        .warn("Unsupported plugin parameter type: " + parameter.getType() + ". Reverting to default parameter editor.");
      createStringLayout();
    }
  }
  
  private void createSelectAipLayout() {
    Label parameterName = new Label(parameter.getName());
    final FlowPanel aipPanel = new FlowPanel();
    Button button = new Button(messages.pluginAipIdButton());
    
    aipPanel.setVisible(false);
    
    button.addClickHandler(new ClickHandler() {
      
      @Override
      public void onClick(ClickEvent event) {
       SelectAipDialog selectAipDialog = new SelectAipDialog(parameter.getName());
       selectAipDialog.showAndCenter();
       selectAipDialog.addValueChangeHandler(new ValueChangeHandler<IndexedAIP>() {

         @Override
         public void onValueChange(ValueChangeEvent<IndexedAIP> event) {
           IndexedAIP aip = event.getValue();
           
           SimplePanel itemIcon = new SimplePanel();
           Label itemTitle = new Label();
           
           HTMLPanel itemIconHtmlPanel = DescriptionLevelUtils.getElementLevelIconHTMLPanel(aip.getLevel());
           itemIcon.setWidget(itemIconHtmlPanel);
           itemIcon.addStyleName("itemIcon");
           itemTitle.setText(aip.getTitle() != null ? aip.getTitle() : aip.getId());
           itemTitle.addStyleName("itemText");
           
           aipPanel.clear();
           aipPanel.add(itemIcon);
           aipPanel.add(itemTitle);
           aipPanel.setVisible(true);
           
           value = aip.getId();
         }
       });
      }
    });
    
    layout.add(parameterName);
    layout.add(aipPanel);
    layout.add(button);
    
    parameterName.addStyleName("form-label");
    aipPanel.addStyleName("itemPanel");
    button.addStyleName("form-button btn btn-plus");
  }

  private void createPluginSipToAipLayout() {
    Label parameterName = new Label(parameter.getName());

    layout.add(parameterName);
    addHelp();

    FlowPanel radioGroup = new FlowPanel();

    PluginUtils.sortByName(sipToAipPlugins);

    for (final PluginInfo pluginInfo : sipToAipPlugins) {
      if (pluginInfo != null) {
        RadioButton pRadio = new RadioButton(parameter.getName(),
          messages.pluginLabel(pluginInfo.getName(), pluginInfo.getVersion()));

        if (pluginInfo.getId().equals(parameter.getDefaultValue())) {
          pRadio.setValue(true);
          value = pluginInfo.getId();
        }

        Label pHelp = new Label(pluginInfo.getDescription());

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
      } else {
        GWT.log("Got a null plugin");
      }
    }

    layout.add(radioGroup);

    radioGroup.addStyleName("form-radiogroup");
    parameterName.addStyleName("form-label");
  }

  private void createStringLayout() {
    Label parameterName = new Label(parameter.getName());
    TextBox parameterBox = new TextBox();
    if (parameter.getDefaultValue() != null) {
      parameterBox.setText(parameter.getDefaultValue());
      value = parameter.getDefaultValue();
    }

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
