/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.synchronization.packages;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DipPackagePlugin extends RodaEntityPackagesPlugin<DIP> {
  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  public static String getStaticName() {
    return "DipPackagePlugin";
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
  protected List<IterableIndexResult> retrieveList(IndexService index)
    throws RequestNotValidException, GenericException {

    Filter filter = new Filter();
    if (fromDate != null) {
      filter.add(new DateIntervalFilterParameter(RodaConstants.DIP_LAST_MODIFIED, RodaConstants.DIP_LAST_MODIFIED,
        fromDate, toDate));
    }
    return Arrays.asList(index.findAll(IndexedDIP.class, filter, Collections.singletonList(RodaConstants.INDEX_UUID)));
  }

  @Override
  protected void createPackage(IndexService index, ModelService model, IterableIndexResult objectList)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    AlreadyExistsException {
    for (Object object : objectList) {
      if (object instanceof IndexedDIP) {
        DIP dip = model.retrieveDIP(((IndexedDIP) object).getId());
        createDIPBundle(model, dip);
      }
    }
  }

  private void createDIPBundle(ModelService model, DIP dip)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException {
    Path destinationPath = workingDirPath.resolve(RodaConstants.CORE_STORAGE_FOLDER)
      .resolve(RodaConstants.STORAGE_CONTAINER_DIP).resolve(dip.getId());

    Path dipFilePath = destinationPath.resolve(RodaConstants.STORAGE_DIP_METADATA_FILENAME);

    model.copyObjectFromContainer(dip, RodaConstants.STORAGE_DIP_METADATA_FILENAME, dipFilePath);
  }
}
