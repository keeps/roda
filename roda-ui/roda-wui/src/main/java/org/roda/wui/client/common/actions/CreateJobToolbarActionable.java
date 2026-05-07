package org.roda.wui.client.common.actions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import config.i18n.client.ClientMessages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.JobUtils;
import org.roda.wui.client.process.JobCreationDataProvider;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.widgets.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CreateJobToolbarActionable extends AbstractActionable<IndexedJob> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final JobCreationDataProvider dataProvider;

  private CreateJobToolbarActionable(JobCreationDataProvider dataProvider) {
    this.dataProvider = dataProvider;
  }

  public static CreateJobToolbarActionable get(JobCreationDataProvider dataProvider) {
    return new CreateJobToolbarActionable(dataProvider);
  }

  @Override
  public CreateJobAction[] getActions() {
    return CreateJobAction.values();
  }

  @Override
  public CanActResult userCanAct(Action<IndexedJob> action, ActionableObject<IndexedJob> object) {
    return new CanActResult(true, CanActResult.Reason.CONTEXT, messages.reasonNoObjectSelected());
  }

  @Override
  public CanActResult contextCanAct(Action<IndexedJob> action) {
    return new CanActResult(true, CanActResult.Reason.CONTEXT, messages.reasonNoObjectSelected());
  }

  @Override
  public CanActResult userCanAct(Action<IndexedJob> action, IndexedJob object) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }



  @Override
  public Action<IndexedJob> actionForName(String name) {
    return CreateJobAction.valueOf(name);
  }

  @Override
  public void act(Action<IndexedJob> action, AsyncCallback<ActionImpact> callback) {
    if (CreateJobAction.OBTAIN_COMMAND.equals(action)) {
      SelectedItems<? extends IsRODAObject> selected = dataProvider.getSelectedItems();
      if (Void.class.getName().equals(dataProvider.getSelectedItems().getSelectedClass())) {
          selected = new SelectedItemsNone<>();
      }

      Services services = new Services("Obtain cURL command", "get");
      Job job = JobUtils.createJob(dataProvider.getJobName(), dataProvider.getSelectedPriority(), dataProvider.getSelectedParallelism(),
        selected, dataProvider.getSelectedPlugin().getId(), dataProvider.getPluginParameters());

      services.jobsResource(s -> s.obtainJobCommand(job)).whenComplete((result, throwable) -> {
        if (throwable != null) {
          Toast.showError(messages.dialogFailure(), throwable.getCause().getMessage());
        } else {
          Dialogs.showInformationDialog(messages.createJobCurlCommand(), result.getValue(), messages.closeButton(),
            true);
        }
        callback.onSuccess(ActionImpact.NONE);
      });
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public ActionableBundle<IndexedJob> createActionsBundle() {
      ActionableBundle<IndexedJob> actionableBundle = new ActionableBundle<>();

      // MANAGEMENT
      ActionableGroup<IndexedJob> managementGroup = new ActionableGroup<>(messages.sidebarActionsTitle());
      managementGroup.addButton(messages.createJobObtainCommandTitle(), CreateJobAction.OBTAIN_COMMAND, ActionImpact.NONE,
              "btn-command");

      actionableBundle.addGroup(managementGroup);

      return actionableBundle;
  }

  public enum CreateJobAction implements Action<IndexedJob> {
    OBTAIN_COMMAND(RodaConstants.PERMISSION_METHOD_CREATE_JOB);

    private List<String> methods;

    CreateJobAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }
}
