package org.roda.wui.api.v2.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.DisposalHoldNotValidException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.disposal.metadata.DisposalTransitiveHoldsAIPMetadata;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHolds;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.user.User;
import org.roda.core.plugins.base.disposal.hold.ApplyDisposalHoldToAIPPlugin;
import org.roda.core.plugins.base.disposal.hold.DisassociateDisposalHoldFromAIPPlugin;
import org.roda.core.plugins.base.disposal.hold.LiftDisposalHoldPlugin;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
public class DisposalHoldService {

  public DisposalHolds getDisposalHolds()
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, IOException {
    return RodaCoreFactory.getModelService().listDisposalHolds();
  }

  public DisposalHold updateDisposalHold(DisposalHold hold, User user) throws AuthorizationDeniedException,
    RequestNotValidException, NotFoundException, IllegalOperationException, GenericException {
    return RodaCoreFactory.getModelService().updateDisposalHold(hold, user.getName());
  }

  public DisposalHold createDisposalHold(DisposalHold disposalHold, User user) throws GenericException,
    AuthorizationDeniedException, AlreadyExistsException, NotFoundException, RequestNotValidException {
    return RodaCoreFactory.getModelService().createDisposalHold(disposalHold, user.getName());
  }

  public DisposalHold retrieveDisposalHold(String id)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return RodaCoreFactory.getModelService().retrieveDisposalHold(id);
  }

  public void deleteDisposalHold(String disposalHoldId) throws GenericException, RequestNotValidException,
    NotFoundException, AuthorizationDeniedException, IllegalOperationException {
    RodaCoreFactory.getModelService().deleteDisposalHold(disposalHoldId);
  }

  public DisposalTransitiveHoldsAIPMetadata listTransitiveDisposalHolds(String aipId) throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return RodaCoreFactory.getModelService().listTransitiveDisposalHolds(aipId);
  }

  public void validateDisposalHold(DisposalHold disposalHold) throws DisposalHoldNotValidException {
    if (StringUtils.isBlank(disposalHold.getTitle())) {
      throw new DisposalHoldNotValidException("The disposal hold title is mandatory");
    }
  }

  public Job applyDisposalHold(User user, SelectedItems<IndexedAIP> items, String disposalHoldId, boolean override)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID, disposalHoldId);
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_OVERRIDE, Boolean.toString(override));

    return CommonServicesUtils.createAndExecuteInternalJob("Apply disposal hold", items,
      ApplyDisposalHoldToAIPPlugin.class, user, pluginParameters, "Could not execute apply disposal hold action");
  }

  public Job liftDisposalHold(User user, SelectedItems<IndexedAIP> items, String disposalHoldId)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID, disposalHoldId);

    return CommonServicesUtils.createAndExecuteInternalJob("Lift disposal hold", items, LiftDisposalHoldPlugin.class,
      user, pluginParameters, "Could not execute lift disposal hold action");
  }

  public Job disassociateDisposalHold(User user, SelectedItems<IndexedAIP> items, String disposalHoldId,
    boolean clearAll)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID, disposalHoldId);
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_DISASSOCIATE_ALL, Boolean.toString(clearAll));

    return CommonServicesUtils.createAndExecuteInternalJob("Disassociate disposal hold", items,
      DisassociateDisposalHoldFromAIPPlugin.class, user, pluginParameters,
      "Could not execute disassociate disposal hold action");
  }
}
