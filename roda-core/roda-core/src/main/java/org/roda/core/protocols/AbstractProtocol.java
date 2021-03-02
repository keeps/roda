/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.protocols;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants.PreservationAgentType;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.ip.SIPInformation;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.notifications.JobNotification;

public abstract class AbstractProtocol<T extends IsRODAObject> implements Protocol {
  private URI connectionString = null;

  public void setConnectionString(URI connectionString) {
    this.connectionString = connectionString;
  }

  public URI getConnectionString() {
    return connectionString;
  }
}
