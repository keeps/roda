package org.roda.wui.api.controllers;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.LockingException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetValue;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.plugins.PluginHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class Aips extends RemoteServiceServlet {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobsHelper.class);

  Aips(){
    // do nothing
  }

  public static void releaseAIPLock(String aipId, User user) {
    boolean lockEnabled = RodaCoreFactory.getRodaConfiguration().getBoolean("core.aip.lockToEdit", false);

    if (lockEnabled) {
      PluginHelper.releaseObjectLock(aipId, user.getUUID());
    }
  }

  public static boolean requestAIPLock(String aipId, User user) {
    boolean lockEnabled = RodaCoreFactory.getRodaConfiguration().getBoolean("core.aip.lockToEdit", false);

    if (!lockEnabled) {
      return true;
    }


    try {
      PluginHelper.tryLock(Collections.singletonList(aipId), user.getUUID());
    } catch (LockingException e) {
      return false;
    }
    return true;
  }

  public static Pair<Boolean, List<String>> retrieveAIPTypeOptions(String locale, User user) {
    List<String> types = new ArrayList<>();
    boolean isControlled = RodaCoreFactory.getRodaConfiguration().getBoolean("core.aip_type.controlled_vocabulary",
      false);

    if (isControlled) {
      types = RodaCoreFactory.getRodaConfigurationAsList("core.aip_type.value");
    } else {
      try {
        Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.AIP_TYPE));
        IndexResult<IndexedAIP> result = Browser.find(IndexedAIP.class.getName(), Filter.ALL, Sorter.NONE, user, Sublist.NONE, facets,
          locale, false, new ArrayList<>());

        List<FacetFieldResult> facetResults = result.getFacetResults();
        for (FacetValue facetValue : facetResults.get(0).getValues()) {
          types.add(facetValue.getValue());
        }
      } catch (GenericException | AuthorizationDeniedException | RequestNotValidException e) {
        LOGGER.error("Could not execute find request on AIPs", e);
      }
    }

    return Pair.of(isControlled, types);
  }

}
