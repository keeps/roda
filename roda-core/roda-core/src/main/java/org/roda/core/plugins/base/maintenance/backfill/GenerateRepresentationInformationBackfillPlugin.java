package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class GenerateRepresentationInformationBackfillPlugin
  extends GenerateRODAEntityBackfillPlugin<RepresentationInformation> {

  @Override
  protected <I extends IsIndexed> Class<I> getIndexClass() {
    return (Class<I>) RepresentationInformation.class;
  }

  @Override
  protected RepresentationInformation retrieveModelObject(ModelService model, String id)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    return model.retrieveRepresentationInformation(id);
  }

  @Override
  public Plugin<RepresentationInformation> cloneMe() {
    return new GenerateRepresentationInformationBackfillPlugin();
  }

  @Override
  public List<Class<RepresentationInformation>> getObjectClasses() {
    return List.of(RepresentationInformation.class);
  }
}
