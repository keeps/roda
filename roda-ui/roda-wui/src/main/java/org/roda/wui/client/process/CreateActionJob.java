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

import java.util.Arrays;
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
import org.roda.wui.client.common.CreateJob;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.RepresentationList;
import org.roda.wui.client.common.lists.SimpleFileList;
import org.roda.wui.client.search.Search;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;

import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria
 * 
 */
public class CreateActionJob extends CreateJob<IsIndexed> {

  @SuppressWarnings("unused")
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static PluginType[] pluginTypes = {PluginType.AIP_TO_AIP, PluginType.MISC};

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  public CreateActionJob() {
    super(IsIndexed.class, Arrays.asList(pluginTypes));
  }

  @Override
  public boolean updateObjectList() {

    SelectedItems<?> selected = getSelected();
    boolean selectable = false;
    boolean justActive = true;
    boolean isEmpty = false;

    if (selected != null) {
      if (selected instanceof SelectedItemsList) {
        List<String> ids = ((SelectedItemsList<?>) selected).getIds();

        if (ids.size() == 0) {
          getTargetPanel().clear();

          Button searchButton = new Button();
          searchButton.addStyleName("btn");
          searchButton.addStyleName("btn-search");
          searchButton.setText("Search");
          searchButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
              Tools.newHistory(Search.RESOLVER);
            }
          });

          getTargetPanel().add(searchButton);
          setJobSelectedDescription(messages.createJobSelectedObject());
          return true;
        }

        if (IndexedAIP.class.getName().equals(selected.getSelectedClass())) {
          Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.AIP_ID, ids));
          AIPList list = new AIPList(filter, justActive, null, "AIPs", selectable, 10, 10);
          getTargetPanel().clear();
          getTargetPanel().add(list);
          setJobSelectedDescription(messages.createJobSelectedAIP());
        }

        if (IndexedRepresentation.class.getName().equals(selected.getSelectedClass())) {
          Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.REPRESENTATION_UUID, ids));
          RepresentationList list = new RepresentationList(filter, justActive, null, "Representations", selectable, 10,
            10);
          getTargetPanel().clear();
          getTargetPanel().add(list);
          setJobSelectedDescription(messages.createJobSelectedRepresentation());
        }

        if (IndexedFile.class.getName().equals(selected.getSelectedClass())) {
          Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.FILE_UUID, ids));
          SimpleFileList list = new SimpleFileList(filter, justActive, null, "Files", selectable, 10, 10);
          getTargetPanel().clear();
          getTargetPanel().add(list);
          setJobSelectedDescription(messages.createJobSelectedFile());
        }

      } else if (getSelected() instanceof SelectedItemsFilter) {
        Filter filter = ((SelectedItemsFilter<?>) getSelected()).getFilter();

        if (IndexedAIP.class.getName().equals(selected.getSelectedClass())) {
          AIPList list = new AIPList(filter, justActive, null, "AIPs", selectable, 10, 10);
          getTargetPanel().clear();
          getTargetPanel().add(list);
          setJobSelectedDescription(messages.createJobSelectedAIP());
        }

        if (IndexedRepresentation.class.getName().equals(selected.getSelectedClass())) {
          RepresentationList list = new RepresentationList(filter, justActive, null, "Representations", selectable, 10,
            10);
          getTargetPanel().clear();
          getTargetPanel().add(list);
          setJobSelectedDescription(messages.createJobSelectedRepresentation());
        }

        if (IndexedFile.class.getName().equals(selected.getSelectedClass())) {
          SimpleFileList list = new SimpleFileList(filter, justActive, null, "Files", selectable, 10, 10);
          getTargetPanel().clear();
          getTargetPanel().add(list);
          setJobSelectedDescription(messages.createJobSelectedFile());
        }

      } else {
        isEmpty = true;
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
          Toast.showError("Error", caught.getMessage());
          getButtonCreate().setEnabled(true);
        }

        @Override
        public void onSuccess(Job result) {
          Toast.showInfo("Done", "New job process created");
          Tools.newHistory(ActionProcess.RESOLVER);
        }
      });

  }

  @Override
  public void cancel() {
    Tools.newHistory(ActionProcess.RESOLVER);
  }

}
