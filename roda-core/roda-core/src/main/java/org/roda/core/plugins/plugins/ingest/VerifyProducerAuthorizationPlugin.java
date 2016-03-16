/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPPermissions;
import org.roda.core.data.v2.ip.AIPPermissions.PermissionType;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerifyProducerAuthorizationPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(VerifyProducerAuthorizationPlugin.class);
  private static final String CREATE_TOP_LEVEL_AIP_PERMISSION = "create.top.level.aip";
  public static final String NO_PERMISSION_TO_CREATE_UNDER_AIP = "The user doesn't have permission to create under AIP";
  private static final String PARENT_AIP_NOT_FOUND = "The parent of the AIP was not found";
  private static final String NO_AIP_PERMISSION = "The user doesn't have access to the parent AIP";
  private static final String AIP_PERMISSIONS_SUCCESSFULLY_VERIFIED = "The user permissions are valid and the AIP permissions were updated";
  private static final String NO_CREATE_TOP_LEVEL_AIP_PERMISSION = "The user doesn't have CREATE_TOP_LEVEL_AIP_PERMISSION permission";

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
    return "Check producer authorization";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getDescription() {
    return "Check SIP producer authorization. Verifies that the producer of the SIP has permissions to ingest to the specified Fonds.";
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    Report report = PluginHelper.createPluginReport(this);

    for (AIP aip : list) {
      PluginState state = PluginState.SUCCESS;
      String details = AIP_PERMISSIONS_SUCCESSFULLY_VERIFIED;
      Report reportItem = PluginHelper.createPluginReportItem(this, aip.getId(), null);

      LOGGER.debug("Checking producer authorization for AIingest.submitP {}", aip.getId());
      Job currentJob = null;
      try {
        currentJob = PluginHelper.getJobFromIndex(this, index);
      } catch (NotFoundException | GenericException e) {
        LOGGER.error(e.getMessage(),e);
      }
      if (currentJob != null) {
        try {
          AIP parentAIP = null;
          if (aip.getParentId() != null) {
            LOGGER.debug("PARENT ID: "+aip.getParentId());
            try {
              parentAIP = model.retrieveAIP(aip.getParentId());
              Set<PermissionType> userPermissions = parentAIP.getPermissions()
                .getUserPermissions(currentJob.getUsername());
              if (userPermissions.contains(PermissionType.CREATE)) {
                LOGGER.debug("User have CREATE permission on parent... Granting user permission to this aip");
                grantPermissionToUser(currentJob.getUsername(),aip,model);
              } else {               
                LOGGER.debug("User doesn't have CREATE permission on parent... Error...");
                state = PluginState.FAILURE;
                details = NO_PERMISSION_TO_CREATE_UNDER_AIP;
              }
            } catch (NotFoundException nfe) { // parent not found
              state = PluginState.FAILURE;
              details = PARENT_AIP_NOT_FOUND;
            } catch (AuthorizationDeniedException e) { // doesn't have access to
                                                       // AIP
              LOGGER.debug("User doesn't have access to parent... Error...");
              state = PluginState.FAILURE;
              details = NO_AIP_PERMISSION;
            }
          } else {
            RODAMember member = index.retrieve(RODAMember.class, currentJob.getUsername());
            if (member.getAllRoles().contains(CREATE_TOP_LEVEL_AIP_PERMISSION)) {
              LOGGER.debug("User have CREATE_TOP_LEVEL_AIP_PERMISSION permission... Granting user permission to this aip...");
              grantPermissionToUser(currentJob.getUsername(),aip,model);
            }else{
              state = PluginState.FAILURE;
              details = NO_CREATE_TOP_LEVEL_AIP_PERMISSION;
              LOGGER.debug("User doesn't have CREATE_TOP_LEVEL_AIP_PERMISSION permission...");
            }
          }
        } catch (GenericException | RequestNotValidException e) {
          LOGGER.error("Error: "+e.getMessage(),e);
          state = PluginState.FAILURE;
          details = e.getMessage();
        } catch (NotFoundException e) { // thrown if user associated to job
                                        // doesn't exist... never thrown, i
                                        // guess...

        } catch(AuthorizationDeniedException ade){
          LOGGER.error("Authorization denied: "+ade.getMessage(),ade);
          state = PluginState.FAILURE;
          details = ade.getMessage();
        }
      }
      reportItem.setPluginState(state).setPluginDetails(details);

      try {
        boolean notify = true;
        PluginHelper.createPluginEvent(this, aip.getId(), model, index, state, details, notify);
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
        | ValidationException | AlreadyExistsException e) {
        throw new PluginException("Error while creating the event", e);
      }
      reportItem.setPluginState(PluginState.SUCCESS)
        .setPluginDetails(String.format("Done with checking producer authorization for AIP %s", aip.getId()));
      LOGGER.debug("Done with checking producer authorization for AIP {}", aip.getId());

      report.addReport(reportItem);

      PluginHelper.updateJobReport(this, model, index, reportItem);

    }

    return report;
  }

  private void grantPermissionToUser(String username, AIP aip, ModelService model) throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    AIPPermissions aipPermissions = aip.getPermissions();
    Set<PermissionType> allPermissions = Stream.of(PermissionType.CREATE, PermissionType.DELETE,
      PermissionType.GRANT, PermissionType.READ, PermissionType.UPDATE).collect(Collectors.toSet());
    aipPermissions.setUserPermissions(username, allPermissions);
    aip.setPermissions(aipPermissions);
    model.updateAIPPermissions(aip);
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new VerifyProducerAuthorizationPlugin();
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
    return PreservationEventType.OBJECT_VALIDATION;
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
}
