/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.antivirus;

import org.roda.core.plugins.PluginException;

/**
 * Exception thrown if something went wrong in a particular implementation of
 * {@link AntiVirus} (as defined by the interface)
 */
public class AntiVirusException extends PluginException {
  private static final long serialVersionUID = 3567732420458265208L;

  public AntiVirusException(String message, Throwable cause) {
    super(message, cause);
  }
}
