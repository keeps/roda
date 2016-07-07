package org.roda.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.plugins.Plugin;

public final class TestsHelper {
  private TestsHelper() {

  }

  public static <T extends IsRODAObject, T1 extends Plugin<T>> Job executeJob(Class<T1> plugin, PluginType pluginType,
    SelectedItems<T> selectedItems)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return executeJob(plugin, new HashMap<>(), pluginType, selectedItems);
  }

  public static <T extends IsRODAObject, T1 extends Plugin<T>> Job executeJob(Class<T1> plugin,
    Map<String, String> pluginParameters, PluginType pluginType, SelectedItems<T> selectedItems)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    Job job = new Job();
    job.setId(UUID.randomUUID().toString());
    job.setName(job.getId());
    job.setPlugin(plugin.getName());
    job.setPluginParameters(pluginParameters);
    job.setPluginType(pluginType);
    job.setSourceObjects(selectedItems);
    job.setUsername("admin");
    try {
      RodaCoreFactory.getModelService().createJob(job);
      RodaCoreFactory.getPluginOrchestrator().executeJob(job, false);
    } catch (Exception e) {
      Assert.fail("Unable to execute job in test mode: [" + e.getClass().getName() + "] " + e.getMessage());
    }

    Job jobUpdated = RodaCoreFactory.getModelService().retrieveJob(job.getId());
    Assert.assertThat(jobUpdated.getState(), Is.is(JOB_STATE.COMPLETED));
    return jobUpdated;

  }
}
