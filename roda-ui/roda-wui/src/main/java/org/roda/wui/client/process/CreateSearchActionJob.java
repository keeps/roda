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

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsFilter;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.RepresentationList;
import org.roda.wui.client.common.lists.SimpleFileList;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.PluginUtils;
import org.roda.wui.client.search.Search;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class CreateSearchActionJob extends CreateJob<IsIndexed> {

  @SuppressWarnings("unused")
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static List<PluginType> pluginTypes = PluginUtils.getPluginTypesWithoutIngest();

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public CreateSearchActionJob() {
    super(IsIndexed.class, pluginTypes);
  }

  public CreateSearchActionJob(SelectedItems items) {
    super(IsIndexed.class, pluginTypes, items);
  }

  @Override
  public boolean updateObjectList() {

    SelectedItems<?> selected = getSelected();
    boolean selectable = false;
    boolean justActive = true;
    boolean isEmpty = true;

    if (selected != null) {
      getTargetPanel().clear();

      if (selected instanceof SelectedItemsList) {
        List<String> ids = ((SelectedItemsList<?>) selected).getIds();

        if (IndexedAIP.class.getName().equals(selected.getSelectedClass())) {
          Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, ids));
          AIPList list = new AIPList(filter, justActive, null, messages.aipsTitle(), selectable, 10, 10);
          getTargetPanel().add(list);
          setJobSelectedDescription(messages.createJobSelectedAIP());
          return false;
        } else if (IndexedRepresentation.class.getName().equals(selected.getSelectedClass())) {
          Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, ids));
          RepresentationList list = new RepresentationList(filter, justActive, null, messages.representationsTitle(),
            selectable, 10, 10);
          getTargetPanel().add(list);
          setJobSelectedDescription(messages.createJobSelectedRepresentation());
          return false;
        } else if (IndexedFile.class.getName().equals(selected.getSelectedClass())) {
          Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, ids));
          SimpleFileList list = new SimpleFileList(filter, justActive, null, messages.filesTitle(), selectable, 10, 10);
          getTargetPanel().add(list);
          setJobSelectedDescription(messages.createJobSelectedFile());
          return false;
        }

      } else if (selected instanceof SelectedItemsFilter) {
        Filter filter = ((SelectedItemsFilter<?>) getSelected()).getFilter();

        if (IndexedAIP.class.getName().equals(selected.getSelectedClass())) {
          AIPList list = new AIPList(filter, justActive, null, messages.aipsTitle(), selectable, 10, 10);
          getTargetPanel().add(list);
          setJobSelectedDescription(messages.createJobSelectedAIP());
          return false;
        } else if (IndexedRepresentation.class.getName().equals(selected.getSelectedClass())) {
          RepresentationList list = new RepresentationList(filter, justActive, null, messages.representationsTitle(),
            selectable, 10, 10);
          getTargetPanel().add(list);
          setJobSelectedDescription(messages.createJobSelectedRepresentation());
          return false;
        } else if (IndexedFile.class.getName().equals(selected.getSelectedClass())) {
          SimpleFileList list = new SimpleFileList(filter, justActive, null, messages.filesTitle(), selectable, 10, 10);
          getTargetPanel().add(list);
          setJobSelectedDescription(messages.createJobSelectedFile());
          return false;
        }

      }
    }

    return isEmpty;
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
          Tools.newHistory(ActionProcess.RESOLVER);
        }
      });

  }

  @Override
  public void cancel() {
    Tools.newHistory(Search.RESOLVER);
  }

}
