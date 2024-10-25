package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.plugins.Plugin;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class GenerateRODAMemberBackfillPlugin extends GenerateRODAEntityBackfillPlugin<RODAMember> {

    @Override
    protected <I extends IsIndexed> Class<I> getIndexClass() {
      return (Class<I>) RODAMember.class;
    }


    @Override
    public Plugin<RODAMember> cloneMe() {
        return new GenerateRODAMemberBackfillPlugin();
    }

    @Override
    public List<Class<RODAMember>> getObjectClasses() {
      return List.of(RODAMember.class);
    }
}
