/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.common;

import java.util.Optional;

import org.roda.core.data.exceptions.RODAException;

public class OptionalWithCause<T> {

  private final Optional<T> optional;
  private final RODAException cause;

  private OptionalWithCause() {
    super();
    this.optional = Optional.empty();
    this.cause = null;
  }

  private OptionalWithCause(Optional<T> optional, RODAException cause) {
    super();
    this.optional = optional;
    this.cause = cause;
  }

  public RODAException getCause() {
    return cause;
  }

  public Optional<T> getOptional() {
    return optional;
  }

  public T get() {
    return optional.get();
  }

  public boolean isPresent() {
    return optional.isPresent();
  }

  public static <T> OptionalWithCause<T> empty(RODAException cause) {
    return new OptionalWithCause<>(Optional.empty(), cause);
  }

  public static <T> OptionalWithCause<T> of(T value) {
    return new OptionalWithCause<>(Optional.of(value), null);
  }

  public static <T> OptionalWithCause<T> of(Optional<T> value) {
    return new OptionalWithCause<>(value, null);
  }

}
