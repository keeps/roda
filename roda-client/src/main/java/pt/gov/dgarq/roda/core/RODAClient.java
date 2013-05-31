package pt.gov.dgarq.roda.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.w3c.dom.Element;

import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.stubs.AcceptSIP;
import pt.gov.dgarq.roda.core.stubs.AcceptSIPServiceLocator;
import pt.gov.dgarq.roda.core.stubs.AcceptSIPSoapBindingStub;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.core.stubs.BrowserServiceLocator;
import pt.gov.dgarq.roda.core.stubs.BrowserSoapBindingStub;
import pt.gov.dgarq.roda.core.stubs.Editor;
import pt.gov.dgarq.roda.core.stubs.EditorServiceLocator;
import pt.gov.dgarq.roda.core.stubs.EditorSoapBindingStub;
import pt.gov.dgarq.roda.core.stubs.Ingest;
import pt.gov.dgarq.roda.core.stubs.IngestMonitor;
import pt.gov.dgarq.roda.core.stubs.IngestMonitorServiceLocator;
import pt.gov.dgarq.roda.core.stubs.IngestMonitorSoapBindingStub;
import pt.gov.dgarq.roda.core.stubs.IngestServiceLocator;
import pt.gov.dgarq.roda.core.stubs.IngestSoapBindingStub;
import pt.gov.dgarq.roda.core.stubs.LogMonitor;
import pt.gov.dgarq.roda.core.stubs.LogMonitorServiceLocator;
import pt.gov.dgarq.roda.core.stubs.LogMonitorSoapBindingStub;
import pt.gov.dgarq.roda.core.stubs.Logger;
import pt.gov.dgarq.roda.core.stubs.LoggerServiceLocator;
import pt.gov.dgarq.roda.core.stubs.LoggerSoapBindingStub;
import pt.gov.dgarq.roda.core.stubs.Login;
import pt.gov.dgarq.roda.core.stubs.LoginServiceLocator;
import pt.gov.dgarq.roda.core.stubs.Plugins;
import pt.gov.dgarq.roda.core.stubs.PluginsServiceLocator;
import pt.gov.dgarq.roda.core.stubs.PluginsSoapBindingStub;
import pt.gov.dgarq.roda.core.stubs.Reports;
import pt.gov.dgarq.roda.core.stubs.ReportsServiceLocator;
import pt.gov.dgarq.roda.core.stubs.ReportsSoapBindingStub;
import pt.gov.dgarq.roda.core.stubs.Scheduler;
import pt.gov.dgarq.roda.core.stubs.SchedulerServiceLocator;
import pt.gov.dgarq.roda.core.stubs.SchedulerSoapBindingStub;
import pt.gov.dgarq.roda.core.stubs.Search;
import pt.gov.dgarq.roda.core.stubs.SearchServiceLocator;
import pt.gov.dgarq.roda.core.stubs.SearchSoapBindingStub;
import pt.gov.dgarq.roda.core.stubs.Statistics;
import pt.gov.dgarq.roda.core.stubs.StatisticsMonitor;
import pt.gov.dgarq.roda.core.stubs.StatisticsMonitorServiceLocator;
import pt.gov.dgarq.roda.core.stubs.StatisticsMonitorSoapBindingStub;
import pt.gov.dgarq.roda.core.stubs.StatisticsServiceLocator;
import pt.gov.dgarq.roda.core.stubs.StatisticsSoapBindingStub;
import pt.gov.dgarq.roda.core.stubs.UserBrowser;
import pt.gov.dgarq.roda.core.stubs.UserBrowserServiceLocator;
import pt.gov.dgarq.roda.core.stubs.UserBrowserSoapBindingStub;
import pt.gov.dgarq.roda.core.stubs.UserEditor;
import pt.gov.dgarq.roda.core.stubs.UserEditorServiceLocator;
import pt.gov.dgarq.roda.core.stubs.UserEditorSoapBindingStub;
import pt.gov.dgarq.roda.core.stubs.UserManagement;
import pt.gov.dgarq.roda.core.stubs.UserManagementServiceLocator;
import pt.gov.dgarq.roda.core.stubs.UserManagementSoapBindingStub;
import pt.gov.dgarq.roda.core.stubs.UserRegistration;
import pt.gov.dgarq.roda.core.stubs.UserRegistrationServiceLocator;
import pt.gov.dgarq.roda.core.stubs.UserRegistrationSoapBindingStub;

