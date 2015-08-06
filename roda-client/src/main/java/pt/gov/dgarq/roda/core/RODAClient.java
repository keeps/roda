package pt.gov.dgarq.roda.core;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.naming.AuthenticationException;
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
import pt.gov.dgarq.roda.core.stubs.ConfigurationManager;
import pt.gov.dgarq.roda.core.stubs.ConfigurationManagerServiceLocator;
import pt.gov.dgarq.roda.core.stubs.ConfigurationManagerSoapBindingStub;
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
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

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

	private ConfigurationManager serviceConfigurationManager = null;
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
	private String[] guestCredentials = null;

	private String userIPAddress = null;
	
	private CASUtility casUtility = null;
	private CASUserPrincipal cup = null;

	
	
	public CASUserPrincipal getCup() {
		return cup;
	}

	public void setCup(CASUserPrincipal cup) {
		this.cup = cup;
	}

	public CASUtility getCasUtility() {
		return casUtility;
	}

	public void setCasUtility(CASUtility casUtility) {
		this.casUtility = casUtility;
	}

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

	
	public RODAClient(URL rodacoreURL, CASUtility casUtility) throws RODAClientException,
	LoginException {
		this(rodacoreURL, null, null,casUtility);
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
	public RODAClient(URL rodacoreURL, String username, String password, CASUtility casUtility)
			throws RODAClientException, LoginException {

		this.rodaCoreURL = rodacoreURL;
		this.casUtility = casUtility;
		if(username!=null && password!=null){
			try{
				// FIXME empty string
				this.cup = casUtility.getCASUserPrincipal(username, password,"");
			}catch(AuthenticationException e){
			  System.err.println(e);
				throw new RODAClientException(e.getMessage());
			}
		}

		

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
	
	public RODAClient(URL rodacoreURL, String proxyGrantingTicket, CASUtility casUtility) throws RODAClientException, LoginException {
          this.casUtility = casUtility;
          this.rodaCoreURL = rodacoreURL;
          try{
        	 // FIXME empty string
            this.cup = casUtility.getCASUserPrincipalFromProxyGrantingTicket(proxyGrantingTicket,"");
          }catch (AuthenticationException e) {
            throw new RODAClientException("Unable to get CasUserPrincipal");
          }
          
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
	
	public RODAClient(URL rodacoreURL, CASUserPrincipal cup, CASUtility casUtility) throws RODAClientException, LoginException {
		this.casUtility = casUtility;
		this.rodaCoreURL = rodacoreURL;
		this.cup = cup;
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

				guestCredentials = this.serviceLogin.getGuestCredentials();

				if(this.cup!=null){
					String proxyTicket = this.casUtility.generateProxyTicket(this.cup.getProxyGrantingTicket());
					User u = this.serviceLogin.getAuthenticatedUserCAS(proxyTicket);
				}else{
					logger.debug("RodaClient username null. Using guest credentials.");
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
					.setUsername(this.getUsername());
			((UserManagementSoapBindingStub) this.serviceUserManagement)
					.setPassword("");
			((UserManagementSoapBindingStub) this.serviceUserManagement)
					.setTimeout(0);
		}

		InvocationHandler handler = new UserManagementProxy(serviceUserManagement, getProxyGrantingTicket());
        Class[] interfaces = this.serviceUserManagement.getClass().getInterfaces();
        Object myproxy = Proxy.newProxyInstance( this.getClass().getClassLoader(),interfaces,handler);
		return (UserManagement) myproxy;
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
					.setUsername(this.getUsername());
			((UserRegistrationSoapBindingStub) this.serviceUserRegistration)
					.setPassword("");
			((UserRegistrationSoapBindingStub) this.serviceUserRegistration)
					.setTimeout(0);
		}
		InvocationHandler handler = new UserRegistrationProxy(serviceUserRegistration, getProxyGrantingTicket());
        Class[] interfaces = this.serviceUserRegistration.getClass().getInterfaces();
        Object myproxy = Proxy.newProxyInstance( this.getClass().getClassLoader(),interfaces,handler);
		return (UserRegistration) myproxy;
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
					.setUsername(this.getUsername());
			((UserBrowserSoapBindingStub) this.serviceUserBrowser)
					.setPassword("");
			((UserBrowserSoapBindingStub) this.serviceUserBrowser)
					.setTimeout(0);
		}

		InvocationHandler handler = new UserBrowserProxy(serviceUserBrowser, getProxyGrantingTicket());
        Class[] interfaces = this.serviceUserBrowser.getClass().getInterfaces();
        Object myproxy = Proxy.newProxyInstance( this.getClass().getClassLoader(),interfaces,handler);
		return (UserBrowser) myproxy;
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
					.setUsername(this.getUsername());
			((UserEditorSoapBindingStub) this.serviceUserEditor)
					.setPassword("");
			((UserEditorSoapBindingStub) this.serviceUserEditor).setTimeout(0);
		}
		InvocationHandler handler = new UserEditorProxy(serviceUserEditor, getProxyGrantingTicket());
        Class[] interfaces = this.serviceUserEditor.getClass().getInterfaces();
        Object myproxy = Proxy.newProxyInstance( this.getClass().getClassLoader(),interfaces,handler);
		return (UserEditor) myproxy;
	}
	
	/**
	 * Returns a stub to the ConfigurationManager service.
	 * 
	 * @return a stub to the ConfigurationManager service.
	 * 
	 * @throws RODAClientException
	 *             if the service client could not be created.
	 */
	public ConfigurationManager getConfigurationManagerService() throws RODAClientException {

		if (this.serviceConfigurationManager == null) {
			ConfigurationManagerServiceLocator serviceLocator = new ConfigurationManagerServiceLocator();
			serviceLocator
					.setConfigurationManagerEndpointAddress(getServiceAddress("ConfigurationManager"));

			try {

				this.serviceConfigurationManager = serviceLocator.getConfigurationManager();

			} catch (ServiceException e) {
				logger.debug("Error accessing ConfigurationManager service - "
						+ e.getMessage(), e);
				throw new RODAClientException(
						"Error accessing ConfigurationManager service - " + e.getMessage(),
						e);
			}

			((ConfigurationManagerSoapBindingStub) this.serviceConfigurationManager)
					.setUsername(this.getUsername());
			((ConfigurationManagerSoapBindingStub) this.serviceConfigurationManager)
					.setPassword("");
			((ConfigurationManagerSoapBindingStub) this.serviceConfigurationManager).setTimeout(0);

		}
		InvocationHandler handler = new ConfigurationManagerProxy(serviceConfigurationManager, getProxyGrantingTicket());
        Class[] interfaces = this.serviceConfigurationManager.getClass().getInterfaces();
        Object myproxy = Proxy.newProxyInstance( this.getClass().getClassLoader(),interfaces,handler);
		return (ConfigurationManager) myproxy;
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
					.setUsername(this.getUsername());
			((BrowserSoapBindingStub) this.serviceBrowser)
					.setPassword("");
			((BrowserSoapBindingStub) this.serviceBrowser).setTimeout(0);

		}
		InvocationHandler handler = new BrowserProxy(serviceBrowser, getProxyGrantingTicket());
        Class[] interfaces = this.serviceBrowser.getClass().getInterfaces();
        Object myproxy = Proxy.newProxyInstance( this.getClass().getClassLoader(),interfaces,handler);
		return (Browser) myproxy;
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
					.setUsername(this.getUsername());
			((SearchSoapBindingStub) this.serviceSearch)
					.setPassword("");
			((SearchSoapBindingStub) this.serviceSearch).setTimeout(0);
		}
		InvocationHandler handler = new SearchProxy(this.serviceSearch, getProxyGrantingTicket());
        Class[] interfaces = this.serviceSearch.getClass().getInterfaces();
        Object myproxy = Proxy.newProxyInstance( this.getClass().getClassLoader(),interfaces,handler);
		return (Search) myproxy;
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
					.setUsername(this.getUsername());
			((LoggerSoapBindingStub) this.serviceLogger)
					.setPassword("");
			((LoggerSoapBindingStub) this.serviceLogger).setTimeout(0);
		}
		InvocationHandler handler = new LoggerProxy(this.serviceLogger, getProxyGrantingTicket());
        Class[] interfaces = this.serviceLogger.getClass().getInterfaces();
        Object myproxy = Proxy.newProxyInstance( this.getClass().getClassLoader(),interfaces,handler);
		return (Logger) myproxy;
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
					.setUsername(this.getUsername());
			((LogMonitorSoapBindingStub) this.serviceLogMonitor)
					.setPassword("");
			((LogMonitorSoapBindingStub) this.serviceLogMonitor).setTimeout(0);
		}
		InvocationHandler handler = new LogMonitorProxy(this.serviceLogMonitor, getProxyGrantingTicket());
        Class[] interfaces = this.serviceLogMonitor.getClass().getInterfaces();
        Object myproxy = Proxy.newProxyInstance( this.getClass().getClassLoader(),interfaces,handler);
		return (LogMonitor) myproxy;
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
					.setUsername(this.getUsername());
			((EditorSoapBindingStub) this.serviceEditor)
					.setPassword("");
			((EditorSoapBindingStub) this.serviceEditor).setTimeout(0);
		}
		InvocationHandler handler = new EditorProxy(this.serviceEditor, getProxyGrantingTicket());
        Class[] interfaces = this.serviceEditor.getClass().getInterfaces();
        Object myproxy = Proxy.newProxyInstance( this.getClass().getClassLoader(),interfaces,handler);
		return (Editor) myproxy;
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
					.setUsername(this.getUsername());
			((IngestMonitorSoapBindingStub) this.serviceIngestMonitor)
					.setPassword("");
			((IngestMonitorSoapBindingStub) this.serviceIngestMonitor)
					.setTimeout(0);
		}
		
		InvocationHandler handler = new IngestMonitorProxy(this.serviceIngestMonitor, getProxyGrantingTicket());
        Class[] interfaces = this.serviceIngestMonitor.getClass().getInterfaces();
        Object myproxy = Proxy.newProxyInstance( this.getClass().getClassLoader(),interfaces,handler);
		return (IngestMonitor) myproxy;
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
					.setUsername(this.getUsername());
			((AcceptSIPSoapBindingStub) this.serviceAcceptSIP)
					.setPassword("");
			((AcceptSIPSoapBindingStub) this.serviceAcceptSIP).setTimeout(0);
		}
		InvocationHandler handler = new AcceptSIPProxy(serviceAcceptSIP, getProxyGrantingTicket());
        Class[] interfaces = this.serviceAcceptSIP.getClass().getInterfaces();
        Object myproxy = Proxy.newProxyInstance( this.getClass().getClassLoader(),interfaces,handler);
		return (AcceptSIP) myproxy;
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
					.setUsername(this.getUsername());
			((IngestSoapBindingStub) this.serviceIngest)
					.setPassword("");
			((IngestSoapBindingStub) this.serviceIngest).setTimeout(0);
		}
		InvocationHandler handler = new IngestProxy(serviceIngest, getProxyGrantingTicket());
        Class[] interfaces = this.serviceIngest.getClass().getInterfaces();
        Object myproxy = Proxy.newProxyInstance( this.getClass().getClassLoader(),interfaces,handler);
		return (Ingest) myproxy;
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
					.setUsername(this.getUsername());
			((SchedulerSoapBindingStub) this.serviceScheduler)
					.setPassword("");
			((SchedulerSoapBindingStub) this.serviceScheduler).setTimeout(0);
		}
		InvocationHandler handler = new SchedulerProxy(serviceScheduler, getProxyGrantingTicket());
        Class[] interfaces = this.serviceScheduler.getClass().getInterfaces();
        Object myproxy = Proxy.newProxyInstance( this.getClass().getClassLoader(),interfaces,handler);
		return (Scheduler) myproxy;
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
					.setUsername(this.getUsername());
			((PluginsSoapBindingStub) this.servicePlugins)
					.setPassword("");
			((PluginsSoapBindingStub) this.servicePlugins).setTimeout(0);
		}
		InvocationHandler handler = new PluginsProxy(servicePlugins, getProxyGrantingTicket());
        Class[] interfaces = this.servicePlugins.getClass().getInterfaces();
        Object myproxy = Proxy.newProxyInstance( this.getClass().getClassLoader(),interfaces,handler);
		return (Plugins) myproxy;
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
					.setUsername(this.getUsername());
			((ReportsSoapBindingStub) this.serviceReports)
					.setPassword("");
			((ReportsSoapBindingStub) this.serviceReports).setTimeout(0);
		}
		InvocationHandler handler = new ReportsProxy(serviceReports, getProxyGrantingTicket());
        Class[] interfaces = this.serviceReports.getClass().getInterfaces();
        Object myproxy = Proxy.newProxyInstance( this.getClass().getClassLoader(),interfaces,handler);
		return (Reports) myproxy;
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
					.setUsername(this.getUsername());
			((StatisticsSoapBindingStub) this.serviceStatistics)
					.setPassword("");
			((StatisticsSoapBindingStub) this.serviceStatistics).setTimeout(0);
		}
		InvocationHandler handler = new StatisticsProxy(serviceStatistics, getProxyGrantingTicket());
        Class[] interfaces = this.serviceStatistics.getClass().getInterfaces();
        Object myproxy = Proxy.newProxyInstance( this.getClass().getClassLoader(),interfaces,handler);
		return (Statistics) myproxy;
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
					.setUsername(this.getUsername());
			((StatisticsMonitorSoapBindingStub) this.serviceStatisticsMonitor)
					.setPassword("");
			((StatisticsMonitorSoapBindingStub) this.serviceStatisticsMonitor)
					.setTimeout(0);
		}

        InvocationHandler handler = new StatisticsMonitorProxy(serviceStatisticsMonitor, getProxyGrantingTicket());
        Class[] interfaces = serviceStatisticsMonitor.getClass().getInterfaces();
        Object myproxy = Proxy.newProxyInstance( this.getClass().getClassLoader(),interfaces,handler);
		return (StatisticsMonitor) myproxy;
	}

	/**
	 * @return the authenticatedUser
	 * 
	 * @throws LoginException
	 * @throws RemoteException
	 */
	public User getAuthenticatedUser() throws LoginException, RemoteException {
		try{
			if(this.cup!=null){
				User principal = this.serviceLogin.getAuthenticatedUserCAS(this.casUtility.generateProxyTicket(getProxyGrantingTicket()));
				return principal;
			}else{
				String[] guestCredentials = this.serviceLogin.getGuestCredentials();
				User principal = this.serviceLogin.getAuthenticatedUserCAS(this.casUtility.generateProxyTicket(getProxyGrantingTicket()));
				return principal;
			}
		}catch(LoginException e){
			throw(e);
		}catch(RemoteException e){
			throw(e);
		}
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		if(cup!=null){
			return cup.getName();
		}else{
			return guestCredentials[0];
		}
	}


	/**
	 * Returns <code>true</code> if current user is the guest user and
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if current user is the guest user and
	 *         <code>false</code> otherwise.
	 */
	public boolean isGuestLogin() {
		return this.getUsername().equals(this.guestCredentials[0]);
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
			downloader = new Downloader(rodaCoreURL, getCasUserPrincipal(),this.casUtility);
		}
		return downloader;
	}

	private CASUserPrincipal getCasUserPrincipal() {
		try{
			if(this.cup!=null){
				return cup;
			}else{
				// FIXME empty string
				return casUtility.getCASUserPrincipal(guestCredentials[0],guestCredentials[1],"");
			}
		}catch(AuthenticationException ae){
			return null;
		}
	}

	private String getServiceAddress(String serviceName) {
		return this.rodaCoreURL + "/services/" + serviceName;
	}

	
	
	
	
	
	

	class UserManagementProxy implements InvocationHandler {
		private UserManagement wrapped;
		private String proxyGrantingTicket;
		public UserManagementProxy(UserManagement r,String proxyGrantingTicket) {
	        this.wrapped = r;
	        this.proxyGrantingTicket = proxyGrantingTicket;
	    }
	    public Object invoke(Object proxy, Method method, Object[] args)
	            throws Throwable {
	    	
	    	if(this.proxyGrantingTicket!=null && !this.proxyGrantingTicket.equalsIgnoreCase("")){
	    		String ticket = casUtility.generateProxyTicket(this.proxyGrantingTicket);
	    		((UserManagementSoapBindingStub) this.wrapped).setPassword(ticket);
	    	}
	    	logger.debug("Proxy: "+((UserManagementSoapBindingStub) this.wrapped).getUsername()+"/"+((UserManagementSoapBindingStub) this.wrapped).getPassword());
//	    	SOAPHeaderElement casAuthentication = new SOAPHeaderElement("http://www.keep.pt/","ticket");
//	    	casAuthentication.setValue(casUtility.generateProxyTicket(this.proxyGrantingTicket));
//			((UserManagementSoapBindingStub) this.wrapped).setHeader(casAuthentication);
	    	try {
	            return method.invoke(wrapped, args);
	        } catch (InvocationTargetException e) {
	            throw e.getCause();
	        }
	    }
	}
	
	class UserRegistrationProxy implements InvocationHandler {
		private UserRegistration wrapped;
		private String proxyGrantingTicket;
		public UserRegistrationProxy(UserRegistration r,String proxyGrantingTicket) {
	        this.wrapped = r;
	        this.proxyGrantingTicket = proxyGrantingTicket;
	    }
	    public Object invoke(Object proxy, Method method, Object[] args)
	            throws Throwable {
	    	if(this.proxyGrantingTicket!=null && !this.proxyGrantingTicket.equalsIgnoreCase("")){
	    		String ticket = casUtility.generateProxyTicket(this.proxyGrantingTicket);
	    		((UserRegistrationSoapBindingStub) this.wrapped).setPassword(ticket);
	    	}
	    	logger.debug("Proxy: "+((UserRegistrationSoapBindingStub) this.wrapped).getUsername()+"/"+((UserRegistrationSoapBindingStub) this.wrapped).getPassword());
//	    	SOAPHeaderElement casAuthentication = new SOAPHeaderElement("http://www.keep.pt/","ticket");
//	    	casAuthentication.setValue(casUtility.generateProxyTicket(this.proxyGrantingTicket));
//			((UserRegistrationSoapBindingStub) this.wrapped).setHeader(casAuthentication);
	    	try {
	            return method.invoke(wrapped, args);
	        } catch (InvocationTargetException e) {
	            throw e.getCause();
	        }
	    }
	}
	
	class UserBrowserProxy implements InvocationHandler {
		private UserBrowser wrapped;
		private String proxyGrantingTicket;
		public UserBrowserProxy(UserBrowser r,String proxyGrantingTicket) {
	        this.wrapped = r;
	        this.proxyGrantingTicket = proxyGrantingTicket;
	    }
	    public Object invoke(Object proxy, Method method, Object[] args)
	            throws Throwable {
	    	if(this.proxyGrantingTicket!=null && !this.proxyGrantingTicket.equalsIgnoreCase("")){
	    		String ticket = casUtility.generateProxyTicket(this.proxyGrantingTicket);
	    		((UserBrowserSoapBindingStub) this.wrapped).setPassword(ticket);
	    	}
	          logger.debug("Proxy: "+((UserBrowserSoapBindingStub) this.wrapped).getUsername()+"/"+((UserBrowserSoapBindingStub) this.wrapped).getPassword());

//	    	SOAPHeaderElement casAuthentication = new SOAPHeaderElement("http://www.keep.pt/","ticket");
//	    	casAuthentication.setValue(casUtility.generateProxyTicket(this.proxyGrantingTicket));
//			((UserBrowserSoapBindingStub) this.wrapped).setHeader(casAuthentication);
	          try {
		            return method.invoke(wrapped, args);
		        } catch (InvocationTargetException e) {
		            throw e.getCause();
		        }
	    }
	}
	
	class UserEditorProxy implements InvocationHandler {
		private UserEditor wrapped;
		private String proxyGrantingTicket;
		public UserEditorProxy(UserEditor r,String proxyGrantingTicket) {
	        this.wrapped = r;
	        this.proxyGrantingTicket = proxyGrantingTicket;
	    }
	    public Object invoke(Object proxy, Method method, Object[] args)
	            throws Throwable {
	    	if(this.proxyGrantingTicket!=null && !this.proxyGrantingTicket.equalsIgnoreCase("")){
	    		String ticket = casUtility.generateProxyTicket(this.proxyGrantingTicket);
	    		((UserEditorSoapBindingStub) this.wrapped).setPassword(ticket);
	    	}
	        logger.debug("Proxy: "+((UserEditorSoapBindingStub) this.wrapped).getUsername()+"/"+((UserEditorSoapBindingStub) this.wrapped).getPassword());

//	    	SOAPHeaderElement casAuthentication = new SOAPHeaderElement("http://www.keep.pt/","ticket");
//	    	casAuthentication.setValue(casUtility.generateProxyTicket(this.proxyGrantingTicket));
//			((UserEditorSoapBindingStub) this.wrapped).setHeader(casAuthentication);
	        try {
	            return method.invoke(wrapped, args);
	        } catch (InvocationTargetException e) {
	            throw e.getCause();
	        }
	    }
	}
	
	class ConfigurationManagerProxy implements InvocationHandler {
		private ConfigurationManager wrapped;
		private String proxyGrantingTicket;
		public ConfigurationManagerProxy(ConfigurationManager r,String proxyGrantingTicket) {
	        this.wrapped = r;
	        this.proxyGrantingTicket = proxyGrantingTicket;
	    }
	    public Object invoke(Object proxy, Method method, Object[] args)
	            throws Throwable {
	    	if(this.proxyGrantingTicket!=null && !this.proxyGrantingTicket.equalsIgnoreCase("")){
	    		String ticket = casUtility.generateProxyTicket(this.proxyGrantingTicket);
	    		((ConfigurationManagerSoapBindingStub) this.wrapped).setPassword(ticket);
	    	}
//	    	SOAPHeaderElement casAuthentication = new SOAPHeaderElement("http://www.keep.pt/","ticket");
//	    	casAuthentication.setValue(casUtility.generateProxyTicket(this.proxyGrantingTicket));
//			((BrowserSoapBindingStub) this.wrapped).setHeader(casAuthentication);
	    	try {
	            return method.invoke(wrapped, args);
	        } catch (InvocationTargetException e) {
	            throw e.getCause();
	        }
	    }
	}
	class BrowserProxy implements InvocationHandler {
		private Browser wrapped;
		private String proxyGrantingTicket;
		public BrowserProxy(Browser r,String proxyGrantingTicket) {
	        this.wrapped = r;
	        this.proxyGrantingTicket = proxyGrantingTicket;
	    }
	    public Object invoke(Object proxy, Method method, Object[] args)
	            throws Throwable {
	    	if(this.proxyGrantingTicket!=null && !this.proxyGrantingTicket.equalsIgnoreCase("")){
	    		String ticket = casUtility.generateProxyTicket(this.proxyGrantingTicket);
	    		((BrowserSoapBindingStub) this.wrapped).setPassword(ticket);
	    	}
	        logger.debug("Proxy: "+((BrowserSoapBindingStub) this.wrapped).getUsername()+"/"+((BrowserSoapBindingStub) this.wrapped).getPassword());

//	    	SOAPHeaderElement casAuthentication = new SOAPHeaderElement("http://www.keep.pt/","ticket");
//	    	casAuthentication.setValue(casUtility.generateProxyTicket(this.proxyGrantingTicket));
//			((BrowserSoapBindingStub) this.wrapped).setHeader(casAuthentication);
	        
	        try {
	            return method.invoke(wrapped, args);
	        } catch (InvocationTargetException e) {
	            throw e.getCause();
	        }
	    }
	}
	class LoggerProxy implements InvocationHandler {
		private Logger wrapped;
		private String proxyGrantingTicket;
		public LoggerProxy(Logger r,String proxyGrantingTicket) {
	        this.wrapped = r;
	        this.proxyGrantingTicket = proxyGrantingTicket;
	    }
	    public Object invoke(Object proxy, Method method, Object[] args)
	            throws Throwable {
	    	if(this.proxyGrantingTicket!=null && !this.proxyGrantingTicket.equalsIgnoreCase("")){
	    		String ticket = casUtility.generateProxyTicket(this.proxyGrantingTicket);
	    		((LoggerSoapBindingStub) this.wrapped).setPassword(ticket);
	    	}
	          logger.debug("Proxy: "+((LoggerSoapBindingStub) this.wrapped).getUsername()+"/"+((LoggerSoapBindingStub) this.wrapped).getPassword());

//	    	SOAPHeaderElement casAuthentication = new SOAPHeaderElement("http://www.keep.pt/","ticket");
//	    	casAuthentication.setValue(casUtility.generateProxyTicket(this.proxyGrantingTicket));
//			((LoggerSoapBindingStub) this.wrapped).setHeader(casAuthentication);
	          try {
		            return method.invoke(wrapped, args);
		        } catch (InvocationTargetException e) {
		            throw e.getCause();
		        }
	    }
	}
	class LogMonitorProxy implements InvocationHandler {
		private LogMonitor wrapped;
		private String proxyGrantingTicket;
		public LogMonitorProxy(LogMonitor r,String proxyGrantingTicket) {
	        this.wrapped = r;
	        this.proxyGrantingTicket = proxyGrantingTicket;
	    }
	    public Object invoke(Object proxy, Method method, Object[] args)
	            throws Throwable {
	    	if(this.proxyGrantingTicket!=null && !this.proxyGrantingTicket.equalsIgnoreCase("")){
	    		String ticket = casUtility.generateProxyTicket(this.proxyGrantingTicket);
	    		((LogMonitorSoapBindingStub) this.wrapped).setPassword(ticket);
	    	}
	            logger.debug("Proxy: "+((LogMonitorSoapBindingStub) this.wrapped).getUsername()+"/"+((LogMonitorSoapBindingStub) this.wrapped).getPassword());

//	    	SOAPHeaderElement casAuthentication = new SOAPHeaderElement("http://www.keep.pt/","ticket");
//	    	casAuthentication.setValue(casUtility.generateProxyTicket(this.proxyGrantingTicket));
//			((LogMonitorSoapBindingStub) this.wrapped).setHeader(casAuthentication);
	            try {
		            return method.invoke(wrapped, args);
		        } catch (InvocationTargetException e) {
		            throw e.getCause();
		        }
	    }
	}
	class EditorProxy implements InvocationHandler {
		private Editor wrapped;
		private String proxyGrantingTicket;
		public EditorProxy(Editor r,String proxyGrantingTicket) {
	        this.wrapped = r;
	        this.proxyGrantingTicket = proxyGrantingTicket;
	    }
	    public Object invoke(Object proxy, Method method, Object[] args)
	            throws Throwable {
	    	if(this.proxyGrantingTicket!=null && !this.proxyGrantingTicket.equalsIgnoreCase("")){
	    		String ticket = casUtility.generateProxyTicket(this.proxyGrantingTicket);
	    		((EditorSoapBindingStub) this.wrapped).setPassword(ticket);
	    	}
	              logger.debug("Proxy: "+((EditorSoapBindingStub) this.wrapped).getUsername()+"/"+((EditorSoapBindingStub) this.wrapped).getPassword());

//	    	SOAPHeaderElement casAuthentication = new SOAPHeaderElement("http://www.keep.pt/","ticket");
//	    	casAuthentication.setValue(casUtility.generateProxyTicket(this.proxyGrantingTicket));
//			((EditorSoapBindingStub) this.wrapped).setHeader(casAuthentication);
	              try {
	  	            return method.invoke(wrapped, args);
	  	        } catch (InvocationTargetException e) {
	  	            throw e.getCause();
	  	        }
	    }
	}
	class IngestMonitorProxy implements InvocationHandler {
		private IngestMonitor wrapped;
		private String proxyGrantingTicket;
		public IngestMonitorProxy(IngestMonitor r,String proxyGrantingTicket) {
	        this.wrapped = r;
	        this.proxyGrantingTicket = proxyGrantingTicket;
	    }
	    public Object invoke(Object proxy, Method method, Object[] args)
	            throws Throwable {
	    	if(this.proxyGrantingTicket!=null && !this.proxyGrantingTicket.equalsIgnoreCase("")){
	    		String ticket = casUtility.generateProxyTicket(this.proxyGrantingTicket);
	    		((IngestMonitorSoapBindingStub) this.wrapped).setPassword(ticket);
	    	}
                logger.debug("Proxy: "+((IngestMonitorSoapBindingStub) this.wrapped).getUsername()+"/"+((IngestMonitorSoapBindingStub) this.wrapped).getPassword());

//	    	SOAPHeaderElement casAuthentication = new SOAPHeaderElement("http://www.keep.pt/","ticket");
//	    	casAuthentication.setValue(casUtility.generateProxyTicket(this.proxyGrantingTicket));
//			((IngestMonitorSoapBindingStub) this.wrapped).setHeader(casAuthentication);
                try {
    	            return method.invoke(wrapped, args);
    	        } catch (InvocationTargetException e) {
    	            throw e.getCause();
    	        }
	    }
	}
	class AcceptSIPProxy implements InvocationHandler {
		private AcceptSIP wrapped;
		private String proxyGrantingTicket;
		public AcceptSIPProxy(AcceptSIP r,String proxyGrantingTicket) {
	        this.wrapped = r;
	        this.proxyGrantingTicket = proxyGrantingTicket;
	    }
	    public Object invoke(Object proxy, Method method, Object[] args)
	            throws Throwable {
	    	if(this.proxyGrantingTicket!=null && !this.proxyGrantingTicket.equalsIgnoreCase("")){
	    		String ticket = casUtility.generateProxyTicket(this.proxyGrantingTicket);
	    		((AcceptSIPSoapBindingStub) this.wrapped).setPassword(ticket);
	    	}
                logger.debug("Proxy: "+((AcceptSIPSoapBindingStub) this.wrapped).getUsername()+"/"+((AcceptSIPSoapBindingStub) this.wrapped).getPassword());

//	    	SOAPHeaderElement casAuthentication = new SOAPHeaderElement("http://www.keep.pt/","ticket");
//	    	casAuthentication.setValue(casUtility.generateProxyTicket(this.proxyGrantingTicket));
//			((AcceptSIPSoapBindingStub) this.wrapped).setHeader(casAuthentication);
                try {
    	            return method.invoke(wrapped, args);
    	        } catch (InvocationTargetException e) {
    	            throw e.getCause();
    	        }
	    }
	}
	class IngestProxy implements InvocationHandler {
		private Ingest wrapped;
		private String proxyGrantingTicket;
		public IngestProxy(Ingest r,String proxyGrantingTicket) {
	        this.wrapped = r;
	        this.proxyGrantingTicket = proxyGrantingTicket;
	    }
	    public Object invoke(Object proxy, Method method, Object[] args)
	            throws Throwable {
	    	if(this.proxyGrantingTicket!=null && !this.proxyGrantingTicket.equalsIgnoreCase("")){
	    		String ticket = casUtility.generateProxyTicket(this.proxyGrantingTicket);
	    		((IngestSoapBindingStub) this.wrapped).setPassword(ticket);
	    	}
                logger.debug("Proxy: "+((IngestSoapBindingStub) this.wrapped).getUsername()+"/"+((IngestSoapBindingStub) this.wrapped).getPassword());

//	    	SOAPHeaderElement casAuthentication = new SOAPHeaderElement("http://www.keep.pt/","ticket");
//	    	casAuthentication.setValue(casUtility.generateProxyTicket(this.proxyGrantingTicket));
//			((IngestSoapBindingStub) this.wrapped).setHeader(casAuthentication);
                try {
    	            return method.invoke(wrapped, args);
    	        } catch (InvocationTargetException e) {
    	            throw e.getCause();
    	        }
	    }
	}
	class SchedulerProxy implements InvocationHandler {
		private Scheduler wrapped;
		private String proxyGrantingTicket;
		public SchedulerProxy(Scheduler r,String proxyGrantingTicket) {
	        this.wrapped = r;
	        this.proxyGrantingTicket = proxyGrantingTicket;
	    }
	    public Object invoke(Object proxy, Method method, Object[] args)
	            throws Throwable {
	    	if(this.proxyGrantingTicket!=null && !this.proxyGrantingTicket.equalsIgnoreCase("")){
	    		String ticket = casUtility.generateProxyTicket(this.proxyGrantingTicket);
	    		((SchedulerSoapBindingStub) this.wrapped).setPassword(ticket);
	    	}
                logger.debug("Proxy: "+((SchedulerSoapBindingStub) this.wrapped).getUsername()+"/"+((SchedulerSoapBindingStub) this.wrapped).getPassword());

//	    	SOAPHeaderElement casAuthentication = new SOAPHeaderElement("http://www.keep.pt/","ticket");
//	    	casAuthentication.setValue(casUtility.generateProxyTicket(this.proxyGrantingTicket));
//			((SchedulerSoapBindingStub) this.wrapped).setHeader(casAuthentication);
                try {
    	            return method.invoke(wrapped, args);
    	        } catch (InvocationTargetException e) {
    	            throw e.getCause();
    	        }
	    }
	}
	
	class PluginsProxy implements InvocationHandler {
		private Plugins wrapped;
		private String proxyGrantingTicket;
		public PluginsProxy(Plugins r,String proxyGrantingTicket) {
	        this.wrapped = r;
	        this.proxyGrantingTicket = proxyGrantingTicket;
	    }
	    public Object invoke(Object proxy, Method method, Object[] args)
	            throws Throwable {
	    	if(this.proxyGrantingTicket!=null && !this.proxyGrantingTicket.equalsIgnoreCase("")){
	    		String ticket = casUtility.generateProxyTicket(this.proxyGrantingTicket);
	    		((PluginsSoapBindingStub) this.wrapped).setPassword(ticket);
	    	}
                logger.debug("Proxy: "+((PluginsSoapBindingStub) this.wrapped).getUsername()+"/"+((PluginsSoapBindingStub) this.wrapped).getPassword());

//	    	SOAPHeaderElement casAuthentication = new SOAPHeaderElement("http://www.keep.pt/","ticket");
//	    	casAuthentication.setValue(casUtility.generateProxyTicket(this.proxyGrantingTicket));
//			((PluginsSoapBindingStub) this.wrapped).setHeader(casAuthentication);
                try {
    	            return method.invoke(wrapped, args);
    	        } catch (InvocationTargetException e) {
    	            throw e.getCause();
    	        }
	    }
	}
	
	class ReportsProxy implements InvocationHandler {
		private Reports wrapped;
		private String proxyGrantingTicket;
		public ReportsProxy(Reports r,String proxyGrantingTicket) {
	        this.wrapped = r;
	        this.proxyGrantingTicket = proxyGrantingTicket;
	    }
	    public Object invoke(Object proxy, Method method, Object[] args)
	            throws Throwable {
	    	if(this.proxyGrantingTicket!=null && !this.proxyGrantingTicket.equalsIgnoreCase("")){
	    		String ticket = casUtility.generateProxyTicket(this.proxyGrantingTicket);
	    		((ReportsSoapBindingStub) this.wrapped).setPassword(ticket);
	    	}
                logger.debug("Proxy: "+((ReportsSoapBindingStub) this.wrapped).getUsername()+"/"+((ReportsSoapBindingStub) this.wrapped).getPassword());

//	    	SOAPHeaderElement casAuthentication = new SOAPHeaderElement("http://www.keep.pt/","ticket");
//	    	casAuthentication.setValue(casUtility.generateProxyTicket(this.proxyGrantingTicket));
//			((ReportsSoapBindingStub) this.wrapped).setHeader(casAuthentication);
                try {
    	            return method.invoke(wrapped, args);
    	        } catch (InvocationTargetException e) {
    	            throw e.getCause();
    	        }
	    }
	}
	class StatisticsProxy implements InvocationHandler {
		private Statistics wrapped;
		private String proxyGrantingTicket;
		public StatisticsProxy(Statistics r,String proxyGrantingTicket) {
	        this.wrapped = r;
	        this.proxyGrantingTicket = proxyGrantingTicket;
	    }
	    public Object invoke(Object proxy, Method method, Object[] args)
	            throws Throwable {
	    	if(this.proxyGrantingTicket!=null && !this.proxyGrantingTicket.equalsIgnoreCase("")){
	    		String ticket = casUtility.generateProxyTicket(this.proxyGrantingTicket);
	    		((StatisticsSoapBindingStub) this.wrapped).setPassword(ticket);
	    	}
                logger.debug("Proxy: "+((StatisticsSoapBindingStub) this.wrapped).getUsername()+"/"+((StatisticsSoapBindingStub) this.wrapped).getPassword());

//	    	SOAPHeaderElement casAuthentication = new SOAPHeaderElement("http://www.keep.pt/","ticket");
//	    	casAuthentication.setValue(casUtility.generateProxyTicket(this.proxyGrantingTicket));
//			((StatisticsSoapBindingStub) this.wrapped).setHeader(casAuthentication);
                try {
    	            return method.invoke(wrapped, args);
    	        } catch (InvocationTargetException e) {
    	            throw e.getCause();
    	        }
	    }
	}
	class SearchProxy implements InvocationHandler {
		private Search wrapped;
		private String proxyGrantingTicket;
		public SearchProxy(Search r,String proxyGrantingTicket) {
	        this.wrapped = r;
	        this.proxyGrantingTicket = proxyGrantingTicket;
	    }
	    public Object invoke(Object proxy, Method method, Object[] args)
	            throws Throwable {
	    	if(this.proxyGrantingTicket!=null && !this.proxyGrantingTicket.equalsIgnoreCase("")){
	    		String ticket = casUtility.generateProxyTicket(this.proxyGrantingTicket);
	    		((SearchSoapBindingStub) this.wrapped).setPassword(ticket);
	    	}
                logger.debug("Proxy: "+((SearchSoapBindingStub) this.wrapped).getUsername()+"/"+((SearchSoapBindingStub) this.wrapped).getPassword());

//	    	SOAPHeaderElement casAuthentication = new SOAPHeaderElement("http://www.keep.pt/","ticket");
//	    	casAuthentication.setValue(casUtility.generateProxyTicket(this.proxyGrantingTicket));
//			((SearchSoapBindingStub) this.wrapped).setHeader(casAuthentication);
                try {
    	            return method.invoke(wrapped, args);
    	        } catch (InvocationTargetException e) {
    	            throw e.getCause();
    	        }
	    }
	}
	
	
	class StatisticsMonitorProxy implements InvocationHandler {
	    private StatisticsMonitor wrapped;
	    private String proxyGrantingTicket;
	    public StatisticsMonitorProxy(StatisticsMonitor r,String proxyGrantingTicket) {
	        this.wrapped = r;
	        this.proxyGrantingTicket = proxyGrantingTicket;
	    }
	    public Object invoke(Object proxy, Method method, Object[] args)
	            throws Throwable {
	    	if(this.proxyGrantingTicket!=null && !this.proxyGrantingTicket.equalsIgnoreCase("")){
	    		String ticket = casUtility.generateProxyTicket(this.proxyGrantingTicket);
	    		((StatisticsMonitorSoapBindingStub) this.wrapped).setPassword(ticket);
	    	}
                logger.debug("Proxy: "+((StatisticsMonitorSoapBindingStub) this.wrapped).getUsername()+"/"+((StatisticsMonitorSoapBindingStub) this.wrapped).getPassword());

//	    	SOAPHeaderElement casAuthentication = new SOAPHeaderElement("http://www.keep.pt/","ticket");
//	    	casAuthentication.setValue(casUtility.generateProxyTicket(this.proxyGrantingTicket));
//			((StatisticsMonitorSoapBindingStub) this.wrapped).setHeader(casAuthentication);
                try {
    	            return method.invoke(wrapped, args);
    	        } catch (InvocationTargetException e) {
    	            throw e.getCause();
    	        }
	    }
	}
	
	public String getProxyGrantingTicket(){
		try{
			if(this.cup!=null){
				return this.cup.getProxyGrantingTicket();
			}else{
				return this.casUtility.getProxyGrantingTicket(guestCredentials[0], guestCredentials[1]);
			}
		}catch(AuthenticationException ae){
			return null;
		}
	}
	
	public URL getIngestSubmitUrl() throws IOException {
		return new URL(this.rodaCoreURL + "/SIPUpload");
	}
	
	
	
	
	
	
	
	
	
}
