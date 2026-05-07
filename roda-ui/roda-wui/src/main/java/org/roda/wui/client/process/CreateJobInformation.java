package org.roda.wui.client.process;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.wui.client.common.lists.TransferredResourceList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.lists.utils.ListFactory;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.common.client.tools.HistoryUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class CreateJobInformation extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  TextBox jobNameTextBox;

  @UiField
  ListBox runOnListBox;

  @UiField
  FlowPanel targetListPanel;

  private SelectedItems<? extends IsRODAObject> selectedItems;

  public CreateJobInformation() {
    initWidget(uiBinder.createAndBindUi(this));
    this.selectedItems = new SelectedItemsAll<>();
  }

  public CreateJobInformation(SelectedItems<? extends IsRODAObject> selectedItems) {
    this.selectedItems = selectedItems;

    initWidget(uiBinder.createAndBindUi(this));

    setTargetList();
  }

  private void setTargetList() {
    if (selectedItems instanceof SelectedItemsAll<?>) {
      targetListPanel.clear();
    } else {
      if (selectedItems instanceof SelectedItemsList || selectedItems instanceof SelectedItemsFilter) {
        Filter filter;
        if (selectedItems instanceof SelectedItemsList) {
          List<String> ids = ((SelectedItemsList<?>) selectedItems).getIds();
          filter = new Filter(new OneOfManyFilterParameter(RodaConstants.INDEX_UUID, ids));
        } else {
          filter = ((SelectedItemsFilter<?>) selectedItems).getFilter();
        }

        SearchWrapper search;
        String option;

        if (TransferredResource.class.getName().equals(selectedItems.getSelectedClass())) {

          ListBuilder<TransferredResource> transferredResourceListBuilder = new ListBuilder<>(
            () -> new TransferredResourceList(),
            new AsyncTableCellOptions<>(TransferredResource.class, "CreateIngestJob_transferredResources")
              .withFilter(filter).withSummary(messages.transferredResourcesTitle()).withInitialPageSize(10)
              .withPageSizeIncrement(10));

          search = new SearchWrapper(false).createListAndSearchPanel(transferredResourceListBuilder, false, false,
            "px-16 pb-16");
          option = messages.createJobSelectedSIP();
        } else {
          ListFactory listFactory = new ListFactory();
          ListBuilder<? extends IsIndexed> listBuilder = listFactory.getListBuilder("CreateActionJob",
            selectedItems.getSelectedClass(), messages.allOfAObject(selectedItems.getSelectedClass()), filter, 10, 10);

          search = new SearchWrapper(false).createListAndSearchPanel(listBuilder, false, false, "px-16 pb-16");
          option = messages.createJobSelectedObjects();
        }

        runOnListBox.clear();
        runOnListBox.addItem(option);
        targetListPanel.clear();
        targetListPanel.add(search);
      }
    }
  }

  public void setPluginName(String pluginName, Set<String> objectClasses) {
    if (jobNameTextBox != null && pluginName != null) {
      jobNameTextBox.setText(pluginName);
    }

    if (selectedItems instanceof SelectedItemsAll) {
      Set<String> rodaClasses = getPluginNames(objectClasses);
      runOnListBox.clear();
      for (String objectClass : rodaClasses) {
        runOnListBox.addItem(messages.allOfAObject(objectClass), objectClass);
      }

      runOnListBox.addChangeHandler(new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {
          targetListPanel.clear();
        }
      });

      targetListPanel.clear();
    }
  }

  public String getJobName() {
    return jobNameTextBox.getText();
  }

  public SelectedItems<? extends IsRODAObject> getSelectedItems() {
    if (selectedItems instanceof SelectedItemsAll<?>) {
      return SelectedItemsAll.create(runOnListBox.getSelectedValue());
    } else {
      return selectedItems;
    }
  }

  private Set<String> getPluginNames(Set<String> objectClasses) {
    Set<String> objectList = new HashSet<>();
    for (String objectClass : objectClasses) {
      if (IndexedAIP.class.getName().equals(objectClass)) {
        objectList.add(AIP.class.getName());
      } else if (IndexedRepresentation.class.getName().equals(objectClass)) {
        objectList.add(Representation.class.getName());
      } else if (IndexedFile.class.getName().equals(objectClass)) {
        objectList.add(File.class.getName());
      } else if (IndexedRisk.class.getName().equals(objectClass)) {
        objectList.add(Risk.class.getName());
      } else if (IndexedDIP.class.getName().equals(objectClass)) {
        objectList.add(DIP.class.getName());
      } else if (IndexedReport.class.getName().equals(objectClass)) {
        objectList.add(Report.class.getName());
      } else {
        objectList.add(objectClass);
      }
    }
    return objectList;
  }

  public interface MyUiBinder extends UiBinder<Widget, CreateJobInformation> {
  }
}
