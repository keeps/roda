package org.roda.core.plugins.plugins.internal.synchronization.packages;

import java.io.IOException;
import java.util.List;

import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class RiskPackagePlugin extends RodaEntityPackagesPlugin<IndexedRisk> {
  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getName() {
    return "RiskPackage";
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new RiskPackagePlugin();
  }

  @Override
  protected String getEntity() {
    return "Risk";
  }

  @Override
  protected Class<IndexedRisk> getEntityClass() {
    return IndexedRisk.class;
  }

  @Override
  protected List<String> retrieveList(IndexService index) throws RequestNotValidException, GenericException {
    return null;
  }

  @Override
  protected void createPackage(IndexService index, ModelService model, List<String> list) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, NotFoundException, AlreadyExistsException, IOException {

  }
}
