package org.roda.core.plugins.plugins.internal.synchronization.packages;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.*;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DipPackagePlugin extends RodaEntityPackagesPlugin<DIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AipPackagePlugin.class);

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getName() {
    return "DipPackagePlugin";
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new DipPackagePlugin();
  }

  @Override
  protected String getEntity() {
    return "dip";
  }

  @Override
  protected Class<DIP> getEntityClass() {
    return DIP.class;
  }

  @Override
  protected List<String> retrieveList(IndexService index) throws RequestNotValidException, GenericException {
    ArrayList<String> dipList = new ArrayList<>();
    Filter filter = new Filter();
    if (fromDate != null) {
      filter.add(new DateIntervalFilterParameter(RodaConstants.DIP_LAST_MODIFIED, RodaConstants.DIP_LAST_MODIFIED,
        fromDate, toDate));
    }
    IterableIndexResult<IndexedDIP> indexedDIPS = index.findAll(IndexedDIP.class, filter,
      Collections.singletonList(RodaConstants.INDEX_UUID));
    for (IndexedDIP dip : indexedDIPS) {
      dipList.add(dip.getId());
    }
    return dipList;
  }

  @Override
  protected void createPackage(IndexService index, ModelService model, List<String> list) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, NotFoundException, AlreadyExistsException, IOException {
    for (String dipId : list) {
      DIP dip = model.retrieveDIP(dipId);
      createDIPBundle(model, dip);
    }
  }

  public void createDIPBundle(ModelService model, DIP dip) throws RequestNotValidException, NotFoundException,
    AuthorizationDeniedException, GenericException, AlreadyExistsException {

    StorageService storage = model.getStorage();
    StoragePath dipStoragePath = ModelUtils.getDIPStoragePath(dip.getId());
    Path destinationPath = bundlePath.resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_DIP).resolve(dip.getId());

    Path dipFilePath = destinationPath.resolve(RodaConstants.STORAGE_DIP_METADATA_FILENAME);

    storage.copy(storage, dipStoragePath, dipFilePath, RodaConstants.STORAGE_DIP_METADATA_FILENAME);

  }
}
