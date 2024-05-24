package org.roda.wui.client.common.utils;

import java.util.Map;

import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobPriority;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JobUtils {

  private JobUtils() {
    // empty constructor
  }

  public static Job createJob(String jobName, JobPriority priority, JobParallelism parallelism,
    SelectedItems<?> selected, String id, Map<String, String> value) {
    SelectedItems<?> selectedItems = selected;
    if (selectedItems instanceof SelectedItemsList) {
      SelectedItemsList<?> items = (SelectedItemsList<?>) selectedItems;

      if (items.getIds().isEmpty()) {
        selectedItems = getAllItemsByClass(null);
      }
    }

    Job job = new Job();
    job.setName(jobName);
    job.setSourceObjects(selectedItems);
    job.setPlugin(id);
    job.setPluginParameters(value);
    job.setPriority(priority);
    job.setParallelism(parallelism);

    return job;
  }

  public static <T extends IsIndexed> Job createJob(String jobName, JobPriority priority, JobParallelism parallelism,
    SelectedItems<T> selected, String id, Map<String, String> value, String selectedClass) {
    SelectedItems<T> selectedItems = selected;

    if (selectedItems instanceof SelectedItemsList) {
      SelectedItemsList<T> items = (SelectedItemsList<T>) selectedItems;

      if (items.getIds().isEmpty()) {
        selectedItems = getAllItemsByClass(selectedClass);
      }
    }

    Job job = new Job();
    job.setName(jobName);
    job.setSourceObjects(selectedItems);
    job.setPlugin(id);
    job.setPluginParameters(value);
    job.setPriority(priority);
    job.setParallelism(parallelism);

    return job;
  }

  private static <T extends IsIndexed> SelectedItems<T> getAllItemsByClass(String selectedClass) {
    if (selectedClass == null || Void.class.getName().equals(selectedClass)) {
      return new SelectedItemsNone<>();
    } else {
      return new SelectedItemsAll<>(selectedClass);
    }
  }
}
