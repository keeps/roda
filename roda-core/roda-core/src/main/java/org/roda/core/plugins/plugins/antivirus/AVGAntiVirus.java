/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.antivirus;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the AVG anti-virus.
 * 
 * @author Rui Castro
 */
@SuppressWarnings("unused")
public class AVGAntiVirus implements AntiVirus {

  private static final Logger LOGGER = LoggerFactory.getLogger(AVGAntiVirus.class);

  /**
   * no errors
   */
  private static final int NO_ERRORS = 0;

  /**
   * the test was interrupted by the user
   */
  private static final int INTERRUPTED_BY_USER = 1;

  /**
   * an error occurred during the test (e.g. "cannot open file" event)
   */
  private static final int ERROR_OCCURED_DURING_TEST = 2;

  /**
   * file system changes detected
   */
  private static final int FS_CHANGES_DETECTED = 3;

  /**
   * a suspect object was found by heuristic analysis
   */
  private static final int SUSPECT_OBJECT_FOUND_BY_HEURISTIC_ANALYSIS = 4;

  /**
   * a virus was found by heuristic analysis
   */
  private static final int VIRUS_FOUND_BY_HEURISTIC_ANALYSIS = 5;

  /**
   * a particular virus was found
   */
  private static final int PARTICULAR_VIRUS_FOUND = 6;

  /**
   * an active virus was found in memory
   */
  private static final int ACTIVE_VIRUS_FOUND_IN_MEMORY = 7;

  /**
   * corruption of some of the AVG Free for Linux command line components
   */
  private static final int CORRUPTION_OF_SOME_AVG_COMPONENTS = 8;

  /**
   * an archive contains password protected files
   */
  private static final int ARCHIVE_CONTAINS_PASSWD_PROTECTED_FILES = 10;

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

      LOGGER.debug("Executing virus scan in {}", path);

      String avgBin = RodaCoreFactory.getRodaConfiguration().getString("core.plugins.internal.virus_check.avg.bin",
        "/usr/bin/avgscan");
      String avgParams = RodaCoreFactory.getRodaConfiguration()
        .getString("core.plugins.internal.virus_check.avg.params", "-repok -arc");

      List<String> command = new ArrayList<>();
      command.add(avgBin);
      for (String param : avgParams.split(" ")) {
        command.add(param);
      }
      command.add(path.toString());
      // Arrays.asList(clamavBin, clamavParams, path.toString());
      String commandOutput = CommandUtility.execute(command, true);
      result.setClean(true);
      result.setReport(commandOutput);
    } catch (CommandException e) {
      LOGGER.debug("Error executing virus scan command", e);
      result.setClean(false);
      result.setReport(e.getOutput());
    }

    return result;
  }

  @Override
  public String getVersion() {
    String avgGetVersion = RodaCoreFactory.getRodaConfiguration()
      .getString("core.plugins.internal.virus_check.avg.get_version", "/usr/bin/avgscan --version");
    List<String> command = new ArrayList<String>(Arrays.asList(avgGetVersion.split(" ")));
    try {
      return CommandUtility.execute(command);
    } catch (CommandException e) {
      return "1.0";
    }
  }

}
