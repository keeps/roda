/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class PluginThatStopsItself extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PluginThatStopsItself.class);

  @Override
  public String getName() {
    return getClass().getName();
  }

  @Override
  public String getDescription() {
    return getClass().getName();
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return null;
  }

  @Override
  public String getPreservationEventDescription() {
    return null;
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return null;
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return null;
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public List<String> getCategories() {
    return Collections.emptyList();
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new PluginThatStopsItself();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public void init() throws PluginException {
    LOGGER.info("Doing nothing during init");
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    LOGGER.info("Doing nothing during beforeAllExecute");
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> list) throws PluginException {
    long sleepTime = 1000;
    LOGGER.info("Going to request to stop job executing me (after sleeping for {} miliseconds)", sleepTime);
    try {
      Thread.sleep(sleepTime);
      Job job = PluginHelper.getJob(this, RodaCoreFactory.getModelService());
      RodaCoreFactory.getPluginOrchestrator().stopJob(job);
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException
      | InterruptedException e) {
      LOGGER.error("Error while trying to stop job", e);
    }
    LOGGER.info("Finished sending stop job request (after sleeping for {} miliseconds)", sleepTime);
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    LOGGER.info("Doing nothing during afterAllExecute");
    return null;
  }

  @Override
  public void shutdown() {
    LOGGER.info("Doing nothing during shutdown");
  }

  @Override
  public String getVersionImpl() {
    return null;
  }

}
