/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifReader;
import org.apache.directory.api.ldap.model.message.ModifyRequestImpl;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.model.schema.registries.SchemaLoader;
import org.apache.directory.api.ldap.schema.extractor.SchemaLdifExtractor;
import org.apache.directory.api.ldap.schema.extractor.impl.DefaultSchemaLdifExtractor;
import org.apache.directory.api.ldap.schema.loader.LdifSchemaLoader;
import org.apache.directory.api.ldap.schema.manager.impl.DefaultSchemaManager;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.api.CacheService;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.DnFactory;
import org.apache.directory.server.core.api.InstanceLayout;
import org.apache.directory.server.core.api.schema.SchemaPartition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.server.xdbm.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME this should be moved to a more meaningful maven module
/**
 * ApacheDS.
 * 
 * @see <a href="https://directory.apache.org/apacheds/">https://directory.
 *      apache.org/apacheds/</a>
 */
public class ApacheDS {
  /** Class logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ApacheDS.class);

  /** RODA instance name. */
  private static final String INSTANCE_NAME = "RODA";

  /** The directory service. */
  private DirectoryService service;

  /** The LDAP server. */
  private LdapServer server;

  /**
   * Add a new partition to the server.
   *
   * @param partitionId
   *          The partition Id
   * @param partitionDn
   *          The partition DN
   * @param dnFactory
   *          the DN factory
   * @return The newly added partition
   * @throws Exception
   *           If the partition can't be added
   */
  private JdbmPartition addPartition(final String partitionId, final String partitionDn, final DnFactory dnFactory)
    throws Exception {
    // Create a new partition with the given partition id
    final JdbmPartition partition = new JdbmPartition(service.getSchemaManager(), dnFactory);
    partition.setId(partitionId);
    partition
      .setPartitionPath(new File(service.getInstanceLayout().getPartitionsDirectory(), partitionId).toURI());
    partition.setSuffixDn(new Dn(partitionDn));
    service.addPartition(partition);

    return partition;
  }

  /**
   * Add a new set of index on the given attributes.
   *
   * @param partition
   *          The partition on which we want to add index
   * @param attrs
   *          The list of attributes to index
   */
  private void addIndex(final JdbmPartition partition, final String... attrs) {
    // Index some attributes on the apache partition
    final Set<Index<?, String>> indexedAttributes = new HashSet<>();

    for (String attribute : attrs) {
      indexedAttributes.add(new JdbmIndex<String>(attribute, false));
    }

    partition.setIndexedAttributes(indexedAttributes);
  }

  /**
   * Initialize the schema manager and add the schema partition to directory
   * service.
   *
   * @throws Exception
   *           if the schema LDIF files are not found on the classpath
   */
  private void initSchemaPartition() throws Exception {
    final InstanceLayout instanceLayout = service.getInstanceLayout();

    final File schemaPartitionDirectory = new File(instanceLayout.getPartitionsDirectory(), "schema");

    // Extract the schema on disk (a brand new one) and load the registries
    if (!schemaPartitionDirectory.exists()) {
      final SchemaLdifExtractor extractor = new DefaultSchemaLdifExtractor(instanceLayout.getPartitionsDirectory());
      extractor.extractOrCopy();
    }

    final SchemaLoader loader = new LdifSchemaLoader(schemaPartitionDirectory);
    final SchemaManager schemaManager = new DefaultSchemaManager(loader);

    // We have to load the schema now, otherwise we won't be able
    // to initialize the Partitions, as we won't be able to parse
    // and normalize their suffix Dn
    schemaManager.loadAllEnabled();

    final List<Throwable> errors = schemaManager.getErrors();

    if (!errors.isEmpty()) {
      throw new LdapUtilityException("Error while loading apacheds schemas");
    }

    service.setSchemaManager(schemaManager);

    // Init the LdifPartition with schema
    final LdifPartition schemaLdifPartition = new LdifPartition(schemaManager, service.getDnFactory());
    schemaLdifPartition.setPartitionPath(schemaPartitionDirectory.toURI());

    // The schema partition
    final SchemaPartition schemaPartition = new SchemaPartition(schemaManager);
    schemaPartition.setWrappedPartition(schemaLdifPartition);
    service.setSchemaPartition(schemaPartition);
  }

