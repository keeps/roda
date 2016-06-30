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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveOrphansPlugin extends AbstractPlugin<IndexedAIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RemoveOrphansPlugin.class);
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
    return "MoveOrphansToNewParent";
  }

  @Override
  public String getDescription() {
    return "Moves the orphans AIP (not Fonds) to a new parent";
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
  public Report execute(IndexService index, ModelService model, StorageService storage, List<IndexedAIP> list)
    throws PluginException {

    for (IndexedAIP indexedAIP : list) {
      try {
        LOGGER.debug("Processing AIP {}", indexedAIP.getId());
        if (indexedAIP.getLevel() == null || !indexedAIP.getLevel().trim().equalsIgnoreCase("fonds")) {
          AIP aip = model.retrieveAIP(indexedAIP.getId());
          aip.setParentId(newParent.getId());
          model.updateAIP(aip);
        } else {
          LOGGER.debug("  AIP doesn't need to be moved... Level: {}", indexedAIP.getLevel());
        }
      } catch (RODAException e) {
        LOGGER.error("Error processing AIP " + indexedAIP.getId() + " (RemoveOrphansAction)", e);
      }
    }
    return null;
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
  public Plugin<IndexedAIP> cloneMe() {
    return new RemoveOrphansPlugin();
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
    return "XXXXXXXXXX";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

}
