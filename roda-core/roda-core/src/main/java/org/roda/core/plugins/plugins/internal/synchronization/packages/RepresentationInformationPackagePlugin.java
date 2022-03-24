package org.roda.core.plugins.plugins.internal.synchronization.packages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class RepresentationInformationPackagePlugin extends RodaEntityPackagesPlugin<RepresentationInformation> {
  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getName() {
    return "RepresentationInformationPackage";
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new RepresentationInformationPackagePlugin();
  }

  @Override
  protected String getEntity() {
    return "Representation Information";
  }

  @Override
  protected Class<RepresentationInformation> getEntityClass() {
    return RepresentationInformation.class;
  }

  @Override
  protected List<String> retrieveList(IndexService index) throws RequestNotValidException, GenericException {
    final List<String> representationInformationList = new ArrayList<>();
    final Filter filter = new Filter();
    
    return null;
  }

  @Override
  protected void createPackage(IndexService index, ModelService model, List<String> list) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, NotFoundException, AlreadyExistsException, IOException {

  }
}
