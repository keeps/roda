package org.roda.wui.api.v2.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.LiteRODAObject;
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
import org.roda.core.data.v2.ip.metadata.ResourceVersion;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.risks.IncidenceStatus;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.risks.RiskMitigationProperties;
import org.roda.core.data.v2.risks.RiskMitigationTerms;
import org.roda.core.data.v2.risks.RiskVersions;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.base.maintenance.DeleteRODAObjectPlugin;
import org.roda.core.storage.BinaryVersion;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.common.model.RequestContext;
import org.springframework.stereotype.Service;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
@Service
public class RiskService {

  public Risk revertRiskVersion(ModelService modelService, String riskId, String versionId,
    Map<String, String> properties, int incidences)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    modelService.revertRiskVersion(riskId, versionId, properties, false, incidences);
    return modelService.retrieveRisk(riskId);
  }

  public void deleteRiskVersion(ModelService model, String riskId, String versionId)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    Optional<LiteRODAObject> liteRisk = LiteRODAObjectFactory.get(Risk.class, riskId);
    if (liteRisk.isEmpty()) {
      throw new RequestNotValidException("Could not get LITE for Risk " + riskId);
    }
    model.deleteBinaryVersion(liteRisk.get(), versionId);
  }

  public Risk retrieveRiskVersion(ModelService model, String riskId, String selectedVersion)
    throws RequestNotValidException, GenericException, NotFoundException {
    BinaryVersion bv = model.retrieveVersion(riskId, selectedVersion);
    try {
      return JsonUtils.getObjectFromJson(bv.getBinary().getContent().createInputStream(), Risk.class);
    } catch (IOException e) {
      throw new GenericException(e.getMessage(), e);
    }
  }

  public RiskVersions retrieveRiskVersions(ModelService model, String riskId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Optional<LiteRODAObject> liteRisk = LiteRODAObjectFactory.get(Risk.class, riskId);
    if (liteRisk.isEmpty()) {
      throw new RequestNotValidException("Could not get LITE for Risk " + riskId);
    }
    RiskVersions versions = new RiskVersions();

    try (CloseableIterable<BinaryVersion> iterable = model.listBinaryVersions(liteRisk.get())) {
      for (BinaryVersion bv : iterable) {
        versions.addObject(new ResourceVersion(bv.getId(), bv.getCreatedDate(), bv.getProperties()));
      }
    } catch (IOException e) {
      throw new GenericException(e.getMessage() != null ? e.getMessage() : "");
    }

    return versions;
  }

  public Job deleteRisk(User user, SelectedItems<IndexedRisk> selected)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    return CommonServicesUtils.createAndExecuteInternalJob("Delete risks", selected, DeleteRODAObjectPlugin.class, user,
      Collections.emptyMap(), "Could not execute risk delete action");
  }

  public void updateRiskCounters(IndexService index)
    throws GenericException, RequestNotValidException {

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
      //LOGGER.error("Error getting risks when updating counters", e);
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
          //LOGGER.warn("Updating risk counters found incidences pointing to non-existing risk: {}", riskId);
        }
      }
    }

    // update risks from facets (not mitigated incidences)
    for (FacetFieldResult fieldResult : findNotMitigatedRiskIncidences.getFacetResults()) {
      for (FacetValue facetValue : fieldResult.getValues()) {
        String riskId = facetValue.getValue();
        long counter = facetValue.getCount();

        IndexedRisk risk = allRisks.get(riskId);
        if (risk != null) {
          risk.setUnmitigatedIncidencesCount((int) counter);
        } else {
         // LOGGER.warn("Updating risk counters found incidences pointing to non-existing risk: {}", riskId);
        }
      }
    }

    // update all in index
    for (IndexedRisk risk : allRisks.values()) {
      index.reindexRisk(risk);
    }
  }
  public boolean hasRiskVersions(String riskId, ModelService modelService)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Optional<LiteRODAObject> liteRisk = LiteRODAObjectFactory.get(Risk.class, riskId);
    if (liteRisk.isEmpty()) {
      throw new RequestNotValidException("Could not get LITE for Risk " + riskId);
    }
    try (CloseableIterable<BinaryVersion> iterable = modelService.listBinaryVersions(liteRisk.get())) {
      return iterable.iterator().hasNext();
    } catch (IOException e) {
      throw new GenericException(e);
    }
  }

  public Risk createRisk(Risk risk, RequestContext requestContext, boolean commit)
    throws GenericException, AuthorizationDeniedException {
    risk.setCreatedBy(requestContext.getUser().getName());
    risk.setUpdatedBy(requestContext.getUser().getName());
    return requestContext.getModelService().createRisk(risk, commit);
  }

  public Risk updateRisk(Risk risk, RequestContext requestContext, Map<String, String> properties, boolean commit,
    int incidences) throws GenericException, AuthorizationDeniedException {
    risk.setUpdatedBy(requestContext.getUser().getName());
    return requestContext.getModelService().updateRisk(risk, properties, commit, incidences);
  }

  public RiskMitigationTerms retrieveFromConfigurationMitigationTerms(IndexedRisk indexedRisk) {
    int preMitigationProbability = indexedRisk.getPreMitigationProbability();
    int preMitigationImpact = indexedRisk.getPreMitigationImpact();
    int posMitigationProbability = indexedRisk.getPostMitigationProbability();
    int posMitigationImpact = indexedRisk.getPostMitigationImpact();

    int lowLimit = RodaCoreFactory.getRodaConfigurationAsInt("ui", "risk", "mitigationSeverity", "lowLimit");
    int highLimit = RodaCoreFactory.getRodaConfigurationAsInt("ui", "risk", "mitigationSeverity", "highLimit");

    String preProbability = RodaCoreFactory.getRodaConfigurationAsString("ui", "risk", "mitigationProbability",
      Integer.toString(preMitigationProbability));
    String preImpact = RodaCoreFactory.getRodaConfigurationAsString("ui", "risk", "mitigationImpact",
      Integer.toString(preMitigationImpact));
    String posProbability = RodaCoreFactory.getRodaConfigurationAsString("ui", "risk", "mitigationProbability",
      Integer.toString(posMitigationProbability));
    String posImpact = RodaCoreFactory.getRodaConfigurationAsString("ui", "risk", "mitigationImpact",
      Integer.toString(posMitigationImpact));

    return new RiskMitigationTerms(lowLimit, highLimit, preProbability, preImpact, posProbability, posImpact);
  }

  public RiskMitigationProperties retrieveMitigationProperties() {
    int lowLimit = RodaCoreFactory.getRodaConfigurationAsInt("ui", "risk", "mitigationSeverity", "lowLimit");
    int highLimit = RodaCoreFactory.getRodaConfigurationAsInt("ui", "risk", "mitigationSeverity", "highLimit");

    int probabilityLimit = RodaCoreFactory.getRodaConfigurationAsInt("ui", "risk", "mitigationProbability", "limit");
    int impactLimit = RodaCoreFactory.getRodaConfigurationAsInt("ui", "risk", "mitigationImpact", "limit");

    // second list contains probability content
    List<String> probabilities = new ArrayList<>();
    for (int i = 0; i <= probabilityLimit; i++) {
      String value = Integer.toString(i);
      probabilities.add(RodaCoreFactory.getRodaConfigurationAsString("ui", "risk", "mitigationProbability", value));
    }

    // third list contains impact content
    List<String> impacts = new ArrayList<>();
    for (int i = 0; i <= impactLimit; i++) {
      String value = Integer.toString(i);
      impacts.add(RodaCoreFactory.getRodaConfigurationAsString("ui", "risk", "mitigationImpact", value));
    }

    return new RiskMitigationProperties(lowLimit, highLimit, probabilities, impacts);
  }
}
