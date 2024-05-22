/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.ingest;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 20-07-2016.
 */
public class PermissionUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(PermissionUtils.class);

  private PermissionUtils() {
    // do nothing
  }

  public static Permissions calculatePermissions(User user, Optional<Permissions> inheritedPermissions)
    throws GenericException {

    return calculatePermissions(user, inheritedPermissions, Optional.empty());

  }

  public static Permissions calculatePermissions(User user, Optional<Permissions> inheritedPermissions, Optional<Permissions> otherPermissions)
    throws GenericException {

    Permissions finalPermissions = new Permissions();

    Set<String> creatorGroups = user.getGroups();

    String creatorUsername = user.getId();

    // get inherited permissions
    if (inheritedPermissions.isPresent()) {

      for (String name : inheritedPermissions.get().getUsernames()) {
        finalPermissions.setUserPermissions(name, inheritedPermissions.get().getUserPermissions(name));
      }
      for (String name : inheritedPermissions.get().getGroupnames()) {
        finalPermissions.setGroupPermissions(name, inheritedPermissions.get().getGroupPermissions(name));
      }

    }

    // SET administrators permissions
    List<String> adminGroups = RodaCoreFactory.getRodaConfigurationAsList("core.aip.default_permissions.admin.group[]");

    for (String name : adminGroups) {
      finalPermissions.setGroupPermissions(name,
        RodaCoreFactory
          .getRodaConfigurationAsList("core.aip.default_permissions.admin.group[]." + name + ".permission[]").stream()
          .map(Permissions.PermissionType::valueOf) // Convert string to PermissionType enum
          .collect(Collectors.toSet()));
    }

    List<String> adminUser = RodaCoreFactory.getRodaConfigurationAsList("core.aip.default_permissions.admin.user[]");

    for (String name : adminUser) {
      finalPermissions.setUserPermissions(name,
        RodaCoreFactory
          .getRodaConfigurationAsList("core.aip.default_permissions.admin.user[]." + name + ".permission[]").stream()
          .map(Permissions.PermissionType::valueOf) // Convert string to PermissionType enum
          .collect(Collectors.toSet()));
    }

    // creator Permissions
    Set<Permissions.PermissionType> userCreatorPermissions = RodaCoreFactory
      .getRodaConfigurationAsList("core.aip.default_permissions.creator.user.permission[]").stream()
      .map(Permissions.PermissionType::valueOf) // Convert string to PermissionType enum
      .collect(Collectors.toSet());

    // add default creator permissions
    Set<Permissions.PermissionType> tempUserPermissions = finalPermissions.getUserPermissions(creatorUsername);
    tempUserPermissions.addAll(userCreatorPermissions);
    finalPermissions.setUserPermissions(creatorUsername, tempUserPermissions);

    // default legacy behaviour
    boolean getLegacyPermissions = RodaCoreFactory.getProperty("core.aip.default_permissions.legacy_permissions", true);

    if (getLegacyPermissions) {
      Set<Permissions.PermissionType> defaultCreatorPermissions = RodaCoreFactory
        .getRodaConfigurationAsList("core.aip.default_permissions.creator.permission[]").stream()
        .map(Permissions.PermissionType::valueOf) // Convert string to PermissionType enum
        .collect(Collectors.toSet());

      // add default creator permissions
      Set<Permissions.PermissionType> temp = finalPermissions.getUserPermissions(creatorUsername);
      temp.addAll(defaultCreatorPermissions);
      finalPermissions.setUserPermissions(creatorUsername, temp);
    }

    // defaultPermissions
    Permissions defaultPermissions = new Permissions();

    // add default users
    List<String> defaultUsers = RodaCoreFactory.getRodaConfigurationAsList("core.aip.default_permissions.users[]");

    for (String name : defaultUsers) {
      defaultPermissions.setUserPermissions(name,
        RodaCoreFactory.getRodaConfigurationAsList("core.aip.default_permissions.users[]." + name + ".permission[]")
          .stream().map(Permissions.PermissionType::valueOf) // Convert string to PermissionType enum
          .collect(Collectors.toSet()));
    }

    // add default groups
    List<String> defaultGroups = RodaCoreFactory.getRodaConfigurationAsList("core.aip.default_permissions.group[]");

    for (String name : defaultGroups) {
      defaultPermissions.setGroupPermissions(name,
        RodaCoreFactory.getRodaConfigurationAsList("core.aip.default_permissions.group[]." + name + ".permission[]")
          .stream().map(Permissions.PermissionType::valueOf) // Convert string to PermissionType enum
          .collect(Collectors.toSet()));
    }

    // add default User permissions
    for (String name : defaultPermissions.getUsernames()) {
      Set<Permissions.PermissionType> temp = finalPermissions.getUserPermissions(name);
      temp.addAll(defaultPermissions.getUserPermissions(name));
      finalPermissions.setUserPermissions(name, temp);
    }


    // intersection
    boolean intersection = RodaCoreFactory.getProperty("core.aip.default_permissions.intersect_groups", false);

    // configuration
    if (intersection) {
      // add default groups intercepted with creator groups
      Set<String> interceptedGroups = new HashSet<>(defaultPermissions.getGroupnames());
      // intercept creator groups with config groups
      interceptedGroups.retainAll(creatorGroups);

      for (String name : interceptedGroups) {
        Set<Permissions.PermissionType> tempGroups = finalPermissions.getGroupPermissions(name);
        tempGroups.addAll(defaultPermissions.getGroupPermissions(name));
        finalPermissions.setGroupPermissions(name, tempGroups);
      }

    } else {
      // add default groups without interception
      for (String name : defaultPermissions.getGroupnames()) {
        Set<Permissions.PermissionType> tempGroups = finalPermissions.getGroupPermissions(name);
        tempGroups.addAll(defaultPermissions.getGroupPermissions(name));
        finalPermissions.setGroupPermissions(name, tempGroups);
      }
    }

    // add otherPermissions to final permissions
    if (otherPermissions.isPresent()) {
      //add otherPermissions user permissions
      for (String name : otherPermissions.get().getUsernames()) {
        Set<Permissions.PermissionType> tempPermissions = finalPermissions.getUserPermissions(name);
        tempPermissions.addAll(otherPermissions.get().getUserPermissions(name));
        finalPermissions.setUserPermissions(name, tempPermissions);
      }

      //add otherPermissions user permissions
      for (String name : otherPermissions.get().getGroupnames()) {
        Set<Permissions.PermissionType> tempPermissions = finalPermissions.getGroupPermissions(name);
        tempPermissions.addAll(otherPermissions.get().getGroupPermissions(name));
        finalPermissions.setGroupPermissions(name, tempPermissions);
      }
    }

    // check if user has must have permissions to create the aip or the sublevel aip
    Set<Permissions.PermissionType> mustHavePermissions = RodaCoreFactory
      .getRodaConfigurationAsList("core.aip.default_permissions.creator.minimum.permissions[]").stream()
      .map(Permissions.PermissionType::valueOf).collect(Collectors.toSet());

    Set<Permissions.PermissionType> creatorPermissions = creatorGroups.stream()
      .map(g -> finalPermissions.getGroupPermissions(g)).flatMap(Set::stream).collect(Collectors.toSet());
    creatorPermissions.addAll(finalPermissions.getUserPermissions(creatorUsername));

    if(mustHavePermissions.isEmpty()) {
      LOGGER.error("Minimum set of permissions is empty!");
      throw new GenericException("Configuration issue, please contact administrator");
    } else if (!creatorPermissions.containsAll(mustHavePermissions)) {
      mustHavePermissions.addAll(creatorPermissions);
      finalPermissions.setUserPermissions(creatorUsername, mustHavePermissions);
    }

    LOGGER.warn("Permissions have been set");

    return finalPermissions;

  }

}
