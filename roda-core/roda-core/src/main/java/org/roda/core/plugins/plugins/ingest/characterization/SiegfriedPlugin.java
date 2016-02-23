/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.common.PremisUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IdUtils;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.jobs.Attribute;
import org.roda.core.data.v2.jobs.JobReport.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.ReportItem;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiegfriedPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SiegfriedPlugin.class);

  public static final String OTHER_METADATA_TYPE = "Siegfried";
  public static final String FILE_SUFFIX = ".json";

  private boolean createsPluginEvent = true;

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Siegfried file format identification";
  }

  @Override
  public String getDescription() {
    return "Identify file format and update preservation metadata";
  }

  @Override
  public String getVersion() {
    return SiegfriedPluginUtils.getVersion();
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    // updates the flag responsible to allow plugin event creation
    if (getParameterValues().containsKey("createsPluginEvent")) {
      createsPluginEvent = Boolean.parseBoolean(getParameterValues().get("createsPluginEvent"));
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    Report report = PluginHelper.createPluginReport(this);
    PluginState state;
    

    for (AIP aip : list) {
      ReportItem reportItem = PluginHelper.createPluginReportItem(this, aip.getId(), null);

      LOGGER.debug("Processing AIP {}", aip.getId());
      List<String> sources = new ArrayList<String>();
      try {
        List<String> siegfriedOutputs = new ArrayList<String>();
        for (Representation representation : aip.getRepresentations()) {
          LOGGER.debug("Processing representation {} of AIP {}", representation.getId(), aip.getId());
          siegfriedOutputs
            .add(SiegfriedPluginUtils.runSiegfriedOnRepresentation(index, model, storage, aip, representation));

          CloseableIterable<File> files = model.listFilesUnder(aip.getId(), representation.getId(), true);
          Iterator<File> it = files.iterator();
          while (it.hasNext()) {
            File f = it.next();
            if (!f.isDirectory()) {
              sources
                .add(IdUtils.getLinkingIdentifierId(f.getAipId(), f.getRepresentationId(), f.getPath(), f.getId()));
            }
          }

        }
        boolean inotify = false;
        if (createsPluginEvent) {
          try {
            PluginHelper.createPluginEvent(this,aip.getId(), null, null, null, model,
              RodaConstants.PRESERVATION_EVENT_TYPE_FORMAT_IDENTIFICATION,
              "The files of the representation were successfully identified.", sources, null,
              PluginState.SUCCESS.name(), StringUtils.join(siegfriedOutputs), "", inotify);
          } catch (ValidationException e) {
            LOGGER.error("Error creating Premis event: " + e.getMessage(), e);
          }
        }
        model.notifyAIPUpdated(aip.getId());

        state = PluginState.SUCCESS;
        reportItem.addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()));

      } catch (PluginException | NotFoundException | GenericException | RequestNotValidException
        | AuthorizationDeniedException | AlreadyExistsException e) {
        LOGGER.error("Error running SIEGFRIED " + aip.getId() + ": " + e.getMessage(), e);

        state = PluginState.FAILURE;
        reportItem.addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()))
          .addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS,
            "Error running SIEGFRIED " + aip.getId() + ": " + e.getMessage()));
      }

      report.addItem(reportItem);

      // TODO Remove try catch... only added to run siegfried plugin via sh
      // script
      try {
        if (createsPluginEvent) {
          PluginHelper.updateJobReport(model, index, this, reportItem, state, aip.getId());
        }
      } catch (Throwable t) {

      }
    }
    return report;
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    SiegfriedPlugin siegfriedPlugin = new SiegfriedPlugin();
    try {
      siegfriedPlugin.init();
    } catch (PluginException e) {
      LOGGER.error("Error doing " + SiegfriedPlugin.class.getName() + "init", e);
    }
    return siegfriedPlugin;
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

}
