package config.i18n.client;

import com.google.gwt.i18n.client.Messages;

public interface FormatMessages extends Messages {
  @DefaultMessage("Format creation failed {0}")
  String createFormatFailure(String message);

  @DefaultMessage("Format not found {0}")
  String editFormatNotFound(String name);

  @DefaultMessage("Format edit failed {0}")
  String editFormatFailure(String message);

  @DefaultMessage("Confirm remove format")
  String formatRemoveFolderConfirmDialogTitle();

  @DefaultMessage("Are you sure you want to remove the the selected {0} format(s)?")
  String formatRemoveSelectedConfirmDialogMessage(Long size);

  @DefaultMessage("No")
  String formatRemoveFolderConfirmDialogCancel();

  @DefaultMessage("Yes")
  String formatRemoveFolderConfirmDialogOk();

  @DefaultMessage("Removed format(s)")
  String formatRemoveSuccessTitle();

  @DefaultMessage("Successfully removed {0} format(s)")
  String formatRemoveSuccessMessage(Long size);

  @DefaultMessage("Search formats...")
  String formatRegisterSearchPlaceHolder();
}
