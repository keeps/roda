package org.roda.action.antivirus.utils;

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

}
