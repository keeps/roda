package org.roda.wui.api.v2.filters;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.PropertyWriter;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.disposal.metadata.DisposalAIPMetadata;
import org.roda.core.data.v2.disposal.metadata.DisposalConfirmationAIPMetadata;
import org.roda.core.data.v2.disposal.metadata.DisposalScheduleAIPMetadata;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.HasPermissions;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AipPermissionPropertyFilter extends SimpleBeanPropertyFilter {

  private static final Set<String> INDEXED_DISPOSAL_SCHEDULE_FIELDS = Set.of("disposalAction", "disposalScheduleId",
    "disposalScheduleName", "retentionPeriodDetails", "retentionPeriodDuration", "retentionPeriodInterval",
    "retentionPeriodState", "retentionPeriodStartDate", "scheduleAssociationType", "overdueDate");

  private static final Set<String> INDEXED_DISPOSAL_HOLDS_FIELDS = Set.of("transitiveDisposalHoldsId",
    "disposalHoldsId", "onHold");

  private final User user;

  public AipPermissionPropertyFilter(User user) {
    this.user = user;
  }

  @Override
  public void serializeAsProperty(Object pojo, JsonGenerator jgen, SerializationContext ctxt, PropertyWriter writer)
    throws Exception {

    // 1. Check if top-level properties should be omitted entirely
    if (!include(writer)) {
      if (!jgen.canOmitProperties()) {
        writer.serializeAsOmittedProperty(pojo, jgen, ctxt);
      }
      return;
    }

    String propName = writer.getName();

    // 2. INTERCEPT BOOLEANS: Prevent client-side NPEs by sending false instead of
    // null
    if ("hasRepresentations".equals(propName) && pojo instanceof IndexedAIP) {
      if (!UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_FIND_REPRESENTATION)) {
        jgen.writeName(propName);
        ctxt.writeValue(jgen, Boolean.FALSE);
        return;
      }
    }

    // 3. INTERCEPT PERMISSIONS
    if ("permissions".equals(propName) && pojo instanceof HasPermissions hasPermsObj) {
      if (!UserUtility.hasPermissions(user, "org.roda.wui.api.controllers.Browser.verifyPermissions")) {
        Permissions sanitizedPerms = sanitizePermissions(hasPermsObj.getPermissions(), user);
        jgen.writeName(propName);
        ctxt.writeValue(jgen, sanitizedPerms);
        return;
      }
    }

    // 4. INTERCEPT NESTED DISPOSAL (AIP model)
    if ("disposal".equals(propName) && pojo instanceof AIP aip) {
      if (needsDisposalSanitization()) {
        DisposalAIPMetadata sanitizedDisposal = sanitizeDisposal(aip.getDisposal(), user);
        jgen.writeName(propName);
        ctxt.writeValue(jgen, sanitizedDisposal);
        return;
      }
    }

    // 5. INTERCEPT INDEXED FIELDS MAP: Close the data leak inside
    // IndexedAIP.fields!
    if ("fields".equals(propName) && pojo instanceof IsIndexed indexedObj) {
      Map<String, Object> sanitizedFields = sanitizeIndexedFields(indexedObj.getFields(), user);
      jgen.writeName(propName);
      ctxt.writeValue(jgen, sanitizedFields);
      return;
    }

    // 6. Default behavior for all authorized properties
    writer.serializeAsProperty(pojo, jgen, ctxt);
  }

  @Override
  protected boolean include(PropertyWriter writer) {
    String fieldName = writer.getName();

    // --- Top-Level Checks for AIP ---
    if ("descriptiveMetadata".equals(fieldName)) {
      return UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_LIST_AIP_DESCRIPTIVE_METADATA);
    }
    if ("representations".equals(fieldName)) {
      return UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_FIND_REPRESENTATION);
    }

    // --- Top-Level Checks for IndexedAIP ---
    // Notice: hasRepresentations was removed from here so step 2 can sanitize it to
    // false!
    if (INDEXED_DISPOSAL_SCHEDULE_FIELDS.contains(fieldName)) {
      return UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_SCHEDULES);
    }
    if (INDEXED_DISPOSAL_HOLDS_FIELDS.contains(fieldName)) {
      return UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_HOLDS);
    }
    if ("disposalConfirmationId".equals(fieldName)) {
      return UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_FIND_DISPOSAL_CONFIRMATION);
    }

    return true;
  }

  /**
   * Prevents security leaks by removing restricted keys from the Lucene/Solr
   * fields map.
   */
  private Map<String, Object> sanitizeIndexedFields(Map<String, Object> originalFields, User user) {
    if (originalFields == null) {
      return null;
    }

    Map<String, Object> copy = new HashMap<>(originalFields);

    // Sanitize representations flag inside fields map
    if (!UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_FIND_REPRESENTATION)) {
      copy.put("hasRepresentations", Boolean.FALSE);
    }

    // Sanitize disposal schedules inside fields map
    if (!UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_SCHEDULES)) {
      INDEXED_DISPOSAL_SCHEDULE_FIELDS.forEach(copy::remove);
    }

    // Sanitize disposal holds inside fields map
    if (!UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_HOLDS)) {
      INDEXED_DISPOSAL_HOLDS_FIELDS.forEach(copy::remove);
    }

    // Sanitize disposal confirmation inside fields map
    if (!UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_FIND_DISPOSAL_CONFIRMATION)) {
      copy.remove("disposalConfirmationId");
    }

    // Sanitize raw permission strings inside fields map (e.g.,
    // permission_users_READ)
    if (!UserUtility.hasPermissions(user, "org.roda.wui.api.controllers.Browser.verifyPermissions")) {
      copy.keySet().removeIf(key -> key.startsWith("permission_"));
    }

    return copy;
  }

  private boolean needsDisposalSanitization() {
    return !UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_SCHEDULES)
      || !UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_HOLDS)
      || !UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_FIND_DISPOSAL_CONFIRMATION);
  }

  private DisposalAIPMetadata sanitizeDisposal(DisposalAIPMetadata original, User user) {
    if (original == null)
      return null;
    DisposalAIPMetadata copy = new DisposalAIPMetadata();

    if (UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_SCHEDULES)) {
      copy.setSchedule(original.getSchedule());
    } else {
      copy.setSchedule(new DisposalScheduleAIPMetadata());
    }

    if (UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_HOLDS)) {
      copy.setHolds(original.getHolds());
      copy.setTransitiveHolds(original.getTransitiveHolds());
    } else {
      copy.setHolds(List.of());
      copy.setTransitiveHolds(List.of());
    }

    if (UserUtility.hasPermissions(user, RodaConstants.PERMISSION_METHOD_FIND_DISPOSAL_CONFIRMATION)) {
      copy.setConfirmation(original.getConfirmation());
    } else {
      copy.setConfirmation(new DisposalConfirmationAIPMetadata());
    }
    return copy;
  }

  private Permissions sanitizePermissions(Permissions original, User user) {
    if (original == null)
      return null;
    Permissions copy = new Permissions();
    copy.setGroups(Map.of());

    Map<Permissions.PermissionType, Set<String>> filteredUsers = new EnumMap<>(Permissions.PermissionType.class);
    if (original.getUsers() != null) {
      original.getUsers().forEach((key, userIds) -> {
        if (userIds != null) {
          Set<String> safeUserIds = userIds.stream().filter(id -> id.equals(user.getId())).collect(Collectors.toSet());
          if (!safeUserIds.isEmpty()) {
            filteredUsers.put(key, safeUserIds);
          }
        }
      });
    }
    copy.setUsers(filteredUsers);
    return copy;
  }
}