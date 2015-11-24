/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.SimpleDescriptionObject;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbPanel;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.SelectionChangeEvent;

public class SelectableAIPList extends FlowPanel implements HasValueChangeHandlers<SimpleDescriptionObject> {

  private static final Filter ROOT_FILTER = new Filter(new EmptyKeyFilterParameter(RodaConstants.AIP_PARENT_ID));

  private SimpleDescriptionObject selected;

  private Label selectedLabel;

  private BreadcrumbPanel breadcrumbPanel;
  private List<BreadcrumbItem> breadcrumbs;

  private AIPList aipList;

  private Filter filter;
  private Filter innerFilter;

  public SelectableAIPList() {
    super();

    selected = null;
    filter = null;
    innerFilter = ROOT_FILTER;

    aipList = new AIPList();

    aipList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        final SimpleDescriptionObject sdo = aipList.getSelectionModel().getSelectedObject();
        if (sdo != null) {
          final int index = breadcrumbs.size();
          BreadcrumbItem breadcrumbItem = new BreadcrumbItem(sdo.getTitle(), new Command() {

            @Override
            public void execute() {
              select(sdo);
              removeAfter(index);
            }

          });
          breadcrumbs.add(breadcrumbItem);
          breadcrumbPanel.updatePath(breadcrumbs);
          select(sdo);
        }
      }
    });

    breadcrumbPanel = new BreadcrumbPanel();
    breadcrumbs = new ArrayList<BreadcrumbItem>();
    breadcrumbs.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-circle-o'></i>"), new Command() {

      @Override
      public void execute() {
        selectRoot();
        removeAfter(0);
      }
    }));

    selectedLabel = new Label("Selected:");

    add(selectedLabel);
    add(breadcrumbPanel);

    add(aipList);

    breadcrumbPanel.updatePath(breadcrumbs);

    selectedLabel.addStyleName("selectableAipList-selectedLabel");
    breadcrumbPanel.addStyleName("selectableAipList-breadcrumb");

    updateVisibles();

  }

  protected void removeAfter(int index) {
    int size = breadcrumbs.size();
    if (index < size) {
      for (int i = size - 1; i > index; i--) {
        breadcrumbs.remove(i);
      }
    }
    breadcrumbPanel.updatePath(breadcrumbs);
  }

  private void select(SimpleDescriptionObject sdo) {
    if (sdo != null) {
      selected = sdo;
      Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, sdo.getId()));
      setInnerFilter(filter);
      ValueChangeEvent.fire(this, sdo);
      updateVisibles();
    }
  }

  private void selectRoot() {
    setInnerFilter(ROOT_FILTER);
    selected = null;
    ValueChangeEvent.fire(this, null);
    updateVisibles();
  }

  private void updateVisibles() {
    selectedLabel.setVisible(selected != null);
    breadcrumbPanel.setVisible(selected != null);
  }

  public SimpleDescriptionObject getSelected() {
    return selected;
  }

  public Filter getFilter() {
    return filter;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
    refresh();
  }

  public Filter getInnerFilter() {
    return innerFilter;
  }

  public void setInnerFilter(Filter innerFilter) {
    this.innerFilter = innerFilter;
    refresh();
  }

  private void refresh() {
    Filter composedFilter = new Filter(innerFilter);
    if (filter != null) {
      composedFilter.add(filter.getParameters());
    }
    aipList.setFilter(composedFilter);
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<SimpleDescriptionObject> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

}
