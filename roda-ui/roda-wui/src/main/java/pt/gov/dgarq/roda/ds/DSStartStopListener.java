package pt.gov.dgarq.roda.ds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.directory.server.core.CoreSession;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.entry.DefaultServerEntry;
import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.server.schema.registries.Registries;
import org.apache.directory.server.xdbm.Index;
import org.apache.directory.shared.ldap.exception.LdapNameNotFoundException;
import org.apache.directory.shared.ldap.ldif.LdifEntry;
import org.apache.directory.shared.ldap.ldif.LdifReader;
import org.apache.directory.shared.ldap.name.LdapDN;
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
        	
        	
        	InputStream relsStream = getConfigurationFile("roda-wui.properties");
			Properties dsProperties = new Properties();
			dsProperties.load(relsStream);
			
			dsPort = (dsProperties.getProperty("ds.port")!=null)?Integer.parseInt(dsProperties.getProperty("ds.port")):10389;
			
            directoryService = new DefaultDirectoryService();
            directoryService.setShutdownHookEnabled( true );
 
            ldapServer = new LdapServer();
            ldapServer.setDirectoryService( directoryService );
            ldapServer.setAllowAnonymousAccess( true );
 
            TcpTransport ldapTransport = new TcpTransport( dsPort );
            ldapServer.setTransports( ldapTransport );
 
            // Determine an appropriate working directory
            ServletContext servletContext = evt.getServletContext();
            directoryService.setWorkingDirectory( RODA_APACHE_DS_DIRECTORY );

            Partition keepPartition = addPartition( "keep", "dc=keep,dc=pt" );
            
            // Index some attributes on the apache partition
            addIndex( keepPartition, "objectClass", "ou", "uid" );
            
            
            directoryService.startup();
            
            
            try
            {
                directoryService.getAdminSession().lookup( keepPartition.getSuffixDn() );
            }
            catch ( LdapNameNotFoundException lnnfe )
            {
                LdapDN dnFoo = new LdapDN( "dc=keep,dc=pt" );
                ServerEntry entryKeep = directoryService.newEntry( dnFoo );
                entryKeep.add( "objectClass", "top", "domain", "extensibleObject" );
                entryKeep.add( "dc", "foo" );
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
	        CoreSession rootDSE = directoryService.getAdminSession();
	        for (LdifEntry ldifEntry : entries) {
	            Registries registries = rootDSE.getDirectoryService().getRegistries();
	            rootDSE.add(new DefaultServerEntry(rootDSE.getDirectoryService().getRegistries(), ldifEntry.getEntry()));
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
        Partition partition = new JdbmPartition();
        partition.setId( partitionId );
        partition.setSuffix( partitionDn );
        directoryService.addPartition( partition );
         
        return partition;
    }
    private void addIndex( Partition partition, String... attrs )
    {
        // Index some attributes on the apache partition
        HashSet<Index<?, ServerEntry>> indexedAttributes = new HashSet<Index<?, ServerEntry>>();
        
        for ( String attribute:attrs )
        {
            indexedAttributes.add( new JdbmIndex<String,ServerEntry>( attribute ) );
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