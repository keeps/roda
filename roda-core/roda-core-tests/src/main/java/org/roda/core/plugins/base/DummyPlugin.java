/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin that does nothing (including not throwing any type of exception) and
 * therefore it must be possible to execute a Job with this plugin and ending up
 * with a Job in the state completed
 *
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class DummyPlugin extends AbstractPlugin<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DummyPlugin.class);

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
  public Plugin<Void> cloneMe() {
    return new DummyPlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public void init() {
    LOGGER.info("Doing nothing during init");
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model) {
    LOGGER.info("Doing nothing during beforeAllExecute");
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model,
    List<LiteOptionalWithCause> list) {
    LOGGER.info("Doing nothing during execute");
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model) {
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