/**
 * @author Rui Castro
 */
public class RODAClient {

	static final private org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(RODAClient.class);

	private static final String defaultRodaCorePath = "roda-core";

	/**
	 * Reads an {@link RemoteException} and returns a
	 * {@link RODAClientException} or an {@link AuthorizationDeniedException}
	 * depending on the given exception.
	 * 
	 * @param e
	 *            the {@link RemoteException}.
	 * 
	 * @return a {@link RODAClientException} or
	 *         {@link AuthorizationDeniedException}.
	 */
	public static RODAException parseRemoteException(RemoteException e) {

		RODAException exception = new RODAClientException(e);

		if (e instanceof AxisFault) {

			AxisFault axisFault = (AxisFault) e;

			for (Element element : axisFault.getFaultDetails()) {

				if (element.getLocalName() != null
						&& element.getLocalName().equals("HttpErrorCode")) {

					String httpErrorCode = element.getFirstChild()
							.getNodeValue();

					if (httpErrorCode.equals("401")) {

						exception = new AuthorizationDeniedException(axisFault
								.getFaultString());
						break;
					}
				}
			}

		}

		return exception;
	}

	private Login serviceLogin = null;
	private UserManagement serviceUserManagement = null;
	private UserRegistration serviceUserRegistration = null;
	private UserBrowser serviceUserBrowser = null;
	private UserEditor serviceUserEditor = null;
	private Browser serviceBrowser = null;
	private Search serviceSearch = null;
	private Logger serviceLogger = null;
	private LogMonitor serviceLogMonitor = null;
	private Editor serviceEditor = null;
	private IngestMonitor serviceIngestMonitor = null;
	private AcceptSIP serviceAcceptSIP = null;
	private Ingest serviceIngest = null;
	private Scheduler serviceScheduler = null;
	private Plugins servicePlugins = null;
	private Reports serviceReports = null;
	private Statistics serviceStatistics = null;
	private StatisticsMonitor serviceStatisticsMonitor = null;
	private Downloader downloader = null;

	private URL rodaCoreURL = null;
	private String guestUsername = null;

	private String username = null;
	private String password = null;
	private String userIPAddress = null;

	/**
	 * Constructs a new anonymous RODAClient for the specified RODA Services
	 * host.
	 * 
	 * @param rodacoreURL
	 *            the {@link URL} to the RODA Core application.
	 * 
	 * @throws LoginException
	 * @throws RODAClientException
	 */
	public RODAClient(URL rodacoreURL) throws RODAClientException,
			LoginException {
		this(rodacoreURL, null, null);
	}

	/**
	 * Constructs a new authenticated RODAClient for the specified RODA Services
	 * host.
	 * 
	 * @param rodacoreURL
	 *            the {@link URL} to the RODA Core application.
	 * @param username
	 *            the username to use in the connection to the service
	 * @param password
	 *            the password to use in the connection to the service
	 * 
	 * @throws LoginException
	 * @throws RODAClientException
	 */
	public RODAClient(URL rodacoreURL, String username, String password)
			throws RODAClientException, LoginException {

		this.rodaCoreURL = rodacoreURL;
		this.username = username;
		this.password = password;

		if ("/".equals(rodacoreURL.getPath())) {

			logger.warn("No path to roda-core specified. Using default value '"
					+ defaultRodaCorePath + "'");
			try {

				this.rodaCoreURL = new URL(rodacoreURL + defaultRodaCorePath);

			} catch (MalformedURLException e) {
				logger
						.warn("Error creating default service URL. Using provided value '"
								+ rodacoreURL + "'");
			}
		}

		// this forces the creation of the login service causing an exception to
		// be thrown if something goes wrong.
		this.serviceLogin = getLoginService();
	}

