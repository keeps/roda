package org.roda.wui.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.apereo.cas.client.session.SingleSignOutHttpSessionListener;
import org.roda.wui.filter.OnOffFilter;
import org.roda.wui.filter.SecurityHeadersFilter;
import org.roda.wui.servlets.ContextListener;
import org.roda.wui.servlets.RodaWuiServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.ServletContextListener;
import jakarta.servlet.http.HttpServlet;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Configuration
public class RodaConfig {
  @Autowired
  AutowireCapableBeanFactory beanFactory;

  @Bean
  public ServletListenerRegistrationBean<ServletContextListener> registerServletContext() {
    return new ServletListenerRegistrationBean<>(new ContextListener());
  }

  @Bean
  public ServletRegistrationBean<HttpServlet> initService() {
    ServletRegistrationBean<HttpServlet> bean = new ServletRegistrationBean<>();
    final RodaWuiServlet servlet = new RodaWuiServlet();
    beanFactory.autowireBean(servlet);
    bean.setServlet(servlet);
    bean.addUrlMappings("/info");
    bean.setLoadOnStartup(1);
    return bean;
  }

  @Bean
  public FilterRegistrationBean<OnOffFilter> internalWebAuthFilter() {
    FilterRegistrationBean<OnOffFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new OnOffFilter());
    registrationBean.setName("InternalWebAuthFilter");
    registrationBean.addInitParameter("inner-filter-class", "org.roda.wui.filter.InternalWebAuthFilter");
    registrationBean.addInitParameter("config-prefix", "ui.filter.internal");
    registrationBean.addUrlPatterns("/login", "/logout");

