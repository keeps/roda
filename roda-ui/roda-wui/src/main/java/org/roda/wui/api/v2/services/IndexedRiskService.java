package org.roda.wui.api.v2.services;

import static org.roda.wui.api.controllers.BrowserHelper.createAndExecuteInternalJob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetValue;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.risks.IncidenceStatus;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.base.maintenance.DeleteRODAObjectPlugin;
import org.roda.core.storage.BinaryVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
@Service
public class IndexedRiskService {
  private static final Logger LOGGER = LoggerFactory.getLogger(IndexedRiskService.class);

  public Job deleteRisk(User user, SelectedItems<IndexedRisk> selected)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    return createAndExecuteInternalJob("Delete risks", selected, DeleteRODAObjectPlugin.class, user,
      Collections.emptyMap(), "Could not execute risk delete action");
  }

  public boolean hasRiskVersions(String riskId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath storagePath = ModelUtils.getRiskStoragePath(riskId);
    try (
      CloseableIterable<BinaryVersion> iterable = RodaCoreFactory.getStorageService().listBinaryVersions(storagePath)) {
      return iterable.iterator().hasNext();
    } catch (IOException e) {
      throw new GenericException(e);
    }
  }

  public Risk updateRisk(Risk risk, User user, Map<String, String> properties, boolean commit, int incidences)
    throws GenericException, AuthorizationDeniedException {
    risk.setUpdatedBy(user.getName());
    return RodaCoreFactory.getModelService().updateRisk(risk, properties, commit, incidences);
  }

  public void updateRiskCounters()
    throws GenericException, RequestNotValidException, AuthorizationDeniedException {
    IndexService index = RodaCoreFactory.getIndexService();

    IndexResult<RiskIncidence> findAllRiskIncidences = index.find(RiskIncidence.class, Filter.ALL, Sorter.NONE,
      new Sublist(0, 0), new Facets(new SimpleFacetParameter(RodaConstants.RISK_INCIDENCE_RISK_ID)),
      Arrays.asList(RodaConstants.INDEX_UUID));

    Filter filter = new Filter(
      new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_STATUS, IncidenceStatus.UNMITIGATED.toString()));
    IndexResult<RiskIncidence> findNotMitigatedRiskIncidences = index.find(RiskIncidence.class, filter, Sorter.NONE,
      new Sublist(0, 0), new Facets(new SimpleFacetParameter(RodaConstants.RISK_INCIDENCE_RISK_ID)),
      Arrays.asList(RodaConstants.INDEX_UUID));

    Map<String, IndexedRisk> allRisks = new HashMap<>();

    // retrieve risks and set default object count to zero
    try (IterableIndexResult<IndexedRisk> risks = index.findAll(IndexedRisk.class, Filter.ALL, new ArrayList<>())) {
      for (IndexedRisk indexedRisk : risks) {
        indexedRisk.setIncidencesCount(0);
        indexedRisk.setUnmitigatedIncidencesCount(0);
        allRisks.put(indexedRisk.getId(), indexedRisk);
      }
    } catch (IOException e) {
      LOGGER.error("Error getting risks when updating counters", e);
    }

    // update risks from facets (all incidences)
    for (FacetFieldResult fieldResult : findAllRiskIncidences.getFacetResults()) {
      for (FacetValue facetValue : fieldResult.getValues()) {
        String riskId = facetValue.getValue();
        long counter = facetValue.getCount();

        IndexedRisk risk = allRisks.get(riskId);
        if (risk != null) {
          risk.setIncidencesCount((int) counter);
        } else {
          LOGGER.warn("Updating risk counters found incidences pointing to non-existing risk: {}", riskId);
        }
      }
    }
  }

}
