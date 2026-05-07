package org.roda.wui.client.browse.tabs;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginInfoList;
import org.roda.wui.client.process.tabs.MarketplaceTab;
import org.roda.wui.client.process.tabs.PreservationActionsTab;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class JobPluginActionsTab extends Tabs {

  private PreservationActionsTab installedPluginsTab;
  private Consumer<PluginInfo> onPluginSelected;
  private Runnable onStartJobCallback;
  private Runnable onCancelJobCallback;

  public void setOnPluginSelected(Consumer<PluginInfo> onPluginSelected) {
    this.onPluginSelected = onPluginSelected;
    if (installedPluginsTab != null) {
      installedPluginsTab.setOnPluginSelected(onPluginSelected);
    }
  }

  public void setOnStartJobCallback(Runnable onStartJobCallback) {
    this.onStartJobCallback = onStartJobCallback;
    if (installedPluginsTab != null) {
      installedPluginsTab.setOnStartJobCallback(onStartJobCallback);
    }
  }

  public void setOnCancelJobCallback(Runnable runnable) {
    this.onCancelJobCallback = runnable;
    if (installedPluginsTab != null) {
      installedPluginsTab.setOnCancelJobCallback(onCancelJobCallback);
    }
  }

  public void init(PluginInfoList pluginInfoList, boolean showMarketplaceTab) {
    init(pluginInfoList, null, showMarketplaceTab);
  }

  public void init(PluginInfoList pluginInfoList, String selectedClass, boolean showMarketplaceTab) {
    installedPluginsTab = new PreservationActionsTab(pluginInfoList, selectedClass);

    if (onPluginSelected != null) {
      installedPluginsTab.setOnPluginSelected(onPluginSelected);
    }

    if (onStartJobCallback != null) {
      installedPluginsTab.setOnStartJobCallback(onStartJobCallback);
    }

    if (onCancelJobCallback != null) {
      installedPluginsTab.setOnCancelJobCallback(onCancelJobCallback);
    }

    createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.availableActionsTabLabel()), new TabContentBuilder() {
      @Override
      public Widget buildTabWidget() {
        return installedPluginsTab;
      }
    });

    if (showMarketplaceTab) {
      createAndAddTab(SafeHtmlUtils.fromSafeConstant(messages.marketTabLabel()), new TabContentBuilder() {
        @Override
        public Widget buildTabWidget() {
          return new MarketplaceTab(pluginInfoList);
        }
      });
    }
  }

  public PluginInfo getSelectedPlugin() {
    return installedPluginsTab != null ? installedPluginsTab.getSelectedPlugin() : null;
  }

  public Map<String, String> getPluginParameters() {
    return installedPluginsTab != null ? installedPluginsTab.getPluginParameters() : null;
  }
}
