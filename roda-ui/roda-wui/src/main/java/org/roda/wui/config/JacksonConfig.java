package org.roda.wui.config;

import org.roda.core.data.common.SecureString;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

@Configuration
public class JacksonConfig {

  @Bean
  @Primary
  public JsonMapper jsonMapper() {
    SimpleModule secureStringModule = new SimpleModule();
    // Custom (de)serializers for SecureString to support password fields in REST request DTOs.
    // Without these, Jackson cannot deserialize JSON strings to SecureString, breaking every
    // password-related REST v2 endpoint (createUser, registerUser, resetPassword, updateUser).
    secureStringModule.addDeserializer(SecureString.class, new SecureStringDeserializer());
    secureStringModule.addSerializer(SecureString.class, new SecureStringSerializer());

    return JsonMapper.builder()
      // Prevents 400 errors when RestyGWT sends extra or unmapped type fields
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      // Jackson 3's FAIL_ON_NULL_FOR_PRIMITIVES defaults to enabled (Jackson 2 had it
      // disabled) - without this, any request DTO with a primitive @JsonCreator
      // parameter (e.g. FindRequest/CountRequest's onlyActive) rejects every request
      // that omits that field, breaking every generic /find and /count endpoint.
      .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
      .addModule(secureStringModule).build();
  }
}
