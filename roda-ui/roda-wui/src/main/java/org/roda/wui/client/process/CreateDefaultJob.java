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
package org.roda.wui.client.process;

import java.util.List;
import java.util.Map;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import org.roda.core.data.utils.SelectedItemsUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.CreateJobRequest;
import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobPriority;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginInfoList;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.wui.client.common.CreateJobActionsToolbar;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.PluginUtils;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;
import org.roda.wui.common.client.widgets.Toast;

/**
 * @author Luis Faria
 *
 */
public class CreateDefaultJob extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private final CreateJobInformation jobInformationWidget;
  private final CreateJobPluginSelection pluginSelectionWidget;
  private final CreateJobOrchestration orchestrationWidget;

  @UiField
  FocusPanel keyboardFocus;
  @UiField
  NavigationToolbar<IndexedJob> navigationToolbar;
  @UiField
  TitlePanel title;
  @UiField
  CreateJobActionsToolbar actionsToolbar;
  @UiField
  FlowPanel content;

  public CreateDefaultJob(PluginInfoList pluginInfoList) {
    initWidget(uiBinder.createAndBindUi(this));

    navigationToolbar.withoutButtons().build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getCreateJobBreadcrumbs());

    title.setIconClass("IndexedJob");
    title.setText(messages.createJobTitle());
    title.addStyleName("mb-16");

    actionsToolbar.setDataProvider(new JobCreationDataProvider() {
      @Override
      public String getJobName() {
        return jobInformationWidget.getJobName();
      }

      @Override
      public Map<String, String> getPluginParameters() {
        return pluginSelectionWidget.getPluginParameters();
      }

      @Override
      public SelectedItems<? extends IsRODAObject> getSelectedItems() {
        return jobInformationWidget.getSelectedItems();
      }

      @Override
      public PluginInfo getSelectedPlugin() {
        return pluginSelectionWidget.getSelectedPlugin();
      }

      @Override
      public JobPriority getSelectedPriority() {
        return orchestrationWidget.getSelectedPriority();
      }

      @Override
      public JobParallelism getSelectedParallelism() {
        return orchestrationWidget.getSelectedParallelism();
      }
    });

    actionsToolbar.setObjectAndBuild(null, null, null);
    actionsToolbar.setLabel(messages.processTitle());

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");

    jobInformationWidget = new CreateJobInformation();
    pluginSelectionWidget = new CreateJobPluginSelection(pluginInfoList);
    orchestrationWidget = new CreateJobOrchestration();

    pluginSelectionWidget.setOnPluginSelected(pluginInfo -> {
      if (pluginInfo != null) {
        jobInformationWidget.setPluginName(pluginInfo.getName(), pluginInfo.getObjectClasses());
      }
    });

    PluginInfo initialPlugin = pluginSelectionWidget.getSelectedPlugin();
    if (initialPlugin != null) {
      jobInformationWidget.setPluginName(initialPlugin.getName(), initialPlugin.getObjectClasses());
    }

    orchestrationWidget.addStyleName("mb-16");

    content.add(jobInformationWidget);
    content.add(orchestrationWidget);
    content.add(pluginSelectionWidget);

    pluginSelectionWidget.setOnStartJobCallback(() -> {
      String jobName = jobInformationWidget.getJobName();
      SelectedItems<? extends IsRODAObject> selectedItems = jobInformationWidget.getSelectedItems();

      CreateJobRequest jobRequest = new CreateJobRequest();
      jobRequest.setName(jobName);
      jobRequest.setPlugin(pluginSelectionWidget.getSelectedPlugin().getId());
      jobRequest.setPluginParameters(pluginSelectionWidget.getPluginParameters());
      SelectedItemsRequest selectedItemsRequest = SelectedItemsUtils.convertToRESTRequest(selectedItems);
      jobRequest.setSourceObjects(selectedItemsRequest);
      jobRequest.setPriority(orchestrationWidget.getSelectedPriority().name());
      jobRequest.setParallelism(orchestrationWidget.getSelectedParallelism().name());
      jobRequest.setSourceObjectsClass(jobInformationWidget.getSelectedItems().getSelectedClass());

      Services services = new Services("Create job", "create");
      services.jobsResource(s -> s.createJob(jobRequest)).whenComplete((job1, throwable) -> {
        if (throwable != null) {
          Toast.showError(messages.dialogFailure(), throwable.getMessage());
        } else {
          Toast.showInfo(messages.dialogDone(), messages.processCreated());
          HistoryUtils.newHistory(ActionProcess.RESOLVER);
        }
      });
    });

    pluginSelectionWidget.setOnCancelJobCallback(() -> HistoryUtils.newHistory(ActionProcess.RESOLVER));
  }

  public interface MyUiBinder extends UiBinder<Widget, CreateDefaultJob> {
  }

  public static final HistoryResolver RESOLVER = new HistoryResolver() {
    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.isEmpty()) {
        Services services = new Services("Retrieve plugin information", "get");
        List<PluginType> pluginTypes = PluginUtils.getPluginTypesWithoutIngestAndInternal();

        services
          .configurationsResource(
            s -> s.retrievePluginsInfo(pluginTypes, true, LocaleInfo.getCurrentLocale().getLocaleName()))
          .whenComplete((pluginInfoList, throwable) -> {
            if (throwable == null) {
              CreateDefaultJob createDefaultJob = new CreateDefaultJob(pluginInfoList);
              callback.onSuccess(createDefaultJob);
            }
          });
      } else {
        HistoryUtils.newHistory(CreateDefaultJob.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {Process.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Process.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "create_job";
    }
  };
}
