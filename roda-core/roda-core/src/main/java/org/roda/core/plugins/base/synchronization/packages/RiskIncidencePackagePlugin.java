/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.synchronization.packages;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;

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
    Optional<LiteRODAObject> incidenceLite = LiteRODAObjectFactory.get(RiskIncidence.class, incidenceId);
    if (incidenceLite.isEmpty()) {
      throw new RequestNotValidException("Could not get LITE for incidence " + incidenceId);
    }

    String incidenceFile = incidenceId + RodaConstants.RISK_INCIDENCE_FILE_EXTENSION;

    Path destinationPath = workingDirPath.resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_RISK_INCIDENCE);

    Path incidencePath = destinationPath.resolve(incidenceFile);

    model.exportToPath(incidenceLite.get(), incidencePath, false);
  }
}
