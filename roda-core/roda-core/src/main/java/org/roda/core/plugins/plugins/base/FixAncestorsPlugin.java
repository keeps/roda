/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.*;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class FixAncestorsPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(FixAncestorsPlugin.class);

  private boolean hasFreeAccess = false;

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  public static String getStaticName() {
    return "Fix the ancestor hierarchy";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Attempts to fix the ancestor hierarchy of the AIPs, removing ghosts and merging AIPs with the same Ingest SIP ID.";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
      throws PluginException {
    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      list.forEach(aip -> jobPluginInfo.incrementObjectsProcessedWithSuccess());

      String forcedParent = PluginHelper.getParentIdFromParameters(this);

      index.execute(IndexedAIP.class,
          new Filter(new SimpleFilterParameter(RodaConstants.AIP_GHOST, Boolean.TRUE.toString())),
          ghost -> {
            Filter nonGhostsFilter = new Filter(new SimpleFilterParameter(RodaConstants.INGEST_SIP_ID, ghost.getIngestSIPId()),
                new SimpleFilterParameter(RodaConstants.AIP_GHOST, Boolean.FALSE.toString()));
            if(!StringUtils.isBlank(forcedParent)){
              nonGhostsFilter.add(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, forcedParent));
            }
            // if there are AIPs that have the same sip id
            IndexResult<IndexedAIP> result = index.find(IndexedAIP.class, nonGhostsFilter, Sorter.NONE, new Sublist(0, 1));

            if(result.getTotalCount() > 1){
              LOGGER.debug("Couldn't find non-ghost AIP with ingest SIP id: " + ghost.getIngestSIPId());
            } else if(result.getTotalCount() == 1){
              IndexedAIP newParentIAIP = result.getResults().get(0);
              moveChildrenAIPsAndDelete(index, model, ghost.getId(), newParentIAIP.getId(), forcedParent);
            }else if(result.getTotalCount() == 0){
              //check if there are other ghosts with the same sip id and from the same job, move all of this ghost children
              Filter otherGhostsFilter = new Filter(new SimpleFilterParameter(RodaConstants.INGEST_SIP_ID, ghost.getIngestSIPId()),
                  new SimpleFilterParameter(RodaConstants.AIP_GHOST, Boolean.TRUE.toString()));
              if(!StringUtils.isBlank(forcedParent)){
                otherGhostsFilter.add(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, forcedParent));
              }
              IndexResult<IndexedAIP> otherGhosts = index.find(IndexedAIP.class, otherGhostsFilter, Sorter.NONE, new Sublist(0, 1));
              if(result.getTotalCount() >= 1){
                IndexedAIP otherGhost = otherGhosts.getResults().get(0);
                moveChildrenAIPsAndDelete(index, model, ghost.getId(), otherGhost.getId(), forcedParent);
              }
            }
          });

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException |JobException e) {
      LOGGER.error("Error while fixing the ancestors.", e);
    }
    return PluginHelper.initPluginReport(this);
  }

  private void moveChildrenAIPsAndDelete(IndexService index, ModelService model, String aipId, String newParentId, String forcedParent)
      throws GenericException, AuthorizationDeniedException, RequestNotValidException {
    Filter parentFilter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, aipId));
    if(!StringUtils.isBlank(forcedParent)){
      parentFilter.add(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, PluginHelper.getParentIdFromParameters(this)));
    }
    index.execute(IndexedAIP.class,
        parentFilter,
        child -> {
          try {
            model.moveAIP(child.getId(), newParentId);
          } catch (NotFoundException e) {
            LOGGER.debug("Can't move child. It wasn't found.", e);
          }
        });
    try {
      model.deleteAIP(aipId);
    } catch (NotFoundException e) {
      LOGGER.debug("Can't delete ghost or move node. It wasn't found.", e);
    }
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new FixAncestorsPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
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
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_MISC);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }
}
