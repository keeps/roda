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
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHolds;
import org.roda.core.data.v2.disposal.metadata.DisposalTransitiveHoldsAIPMetadata;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposalhold.DisassociateDisposalHoldRequest;
import org.roda.core.data.v2.ip.disposalhold.LiftDisposalHoldRequest;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.base.disposal.hold.ApplyDisposalHoldToAIPPlugin;
import org.roda.core.plugins.base.disposal.hold.DisassociateDisposalHoldFromAIPPlugin;
import org.roda.core.plugins.base.disposal.hold.LiftDisposalHoldPlugin;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.model.RequestContext;
import org.springframework.stereotype.Service;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
public class DisposalHoldService {

  public DisposalHolds getDisposalHolds(ModelService model)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, IOException {
    return model.listDisposalHolds();
  }

  public DisposalHold updateDisposalHold(DisposalHold hold, String details, RequestContext context)
    throws AuthorizationDeniedException,
    RequestNotValidException, NotFoundException, IllegalOperationException, GenericException {
    return context.getModelService().updateDisposalHold(hold, context.getUser().getName(), details);
  }

  public DisposalHold createDisposalHold(DisposalHold disposalHold, RequestContext context) throws GenericException,
    AuthorizationDeniedException, AlreadyExistsException, NotFoundException, RequestNotValidException {
    return context.getModelService().createDisposalHold(disposalHold, context.getUser().getName());
  }

  public DisposalHold retrieveDisposalHold(String id, ModelService model)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return model.retrieveDisposalHold(id);
  }

  public DisposalTransitiveHoldsAIPMetadata listTransitiveDisposalHolds(String aipId, ModelService model) throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return model.listTransitiveDisposalHolds(aipId);
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

  public Job liftDisposalHold(User user, String disposalHoldId, String details)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID, disposalHoldId);
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, details);

    return CommonServicesUtils.createAndExecuteInternalJob("Lift disposal hold", SelectedItemsNone.create(), LiftDisposalHoldPlugin.class,
      user, pluginParameters, "Could not execute lift disposal hold action");
  }

  public Job disassociateDisposalHold(User user, DisassociateDisposalHoldRequest request, String disposalHoldId)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_ID, disposalHoldId);
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_HOLD_DISASSOCIATE_ALL,
      Boolean.toString(request.getClear()));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, request.getDetails());

    SelectedItems<IndexedAIP> items = CommonServicesUtils.convertSelectedItems(request.getSelectedItems(),
      IndexedAIP.class);
    return CommonServicesUtils.createAndExecuteInternalJob("Disassociate disposal hold", items,
      DisassociateDisposalHoldFromAIPPlugin.class, user, pluginParameters,
      "Could not execute disassociate disposal hold action");
  }
}
