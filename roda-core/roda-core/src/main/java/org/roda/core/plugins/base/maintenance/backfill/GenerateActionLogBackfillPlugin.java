package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.plugins.Plugin;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class GenerateActionLogBackfillPlugin extends GenerateRODAEntityBackfillPlugin<LogEntry> {

    @Override
    protected <I extends IsIndexed> Class<I> getIndexClass() {
      return (Class<I>) LogEntry.class;
    }


    @Override
    public Plugin<LogEntry> cloneMe() {
      return new GenerateActionLogBackfillPlugin();
    }

    @Override
    public List<Class<LogEntry>> getObjectClasses() {
      return List.of(LogEntry.class);
    }
}
