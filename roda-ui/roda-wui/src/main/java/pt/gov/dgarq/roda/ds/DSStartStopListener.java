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

import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifReader;
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
import org.apache.directory.server.core.api.partition.Partition;
import org.apache.directory.server.core.api.schema.SchemaPartition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.RodaCoreFactory;

public class DSStartStopListener implements ServletContextListener {
	static final private Logger logger = Logger.getLogger(DSStartStopListener.class);
	// // First while the server is up and running log into as admin
	// // (uid=admin,ou=system) using the default password 'secret' and bind to
	// // ou=system
	private static final int APACHE_DS_DEFAULT_PORT = 10389;
	private static Path RODA_APACHE_DS_DATA_DIRECTORY = null;
	private static Path RODA_APACHE_DS_CONFIG_DIRECTORY = null;
	private static final String INSTANCE_NAME = "RODA";
	private static final String BASE_DN = "dc=roda,dc=org";

	private String host;
	private Integer port;

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
	private Partition addPartition(String partitionId, String partitionDn, DnFactory dnFactory) throws Exception {
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
	private void addIndex(Partition partition, String... attrs) {
		// Index some attributes on the apache partition
		Set indexedAttributes = new HashSet();

		for (String attribute : attrs) {
			indexedAttributes.add(new JdbmIndex(attribute, false));
		}

		((JdbmPartition) partition).setIndexedAttributes(indexedAttributes);
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
		// Create some new partitions named 'foo', 'bar' and 'apache'.
		Partition rodaPartition = addPartition(INSTANCE_NAME, BASE_DN, service.getDnFactory());

		// Index some attributes on the apache partition
		addIndex(rodaPartition, "objectClass", "ou", "uid");

		// And start the service
		service.startup();

		// Inject the context entry for dc=Apache,dc=Org partition
		if (!service.getAdminSession().exists(rodaPartition.getSuffixDn())) {
			Dn dnApache = new Dn(BASE_DN);
			Entry entryApache = service.newEntry(dnApache);
			entryApache.add("objectClass", "top", "domain", "extensibleObject");
			entryApache.add("dc", "roda");
			service.getAdminSession().add(entryApache);
			
			applyLdif(RODA_APACHE_DS_CONFIG_DIRECTORY.resolve("users.ldif").toFile());
			applyLdif(RODA_APACHE_DS_CONFIG_DIRECTORY.resolve("groups.ldif").toFile());
			applyLdif(RODA_APACHE_DS_CONFIG_DIRECTORY.resolve("roles.ldif").toFile());
		}

		// We are all done !
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
		server = new LdapServer();
		server.setTransports(new TcpTransport(port));
		server.setDirectoryService(service);

		server.start();
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {

	}

	public void applyLdif(final File ldifFile) throws Exception {
		LdifReader entries = new LdifReader(new FileInputStream(ldifFile));
		for (LdifEntry ldifEntry : entries) {
			DefaultEntry newEntry = new DefaultEntry(service.getSchemaManager(), ldifEntry.getEntry());
			service.getAdminSession().add(newEntry);
		}
		// new LdifFileLoader(service.getAdminSession(), ldifFile,
		// null).execute();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		host = "localhost";
		port = APACHE_DS_DEFAULT_PORT;
		RODA_APACHE_DS_DATA_DIRECTORY = RodaCoreFactory.getDataPath().resolve("ldap");
		RODA_APACHE_DS_CONFIG_DIRECTORY = RodaCoreFactory.getConfigPath().resolve("ldap");

		try {
			Files.createDirectories(RODA_APACHE_DS_DATA_DIRECTORY);
			initDirectoryService(RODA_APACHE_DS_DATA_DIRECTORY.toFile());
			startServer();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			e.printStackTrace();
		}

	}
}