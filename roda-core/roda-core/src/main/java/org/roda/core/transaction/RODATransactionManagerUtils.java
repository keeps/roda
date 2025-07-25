package org.roda.core.transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.model.ModelService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class RODATransactionManagerUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(RODATransactionManagerUtils.class);

  public static List<Report> getReportsForTransaction(String jobId, UUID transactionId, ModelService model)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException,
    RODATransactionException {
    List<Report> reports = new ArrayList<>();
    try (CloseableIterable<OptionalWithCause<Report>> reportList = model.listJobReports(jobId)) {
      for (OptionalWithCause<Report> optionalReport : reportList) {
        if (optionalReport.isPresent()) {
          Report innerReport = optionalReport.get();
          if (innerReport.getTransactionId().equals(transactionId.toString())) {
            reports.add(innerReport);
          }
        }
      }
    } catch (NotFoundException | IOException e) {
      throw new RODATransactionException("Error retrieving reports for transaction ID: " + transactionId, e);
    }
    return reports;
  }

  public static void createTransactionFailureReports(List<Report> failedReports, List<Report> nonFailedReports,
    UUID transactionId, Date initDate, ModelService model) throws RODATransactionException {

    for (Report report : nonFailedReports) {
      String details = "This transaction failed because a related transaction also failed";
      createTransactionReportItem(report, transactionId, PluginState.FAILURE, initDate, details, model);
    }

    for (Report report : failedReports) {
      String details = "Transaction was rolled back due to a failure in the plugin execution.";
      createTransactionReportItem(report, transactionId, PluginState.FAILURE, initDate, details, model);
    }
  }

  public static void createTransactionSuccessReports(List<Report> relatedReports, UUID transactionId, Date initDate,
    ModelService model) throws RODATransactionException {

    String details = "Transaction was committed successfully.";
    for (Report report : relatedReports) {
      createTransactionReportItem(report, transactionId, PluginState.SUCCESS, initDate, details, model);
    }
  }

  public static void createTransactionReportItem(Report innerReport, UUID transactionId, PluginState state,
    Date initDate, String details, ModelService model) throws RODATransactionException {
    try {
      Job job = model.retrieveJob(innerReport.getJobId());
      innerReport.setTotalSteps(innerReport.getTotalSteps() + 1);

      Report reportItem = new Report();
      reportItem.injectLineSeparator(System.lineSeparator());
      reportItem.setId(IdUtils.getJobReportId(innerReport.getJobId(), innerReport.getSourceObjectId(),
        innerReport.getOutcomeObjectId()));
      reportItem.setJobId(innerReport.getJobId());
      reportItem.setSourceAndOutcomeObjectId(innerReport.getSourceObjectId(), innerReport.getOutcomeObjectId());
      reportItem.setTitle("RODA Transaction Manager");
      reportItem.setPlugin(RODATransactionManager.class.getName());
      reportItem.setPluginName("RODA Transaction Manager");
      reportItem.setPluginDetails(String.format("[Transaction ID: %s] %s", transactionId, details));
      reportItem.setPluginState(state);
      reportItem.setOutcomeObjectState(innerReport.getOutcomeObjectState());
      reportItem.setDateCreated(initDate);
      reportItem.setDateUpdated(new Date());
      reportItem.setHtmlPluginDetails(innerReport.isHtmlPluginDetails());
      innerReport.addReport(reportItem);

      model.createOrUpdateJobReport(innerReport, job);
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      throw new RODATransactionException("Error adding report item for transaction ID: " + transactionId, e);
    }
  }
}
