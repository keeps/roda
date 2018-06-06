/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.risks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.LockingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RiskIncidenceRemoverPlugin<T extends IsRODAObject> extends AbstractPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RiskIncidenceRemoverPlugin.class);

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Removes old risk incidences";
  }

  @Override
  public String getDescription() {
    return "Removes all risk incidences related to removed AIPs or risks";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    LOGGER.debug("Removing old risk incidences");
    Report pluginReport = PluginHelper.initPluginReport(this);

    try {
      JobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, liteList.size());
      PluginHelper.updateJobInformationAsync(this, jobPluginInfo);

      Job job = PluginHelper.getJob(this, model);
      List<T> list = PluginHelper.transformLitesIntoObjects(model, this, pluginReport, jobPluginInfo, liteList, job,
        false);

      try {
        Filter filter = new Filter();

        for (T object : list) {
          if (object instanceof AIP) {
            AIP aip = (AIP) object;
            filter.add(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_AIP_ID, aip.getId()));
          } else if (object instanceof Risk) {
            Risk risk = (Risk) object;
            filter.add(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_RISK_ID, risk.getId()));
          } else if (object instanceof Representation) {
            Representation representation = (Representation) object;
            filter
              .add(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID, representation.getId()));
          } else if (object instanceof File) {
            File file = (File) object;
            filter.add(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_FILE_ID, file.getId()));
          }
        }

        IndexResult<RiskIncidence> incidences = index.find(RiskIncidence.class, filter, Sorter.NONE,
          new Sublist(0, list.size()), Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.RISK_INCIDENCE_ID));

        for (RiskIncidence incidence : incidences.getResults()) {
          model.deleteRiskIncidence(incidence.getId(), false);
        }

        jobPluginInfo.incrementObjectsProcessedWithSuccess(list.size());
        jobPluginInfo.finalizeInfo();
        PluginHelper.updateJobInformationAsync(this, jobPluginInfo);
      } catch (GenericException | NotFoundException | AuthorizationDeniedException | RequestNotValidException e) {
        LOGGER.error("Could not delete risk incidence", e);
        jobPluginInfo.incrementObjectsProcessedWithFailure(list.size());
      }
    } catch (JobException | AuthorizationDeniedException | NotFoundException | GenericException
      | RequestNotValidException | LockingException e) {
      throw new PluginException("A job exception has occurred", e);
    }

    LOGGER.debug("Done removing old risk incidences");
    return pluginReport;
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
  public Plugin<T> cloneMe() {
    return new RiskIncidenceRemoverPlugin<>();
  }

  @Override
  public PluginType getType() {
    return PluginType.INTERNAL;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.DELETION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Removed all risk incidences";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Removed all risk incidences successfully";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Remove of risk incidences failed";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public List<Class<T>> getObjectClasses() {
    List<Class<? extends IsRODAObject>> list = new ArrayList<>();
    list.add(AIP.class);
    list.add(Risk.class);
    list.add(Representation.class);
    list.add(File.class);
    return (List) list;
  }
}
