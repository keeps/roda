/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * System command utility.
 * 
 * @author Rui Castro
 * @author Luis Faria
 */
public class CommandUtility {
  private static final Logger LOGGER = LoggerFactory.getLogger(CommandUtility.class);

  /**
   * Execute the given command line.
   * 
   * @param args
   *          the command line as a list of arguments.
   * 
   * @return a {@link String} with the output of the command.
   * 
   * @throws CommandException
   */
  public static String execute(String... args) throws CommandException {
    return execute(true, args);
  }

  /**
   * Execute the given command line.
   * 
   * @param args
   *          the command line as a list of arguments.
   * 
   * @return a {@link String} with the output of the command.
   * 
   * @throws CommandException
   */
  public static String execute(boolean withErrorStream, String... args) throws CommandException {

    int exitValue = 0;
    String output;

    try {

      StringBuilder builder = new StringBuilder();
      for (String arg : args) {
        builder.append(arg + " ");
      }
      LOGGER.debug("Executing {}", builder);

      // create and execute process
      ProcessBuilder processBuilder = new ProcessBuilder(args);
      processBuilder.redirectErrorStream(withErrorStream);
      Process process = processBuilder.start();

      // Get process output
      InputStream is = process.getInputStream();

      // output = StreamUtility.inputStreamToString(is);

      CaptureOutputThread captureOutputThread = new CaptureOutputThread(is);

      synchronized (is) {

        captureOutputThread.start();

        // Wait until the CaptureOutputThread notifies that is finished
        // reading the input stream.
        LOGGER.debug("Waiting until CaptureOutputThread notifies");
        is.wait();

      }

      LOGGER.debug("CaptureOutputThread notified. Getting output...");

      output = captureOutputThread.output;

      // Get process exit value
      exitValue = process.waitFor();

      IOUtils.closeQuietly(is);

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Command {} terminated with value {}", Arrays.toString(args), exitValue);
      }

      if (exitValue == 0) {
        return output;
      } else {
        throw new CommandException("Command " + Arrays.toString(args) + " terminated with error code " + exitValue,
          exitValue, output);
      }

    } catch (IOException | InterruptedException e) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Error executing command {}", Arrays.toString(args), e);
      }
      throw new CommandException("Error executing command " + Arrays.toString(args) + " - " + e.getMessage(), e);
    }
  }

  /**
   * Execute the given command line.
   * 
   * @param args
   *          the command line as a list of arguments.
   * 
   * @return a {@link String} with the output of the command.
   * 
   * @throws CommandException
   */
  public static String execute(List<String> args) throws CommandException {
    return execute(args, true);
  }

  public static String execute(List<String> args, boolean withErrorStream) throws CommandException {
    return execute(withErrorStream, args.toArray(new String[args.size()]));
  }

}

class CaptureOutputThread extends Thread {
  private static final Logger logger = LoggerFactory.getLogger(CaptureOutputThread.class);

  InputStream is;
  String output;

  public CaptureOutputThread(InputStream is) {
    this.is = is;
  }

  public void run() {

    StringBuilder outputBuffer = new StringBuilder();

    try {

      // output = StreamUtility.inputStreamToString(is);
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));

      String line = null;

      while ((line = reader.readLine()) != null) {
        outputBuffer.append(line + System.lineSeparator());
        logger.trace(line);
      }

    } catch (IOException e) {
      logger.error("Exception reading from inputstream", e);
    }

    output = outputBuffer.toString();

    synchronized (is) {
      is.notify();
    }
  }
}
