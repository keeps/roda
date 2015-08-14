package pt.gov.dgarq.roda.ds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifReader;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.LdapComparator;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.model.schema.comparators.NormalizingComparator;
import org.apache.directory.api.ldap.model.schema.registries.ComparatorRegistry;
import org.apache.directory.api.ldap.model.schema.registries.SchemaLoader;
import org.apache.directory.api.ldap.schema.loader.LdifSchemaLoader;
import org.apache.directory.api.ldap.schema.manager.impl.DefaultSchemaManager;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.InstanceLayout;
import org.apache.directory.server.core.api.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.UserUtility;


public class DSStartStopListener implements ServletContextListener
{
	private static final Logger logger = Logger
			.getLogger(DSStartStopListener.class);
    private DirectoryService directoryService;
 
    private LdapServer ldapServer;
    
    public static File RODA_HOME = null;
    
    public static File RODA_APACHE_DS_DIRECTORY = null;
    
	public static File RODA_APACHE_DS_CONFIG_DIRECTORY = null;
	
	int dsPort;
 
    /**
     * Startup ApacheDS embedded.
     */
    public void contextInitialized( ServletContextEvent evt )
    {
        try
        {
        	
        	
        	
        	if (System.getProperty("roda.home") != null) {
				RODA_HOME = new File(System.getProperty("roda.home"));
			} else if (System.getenv("RODA_HOME") != null) {
				RODA_HOME = new File(System.getenv("RODA_HOME")); //$NON-NLS-1$
			} else {
				RODA_HOME = new File(".");
			}
        	 
        	File rodaDataDir = new File(RODA_HOME, "data");
        	RODA_APACHE_DS_DIRECTORY =  new File(rodaDataDir, "ldap");
        	
        	File rodaConfigDir = new File(RODA_HOME, "config");
        	RODA_APACHE_DS_CONFIG_DIRECTORY = new File(rodaConfigDir,"ldap"); //$NON-NLS-1$
        	
        	boolean initializeDS = false;
        	if(!RODA_APACHE_DS_DIRECTORY.exists()){
        		RODA_APACHE_DS_DIRECTORY.mkdirs();
        		initializeDS = true;
        	}
        	
        	File schemaRepository = new File( RODA_APACHE_DS_DIRECTORY, "schema" );
        	schemaRepository.mkdirs();
        	InputStream relsStream = getConfigurationFile("roda-wui.properties");
			Properties dsProperties = new Properties();
			dsProperties.load(relsStream);
			
			dsPort = (dsProperties.getProperty("ds.port")!=null)?Integer.parseInt(dsProperties.getProperty("ds.port")):10389;
			
            directoryService = new DefaultDirectoryService();
            directoryService.setShutdownHookEnabled( true );
            
            SchemaLoader loader = new LdifSchemaLoader( schemaRepository );
            SchemaManager schemaManager = new DefaultSchemaManager( loader );

            // We have to load the schema now, otherwise we won't be able
            // to initialize the Partitions, as we won't be able to parse
            // and normalize their suffix Dn
            schemaManager.loadAllEnabled();

            // Tell all the normalizer comparators that they should not normalize anything
            ComparatorRegistry comparatorRegistry = schemaManager.getComparatorRegistry();

            for ( LdapComparator<?> comparator : comparatorRegistry )
            {
              if ( comparator instanceof NormalizingComparator )
              {
                ( ( NormalizingComparator ) comparator ).setOnServer();
              }
            }

            directoryService.setSchemaManager( schemaManager );
            
 
            ldapServer = new LdapServer();
            ldapServer.setDirectoryService( directoryService );
 
            TcpTransport ldapTransport = new TcpTransport( dsPort );
            ldapServer.setTransports( ldapTransport );
 
            // Determine an appropriate working directory
            ServletContext servletContext = evt.getServletContext();
            directoryService.setInstanceLayout( new InstanceLayout( RODA_APACHE_DS_DIRECTORY ) );

            Partition keepPartition = addPartition( "keep", "dc=keep,dc=pt" );
            
            // Index some attributes on the apache partition
            addIndex( keepPartition, "objectClass", "ou", "uid" );
            
            
            directoryService.startup();
            
            
            try
            {
                directoryService.getAdminSession().lookup( keepPartition.getSuffixDn() );
            }
            catch ( LdapException lnnfe )
            {
                Dn dnFoo = new Dn( "dc=keep,dc=pt" );
                Entry entryKeep = directoryService.newEntry( dnFoo );
                entryKeep.add( "objectClass", "top", "domain", "extensibleObject" );
                directoryService.getAdminSession().add( entryKeep );
            }
            
            
           
            ldapServer.start();
 
            // Store directoryService in context to provide it to servlets etc.
            servletContext.setAttribute( DirectoryService.JNDI_KEY, directoryService );
            
            UserUtility.setDirectoryService(directoryService);
            
            if(initializeDS){
            	//addLdif("root.ldif");
            	addLdif("roles.ldif");
            	addLdif("groups.ldif");
            	addLdif("users.ldif");
            }
        }
        catch ( Exception e )
        {
        	logger.error(e.getMessage(),e);
            throw new RuntimeException( e );
        }
    }
 
