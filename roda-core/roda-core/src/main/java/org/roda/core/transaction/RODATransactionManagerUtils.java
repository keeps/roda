/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class RODATransactionManagerUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(RODATransactionManagerUtils.class);

  public static List<Report> getReportsForTransaction(Plugin<IsRODAObject> plugin, UUID transactionId,
    ModelService model) throws RODATransactionException {
    try {
      Job job = model.retrieveJob(PluginHelper.getJobId(plugin));
      List<Report> reports = new ArrayList<>();
      try (CloseableIterable<OptionalWithCause<Report>> reportList = model.listJobReports(job.getId())) {
        for (OptionalWithCause<Report> optionalReport : reportList) {
          if (optionalReport.isPresent()) {
            Report innerReport = optionalReport.get();
            if (innerReport.getTransactionId().equals(transactionId.toString())) {
              reports.add(innerReport);
            }
          }
        }
      }
      return reports;
    } catch (NotFoundException | IOException | RequestNotValidException | GenericException
      | AuthorizationDeniedException e) {
      throw new RODATransactionException("Error retrieving reports for transaction ID: " + transactionId, e);
    }
  }

  public static void createTransactionFailureReports(List<Report> failedReports, List<Report> nonFailedReports,
    UUID transactionId, Date initDate, ModelService model)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {

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
    ModelService model)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {

    String details = "Transaction was committed successfully.";
    for (Report report : relatedReports) {
      createTransactionReportItem(report, transactionId, PluginState.SUCCESS, initDate, details, model);
    }
  }

  public static void createTransactionReportItem(Report innerReport, UUID transactionId, PluginState state,
    Date initDate, String details, ModelService model)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {

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
  }

  public static boolean shouldRollback(Plugin<IsRODAObject> plugin, List<Report> failedReports) {
    String noRollback = plugin.getParameterValues()
      .getOrDefault(RodaConstants.PLUGIN_PARAM_SKIP_ROLLBACK_ON_VALIDATION_FAILURE, "");

    Set<String> noRollbackPlugins = Arrays.stream(noRollback.split(",")).map(String::trim).filter(s -> !s.isEmpty())
      .collect(Collectors.toSet());

    return failedReports.stream().flatMap(fr -> fr.getReports() == null ? Stream.empty() : fr.getReports().stream())
      .filter(nr -> PluginState.FAILURE.equals(nr.getPluginState())).map(Report::getPlugin)
      .filter(java.util.Objects::nonNull).anyMatch(pluginName -> !noRollbackPlugins.contains(pluginName));
  }

  public static List<Report> getFailedReports(List<Report> reports) {
    return reports.stream().filter(report -> PluginState.FAILURE.equals(report.getPluginState())).toList();
  }

  public static List<Report> getNonFailedReports(List<Report> reports) {
    return reports.stream().filter(report -> !PluginState.FAILURE.equals(report.getPluginState())).toList();
  }
}