  /**
   * Initialize the server. It creates the partition, adds the index, and
   * injects the context entries for the created partitions.
   *
   * @param dataDirectory
   *          the directory to be used for storing the data
   * @param adminDN
   *          admin DN.
   * @param adminPassword
   *          the admin password to be set in the first time the server is
   *          started
   * @param ldapBaseDN
   *          LDAP base DN.
   * @param ldifs
   *          LDIF files to apply to Directory Service.
   * @throws Exception
   *           if there were some problems while initializing the system
   */
  public void initDirectoryService(final Path dataDirectory, final String adminDN, final String adminPassword,
    final String ldapBaseDN, final List<String> ldifs) throws Exception {
    // Initialize the LDAP service
    final JdbmPartition rodaPartition = instantiateDirectoryService(dataDirectory, ldapBaseDN);

    // Inject the context entry for dc=roda,dc=org partition
    if (!service.getAdminSession().exists(rodaPartition.getSuffixDn())) {
      final Dn dnApache = new Dn(ldapBaseDN);
      final Entry entryRoda = service.newEntry(dnApache);
      // @checkstyle MultipleStringLiterals (1 line)
      entryRoda.add("objectClass", "top", "domain", "extensibleObject");
      entryRoda.add("dc", "roda");
      service.getAdminSession().add(entryRoda);

      // change nis attribute in order to make things like
      // "shadowinactive" work
      ModifyRequestImpl modifyRequestImpl = new ModifyRequestImpl();
      modifyRequestImpl.setName(new Dn("cn=nis,ou=schema"));
      modifyRequestImpl.replace("m-disabled", "FALSE");
      service.getAdminSession().modify(modifyRequestImpl);

      // change apacheds admin password
      modifyRequestImpl = new ModifyRequestImpl();
      modifyRequestImpl.setName(new Dn(adminDN));
      modifyRequestImpl.replace("userPassword", adminPassword);
      service.getAdminSession().modify(modifyRequestImpl);

      for (String ldif : ldifs) {
        applyLdif(ldif);
      }
    }
  }

  /**
   * Instantiate Directory Service.
   * 
   * @param dataDirectory
   *          the data directory.
   * @param ldapBaseDN
   *          LDAP base DN.
   * @return RODA {@link JdbmPartition}
   * @throws Exception
   *           if some error occurs.
   */
  public JdbmPartition instantiateDirectoryService(final Path dataDirectory, final String ldapBaseDN) throws Exception {
    this.service = new DefaultDirectoryService();
    service.setInstanceId(INSTANCE_NAME);
    service.setInstanceLayout(new InstanceLayout(dataDirectory.toFile()));

    final CacheService cacheService = new CacheService();
    cacheService.initialize(service.getInstanceLayout());

    service.setCacheService(cacheService);

    // first load the schema
    initSchemaPartition();

    // then the system partition
    // this is a MANDATORY partition
    // DO NOT add this via addPartition() method, trunk code complains about
    // duplicate partition
    // while initializing
    final JdbmPartition systemPartition = new JdbmPartition(service.getSchemaManager(),
      service.getDnFactory());
    systemPartition.setId("system");
    systemPartition.setPartitionPath(
      new File(service.getInstanceLayout().getPartitionsDirectory(), systemPartition.getId()).toURI());
    systemPartition.setSuffixDn(new Dn(ServerDNConstants.SYSTEM_DN));
    systemPartition.setSchemaManager(service.getSchemaManager());

    // mandatory to call this method to set the system partition
    // Note: this system partition might be removed from trunk
    service.setSystemPartition(systemPartition);

    // Disable the ChangeLog system
    service.getChangeLog().setEnabled(false);
    service.setDenormalizeOpAttrsEnabled(true);

    // Now we can create as many partitions as we need
    final JdbmPartition rodaPartition = addPartition(INSTANCE_NAME, ldapBaseDN, service.getDnFactory());

    // Index some attributes on the apache partition
    addIndex(rodaPartition, "objectClass", "ou", "uid");

    // And start the service
    service.startup();

    return rodaPartition;
  }

  /**
   * Stop {@link LdapServer} and shutdown {@link DirectoryService}.
   * 
   * @throws Exception
   *           if some error occurs.
   */
  public void stop() throws Exception {

    if (!server.isStarted()) {
      throw new IllegalStateException("Service is not running");
    }

    server.stop();
    service.shutdown();
  }

  /**
   * Starts the LdapServer.
   * 
   * @param ldapPort
   *          The port where LDAP should bind.
   * @throws Exception
   *           if some error occurs.
   */
  public void startServer(final int ldapPort) throws Exception {

    this.server = new LdapServer();
    server.setTransports(new TcpTransport(ldapPort));
    server.setDirectoryService(service);

    server.start();
  }

  /**
   * Apply LDIF text.
   * 
   * @param ldif
   *          LDIF text.
   * @throws LdapException
   *           if some LDAP related error occurs.
   * @throws IOException
   *           if stream could not be closed.
   */
  private void applyLdif(final String ldif) throws LdapException, IOException {
    try (LdifReader entries = new LdifReader(new StringReader(ldif))) {
      for (LdifEntry ldifEntry : entries) {
        final DefaultEntry newEntry = new DefaultEntry(service.getSchemaManager(), ldifEntry.getEntry());
        LOGGER.debug("LDIF entry: {}", newEntry);
        service.getAdminSession().add(newEntry);
      }
    }
  }

}
