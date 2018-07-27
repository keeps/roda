package org.roda.wui.client.common.search;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SelectedPanel extends SimplePanel implements HasValueChangeHandlers<Boolean> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private AsyncTableCell<IsIndexed, ?> boundList = null;
  private AsyncTableCell.CheckboxSelectionListener<IsIndexed> checkboxSelectionListener = null;

  private List<HandlerRegistration> valueChangedHandlers = new ArrayList<>();

  private Label selectedLabel;

  public SelectedPanel() {
    selectedLabel = new Label();
    add(selectedLabel);

    addStyleName("selected-count-panel");
    selectedLabel.addStyleName("selected-count-label");
    selectedLabel.setText("10 selected");
  }

  public void bindList(AsyncTableCell<IsIndexed, ?> list) {
    if (boundList != null) {
      boundList.removeCheckboxSelectionListener(checkboxSelectionListener);
    }
    boundList = list;

    checkboxSelectionListener = selected -> {
      int count = 0;
      if (selected instanceof SelectedItemsList) {
        count = ((SelectedItemsList) selected).getIds().size();
      } else if (selected instanceof SelectedItemsFilter) {
        long longCount = boundList.getResult().getTotalCount();
        count = longCount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) longCount;
      }

      // messages handles count of 0, 1 and N in different ways, but 0 and 1 are only
      // supported for integers
      selectedLabel.setText(messages.selected(count));

      ValueChangeEvent.fire(SelectedPanel.this, count > 0);
    };
    boundList.addCheckboxSelectionListener(checkboxSelectionListener);
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

}
