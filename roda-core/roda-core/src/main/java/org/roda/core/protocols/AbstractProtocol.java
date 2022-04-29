/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.protocols;

import java.net.URI;

import org.roda.core.data.v2.IsRODAObject;

public abstract class AbstractProtocol<T extends IsRODAObject> implements Protocol {
  private URI connectionString = null;

  public void setConnectionString(URI connectionString) {
    this.connectionString = connectionString;
  }

  public URI getConnectionString() {
    return connectionString;
  }
}
