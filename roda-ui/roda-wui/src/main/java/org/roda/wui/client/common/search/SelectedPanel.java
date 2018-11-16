package org.roda.wui.client.common.search;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.common.client.tools.ConfigurationManager;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SelectedPanel<T extends IsIndexed> extends SimplePanel implements HasValueChangeHandlers<Boolean> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static String getIconForList(String listId, String classSimpleName) {
    // get icon for list
    String searchSelectedPanelIcon = ConfigurationManager.getString(RodaConstants.UI_LISTS_PROPERTY, listId,
      RodaConstants.UI_LISTS_SEARCH_SELECTEDINFO_ICON_PROPERTY);

    if (searchSelectedPanelIcon == null) {
      // fallback: get icon for class
      searchSelectedPanelIcon = ConfigurationManager.getString(RodaConstants.UI_ICONS_CLASS, classSimpleName);

      if (searchSelectedPanelIcon == null) {
        // second fallback: use a default icon
        searchSelectedPanelIcon = "fa fa-question-circle";
      }
    }

    return searchSelectedPanelIcon;
  }

  SelectedPanel(AsyncTableCell<T> list) {

    // get default text to be shown when nothing is selected
    String defaultLabelText = ConfigurationManager.resolveTranslation(RodaConstants.UI_LISTS_PROPERTY, list.getListId(),
      RodaConstants.UI_LISTS_SEARCH_SELECTEDINFO_LABEL_DEFAULT_I18N_PROPERTY);
    if (defaultLabelText == null) {
      defaultLabelText = messages.someOfAObject(list.getClassToReturn().getName());
    }

    // get "items" part for "1 item selected"
    String selectedSingleItemTextTmp = ConfigurationManager.resolveTranslation(RodaConstants.UI_LISTS_PROPERTY,
      list.getListId(), RodaConstants.UI_LISTS_SEARCH_SELECTEDINFO_LABEL_SELECTED_I18N_SINGLE_PROPERTY);
    if (selectedSingleItemTextTmp == null) {
      selectedSingleItemTextTmp = messages.oneOfAObject(list.getClassToReturn().getName());
    }
    String selectedSingleItemText = selectedSingleItemTextTmp;

    // get "items" part for "N items selected"
    String selectedMultipleItemTextTmp = ConfigurationManager.resolveTranslation(RodaConstants.UI_LISTS_PROPERTY,
      list.getListId(), RodaConstants.UI_LISTS_SEARCH_SELECTEDINFO_LABEL_SELECTED_I18N_MULTIPLE_PROPERTY);
    if (selectedMultipleItemTextTmp == null) {
      selectedMultipleItemTextTmp = messages.someOfAObject(list.getClassToReturn().getName());
    }
    final String selectedMultipleItemText = selectedMultipleItemTextTmp;

    // build widgets
    FlowPanel innerPanel = new FlowPanel();
    addStyleName("selected-count-panel");
    add(innerPanel);

    InlineHTML iconPanel = new InlineHTML();
    iconPanel.setHTML(SafeHtmlUtils.fromSafeConstant(
      "<i class=\"" + getIconForList(list.getListId(), list.getClassToReturn().getSimpleName()) + "\"></i>"));
    innerPanel.add(iconPanel);

    Label selectedLabel = new Label(defaultLabelText);
    selectedLabel.addStyleName("inline selected-count-panel-default");
    innerPanel.add(selectedLabel);

    list.addCheckboxSelectionListener(selected -> {
      int count = 0;
      if (selected instanceof SelectedItemsList) {
        count = ((SelectedItemsList) selected).getIds().size();
      } else if (selected instanceof SelectedItemsFilter && list.getResult() != null) {
        long longCount = list.getResult().getTotalCount();
        count = longCount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) longCount;
      }

      if (count == 0) {
        selectedLabel.setText(selectedMultipleItemText);
        selectedLabel.addStyleName("selected-count-panel-default");
      } else {
        selectedLabel.removeStyleName("selected-count-panel-default");
        selectedLabel.setText(messages.selected(count, count == 1 ? selectedSingleItemText : selectedMultipleItemText));
      }

      ValueChangeEvent.fire(SelectedPanel.this, count > 0);
    });
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }
}
