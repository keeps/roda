package org.roda.core.plugins.plugins.internal.synchronization.packages;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
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

  public static String getStaticName() {
    return "RiskIncidencePackagePlugin";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
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
  protected List<IterableIndexResult> retrieveList(IndexService index)
    throws RequestNotValidException, GenericException {
    Filter filter = new Filter();
    if (fromDate != null) {
      filter.add(new DateIntervalFilterParameter(RodaConstants.RISK_INCIDENCE_UPDATED_ON,
        RodaConstants.RISK_INCIDENCE_UPDATED_ON, fromDate, toDate));
    }
    return Arrays.asList(index.findAll(RiskIncidence.class, filter, Collections.emptyList()));
  }

  @Override
  protected void createPackage(IndexService index, ModelService model, IterableIndexResult objectList)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    AlreadyExistsException {
    for (Object object : objectList) {
      if (object instanceof RiskIncidence) {
        RiskIncidence riskIncidence = model.retrieveRiskIncidence(((RiskIncidence) object).getId());
        createRiskIncidenceBundle(model, riskIncidence.getId());
      }
    }
  }

  private void createRiskIncidenceBundle(ModelService model, String incidenceId)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException {
    StorageService storage = model.getStorage();
    StoragePath riskIncidenceStoragePath = ModelUtils.getRiskIncidenceContainerPath();
    String incidenceFile = incidenceId + RodaConstants.RISK_INCIDENCE_FILE_EXTENSION;

    Path destinationPath = workingDirPath.resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_RISK_INCIDENCE);

    Path incidencePath = destinationPath.resolve(incidenceFile);

    storage.copy(storage, riskIncidenceStoragePath, incidencePath, incidenceFile);
  }
}
