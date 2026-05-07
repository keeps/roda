package org.roda.wui.client.process;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginInfoList;
import org.roda.wui.client.browse.tabs.JobPluginActionsTab;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class CreateJobPluginSelection extends Composite {

  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  JobPluginActionsTab tabs;

  public CreateJobPluginSelection(PluginInfoList pluginInfoList) {
    initWidget(uiBinder.createAndBindUi(this));

    tabs.init(pluginInfoList, true);
  }

  public CreateJobPluginSelection(PluginInfoList pluginInfoList, String selectedClass, boolean showMarketplaceTab) {
    initWidget(uiBinder.createAndBindUi(this));

    tabs.init(pluginInfoList, selectedClass, showMarketplaceTab);
  }

  public PluginInfo getSelectedPlugin() {
    return tabs.getSelectedPlugin();
  }

  public Map<String, String> getPluginParameters() {
    return tabs.getPluginParameters();
  }

  public void setOnPluginSelected(Consumer<PluginInfo> handler) {
    tabs.setOnPluginSelected(handler);
  }

  public void setOnStartJobCallback(Runnable callback) {
    tabs.setOnStartJobCallback(callback);
  }

  public void setOnCancelJobCallback(Runnable callback) {
    tabs.setOnCancelJobCallback(callback);
  }

  public interface MyUiBinder extends UiBinder<Widget, CreateJobPluginSelection> {
  }
}
