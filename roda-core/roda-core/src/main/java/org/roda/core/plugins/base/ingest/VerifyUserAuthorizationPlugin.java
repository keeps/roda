/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.ingest;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class VerifyUserAuthorizationPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(VerifyUserAuthorizationPlugin.class);
  private static final String PARENT_AIP_NOT_FOUND = "The parent of the AIP was not found";

  private static final List<String> userFieldsToReturn = Arrays.asList(RodaConstants.MEMBERS_GROUPS,
    RodaConstants.MEMBERS_ID);

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
    return "Verify user authorization";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Checks if the user has enough permissions to place the AIP under the desired node in the classification scheme";
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
  public Report execute(IndexService index, ModelService model,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectProcessingLogic<AIP>() {
      @Override
      public void process(IndexService index, ModelService model, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<AIP> plugin, AIP object) {
        processAIP(index, model, report, cachedJob, jobPluginInfo, object);
      }
    }, index, model, liteList);
  }

  private void processAIP(IndexService index, ModelService model, Report report, Job cachedJob,
    JobPluginInfo jobPluginInfo, AIP aip) {
    LOGGER.debug("Checking user authorization for creating AIP {}", aip.getId());

    Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.INGEST_PROCESSING);
    PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);

    reportItem.setPluginState(PluginState.SUCCESS)
      .setPluginDetails(String.format("Done with checking user authorization for AIP %s", aip.getId()));

    if (cachedJob != null) {
      processAIPPermissions(index, cachedJob, aip, reportItem);
    } else {
      reportItem.setPluginState(PluginState.FAILURE).setPluginDetails("Unable to determine Job.");
    }

    try {
      boolean notify = true;
      PluginHelper.createPluginEvent(this, aip.getId(), model, index, reportItem.getPluginState(),
        reportItem.getPluginDetails(), notify, cachedJob);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | ValidationException | AlreadyExistsException e) {
      reportItem.setPluginState(PluginState.FAILURE).addPluginDetails("Unable to create event.");
    }

    // set counters
    if (reportItem.getPluginState() == PluginState.SUCCESS) {
      jobPluginInfo.incrementObjectsProcessedWithSuccess();
    } else {
      jobPluginInfo.incrementObjectsProcessedWithFailure();
    }

    LOGGER.debug("Done with checking user authorization for AIP {}", aip.getId());

    report.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
  }

  private void processAIPPermissions(IndexService index, Job cachedJob, AIP aip, Report reportItem) {
    try {
      String jobCreatorUsername = cachedJob.getUsername();
      if (aip.getParentId() != null) {
        try {
          IndexedAIP parentAIP = index.retrieve(IndexedAIP.class, aip.getParentId(),
            RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
          User user = (User) index.retrieve(RODAMember.class, IdUtils.getUserId(jobCreatorUsername),
            userFieldsToReturn);
          UserUtility.checkAIPPermissions(user, parentAIP, PermissionType.CREATE);
        } catch (NotFoundException nfe) {
          reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(PARENT_AIP_NOT_FOUND);
        } catch (AuthorizationDeniedException e) {
          LOGGER.debug("User '{}' doesn't have CREATE permission on parent... Error...", jobCreatorUsername);
          reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(
            "The user " + jobCreatorUsername + " doesn't have permission to create under AIP " + aip.getId());
        }
      } else {
        RODAMember member = index.retrieve(RODAMember.class, IdUtils.getUserId(jobCreatorUsername),
          Arrays.asList(RodaConstants.MEMBERS_ROLES_ALL));
        if (member.getAllRoles().contains(RodaConstants.REPOSITORY_PERMISSIONS_AIP_CREATE_TOP)) {
          LOGGER.debug("User have CREATE_TOP_LEVEL_AIP_PERMISSION permission.");
        } else {
          reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(
            "The user " + jobCreatorUsername + " doesn't have CREATE_TOP_LEVEL_AIP_PERMISSION permission");
          LOGGER.debug("User doesn't have CREATE_TOP_LEVEL_AIP_PERMISSION permission...");
        }
      }
    } catch (GenericException | NotFoundException e) {
      LOGGER.error("Error processing AIP permissions", e);
      reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
    }
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new VerifyUserAuthorizationPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.AUTHORIZATION_CHECK;
  }

  @Override
  public String getPreservationEventDescription() {
    String description = "User permissions have been checked to ensure that he has sufficient authorization to store the AIP under the desired "
      + "node of the classification scheme.";

    if (hasFreeAccess) {
      description += " It was given READ permission to the users group as indicated on the descriptive metadata";
    }

    return description;
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "The user has enough permissions to deposit the AIP under the designated node of the classification scheme";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "The user does not have enough permissions to deposit the AIP under the designated node of the classification scheme";
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_VALIDATION);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }
}
