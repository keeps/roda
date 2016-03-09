/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.antivirus;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the Clam anti-virus.
 * 
 * @author Rui Castro
 */
public class ClamAntiVirus implements AntiVirus {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClamAntiVirus.class);

  /**
   * No virus found.
   */
  private static final int NO_VIRUS_FOUND = 0;

  /**
   * Virus(es) found.
   */
  private static final int VIRUS_FOUND = 1;

  /**
   * Unknown option passed.
   */
  private static final int UNKNOWN_OPTION_PASSED = 40;

  /**
   * Database initialization error.
   */
  private static final int DATABASE_INITIALIZATION_ERROR = 50;

  /**
   * Not supported file type.
   */
  private static final int NOT_SUPPORTED_FILE_TYPE = 52;

  /**
   * Can’t open directory.
   */
  private static final int CANT_OPEN_DIRECTORY = 53;

  /**
   * Can’t open file. (ofm)
   */
  private static final int CANT_OPEN_FILE = 54;

  /**
   * Error reading file. (ofm)
   */
  private static final int ERROR_READING_FILE = 55;

  /**
   * Can’t stat input file / directory.
   */
  private static final int CANT_STAT_INPUT_FILE_OR_DIRECTORY = 56;

  /**
   * Can’t get absolute path name of current working directory.
   */
  private static final int CANT_GET_ABSOLUTE_PATH_NAME_OF_CURRENT_WORKING_DIRECTORY = 57;

  /**
   * I/O error, please check your file system.
   */
  private static final int IO_ERROR_CHECK_FILESYSTEM = 58;
  /**
   * Can’t get information about current user from /etc/passwd.
   */
  private static final int CANT_GET_INFO_ABOUT_CURRENT_USER = 59;
  /**
   * Can’t get information about user ’’ from /etc/passwd.
   */
  private static final int CANT_GET_INFO_ABOUT_USER = 60;
  /**
   * Can’t fork.
   */
  private static final int CANT_FORK = 61;
  /**
   * Can’t initialize logger.
   */
  private static final int CANT_INITIALIZE_LOGGER = 62;
  /**
   * Can’t create temporary files/directories (check permissions).
   */
  private static final int CANT_CREATE_TEMP_FILE_OR_DIR = 63;
  /**
   * Can’t write to temporary directory (please specify another one).
   */
  private static final int CANT_WRITE_TO_TEMP_DIR = 64;
  /**
   * Can’t allocate memory (calloc).
   */
  private static final int CANT_ALLOCATE_MEMORY_CALLOC = 70;
  /**
   * Can’t allocate memory (malloc).
   */
  private static final int CANT_ALLOCATE_MEMORY_MALLOC = 71;

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
   * @see AntiVirus
   */
  public VirusCheckResult checkForVirus(Path path) throws RuntimeException {

    VirusCheckResult result = new VirusCheckResult();

    try {

      LOGGER.debug("Executing virus scan in " + path.toString());

      // clamscan -r -i bin/ 2> /dev/null
      String clamavBin = RodaCoreFactory.getRodaConfiguration()
        .getString("core.plugins.internal.virus_check.clamav.bin", "clamscan");
      String clamavParams = RodaCoreFactory.getRodaConfiguration()
        .getString("core.plugins.internal.virus_check.clamav.params", "-ri");
      ProcessBuilder processBuilder = new ProcessBuilder(clamavBin, clamavParams, path.toString());

      // processBuilder.redirectErrorStream();
      Process process = processBuilder.start();

      StringWriter outputWriter = new StringWriter();

      IOUtils.copy(process.getInputStream(), outputWriter);

      // Set the result report
      result.setReport(outputWriter.toString());

      int exitValue = process.waitFor();

      switch (exitValue) {

        case NO_VIRUS_FOUND:
          result.setClean(true);
          break;

        default:
          result.setClean(false);
          break;
      }

    } catch (IOException e) {
      LOGGER.debug("Error executing virus scan command - " + e.getMessage(), e);
      throw new RuntimeException("Error executing virus scan command - " + e.getMessage(), e);
    } catch (InterruptedException e) {
      LOGGER.debug("Error executing virus scan command - " + e.getMessage(), e);
      throw new RuntimeException("Error executing virus scan command - " + e.getMessage(), e);
    }

    return result;
  }

}
