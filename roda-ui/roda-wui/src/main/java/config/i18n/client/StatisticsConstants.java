/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package config.i18n.client;

import com.google.gwt.i18n.client.ConstantsWithLookup;

/**
 * @author Luis Faria
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 * 
 */
public interface StatisticsConstants extends ConstantsWithLookup {

  String timeSegmentationLabel();

  // segmentation
  String segmentation_YEAR();

  String segmentation_MONTH();

  String segmentation_DAY();

  // statistic type classes

  String systemStatistics();

  String repositoryStatistics();

  String userStatistics();

  String userActionsStatistics();

  String eventStatistics();

  String ingestStatistics();

  String accessStatistics();

  String actionsStatistics();

  String producersStatistics();

  String chartActual();

  String chartHistory();

  String chartPieTooltip();

  String noDataAvailable();

  String viewImpossibleBcSipNotIngested();

  String dateIntervalPickerWindowTitle();

  String dateIntervalLabelInitial();

  String dateIntervalLabelFinal();

  String dateIntervalPickerWindowApply();

  // Statistic panel titles

  String descriptiveObjectsCountTitle();

  String descriptiveObjectsCountDesc();

  String fondsCountTitle();

  String fondsCountDesc();

  String presEventCountTitle();

  String presEventCountDesc();

  String presRepCountTitle();

  String presRepCountDesc();

  String representationObjectsCountTitle();

  String representationObjectsCountDesc();

  String representationObjectTypeTitle();

  String representationObjectTypeDesc();

  String representationObjectSubTypeTitle();

  String representationObjectSubTypeDesc();

  String userCountTitle();

  String userCountDesc();

  String groupCountTitle();

  String groupCountDesc();

  String groupTop5Title();

  String groupTop5Desc();

  String logWuiLoginTitle();

  String logWuiLoginDesc();

  String logWuiPageHitsTitle();

  String logWuiPageHitsDesc();

  String logWuiErrorsTitle();

  String logWuiErrorsDesc();

  String logDescriptiveMetadataViewsTitle();

  String logDescriptiveMetadataViewsDesc();

  String logPreservationMetadataViewsTitle();

  String logPreservationMetadataViewsDesc();

  String logBasicSearchTitle();

  String logBasicSearchDesc();

  String logAdvancedSearchTitle();

  String logAdvancedSearchDesc();

  String disseminatorHitsTitle();

  String disseminatorHitsDesc();

  String disseminatorMissTitle();

  String disseminatorMissDesc();

  String logRegisterUserTitle();

  String logRegisterUserDesc();

  String logUserEmailConfirmationTitle();

  String logUserEmailConfirmationDesc();

  String logUserPasswordResetTitle();

  String logUserPasswordResetDesc();

  String taskCountTitle();

  String taskCountDesc();

  String taskStateTitle();

  String taskStateDesc();

  String taskInstanceCountTitle();

  String taskInstanceCountDesc();

  String taskInstanceStateTitle();

  String taskInstanceStateDesc();

  String sipCountTitle();

  String sipCountDesc();

  String sipCompletenessTitle();

  String sipCompletenessDesc();

  String sipStateTitle();

  String sipStateDesc();

  String sipDurationAutoTitle();

  String sipDurationAutoDesc();

  String sipDurationManualTitle();

  String sipDurationManualDesc();

  String sipMinAutomaticProcessingTimeTitle();

  String sipMinAutomaticProcessingTimeDesc();

  String sipMaxAutomaticProcessingTimeTitle();

  String sipMaxAutomaticProcessingTimeDesc();

  String sipMinManualProcessingTimeTitle();

  String sipMinManualProcessingTimeDesc();

  String sipMaxManualProcessingTimeTitle();

  String sipMaxManualProcessingTimeDesc();

  String producerTitle();

  String producerLastSubmissionDate();

  String producerSubmissionStateChartTitle();

  String producerSubmissionStateChartDesc();

  String createDescriptionObjectTitle();

  String createDescriptionObjectDesc();

  String modifyDescriptionObjectTitle();

  String modifyDescriptionObjectDesc();

  String removeDescriptionObjectTitle();

  String removeDescriptionObjectDesc();

  String moveDescriptionObjectTitle();

  String moveDescriptionObjectDesc();

  String addUserTitle();

  String addUserDesc();

  String modifyUserTitle();

  String modifyUserDesc();

  String removeUserTitle();

  String removeUserDesc();

  String addGroupTitle();

  String addGroupDesc();

  String modifyGroupTitle();

  String modifyGroupDesc();

  String removeGroupTitle();

  String removeGroupDesc();

  String setUserPasswordTitle();

  String setUserPasswordDesc();

  String acceptSIPTitle();

  String acceptSIPDesc();

  String addTaskTitle();

  String addTaskDesc();

  String modifyTaskTitle();

  String modifyTaskDesc();

  String removeTaskTitle();

  String removeTaskDesc();

  String systemStatisticsLink();

  // Reports
  String statisticsReportClose();

  String statisticsReportSegmentationLabel();

  String statisticsReportDateSeparatorLabel();

  String statisticsReportChart();

  String statisticsReportList();

  String statisticsReportListTotal();

  String statisticsReportListHeaderDate();

  String statisticsReportListHeaderType();

  String statisticsReportListHeaderValue();

  // Statistic types
  String statistic_type_others();

  String tasks_state_running();

  String tasks_state_suspended();

  String instances_state_running();

  String instances_state_paused();

  String instances_state_stopped();

  String sips_complete_true();

  String sips_complete_false();

  String sips_state_DROPED_FTP();

  String sips_state_DROPED_UPLOAD_SERVICE();

  String sips_state_DROPED_LOCAL();

  String sips_state_UNPACKED();

  String sips_state_VIRUS_FREE();

  String sips_state_SIP_VALID();

  String sips_state_AUTHORIZED();

  String sips_state_SIP_INGESTED();

  String sips_state_SIP_NORMALIZED();

  String sips_state_ACCEPTED();

  String sips_state_QUARANTINE();

  String object_representation_type_digitalized_work();

  String object_representation_type_email();

  String object_representation_type_structured_text();

  String object_representation_type_presentation();

  String object_representation_type_spreadsheet();

  String object_representation_type_vector_graphic();

  String object_representation_type_relational_database();

  String object_representation_type_video();

  String object_representation_type_audio();

  String object_representation_type_unknown();

  String users_state_active();

  String users_state_inactive();

}
