package pt.gov.dgarq.roda.wui.filter;

import java.io.File;
import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jasig.cas.client.authentication.DefaultGatewayResolverImpl;
import org.jasig.cas.client.authentication.GatewayResolver;
import org.jasig.cas.client.util.CommonUtils;

import pt.gov.dgarq.roda.common.RodaClientFactory;
import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.servlet.RODAFilter;
import pt.gov.dgarq.roda.wui.common.server.UserLoginServiceImpl;

public class TicketInterceptorFilter extends RODAFilter {
	static final private Logger logger = Logger
			.getLogger(TicketInterceptorFilter.class);
	
	protected String configFile = "cas-filter.properties";
	protected String casLoginURL = null;
	private GatewayResolver gatewayStorage = new DefaultGatewayResolverImpl();
	
	
	public void init(FilterConfig filterConfig) throws ServletException {

          super.init(filterConfig);

          Configuration configuration = null;
          try {

                  configuration = getConfiguration(configFile);

          } catch (ConfigurationException e) {
                  logger.error("Error reading configuration file " + configFile
                                  + " - " + e.getMessage());
          }

          if (configuration != null) {
                  casLoginURL = configuration.getString("roda.cas.url")+"/login";
          } 
          


          //this.authenticationCache = new AuthenticationCache();

          logger.info(getClass().getSimpleName() + " initialized ok");
  }

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest servletRequest = (HttpServletRequest) request;
		HttpServletResponse servletResponse = (HttpServletResponse) response;

		boolean needChain = true;
		try {
			if (request.getParameter("ticket") != null) {
			  logger.info("Ticket in request");
				String ticket = request.getParameter("ticket");
				String cleanURL = getCleanURL(servletRequest);
				logger.info("Clean URL:"+cleanURL);
				UserLoginServiceImpl.getInstance()
						.loginCAS(servletRequest.getSession(true),
								cleanURL.toString(), ticket);
				needChain = false;
				logger.info("Redirecting to "+cleanURL.toString());
				servletResponse.sendRedirect(cleanURL.toString());
			}else{
			  logger.info("No ticket in request");
			  RODAClient client = RodaClientFactory.getRodaClient(servletRequest.getSession(true));
			  if(client!=null){
			    logger.info("Client not null");
			  }
			  String cleanURL = getCleanURL(servletRequest);

			  
			  if(client.isGuestLogin()){
			    logger.info("Guest Login");
			    
			    
			    
			    String urlToRedirectTo = CommonUtils.constructRedirectUrl(casLoginURL,"service", cleanURL, false, true);
			    if(!this.gatewayStorage.hasGatewayedAlready(servletRequest, urlToRedirectTo)){
			      logger.info("Not gatewayed...");
			      urlToRedirectTo = this.gatewayStorage.storeGatewayInformation(servletRequest, urlToRedirectTo);
                              needChain=false;
                              logger.info("Guest redirected to "+urlToRedirectTo);
                              servletResponse.sendRedirect(urlToRedirectTo);
			    }else{
			      needChain=false;
	                      chain.doFilter(servletRequest, servletResponse);
			    }
                          }else{
                            logger.info("Already gatewayed... Do chain...");
                            needChain=false;
			    chain.doFilter(servletRequest, servletResponse);
			  }
    			    
			}
		} catch (RODAException re) {
			logger.error("Error in filter: "+re.getMessage(), re);
		}
		if(needChain){
		  chain.doFilter(servletRequest, servletResponse);
		}
	}

	private String getCleanURL(HttpServletRequest servletRequest) {
	  String url = servletRequest.getRequestURL().toString();
          String cleanQueryString = removeParameterFromQueryString(
                          servletRequest.getQueryString(), "ticket");
          StringBuilder cleanURL = new StringBuilder();
          logger.debug("Clean query string: "+cleanQueryString);
          if(!StringUtils.isBlank(cleanQueryString)){
            cleanURL.append(url).append('?').append(cleanQueryString);
          }else{
            cleanURL.append(url);
          }
          return cleanURL.toString();
	}

      @Override
	public void destroy() {
		logger.info("destroy()");
	}


	public static String removeParameterFromQueryString(String queryString,
			String paramToRemove) {
	  if(queryString!=null){
		String oneParam = "^" + paramToRemove + "(=[^&]*)$";
		String begin = "^" + paramToRemove + "(=[^&]*)(&?)";
		String end = "&" + paramToRemove + "(=[^&]*)$";
		String middle = "(?<=[&])" + paramToRemove + "(=[^&]*)&";
		String removedMiddleParams = queryString.replaceAll(middle, "");
		String removedBeginParams = removedMiddleParams.replaceAll(begin, "");
		String removedEndParams = removedBeginParams.replaceAll(end, "");
		String cleanURL = removedEndParams.replaceAll(oneParam, "");
		  
		return cleanURL;
      	}else{
      	  return null;
      	}
	}
	
	private Configuration getConfiguration(String configurationFile)
          throws ConfigurationException {

  File RODA_HOME = null;
  if (System.getProperty("roda.home") != null) {
          RODA_HOME = new File(System.getProperty("roda.home"));//$NON-NLS-1$
          logger.info("RODA_HOME defined as " + RODA_HOME);
  } else if (System.getenv("RODA_HOME") != null) {
          RODA_HOME = new File(System.getenv("RODA_HOME")); //$NON-NLS-1$
          logger.info("RODA_HOME defined as " + RODA_HOME);
  } else {
          RODA_HOME = new File("."); //$NON-NLS-1$
          logger.info("RODA_HOME not defined. Using current directory '" + RODA_HOME + "'");
  }
  
  File RODA_CONFIG_DIRECTORY = new File(RODA_HOME, "config"); //$NON-NLS-1$

  PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
  propertiesConfiguration.setDelimiterParsingDisabled(true);

  File externalConfigurationFile = new File(RODA_CONFIG_DIRECTORY,
                  configurationFile);

  if (externalConfigurationFile.isFile()) {
          propertiesConfiguration.load(externalConfigurationFile);
          logger.debug("Loading configuration " + externalConfigurationFile);
  } else {
          propertiesConfiguration = null;
          logger.debug("Configuration " + configurationFile
                          + " doesn't exist");
  }

  return propertiesConfiguration;
}

}
