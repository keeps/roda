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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.lists.utils.BasicAsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListFactory;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.PluginUtils;
import org.roda.wui.client.search.Search;
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
public class CreateActionJob extends CreateSelectedJob<IsIndexed> {

  @SuppressWarnings("unused")
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static List<PluginType> pluginTypes = PluginUtils.getPluginTypesWithoutIngest();

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public CreateActionJob() {
    super(pluginTypes);
  }

  @Override
  public boolean updateObjectList() {
    SelectedItems<?> selected = getSelected();
    boolean selectable = false;

    if (selected != null) {
      getTargetPanel().clear();
      Filter filter = Filter.ALL;

      if (selected instanceof SelectedItemsList) {
        List<String> ids = ((SelectedItemsList<?>) selected).getIds();
        filter = new Filter(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, ids));
      } else if (selected instanceof SelectedItemsFilter) {
        filter = ((SelectedItemsFilter<?>) getSelected()).getFilter();
      }

      ListFactory listFactory = new ListFactory();
      BasicAsyncTableCell<?> list = listFactory.getList(selected.getSelectedClass(),
        messages.allOfAObject(selected.getSelectedClass()), filter, selectable, 10, 10);

      if (list != null) {
        getTargetPanel().add(list);
      } else {
        HistoryUtils.newHistory(CreateDefaultJob.RESOLVER);
      }

      setJobSelectedDescription(messages.createJobSelectObject());
      return false;
    }

    return true;
  }

  @Override
  public void buttonCreateHandler(ClickEvent e) {
    getButtonCreate().setEnabled(false);
    String jobName = getName().getText();

    BrowserService.Util.getInstance().createProcess(jobName, getSelected(), getSelectedPlugin().getId(),
      getWorkflowOptions().getValue(), getSelectedClass(), new AsyncCallback<Job>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
          getButtonCreate().setEnabled(true);
        }

        @Override
        public void onSuccess(Job result) {
          Toast.showInfo(messages.dialogDone(), messages.processCreated());
          HistoryUtils.newHistory(ActionProcess.RESOLVER);
        }
      });

  }

  @Override
  public void cancel() {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    if (selectedItems.getLastHistory().isEmpty()) {
      HistoryUtils.newHistory(Search.RESOLVER);
    } else {
      HistoryUtils.newHistory(selectedItems.getLastHistory());
    }
  }
}
