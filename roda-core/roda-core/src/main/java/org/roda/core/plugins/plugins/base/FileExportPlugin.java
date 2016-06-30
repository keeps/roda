/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileExportPlugin extends AbstractPlugin<File> {

  public Charset charset = StandardCharsets.UTF_8;

  public static final String EXPORT_CSV_TEMP_FOLDER = "CSV";
  public static final String CSV_FILE_FIELDS = "parameter.csv.file.fields";
  public static final String CSV_FILE_OUTPUT = "parameter.csv.file.output";
  public static final String CSV_FILE_HEADERS = "parameter.csv.file.headers";

  private static final Logger LOGGER = LoggerFactory.getLogger(FileExportPlugin.class);

  private List<String> fields = null;
  private Path output;
  private boolean enableHeaders;
  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  // TODO -> add plugin parameter type "LIST"...
  static {
    pluginParameters.put(CSV_FILE_FIELDS, new PluginParameter(CSV_FILE_FIELDS, "CSV Fields", PluginParameterType.STRING,
      "file id", true, false, "List of fields to export"));
    pluginParameters.put(CSV_FILE_OUTPUT, new PluginParameter(CSV_FILE_OUTPUT, "CSV output path",
      PluginParameterType.STRING, "/home/hsilva/Desktop/.csv", true, false, "Path where the CSV file is created."));
    pluginParameters.put(CSV_FILE_HEADERS, new PluginParameter(CSV_FILE_HEADERS, "CSV headers",
      PluginParameterType.BOOLEAN, "true", true, false, "Output CSV headers."));
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Export file metadata to CSV";
  }

  @Override
  public String getDescription() {
    return "Export a list of files metadata to a CSV file";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<PluginParameter>();
    parameters.add(pluginParameters.get(CSV_FILE_FIELDS));
    parameters.add(pluginParameters.get(CSV_FILE_OUTPUT));
    parameters.add(pluginParameters.get(CSV_FILE_HEADERS));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters.containsKey(CSV_FILE_FIELDS)) {
      String fieldsSTR = parameters.get(CSV_FILE_FIELDS);
      if (fieldsSTR != null && !fieldsSTR.trim().equalsIgnoreCase("")) {
        fields = new ArrayList<String>();
        fields.addAll(Arrays.asList(fieldsSTR.split(",")));
      }
    }
    if (parameters.containsKey(CSV_FILE_OUTPUT)) {
      output = Paths.get(parameters.get(CSV_FILE_OUTPUT));
    }
    if (parameters.containsKey(CSV_FILE_HEADERS)) {
      enableHeaders = Boolean.parseBoolean(parameters.get(CSV_FILE_HEADERS));
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<File> list)
    throws PluginException {
    String firstFileId = list.get(0).getId();
    LOGGER.debug("(1st: {}) Exporting to CSV a total of {} files", firstFileId, list.size());
    BufferedWriter fileWriter = null;
    CSVPrinter csvFilePrinter = null;
    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);
      Path jobCSVTempFolder = getJobCSVTempFolder();
      Path csvTempFile = jobCSVTempFolder.resolve(UUID.randomUUID().toString() + ".csv");

      CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
      fileWriter = Files.newBufferedWriter(csvTempFile);
      csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
      LOGGER.debug("(1st: {}) Going to iterate through {} files", firstFileId, list.size());
      for (File file : list) {
        LOGGER.debug("(1st: {}) Processing file {}", firstFileId, file);
        List<String> fileMetadata = FileExportPluginUtils.retrieveFileInfo(fields, file, model);
        csvFilePrinter.printRecord(fileMetadata);
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
      }
      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
      LOGGER.debug("(1st: {}) Done exporting to CSV a total of {} files", firstFileId, list.size());
    } catch (JobException e) {
      LOGGER.error("Could not update Job information");
    } catch (IOException e) {
      LOGGER.error("Error executing FileExportPlugin: " + e.getMessage(), e);
    } finally {
      IOUtils.closeQuietly(fileWriter);
      IOUtils.closeQuietly(csvFilePrinter);
    }

    return PluginHelper.initPluginReport(this);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    try {
      Path jobCSVTempFolder = getJobCSVTempFolder();
      Files.createDirectories(jobCSVTempFolder);
    } catch (IOException e) {
      LOGGER.error("Error while creating plugin working dir", e);
    }

    return new Report();
  }

  private Path getJobCSVTempFolder() {
    Path wd = RodaCoreFactory.getWorkingDirectory();
    Path csvExportTempFolder = wd.resolve(FileExportPlugin.EXPORT_CSV_TEMP_FOLDER);
    Path jobCSVTempFolder = csvExportTempFolder.resolve(PluginHelper.getJobId(this));
    return jobCSVTempFolder;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
    Path csvTempFolder = getJobCSVTempFolder();

    if (csvTempFolder != null) {
      List<Path> partials = new ArrayList<Path>();
      try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(csvTempFolder);
        FileWriter fileWriter = new FileWriter(output.toFile());
        CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);) {
        if (enableHeaders) {
          csvFilePrinter.printRecord(fields);
        }
        for (Path path : directoryStream) {
          partials.add(path);
        }
      } catch (IOException ex) {
        LOGGER.error("Error while merging partial CSVs: " + ex.getMessage(), ex);
      }
      try {
        FileExportPluginUtils.mergeFiles(partials, output);
        FSUtils.deletePathQuietly(csvTempFolder);
      } catch (IOException e) {
        LOGGER.error("Error while merging partial CSVs: " + e.getMessage(), e);
      }
    }
    return new Report();
  }

  @Override
  public Plugin<File> cloneMe() {
    return new FileExportPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  // TODO FIX
  @Override
  public PreservationEventType getPreservationEventType() {
    return null;
  }

  @Override
  public String getPreservationEventDescription() {
    return "XXXXXXXXXX";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public List<String> getCategories() {
    return new ArrayList<String>();
  }

  @Override
  public Report beforeBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Report afterBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // TODO Auto-generated method stub
    return null;
  }
}
