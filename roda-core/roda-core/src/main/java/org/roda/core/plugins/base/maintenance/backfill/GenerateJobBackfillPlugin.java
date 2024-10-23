package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.model.ModelService;
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
    protected Job retrieveModelObject(ModelService model, String id)
            throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
        return model.retrieveJob(id);
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
