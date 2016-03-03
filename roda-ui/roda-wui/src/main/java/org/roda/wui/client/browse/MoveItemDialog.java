package org.roda.wui.client.browse;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.facet.SimpleFacetParameter;
import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.FilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.AIP;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.BrowseMessages;

public class MoveItemDialog extends DialogBox {
  private static final Binder binder = GWT.create(Binder.class);

  interface Binder extends UiBinder<Widget, MoveItemDialog> {
  }

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  @UiField
  TextBox searchInputBox;

  @UiField
  AccessibleFocusPanel searchInputButton;

  @UiField
  Button cancel;

  @UiField
  Button moveItem;

  @UiField(provided = true)
  AIPList searchResultsPanel;

  String aipId;
  AsyncCallback<Boolean> callback;

  private static final Filter DEFAULT_FILTER_AIP = new Filter(
    new BasicSearchFilterParameter(RodaConstants.AIP_SEARCH, "*"));

  public MoveItemDialog(String aipId) {
    this.aipId = aipId;

    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.AIP_LEVEL),
      new SimpleFacetParameter(RodaConstants.AIP_HAS_REPRESENTATIONS));
    searchResultsPanel = new AIPList(DEFAULT_FILTER_AIP, facets, messages.moveItemSearchResults(), false);

    setWidget(binder.createAndBindUi(this));

    searchInputBox.getElement().setPropertyString("placeholder", messages.moveItemSearchPlaceHolder());

    setAutoHideEnabled(false);
    setModal(true);
    setGlassEnabled(true);
    setAnimationEnabled(false);

    setText(messages.moveItemTitle());

    center();

    moveItem.setEnabled(false);

    searchInputBox.addValueChangeHandler(new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        doSearch();
      }
    });

    searchInputButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        doSearch();
      }
    });

    searchResultsPanel.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        if (searchResultsPanel.getSelectionModel().getSelectedObject() != null) {
          moveItem.setEnabled(true);
        } else {
          moveItem.setEnabled(false);
        }
      }
    });
  }

  void show(AsyncCallback<Boolean> callback) {
    this.callback = callback;

    if (Window.getClientWidth() < 800) {
      this.setWidth(Window.getClientWidth() + "px");
    }

    show();
    center();
  }

  @UiHandler("cancel")
  void buttonCancelHandler(ClickEvent e) {
    callback.onSuccess(false);
  }

  @UiHandler("moveItem")
  void buttonMoveItemHandler(ClickEvent e) {
    final String parentId = searchResultsPanel.getSelectionModel().getSelectedObject().getId();
    BrowserService.Util.getInstance().moveInHierarchy(aipId, parentId, new AsyncCallback<AIP>() {

      @Override
      public void onSuccess(AIP result) {
        callback.onSuccess(true);
      }

      @Override
      public void onFailure(Throwable caught) {
        callback.onFailure(caught);
      }
    });
  }

  public void doSearch() {
    List<FilterParameter> parameters = new ArrayList<FilterParameter>();

    String basicQuery = searchInputBox.getText();
    if (basicQuery != null && basicQuery.trim().length() > 0) {
      parameters.add(new BasicSearchFilterParameter(RodaConstants.AIP_SEARCH, basicQuery));
    }

    Filter filter;
    if (parameters.size() == 0) {
      filter = DEFAULT_FILTER_AIP;
    } else {
      filter = new Filter(parameters);
    }

    searchResultsPanel.setFilter(filter);
  }
}