	/**
	 * Returns a stub to the Login service.
	 * 
	 * @return the loginService
	 * 
	 * @throws LoginException
	 * @throws RODAClientException
	 */
	public Login getLoginService() throws LoginException, RODAClientException {

		if (this.serviceLogin == null) {

			LoginServiceLocator loginServiceLocator = new LoginServiceLocator();
			loginServiceLocator
					.setLoginEndpointAddress(getServiceAddress("Login"));
			try {

				this.serviceLogin = loginServiceLocator.getLogin();

			} catch (ServiceException e) {
				logger.debug("Error accessing Login service - "
						+ e.getMessage(), e);
				throw new RODAClientException(
						"Error accessing Login service - " + e.getMessage(), e);
			}

			try {

				String[] guestCredentials = this.serviceLogin
						.getGuestCredentials();

				this.guestUsername = guestCredentials[0];

				if (this.username == null) {

					this.username = guestCredentials[0];
					this.password = guestCredentials[1];

				} else {

					this.serviceLogin.getAuthenticatedUser(this.username,
							this.password);
				}

			} catch (RemoteException e) {

				if (e.detail != null) {

					logger.debug("Error connecting to Login service - "
							+ e.detail.getMessage(), e.detail);
					throw new RODAClientException(
							"Error connecting to Login service - "
									+ e.detail.getMessage(), e.detail);
				} else {
					logger.debug("Error in Login service - " + e.getMessage(),
							e);
					throw new RODAClientException("Error in Login service - "
							+ e.getMessage(), e);
				}
			}

		}

		return this.serviceLogin;
	}

	/**
	 * Returns a stub to the UserManagement service.
	 * 
	 * @return a stub to the UserManagement service.
	 * 
	 * @throws RODAClientException
	 *             if the service client could not be created.
	 */
	public UserManagement getUserManagementService() throws RODAClientException {

		if (serviceUserManagement == null) {
			UserManagementServiceLocator serviceLocator = new UserManagementServiceLocator();
			serviceLocator
					.setUserManagementEndpointAddress(getServiceAddress("UserManagement"));
			try {

				this.serviceUserManagement = serviceLocator.getUserManagement();

			} catch (ServiceException e) {
				logger.debug("Error accessing UserManagement service - "
						+ e.getMessage(), e);
				throw new RODAClientException(
						"Error accessing UserManagement service - "
								+ e.getMessage(), e);
			}

			((UserManagementSoapBindingStub) this.serviceUserManagement)
					.setUsername(this.username);
			((UserManagementSoapBindingStub) this.serviceUserManagement)
					.setPassword(this.password);
			((UserManagementSoapBindingStub) this.serviceUserManagement)
					.setTimeout(0);
		}

		return serviceUserManagement;
	}

	/**
	 * Returns a stub to the UserRegistration service.
	 * 
	 * @return a stub to the UserRegistration service.
	 * 
	 * @throws RODAClientException
	 *             if the service client could not be created.
	 */
	public UserRegistration getUserRegistrationService()
			throws RODAClientException {

		if (serviceUserRegistration == null) {
			UserRegistrationServiceLocator serviceLocator = new UserRegistrationServiceLocator();
			serviceLocator
					.setUserRegistrationEndpointAddress(getServiceAddress("UserRegistration"));
			try {

				this.serviceUserRegistration = serviceLocator
						.getUserRegistration();

			} catch (ServiceException e) {
				logger.debug("Error accessing UserRegistration service - "
						+ e.getMessage(), e);
				throw new RODAClientException(
						"Error accessing UserRegistration service - "
								+ e.getMessage(), e);
			}

			((UserRegistrationSoapBindingStub) this.serviceUserRegistration)
					.setUsername(this.username);
			((UserRegistrationSoapBindingStub) this.serviceUserRegistration)
					.setPassword(this.password);
			((UserRegistrationSoapBindingStub) this.serviceUserRegistration)
					.setTimeout(0);
		}

		return serviceUserRegistration;
	}

	/**
	 * Returns a stub to the {@link UserBrowser} service.
	 * 
	 * @return a stub to the {@link UserBrowser} service.
	 * 
	 * @throws RODAClientException
	 *             if the service client could not be created.
	 */
	public UserBrowser getUserBrowserService() throws RODAClientException {

		if (serviceUserBrowser == null) {
			UserBrowserServiceLocator serviceLocator = new UserBrowserServiceLocator();
			serviceLocator
					.setUserBrowserEndpointAddress(getServiceAddress("UserBrowser"));
			try {

				this.serviceUserBrowser = serviceLocator.getUserBrowser();

			} catch (ServiceException e) {
				logger.debug("Error accessing UserBrowser service - "
						+ e.getMessage(), e);
				throw new RODAClientException(
						"Error accessing UserBrowser service - "
								+ e.getMessage(), e);
			}

			((UserBrowserSoapBindingStub) this.serviceUserBrowser)
					.setUsername(this.username);
			((UserBrowserSoapBindingStub) this.serviceUserBrowser)
					.setPassword(this.password);
			((UserBrowserSoapBindingStub) this.serviceUserBrowser)
					.setTimeout(0);
		}

		return serviceUserBrowser;
	}

