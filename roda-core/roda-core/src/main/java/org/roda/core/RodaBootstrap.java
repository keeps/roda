package org.roda.core;

import org.roda.core.config.ConfigurationManager;
import org.roda.core.config.DirectoryInitializer;
import org.roda.core.plugins.PluginManager;
import org.roda.core.plugins.PluginManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for bootstrapping the RODA application tha needs to
 * be done before starting the spring boot application. It initializes the
 * configuration manager, directory initializer, and plugin manager. It also
 * sets the context class loader for the current thread.
 * 
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class RodaBootstrap {
  private static final Logger LOGGER = LoggerFactory.getLogger(RodaBootstrap.class);
  private static boolean instantiated = false;

  /** Private empty constructor */
  private RodaBootstrap() {
    // do nothing
  }

  public static synchronized void instantiate() {
    try {
      if (!instantiated) {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        RodaCoreFactory.setConfigurationManager(configurationManager);
        DirectoryInitializer.getInstance(configurationManager);
        PluginManager pluginManager = PluginManager.instantiatePluginManager(configurationManager);
        pluginManager.getEssentialPluginsClassLoader()
          .ifPresent(compoundClassLoader -> Thread.currentThread().setContextClassLoader(compoundClassLoader));
        instantiated = true;
        LOGGER.debug("Finished bootstrapping RODA");
      }
    } catch (PluginManagerException e) {
      throw new RuntimeException("Unable to instantiating plugin manager. Aborting...", e);
    }
  }
}
