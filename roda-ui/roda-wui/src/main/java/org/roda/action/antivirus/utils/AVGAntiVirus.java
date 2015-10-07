package org.roda.action.antivirus.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * This is the AVG anti-virus.
 * 
 * @author Rui Castro
 */
public class AVGAntiVirus implements AntiVirus {

  static final private Logger logger = Logger.getLogger(AVGAntiVirus.class);

  /**
   * no errors
   */
  static final private int NO_ERRORS = 0;

  /**
   * the test was interrupted by the user
   */
  static final private int INTERRUPTED_BY_USER = 1;

  /**
   * an error occurred during the test (e.g. "cannot open file" event)
   */
  static final private int ERROR_OCCURED_DURING_TEST = 2;

  /**
   * file system changes detected
   */
  static final private int FS_CHANGES_DETECTED = 3;

  /**
   * a suspect object was found by heuristic analysis
   */
  static final private int SUSPECT_OBJECT_FOUND_BY_HEURISTIC_ANALYSIS = 4;

  /**
   * a virus was found by heuristic analysis
   */
  static final private int VIRUS_FOUND_BY_HEURISTIC_ANALYSIS = 5;

  /**
   * a particular virus was found
   */
  static final private int PARTICULAR_VIRUS_FOUND = 6;

  /**
   * an active virus was found in memory
   */
  static final private int ACTIVE_VIRUS_FOUND_IN_MEMORY = 7;

  /**
   * corruption of some of the AVG Free for Linux command line components
   */
  static final private int CORRUPTION_OF_SOME_AVG_COMPONENTS = 8;

  /**
   * an archive contains password protected files
   */
  static final private int ARCHIVE_CONTAINS_PASSWD_PROTECTED_FILES = 10;

  /**
   * Performs a virus check on the specified path.
   * 
   * @param file
   *          a file or directory to scan.
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

      ProcessBuilder processBuilder = new ProcessBuilder("/usr/bin/avgscan", "-repok", "-arc", path.toString());
      processBuilder.redirectErrorStream();
      Process process = processBuilder.start();

      StringWriter outputWriter = new StringWriter();

      IOUtils.copy(process.getInputStream(), outputWriter);

      // Set the result report
      result.setReport(outputWriter.toString());

      int exitValue = process.waitFor();

      switch (exitValue) {

        case NO_ERRORS:
          result.setClean(true);
          break;

        case INTERRUPTED_BY_USER:
        case ERROR_OCCURED_DURING_TEST:
        case FS_CHANGES_DETECTED:
        case SUSPECT_OBJECT_FOUND_BY_HEURISTIC_ANALYSIS:
        case VIRUS_FOUND_BY_HEURISTIC_ANALYSIS:
        case PARTICULAR_VIRUS_FOUND:
        case ACTIVE_VIRUS_FOUND_IN_MEMORY:
        case CORRUPTION_OF_SOME_AVG_COMPONENTS:
        case ARCHIVE_CONTAINS_PASSWD_PROTECTED_FILES:
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
