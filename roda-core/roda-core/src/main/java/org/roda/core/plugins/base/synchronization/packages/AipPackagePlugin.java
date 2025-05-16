/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.synchronization.packages;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.AllFilterParameter;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.ShallowFile;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.protocols.protocols.RODAProtocol;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AipPackagePlugin extends RodaEntityPackagesPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AipPackagePlugin.class);

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  public static String getStaticName() {
    return "AipPackagePlugin";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
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
  protected Class getEntityClass() {
    return AIP.class;
  }

  @Override
  protected List<IterableIndexResult> retrieveList(IndexService index)
    throws RequestNotValidException, GenericException {
    List<IterableIndexResult> list = new ArrayList<>();
    list.add(listIndexedAip(index));
    list.add(listPreservationEvents(index));

    return list;
  }

  @Override
  protected void createPackage(IndexService index, ModelService model, IterableIndexResult objectList)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    AlreadyExistsException {
    for (Object object : objectList) {
      if (object instanceof IndexedAIP) {
        AIP aip = model.retrieveAIP(((IndexedAIP) object).getId());
        createAIPBundle(model, index, aip);
      } else if (object instanceof IndexedPreservationEvent) {
        AIP aip = model.retrieveAIP(((IndexedPreservationEvent) object).getAipID());
        createAIPBundle(model, index, aip);
      }
    }
  }

  private IterableIndexResult<IndexedAIP> listIndexedAip(IndexService index)
    throws RequestNotValidException, GenericException {
    Filter filter = new Filter();
    if (fromDate != null) {
      filter.add(
        new DateIntervalFilterParameter(RodaConstants.AIP_UPDATED_ON, RodaConstants.AIP_UPDATED_ON, fromDate, toDate));
    } else {
      filter.add(new AllFilterParameter());
    }
    return index.findAll(IndexedAIP.class, filter, Collections.singletonList(RodaConstants.INDEX_UUID));
  }

  private IterableIndexResult<IndexedPreservationEvent> listPreservationEvents(IndexService index)
    throws RequestNotValidException, GenericException {
    Filter filter = new Filter();
    filter.add(new NotSimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS,
      IndexedPreservationEvent.PreservationMetadataEventClass.REPOSITORY.toString()));
    if (fromDate != null) {
      filter.add(new DateIntervalFilterParameter(RodaConstants.PRESERVATION_EVENT_DATETIME,
        RodaConstants.PRESERVATION_EVENT_DATETIME, fromDate, toDate));
    }
    return index.findAll(IndexedPreservationEvent.class, filter,
      Collections.singletonList(RodaConstants.PRESERVATION_EVENT_AIP_ID));
  }

  public void createAIPBundle(ModelService model, IndexService index, AIP aip)
    throws RequestNotValidException, AuthorizationDeniedException, GenericException, AlreadyExistsException {
    Path destinationPath = workingDirPath.resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_AIP).resolve(aip.getId());

    if (!Files.exists(destinationPath)) {
      totalCount++; //
      Path documentationPath = destinationPath.resolve(RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION);
      Path metadataPath = destinationPath.resolve(RodaConstants.STORAGE_DIRECTORY_METADATA);
      Path schemasPath = destinationPath.resolve(RodaConstants.STORAGE_DIRECTORY_SCHEMAS);
      Path submissionsPath = destinationPath.resolve(RodaConstants.STORAGE_DIRECTORY_SUBMISSION);
      Path aipMetadataPath = destinationPath.resolve(RodaConstants.STORAGE_AIP_METADATA_FILENAME);

      model.exportToPath(aip, RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION, , documentationPath, );
      model.exportToPath(aip, RodaConstants.STORAGE_DIRECTORY_METADATA, , metadataPath, );
      model.exportToPath(aip, RodaConstants.STORAGE_DIRECTORY_SCHEMAS, , schemasPath, );
      model.exportToPath(aip, RodaConstants.STORAGE_DIRECTORY_SUBMISSION, , submissionsPath, );
      model.exportToPath(aip, RodaConstants.STORAGE_AIP_METADATA_FILENAME, , aipMetadataPath, );

      for (Representation representation : aip.getRepresentations()) {
        Path repDataPath = Paths.get(RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS, representation.getId(),
          RodaConstants.STORAGE_DIRECTORY_DATA);
        Path repDataDestinationPath = destinationPath.resolve(repDataPath);

        Path repMetadataPath = Paths.get(RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS, representation.getId(),
          RodaConstants.STORAGE_DIRECTORY_METADATA);
        Path repMetadataDestinationPath = destinationPath.resolve(repMetadataPath);
        model.exportToPath(aip, repMetadataPath.toString(), , repMetadataDestinationPath, );

        addFilesToBundle(aip, representation, index, repDataDestinationPath);
      }
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
    } catch (GenericException | RequestNotValidException | IOException e) {
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
    throws GenericException {
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
  public void addTotalCount(long totalCount) {
    // do nothing; AIPs are counted during package creation;
  }
}
