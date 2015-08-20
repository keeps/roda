package pt.gov.dgarq.roda.ds;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.Configuration;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
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
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.common.UserUtility;

public class DSStartStopListener implements ServletContextListener {
	// First while the server is up and running log into as admin
	// (uid=admin,ou=system) using the default password 'secret' and bind to
	// ou=system
	private static final Logger LOGGER = Logger.getLogger(DSStartStopListener.class);
	private static final String APACHE_DS_DEFAULT_HOST = "localhost";
	private static final int APACHE_DS_DEFAULT_PORT = 10389;
	private static Path RODA_APACHE_DS_DATA_DIRECTORY = null;
	private static Path RODA_APACHE_DS_CONFIG_DIRECTORY = null;
	private static final String INSTANCE_NAME = "RODA";
	private static final String BASE_DN = "dc=roda,dc=org";

	/** The directory service */
	private DirectoryService service;

	/** The LDAP server */
	private LdapServer server;

	/**
	 * Add a new partition to the server
	 *
	 * @param partitionId
	 *            The partition Id
	 * @param partitionDn
	 *            The partition DN
	 * @param dnFactory
	 *            the DN factory
	 * @return The newly added partition
	 * @throws Exception
	 *             If the partition can't be added
	 */
	private JdbmPartition addPartition(String partitionId, String partitionDn, DnFactory dnFactory) throws Exception {
		// Create a new partition with the given partition id
		JdbmPartition partition = new JdbmPartition(service.getSchemaManager(), dnFactory);
		partition.setId(partitionId);
		partition.setPartitionPath(new File(service.getInstanceLayout().getPartitionsDirectory(), partitionId).toURI());
		partition.setSuffixDn(new Dn(partitionDn));
		service.addPartition(partition);

		return partition;
	}

	/**
	 * Add a new set of index on the given attributes
	 *
	 * @param partition
	 *            The partition on which we want to add index
	 * @param attrs
	 *            The list of attributes to index
	 */
	private void addIndex(JdbmPartition partition, String... attrs) {
		// Index some attributes on the apache partition
		Set<Index<?, String>> indexedAttributes = new HashSet<Index<?, String>>();

		for (String attribute : attrs) {
			indexedAttributes.add(new JdbmIndex<String>(attribute, false));
		}

		partition.setIndexedAttributes(indexedAttributes);
	}

	/**
	 * initialize the schema manager and add the schema partition to diectory
	 * service
	 *
	 * @throws Exception
	 *             if the schema LDIF files are not found on the classpath
	 */
	private void initSchemaPartition() throws Exception {
		InstanceLayout instanceLayout = service.getInstanceLayout();

		File schemaPartitionDirectory = new File(instanceLayout.getPartitionsDirectory(), "schema");

		// Extract the schema on disk (a brand new one) and load the registries
		if (schemaPartitionDirectory.exists()) {
			System.out.println("schema partition already exists, skipping schema extraction");
		} else {
			SchemaLdifExtractor extractor = new DefaultSchemaLdifExtractor(instanceLayout.getPartitionsDirectory());
			extractor.extractOrCopy();
		}

		SchemaLoader loader = new LdifSchemaLoader(schemaPartitionDirectory);
		SchemaManager schemaManager = new DefaultSchemaManager(loader);

		// We have to load the schema now, otherwise we won't be able
		// to initialize the Partitions, as we won't be able to parse
		// and normalize their suffix Dn
		schemaManager.loadAllEnabled();

		List<Throwable> errors = schemaManager.getErrors();

		if (errors.size() != 0) {
			throw new Exception("");
		}

		service.setSchemaManager(schemaManager);

		// Init the LdifPartition with schema
		LdifPartition schemaLdifPartition = new LdifPartition(schemaManager, service.getDnFactory());
		schemaLdifPartition.setPartitionPath(schemaPartitionDirectory.toURI());

		// The schema partition
		SchemaPartition schemaPartition = new SchemaPartition(schemaManager);
		schemaPartition.setWrappedPartition(schemaLdifPartition);
		service.setSchemaPartition(schemaPartition);
	}

