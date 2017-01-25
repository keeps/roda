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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.lists.TransferredResourceList;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class CreateIngestJob extends CreateSelectedJob<TransferredResource> {

  @SuppressWarnings("unused")
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
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
        Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, ids));
        TransferredResourceList list = new TransferredResourceList(filter, null, messages.transferredResourcesTitle(),
          false, 10, 10);
        getTargetPanel().clear();
        getTargetPanel().add(list);
        isEmpty = ids.isEmpty();
      } else if (selected instanceof SelectedItemsFilter) {
        Filter filter = ((SelectedItemsFilter) selected).getFilter();
        TransferredResourceList list = new TransferredResourceList(filter, null, messages.transferredResourcesTitle(),
          false, 10, 10);
        getTargetPanel().clear();
        getTargetPanel().add(list);
      } else {
        isEmpty = true;
      }

      setJobSelectedDescription(messages.createJobSelectedSIP());
    } else {
      isEmpty = true;
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
            getButtonCreate().setEnabled(true);
            AsyncCallbackUtils.defaultFailureTreatment(caught);
          }

          @Override
          public void onSuccess(Job result) {
            Toast.showInfo(messages.dialogDone(), messages.processCreated());
            HistoryUtils.newHistory(IngestProcess.RESOLVER);
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
            getButtonCreate().setEnabled(true);
            AsyncCallbackUtils.defaultFailureTreatment(caught);
          }

          @Override
          public void onSuccess(Void result) {
            getButtonCreate().setEnabled(true);
          }
        });
    }

  }

  @Override
  public void cancel() {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    if (selectedItems.getLastHistory().isEmpty()) {
      HistoryUtils.newHistory(IngestProcess.RESOLVER);
    } else {
      HistoryUtils.newHistory(selectedItems.getLastHistory());
    }
  }
}
