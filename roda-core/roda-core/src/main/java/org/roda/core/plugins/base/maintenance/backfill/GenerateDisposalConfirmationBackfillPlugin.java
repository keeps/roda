package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmation;
import org.roda.core.plugins.Plugin;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class GenerateDisposalConfirmationBackfillPlugin
  extends GenerateRODAEntityBackfillPlugin<DisposalConfirmation> {

  @Override
  protected <I extends IsIndexed> Class<I> getIndexClass() {
    return (Class<I>) DisposalConfirmation.class;
  }

  @Override
  protected String getObjectId(DisposalConfirmation object) {
    return object.getId();
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
