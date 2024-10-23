package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class GenerateIncidenceBackfillPlugin extends GenerateRODAEntityBackfillPlugin<RiskIncidence> {

  @Override
  protected <I extends IsIndexed> Class<I> getIndexClass() {
    return (Class<I>) RiskIncidence.class;
  }

  @Override
  protected RiskIncidence retrieveModelObject(ModelService model, String id)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    return model.retrieveRiskIncidence(id);
  }

  @Override
  public Plugin<RiskIncidence> cloneMe() {
    return new GenerateIncidenceBackfillPlugin();
  }

  @Override
  public List<Class<RiskIncidence>> getObjectClasses() {
    return List.of(RiskIncidence.class);
  }
}
