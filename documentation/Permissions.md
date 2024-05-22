# Default AIP permissions configuration

It is possible to change how AIP permissions are granted to users and groups when creating an AIP on the Web interface
or when creating it via ingest. To do so, the following configurations must be changed in the roda-core.properties file.

1. If we wish to add a group as a super group that has full permissions when creating aip's, we simply need add the
   following lines:
   ```properties
    # Admin users or groups so AIPs can be administered
    
    core.aip.default_permissions.admin.group[] = administrators
    
    core.aip.default_permissions.admin.group[].administrators.permission[] = READ
    core.aip.default_permissions.admin.group[].administrators.permission[] = UPDATE
    core.aip.default_permissions.admin.group[].administrators.permission[] = CREATE
    core.aip.default_permissions.admin.group[].administrators.permission[] = GRANT
    core.aip.default_permissions.admin.group[].administrators.permission[] = DELETE
    ```

   The lines above configure the administrators group. If we wanted to add a super user, for example, an admin user, we
   would do it like so:
    ```properties
    core.aip.default_permissions.admin.user[] = admin
   
    core.aip.default_permissions.admin.user[].admin.permission[] = READ
    core.aip.default_permissions.admin.user[].admin.permission[] = UPDATE
    core.aip.default_permissions.admin.user[].admin.permission[] = CREATE
    core.aip.default_permissions.admin.user[].admin.permission[] = GRANT
    core.aip.default_permissions.admin.user[].admin.permission[] = DELETE
    ```

   If we want to set permissions for the user that is creating the AIP, we do it like so:
    ```properties
    # Direct creator permissions
    core.aip.default_permissions.creator.user.permission[] = CREATE
    core.aip.default_permissions.creator.user.permission[] = UPDATE
    core.aip.default_permissions.creator.user.permission[] = READ
    ```

   If we want to set permissions for some specific user, simply add the following (example of setting permissions for
   user 'foo'):
    ```properties
    # Direct creator permissions
    core.aip.default_permissions.users[] = foo
    
    core.aip.default_permissions.users[].foo.permission[] = CREATE
    core.aip.default_permissions.users[].foo.permission[] = DELETE
    core.aip.default_permissions.users[].foo.permission[] = READ
    ```    

   Finally, we can also add other groups, other than super groups. These are normal groups, and do not have full
   permissions.
    ```properties
    # Additional group permissions
    core.aip.default_permissions.group[] = archivists
    core.aip.default_permissions.group[] = producers
    core.aip.default_permissions.group[] = guests
    
    core.aip.default_permissions.group[].archivists.permission[] = READ
    core.aip.default_permissions.group[].archivists.permission[] = UPDATE
    core.aip.default_permissions.group[].archivists.permission[] = CREATE
    
    core.aip.default_permissions.group[].producers.permission[] = READ
    
    core.aip.default_permissions.group[].guests.permission[] = READ
    ```

2. A requirement might be that the user that created the AIP must belong to one of the groups defined on the
   configurations file and it has READ and UPDATE permissions at least. If the intersection is empty, or generally, if
   the set permission ends up not providing the user with at least READ and UPDATE permissions to the created AIP, then
   these permissions are loaded as the minimum permissions from the config file.
   ```properties
    # Intersect creator groups with the configuration groups
    core.aip.default_permissions.intersect_groups = true

    # System expects a minimum set of direct or indirect permissions for the creator (DO NOT CHANGE THIS!)
    core.aip.default_permissions.creator.minimum.permissions[] = UPDATE
    core.aip.default_permissions.creator.minimum.permissions[] = READ
    ```

   By default, and to conform to legacy behaviour, the creator user is granted all permissions when creating an AIP. If
   you do not wish to use legacy permissions, set this property as follows:
    ```properties
    # Use legacy behaviour
    core.aip.default_permissions.legacy_permissions = false
    ```