	/**
	 * Returns a stub to the {@link UserEditor} service.
	 * 
	 * @return a stub to the {@link UserEditor} service.
	 * 
	 * @throws RODAClientException
	 *             if the service client could not be created.
	 */
	public UserEditor getUserEditorService() throws RODAClientException {

		if (serviceUserEditor == null) {
			UserEditorServiceLocator serviceLocator = new UserEditorServiceLocator();
			serviceLocator
					.setUserEditorEndpointAddress(getServiceAddress("UserEditor"));
			try {

				this.serviceUserEditor = serviceLocator.getUserEditor();

			} catch (ServiceException e) {
				logger.debug("Error accessing UserEditor service - "
						+ e.getMessage(), e);
				throw new RODAClientException(
						"Error accessing UserEditor service - "
								+ e.getMessage(), e);
			}

			((UserEditorSoapBindingStub) this.serviceUserEditor)
					.setUsername(this.username);
			((UserEditorSoapBindingStub) this.serviceUserEditor)
					.setPassword(this.password);
			((UserEditorSoapBindingStub) this.serviceUserEditor).setTimeout(0);
		}

		return serviceUserEditor;
	}

	/**
	 * Returns a stub to the Browser service.
	 * 
	 * @return a stub to the Browser service.
	 * 
	 * @throws RODAClientException
	 *             if the service client could not be created.
	 */
	public Browser getBrowserService() throws RODAClientException {

		if (this.serviceBrowser == null) {
			BrowserServiceLocator serviceLocator = new BrowserServiceLocator();
			serviceLocator
					.setBrowserEndpointAddress(getServiceAddress("Browser"));

			try {

				this.serviceBrowser = serviceLocator.getBrowser();

			} catch (ServiceException e) {
				logger.debug("Error accessing Browser service - "
						+ e.getMessage(), e);
				throw new RODAClientException(
						"Error accessing Browser service - " + e.getMessage(),
						e);
			}

			((BrowserSoapBindingStub) this.serviceBrowser)
					.setUsername(this.username);
			((BrowserSoapBindingStub) this.serviceBrowser)
					.setPassword(this.password);
			((BrowserSoapBindingStub) this.serviceBrowser).setTimeout(0);

		}

		return serviceBrowser;
	}

	/**
	 * Returns a stub to the Search service.
	 * 
	 * @return a stub to the Search service.
	 * 
	 * @throws RODAClientException
	 *             if the service client could not be created.
	 */
	public Search getSearchService() throws RODAClientException {

		if (this.serviceSearch == null) {

			SearchServiceLocator serviceLocator = new SearchServiceLocator();
			serviceLocator
					.setSearchEndpointAddress(getServiceAddress("Search"));

			try {

				this.serviceSearch = serviceLocator.getSearch();

			} catch (ServiceException e) {
				logger.debug("Error accessing Search service - "
						+ e.getMessage(), e);
				throw new RODAClientException(
						"Error accessing Search service - " + e.getMessage(), e);
			}

			((SearchSoapBindingStub) this.serviceSearch)
					.setUsername(this.username);
			((SearchSoapBindingStub) this.serviceSearch)
					.setPassword(this.password);
			((SearchSoapBindingStub) this.serviceSearch).setTimeout(0);
		}

		return this.serviceSearch;
	}

	/**
	 * Returns a stub to the Logger service.
	 * 
	 * @return a stub to the Logger service.
	 * 
	 * @throws RODAClientException
	 *             if the service client could not be created.
	 */
	public Logger getLoggerService() throws RODAClientException {

		if (this.serviceLogger == null) {

			LoggerServiceLocator serviceLocator = new LoggerServiceLocator();
			serviceLocator
					.setLoggerEndpointAddress(getServiceAddress("Logger"));

			try {

				this.serviceLogger = serviceLocator.getLogger();

			} catch (ServiceException e) {
				logger.debug("Error accessing Logger service - "
						+ e.getMessage(), e);
				throw new RODAClientException(
						"Error accessing Logger service - " + e.getMessage(), e);
			}

			((LoggerSoapBindingStub) this.serviceLogger)
					.setUsername(this.username);
			((LoggerSoapBindingStub) this.serviceLogger)
					.setPassword(this.password);
			((LoggerSoapBindingStub) this.serviceLogger).setTimeout(0);
		}

		return this.serviceLogger;
	}

