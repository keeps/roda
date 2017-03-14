/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2;

import java.io.Serializable;

public class SerializationProxy<T extends Serializable> implements Serializable {
  private static final long serialVersionUID = -8250756629392696183L;
  private final T value;

  public SerializationProxy(SerializableOptional<T> serializableOptional) {
    value = serializableOptional.getOptional().orElse(null);
  }

  public Object readResolve() {
    return SerializableOptional.of(value);
  }
}