	/**
	 * Initialize the server. It creates the partition, adds the index, and
	 * injects the context entries for the created partitions.
	 *
	 * @param workDir
	 *            the directory to be used for storing the data
	 * @throws Exception
	 *             if there were some problems while initializing the system
	 */
	private void initDirectoryService(File workDir) throws Exception {
		// Initialize the LDAP service
		service = new DefaultDirectoryService();
		service.setInstanceLayout(new InstanceLayout(workDir));

		CacheService cacheService = new CacheService();
		cacheService.initialize(service.getInstanceLayout());

		service.setCacheService(cacheService);

		// first load the schema
		initSchemaPartition();

		// then the system partition
		// this is a MANDATORY partition
		// DO NOT add this via addPartition() method, trunk code complains about
		// duplicate partition
		// while initializing
		JdbmPartition systemPartition = new JdbmPartition(service.getSchemaManager(), service.getDnFactory());
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
		JdbmPartition rodaPartition = addPartition(INSTANCE_NAME, BASE_DN, service.getDnFactory());

		// Index some attributes on the apache partition
		addIndex(rodaPartition, "objectClass", "ou", "uid");

		// And start the service
		service.startup();

		// Inject the context entry for dc=roda,dc=org partition
		if (!service.getAdminSession().exists(rodaPartition.getSuffixDn())) {
			Dn dnApache = new Dn(BASE_DN);
			Entry entryApache = service.newEntry(dnApache);
			entryApache.add("objectClass", "top", "domain", "extensibleObject");
			entryApache.add("dc", "roda");
			service.getAdminSession().add(entryApache);

			// change attribute in order to make things like "shadowinactive" work
			ModifyRequestImpl modifyRequestImpl = new ModifyRequestImpl();
			modifyRequestImpl.setName(new Dn("cn=nis,ou=schema"));
			modifyRequestImpl.replace("m-disabled", "FALSE");
			service.getAdminSession().modify(modifyRequestImpl);

			applyLdif(RODA_APACHE_DS_CONFIG_DIRECTORY.resolve("users.ldif").toFile());
			applyLdif(RODA_APACHE_DS_CONFIG_DIRECTORY.resolve("groups.ldif").toFile());
			applyLdif(RODA_APACHE_DS_CONFIG_DIRECTORY.resolve("roles.ldif").toFile());
		}

		UserUtility.setDirectoryService(service);
	}

	public void stop() throws Exception {

		if (!server.isStarted()) {
			throw new IllegalStateException("Service is not running");
		}

		server.stop();
		service.shutdown();
	}

	/**
	 * starts the LdapServer
	 *
	 * @throws Exception
	 */
	public void startServer() throws Exception {
		// FIXME these configurations should be relocated, e.g. to
		// roda-ui.properties
		Configuration rodaConfig = RodaCoreFactory.getConfiguration("roda-core.properties");

		String ldapHost = rodaConfig.getString("ldapHost", APACHE_DS_DEFAULT_HOST);
		// int ldapPort = rodaConfig.getInt("ldapPort", APACHE_DS_DEFAULT_PORT);
		int ldapPort = APACHE_DS_DEFAULT_PORT;
		String ldapPeopleDN = rodaConfig.getString("ldapPeopleDN");
		String ldapGroupsDN = rodaConfig.getString("ldapGroupsDN");
		String ldapRolesDN = rodaConfig.getString("ldapRolesDN");
		String ldapAdminDN = rodaConfig.getString("ldapAdminDN");
		String ldapAdminPassword = rodaConfig.getString("ldapAdminPassword");
		String ldapPasswordDigestAlgorithm = rodaConfig.getString("ldapPasswordDigestAlgorithm");
		List<String> ldapProtectedUsers = rodaConfig.getList("ldapProtectedUsers");
		List<String> ldapProtectedGroups = rodaConfig.getList("ldapProtectedGroups");

		LdapUtility ldapUtility = new LdapUtility(ldapHost, ldapPort, ldapPeopleDN, ldapGroupsDN, ldapRolesDN,
				ldapAdminDN, ldapAdminPassword, ldapPasswordDigestAlgorithm, ldapProtectedUsers, ldapProtectedGroups);

		UserUtility.setLdapUtility(ldapUtility);

		server = new LdapServer();
		server.setTransports(new TcpTransport(ldapPort));
		server.setDirectoryService(service);

		server.start();
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		server.stop();
	}

	public void applyLdif(final File ldifFile) throws Exception {
		LdifReader entries = new LdifReader(new FileInputStream(ldifFile));
		for (LdifEntry ldifEntry : entries) {
			DefaultEntry newEntry = new DefaultEntry(service.getSchemaManager(), ldifEntry.getEntry());
			LOGGER.debug("ldif entry: " + newEntry);
			service.getAdminSession().add(newEntry);
		}
		entries.close();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		RODA_APACHE_DS_DATA_DIRECTORY = RodaCoreFactory.getDataPath().resolve("ldap");
		RODA_APACHE_DS_CONFIG_DIRECTORY = RodaCoreFactory.getConfigPath().resolve("ldap");

		try {
			if (!Files.exists(RODA_APACHE_DS_DATA_DIRECTORY)) {
				Files.createDirectories(RODA_APACHE_DS_DATA_DIRECTORY);
				initDirectoryService(RODA_APACHE_DS_DATA_DIRECTORY.toFile());
			}
			startServer();
		} catch (Exception e) {
			LOGGER.error("Error starting up embedded ApacheDS", e);
		}

	}
}