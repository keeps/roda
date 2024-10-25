package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.plugins.Plugin;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class GenerateTransferredResourceBackfillPlugin extends GenerateRODAEntityBackfillPlugin<TransferredResource> {

    @Override
    protected <I extends IsIndexed> Class<I> getIndexClass() {
      return (Class<I>) TransferredResource.class;
    }


    @Override
    public Plugin<TransferredResource> cloneMe() {
        return new GenerateTransferredResourceBackfillPlugin();
    }

    @Override
    public List<Class<TransferredResource>> getObjectClasses() {
      return List.of(TransferredResource.class);
    }
}
