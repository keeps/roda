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
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Custom Jackson deserializer for SecureString. Converts incoming JSON string values
 * into SecureString instances by converting the string to a char array.
 */
public class SecureStringDeserializer extends StdDeserializer<SecureString> {

  private static final long serialVersionUID = 1L;

  public SecureStringDeserializer() {
    super(SecureString.class);
  }

  @Override
  public SecureString deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
    String value = p.getValueAsString();
    if (value == null) {
      return new SecureString();
    }
    return new SecureString(value.toCharArray());
  }
}
