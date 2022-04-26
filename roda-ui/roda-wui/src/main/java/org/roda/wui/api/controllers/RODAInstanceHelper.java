package org.roda.wui.api.controllers;

import java.util.Date;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Shahzod Yusupov <syusupov@keep.pt>
 */
public class RODAInstanceHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(RODAInstanceHelper.class);

  public static Long synchronizeIfUpdated(User user) throws GenericException, NotFoundException {

    IndexService index = RodaCoreFactory.getIndexService();
    Long total = 0L;

    LocalInstance localInstance = RodaCoreFactory.getLocalInstance();
    if (localInstance != null) {
      Date fromDate = localInstance.getLastSynchronizationDate();
      Date toDate = new Date();

      // check if updates in AIPs
      total += retrieveNumberOfUpdated(IndexedAIP.class, RodaConstants.AIP_UPDATED_ON, RodaConstants.AIP_UPDATED_ON,
        index, fromDate, toDate);

      // check if updates in Jobs
      total += retrieveNumberOfUpdated(Job.class, RodaConstants.JOB_START_DATE, RodaConstants.JOB_END_DATE, index,
        fromDate, toDate);
      // check if updates in Dips
      total += retrieveNumberOfUpdated(IndexedDIP.class, RodaConstants.DIP_LAST_MODIFIED,
        RodaConstants.DIP_LAST_MODIFIED, index, fromDate, toDate);
      // check if updates in RiskIncidences
      total += retrieveNumberOfUpdated(RiskIncidence.class, RodaConstants.RISK_INCIDENCE_UPDATED_ON,
        RodaConstants.RISK_INCIDENCE_UPDATED_ON, index, fromDate, toDate);
      // check if updates in RepositoryEvent
      total += retrieveNumberOfUpdated(IndexedPreservationEvent.class, RodaConstants.PRESERVATION_EVENT_DATETIME,
        RodaConstants.PRESERVATION_EVENT_DATETIME, index, fromDate, toDate);
      // TODO check if updates in PreservationAgents
      /*
       * total += retrieveNumberOfUpdated(IndexedPreservationAgent.class,
       * RodaConstants.PRESERVATION_EVENT_DATETIME,
       * RodaConstants.PRESERVATION_EVENT_DATETIME, index, fromDate, toDate);
       */
    }else{
      LOGGER.warn("Could not find local instance");
      throw new NotFoundException("Could not find local instance");
    }
    return total;
  }

  public static <T extends IsIndexed> Long retrieveNumberOfUpdated(Class<T> returnClass, String startDate,
    String endDate, IndexService indexService, Date fromDate, Date toDate) throws GenericException {

    Long updatedItems = 0L;
    Filter filter = new Filter();

    if (returnClass.equals(Job.class)) {
      filter.add(new NotSimpleFilterParameter(RodaConstants.JOB_PLUGIN_TYPE, PluginType.INTERNAL.toString()));
      filter.add(new NotSimpleFilterParameter(RodaConstants.JOB_STATE, Job.JOB_STATE.CREATED.name()));
      filter.add(new NotSimpleFilterParameter(RodaConstants.JOB_STATE, Job.JOB_STATE.PENDING_APPROVAL.name()));
    } else if (returnClass.equals(IndexedAIP.class)) {
      Filter pfilter = new Filter();
      pfilter.add(new DateIntervalFilterParameter(RodaConstants.PRESERVATION_EVENT_DATETIME,
        RodaConstants.PRESERVATION_EVENT_DATETIME, fromDate, toDate));
      try {
        updatedItems += indexService.count(IndexedPreservationEvent.class, pfilter);
      } catch (GenericException | RequestNotValidException e) {
        LOGGER.warn("Internal error searching for updates in " + IndexedPreservationEvent.class , e);
        throw new GenericException("Internal error searching for updates in " + IndexedPreservationEvent.class);
      }
    }
    filter.add(new DateIntervalFilterParameter(startDate, endDate, fromDate, toDate));
    try {
      updatedItems += indexService.count(returnClass, filter);
    } catch (RequestNotValidException | GenericException e) {
        LOGGER.warn("Internal error searching for updates in " + returnClass , e);
        throw new GenericException("Internal error searching for updates in " + returnClass);
    }
    return updatedItems;
  }
}
