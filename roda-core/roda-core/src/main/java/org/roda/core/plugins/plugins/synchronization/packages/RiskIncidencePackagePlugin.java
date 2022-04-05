package org.roda.core.plugins.plugins.synchronization.packages;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.*;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.storage.StorageService;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class RiskIncidencePackagePlugin extends RodaEntityPackagesPlugin<RiskIncidence> {
  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getName() {
    return "RiskIncidencePackagePlugin";
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new RiskIncidencePackagePlugin();
  }

  @Override
  protected String getEntity() {
    return "risk_incidence";
  }

  @Override
  protected Class<RiskIncidence> getEntityClass() {
    return RiskIncidence.class;
  }

  @Override
  protected List<String> retrieveList(IndexService index) throws RequestNotValidException, GenericException {
    ArrayList<String> riskList = new ArrayList<>();
    Filter filter = new Filter();
    if (fromDate != null) {
      filter.add(new DateIntervalFilterParameter(RodaConstants.RISK_INCIDENCE_UPDATED_ON,
        RodaConstants.RISK_INCIDENCE_UPDATED_ON, fromDate, toDate));
    }
    IterableIndexResult<RiskIncidence> incidences = index.findAll(RiskIncidence.class, filter, Collections.emptyList());
    for (RiskIncidence incidence : incidences) {
      riskList.add(incidence.getId());
    }
    return riskList;
  }

  @Override
  protected void createPackage(IndexService index, ModelService model, List<String> list) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, NotFoundException, AlreadyExistsException, IOException {
    for (String incidenceId : list) {
      createRiskIncidenceBundle(model, incidenceId);
    }
  }

  public void createRiskIncidenceBundle(ModelService model, String incidenceId) throws RequestNotValidException,
    NotFoundException, AuthorizationDeniedException, GenericException, AlreadyExistsException {

    StorageService storage = model.getStorage();
    StoragePath riskIncidenceStoragePath = ModelUtils.getRiskIncidenceContainerPath();
    String incidenceFile = incidenceId + RodaConstants.RISK_INCIDENCE_FILE_EXTENSION;

    Path destinationPath = bundlePath.resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_RISK_INCIDENCE);

    Path incidencePath = destinationPath.resolve(incidenceFile);

    storage.copy(storage, riskIncidenceStoragePath, incidencePath, incidenceFile);

  }
}
