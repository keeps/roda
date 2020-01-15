/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui;

import javax.servlet.http.HttpServlet;

import org.jasig.cas.client.session.SingleSignOutHttpSessionListener;
import org.roda.wui.filter.OnOffFilter;
import org.roda.wui.servlets.RodaWuiServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class RODA {
  public static void main(String[] args) {
    SpringApplication.run(RODA.class, args);
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
        registry
          .addResourceHandler("/org.roda.wui.RodaWUI/**")
          .addResourceLocations("classpath:/org.roda.wui.RodaWUI/"); 
    }
  }

  @Bean
  public ServletRegistrationBean<HttpServlet> initService() {
    ServletRegistrationBean<HttpServlet> bean;
    bean = new ServletRegistrationBean<>(new RodaWuiServlet(), "/info");
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
    registrationBean.addInitParameter("inner-filter-class", "org.jasig.cas.client.session.SingleSignOutFilter");
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
      "org.jasig.cas.client.validation.Cas30ProxyReceivingTicketValidationFilter");
    registrationBean.addInitParameter("config-prefix", "ui.filter.cas");
    registrationBean.addInitParameter("casServerUrlPrefix", "https://localhost:8443/cas");
    registrationBean.addInitParameter("serverName", "https://localhost:8888");
    registrationBean.addInitParameter("exceptionOnValidationFailure", "false");
    registrationBean.addInitParameter("redirectAfterValidation", "false");
    registrationBean.addInitParameter("proxyCallbackUrl", "https://localhost:8888/callback");
    registrationBean.addInitParameter("proxyReceptorUrl", "/callback");
    registrationBean.addInitParameter("acceptAnyProxy", "true");
    registrationBean.addUrlPatterns("/*");

    return registrationBean;
  }

  @Bean
  public FilterRegistrationBean<OnOffFilter> casAuthenticationFilter() {
    FilterRegistrationBean<OnOffFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new OnOffFilter());
    registrationBean.setName("CasAuthenticationFilter");
    registrationBean.addInitParameter("inner-filter-class", "org.jasig.cas.client.authentication.AuthenticationFilter");
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
      "org.jasig.cas.client.util.HttpServletRequestWrapperFilter");
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
    registrationBean.addUrlPatterns("/api/v1/*");

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

  /*******************************************
   * REST related servlets/servlet-mappings
   *******************************************/
  @Bean
  public ServletRegistrationBean<HttpServlet> restAPI() {
    ServletRegistrationBean<HttpServlet> bean;
    bean = new ServletRegistrationBean<>(new org.glassfish.jersey.servlet.ServletContainer());
    bean.addInitParameter("javax.ws.rs.Application", "org.roda.wui.api.RestApplication");

    bean.setLoadOnStartup(2);
    bean.addUrlMappings("/api/*");
    return bean;
  }

  @Bean
  public ServletRegistrationBean<HttpServlet> clientLogger() {
    ServletRegistrationBean<HttpServlet> bean;
    bean = new ServletRegistrationBean<>(new org.roda.wui.common.server.ClientLoggerImpl());
    bean.setLoadOnStartup(2);
    bean.addUrlMappings("/gwtrpc/wuilogger");
    return bean;
  }

  @Bean
  public ServletRegistrationBean<HttpServlet> userManagementService() {
    ServletRegistrationBean<HttpServlet> bean;
    bean = new ServletRegistrationBean<>(new org.roda.wui.server.management.UserManagementServiceImpl());
    bean.addUrlMappings("/gwtrpc/UserManagementService");
    return bean;
  }

  @Bean
  public ServletRegistrationBean<HttpServlet> userLoginService() {
    ServletRegistrationBean<HttpServlet> bean;
    bean = new ServletRegistrationBean<>(new org.roda.wui.server.common.UserLoginServiceImpl());
    bean.addUrlMappings("/gwtrpc/userlogin");
    return bean;
  }

  @Bean
  public ServletRegistrationBean<HttpServlet> browserService() {
    ServletRegistrationBean<HttpServlet> bean;
    bean = new ServletRegistrationBean<>(new org.roda.wui.server.browse.BrowserServiceImpl());
    bean.addUrlMappings("/gwtrpc/browserservice");
    return bean;
  }

  // TODO: add welcome page
  // TODO: add error handler
  // TODO: add security constraints
}