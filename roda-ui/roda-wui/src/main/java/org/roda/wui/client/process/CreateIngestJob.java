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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsFilter;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.CreateJob;
import org.roda.wui.client.common.Dialogs;
import org.roda.wui.client.common.lists.TransferredResourceList;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria
 * 
 */
public class CreateIngestJob extends CreateJob<TransferredResource> {

  @SuppressWarnings("unused")
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);
  private static PluginType[] pluginTypes = {PluginType.INGEST};

  public CreateIngestJob() {
    super(TransferredResource.class, Arrays.asList(pluginTypes));
    super.setCategoryListBoxVisible(false);
  }

  @Override
  public boolean updateObjectList() {

    SelectedItems selected = getSelected();
    boolean isEmpty = false;

    if (selected != null) {
      if (selected instanceof SelectedItemsList) {
        List<String> ids = ((SelectedItemsList) selected).getIds();
        Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_UUID, ids));
        TransferredResourceList list = new TransferredResourceList(filter, null, "Transferred resources", false, 10,
          10);
        getTargetPanel().clear();
        getTargetPanel().add(list);
      } else if (selected instanceof SelectedItemsFilter) {
        Filter filter = ((SelectedItemsFilter) selected).getFilter();
        TransferredResourceList list = new TransferredResourceList(filter, null, "Transferred resources", false, 10,
          10);
        getTargetPanel().clear();
        getTargetPanel().add(list);
      } else {
        isEmpty = true;
      }

      setJobSelectedDescription(messages.createJobSelectedSIP());
    }

    return isEmpty;
  }

  @Override
  public void buttonCreateHandler(ClickEvent e) {
    getButtonCreate().setEnabled(false);
    String jobName = getName().getText();

    List<PluginParameter> missingMandatoryParameters = getWorkflowOptions().getMissingMandatoryParameters();
    if (missingMandatoryParameters.isEmpty()) {

      BrowserService.Util.getInstance().createProcess(jobName, getSelected(), getSelectedPlugin().getId(),
        getWorkflowOptions().getValue(), null, new AsyncCallback<Job>() {

          @Override
          public void onFailure(Throwable caught) {
            Toast.showError("Error", caught.getMessage());
            getButtonCreate().setEnabled(true);
          }

          @Override
          public void onSuccess(Job result) {
            Toast.showInfo("Done", "New ingest process created");
            Tools.newHistory(IngestProcess.RESOLVER);
          }
        });
    } else {

      List<String> missingPluginNames = new ArrayList<>();
      for (PluginParameter parameter : missingMandatoryParameters) {
        missingPluginNames.add(parameter.getName());
      }

      Dialogs.showInformationDialog(messages.processNewMissingMandatoryInfoDialogTitle(),
        messages.processNewMissingMandatoryInfoDialogMessage(missingPluginNames), messages.dialogOk(),
        new AsyncCallback<Void>() {

          @Override
          public void onFailure(Throwable caught) {
            Toast.showError(caught.getMessage());
            getButtonCreate().setEnabled(true);
          }

          @Override
          public void onSuccess(Void result) {
            // do nothing
            getButtonCreate().setEnabled(true);
          }
        });
    }

  }

  @Override
  public void cancel() {
    Tools.newHistory(IngestProcess.RESOLVER);
  }

}
