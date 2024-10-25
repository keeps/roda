package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.risks.RiskIncidence;
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
  public Plugin<RiskIncidence> cloneMe() {
    return new GenerateIncidenceBackfillPlugin();
  }

  @Override
  public List<Class<RiskIncidence>> getObjectClasses() {
    return List.of(RiskIncidence.class);
  }
}
