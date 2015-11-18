/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.disseminators.common.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.log4j.lf5.util.StreamUtils;

/**
 * UNIX command executing tools
 * 
 * @author Luis Faria
 * 
 */
public class ExecTools {
  private static final Logger logger = LoggerFactory.getLogger(ExecTools.class);
  private static final int DEFAULT_NICENESS = -5;

  /**
   * Tries to execute the command, waits for it to finish, logs errors if exit
   * status is nonzero, and returns true if exit status is 0 (success).
   * 
   * @param command
   *          the command to execute
   * @return true if command ended successfully, false otherwise
   */
  public static boolean exec(String[] command) {
    return exec(command, DEFAULT_NICENESS);
  }

  /**
   * Tries to execute the command, waits for it to finish, logs errors if exit
   * status is nonzero, and returns true if exit status is 0 (success).
   * 
   * @param command
   *          the command to execute
   * @param niceness
   *          the scheduling priority. Nicenesses range from -20 (most favorable
   *          scheduling) to 19 (least favorable).
   * @return true if command ended successfully, false otherwise
   */
  public static boolean exec(String[] command, int niceness) {
    Process proc;

    try {
      logger.debug("executing: " + Arrays.asList(command));
      List<String> niceCommand = new Vector<String>(Arrays.asList(new String[] {"nice", "-n", niceness + ""}));
      niceCommand.addAll(Arrays.asList(command));
      proc = Runtime.getRuntime().exec(niceCommand.toArray(new String[] {}));
    } catch (IOException e) {
      logger.error("IOException while trying to execute " + command, e);
      return false;
    }

    int exitStatus;

    while (true) {
      try {
        exitStatus = proc.waitFor();
        break;
      } catch (InterruptedException e) {
        logger.warn("Interrupted: Ignoring and waiting", e);
      }
    }
    if (exitStatus != 0) {
      logger.error("Error executing command: " + exitStatus);
    }
    return (exitStatus == 0);
  }

  /**
   * Execute a command and read the default output stream as a string
   * 
   * @param command
   *          the command to execute
   * @return the default output stream
   * @throws IOException
   */
  public static String execAndRead(String[] command) throws IOException {
    Process child = Runtime.getRuntime().exec(command);

    // Get the input stream and read from it
    InputStream in = child.getInputStream();
    String ret = new String(StreamUtils.getBytes(in));
    in.close();
    return ret;
  }
}