	/**
	 * Returns a stub to the {@link LogMonitor} service.
	 * 
	 * @return a stub to the {@link LogMonitor} service.
	 * 
	 * @throws RODAClientException
	 *             if the service client could not be created.
	 */
	public LogMonitor getLogMonitorService() throws RODAClientException {

		if (this.serviceLogMonitor == null) {

			LogMonitorServiceLocator serviceLocator = new LogMonitorServiceLocator();
			serviceLocator
					.setLogMonitorEndpointAddress(getServiceAddress("LogMonitor"));

			try {

				this.serviceLogMonitor = serviceLocator.getLogMonitor();

			} catch (ServiceException e) {
				logger.debug("Error accessing LogMonitor service - "
						+ e.getMessage(), e);
				throw new RODAClientException(
						"Error accessing LogMonitor service - "
								+ e.getMessage(), e);
			}

			((LogMonitorSoapBindingStub) this.serviceLogMonitor)
					.setUsername(this.username);
			((LogMonitorSoapBindingStub) this.serviceLogMonitor)
					.setPassword(this.password);
			((LogMonitorSoapBindingStub) this.serviceLogMonitor).setTimeout(0);
		}

		return this.serviceLogMonitor;
	}

	/**
	 * Returns a stub to the Editor service.
	 * 
	 * @return a stub to the Editor service.
	 * 
	 * @throws RODAClientException
	 *             if the service client could not be created.
	 */
	public Editor getEditorService() throws RODAClientException {

		if (this.serviceEditor == null) {

			EditorServiceLocator serviceLocator = new EditorServiceLocator();
			serviceLocator
					.setEditorEndpointAddress(getServiceAddress("Editor"));

			try {

				this.serviceEditor = serviceLocator.getEditor();

			} catch (ServiceException e) {
				logger.debug("Error accessing Editor service - "
						+ e.getMessage(), e);
				throw new RODAClientException(
						"Error accessing Editor service - " + e.getMessage(), e);
			}

			((EditorSoapBindingStub) this.serviceEditor)
					.setUsername(this.username);
			((EditorSoapBindingStub) this.serviceEditor)
					.setPassword(this.password);
			((EditorSoapBindingStub) this.serviceEditor).setTimeout(0);
		}

		return this.serviceEditor;
	}

	/**
	 * Returns a stub to the IngestMonitor service.
	 * 
	 * @return a stub to the IngestMonitor service.
	 * 
	 * @throws RODAClientException
	 *             if the service client could not be created.
	 */
	public IngestMonitor getIngestMonitorService() throws RODAClientException {

		if (this.serviceIngestMonitor == null) {

			IngestMonitorServiceLocator ingestMonitorLocator = new IngestMonitorServiceLocator();
			ingestMonitorLocator
					.setIngestMonitorEndpointAddress(getServiceAddress("IngestMonitor"));

			try {

				this.serviceIngestMonitor = ingestMonitorLocator
						.getIngestMonitor();

			} catch (ServiceException e) {
				logger.debug("Error accessing IngestMonitor service - "
						+ e.getMessage(), e);
				throw new RODAClientException(
						"Error accessing IngestMonitor service - "
								+ e.getMessage(), e);
			}

			((IngestMonitorSoapBindingStub) this.serviceIngestMonitor)
					.setUsername(this.username);
			((IngestMonitorSoapBindingStub) this.serviceIngestMonitor)
					.setPassword(this.password);
			((IngestMonitorSoapBindingStub) this.serviceIngestMonitor)
					.setTimeout(0);
		}

		return this.serviceIngestMonitor;
	}

	/**
	 * Returns a stub to the AcceptSIP service.
	 * 
	 * @return a stub to the AcceptSIP service.
	 * 
	 * @throws RODAClientException
	 *             if the service client could not be created.
	 */
	public AcceptSIP getAcceptSIPService() throws RODAClientException {
		if (this.serviceAcceptSIP == null) {

			AcceptSIPServiceLocator acceptSIPLocator = new AcceptSIPServiceLocator();
			acceptSIPLocator
					.setAcceptSIPEndpointAddress(getServiceAddress("AcceptSIP"));

			try {

				this.serviceAcceptSIP = acceptSIPLocator.getAcceptSIP();

			} catch (ServiceException e) {
				logger.debug("Error accessing AcceptSIP service - "
						+ e.getMessage(), e);
				throw new RODAClientException(
						"Error accessing AcceptSIP service - " + e.getMessage(),
						e);
			}

			((AcceptSIPSoapBindingStub) this.serviceAcceptSIP)
					.setUsername(this.username);
			((AcceptSIPSoapBindingStub) this.serviceAcceptSIP)
					.setPassword(this.password);
			((AcceptSIPSoapBindingStub) this.serviceAcceptSIP).setTimeout(0);
		}
		return serviceAcceptSIP;
	}