    return registrationBean;
  }

  @Bean
  public FilterRegistrationBean<OnOffFilter> internalApiAuthFilter() {
    FilterRegistrationBean<OnOffFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new OnOffFilter());
    registrationBean.setName("InternalApiAuthFilter");
    registrationBean.addInitParameter("inner-filter-class", "org.roda.wui.filter.InternalApiAuthFilter");
    registrationBean.addInitParameter("config-prefix", "ui.filter.internal");

    // Realm to be used
    registrationBean.addInitParameter("realm", "RODA REST API");

    // Comma separated list of relative paths to exclude in filter logic (using
    // regular expressions for extra power)
    registrationBean.addInitParameter("exclusions", "^/swagger.json,^/v1/theme/?");

    registrationBean.addUrlPatterns("/api/*");

    return registrationBean;
  }

  @Bean
  public FilterRegistrationBean<OnOffFilter> casSingleSignOutFilter() {
    FilterRegistrationBean<OnOffFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new OnOffFilter());
    registrationBean.setName("CasSingleSignOutFilter");
    registrationBean.addInitParameter("inner-filter-class", "org.apereo.cas.client.session.SingleSignOutFilter");
    registrationBean.addInitParameter("config-prefix", "ui.filter.cas");
    registrationBean.addInitParameter("casServerUrlPrefix", "http://localhost:8888/cas");
    registrationBean.addUrlPatterns("/*");

    return registrationBean;
  }

  @Bean
  public SingleSignOutHttpSessionListener singleSignOutHttpSessionListener() {
    return new SingleSignOutHttpSessionListener();
  }

  @Bean
  public FilterRegistrationBean<OnOffFilter> casValidationFilter() {
    FilterRegistrationBean<OnOffFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new OnOffFilter());
    registrationBean.setName("CasValidationFilter");
    registrationBean.addInitParameter("inner-filter-class",
      "org.apereo.cas.client.validation.Cas30ProxyReceivingTicketValidationFilter");
    registrationBean.addInitParameter("config-prefix", "ui.filter.cas");
    registrationBean.addInitParameter("casServerUrlPrefix", "https://localhost:8443/cas");
    registrationBean.addInitParameter("serverName", "https://localhost:8888");
    registrationBean.addInitParameter("exceptionOnValidationFailure", "false");
    registrationBean.addInitParameter("redirectAfterValidation", "false");
    registrationBean.addInitParameter("proxyCallbackUrl", "");
    registrationBean.addInitParameter("proxyReceptorUrl", "");
    registrationBean.addInitParameter("acceptAnyProxy", "false");
    registrationBean.addUrlPatterns("/*");

    return registrationBean;
  }

  @Bean
  public FilterRegistrationBean<OnOffFilter> casAuthenticationFilter() {
    FilterRegistrationBean<OnOffFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new OnOffFilter());
    registrationBean.setName("CasAuthenticationFilter");
    registrationBean.addInitParameter("inner-filter-class",
      "org.apereo.cas.client.authentication.AuthenticationFilter");
    registrationBean.addInitParameter("config-prefix", "ui.filter.cas");
    registrationBean.addInitParameter("casServerLoginUrl", "https://localhost:8443/cas/login");
    registrationBean.addUrlPatterns("/login");

    return registrationBean;
  }

  @Bean
  public FilterRegistrationBean<OnOffFilter> casRequestWrapperFilter() {
    FilterRegistrationBean<OnOffFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new OnOffFilter());
    registrationBean.setName("CasRequestWrapperFilter");
    registrationBean.addInitParameter("inner-filter-class",
      "org.apereo.cas.client.util.HttpServletRequestWrapperFilter");
    registrationBean.addInitParameter("config-prefix", "ui.filter.cas");
    registrationBean.addUrlPatterns("/*");

    return registrationBean;
  }

  @Bean
  public FilterRegistrationBean<OnOffFilter> casApiAuthFilter() {
    FilterRegistrationBean<OnOffFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new OnOffFilter());
    registrationBean.setName("CasApiAuthFilter");
    registrationBean.addInitParameter("inner-filter-class", "org.roda.wui.filter.CasApiAuthFilter");
    registrationBean.addInitParameter("config-prefix", "ui.filter.cas");
    registrationBean.addInitParameter("casServerUrlPrefix", "https://localhost:8443/cas");
    registrationBean.addInitParameter("exclusions", "^/swagger.json,^/v1/theme/?,^/v1/auth/ticket?");
    registrationBean.addUrlPatterns("/api/v1/*", "/api/v2/*");

    return registrationBean;
  }

  @Bean
  public FilterRegistrationBean<OnOffFilter> casWebAuthFilter() {
    FilterRegistrationBean<OnOffFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new OnOffFilter());
    registrationBean.setName("CasWebAuthFilter");
    registrationBean.addInitParameter("inner-filter-class", "org.roda.wui.filter.CasWebAuthFilter");
    registrationBean.addInitParameter("config-prefix", "ui.filter.cas");
    registrationBean.addInitParameter("casServerLogoutUrl", "https://localhost:8443/cas/logout");

    registrationBean.addUrlPatterns("/login", "/logout");

    return registrationBean;
  }

  @Bean
  public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
    FilterRegistrationBean<SecurityHeadersFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new SecurityHeadersFilter());
    registrationBean.addUrlPatterns("/*"); // Apply the filter to all requests
    return registrationBean;
  }

  @Bean
  public ServletContextInitializer servletContextInitializer() {
    return new ServletContextInitializer() {

      @Override
      public void onStartup(ServletContext servletContext) throws ServletException {
        servletContext.getSessionCookieConfig().setSecure(true);
        servletContext.getSessionCookieConfig().setHttpOnly(true);
      }
    };
  }

  @Configuration(proxyBeanMethods = false)
  public class SameSiteConfiguration {
    @Bean
    public CookieSameSiteSupplier applicationCookieSameSiteSupplier() {
      return CookieSameSiteSupplier.ofStrict();
    }
  }

  @Configuration
  public static class DefaultView implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
      registry.addViewController("/").setViewName("forward:/Main.html");
      registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
      registry.addResourceHandler("/org.roda.wui.RodaWUI/**").addResourceLocations("classpath:/org.roda.wui.RodaWUI/");
    }
  }

  // TODO: add welcome page
  // TODO: add error handler
  // TODO: add security constraints
}
