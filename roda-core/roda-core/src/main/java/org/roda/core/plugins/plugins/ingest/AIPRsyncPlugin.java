/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.roda.core.util.CommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AIPRsyncPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AIPRsyncPlugin.class);

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  public static String getStaticName() {
    return "Rsync AIPs";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Replicates AIPs on a different RODA of a different machine.";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    List<PreservationMetadata> pms = new ArrayList<PreservationMetadata>();
    PluginState state = PluginState.SUCCESS;
    String commandResult = null;

    try {
      commandResult = AIPRsyncPluginUtils.executeRsyncAIP(list);

    } catch (UnsupportedOperationException | CommandException | IOException e) {
      LOGGER.error("Could not run rsync command on AIPs: ", e);
      state = PluginState.FAILURE;
    }

    for (AIP aip : list) {
      pms.add(createEvent(commandResult, state, aip, model, index));
    }

    if (state.equals(PluginState.SUCCESS)) {
      try {
        AIPRsyncPluginUtils.executeRsyncEvents(pms);
      } catch (UnsupportedOperationException | CommandException | IOException e) {
        LOGGER.error("Could not run rsync command on AIPs ", e);
      }
    }

    return PluginHelper.initPluginReport(this);
  }

  private PreservationMetadata createEvent(String outcomeDetail, PluginState state, AIP aip, ModelService model,
    IndexService index) throws PluginException {

    try {
      boolean notify = true;
      return PluginHelper.createPluginEvent(this, aip.getId(), model, index, state, outcomeDetail, notify);
    } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
      | AuthorizationDeniedException | AlreadyExistsException e) {
      LOGGER.error("Error creating event: " + e.getMessage(), e);
      return null;
    }
  }

  @Override
  public Report beforeBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new AIPRsyncPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.REPLICATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_REPLICATION);
  }

}
