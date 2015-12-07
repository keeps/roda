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
package org.roda.wui.client.ingest.process;

import java.util.List;

import org.roda.core.data.PluginInfo;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.Job;
import org.roda.core.data.v2.Job.JOB_STATE;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria
 * 
 */
public class ShowJob extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        String jobId = historyTokens.get(0);
        BrowserService.Util.getInstance().retrieveJobBundle(jobId, new AsyncCallback<JobBundle>() {

          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          @Override
          public void onSuccess(JobBundle jobBundle) {
            ShowJob showJob = new ShowJob(jobBundle.getJob(), jobBundle.getPluginsInfo());
            callback.onSuccess(showJob);
          }
        });
      } else {
        Tools.newHistory(IngestProcess.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      // TODO check for show job permission
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {IngestProcess.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(IngestProcess.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "job";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, ShowJob> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  // private ClientLogger logger = new ClientLogger(getClass().getName());

  private final Job job;
  private final List<PluginInfo> pluginsInfo;

  @UiField
  Label name;

  @UiField
  Label creator;

  @UiField
  Label dateStarted;

  @UiField
  Label status;

  @UiField
  Label plugin;

  @UiField
  FlowPanel pluginOptions;

  @UiField
  HTML objectList;

  @UiField
  Button buttonBack;

  public ShowJob(Job job, List<PluginInfo> pluginsInfo) {
    this.job = job;
    this.pluginsInfo = pluginsInfo;

    initWidget(uiBinder.createAndBindUi(this));

    name.setText(job.getName());
    creator.setText(job.getUsername());
    DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL);
    dateStarted.setText(dateTimeFormat.format(job.getStartDate()));
    JOB_STATE state = job.getState();
    if (JOB_STATE.COMPLETED.equals(state) || JOB_STATE.FAILED_DURING_CREATION.equals(state)) {
      // TODO different message for failure?
      status.setText(messages.showJobStatusCompleted(job.getEndDate()));
    } else if (JOB_STATE.CREATED.equals(state)) {
      status.setText(messages.showJobStatusCreated());
    } else if (JOB_STATE.STARTED.equals(state)) {
      status.setText(messages.showJobStatusStarted(job.getCompletionPercentage()));
    } else {
      status.setText(state.toString());
    }

    updateObjectList();

    PluginInfo pluginInfo = lookupPlugin(job.getPlugin());
    plugin.setText(messages.pluginLabel(pluginInfo.getName(), pluginInfo.getVersion()));

    for (PluginParameter parameter : pluginInfo.getParameters()) {

      if (PluginParameterType.BOOLEAN.equals(parameter.getType())) {
        createBooleanLayout(parameter);
      } else if (PluginParameterType.STRING.equals(parameter.getType())) {
        createStringLayout(parameter);
      } else if (PluginParameterType.PLUGIN_SIP_TO_AIP.equals(parameter.getType())) {
        createPluginSipToAipLayout(parameter);
      } else {
        // TODO log a warning
        createStringLayout(parameter);
      }
    }

  }

  private void createBooleanLayout(PluginParameter parameter) {
    CheckBox checkBox = new CheckBox(parameter.getName());
    String value = job.getPluginParameters().get(parameter.getId());
    checkBox.setValue("true".equals(value));
    checkBox.setEnabled(false);

    pluginOptions.add(checkBox);
    addHelp(parameter.getDescription());

    checkBox.addStyleName("form-checkbox");
  }

  private void createStringLayout(PluginParameter parameter) {
    String value = job.getPluginParameters().get(parameter.getId());
    if (value != null && value.length() > 0) {
      Label parameterLabel = new Label(parameter.getName());
      Label parameterValue = new Label(value);
      pluginOptions.add(parameterLabel);
      pluginOptions.add(parameterValue);

      parameterLabel.addStyleName("label");

      addHelp(parameter.getDescription());
    }
  }

  private void createPluginSipToAipLayout(PluginParameter parameter) {
    String value = job.getPluginParameters().get(parameter.getId());
    if (value != null && value.length() > 0) {
      Label pluginLabel = new Label(parameter.getName());
      PluginInfo sipToAipPlugin = lookupPlugin(value);
      // Label pluginValue = new
      // Label(messages.pluginLabel(sipToAipPlugin.getName(),
      // sipToAipPlugin.getVersion()));

      RadioButton pluginValue = new RadioButton(parameter.getId(),
        messages.pluginLabel(sipToAipPlugin.getName(), sipToAipPlugin.getVersion()));
      pluginValue.setValue(true);
      pluginValue.setEnabled(false);

      pluginOptions.add(pluginLabel);
      addHelp(parameter.getDescription());
      pluginOptions.add(pluginValue);
      addHelp(sipToAipPlugin.getDescription());

      pluginLabel.addStyleName("label");
      pluginValue.addStyleName("form-radiobutton");
      

      // TODO show SIP_TO_AIP plugin description
    }
  }

  private void addHelp(String description) {
    if (description != null && description.length() > 0) {
      Label pHelp = new Label(description);

      pluginOptions.add(pHelp);

      pHelp.addStyleName("form-help");
    }
  }

  private void updateObjectList() {
    // TODO show list with SIP status

    SafeHtmlBuilder b = new SafeHtmlBuilder();
    b.append(SafeHtmlUtils.fromSafeConstant("<ul>"));

    for (String objId : job.getObjectIds()) {
      b.append(SafeHtmlUtils.fromSafeConstant("<li>"));
      b.append(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-file-o'></i>"));
      b.append(SafeHtmlUtils.fromString(objId));
      b.append(SafeHtmlUtils.fromSafeConstant("</li>"));
    }

    b.append(SafeHtmlUtils.fromSafeConstant("</ul>"));
    objectList.setHTML(b.toSafeHtml());

  }

  private PluginInfo lookupPlugin(String pluginId) {
    PluginInfo p = null;

    for (PluginInfo pluginInfo : pluginsInfo) {
      if (pluginInfo.getId().equals(pluginId)) {
        p = pluginInfo;
        break;
      }
    }

    return p;
  }
  
  
  @UiHandler("buttonStop")
  void buttonStopHandler(ClickEvent e) {
    Toast.showInfo("Sorry", "Feature not yet implemented");
  }

  @UiHandler("buttonBack")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    Tools.newHistory(IngestProcess.RESOLVER);
  }

}
