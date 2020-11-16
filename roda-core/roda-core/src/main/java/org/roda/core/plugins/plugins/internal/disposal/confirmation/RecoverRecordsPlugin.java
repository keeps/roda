package org.roda.core.plugins.plugins.internal.disposal.confirmation;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmation;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationState;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RecoverRecordsPlugin extends AbstractPlugin<DisposalConfirmation> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RecoverRecordsPlugin.class);

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  public static String getStaticName() {
    return "Recover records under disposal confirmation report";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public RodaConstants.PreservationEventType getPreservationEventType() {
    return RodaConstants.PreservationEventType.RECOVERY;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Recover records under disposal confirmation report";
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
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectProcessingLogic<DisposalConfirmation>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<DisposalConfirmation> plugin, DisposalConfirmation object) {
        processDisposalConfirmation(index, model, storage, report, cachedJob, jobPluginInfo, object);
      }
    }, index, model, storage, liteList);
  }

  private void processDisposalConfirmation(IndexService index, ModelService model, StorageService storage,
    Report report, Job cachedJob, JobPluginInfo jobPluginInfo, DisposalConfirmation disposalConfirmation) {

    disposalConfirmation.setState(DisposalConfirmationState.RESTORED);
    disposalConfirmation.setRestoredOn(new Date());
    disposalConfirmation.setRestoredBy(cachedJob.getUsername());
    try {
      model.updateDisposalConfirmation(disposalConfirmation);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
      e.printStackTrace();
    }

  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public List<Class<DisposalConfirmation>> getObjectClasses() {
    return Collections.singletonList(DisposalConfirmation.class);
  }

  @Override
  public List<String> getCategories() {
    return Collections.singletonList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public Plugin<DisposalConfirmation> cloneMe() {
    return new RecoverRecordsPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.INTERNAL;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }
}
