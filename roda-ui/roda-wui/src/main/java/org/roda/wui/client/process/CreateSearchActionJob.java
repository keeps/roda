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
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.FormatList;
import org.roda.wui.client.common.lists.RepresentationList;
import org.roda.wui.client.common.lists.RiskIncidenceList;
import org.roda.wui.client.common.lists.RiskList;
import org.roda.wui.client.common.lists.SimpleFileList;
import org.roda.wui.client.common.lists.utils.BasicAsyncTableCell;
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
      Filter filter = Filter.ALL;
      BasicAsyncTableCell list = null;

      if (selected instanceof SelectedItemsList) {
        List<String> ids = ((SelectedItemsList<?>) selected).getIds();
        filter = new Filter(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, ids));

      } else if (selected instanceof SelectedItemsFilter) {
        filter = ((SelectedItemsFilter<?>) getSelected()).getFilter();
      }

      if (IndexedAIP.class.getName().equals(selected.getSelectedClass())) {
        list = new AIPList(filter, justActive, null, messages.aipsTitle(), selectable, 10, 10);
      } else if (IndexedRepresentation.class.getName().equals(selected.getSelectedClass())) {
        list = new RepresentationList(filter, justActive, null, messages.representationsTitle(), selectable, 10, 10);
      } else if (IndexedFile.class.getName().equals(selected.getSelectedClass())) {
        list = new SimpleFileList(filter, justActive, null, messages.filesTitle(), selectable, 10, 10);
      } else if (IndexedRisk.class.getName().equals(selected.getSelectedClass())) {
        list = new RiskList(filter, Facets.NONE, messages.showRiskTitle(), selectable, 10, 10);
      } else if (RiskIncidence.class.getName().equals(selected.getSelectedClass())) {
        list = new RiskIncidenceList(filter, Facets.NONE, messages.showRiskIncidenceTitle(), selectable, 10, 10);
      } else if (Format.class.getName().equals(selected.getSelectedClass())) {
        list = new FormatList(filter, Facets.NONE, messages.showFormatTitle(), selectable, 10, 10);
      }

      // TODO 20160930 add new classes

      if (list != null) {
        getTargetPanel().add(list);
      }

      setJobSelectedDescription(messages.createJobSelectObject());
      return false;
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
