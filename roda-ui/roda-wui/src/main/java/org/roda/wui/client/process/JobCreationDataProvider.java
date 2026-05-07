package org.roda.wui.client.process;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobPriority;
import org.roda.core.data.v2.jobs.PluginInfo;

import java.util.Map;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public interface JobCreationDataProvider {
  String getJobName();

  Map<String, String> getPluginParameters();

  SelectedItems<? extends IsRODAObject> getSelectedItems();

  PluginInfo getSelectedPlugin();

  JobPriority getSelectedPriority();

  JobParallelism getSelectedParallelism();
}
