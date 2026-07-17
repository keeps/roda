package org.roda.wui.api.v2.utils;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;

import java.util.Set;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SecurityFilteringUtils {
  private static final Set<String> DISPOSAL_SCHEDULE_FIELDS = Set.of("disposalAction", "disposalScheduleId",
    "disposalScheduleName", "retentionPeriodDetails", "retentionPeriodDuration", "retentionPeriodInterval",
    "retentionPeriodState", "retentionPeriodStartDate", "scheduleAssociationType", "overdueDate");

  private static final Set<String> DISPOSAL_HOLDS_FIELDS = Set.of("transitiveDisposalHoldsId", "disposalHoldsId",
    "onHold");

  /**
   * Verifies if a user has permission to view a specific index field or facet.
   */
  public static boolean isFieldAuthorizedForUser(String fieldName, User user) {
    if (fieldName == null || user == null)
      return true;

    if ("hasRepresentations".equals(fieldName) || "representations".equals(fieldName)) {
      return UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_FIND_REPRESENTATION);
    }
    if ("descriptiveMetadata".equals(fieldName)) {
      return UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_LIST_AIP_DESCRIPTIVE_METADATA);
    }
    if (DISPOSAL_SCHEDULE_FIELDS.contains(fieldName)) {
      return UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_SCHEDULES);
    }
    if (DISPOSAL_HOLDS_FIELDS.contains(fieldName)) {
      return UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_HOLDS);
    }
    if ("disposalConfirmationId".equals(fieldName)) {
      return UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_FIND_DISPOSAL_CONFIRMATION);
    }
    if (fieldName.startsWith("permission_")) {
      return UserUtility.hasPermissions(user, "org.roda.wui.api.controllers.Browser.verifyPermissions");
    }

    return true;
  }

  /**
   * Strips unauthorized facet parameters from a FindRequest before Solr
   * execution.
   */
  public static void sanitizeFindRequest(FindRequest request, User user) {
    if (request != null && request.getFacets() != null && request.getFacets().getParameters() != null) {
      request.getFacets().getParameters().entrySet().removeIf(entry -> !isFieldAuthorizedForUser(entry.getKey(), user)
        && !isFieldAuthorizedForUser(entry.getValue().getName(), user));
    }
  }
}
