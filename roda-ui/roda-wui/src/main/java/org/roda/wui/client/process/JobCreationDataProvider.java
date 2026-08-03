/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
