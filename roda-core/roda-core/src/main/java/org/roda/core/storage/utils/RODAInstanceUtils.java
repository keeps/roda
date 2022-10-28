/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage.utils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.YamlUtils;
import org.roda.core.data.v2.index.filter.AndFiltersParameters;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.synchronization.SynchronizingStatus;
import org.roda.core.data.v2.synchronization.central.DistributedInstances;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class RODAInstanceUtils {

  public static boolean isConfiguredAsDistributedMode() {
    return !RodaCoreFactory.getDistributedModeType().equals(RodaConstants.DistributedModeType.BASE);
  }

  private static Path getLocalInstanceConfigFile() {
    return RodaCoreFactory.getConfigPath().resolve(RodaConstants.SYNCHRONIZATION_CONFIG_LOCAL_INSTANCE_FOLDER)
      .resolve(RodaConstants.SYNCHRONIZATION_CONFIG_LOCAL_INSTANCE_FILE);
  }

  public static String getLocalInstanceIdentifier() {
    try {
      Path configFile = getLocalInstanceConfigFile();
      LocalInstance localInstance = YamlUtils.readObjectFromFile(configFile, LocalInstance.class);
      if (localInstance.getStatus().equals(SynchronizingStatus.INACTIVE)) {
        return null;
      } else {
        return localInstance.getId();
      }
    } catch (GenericException e) {
      return null;
    }
  }

  public static String retrieveLocalInstanceIdentifierToPlugin() {
    try {
      Path configFile = getLocalInstanceConfigFile();
      LocalInstance localInstance = YamlUtils.readObjectFromFile(configFile, LocalInstance.class);
      return localInstance.getId();
    } catch (GenericException e) {
      return null;
    }
  }

  public static void createDistributedGroup(User user) throws GenericException, AuthorizationDeniedException {
    ModelService model = RodaCoreFactory.getModelService();
    String groupName = RodaConstants.DISTRIBUTED_INSTANCE_GROUP_NAME;

    try {
      model.retrieveGroup(groupName);
    } catch (NotFoundException e) {
      Set<String> roles = new HashSet<>();
      roles.add(RodaConstants.REPOSITORY_PERMISSIONS_AIP_READ);
      roles.add(RodaConstants.REPOSITORY_PERMISSIONS_REPRESENTATION_READ);
      roles.add(RodaConstants.REPOSITORY_PERMISSIONS_DESCRIPTIVE_METADATA_READ);
      roles.add(RodaConstants.REPOSITORY_PERMISSIONS_PRESERVATION_METADATA_READ);
      roles.add(RodaConstants.REPOSITORY_PERMISSIONS_JOB_READ);
      roles.add(RodaConstants.REPOSITORY_PERMISSIONS_JOB_MANAGE);
      roles.add(RodaConstants.REPOSITORY_PERMISSIONS_RISK_READ);
      roles.add(RodaConstants.REPOSITORY_PERMISSIONS_REPRESENTATION_INFORMATION_READ);
      roles.add(RodaConstants.REPOSITORY_PERMISSIONS_DISTRIBUTED_INSTANCES_READ);
      roles.add(RodaConstants.REPOSITORY_PERMISSIONS_LOCAL_INSTANCES_READ);

      Group group = new Group(groupName);
      group.setFullName(groupName);
      group.setUsers(new HashSet<>(Arrays.asList(user.getId())));
      group.setActive(true);
      group.setAllRoles(roles);
      group.setDirectRoles(roles);
      try {
        model.createGroup(group, true);
      } catch (AlreadyExistsException alreadyExistsException) {
        // do nothing
      }
    }
  }

  public static void addLocalInstanceFilter(final Filter filter)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, IOException {

    if (RodaConstants.DistributedModeType.CENTRAL.equals(RodaCoreFactory.getDistributedModeType())) {

      DistributedInstances distributedInstances = RodaCoreFactory.getModelService().listDistributedInstances();
      List<String> distributedInstanceIds = distributedInstances.getObjects().stream()
        .map(distributedInstance -> distributedInstance.getId()).collect(Collectors.toList());

      List<FilterParameter> instanceParams = new ArrayList<>();

      for (String instanceId : distributedInstanceIds) {
        instanceParams.add(new NotSimpleFilterParameter(RodaConstants.INDEX_INSTANCE_ID, instanceId));
      }

      filter.add(new AndFiltersParameters(instanceParams));
    }
  }
}
