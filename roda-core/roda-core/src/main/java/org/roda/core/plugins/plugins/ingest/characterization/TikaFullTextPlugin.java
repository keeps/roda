/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.tika.exception.TikaException;
import org.roda.core.common.PremisUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Attribute;
import org.roda.core.data.v2.jobs.JobReport.PluginState;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.ReportItem;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class TikaFullTextPlugin implements Plugin<AIP> {

  public static final String OUTPUT_EXT = ".html";

  public static final String APP_NAME = "ApacheTika";

  private static final Logger LOGGER = LoggerFactory.getLogger(TikaFullTextPlugin.class);

  private Map<String, String> parameters;

  private boolean createsPluginEvent = true;
  private ContentPayload agent;

  @Override
  public void init() throws PluginException {
    try {
      agent = PremisUtils.createPremisAgentBinary(getName() + "/" + getVersion(), "characterization-tika",
        RodaConstants.PRESERVATION_AGENT_TYPE_CHARACTERIZATION_PLUGIN);
    } catch (GenericException e) {
      throw new PluginException("Error initializing agent", e);
    }
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Full-text extraction action";
  }

  @Override
  public String getDescription() {
    return "Extracts the full-text from the representation files";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    return new ArrayList<>();
  }

  @Override
  public Map<String, String> getParameterValues() {
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    this.parameters = parameters;

    // updates the flag responsible to allow plugin event creation
    if (parameters.containsKey("createsPluginEvent")) {
      createsPluginEvent = Boolean.parseBoolean(parameters.get("createsPluginEvent"));
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    Report report = PluginHelper.createPluginReport(this);
    PluginState state;

    try {
      model.createPreservationMetadata(PreservationMetadataType.AGENT, null, null, getName() + "/" + getVersion(),
        agent);
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.error("Error running adding Siegfried plugin: " + e.getMessage(), e);
    }

    for (AIP aip : list) {
      ReportItem reportItem = PluginHelper.createPluginReportItem(this, "File metadata and full-text extraction",
        aip.getId(), null);

      LOGGER.debug("Processing AIP " + aip.getId());
      try {
        for (Representation representation : aip.getRepresentations()) {
          LOGGER.debug("Processing representation " + representation.getId() + " of AIP " + aip.getId());
          ClosableIterable<File> allFiles = model.listAllFiles(aip.getId(), representation.getId());
          List<IndexedFile> updatedFiles = new ArrayList<IndexedFile>();
          for (File file : allFiles) {

            if (!file.isDirectory()) {
              LOGGER.debug("Processing file: " + file);
              StoragePath storagePath = ModelUtils.getRepresentationFileStoragePath(file);
              Binary binary = storage.getBinary(storagePath);

              // FIXME file that doesn't get deleted afterwards
              Path tikaResult = TikaFullTextPluginUtils.extractMetadata(binary.getContent().createInputStream());

              Binary resource = (Binary) FSUtils.convertPathToResource(tikaResult.getParent(), tikaResult);
              model.createOtherMetadata(aip.getId(), representation.getId(), file.getId() + OUTPUT_EXT, APP_NAME,
                resource);
              try {
                IndexedFile f = index.retrieve(IndexedFile.class,
                  SolrUtils.getId(aip.getId(), representation.getId(), file.getId()));

                Map<String, String> properties = TikaFullTextPluginUtils.extractPropertiesFromResult(tikaResult);
                if (properties.containsKey(RodaConstants.FILE_FULLTEXT)) {
                  f.setFulltext(properties.get(RodaConstants.FILE_FULLTEXT));
                }
                if (properties.containsKey(RodaConstants.FILE_CREATING_APPLICATION_NAME)) {
                  f.setCreatingApplicationName(properties.get(RodaConstants.FILE_CREATING_APPLICATION_NAME));
                }
                if (properties.containsKey(RodaConstants.FILE_CREATING_APPLICATION_VERSION)) {
                  f.setCreatingApplicationVersion(properties.get(RodaConstants.FILE_CREATING_APPLICATION_VERSION));
                }
                if (properties.containsKey(RodaConstants.FILE_DATE_CREATED_BY_APPLICATION)) {
                  f.setDateCreatedByApplication(properties.get(RodaConstants.FILE_DATE_CREATED_BY_APPLICATION));
                }
                LOGGER.info("Tika creating app name: " + f.getCreatingApplicationName() + " version: "
                  + f.getCreatingApplicationVersion() + " date: " + f.getDateCreatedByApplication());
                updatedFiles.add(f);
              } catch (ParserConfigurationException pce) {

              }
            }
          }
          IOUtils.closeQuietly(allFiles);
          model.updateFileFormats(updatedFiles);
        }

        state = PluginState.SUCCESS;
        reportItem.addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()));

      } catch (RODAException | SAXException | TikaException | IOException e) {
        LOGGER.error("Error processing AIP " + aip.getId() + ": " + e.getMessage(), e);

        state = PluginState.FAILURE;
        reportItem.addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()))
          .addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS,
            "Error running Tika " + aip.getId() + ": " + e.getMessage()));
      }

      report.addItem(reportItem);
      if (createsPluginEvent) {
        PluginHelper.updateJobReport(model, index, this, reportItem, state, PluginHelper.getJobId(parameters),
          aip.getId());
      }
    }

    return report;
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    TikaFullTextPlugin tikaPlugin = new TikaFullTextPlugin();
    try {
      tikaPlugin.init();
    } catch (PluginException e) {
      LOGGER.error("Error doing " + TikaFullTextPlugin.class.getName() + "init", e);
    }
    return tikaPlugin;
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

}
