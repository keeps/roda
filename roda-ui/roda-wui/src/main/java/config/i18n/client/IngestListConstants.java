/**
 * 
 */
package config.i18n.client;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.ConstantsWithLookup;

/**
 * @author Luis Faria
 * 
 */
public interface IngestListConstants extends Constants, ConstantsWithLookup {

  // Ingest List

  // * SIP List header
  @DefaultStringValue("File")
  String headerFilename();

  @DefaultStringValue("Submission date")
  String headerStartDate();

  @DefaultStringValue("Current state")
  String headerState();

  @DefaultStringValue("%")
  String headerPercentage();

  @DefaultStringValue("Producer")
  String headerProducer();

  // * Control Panel
  @DefaultStringValue("Filter")
  String ingestListControlTitle();

  @DefaultStringValue("Producer")
  String ingestListControlSearchTitle();

  // * * State filters
  @DefaultStringValue("All")
  String optionAll();

  @DefaultStringValue("processing")
  String optionAllActive();

  @DefaultStringValue("processed")
  String optionWaitingPublishing();

  @DefaultStringValue("history")
  String optionHistory();

  @DefaultStringValue("rejected")
  String optionQuarantine();

  @DefaultStringValue("accepted")
  String optionAccepted();

  // * * Actions
  @DefaultStringValue("REPORT")
  String ingestReport();

  @DefaultStringValue("VIEW")
  String ingestView();

  @DefaultStringValue("VALIDATE")
  String ingestValid();

  @DefaultStringValue("INVALIDATE")
  String ingestInvalid();

  @DefaultStringValue("ACCEPT")
  String ingestAccept();

  @DefaultStringValue("REJECT")
  String ingestReject();

  @DefaultStringValue("accepting SIP")
  String acceptingSIP();

  @DefaultStringValue("rejecting SIP")
  String rejectingSIP();

  // Ingest Report
  @DefaultStringValue("Ingest report")
  String ingestReportWindowTitle();

  @DefaultStringValue("Original file name:")
  String reportFilenameLabel();

  @DefaultStringValue("Date and time:")
  String reportTransitionDate();

  @DefaultStringValue("Task:")
  String reportTransitionTask();

  @DefaultStringValue("Result:")
  String reportTransitionOutcome();

  @DefaultStringValue("Details:")
  String reportTransitionOutcomeDetails();

  @DefaultStringValue("Success")
  String reportTransitionSuccess();

  @DefaultStringValue("Failure")
  String reportTransitionFailure();

  // SIP panel
  @DefaultStringValue("What is the reason for rejecting the SIP?")
  String sipRejectMessagePrompt();

  @DefaultStringValue("The object is not in the repository")
  String objectNotInRepository();

  // Ingest States
  @DefaultStringValue("received (FTP)")
  String state_DROPED_FTP();

  @DefaultStringValue("received (HTTP)")
  String state_DROPED_UPLOAD_SERVICE();

  @DefaultStringValue("received (local)")
  String state_DROPED_LOCAL();

  @DefaultStringValue("unpacked")
  String state_UNPACKED();

  @DefaultStringValue("virus-free")
  String state_VIRUS_FREE();

  @DefaultStringValue("well formed")
  String state_SIP_VALID();

  @DefaultStringValue("authorized")
  String state_AUTHORIZED();

  @DefaultStringValue("ingested")
  String state_SIP_INGESTED();

  @DefaultStringValue("normalized")
  String state_SIP_NORMALIZED();

  @DefaultStringValue("accepted")
  String state_ACCEPTED();

  @DefaultStringValue("rejected")
  String state_QUARANTINE();

  // Ingest Tasks
  @DefaultStringValue("Upload packet to server")
  String task_SIPUploadTask();

  @DefaultStringValue("Move")
  String task_MoveTaskPlugin();

  @DefaultStringValue("Unpack packet")
  String task_UnpackTaskPlugin();

  @DefaultStringValue("Check virus")
  String task_CheckVirusTaskPlugin();

  @DefaultStringValue("Check syntax")
  String task_CheckSIPCoherenceTaskPlugin();

  @DefaultStringValue("Review permissions")
  String task_CheckProducerAuthorizationTaskPlugin();

  @DefaultStringValue("Temporary incorporation")
  String task_CreateObjectsTaskPlugin();

  @DefaultStringValue("Normalization")
  String task_NormalizationTaskPlugin();

  @DefaultStringValue("Semantic Check")
  String task_AcceptSIPTask();

  // Select Description Object Window
  @DefaultStringValue("Select the element you wish to view")
  String selectDescriptionObjectWindowTitle();

  @DefaultStringValue("close")
  String selectDescriptionObjectWindowClose();

  // Accept Message Window
  @DefaultStringValue("Standard messages")
  String acceptMessageWindowTemplates();

  @DefaultStringValue("accept")
  String acceptMessageWindowAccept();

  @DefaultStringValue("cancel")
  String acceptMessageWindowCancel();

  // Reject Message Window
  @DefaultStringValue("reject")
  String rejectMessageWindowReject();

  @DefaultStringValue("cancel")
  String rejectMessageWindowCancel();

  @DefaultStringValue("Standard messages")
  String rejectMessageWindowTemplates();

  @DefaultStringValue("Notify producer")
  String rejectMessageWindowNotifyProducer();

}
