package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.plugins.Plugin;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class GeneratePreservationRepositoryEventBackfillPlugin
  extends GenerateRODAEntityBackfillPlugin<PreservationMetadata> {

    @Override
    protected <I extends IsIndexed> Class<I> getIndexClass() {
      return (Class<I>) IndexedPreservationEvent.class;
    }


    @Override
    public Plugin<PreservationMetadata> cloneMe() {
        return new GeneratePreservationRepositoryEventBackfillPlugin();
    }

    @Override
    public List<Class<PreservationMetadata>> getObjectClasses() {
      return List.of(PreservationMetadata.class);
    }
}
