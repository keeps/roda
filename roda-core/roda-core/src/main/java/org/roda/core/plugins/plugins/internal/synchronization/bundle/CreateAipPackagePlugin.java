package org.roda.core.plugins.plugins.internal.synchronization.bundle;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.ShallowFile;
import org.roda.core.data.v2.ip.ShallowFiles;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.synchronization.bundle.PackageState;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.protocols.protocols.RODAProtocol;
import org.roda.core.storage.StorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.roda.core.data.utils.JsonUtils.getJsonFromObject;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class CreateAipPackagePlugin extends CreateRodaEntityPackagePlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateAipPackagePlugin.class);

  @Override
  public String getName() {
    return "Create AIP Bundle";
  }

  @Override
  public String getVersionImpl() {
    return "1.0.0";
  }

  @Override
  protected String getEntity() {
    return "aip";
  }

  @Override
  protected void createBundle(IndexService index, ModelService model, Report pluginReport, JobPluginInfo jobPluginInfo,
    Job job) {
    pluginReport.setPluginState(PluginState.SUCCESS);

    SelectedItems<?> sourceObjects = job.getSourceObjects();

    if (sourceObjects instanceof SelectedItemsFilter) {
      Filter filter = ((SelectedItemsFilter) sourceObjects).getFilter();
      try {
        int counter = index.count(IndexedAIP.class, filter).intValue();

        jobPluginInfo.setSourceObjectsCount(counter);

        PackageState packageState = SyncBundleHelper.getPackageState(getLocalInstance(), getEntity());
        packageState.setClassName(AIP.class);
        packageState.setCount(counter);
        SyncBundleHelper.updatePackageState(getLocalInstance(), getEntity(), packageState);

        IterableIndexResult<IndexedAIP> aips = index.findAll(IndexedAIP.class, filter,
          Arrays.asList(RodaConstants.INDEX_UUID));
        for (IndexedAIP aip : aips) {
          Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), IndexedAIP.class);
          AIP retrieveAIP = null;
          try {
            retrieveAIP = model.retrieveAIP(aip.getId());
            createAIPBundle(model, index, retrieveAIP);
            packageState.addIdList(retrieveAIP.getId());
            SyncBundleHelper.updatePackageState(getLocalInstance(), getEntity(), packageState);
            jobPluginInfo.incrementObjectsProcessedWithSuccess();
          } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
            LOGGER.error("Error on create bundle for aip {}", aip.getId());
            jobPluginInfo.incrementObjectsProcessedWithFailure();
            reportItem.addPluginDetails("Failed to create bundle for " + aip.getClass() + " " + aip.getId() + "\n");
            reportItem.addPluginDetails(e.getMessage());
            pluginReport.addReport(reportItem.setPluginState(PluginState.FAILURE));
            PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
          }
        }
      } catch (RODAException | IOException e) {
        LOGGER.error("Error on retrieve indexes of a RODA entity", e);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      }
    }
  }

  public void createAIPBundle(ModelService model, IndexService index, AIP aip) throws RequestNotValidException,
    NotFoundException, AuthorizationDeniedException, GenericException, AlreadyExistsException, IOException {

    StorageService storage = model.getStorage();
    StoragePath aipStoragePath = ModelUtils.getAIPStoragePath(aip.getId());
    Path destinationPath = getDestinationPath().resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_AIP).resolve(aip.getId());

    Path documentationPath = destinationPath.resolve(RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION);
    Path metadataPath = destinationPath.resolve(RodaConstants.STORAGE_DIRECTORY_METADATA);
    Path schemasPath = destinationPath.resolve(RodaConstants.STORAGE_DIRECTORY_SCHEMAS);
    Path submissionsPath = destinationPath.resolve(RodaConstants.STORAGE_DIRECTORY_SUBMISSION);
    Path aipMetadataPath = destinationPath.resolve(RodaConstants.STORAGE_AIP_METADATA_FILENAME);

    storage.copy(storage, aipStoragePath, documentationPath, RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION);
    storage.copy(storage, aipStoragePath, metadataPath, RodaConstants.STORAGE_DIRECTORY_METADATA);
    storage.copy(storage, aipStoragePath, schemasPath, RodaConstants.STORAGE_DIRECTORY_SCHEMAS);
    storage.copy(storage, aipStoragePath, submissionsPath, RodaConstants.STORAGE_DIRECTORY_SUBMISSION);
    storage.copy(storage, aipStoragePath, aipMetadataPath, RodaConstants.STORAGE_AIP_METADATA_FILENAME);

    for (Representation representation : aip.getRepresentations()) {
      Path repDataPath = Paths.get(RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS, representation.getId(),
        RodaConstants.STORAGE_DIRECTORY_DATA);
      Path repDataDestinationPath = destinationPath.resolve(repDataPath);

      Path repMetadataPath = Paths.get(RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS, representation.getId(),
        RodaConstants.STORAGE_DIRECTORY_METADATA);
      Path repMetadataDestinationPath = destinationPath.resolve(repMetadataPath);
      storage.copy(storage, aipStoragePath, repMetadataDestinationPath, repMetadataPath.toString());

      ShallowFiles shallowFiles = new ShallowFiles();
      addFilesToBundle(shallowFiles, aip, representation, index);

      if (shallowFiles.getObjects().size() != 0) {
        Files.createDirectory(repDataDestinationPath);
        Path temp = Files.createFile(repDataDestinationPath.resolve(RodaConstants.RODA_MANIFEST_EXTERNAL_FILES));

        for (ShallowFile shallowFile : shallowFiles.getObjects()) {
          JsonUtils.appendObjectToFile(shallowFile, temp);
        }

      }

    }
  }

  private void addFilesToBundle(ShallowFiles shallowFiles, AIP aip, Representation representation, IndexService index) {
    FilterParameter aipFilterParameter = new SimpleFilterParameter(RodaConstants.FILE_AIP_ID, aip.getId());
    FilterParameter repFilterParameter = new SimpleFilterParameter(RodaConstants.FILE_REPRESENTATION_ID,
      representation.getId());

    Filter filter = new Filter();
    filter.add(aipFilterParameter);
    filter.add(repFilterParameter);

    try (IterableIndexResult<IndexedFile> result = index.findAll(IndexedFile.class, filter, Collections.emptyList())) {
      for (IndexedFile indexedFile : result) {
        ShallowFile shallowFile = convertFileOnShallow(aip, representation, indexedFile);
        shallowFiles.addObject(shallowFile);
      }
    } catch (GenericException | RequestNotValidException | IOException | AuthorizationDeniedException
      | AlreadyExistsException | NotFoundException | URISyntaxException e) {
      LOGGER.error("Error getting Files to convert to shallow", e);
    }
  }

  private ShallowFile convertFileOnShallow(AIP aip, Representation representation, IndexedFile indexedFile)
    throws RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException, NotFoundException,
    GenericException, IOException, URISyntaxException {
    ShallowFile shallowFile = new ShallowFile();
    shallowFile.setName(indexedFile.getId());
    shallowFile.setUUID(IdUtils.getFileId(aip.getId(), representation.getId(), null, shallowFile.getName()));

    RODAProtocol rodaProtocol = new RODAProtocol();
    String uriString = rodaProtocol.getSchema() + RodaConstants.PROTOCOL_SEPARATOR
      + RodaCoreFactory.getLocalInstance().getId() + "/" + RodaConstants.RODA_OBJECT_FILE + "/" + indexedFile.getUUID();
    shallowFile.setLocation(URI.create(uriString));

    shallowFile.setSize(indexedFile.getSize());
    shallowFile.setMimeType(indexedFile.getFileFormat().getMimeType());

    shallowFile.setChecksum(indexedFile.getSHA256Checksum());
    shallowFile.setChecksumType(indexedFile.getSHA256Type());

    return shallowFile;
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new CreateAipPackagePlugin();
  }
}
