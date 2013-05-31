package pt.gov.dgarq.roda.core.plugins;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.InvalidParameterException;
import pt.gov.dgarq.roda.core.data.PluginParameter;

/**
 * Abstract implementation of the Plugin interface
 * 
 * @author Luis Faria
 */
public abstract class AbstractPlugin implements Plugin {
	private static Logger logger = Logger.getLogger(AbstractPlugin.class);

	public static PluginParameter PARAMETER_RODA_CORE_URL() {
		return new PluginParameter("rodaServicesURL", //$NON-NLS-1$
				PluginParameter.TYPE_STRING, "http://localhost:8080/roda-core", //$NON-NLS-1$
				true, false, "RODA Core URL"); //$NON-NLS-1$
	}

	/**
	 * Common user name plugin parameter
	 */
	public static PluginParameter PARAMETER_RODA_CORE_USERNAME() {
		return new PluginParameter("username", PluginParameter.TYPE_STRING, //$NON-NLS-1$
				null, true, false, "Username"); //$NON-NLS-1$
	}

	/**
	 * Common user password plugin parameter
	 */
	public static PluginParameter PARAMETER_RODA_CORE_PASSWORD() {
		return new PluginParameter("password", PluginParameter.TYPE_PASSWORD, //$NON-NLS-1$
				null, true, false, "Password"); //$NON-NLS-1$
	}

	private Map<String, String> parameterValues;

	/**
	 */
	public AbstractPlugin() {
	}

	/**
	 * Abstract Plugin Helper constructor
	 * 
	 * @param parameters
	 *            the plugin parameters, set with default values or null
	 */
	public AbstractPlugin(List<PluginParameter> parameters) {
	}

	/**
	 * @see Plugin#getParameterValues()
	 */
	public Map<String, String> getParameterValues() {
		return parameterValues;
	}

	/**
	 * @see Plugin#setParameterValues(Map)
	 */
	public void setParameterValues(Map<String, String> parameterValues)
			throws InvalidParameterException {
		logger.debug("Setting parameters " + parameterValues); //$NON-NLS-1$
		this.parameterValues = parameterValues;
	}

}
