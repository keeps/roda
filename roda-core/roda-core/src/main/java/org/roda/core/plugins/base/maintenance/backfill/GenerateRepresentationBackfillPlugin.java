package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.plugins.Plugin;
import org.roda.core.util.IdUtils;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class GenerateRepresentationBackfillPlugin extends GenerateRODAEntityBackfillPlugin<Representation> {

  @Override
  protected <I extends IsIndexed> Class<I> getIndexClass() {
    return (Class<I>) IndexedRepresentation.class;
  }

  @Override
  protected String getObjectId(Representation object) {
    return IdUtils.getRepresentationId(object);
  }

  @Override
  public Plugin<Representation> cloneMe() {
    return new GenerateRepresentationBackfillPlugin();
  }

  @Override
  public List<Class<Representation>> getObjectClasses() {
    return List.of(Representation.class);
  }
}
