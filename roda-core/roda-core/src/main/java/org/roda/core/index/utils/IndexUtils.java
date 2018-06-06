/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.tools.ZipEntryInfo;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.storage.Binary;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(IndexUtils.class);

  private IndexUtils() {
    // do nothing
  }

  /**
   * @deprecated use DownloadUtils instead.
   */
  @Deprecated
  public static void addToZip(List<ZipEntryInfo> zipEntries, org.roda.core.data.v2.ip.File file, boolean flat)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StorageService storage = RodaCoreFactory.getStorageService();

    if (!file.isDirectory()) {
      StoragePath filePath = ModelUtils.getFileStoragePath(file);
      Binary binary = storage.getBinary(filePath);
      ZipEntryInfo info = new ZipEntryInfo(flat ? filePath.getName() : FSUtils.getStoragePathAsString(filePath, true),
        binary.getContent());
      zipEntries.add(info);
    } else {
      // do nothing
    }
  }

  /**
   * @deprecated use DownloadUtils instead.
   */
  @Deprecated
  public static void addToZip(List<ZipEntryInfo> zipEntries, Binary binary) {
    String path = FSUtils.getStoragePathAsString(binary.getStoragePath(), true);
    ZipEntryInfo info = new ZipEntryInfo(path, binary.getContent());
    zipEntries.add(info);
  }

  /**
   * @deprecated use DownloadUtils instead.
   */
  @Deprecated
  public static List<ZipEntryInfo> zipIndexedAIP(List<IndexedAIP> aips)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<ZipEntryInfo> zipEntries = new ArrayList<>();
    ModelService model = RodaCoreFactory.getModelService();

    for (IndexedAIP aip : aips) {
      AIP fullAIP = model.retrieveAIP(aip.getId());
      zipEntries.addAll(aipToZipEntry(fullAIP));
    }

    return zipEntries;
  }

  /**
   * @deprecated use DownloadUtils instead.
   */
  @Deprecated
  public static List<ZipEntryInfo> aipToZipEntry(AIP aip)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<ZipEntryInfo> zipEntries = new ArrayList<>();
    StorageService storage = RodaCoreFactory.getStorageService();
    ModelService model = RodaCoreFactory.getModelService();

    StoragePath aipJsonPath = DefaultStoragePath.parse(ModelUtils.getAIPStoragePath(aip.getId()),
      RodaConstants.STORAGE_AIP_METADATA_FILENAME);
    addToZip(zipEntries, storage.getBinary(aipJsonPath));
    for (DescriptiveMetadata dm : aip.getDescriptiveMetadata()) {
      Binary dmBinary = model.retrieveDescriptiveMetadataBinary(aip.getId(), dm.getId());
      addToZip(zipEntries, dmBinary);
    }

    try (CloseableIterable<OptionalWithCause<PreservationMetadata>> preservations = model
      .listPreservationMetadata(aip.getId(), true)) {
      for (OptionalWithCause<org.roda.core.data.v2.ip.metadata.PreservationMetadata> preservation : preservations) {
        if (preservation.isPresent()) {
          PreservationMetadata pm = preservation.get();
          StoragePath filePath = ModelUtils.getPreservationMetadataStoragePath(pm);
          addToZip(zipEntries, storage.getBinary(filePath));
        } else {
          LOGGER.error("Cannot get AIP representation file", preservation.getCause());
        }
      }
    } catch (IOException e) {
      throw new GenericException(e);
    }

    for (Representation rep : aip.getRepresentations()) {
      try (CloseableIterable<OptionalWithCause<org.roda.core.data.v2.ip.File>> allFiles = model
        .listFilesUnder(aip.getId(), rep.getId(), true)) {
        for (OptionalWithCause<org.roda.core.data.v2.ip.File> file : allFiles) {
          if (file.isPresent()) {
            addToZip(zipEntries, file.get(), false);
          } else {
            LOGGER.error("Cannot get AIP representation file", file.getCause());
          }
        }
      } catch (IOException e) {
        throw new GenericException(e);
      }

      try (
        CloseableIterable<OptionalWithCause<org.roda.core.data.v2.ip.metadata.OtherMetadata>> allOtherMetadata = model
          .listOtherMetadata(aip.getId(), rep.getId(), null, null, null)) {
        for (OptionalWithCause<org.roda.core.data.v2.ip.metadata.OtherMetadata> otherMetadata : allOtherMetadata) {
          if (otherMetadata.isPresent()) {
            OtherMetadata o = otherMetadata.get();
            StoragePath otherMetadataStoragePath = ModelUtils.getOtherMetadataStoragePath(aip.getId(), rep.getId(),
              o.getFileDirectoryPath(), o.getFileId(), o.getFileSuffix(), o.getType());
            addToZip(zipEntries, storage.getBinary(otherMetadataStoragePath));
          } else {
            LOGGER.error("Cannot get Representation other metadata file", otherMetadata.getCause());
          }
        }
      } catch (IOException e) {
        throw new GenericException(e);
      }
    }

    return zipEntries;
  }

  public static List<ZipEntryInfo> zipAIP(List<AIP> aips, SimpleJobPluginInfo jobPluginInfo)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<ZipEntryInfo> zipEntries = new ArrayList<>();
    for (AIP aip : aips) {
      zipEntries.addAll(aipToZipEntry(aip));
      jobPluginInfo.incrementObjectsProcessedWithSuccess();
    }
    return zipEntries;
  }

  public static List<IndexedAIP> getIndexedAIPsFromObjectIds(SelectedItems<IndexedAIP> selectedItems)
    throws GenericException, RequestNotValidException {
    IndexService index = RodaCoreFactory.getIndexService();
    List<IndexedAIP> res = new ArrayList<>();

    if (selectedItems instanceof SelectedItemsList) {
      SelectedItemsList<IndexedAIP> list = (SelectedItemsList<IndexedAIP>) selectedItems;
      for (String objectId : list.getIds()) {
        try {
          res.add(index.retrieve(IndexedAIP.class, objectId, new ArrayList<>()));
        } catch (GenericException | NotFoundException e) {
          LOGGER.error("Error retrieving AIP", e);
        }
      }
    } else if (selectedItems instanceof SelectedItemsFilter) {
      SelectedItemsFilter<IndexedAIP> selectedItemsFilter = (SelectedItemsFilter<IndexedAIP>) selectedItems;
      long count = index.count(IndexedAIP.class, selectedItemsFilter.getFilter());
      for (int i = 0; i < count; i += RodaConstants.DEFAULT_PAGINATION_VALUE) {
        List<IndexedAIP> aips = index.find(IndexedAIP.class, selectedItemsFilter.getFilter(), null,
          new Sublist(i, RodaConstants.DEFAULT_PAGINATION_VALUE), null).getResults();
        res.addAll(aips);
      }
    }

    return res;
  }

  public static List<IndexedDIP> getIndexedDIPsFromObjectIds(SelectedItems<IndexedDIP> selectedItems)
    throws GenericException, RequestNotValidException {
    IndexService index = RodaCoreFactory.getIndexService();
    List<IndexedDIP> res = new ArrayList<>();

    if (selectedItems instanceof SelectedItemsList) {
      SelectedItemsList<IndexedDIP> list = (SelectedItemsList<IndexedDIP>) selectedItems;
      for (String objectId : list.getIds()) {
        try {
          res.add(index.retrieve(IndexedDIP.class, objectId, new ArrayList<>()));
        } catch (GenericException | NotFoundException e) {
          LOGGER.error("Error retrieving DIP", e);
        }
      }
    } else if (selectedItems instanceof SelectedItemsFilter) {
      SelectedItemsFilter<IndexedDIP> selectedItemsFilter = (SelectedItemsFilter<IndexedDIP>) selectedItems;
      long count = index.count(IndexedDIP.class, selectedItemsFilter.getFilter());
      for (int i = 0; i < count; i += RodaConstants.DEFAULT_PAGINATION_VALUE) {
        List<IndexedDIP> dips = index.find(IndexedDIP.class, selectedItemsFilter.getFilter(), null,
          new Sublist(i, RodaConstants.DEFAULT_PAGINATION_VALUE), null).getResults();
        res.addAll(dips);
      }
    }

    return res;
  }

  public static <T extends IsIndexed> Class<T> giveRespectiveIndexClass(Class<? extends IsRODAObject> inputClass) {
    if (AIP.class.equals(inputClass)) {
      return (Class<T>) IndexedAIP.class;
    } else if (Representation.class.equals(inputClass)) {
      return (Class<T>) IndexedRepresentation.class;
    } else if (File.class.equals(inputClass)) {
      return (Class<T>) IndexedFile.class;
    } else if (Risk.class.equals(inputClass)) {
      return (Class<T>) IndexedRisk.class;
    } else if (DIP.class.equals(inputClass)) {
      return (Class<T>) IndexedDIP.class;
    } else if (Report.class.equals(inputClass)) {
      return (Class<T>) IndexedReport.class;
    } else {
      return (Class<T>) inputClass;
    }
  }
}
