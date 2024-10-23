package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.StorageService;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class GeneratePreservationRepositoryEventBackfillPlugin extends AbstractPlugin<Void> {
    @Override
    public String getName() {
        return "Generate complete preservation event index backfill";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public RodaConstants.PreservationEventType getPreservationEventType() {
        return null;
    }

    @Override
    public String getPreservationEventDescription() {
        return "";
    }

    @Override
    public String getPreservationEventSuccessMessage() {
        return "";
    }

    @Override
    public String getPreservationEventFailureMessage() {
        return "";
    }

    @Override
    public String getPreservationEventSkippedMessage() {
        return super.getPreservationEventSkippedMessage();
    }

    @Override
    public PluginType getType() {
        return PluginType.MISC;
    }

    @Override
    public List<String> getCategories() {
        return List.of();
    }

    @Override
    public Plugin<Void> cloneMe() {
        return new GeneratePreservationRepositoryEventBackfillPlugin();
    }

    @Override
    public boolean areParameterValuesValid() {
        return false;
    }

    @Override
    public void init() throws PluginException {

    }

    @Override
    public List<Class<Void>> getObjectClasses() {
        return List.of(Void.class);
    }

    @Override
    public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
        return null;
    }

    @Override
    public Report execute(IndexService index, ModelService model, StorageService storage, List<LiteOptionalWithCause> list) throws PluginException {
        return null;
    }

    @Override
    public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
        return null;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public String getVersionImpl() {
        return "";
    }
}
