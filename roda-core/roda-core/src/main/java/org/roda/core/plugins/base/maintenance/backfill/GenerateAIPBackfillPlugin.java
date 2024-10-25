package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.plugins.Plugin;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class GenerateAIPBackfillPlugin extends GenerateRODAEntityBackfillPlugin<AIP> {

    @Override
    protected <I extends IsIndexed> Class<I> getIndexClass() {
        return (Class<I>) IndexedAIP.class;
    }

    @Override
    public Plugin<AIP> cloneMe() {
        return new GenerateAIPBackfillPlugin();
    }

    @Override
    public List<Class<AIP>> getObjectClasses() {
        return List.of(AIP.class);
    }
}