    private void addLdif(String fileName){
    	try{
	    	String roda_home;
			if (System.getProperty("roda.home") != null) {
				roda_home = System.getProperty("roda.home");
			} else if (System.getenv("RODA_HOME") != null) {
				roda_home = System.getenv("RODA_HOME");
			} else {
				roda_home = null;
			}
	
			File file = new File(roda_home, "config" + File.separator + "ldap" + File.separator
					+ fileName);
			LdifReader entries = new LdifReader(new FileInputStream(file));
	        for (LdifEntry ldifEntry : entries) {
	        	 DefaultEntry newEntry = new DefaultEntry(directoryService.getSchemaManager(), ldifEntry.getEntry());
	        	 directoryService.getAdminSession().add( newEntry );
	        }
    	}catch(Throwable t){
    		logger.error(t.getMessage(),t);
    	}
		
	}

	/**
     * Shutdown ApacheDS embedded.
     */
    public void contextDestroyed( ServletContextEvent evt )
    {
        try
        {
            ldapServer.stop();
            directoryService.shutdown();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
    
    
    private Partition addPartition( String partitionId, String partitionDn ) throws Exception
    {
        // Create a new partition named 'foo'.
    	Partition partition = new JdbmPartition( directoryService.getSchemaManager(), directoryService.getDnFactory() );
        partition.setId( partitionId );
        partition.setSuffixDn(new Dn(partitionDn));
        directoryService.addPartition( partition );
         
        return partition;
    }
    private void addIndex( Partition partition, String... attrs )
    {
    	Set indexedAttributes = new HashSet();

        for ( String attribute : attrs )
        {
            indexedAttributes.add( new JdbmIndex( attribute, false ) );
        }
        ((JdbmPartition)partition).setIndexedAttributes( indexedAttributes );
    }
    
    public static InputStream getConfigurationFile(String relativePath) {
		InputStream ret;
		String roda_home;
		if (System.getProperty("roda.home") != null) {
			roda_home = System.getProperty("roda.home");
		} else if (System.getenv("RODA_HOME") != null) {
			roda_home = System.getenv("RODA_HOME");
		} else {
			roda_home = null;
		}

		File staticConfig = new File(roda_home, "config" + File.separator
				+ relativePath);

		if (staticConfig.exists()) {
			try {
				ret = new FileInputStream(staticConfig);
			} catch (FileNotFoundException e) {
				logger.warn("Couldn't find static configuration file - "
						+ staticConfig);
				logger.info("Using internal configuration");
				ret = DSStartStopListener.class.getResourceAsStream("/config/"
						+ relativePath);
			}
		} else {
			logger.info("Using internal configuration");
			ret = DSStartStopListener.class.getResourceAsStream("/config/"
					+ relativePath);
		}
		return ret;
	}
}