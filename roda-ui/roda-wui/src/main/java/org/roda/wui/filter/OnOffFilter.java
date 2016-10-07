package org.roda.wui.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.configuration.Configuration;
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
   * Inner filter to which all calls to this filter will be delegated.
   */
  private Filter innerFilter;
  /**
   * This filter is ON?
   */
  private Boolean isOn = null;

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
    final String innerFilterClass = filterConfig.getInitParameter("inner-filter-class");
    try {
      innerFilter = (Filter) Class.forName(innerFilterClass).newInstance();
      innerFilter.init(filterConfig);
    } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      throw new ServletException("Error instantiating inner filter - " + e.getMessage(), e);
    }
    initOnOff();
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
    this.innerFilter.destroy();
  }

  /**
   * Is this filter on?
   * 
   * @return <code>true</code> if the Filter is on and <code>false</code>
   *         otherwise.
   */
  private boolean isOn() {
    if (this.isOn == null) {
      initOnOff();
    }
    return this.isOn != null && this.isOn;
  }

  /**
   * Init on/off configuration.
   */
  private void initOnOff() {
    final Configuration rodaConfig = RodaCoreFactory.getRodaConfiguration();
    if (rodaConfig == null) {
      LOGGER.info(
        "RodaCoreFactory.getRodaConfiguration() is null. " + "Assuming filter is OFF, until next call to doFilter()");
    } else {
      final List<String> activeFilters = Arrays.asList(rodaConfig.getStringArray("ui.auth.filter"));
      this.isOn = activeFilters.contains(this.innerFilter.getClass().getName());
    }
  }
}
