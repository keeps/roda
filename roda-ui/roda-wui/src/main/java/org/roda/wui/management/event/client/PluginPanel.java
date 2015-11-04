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
package org.roda.wui.management.event.client;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.PluginInfo;
import org.roda.core.data.PluginParameter;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.EventManagementConstants;
import config.i18n.client.EventManagementMessages;

/**
 * @author Luis Faria
 * 
 */
public class PluginPanel {

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static EventManagementConstants constants = (EventManagementConstants) GWT
    .create(EventManagementConstants.class);

  private static EventManagementMessages messages = (EventManagementMessages) GWT.create(EventManagementMessages.class);

  private PluginInfo[] plugins;
  private PluginInfo selectedPlugin;

  private final VerticalPanel layout;
  private final ListBox pluginList;
  private final Label pluginDescription;
  private final Label pluginParametersTitle;
  private final FlexTable pluginParameters;

  private boolean initialized;
  private List<Command> initListeners;

  /**
   * Create a new plugin panel
   */
  public PluginPanel() {
    plugins = null;
    selectedPlugin = null;

    layout = new VerticalPanel();
    pluginList = new ListBox();
    pluginDescription = new Label();
    pluginParametersTitle = new Label(constants.pluginParameters());
    pluginParameters = new FlexTable();

    layout.add(pluginList);
    layout.add(pluginDescription);
    layout.add(pluginParametersTitle);
    layout.add(pluginParameters);

    pluginList.addChangeListener(new ChangeListener() {

      public void onChange(Widget sender) {
        String pluginId = pluginList.getValue(pluginList.getSelectedIndex());
        for (PluginInfo plugin : plugins) {
          if (plugin.getId().equals(pluginId)) {
            setSelected(plugin);
            break;
          }
        }
      }

    });

    initialized = false;
    initListeners = new ArrayList<Command>();

    init();

    layout.addStyleName("wui-task-plugin");
    pluginList.addStyleName("task-plugin-list");
    pluginDescription.addStyleName("task-plugin-description");
    pluginParametersTitle.addStyleName("task-plugin-parameters-title");
    pluginParameters.addStyleName("task-plugin-parameters");
  }

  protected void init() {
    EventManagementService.Util.getInstance().getPlugins(new AsyncCallback<PluginInfo[]>() {

      public void onFailure(Throwable caught) {
        logger.error("Error getting plugin list", caught);
      }

      public void onSuccess(PluginInfo[] plugins) {
        setPlugins(plugins);
        initialized = true;
        for (Command command : initListeners) {
          command.execute();
        }
        initListeners.clear();
      }

    });
  }

  protected void afterInit(Command command) {
    if (initialized) {
      command.execute();
    } else {
      initListeners.add(command);
    }
  }

  protected void setPlugins(PluginInfo[] plugins) {
    this.plugins = plugins;
    updatePluginList();
  }

  private void updatePluginList() {
    pluginList.clear();
    for (PluginInfo plugin : plugins) {
      pluginList.addItem(createPluginListItem(plugin), plugin.getId());
    }

  }

  private String createPluginListItem(PluginInfo plugin) {
    return messages.pluginLabel(plugin.getName(), plugin.getVersion());
  }

  /**
   * Get currently selected plugin
   * 
   * @return currently selected plugin
   */
  public PluginInfo getSelected() {
    return selectedPlugin;
  }

  /**
   * Set selected plugin. If plugin does not exist in plugin list, selected will
   * be set to null.
   * 
   * @param plugin
   */
  public void setSelected(final PluginInfo plugin) {
    afterInit(new Command() {

      public void execute() {
        logger.debug("Selecting plugin " + plugin);
        selectedPlugin = plugin;

        boolean foundIt = false;
        for (int i = 0; i < pluginList.getItemCount() && !foundIt; i++) {
          if (pluginList.getValue(i).equals(plugin.getId())) {
            foundIt = true;
            pluginList.setSelectedIndex(i);
            break;
          }
        }

        if (!foundIt) {
          // TODO add to plugin list and select
        }

        pluginDescription.setText(plugin.getDescription());
        updateParameters();
      }

    });
  }

  /**
   * Set selected plugin
   * 
   * @param index
   *          the index of the plugin
   */
  public void setSelected(final int index) {
    afterInit(new Command() {
      public void execute() {
        setSelected(plugins[index]);
      }
    });
  }

  protected void updateParameters() {
    pluginParameters.clear();
    List<PluginParameter> parameters = selectedPlugin.getParameters();
    int row = 0;
    for (final PluginParameter parameter : parameters) {
      if (!parameter.isReadonly()) {
        Label name = new Label(parameter.getDescription() + (parameter.isMandatory() ? "*:" : ":"));
        Widget value = null;

        if (parameter.getType().equals(PluginParameter.TYPE_STRING) && parameter.getPossibleValues().length > 0) {
          final ListBox listbox = new ListBox();
          listbox.setVisibleItemCount(1);
          for (String possibleValue : parameter.getPossibleValues()) {
            listbox.addItem(possibleValue);
          }

          Tools.setSelectedValue(listbox, parameter.getValue());

          listbox.addChangeListener(new ChangeListener() {

            public void onChange(Widget sender) {
              parameter.setValue(listbox.getValue(listbox.getSelectedIndex()));
            }

          });

          value = listbox;

        } else if (parameter.getType().equals(PluginParameter.TYPE_STRING)) {
          final TextBox textBox = new TextBox();

          textBox.setText(parameter.getValue());

          textBox.addChangeListener(new ChangeListener() {

            public void onChange(Widget sender) {
              parameter.setValue(textBox.getText());
            }

          });

          value = textBox;

        } else if (parameter.getType().equals(PluginParameter.TYPE_PASSWORD)) {
          final PasswordTextBox passwordBox = new PasswordTextBox();

          passwordBox.setText(parameter.getValue());

          passwordBox.addChangeListener(new ChangeListener() {

            public void onChange(Widget sender) {
              parameter.setValue(passwordBox.getText());
            }

          });

          value = passwordBox;

        } else if (parameter.getType().equals(PluginParameter.TYPE_BOOLEAN)) {
          final CheckBox checkbox = new CheckBox();

          checkbox.setChecked(Boolean.parseBoolean(parameter.getValue()));

          checkbox.addClickListener(new ClickListener() {

            public void onClick(Widget sender) {
              parameter.setValue(Boolean.toString(checkbox.isChecked()));

            }

          });

          value = checkbox;

        }

        pluginParameters.setWidget(row, 0, name);
        pluginParameters.setWidget(row, 1, value);

        name.addStyleName("plugin-parameter-name");
        value.addStyleName("plugin-parameter-value");
        pluginParameters.getCellFormatter().setWidth(row, 1, "100%");

        row++;
      }

    }

  }

  /**
   * Get widget
   * 
   * @return the widget
   */
  public Widget getWidget() {
    return layout;
  }

  /**
   * Check if all mandatory parameters are filled and valid
   * 
   * @return true if valid
   */
  public boolean isValid() {
    boolean valid = true;
    for (PluginParameter parameter : selectedPlugin.getParameters()) {
      if (parameter.isMandatory() && (parameter.getValue() == null || parameter.getValue().length() == 0)) {
        valid = false;
        break;
      }
    }
    return valid;
  }

}
