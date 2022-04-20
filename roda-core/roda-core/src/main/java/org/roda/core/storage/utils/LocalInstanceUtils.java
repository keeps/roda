package org.roda.core.storage.utils;

import java.nio.file.Path;
import java.util.Date;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.SyncUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.YamlUtils;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.synchronization.bundle.BundleState;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.synchronization.local.LocalInstanceIdentifierState;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class LocalInstanceUtils {

  private LocalInstanceUtils() {
    // do nothing
  }

  public static String getLocalInstanceIdentifier() {
    try {
      Path configFile = RodaCoreFactory.getConfigPath()
        .resolve(RodaConstants.SYNCHRONIZATION_CONFIG_LOCAL_INSTANCE_FILE_PATH);
      LocalInstance localInstance = YamlUtils.readObjectFromFile(configFile, LocalInstance.class);
      if (localInstance.getInstanceIdentifierState().equals(LocalInstanceIdentifierState.INACTIVE)) {
        return null;
      } else {
        return localInstance.getId();
      }
    } catch (GenericException e) {
      return null;
    }
  }

  public static LocalInstanceIdentifierState getLocalInstanceState() {
    try {
      Path configFile = RodaCoreFactory.getConfigPath()
        .resolve(RodaConstants.SYNCHRONIZATION_CONFIG_LOCAL_INSTANCE_FILE_PATH);
      LocalInstance localInstance = YamlUtils.readObjectFromFile(configFile, LocalInstance.class);
      return localInstance.getInstanceIdentifierState();
    } catch (GenericException e) {
      return null;
    }
  }

  public static String retrieveLocalInstanceIdentifierToPlugin() {
    try {
      Path configFile = RodaCoreFactory.getConfigPath()
        .resolve(RodaConstants.SYNCHRONIZATION_CONFIG_LOCAL_INSTANCE_FILE_PATH);
      LocalInstance localInstance = YamlUtils.readObjectFromFile(configFile, LocalInstance.class);
      return localInstance.getId();
    } catch (GenericException e) {
      return null;
    }
  }

  public static Long synchronizeIfUpdated(String instanceId, User user) {

    IndexService index = RodaCoreFactory.getIndexService();
    Long total = 0L;
    try {
      BundleState bundleState = SyncUtils.getOutcomeBundleState(instanceId);
      Date fromDate = bundleState.getFromDate();
      Date toDate = bundleState.getToDate();

      // check if updates in AIPs
      total += retrieveNumberOfUpdated(IndexedAIP.class, RodaConstants.AIP_UPDATED_ON, RodaConstants.AIP_UPDATED_ON,
        bundleState, index, fromDate, toDate);

      // check if updates in Jobs
      total += retrieveNumberOfUpdated(Job.class, RodaConstants.JOB_START_DATE, RodaConstants.JOB_END_DATE, bundleState,
        index, fromDate, toDate);
      // check if updates in Dips
      total += retrieveNumberOfUpdated(IndexedDIP.class, RodaConstants.DIP_LAST_MODIFIED,
        RodaConstants.DIP_LAST_MODIFIED, bundleState, index, fromDate, toDate);
      // check if updates in RiskIncidences
      total += retrieveNumberOfUpdated(IndexedRisk.class, RodaConstants.RISK_INCIDENCE_UPDATED_ON,
        RodaConstants.RISK_INCIDENCE_UPDATED_ON, bundleState, index, fromDate, toDate);
      // check if updates in RepositoryEvent
      total += retrieveNumberOfUpdated(IndexedReport.class, RodaConstants.PRESERVATION_EVENT_DATETIME,
        RodaConstants.PRESERVATION_EVENT_DATETIME, bundleState, index, fromDate, toDate);
      // check if updates in RepositoryEvent
      total += retrieveNumberOfUpdated(IndexedPreservationEvent.class, RodaConstants.PRESERVATION_EVENT_DATETIME,
        RodaConstants.PRESERVATION_EVENT_DATETIME, bundleState, index, fromDate, toDate);
      // check if updates in PreservationAgents
      total += retrieveNumberOfUpdated(IndexedPreservationAgent.class, RodaConstants.PRESERVATION_EVENT_DATETIME,
        RodaConstants.PRESERVATION_EVENT_DATETIME, bundleState, index, fromDate, toDate);

    } catch (GenericException e) {
      e.printStackTrace();
    }
    return total;
  }

  public static <T extends IsIndexed> Long retrieveNumberOfUpdated(Class<T> returnClass, String startDate,
    String endDate, BundleState bundleState, IndexService indexService, Date fromDate, Date toDate) {
    Filter filter = new Filter();
    filter.add(new DateIntervalFilterParameter(startDate, endDate, fromDate, toDate));
    try {
      Long updatedItems = indexService.count(IndexedAIP.class, filter);
      return updatedItems;
    } catch (RequestNotValidException | GenericException e) {
      e.printStackTrace();
    }
    return null;
  }
}