	/**
	 * Returns a stub to the Ingest service.
	 * 
	 * @return a stub to the Ingest service.
	 * 
	 * @throws RODAClientException
	 *             if the service client could not be created.
	 */
	public Ingest getIngestService() throws RODAClientException {
		if (this.serviceIngest == null) {

			IngestServiceLocator ingestLocator = new IngestServiceLocator();
			ingestLocator.setIngestEndpointAddress(getServiceAddress("Ingest"));

			try {

				this.serviceIngest = ingestLocator.getIngest();

			} catch (ServiceException e) {
				logger.debug("Error accessing Ingest service - "
						+ e.getMessage(), e);
				throw new RODAClientException(
						"Error accessing Ingest service - " + e.getMessage(), e);
			}

			((IngestSoapBindingStub) this.serviceIngest)
					.setUsername(this.username);
			((IngestSoapBindingStub) this.serviceIngest)
					.setPassword(this.password);
			((IngestSoapBindingStub) this.serviceIngest).setTimeout(0);
		}
		return serviceIngest;
	}

	/**
	 * Returns a stub to the Scheduler service.
	 * 
	 * @return a stub to the Scheduler service.
	 * 
	 * @throws RODAClientException
	 *             if the service client could not be created.
	 */
	public Scheduler getSchedulerService() throws RODAClientException {
		if (this.serviceScheduler == null) {

			SchedulerServiceLocator ingestLocator = new SchedulerServiceLocator();
			ingestLocator
					.setSchedulerEndpointAddress(getServiceAddress("Scheduler"));
			try {

				this.serviceScheduler = ingestLocator.getScheduler();

			} catch (ServiceException e) {
				logger.debug("Error accessing Scheduler service - "
						+ e.getMessage(), e);
				throw new RODAClientException(
						"Error accessing Scheduler service - " + e.getMessage(),
						e);
			}

			((SchedulerSoapBindingStub) this.serviceScheduler)
					.setUsername(this.username);
			((SchedulerSoapBindingStub) this.serviceScheduler)
					.setPassword(this.password);
			((SchedulerSoapBindingStub) this.serviceScheduler).setTimeout(0);
		}
		return serviceScheduler;
	}

	/**
	 * Returns a stub to the Plugins service.
	 * 
	 * @return a stub to the Plugins service.
	 * 
	 * @throws RODAClientException
	 *             if the service client could not be created.
	 */
	public Plugins getPluginsService() throws RODAClientException {
		if (this.servicePlugins == null) {

			PluginsServiceLocator pluginsLocator = new PluginsServiceLocator();
			pluginsLocator
					.setPluginsEndpointAddress(getServiceAddress("Plugins"));
			try {

				this.servicePlugins = pluginsLocator.getPlugins();

			} catch (ServiceException e) {
				logger.debug("Error accessing Plugins service - "
						+ e.getMessage(), e);
				throw new RODAClientException(
						"Error accessing Plugins service - " + e.getMessage(),
						e);
			}

			((PluginsSoapBindingStub) this.servicePlugins)
					.setUsername(this.username);
			((PluginsSoapBindingStub) this.servicePlugins)
					.setPassword(this.password);
			((PluginsSoapBindingStub) this.servicePlugins).setTimeout(0);
		}
		return servicePlugins;
	}

