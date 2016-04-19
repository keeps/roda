/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package config.i18n.client;

import java.util.Date;

import com.google.gwt.i18n.client.Messages;

public interface RiskMessages extends Messages {
  @DefaultMessage("Risk creation failed {0}")
  String createRiskFailure(String message);

  @DefaultMessage("Risk not found {0}")
  String editRiskNotFound(String name);

  @DefaultMessage("Risk edit failed {0}")
  String editRiskFailure(String message);

  @DefaultMessage("Confirm remove risk")
  String riskRemoveFolderConfirmDialogTitle();

  @DefaultMessage("Are you sure you want to remove the the selected {0} risk(s)?")
  String riskRemoveSelectedConfirmDialogMessage(Long size);

  @DefaultMessage("No")
  String riskRemoveFolderConfirmDialogCancel();

  @DefaultMessage("Yes")
  String riskRemoveFolderConfirmDialogOk();

  @DefaultMessage("Removed risk(s)")
  String riskRemoveSuccessTitle();

  @DefaultMessage("Successfully removed {0} risk(s)")
  String riskRemoveSuccessMessage(Long size);

  @DefaultMessage("Search risks...")
  String riskRegisterSearchPlaceHolder();

  @DefaultMessage("Risk was modified")
  String modifyRiskMessage();

  @DefaultMessage("{0} at {1,localdatetime,predef:DATE_TIME_MEDIUM}")
  String riskHistoryLabel(String versionKey, Date createdDate);

  @DefaultMessage("Low")
  String showLowSeverity();

  @DefaultMessage("Moderate")
  String showModerateSeverity();

  @DefaultMessage("High")
  String showHighSeverity();
}
