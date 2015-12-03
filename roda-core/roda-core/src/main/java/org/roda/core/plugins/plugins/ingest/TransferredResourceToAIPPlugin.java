/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.data.v2.Job;
import org.roda.core.data.v2.TransferredResource;
import org.roda.core.index.IndexService;
import org.roda.core.model.AIP;
import org.roda.core.model.File;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceException;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginUtils;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageServiceException;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferredResourceToAIPPlugin implements Plugin<TransferredResource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransferredResourceToAIPPlugin.class);

  private Map<String, String> parameters;

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "TransferredResource to AIP";
  }

  @Override
  public String getDescription() {
    return "Converts a TransferredResource to an AIP";
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
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<TransferredResource> list)
    throws PluginException {

    String jobId = PluginUtils.getJobId(parameters);
    for (TransferredResource transferredResource : list) {
      try {
        Path transferredResourcePath = Paths.get(transferredResource.getFullPath());
        AIP aip = model.createAIP(new HashMap<String, Set<String>>(), false, true);
        final String aipID = aip.getId();
        String representationID = "representation";
        IngestUtils.createDirectories(model, aip.getId(), representationID);
        if(transferredResource.isFile()){
          Binary fileBinary = (Binary) FSUtils.convertPathToResource(transferredResourcePath.getParent(), transferredResourcePath);
          model.createFile(aip.getId(), representationID, transferredResource.getName(), fileBinary);
        }else{
          EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
          Files.walkFileTree(transferredResourcePath, opts, Integer.MAX_VALUE, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
              try{
                Binary fileBinary = (Binary) FSUtils.convertPathToResource(file.getParent(), file);
                model.createFile(aipID, representationID, transferredResource.getName(), fileBinary);
              }catch(StorageServiceException | ModelServiceException sse){
                
              }
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
              return FileVisitResult.CONTINUE;
            }
          });
        }
        aip = model.retrieveAIP(aip.getId());
        Job job = index.retrieve(Job.class, jobId);
        job.addObjectIdToAipIdMapping(transferredResource.getId(), aip.getId());
        model.updateJob(job);
      } catch (Throwable e) {
        LOGGER.error("Error converting " + transferredResource.getId() + " to AIP: " + e.getMessage(), e);
      }

    }
    return null;
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
  public Plugin<TransferredResource> cloneMe() {
    return new TransferredResourceToAIPPlugin();
  }

}
