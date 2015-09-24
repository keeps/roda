package org.roda.action.orchestrate.actions.antivirus;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * This is the Clam anti-virus.
 * 
 * @author Rui Castro
 */
public class ClamAntiVirus implements AntiVirus {

  static final private Logger logger = Logger.getLogger(ClamAntiVirus.class);

  /**
   * No virus found.
   */
  static final private int NO_VIRUS_FOUND = 0;

  /**
   * Virus(es) found.
   */
  static final private int VIRUS_FOUND = 1;

  /**
   * Unknown option passed.
   */
  static final private int UNKNOWN_OPTION_PASSED = 40;

  /**
   * Database initialization error.
   */
  static final private int DATABASE_INITIALIZATION_ERROR = 50;

  /**
   * Not supported file type.
   */
  static final private int NOT_SUPPORTED_FILE_TYPE = 52;

  /**
   * Can’t open directory.
   */
  static final private int CANT_OPEN_DIRECTORY = 53;

  /**
   * Can’t open file. (ofm)
   */
  static final private int CANT_OPEN_FILE = 54;

  /**
   * Error reading file. (ofm)
   */
  static final private int ERROR_READING_FILE = 55;

  /**
   * Can’t stat input file / directory.
   */
  static final private int CANT_STAT_INPUT_FILE_OR_DIRECTORY = 56;

  /**
   * Can’t get absolute path name of current working directory.
   */
  static final private int CANT_GET_ABSOLUTE_PATH_NAME_OF_CURRENT_WORKING_DIRECTORY = 57;

  /**
   * I/O error, please check your file system.
   */
  static final private int IO_ERROR_CHECK_FILESYSTEM = 58;
  /**
   * Can’t get information about current user from /etc/passwd.
   */
  static final private int CANT_GET_INFO_ABOUT_CURRENT_USER = 59;
  /**
   * Can’t get information about user ’’ from /etc/passwd.
   */
  static final private int CANT_GET_INFO_ABOUT_USER = 60;
  /**
   * Can’t fork.
   */
  static final private int CANT_FORK = 61;
  /**
   * Can’t initialize logger.
   */
  static final private int CANT_INITIALIZE_LOGGER = 62;
  /**
   * Can’t create temporary files/directories (check permissions).
   */
  static final private int CANT_CREATE_TEMP_FILE_OR_DIR = 63;
  /**
   * Can’t write to temporary directory (please specify another one).
   */
  static final private int CANT_WRITE_TO_TEMP_DIR = 64;
  /**
   * Can’t allocate memory (calloc).
   */
  static final private int CANT_ALLOCATE_MEMORY_CALLOC = 70;
  /**
   * Can’t allocate memory (malloc).
   */
  static final private int CANT_ALLOCATE_MEMORY_MALLOC = 71;

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

      logger.debug("Executing virus scan in " + path.toString());

      // clamscan -r -i bin/ 2> /dev/null
      ProcessBuilder processBuilder = new ProcessBuilder("/usr/bin/clamscan", "-ri", path.toString());

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

      logger.debug("Virus checker exit value: " + exitValue);
      logger.debug("Virus checker output:\n" + outputWriter.toString());

    } catch (IOException e) {
      logger.debug("Error executing virus scan command - " + e.getMessage(), e);
      throw new RuntimeException("Error executing virus scan command - " + e.getMessage(), e);
    } catch (InterruptedException e) {
      logger.debug("Error executing virus scan command - " + e.getMessage(), e);
      throw new RuntimeException("Error executing virus scan command - " + e.getMessage(), e);
    }

    return result;
  }

}
