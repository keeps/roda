package org.roda.core.plugins.plugins.internal.synchronization.bundle;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class CreateRiskIncidencePackagePlugin extends CreateRodaEntityPackagePlugin<RiskIncidence> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateRiskIncidencePackagePlugin.class);

  @Override
  public String getName() {
    return "Create Risk Incidence Bundle";
  }

  @Override
  public String getVersionImpl() {
    return "1.0.0";
  }

  @Override
  protected String getEntity() {
    return "risk_incidence";
  }

  @Override
  protected String getEntityStoragePath() {
    return "risk-incidence";
  }

  @Override
  protected void createBundle(IndexService index, ModelService model, Report pluginReport, JobPluginInfo jobPluginInfo,
    Job job) {
    pluginReport.setPluginState(PluginState.SUCCESS);

    SelectedItems<?> sourceObjects = job.getSourceObjects();

    if (sourceObjects instanceof SelectedItemsFilter) {
      Filter filter = ((SelectedItemsFilter) sourceObjects).getFilter();
      try {
        int counter = index.count(RiskIncidence.class, filter).intValue();

        jobPluginInfo.setSourceObjectsCount(counter);
        ArrayList<String> idList = new ArrayList<>();

        IterableIndexResult<RiskIncidence> incidences = index.findAll(RiskIncidence.class, filter,
          Collections.emptyList());
        for (RiskIncidence incidence : incidences) {
          Report reportItem = PluginHelper.initPluginReportItem(this, incidence.getId(), RiskIncidence.class);
          try {
            createRiskIncidenceBundle(model, incidence);
            idList.add(incidence.getId());
            jobPluginInfo.incrementObjectsProcessedWithSuccess();
          } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
            LOGGER.error("Error on create bundle for Risk Incidence {}", incidence.getId());
            jobPluginInfo.incrementObjectsProcessedWithFailure();
            reportItem
              .addPluginDetails("Failed to create bundle for " + incidence.getClass() + " " + incidence.getId() + "\n");
            reportItem.addPluginDetails(e.getMessage());
            pluginReport.addReport(reportItem.setPluginState(PluginState.FAILURE));
            PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
          }
        }
        updateEntityPackageState(RiskIncidence.class, idList);
      } catch (RODAException e) {
        LOGGER.error("Error on retrieve indexes of a RODA entity", e);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      } catch (IOException e) {
        LOGGER.error("Error on update entity package state file", e);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      }
    }
  }

  public void createRiskIncidenceBundle(ModelService model, RiskIncidence incidence) throws RequestNotValidException,
    NotFoundException, AuthorizationDeniedException, GenericException, AlreadyExistsException {

    StorageService storage = model.getStorage();
    StoragePath riskIncidenceStoragePath = ModelUtils.getRiskIncidenceContainerPath();
    String incidenceFile = incidence.getId() + RodaConstants.RISK_INCIDENCE_FILE_EXTENSION;

    Path destinationPath = getDestinationPath().resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_RISK_INCIDENCE);

    Path incidencePath = destinationPath.resolve(incidenceFile);

    storage.copy(storage, riskIncidenceStoragePath, incidencePath, incidenceFile);

  }

  @Override
  public Plugin<Void> cloneMe() {
    return new CreateRiskIncidencePackagePlugin();
  }

}
