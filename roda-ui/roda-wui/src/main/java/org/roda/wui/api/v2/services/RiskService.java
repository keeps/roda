package org.roda.wui.api.v2.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.metadata.ResourceVersion;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskMitigationProperties;
import org.roda.core.data.v2.risks.RiskMitigationTerms;
import org.roda.core.data.v2.risks.RiskVersions;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.plugins.base.maintenance.DeleteRODAObjectPlugin;
import org.roda.core.storage.BinaryVersion;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
@Service
public class RiskService {
  private static final Logger LOGGER = LoggerFactory.getLogger(RiskService.class);

  public Risk revertRiskVersion(String riskId, String versionId, Map<String, String> properties, int incidences)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().revertRiskVersion(riskId, versionId, properties, false, incidences);
    return RodaCoreFactory.getModelService().retrieveRisk(riskId);
  }

  public void deleteRiskVersion(String riskId, String versionId)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    Optional<LiteRODAObject> liteRisk = LiteRODAObjectFactory.get(Risk.class, riskId);
    if (liteRisk.isEmpty()) {
      throw new RequestNotValidException("Could not get LITE for Risk " + riskId);
    }
    RodaCoreFactory.getModelService().deleteBinaryVersion(liteRisk.get(), versionId);
  }

  public Risk retrieveRiskVersion(String riskId, String selectedVersion)
    throws RequestNotValidException, GenericException, NotFoundException {
    BinaryVersion bv = RodaCoreFactory.getModelService().retrieveVersion(riskId, selectedVersion);
    try {
      return JsonUtils.getObjectFromJson(bv.getBinary().getContent().createInputStream(), Risk.class);
    } catch (IOException e) {
      throw new GenericException(e.getMessage(), e);
    }
  }

  public RiskVersions retrieveRiskVersions(String riskId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Optional<LiteRODAObject> liteRisk = LiteRODAObjectFactory.get(Risk.class, riskId);
    if (liteRisk.isEmpty()) {
      throw new RequestNotValidException("Could not get LITE for Risk " + riskId);
    }
    RiskVersions versions = new RiskVersions();

    try (CloseableIterable<BinaryVersion> iterable = RodaCoreFactory.getModelService()
      .listBinaryVersions(liteRisk.get())) {

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

  public boolean hasRiskVersions(String riskId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Optional<LiteRODAObject> liteRisk = LiteRODAObjectFactory.get(Risk.class, riskId);
    if (liteRisk.isEmpty()) {
      throw new RequestNotValidException("Could not get LITE for Risk " + riskId);
    }
    try (CloseableIterable<BinaryVersion> iterable = RodaCoreFactory.getModelService()
      .listBinaryVersions(liteRisk.get())) {
      return iterable.iterator().hasNext();
    } catch (IOException e) {
      throw new GenericException(e);
    }
  }

  public Risk createRisk(Risk risk, User user, boolean commit) throws GenericException, AuthorizationDeniedException {
    risk.setCreatedBy(user.getName());
    risk.setUpdatedBy(user.getName());
    return RodaCoreFactory.getModelService().createRisk(risk, commit);
  }

  public Risk updateRisk(Risk risk, User user, Map<String, String> properties, boolean commit, int incidences)
    throws GenericException, AuthorizationDeniedException {
    risk.setUpdatedBy(user.getName());
    return RodaCoreFactory.getModelService().updateRisk(risk, properties, commit, incidences);
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
