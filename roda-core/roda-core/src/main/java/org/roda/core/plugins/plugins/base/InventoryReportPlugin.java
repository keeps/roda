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
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.InvalidParameterException;
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
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
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

  public static final String CSV_FIELD_CHECKSUM_SHA1 = "SHA-1";
  public static final String CSV_FIELD_CHECKSUM_SHA256 = "SHA-256";
  public static final String CSV_FIELD_CHECKSUM_MD5 = "MD5";

  public static final String CSV_FILE_TYPE = "type";

  public enum CSV_LINE_TYPE {
    DATA, METADATA_DESCRIPTIVE, METADATA_OTHER
  }

  public static final List<String> CHECKSUM_ALGORITHMS = Arrays.asList(CSV_FIELD_CHECKSUM_MD5, CSV_FIELD_CHECKSUM_SHA1,
    CSV_FIELD_CHECKSUM_SHA256);

  public static final String CSV_DEFAULT_FIELDS = StringUtils.join(Arrays.asList(CSV_FIELD_SIP_ID, CSV_FIELD_AIP_ID,
    CSV_FIELD_REPRESENTATION_ID, CSV_FIELD_FILE_PATH, CSV_FIELD_FILE_ID, CSV_FIELD_ISDIRECTORY, CSV_FILE_TYPE,
    CSV_FIELD_CHECKSUM_SHA256, CSV_FIELD_CHECKSUM_MD5, CSV_FIELD_CHECKSUM_SHA1), ",");
  public static final String CSV_DEFAULT_OUTPUT = "/tmp/output.csv";
  public static final String CSV_DEFAULT_HEADERS = "true";
  public static final String CSV_DEFAULT_OTHER_METADATA = "tika,siegfried";

  private List<String> fields = null;
  private Path output;
  private boolean enableHeaders;
  private boolean outputDataInformation;
  private boolean outputDescriptiveMetadataInformation;
  private List<String> otherMetadataTypes;
  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  // TODO -> add plugin parameter type "LIST"...
  static {
    pluginParameters.put(CSV_FILE_FIELDS, new PluginParameter(CSV_FILE_FIELDS, "Attributes to include in the report",
      PluginParameterType.STRING, CSV_DEFAULT_FIELDS, true, false,
      "List of file attributes to include in the inventory export. The example includes all the possible options. Remove attributes as necessary."));
    pluginParameters.put(CSV_FILE_OUTPUT,
      new PluginParameter(CSV_FILE_OUTPUT, "Report file path", PluginParameterType.STRING, CSV_DEFAULT_OUTPUT, true,
        false, "The full path and file name on the server where the inventory report file should be created."));
    pluginParameters.put(CSV_FILE_HEADERS,
      new PluginParameter(CSV_FILE_HEADERS, "Include header line", PluginParameterType.BOOLEAN, CSV_DEFAULT_HEADERS,
        true, false, "Include a header line in the CSV inventory report."));
    pluginParameters.put(CSV_FILE_OUTPUT_DATA,
      new PluginParameter(CSV_FILE_OUTPUT_DATA, "Include data files", PluginParameterType.BOOLEAN, CSV_DEFAULT_HEADERS,
        true, false, "Include in the inventory report information about data files that exist inside AIPs."));
    pluginParameters.put(CSV_FILE_OUTPUT_DESCRIPTIVE,
      new PluginParameter(CSV_FILE_OUTPUT_DESCRIPTIVE, "Include descriptive metadata files",
        PluginParameterType.BOOLEAN, CSV_DEFAULT_HEADERS, true, false,
        "Include in the inventory report information about descriptive metadata files that exist inside AIPs."));
    pluginParameters.put(CSV_FILE_OTHER_METADATA_TYPES,
      new PluginParameter(CSV_FILE_OTHER_METADATA_TYPES, "Include other metadata files", PluginParameterType.STRING,
        CSV_DEFAULT_OTHER_METADATA, true, false,
        "Include in the inventory report information about other metadata files that exist inside AIPs."));
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
    return "Inventory report";
  }

  @Override
  public String getDescription() {
    return "Creates a report in CSV format that includes a listing of all AIP and its inner files (data and metadata) which also includes some of their technical properties (e.g. sipId, aipId, representationId, filePath, SHA-256, MD5, SHA-1). The report will be stored in a folder on the server side as defined by the user. To obtain the report, one needs access to the storage layer of the repository server.\nThis report may be used to validate the completeness and correctness of an ingest process.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<PluginParameter>();
    parameters.add(pluginParameters.get(CSV_FILE_FIELDS));
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
      if (fieldsSTR != null && !fieldsSTR.trim().equalsIgnoreCase("")) {
        fields = new ArrayList<String>();
        fields.addAll(Arrays.asList(fieldsSTR.split(",")));
      }
    }
    if (parameters.containsKey(CSV_FILE_OUTPUT)) {
      try {
        output = Paths.get(parameters.get(CSV_FILE_OUTPUT));
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
      if (otherMetadataSTR != null && !otherMetadataSTR.trim().equalsIgnoreCase("")) {
        otherMetadataTypes = new ArrayList<String>();
        otherMetadataTypes.addAll(Arrays.asList(otherMetadataSTR.split(",")));
      }
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    final CSVPrinter csvFilePrinter = createCSVPrinter();

    return PluginHelper.processObjects(this, new RODAObjectProcessingLogic<AIP>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        SimpleJobPluginInfo jobPluginInfo, Plugin<AIP> plugin, AIP object) {
        processAIP(model, storage, jobPluginInfo, csvFilePrinter, object);
      }
    }, new RODAProcessingLogic<AIP>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        SimpleJobPluginInfo jobPluginInfo, Plugin<AIP> plugin) {
        IOUtils.closeQuietly(csvFilePrinter);
      }
    }, index, model, storage, liteList);
  }

  private CSVPrinter createCSVPrinter() {
    Path jobCSVTempFolder = getJobCSVTempFolder();
    Path csvTempFile = jobCSVTempFolder.resolve(UUID.randomUUID().toString() + ".csv");

    CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
    try {
      BufferedWriter fileWriter = Files.newBufferedWriter(csvTempFile);
      return new CSVPrinter(fileWriter, csvFileFormat);
    } catch (IOException e) {
      LOGGER.error("Unable to instantiate CSVPrinter", e);
      return null;
    }
  }

  private void processAIP(ModelService model, StorageService storage, SimpleJobPluginInfo jobPluginInfo,
    CSVPrinter csvFilePrinter, AIP aip) {
    if (csvFilePrinter == null) {
      LOGGER.warn("CSVPrinter is NULL! Skipping...");
      return;
    }

    try {
      if (outputDataInformation && aip.getRepresentations() != null) {
        List<List<String>> dataInformation = InventoryReportPluginUtils.getDataInformation(fields, aip, model, storage);
        csvFilePrinter.printRecords(dataInformation);
      }
      if (outputDescriptiveMetadataInformation && aip.getDescriptiveMetadata() != null) {
        List<List<String>> dataInformation = InventoryReportPluginUtils.getDescriptiveMetadataInformation(fields, aip,
          model, storage);
        csvFilePrinter.printRecords(dataInformation);
      }
      if (otherMetadataTypes != null && !otherMetadataTypes.isEmpty()) {
        for (String otherMetadataType : otherMetadataTypes) {
          List<List<String>> otherMetadataInformation = InventoryReportPluginUtils.getOtherMetadataInformation(fields,
            otherMetadataType, aip, model, storage);
          csvFilePrinter.printRecords(otherMetadataInformation);
        }
      }
      // if(outputOtherMetadataInformation && aip.getOtherMetadata()!=null){
      //
      // }
      jobPluginInfo.incrementObjectsProcessedWithSuccess();
    } catch (IOException e) {
      jobPluginInfo.incrementObjectsProcessedWithFailure();
    }
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
    try {
      Path reportsFolder = RodaCoreFactory.getRodaHomePath().resolve(RodaConstants.CORE_REPORT_FOLDER);
      if (Files.exists(reportsFolder)) {
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
      } catch (IOException e) {
        LOGGER.error("Error while merging partial CSVs", e);
      }
      try {
        InventoryReportPluginUtils.mergeFiles(partials, output);
        FSUtils.deletePathQuietly(csvTempFolder);
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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_MANAGEMENT);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }
}
