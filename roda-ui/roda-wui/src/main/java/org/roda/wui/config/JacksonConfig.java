package org.roda.wui.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

@Configuration
public class JacksonConfig {

  @Bean
  @Primary
  public JsonMapper jsonMapper() {
    return JsonMapper.builder()
      // Prevents 400 errors when RestyGWT sends extra or unmapped type fields
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();
  }
}
