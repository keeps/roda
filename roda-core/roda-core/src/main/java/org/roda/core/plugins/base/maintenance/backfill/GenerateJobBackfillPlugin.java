package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.plugins.Plugin;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class GenerateJobBackfillPlugin extends GenerateRODAEntityBackfillPlugin<Job> {

    @Override
    protected <I extends IsIndexed> Class<I> getIndexClass() {
        return (Class<I>) Job.class;
    }

    @Override
    protected String getObjectId(Job object) {
      return object.getId();
    }

    @Override
    public Plugin<Job> cloneMe() {
        return new GenerateJobBackfillPlugin();
    }

    @Override
    public List<Class<Job>> getObjectClasses() {
        return List.of(Job.class);
    }
}
