/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Optional;

public final class SerializableOptional<T extends Serializable> implements Serializable {
  private static final long serialVersionUID = -2359014025502550851L;
  private final transient Optional<T> optional;

  private SerializableOptional(Optional<T> optional) {
    this.optional = optional;
  }

  public static <T extends Serializable> SerializableOptional<T> setOptional(Optional<T> optional) {
    return new SerializableOptional<>(optional);
  }

  public static <T extends Serializable> SerializableOptional<T> empty() {
    return new SerializableOptional<>(Optional.empty());
  }

  public static <T extends Serializable> SerializableOptional<T> of(T value) {
    return new SerializableOptional<>(Optional.ofNullable(value));
  }

  public Optional<T> getOptional() {
    return optional;
  }

  private Object writeReplace() {
    return new SerializationProxy<>(this);
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    throw new InvalidObjectException("A serialization proxy will do the work.");
  }
}
