/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/roda
 */
package org.roda.wui.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A filter that can be turned on/off using RODA configuration file.
 */
public class OnOffFilter implements Filter {
  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(OnOffFilter.class);
  /**
   * Inner filter class parameter name.
   */
  private static final String PARAM_INNER_FILTER_CLASS = "inner-filter-class";
  /**
   * Configuration values prefix parameter name.
   */
  private static final String PARAM_CONFIG_PREFIX = "config-prefix";
  /**
   * Inner filter to which all calls to this filter will be delegated.
   */
  private Filter innerFilter = null;
  /**
   * This filter is ON?
   */
  private Boolean isOn = null;
  /**
   * The filter configuration from web.xml.
   */
  private FilterConfig webXmlFilterConfig = null;
  /**
   * Combined filter config.
   */
  private OnOffFilterConfig filterConfig = null;

  @Override
  @SuppressWarnings("checkstyle:hiddenfield")
  public void init(final FilterConfig filterConfig) throws ServletException {
    this.webXmlFilterConfig = filterConfig;
  }

  @Override
  public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
    final FilterChain filterChain) throws IOException, ServletException {
    if (isOn()) {
      this.innerFilter.doFilter(servletRequest, servletResponse, filterChain);
    } else {
      filterChain.doFilter(servletRequest, servletResponse);
    }
  }

  @Override
  public void destroy() {
    if (this.innerFilter != null) {
      this.innerFilter.destroy();
    }
  }

  /**
   * Is this filter on?
   *
   * @return <code>true</code> if the Filter is on and <code>false</code>
   *         otherwise.
   * @throws ServletException
   *           if some error occurs.
   */
  private synchronized boolean isOn() throws ServletException {
    if (this.isOn == null && isConfigAvailable()) {
      initInnerFilter();
    }
    return this.isOn != null && this.isOn;
  }

  /**
   * Is RODA configuration available?
   *
   * @return <code>true</code> if RODA configuration is available and
   *         <code>false</code> otherwise.
   */
  private boolean isConfigAvailable() {
    return RodaCoreFactory.getRodaConfiguration() != null;
  }

  /**
   * Init inner filter.
   *
   * @throws ServletException
   *           if some error occurs.
   */
  private void initInnerFilter() throws ServletException {
    final Configuration rodaConfig = RodaCoreFactory.getRodaConfiguration();
    if (rodaConfig == null) {
      LOGGER.info("RODA configuration not available yet. Delaying init of {}.",
        this.webXmlFilterConfig.getInitParameter(PARAM_INNER_FILTER_CLASS));
    } else {
      final String innerFilterClass = this.webXmlFilterConfig.getInitParameter(PARAM_INNER_FILTER_CLASS);
      final String configPrefix = this.webXmlFilterConfig.getInitParameter(PARAM_CONFIG_PREFIX);
      if (rodaConfig.getBoolean(configPrefix + ".enabled", false)) {
        try {
          this.innerFilter = (Filter) Class.forName(innerFilterClass).newInstance();
          this.innerFilter.init(getFilterConfig());
          this.isOn = true;
        } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException e) {
          this.isOn = false;
          throw new ServletException("Error instantiating inner filter - " + e.getMessage(), e);
        }
      } else {
        this.isOn = false;
      }
    }
    LOGGER.info("{} is {}", getFilterConfig().getFilterName(), (this.isOn ? "ON" : "OFF"));
  }

  /**
   * Return the combined filter configuration.
   *
   * @return the combined filter configuration.
   */
  private FilterConfig getFilterConfig() {
    if (this.filterConfig == null) {
      this.filterConfig = new OnOffFilterConfig(this.webXmlFilterConfig, RodaCoreFactory.getRodaConfiguration());
    }
    return this.filterConfig;
  }

  /**
   * {@link FilterConfig} implementation that combines web.xml &lt;init-param>
   * and RODA configuration values.
   */
  private class OnOffFilterConfig implements FilterConfig {
    /**
     * Default {@link FilterConfig} (from web.xml).
     */
    private final FilterConfig filterConfig;
    /**
     * RODA configuration.
     */
    private final Configuration rodaConfig;
    /**
     * RODA configuration prefix for this filter.
     */
    private final String rodaConfigPrefix;
    /**
     * The list of init parameter names.
     */
    private List<String> configNames;

    /**
     * Constructor.
     *
     * @param filterConfig
     *          default filter configuration (from web.xml).
     * @param rodaConfig
     *          RODA configuration.
     */
    OnOffFilterConfig(final FilterConfig filterConfig, final Configuration rodaConfig) {
      this.filterConfig = filterConfig;
      this.rodaConfig = rodaConfig;
      final String configPrefix = this.filterConfig.getInitParameter(PARAM_CONFIG_PREFIX);
      if (StringUtils.isBlank(configPrefix)) {
        this.rodaConfigPrefix = String.format("ui.filter.%s", this.filterConfig.getFilterName());
      } else {
        this.rodaConfigPrefix = configPrefix;
      }
    }

    @Override
    public String getFilterName() {
      return this.filterConfig.getFilterName();
    }

    @Override
    public ServletContext getServletContext() {
      return this.filterConfig.getServletContext();
    }

    @Override
    public String getInitParameter(final String name) {
      String value = getRodaInitParameter(name);
      if (value == null) {
        value = this.filterConfig.getInitParameter(name);
      }
      return value;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
      if (this.configNames == null) {
        this.configNames = new ArrayList<>();
        final Enumeration<String> filterNames = this.filterConfig.getInitParameterNames();
        while (filterNames.hasMoreElements()) {
          this.configNames.add(filterNames.nextElement());
        }
        final Iterator<String> rodaNames = this.rodaConfig.getKeys(this.rodaConfigPrefix);
        while (rodaNames.hasNext()) {
          this.configNames.add(rodaNames.next().replace(this.rodaConfigPrefix + ".", ""));
        }
      }
      return Collections.enumeration(this.configNames);
    }

    /**
     * Get filter init parameter from RODA configuration.
     *
     * @param name
     *          the parameter name.
     * @return the parameter value.
     */
    private String getRodaInitParameter(final String name) {
      return this.rodaConfig.getString(this.rodaConfigPrefix + "." + name);
    }
  }
}
