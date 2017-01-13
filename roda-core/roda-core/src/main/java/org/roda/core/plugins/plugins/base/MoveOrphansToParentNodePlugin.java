/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveOrphansToParentNodePlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MoveOrphansToParentNodePlugin.class);
  private AIP newParent;

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Move orphan(s) to a parent node";
  }

  @Override
  public String getDescription() {
    return "Moves selected AIP(s) that are also orphans, i.e. AIPs whose direct ancestor in the catalogue hierarchy does not exist "
      + "(except root level nodes) to a new parent node defined by the user.\nThis task aims to fix problems that may occur when SIPs are "
      + "ingested but not all the necessary items to construct the catalogue hierarchy have been received or properly ingested.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  public AIP getNewParent() {
    return newParent;
  }

  public void setNewParent(AIP newParent) {
    this.newParent = newParent;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<AIP>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        SimpleJobPluginInfo jobPluginInfo, Plugin<AIP> plugin, List<AIP> objects) {
        processAIPs(index, model, cachedJob, objects);
      }
    }, index, model, storage, liteList);
  }

  private void processAIPs(IndexService index, ModelService model, Job cachedJob, List<AIP> list) {
    List<String> aipIds = list.stream().map(aip -> aip.getId()).collect(Collectors.toList());

    try {
      IndexResult<IndexedAIP> indexResults = index.find(IndexedAIP.class,
        new Filter(new OneOfManyFilterParameter(RodaConstants.AIP_AIP_ID, aipIds)), Sorter.NONE,
        new Sublist(0, aipIds.size()));

      for (IndexedAIP indexedAIP : indexResults.getResults()) {
        try {
          LOGGER.debug("Processing AIP {}", indexedAIP.getId());
          if (indexedAIP.getLevel() == null || !indexedAIP.getLevel().trim().equalsIgnoreCase("fonds")) {
            AIP aip = model.retrieveAIP(indexedAIP.getId());
            aip.setParentId(newParent.getId());
            model.updateAIP(aip, cachedJob.getUsername());
          } else {
            LOGGER.debug("AIP doesn't need to be moved... level: {}", indexedAIP.getLevel());
          }
        } catch (RODAException e) {
          LOGGER.error("Error processing AIP {} (RemoveOrphansAction)", indexedAIP.getId(), e);
        }
      }
    } catch (GenericException | RequestNotValidException e) {
      LOGGER.error("Error while calculating orphans to be moved", e);
    }
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new MoveOrphansToParentNodePlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  // TODO FIX
  @Override
  public PreservationEventType getPreservationEventType() {
    return null;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Moved orphan AIPs to a new parent";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Moved orphan AIPs to a new parent successfully";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Move of orphan AIPs to a new parent failed";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_MANAGEMENT);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }

}
