package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class GenerateDIPBackfillPlugin extends GenerateRODAEntityBackfillPlugin<DIP> {

  @Override
  protected <I extends IsIndexed> Class<I> getIndexClass() {
    return (Class<I>) IndexedDIP.class;
  }

  @Override
  protected DIP retrieveModelObject(ModelService model, String id)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    return model.retrieveDIP(id);
  }

  @Override
  public Plugin<DIP> cloneMe() {
    return new GenerateDIPBackfillPlugin();
  }

  @Override
  public List<Class<DIP>> getObjectClasses() {
    return List.of(DIP.class);
  }
}
