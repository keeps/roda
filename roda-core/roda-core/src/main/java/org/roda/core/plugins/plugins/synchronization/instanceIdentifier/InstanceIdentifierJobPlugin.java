package org.roda.core.plugins.plugins.synchronization.instanceIdentifier;

import org.roda.core.data.v2.jobs.Job;
import org.roda.core.plugins.Plugin;

import java.util.Arrays;
import java.util.List;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class InstanceIdentifierJobPlugin extends InstanceIdentifierRodaEntityPlugin<Job> {
  @Override
  public String getName() {
    return "Job instance identifier";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Plugin<Job> cloneMe() {
    return new InstanceIdentifierJobPlugin();
  }

  @Override
  public List<Class<Job>> getObjectClasses() {
    return Arrays.asList(Job.class);
  }
}
