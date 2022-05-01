package org.roda.core.plugins.plugins.internal.synchronization;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.synchronization.bundle.AttachmentState;
import org.roda.core.data.v2.synchronization.bundle.v2.BundleManifest;
import org.roda.core.data.v2.synchronization.bundle.v2.PackageState;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IndexUtils;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Container;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class BundleManifestCreator {
  private static final Logger LOGGER = LoggerFactory.getLogger(BundleManifestCreator.class);
  private final String MANIFEST_FILE = "manifest.json";
  private final static String VALIDATION_ENTITIES_DIR = "validation";

  private Path workingDir;
  private Date fromDate;
  private Date toDate;
  private RodaConstants.DistributedModeType type;

  public BundleManifestCreator(Path workingDir) {
    this.workingDir = workingDir;
  }

  public BundleManifestCreator(RodaConstants.DistributedModeType type, Path workingDir) {
    this(type, workingDir, null, new Date());
  }

  public BundleManifestCreator(RodaConstants.DistributedModeType type, Path workingDir, Date fromDate, Date toDate) {
    this.type = type;
    this.workingDir = workingDir;
    this.fromDate = fromDate;
    this.toDate = toDate;
  }

  public BundleManifest parse() throws GenericException, FileNotFoundException {
    Path manifestFile = workingDir.resolve(MANIFEST_FILE);
    if (Files.exists(manifestFile)) {
      return JsonUtils.readObjectFromFile(manifestFile, BundleManifest.class);
    } else {
      throw new FileNotFoundException("Cannot found manifest file at " + manifestFile);
    }
  }

  public void create() throws GenericException, NotFoundException, IOException {
    FileStorageService tmpStorage = new FileStorageService(workingDir.resolve(RodaConstants.CORE_STORAGE_FOLDER), false,
      null, false);

    BundleManifest bundleManifest = new BundleManifest();
    bundleManifest.setId(IdUtils.createUUID());
    bundleManifest.setFromDate(fromDate);
    bundleManifest.setToDate(toDate);
    bundleManifest.setPackageStateList(getPackageList(tmpStorage));
    bundleManifest.setAttachmentStateList(getAttachmentStateList());
    bundleManifest.setValidationEntityList(getValidationEntityList());
    Path manifestPath = workingDir.resolve(MANIFEST_FILE);
    JsonUtils.writeObjectToFile(bundleManifest, manifestPath);
  }

  private List<PackageState> getPackageList(FileStorageService tmpStorage) throws GenericException, NotFoundException {
    ArrayList<PackageState> packageStates = new ArrayList<>();

    for (Container container : tmpStorage.listContainers()) {
      Long count = tmpStorage.countResourcesUnderContainer(container.getStoragePath(), false);
      String containerName = container.getStoragePath().getContainerName();
      Class<IsRODAObject> objectClass = ModelUtils.giveRespectiveModelClassFromContainerName(containerName);

      PackageState packageState = new PackageState();
      packageState.setCount(count.intValue());
      packageState.setClassName(objectClass);
      packageStates.add(packageState);
    }
    return packageStates;
  }

  private List<AttachmentState> getAttachmentStateList() throws IOException {
    ArrayList<AttachmentState> attachmentStates = new ArrayList<>();
    Path jobAttachmentPath = workingDir.resolve(RodaConstants.CORE_JOB_ATTACHMENTS_FOLDER);
    if (Files.exists(jobAttachmentPath)) {
      try (Stream<Path> jobs = Files.list(jobAttachmentPath)) {
        for (Path jobPath : jobs.collect(Collectors.toSet())) {
          AttachmentState attachmentState = new AttachmentState();
          attachmentState.setJobId(jobPath.getFileName().toString());
          try (Stream<Path> attachment = Files.list(jobPath)) {
            attachmentState
              .setAttachmentIdList(attachment.map(Path::getFileName).map(Path::toString).collect(Collectors.toList()));
          }
          attachmentStates.add(attachmentState);
        }
      }
    }
    return attachmentStates;
  }

  private List<PackageState> getValidationEntityList() throws IOException {
    Path validationEntitiesDirectoryPath = workingDir.resolve(VALIDATION_ENTITIES_DIR);
    Files.createDirectory(validationEntitiesDirectoryPath);

    if (RodaConstants.DistributedModeType.CENTRAL.equals(type)) {
      return getCentralValidationEntityList();
    } else {
      return getLocalValidationEntityList();
    }
  }

  private List<PackageState> getLocalValidationEntityList() throws IOException {
    final List<PackageState> validationEntityList = new ArrayList<>();
    // AIPs
    final PackageState aipValidation = new PackageState();
    aipValidation.setClassName(AIP.class);
    aipValidation.setFilePath(RodaConstants.SYNCHRONIZATION_VALIDATION_AIP_FILE_PATH);
    aipValidation.setCount(writeValidationList(aipValidation));
    validationEntityList.add(aipValidation);

    // DIPs
    final PackageState dipValidationPackageState = new PackageState();
    dipValidationPackageState.setClassName(DIP.class);
    dipValidationPackageState.setFilePath(RodaConstants.SYNCHRONIZATION_VALIDATION_DIP_FILE_PATH);
    dipValidationPackageState.setCount(writeValidationList(dipValidationPackageState));
    validationEntityList.add(dipValidationPackageState);

    // DIPs
    final PackageState riskIncidentValidationPackageState = new PackageState();
    riskIncidentValidationPackageState.setClassName(RiskIncidence.class);
    riskIncidentValidationPackageState.setFilePath(RodaConstants.SYNCHRONIZATION_VALIDATION_RISK_INCIDENT_FILE_PATH);
    riskIncidentValidationPackageState.setCount(writeValidationList(riskIncidentValidationPackageState));
    validationEntityList.add(riskIncidentValidationPackageState);

    return validationEntityList;
  }

  private List<PackageState> getCentralValidationEntityList() throws IOException {
    final List<PackageState> validationEntityList = new ArrayList<>();
    // Representation information
    final PackageState representationInformationValidation = new PackageState();
    representationInformationValidation.setClassName(RepresentationInformation.class);
    representationInformationValidation
      .setFilePath(RodaConstants.SYNCHRONIZATION_VALIDATION_REPRESENTATION_INFORMATION_FILE_PATH);
    representationInformationValidation.setCount(writeValidationList(representationInformationValidation));
    validationEntityList.add(representationInformationValidation);

    // Representation information
    final PackageState riskValidation = new PackageState();
    riskValidation.setClassName(IndexedRisk.class);
    riskValidation.setFilePath(RodaConstants.SYNCHRONIZATION_VALIDATION_RISK_FILE_PATH);
    riskValidation.setCount(writeValidationList(riskValidation));
    validationEntityList.add(riskValidation);

    return validationEntityList;
  }

  private int writeValidationList(PackageState validationPackage) throws IOException {
    final IndexService index = RodaCoreFactory.getIndexService();
    final Filter filter = new Filter();
    final Class<? extends IsIndexed> indexedClass = IndexUtils
      .giveRespectiveIndexedClassFromModelClass(validationPackage.getClassName());
    int count = 0;
    Path filePath = workingDir.resolve(validationPackage.getFilePath());

    if (indexedClass == IndexedPreservationEvent.class) {
      filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS,
        IndexedPreservationEvent.PreservationMetadataEventClass.REPOSITORY.toString()));
    }

    JsonListHelper jsonListHelper = new JsonListHelper(filePath);
    try (IterableIndexResult<? extends IsIndexed> result = index.findAll(indexedClass, filter, true,
      Collections.singletonList(RodaConstants.INDEX_UUID))) {
      count = (int) result.getTotalCount();
      jsonListHelper.init();
      for (IsIndexed indexed : result) {
        jsonListHelper.writeString(indexed.getId());
      }
      jsonListHelper.close();
    } catch (IOException | GenericException | RequestNotValidException e) {
      throw new IOException("Error while creating validation list", e);
    }

    return count;
  }

}
