package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.model.ModelService;
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
    protected Risk retrieveModelObject(ModelService model, String id)
            throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
        return model.retrieveRisk(id);
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
