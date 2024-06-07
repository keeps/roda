package org.roda.wui.api.v2.services;

import static org.roda.wui.api.v2.utils.CommonServicesUtils.createAndExecuteInternalJob;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.generics.DeleteRequest;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.User;
import org.roda.core.plugins.base.maintenance.DeleteRODAObjectPlugin;
import org.roda.core.plugins.base.risks.UpdateIncidencesPlugin;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
@Service
public class RiskIncidenceService {
  private static final Logger LOGGER = LoggerFactory.getLogger(RiskIncidenceService.class);

  public Job deleteRiskIncidences(User user, DeleteRequest request)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, request.getDetails());
    return createAndExecuteInternalJob("Delete risk incidences",
      CommonServicesUtils.convertSelectedItems(request.getItemsToDelete(), RiskIncidence.class),
      DeleteRODAObjectPlugin.class, user, pluginParameters, "Could not execute risk incidence delete action");
  }

  public Job updateMultipleIncidences(User user, SelectedItems<RiskIncidence> selected, String status, String severity,
    String mitigatedDescription)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    Map<String, String> pluginParameters = new HashMap<>();
    if (!status.equals("UNMITIGATED") && !status.equals("MITIGATED") && !status.equals("ACCEPT_RISK")
      && !status.equals("FALSE_POSITIVE")) {
      throw new RequestNotValidException("Could not execute risk incidence update action");
    }
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_STATUS, status);
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_SEVERITY, severity);
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_MITIGATED_BY, user.getName());
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_MITIGATED_DESCRIPTION, mitigatedDescription);

    return createAndExecuteInternalJob("Update risk incidences", selected, UpdateIncidencesPlugin.class, user,
      pluginParameters, "Could not execute risk incidence update action");
  }

  public RiskIncidence updateRiskIncidence(RiskIncidence incidence)
    throws GenericException, AuthorizationDeniedException {
    return RodaCoreFactory.getModelService().updateRiskIncidence(incidence, true);
  }

}
