package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class GenerateDisposalConfirmationBackfillPlugin extends GenerateRODAEntityBackfillPlugin<DisposalConfirmation> {

  @Override
  protected <I extends IsIndexed> Class<I> getIndexClass() {
    return (Class<I>) DisposalConfirmation.class;
  }

  @Override
  protected DisposalConfirmation retrieveModelObject(ModelService model, String id)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    return model.retrieveDisposalConfirmation(id);
  }

  @Override
  public Plugin<DisposalConfirmation> cloneMe() {
    return new GenerateDisposalConfirmationBackfillPlugin();
  }

  @Override
  public List<Class<DisposalConfirmation>> getObjectClasses() {
    return List.of(DisposalConfirmation.class);
  }
}
