package config.i18n.client;

import com.google.gwt.i18n.client.Messages;

public interface AgentMessages extends Messages {
  @DefaultMessage("Agent creation failed {0}")
  String createAgentFailure(String message);

  @DefaultMessage("Agent not found {0}")
  String editAgentNotFound(String name);

  @DefaultMessage("Agent edit failed {0}")
  String editAgentFailure(String message);

  @DefaultMessage("Confirm remove agent")
  String agentRemoveFolderConfirmDialogTitle();

  @DefaultMessage("Are you sure you want to remove the the selected {0} agent(s)?")
  String agentRemoveSelectedConfirmDialogMessage(Long size);

  @DefaultMessage("No")
  String agentRemoveFolderConfirmDialogCancel();

  @DefaultMessage("Yes")
  String agentRemoveFolderConfirmDialogOk();

  @DefaultMessage("Removed agent(s)")
  String agentRemoveSuccessTitle();

  @DefaultMessage("Successfully removed {0} agent(s)")
  String agentRemoveSuccessMessage(Long size);

  @DefaultMessage("Search agent...")
  String agentRegisterSearchPlaceHolder();
}
