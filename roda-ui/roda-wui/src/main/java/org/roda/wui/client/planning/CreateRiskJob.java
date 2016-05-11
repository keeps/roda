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
package org.roda.wui.client.planning;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsFilter;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.risks.Risk;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.CreateJob;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.RepresentationList;
import org.roda.wui.client.common.lists.SimpleFileList;
import org.roda.wui.client.search.Search;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class CreateRiskJob extends CreateJob<Risk> {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 0) {
        CreateRiskJob createRiskJob = new CreateRiskJob();
        callback.onSuccess(createRiskJob);
      } else {
        Tools.newHistory(CreateRiskJob.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {RiskRegister.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(RiskRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "create_risk_job";
    }
  };

  @SuppressWarnings("unused")
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static PluginType[] pluginTypes = {PluginType.RISK};

  public CreateRiskJob() {
    super(Risk.class, Arrays.asList(pluginTypes));
  }

  @Override
  public void updateObjectList() {

    SelectedItems selected = getSelected();
    boolean selectable = false;
    boolean showInactive = false;

    if (selected != null) {
      if (selected instanceof SelectedItemsList) {
        List<String> ids = ((SelectedItemsList) selected).getIds();

        if (IndexedAIP.class.getName().equals(selected.getSelectedClass())) {
          Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.AIP_ID, ids));

          AIPList list = new AIPList(filter, null, "AIPs", selectable, showInactive, 10, 10);
          getTargetPanel().clear();
          getTargetPanel().add(list);
        }

        if (IndexedRepresentation.class.getName().equals(selected.getSelectedClass())) {
          Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.REPRESENTATION_UUID, ids));
          RepresentationList list = new RepresentationList(filter, null, "Representations", selectable, showInactive,
            10, 10);
          getTargetPanel().clear();
          getTargetPanel().add(list);
        }

        if (IndexedFile.class.getName().equals(selected.getSelectedClass())) {
          Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.FILE_UUID, ids));
          SimpleFileList list = new SimpleFileList(filter, null, "Files", selectable, showInactive, 10, 10);
          getTargetPanel().clear();
          getTargetPanel().add(list);
        }

      } else if (getSelected() instanceof SelectedItemsFilter) {
        Filter filter = ((SelectedItemsFilter) getSelected()).getFilter();

        if (IndexedAIP.class.getName().equals(selected.getSelectedClass())) {
          AIPList list = new AIPList(filter, null, "AIPs", selectable, showInactive, 10, 10);
          getTargetPanel().clear();
          getTargetPanel().add(list);
        }

        if (IndexedRepresentation.class.getName().equals(selected.getSelectedClass())) {
          RepresentationList list = new RepresentationList(filter, null, "Representations", selectable, showInactive,
            10, 10);
          getTargetPanel().clear();
          getTargetPanel().add(list);
        }

        if (IndexedFile.class.getName().equals(selected.getSelectedClass())) {
          SimpleFileList list = new SimpleFileList(filter, null, "Files", selectable, showInactive, 10, 10);
          getTargetPanel().clear();
          getTargetPanel().add(list);
        }

      } else {
        // do nothing
      }
    }

  }

  @Override
  public void buttonCreateHandler(ClickEvent e) {
    getButtonCreate().setEnabled(false);
    String jobName = getName().getText();

    BrowserService.Util.getInstance().createProcess(jobName, getSelected(), getSelectedPlugin().getId(),
      getWorkflowOptions().getValue(), new AsyncCallback<Job>() {

        @Override
        public void onFailure(Throwable caught) {
          Toast.showError("Error", caught.getMessage());
          getButtonCreate().setEnabled(true);
        }

        @Override
        public void onSuccess(Job result) {
          Toast.showInfo("Done", "New job process created");
          Tools.newHistory(Search.RESOLVER);
        }
      });

  }

  @Override
  public void cancel() {
    Tools.newHistory(Search.RESOLVER);
  }

}
