package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.roda.core.util.CommandException;
import org.roda.core.util.CommandUtility;

public class GeneralCommandConvertPluginUtils {

  public static Path runGeneralCommandConvert(Path input, String inputFormat, String outputFormat,
    String commandArguments) throws IOException, CommandException {

    Path output = Files.createTempFile("result", "." + outputFormat);
    return executeGeneralCommand(input, output, commandArguments);
  }

  private static Path executeGeneralCommand(Path input, Path output, String command) throws CommandException,
    IOException, UnsupportedOperationException {

    command = command.replace("{input_file}", input.toString());
    command = command.replace("{output_file}", output.toString());

    // filling a list of the command line arguments
    List<String> commandList = Arrays.asList(command.split("\\s+"));

    // running the command
    CommandUtility.execute(commandList);
    return output;
  }

}
