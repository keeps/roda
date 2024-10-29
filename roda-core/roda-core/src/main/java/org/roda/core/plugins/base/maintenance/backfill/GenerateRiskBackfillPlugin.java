package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.plugins.Plugin;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class GenerateRiskBackfillPlugin extends GenerateRODAEntityBackfillPlugin<Risk> {

    @Override
    protected <I extends IsIndexed> Class<I> getIndexClass() {
        return (Class<I>) IndexedRisk.class;
    }

    @Override
    protected String getObjectId(Risk object) {
      return object.getId();
    }

    @Override
    public Plugin<Risk> cloneMe() {
        return new GenerateRiskBackfillPlugin();
    }

    @Override
    public List<Class<Risk>> getObjectClasses() {
        return List.of(Risk.class);
    }
}
