/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.config;

import org.roda.core.data.common.SecureString;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Custom Jackson serializer for SecureString. None of its use sites (password fields on
 * request DTOs) are meant to be serialized back out, so this writes a fixed placeholder
 * rather than the real value - only needed so the type has a symmetric module registration
 * and doesn't fail if something incidentally serializes a request DTO (e.g. error handlers
 * echoing a request body, audit logging).
 */
public class SecureStringSerializer extends StdSerializer<SecureString> {

  private static final long serialVersionUID = 1L;

  public SecureStringSerializer() {
    super(SecureString.class);
  }

  @Override
  public void serialize(SecureString value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
    if (value == null) {
      gen.writeNull();
    } else {
      gen.writeString("***");
    }
  }
}