	/**
	 * Returns a stub to the Reports service.
	 * 
	 * @return a stub to the Reports service.
	 * 
	 * @throws RODAClientException
	 *             if the service client could not be created.
	 */
	public Reports getReportsService() throws RODAClientException {
		if (this.serviceReports == null) {

			ReportsServiceLocator reportsLocator = new ReportsServiceLocator();
			reportsLocator
					.setReportsEndpointAddress(getServiceAddress("Reports"));
			try {

				this.serviceReports = reportsLocator.getReports();

			} catch (ServiceException e) {
				logger.debug("Error accessing Reports service - "
						+ e.getMessage(), e);
				throw new RODAClientException(
						"Error accessing Reports service - " + e.getMessage(),
						e);
			}

			((ReportsSoapBindingStub) this.serviceReports)
					.setUsername(this.username);
			((ReportsSoapBindingStub) this.serviceReports)
					.setPassword(this.password);
			((ReportsSoapBindingStub) this.serviceReports).setTimeout(0);
		}
		return serviceReports;
	}

	/**
	 * Returns a stub to the {@link Statistics} service.
	 * 
	 * @return a stub to the {@link Statistics} service.
	 * 
	 * @throws RODAClientException
	 *             if the service client could not be created.
	 */
	public Statistics getStatisticsService() throws RODAClientException {
		if (this.serviceStatistics == null) {

			StatisticsServiceLocator serviceLocator = new StatisticsServiceLocator();
			serviceLocator
					.setStatisticsEndpointAddress(getServiceAddress("Statistics"));
			try {

				this.serviceStatistics = serviceLocator.getStatistics();

			} catch (ServiceException e) {
				logger.debug("Error accessing Statistics service - "
						+ e.getMessage(), e);
				throw new RODAClientException(
						"Error accessing Statistics service - "
								+ e.getMessage(), e);
			}

			((StatisticsSoapBindingStub) this.serviceStatistics)
					.setUsername(this.username);
			((StatisticsSoapBindingStub) this.serviceStatistics)
					.setPassword(this.password);
			((StatisticsSoapBindingStub) this.serviceStatistics).setTimeout(0);
		}
		return serviceStatistics;
	}

	/**
	 * Returns a stub to the {@link StatisticsMonitor} service.
	 * 
	 * @return a stub to the {@link StatisticsMonitor} service.
	 * 
	 * @throws RODAClientException
	 *             if the service client could not be created.
	 */
	public StatisticsMonitor getStatisticsMonitorService()
			throws RODAClientException {

		if (this.serviceStatisticsMonitor == null) {

			StatisticsMonitorServiceLocator serviceLocator = new StatisticsMonitorServiceLocator();
			serviceLocator
					.setStatisticsMonitorEndpointAddress(getServiceAddress("StatisticsMonitor"));
			try {

				this.serviceStatisticsMonitor = serviceLocator
						.getStatisticsMonitor();

			} catch (ServiceException e) {
				logger.debug("Error accessing StatisticsMonitor service - "
						+ e.getMessage(), e);
				throw new RODAClientException(
						"Error accessing StatisticsMonitor service - "
								+ e.getMessage(), e);
			}

			((StatisticsMonitorSoapBindingStub) this.serviceStatisticsMonitor)
					.setUsername(this.username);
			((StatisticsMonitorSoapBindingStub) this.serviceStatisticsMonitor)
					.setPassword(this.password);
			((StatisticsMonitorSoapBindingStub) this.serviceStatisticsMonitor)
					.setTimeout(0);
		}

		return serviceStatisticsMonitor;
	}

	/**
	 * @return the authenticatedUser
	 * 
	 * @throws LoginException
	 * @throws RemoteException
	 */
	public User getAuthenticatedUser() throws LoginException, RemoteException {
		return this.serviceLogin.getAuthenticatedUser(this.username,
				this.password);
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Returns <code>true</code> if current user is the guest user and
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if current user is the guest user and
	 *         <code>false</code> otherwise.
	 */
	public boolean isGuestLogin() {
		return this.username.equals(this.guestUsername);
	}

	/**
	 * @return the userIPAddress
	 */
	public String getUserIPAddress() {
		return userIPAddress;
	}

	/**
	 * @param userIPAddress
	 *            the userIPAddress to set
	 */
	public void setUserIPAddress(String userIPAddress) {
		this.userIPAddress = userIPAddress;
	}

	/**
	 * Get downloader service
	 * 
	 * @return the downloader service
	 * @throws LoginException
	 * @throws DownloaderException
	 */
	public Downloader getDownloader() throws LoginException,
			DownloaderException {
		if (downloader == null) {
			downloader = new Downloader(rodaCoreURL, username, password);
		}
		return downloader;
	}

	private String getServiceAddress(String serviceName) {
		return this.rodaCoreURL + "/services/" + serviceName;
	}

}
