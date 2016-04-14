/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.antivirus;

import java.nio.file.Path;

/**
 * This is an interface for any anti-virus software.
 * 
 * @author Rui Castro
 */
public interface AntiVirus {

  /**
   * Performs a virus check on the specified path.
   * 
   * @param path
   *          a path to scan.
   * 
   * @return the results of the virus check as a {@link VirusCheckResult}.
   * 
   * @throws RuntimeException
   *           if some problem prevented the virus check from run a normal test.
   */
  public VirusCheckResult checkForVirus(Path path) throws RuntimeException;

  public String getVersion();

}
