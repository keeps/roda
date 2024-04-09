package org.roda.wui.api.v2.config;

import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.servlets.ContextListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletContext;

import java.util.ArrayList;

@Configuration
public class OpenAPIConfig {

  @Bean
  public OpenAPI generateOpenAPI() {
    OpenAPI oas = new OpenAPI();
    Info info = new Info().title("RODA REST API").description("REST AIP for RODA")
      .contact(new Contact().email("info@keep.pt").name("KEEP SOLUTIONS")
        .url("https://www.keep.pt/en/contacts-proposals-information-telephone-address"))
      .license(new License().name("LGPLv3").url("http://www.gnu.org/licenses/lgpl-3.0.html")).version("2.0.0");

    oas.info(info);

    ServletContext servletContext = ContextListener.getServletContext();
    if (StringUtils.isNotBlank(servletContext.getContextPath())) {
      oas.addServersItem(new Server().url(servletContext.getContextPath()));
    }

    Components components = new Components();
    SecurityScheme basicAuthScheme = new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic");
    SecurityScheme bearerScheme = new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer");

    components.addSecuritySchemes("basicAuth", basicAuthScheme);
    components.addSecuritySchemes("bearerAuth", bearerScheme);

    oas.addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    oas.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));

    oas.components(components);

    oas.servers(new ArrayList<>());

    return oas;
  }
}
