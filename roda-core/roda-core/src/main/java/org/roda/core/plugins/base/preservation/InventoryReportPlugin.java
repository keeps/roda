/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.preservation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.orchestrate.JobsHelper;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryReportPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(InventoryReportPlugin.class);

  public static final String EXPORT_CSV_TEMP_FOLDER = "CSV";
  public static final String CSV_FILE_FIELDS = "parameter.csv.file.fields";
  public static final String CSV_FILE_OUTPUT = "parameter.csv.file.output";
  public static final String CSV_FILE_HEADERS = "parameter.csv.file.headers";
  public static final String CSV_FILE_OUTPUT_DATA = "parameter.csv.file.output.data";
  public static final String CSV_FILE_OUTPUT_DESCRIPTIVE = "parameter.csv.file.output.descriptive";
  public static final String CSV_FILE_OTHER_METADATA_TYPES = "parameter.csv.file.output.other";

  public static final String CSV_FIELD_SIP_ID = "sipId";
  public static final String CSV_FIELD_AIP_ID = "aipId";
  public static final String CSV_FIELD_REPRESENTATION_ID = "representationId";
  public static final String CSV_FIELD_FILE_PATH = "filePath";
  public static final String CSV_FIELD_FILE_ID = "fileId";
  public static final String CSV_FIELD_ISDIRECTORY = "isDirectory";
  public static final String CSV_FIELD_PARENT_ID = "parentId";

  public static final String CSV_FIELD_CHECKSUM_SHA1 = "SHA-1";
  public static final String CSV_FIELD_CHECKSUM_SHA256 = "SHA-256";
  public static final String CSV_FIELD_CHECKSUM_MD5 = "MD5";

  public static final String CSV_FILE_TYPE = "type";

  public enum CSV_LINE_TYPE {
    DATA, METADATA_DESCRIPTIVE, METADATA_OTHER
  }

  protected static List<String> CHECKSUM_ALGORITHMS;

  public static final String CSV_DEFAULT_FIELDS = StringUtils.join(Arrays.asList(CSV_FIELD_SIP_ID, CSV_FIELD_AIP_ID,
    CSV_FIELD_REPRESENTATION_ID, CSV_FIELD_FILE_PATH, CSV_FIELD_FILE_ID, CSV_FIELD_PARENT_ID, CSV_FIELD_ISDIRECTORY,
    CSV_FILE_TYPE, CSV_FIELD_CHECKSUM_SHA256, CSV_FIELD_CHECKSUM_MD5, CSV_FIELD_CHECKSUM_SHA1), ",");
  public static final String CSV_DEFAULT_OUTPUT = "/tmp/output.csv";
  public static final String CSV_DEFAULT_HEADERS = "true";
  public static final String CSV_DEFAULT_OTHER_METADATA = "ApacheTika,Siegfried";

  private List<String> fields = null;
  private Path output;
  private boolean enableHeaders;
  private boolean outputDataInformation;
  private boolean outputDescriptiveMetadataInformation;
  private List<String> otherMetadataTypes;
  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(CSV_FILE_FIELDS,
      PluginParameter
        .getBuilder(CSV_FILE_FIELDS, "plugin.inventoryReportPlugin.parameter.fields.name", PluginParameterType.STRING)
        .withDefaultValue(CSV_DEFAULT_FIELDS)
        .withDescription("plugin.inventoryReportPlugin.parameter.fields.description").build());

    pluginParameters.put(CSV_FILE_OUTPUT,
      PluginParameter
        .getBuilder(CSV_FILE_OUTPUT, "plugin.inventoryReportPlugin.parameter.output.name", PluginParameterType.STRING)
        .withDefaultValue(CSV_DEFAULT_OUTPUT)
        .withDescription("plugin.inventoryReportPlugin.parameter.output.description").build());

    pluginParameters.put(CSV_FILE_HEADERS, PluginParameter
      .getBuilder(CSV_FILE_HEADERS, "plugin.inventoryReportPlugin.parameter.headers.name", PluginParameterType.BOOLEAN)
      .withDefaultValue(CSV_DEFAULT_HEADERS)
      .withDescription("plugin.inventoryReportPlugin.parameter.headers.description").build());

    pluginParameters.put(CSV_FILE_OUTPUT_DATA,
      PluginParameter
        .getBuilder(CSV_FILE_OUTPUT_DATA, "plugin.inventoryReportPlugin.parameter.outputData.name",
          PluginParameterType.BOOLEAN)
        .withDefaultValue(CSV_DEFAULT_HEADERS)
        .withDescription("plugin.inventoryReportPlugin.parameter.outputData.description").build());

    pluginParameters.put(CSV_FILE_OUTPUT_DESCRIPTIVE,
      PluginParameter
        .getBuilder(CSV_FILE_OUTPUT_DESCRIPTIVE, "plugin.inventoryReportPlugin.parameter.outputDescriptive.name",
          PluginParameterType.BOOLEAN)
        .withDefaultValue(CSV_DEFAULT_HEADERS)
        .withDescription("plugin.inventoryReportPlugin.parameter.outputDescriptive.description").build());

    pluginParameters.put(CSV_FILE_OTHER_METADATA_TYPES,
      PluginParameter
        .getBuilder(CSV_FILE_OTHER_METADATA_TYPES, "plugin.inventoryReportPlugin.parameter.otherMetadataTypes.name",
          PluginParameterType.STRING)
        .withDefaultValue(CSV_DEFAULT_OTHER_METADATA)
        .withDescription("plugin.inventoryReportPlugin.parameter.otherMetadataTypes.description").build());
  }

  @Override
  public void init() throws PluginException {
    List<String> configuredAlgorithms = RodaCoreFactory
      .getRodaConfigurationAsList("core.inventory.report.checksum.algorithms[]");

    if (configuredAlgorithms != null && !configuredAlgorithms.isEmpty()) {
      CHECKSUM_ALGORITHMS = configuredAlgorithms;
    } else {
      CHECKSUM_ALGORITHMS = Arrays.asList(CSV_FIELD_CHECKSUM_MD5, CSV_FIELD_CHECKSUM_SHA1, CSV_FIELD_CHECKSUM_SHA256);
    }
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "plugin.inventoryReportPlugin.name";
  }

  @Override
  public String getDescription() {
    return "plugin.inventoryReportPlugin.description";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    PluginParameter fieldsParam = pluginParameters.get(CSV_FILE_FIELDS);
    fieldsParam.setDefaultValue(getCsvFieldsList());
    parameters.add(fieldsParam);
    PluginParameter outputPluginParameter = pluginParameters.get(CSV_FILE_OUTPUT);
    SimpleDateFormat df = new SimpleDateFormat(RodaConstants.DEFAULT_DATETIME_FORMAT);
    String reportName = "inventory_report_" + df.format(new Date()) + ".csv";
    outputPluginParameter.setDefaultValue(RodaCoreFactory.getReportsDirectory().resolve(reportName).toString());
    parameters.add(outputPluginParameter);
    parameters.add(pluginParameters.get(CSV_FILE_HEADERS));
    parameters.add(pluginParameters.get(CSV_FILE_OUTPUT_DATA));
    parameters.add(pluginParameters.get(CSV_FILE_OUTPUT_DESCRIPTIVE));
    parameters.add(pluginParameters.get(CSV_FILE_OTHER_METADATA_TYPES));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters.containsKey(CSV_FILE_FIELDS)) {
      String fieldsSTR = parameters.get(CSV_FILE_FIELDS);
      if (fieldsSTR != null && !"".equals(fieldsSTR.trim())) {
        fields = new ArrayList<>();
        fields.addAll(Arrays.asList(fieldsSTR.split(",")));
      }
    }
    if (parameters.containsKey(CSV_FILE_OUTPUT)) {
      try {
        output = Paths.get(FilenameUtils.normalize(parameters.get(CSV_FILE_OUTPUT)));
        Path parent = output.getParent();
        Files.createDirectories(parent);
      } catch (IOException e) {
        LOGGER.error("Error creating output parent folder.", e);
      }
    }
    if (parameters.containsKey(CSV_FILE_HEADERS)) {
      enableHeaders = Boolean.parseBoolean(parameters.get(CSV_FILE_HEADERS));
    }
    if (parameters.containsKey(CSV_FILE_OUTPUT_DATA)) {
      outputDataInformation = Boolean.parseBoolean(parameters.get(CSV_FILE_OUTPUT_DATA));
    }
    if (parameters.containsKey(CSV_FILE_OUTPUT_DESCRIPTIVE)) {
      outputDescriptiveMetadataInformation = Boolean.parseBoolean(parameters.get(CSV_FILE_OUTPUT_DESCRIPTIVE));
    }
    if (parameters.containsKey(CSV_FILE_OTHER_METADATA_TYPES)) {
      String otherMetadataSTR = parameters.get(CSV_FILE_OTHER_METADATA_TYPES);
      if (otherMetadataSTR != null && !"".trim().equalsIgnoreCase(otherMetadataSTR)) {
        otherMetadataTypes = new ArrayList<>();
        otherMetadataTypes.addAll(Arrays.asList(otherMetadataSTR.split(",")));
      }
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model,
    List<LiteOptionalWithCause> liteList) throws PluginException {

    Path jobCSVTempFolder = getJobCSVTempFolder();
    Path csvTempFile = jobCSVTempFolder.resolve(IdUtils.createUUID() + ".csv");

    CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
    try (BufferedWriter fileWriter = Files.newBufferedWriter(csvTempFile);
      CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat)) {
      return PluginHelper.processObjects(this, new RODAObjectProcessingLogic<AIP>() {
        @Override

        public void process(IndexService index, ModelService model, Report report,
          Job cachedJob, JobPluginInfo jobPluginInfo, Plugin<AIP> plugin, AIP object) {
          processAIP(model, jobPluginInfo, csvFilePrinter, object);
        }
      }, index, model, liteList);
    } catch (IOException e) {
      throw new PluginException("Unable to create/write to CSVPrinter", e);
    }
  }

  private void processAIP(ModelService model, JobPluginInfo jobPluginInfo,
    CSVPrinter csvFilePrinter, AIP aip) {
    if (csvFilePrinter == null) {
      LOGGER.warn("CSVPrinter is NULL! Skipping...");
      return;
    }

    try {
      if (outputDataInformation && aip.getRepresentations() != null) {
        List<List<String>> dataInformation = InventoryReportPluginUtils.getDataInformation(fields, aip, model);
        csvFilePrinter.printRecords(dataInformation);
      }
      if (outputDescriptiveMetadataInformation && aip.getDescriptiveMetadata() != null) {
        List<List<String>> dataInformation = InventoryReportPluginUtils.getDescriptiveMetadataInformation(fields, aip,
          model);
        csvFilePrinter.printRecords(dataInformation);
      }
      if (otherMetadataTypes != null && !otherMetadataTypes.isEmpty()) {
        for (String otherMetadataType : otherMetadataTypes) {
          List<List<String>> otherMetadataInformation = InventoryReportPluginUtils.getOtherMetadataInformation(fields,
            otherMetadataType, aip, model);
          csvFilePrinter.printRecords(otherMetadataInformation);
        }
      }
      jobPluginInfo.incrementObjectsProcessedWithSuccess();
    } catch (IOException e) {
      LOGGER.error("Error writing CSV file", e);
      jobPluginInfo.incrementObjectsProcessedWithFailure();
    }
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model)
    throws PluginException {
    try {
      Path jobCSVTempFolder = getJobCSVTempFolder();
      Files.createDirectories(jobCSVTempFolder);
    } catch (IOException e) {
      LOGGER.error("Error while creating plugin working dir", e);
    }
    try {
      Path reportsFolder = RodaCoreFactory.getRodaHomePath().resolve(RodaConstants.CORE_REPORT_FOLDER);
      if (FSUtils.exists(reportsFolder)) {
        Files.createDirectories(reportsFolder);
      }
    } catch (IOException e) {
      LOGGER.error("Error while creating report dir", e);
    }
    return new Report();
  }

  private Path getJobCSVTempFolder() {
    Path wd = RodaCoreFactory.getWorkingDirectory();
    Path csvExportTempFolder = wd.resolve(InventoryReportPlugin.EXPORT_CSV_TEMP_FOLDER);
    return csvExportTempFolder.resolve(PluginHelper.getJobId(this));
  }

  private String getCsvFieldsList() {
    List<String> defaultFieldsList = new ArrayList<>(
      Arrays.asList(CSV_FIELD_SIP_ID, CSV_FIELD_AIP_ID, CSV_FIELD_REPRESENTATION_ID, CSV_FIELD_FILE_PATH,
        CSV_FIELD_FILE_ID, CSV_FIELD_PARENT_ID, CSV_FIELD_ISDIRECTORY, CSV_FILE_TYPE));
    if (CHECKSUM_ALGORITHMS != null) {
      defaultFieldsList.addAll(CHECKSUM_ALGORITHMS);
    }
    return StringUtils.join(defaultFieldsList, ",");
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model) throws PluginException {
    CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
    Path csvTempFolder = getJobCSVTempFolder();

    if (csvTempFolder != null) {
      List<Path> partials = new ArrayList<>();
      try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(csvTempFolder);
        FileWriter fileWriter = new FileWriter(output.toFile());
        CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat)) {
        if (enableHeaders) {
          csvFilePrinter.printRecord(fields);
        }
        for (Path path : directoryStream) {
          partials.add(path);
        }
      } catch (IOException e) {
        LOGGER.error("Error while merging partial CSVs", e);
      }
      try {
        InventoryReportPluginUtils.mergeFiles(partials, output);
        FSUtils.deletePathQuietly(csvTempFolder);
        try {
          JobsHelper.createJobAttachment(PluginHelper.getJobId(this), output);
        } catch (AuthorizationDeniedException | GenericException | NotFoundException | RequestNotValidException e) {
          LOGGER.error("Error while creating attached files", e);
        }
      } catch (IOException e) {
        LOGGER.error("Error while merging partial CSVs", e);
      }
    }
    return new Report();
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new InventoryReportPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.VALIDATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Created a report in CSV format";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Created a report in CSV format successfully";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Create of a report in CSV format failed";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_MAINTENANCE);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }
}
