package org.roda.core.plugins.plugins.internal.synchronization.packages;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.*;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.*;
import org.roda.core.data.v2.ip.*;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.protocols.protocols.RODAProtocol;
import org.roda.core.storage.StorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class AipPackagePlugin extends RodaEntityPackagesPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AipPackagePlugin.class);

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getName() {
    return "AipPackagePlugin";
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new AipPackagePlugin();
  }

  @Override
  protected String getEntity() {
    return "aip";
  }

  @Override
  protected Class<AIP> getEntityClass() {
    return AIP.class;
  }

  @Override
  protected List<String> retrieveList(IndexService index) throws RequestNotValidException, GenericException {
    Set<String> aipHashSet = new HashSet<>();

    Filter filter = new Filter();
    if (fromDate != null) {
      filter.add(
        new DateIntervalFilterParameter(RodaConstants.AIP_UPDATED_ON, RodaConstants.AIP_UPDATED_ON, fromDate, toDate));
    }

    IterableIndexResult<IndexedAIP> aips = index.findAll(IndexedAIP.class, filter,
      Collections.singletonList(RodaConstants.INDEX_UUID));
    for (IndexedAIP aip : aips) {
      aipHashSet.add(aip.getUUID());
    }
    retrievePreservationsEvents(index, aipHashSet);

    return new ArrayList<>(aipHashSet);
  }

  private void retrievePreservationsEvents(IndexService index, Set<String> aipHashSet)
    throws RequestNotValidException, GenericException {
    Filter filter = new Filter();
    filter.add(new NotSimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS,
      IndexedPreservationEvent.PreservationMetadataEventClass.REPOSITORY.toString()));
    if (fromDate != null) {
      filter.add(new DateIntervalFilterParameter(RodaConstants.PRESERVATION_EVENT_DATETIME,
        RodaConstants.PRESERVATION_EVENT_DATETIME, fromDate, toDate));
    }

    IterableIndexResult<IndexedPreservationEvent> preservationEvents = index.findAll(IndexedPreservationEvent.class,
      filter, Collections.singletonList(RodaConstants.PRESERVATION_EVENT_AIP_ID));
    for (IndexedPreservationEvent preservationEvent : preservationEvents) {
      aipHashSet.add(preservationEvent.getAipID());
    }
  }

  @Override
  protected void createPackage(IndexService index, ModelService model, List<String> list) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, NotFoundException, AlreadyExistsException, IOException {

    for (String aipId : list) {
      AIP aip = model.retrieveAIP(aipId);
      createAIPBundle(model, index, aip);
    }
  }

  public void createAIPBundle(ModelService model, IndexService index, AIP aip) throws RequestNotValidException,
    NotFoundException, AuthorizationDeniedException, GenericException, AlreadyExistsException, IOException {

    StorageService storage = model.getStorage();
    StoragePath aipStoragePath = ModelUtils.getAIPStoragePath(aip.getId());
    Path destinationPath = bundlePath.resolve(RodaConstants.CORE_STORAGE_FOLDER)
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

      addFilesToBundle(aip, representation, index, repDataDestinationPath);

    }
  }

  private void addFilesToBundle(AIP aip, Representation representation, IndexService index,
    Path repDataDestinationPath) {
    FilterParameter aipFilterParameter = new SimpleFilterParameter(RodaConstants.FILE_AIP_ID, aip.getId());
    FilterParameter repFilterParameter = new SimpleFilterParameter(RodaConstants.FILE_REPRESENTATION_ID,
      representation.getId());

    Filter filter = new Filter();
    filter.add(aipFilterParameter);
    filter.add(repFilterParameter);

    try (IterableIndexResult<IndexedFile> result = index.findAll(IndexedFile.class, filter, Collections.emptyList())) {
      for (IndexedFile indexedFile : result) {
        createDirectory(repDataDestinationPath);
        if (indexedFile.isDirectory()) {
          createDirectoryOnBundle(indexedFile, repDataDestinationPath);
        } else {
          ShallowFile shallowFile = convertFileOnShallow(aip, representation, indexedFile);
          createFileOnBundle(repDataDestinationPath, indexedFile, shallowFile);
        }
      }
    } catch (GenericException | RequestNotValidException | IOException | AuthorizationDeniedException
      | AlreadyExistsException | NotFoundException | URISyntaxException e) {
      LOGGER.error("Error getting Files to convert to shallow", e);
    }
  }

  private void createFileOnBundle(Path repDataDestinationPath, IndexedFile indexedFile, ShallowFile shallowFile)
    throws IOException, GenericException {
    List<String> subFolders = indexedFile.getPath();
    Path subFolder;
    if (subFolders != null && subFolders.size() != 0) {
      subFolder = repDataDestinationPath.resolve(subFolders.get(0));
      createDirectory(subFolder);
      for (int i = 1; i < subFolders.size(); i++) {
        subFolder = subFolder.resolve(subFolders.get(i));
        createDirectory(subFolder);
      }
    } else {
      subFolder = repDataDestinationPath;
    }

    Path manifestExternalFiles = subFolder.resolve(RodaConstants.RODA_MANIFEST_EXTERNAL_FILES);
    if (!java.nio.file.Files.exists(manifestExternalFiles)) {
      manifestExternalFiles = java.nio.file.Files.createFile(manifestExternalFiles);
    }
    JsonUtils.appendObjectToFile(shallowFile, manifestExternalFiles);

  }

  private void createDirectoryOnBundle(IndexedFile indexedFile, Path repDataDestinationPath) throws IOException {
    List<String> subFolders = indexedFile.getPath();
    Path subFolder;
    if (subFolders != null && subFolders.size() != 0) {
      subFolder = repDataDestinationPath.resolve(subFolders.get(0));
      createDirectory(subFolder);
      for (int i = 1; i < subFolders.size(); i++) {
        subFolder = subFolder.resolve(subFolders.get(i));
        createDirectory(subFolder);
      }
      subFolder = subFolder.resolve(indexedFile.getId());
    } else {
      subFolder = repDataDestinationPath.resolve(indexedFile.getId());
    }
    createDirectory(subFolder);

  }

  private void createDirectory(Path subFolder) {
    if (!java.nio.file.Files.exists(subFolder)) {
      try {
        Files.createDirectory(subFolder);
      } catch (IOException e) {
        LOGGER.error("Error creating directory on bundle: {}", subFolder.toString());
      }
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
}
