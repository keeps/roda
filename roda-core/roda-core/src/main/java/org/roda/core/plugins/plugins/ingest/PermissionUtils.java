/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.XMLUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.model.ModelService;
import org.roda.core.storage.Binary;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 20-07-2016.
 */
public class PermissionUtils {

  public static Permissions grantReadPermissionToUserGroup(ModelService model, AIP aip, Permissions permissions)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException, IOException {
    List<DescriptiveMetadata> descriptiveMetadataList = aip.getDescriptiveMetadata();
    Set<Permissions.PermissionType> readPermissionToUserGroup = new HashSet<>();

    for (DescriptiveMetadata descriptiveMetadata : descriptiveMetadataList) {
      Binary descriptiveMetadataBinary = model.retrieveDescriptiveMetadataBinary(aip.getId(),
        descriptiveMetadata.getId());
      InputStream createInputStream = descriptiveMetadataBinary.getContent().createInputStream();
      String xpath = RodaCoreFactory.getRodaConfigurationAsString("core", "permissions", "xpath");
      String freeAccessTerm = RodaCoreFactory.getRodaConfigurationAsString("core", "permissions", "freeaccess");

      String useRestrict = XMLUtility.getString(createInputStream, xpath);
      if (useRestrict.equals(freeAccessTerm)) {
        readPermissionToUserGroup.add(Permissions.PermissionType.READ);
        permissions.setGroupPermissions(RodaConstants.OBJECT_PERMISSIONS_USER_GROUP, readPermissionToUserGroup);
      }
    }

    return permissions;
  }

  public static Permissions grantAllPermissions(String username, Permissions permissions, Permissions parentPermissions)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    Permissions grantedPermissions = grantPermissionToUser(username, permissions);

    for (String name : parentPermissions.getUsernames()) {
      grantedPermissions.setUserPermissions(name, parentPermissions.getUserPermissions(name));
    }

    for (String name : parentPermissions.getGroupnames()) {
      grantedPermissions.setGroupPermissions(name, parentPermissions.getGroupPermissions(name));
    }

    return grantedPermissions;
  }

  private static Permissions grantPermissionToUser(String username, Permissions permissions)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    Set<Permissions.PermissionType> allPermissions = Stream
      .of(Permissions.PermissionType.CREATE, Permissions.PermissionType.DELETE, Permissions.PermissionType.GRANT,
        Permissions.PermissionType.READ, Permissions.PermissionType.UPDATE)
      .collect(Collectors.toSet());
    permissions.setUserPermissions(username, allPermissions);
    return permissions;
  }
}
