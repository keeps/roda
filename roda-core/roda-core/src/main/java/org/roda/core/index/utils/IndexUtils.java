/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.utils;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.index.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(IndexUtils.class);

  private IndexUtils() {
    // do nothing
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

  public static <T extends IsIndexed> Class<T> giveRespectiveIndexedClassFromModelClass(Class<?> modelClass) {
    if (AIP.class.equals(modelClass)) {
      return (Class<T>) IndexedAIP.class;
    } else if (DIP.class.equals(modelClass)) {
      return (Class<T>) IndexedDIP.class;
    } else {
      return (Class<T>) modelClass;
    }
  }

  public static String giveNameFromLocalInstanceIdentifier(String instanceId) {
    String name = null;

    if (instanceId != null
      && RodaCoreFactory.getDistributedModeType().equals(RodaConstants.DistributedModeType.CENTRAL)) {
      try {
        final DistributedInstance distributedInstance = RodaCoreFactory.getModelService()
          .retrieveDistributedInstance(instanceId);
        name = distributedInstance.getName();
      } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
        name = instanceId;
      }
    }
    return name;
  }
}
