package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ri.RepresentationInformation;
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
  protected String getObjectId(RepresentationInformation object) {
    return object.getId();
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
