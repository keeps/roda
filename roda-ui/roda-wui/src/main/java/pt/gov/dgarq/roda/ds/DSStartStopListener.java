package pt.gov.dgarq.roda.ds;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.roda.common.ApacheDS;
import org.roda.common.UserUtility;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.v2.Group;
import pt.gov.dgarq.roda.core.data.v2.User;

public class DSStartStopListener implements ServletContextListener {
	private static final Logger LOGGER = Logger.getLogger(DSStartStopListener.class);

	private ApacheDS ldap;
	private Path rodaApacheDsConfigDirectory = null;
	private Path rodaApacheDsDataDirectory = null;

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		try {
			ldap.stop();
		} catch (Exception e) {
			LOGGER.error("Error while shutting down ApacheDS embedded server", e);
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		ldap = new ApacheDS();
		rodaApacheDsConfigDirectory = RodaCoreFactory.getConfigPath().resolve("ldap");
		rodaApacheDsDataDirectory = RodaCoreFactory.getDataPath().resolve("ldap");

		try {
			Configuration rodaConfig = RodaCoreFactory.getRodaConfiguration();

			if (!Files.exists(rodaApacheDsDataDirectory)) {
				Files.createDirectories(rodaApacheDsDataDirectory);
				ldap.initDirectoryService(rodaApacheDsConfigDirectory, rodaApacheDsDataDirectory);
				ldap.startServer(rodaConfig);
				for (User user : UserUtility.getLdapUtility().getUsers(new Filter())) {
					LOGGER.debug("User to be indexed: " + user);
					RodaCoreFactory.getModelService().addUser(user, false, true);
				}
				for (Group group : UserUtility.getLdapUtility().getGroups(new Filter())) {
					LOGGER.debug("Group to be indexed: " + group);
					RodaCoreFactory.getModelService().addGroup(group, false, true);
				}
			} else {
				ldap.instantiateDirectoryService(rodaApacheDsDataDirectory);
				ldap.startServer(rodaConfig);
			}

		} catch (Exception e) {
			LOGGER.error("Error starting up embedded ApacheDS", e);
		}

	}
}