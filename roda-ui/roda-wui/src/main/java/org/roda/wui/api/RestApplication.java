/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.moxy.xml.MoxyXmlFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.roda.wui.api.v1.utils.ApiOriginFilter;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.servlets.ContextListener;

import io.swagger.v3.jaxrs2.SwaggerSerializers;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;

@ApplicationPath("/api")
public class RestApplication extends ResourceConfig {
  public RestApplication() {
    super();

    OpenAPI oas = new OpenAPI();
    Info info = new Info().title("RODA API").description("REST AIP for RODA")
      .contact(new Contact().email("info@keep.pt").name("KEEP SOLUTIONS")
        .url("https://www.keep.pt/en/contacts-proposals-information-telephone-address"))
      .license(new License().name("LGPLv3").url("http://www.gnu.org/licenses/lgpl-3.0.html")).version("1.0.0");

    oas.info(info);

    ServletContext servletContext = ContextListener.getServletContext();
    if (StringUtils.isNotBlank(servletContext.getContextPath())) {
      oas.addServersItem(new Server().url(servletContext.getContextPath()));
    }

    Components components = new Components();
    SecurityScheme securityScheme = new SecurityScheme().type(Type.HTTP).scheme("basic");
    components.addSecuritySchemes("basicAuth", securityScheme);

    oas.addSecurityItem(new SecurityRequirement().addList("basicAuth"));

    oas.components(components);

    OpenApiResource openApiResource = new OpenApiResource();
    SwaggerConfiguration oasConfig = new SwaggerConfiguration().openAPI(oas).prettyPrint(true)
      .resourcePackages(Stream.of("org.roda.wui.api.v1").collect(Collectors.toSet()));
    openApiResource.setOpenApiConfiguration(oasConfig);
    register(openApiResource);
    register(JacksonFeature.class);
    register(MoxyXmlFeature.class);
    register(MultiPartFeature.class);
    register(ApiOriginFilter.class);
    register(SwaggerSerializers.class);
    packages("org.roda.wui.api");
    packages("io.swagger.v3.jaxrs2.integration.resources");
  }
}
