package pt.gov.dgarq.roda.core.services;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.PluginInfo;
import pt.gov.dgarq.roda.core.plugins.PluginManager;
import pt.gov.dgarq.roda.core.plugins.PluginManagerException;

/**
 * This is the implementation of the Plugins service.
 * 
 * @author Rui Castro
 */
public class Plugins extends RODAWebService {
	static final private Logger logger = Logger.getLogger(Plugins.class);

	private PluginManager pluginManager = null;

	/**
	 * @throws RODAServiceException
	 */
	public Plugins() throws RODAServiceException {
		super();

		try {

			this.pluginManager = PluginManager.getDefaultPluginManager();

		} catch (PluginManagerException e) {
			logger.debug(e.getMessage(), e);
			throw new RODAServiceException(e.getMessage(), e);
		}
	}

	/**
	 * Returns a list of {@link PluginInfo}.
	 * 
	 * @return an array of {@link PluginInfo}.
	 */
	public PluginInfo[] getPluginsInfo() {

		Date start = new Date();
		List<PluginInfo> pluginsInfo = this.pluginManager.getPluginsInfo();
		PluginInfo[] plugins = pluginsInfo.toArray(new PluginInfo[pluginsInfo
				.size()]);
		long duration = new Date().getTime() - start.getTime();

		registerAction("Plugins.getPluginsInfo", new String[0],
				"User %username% called method Plugins.getPluginsInfo(" + ")",
				duration);

		return plugins;
	}

}
