/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.client.widgets;

import java.util.List;
import java.util.Vector;

import pt.gov.dgarq.roda.wui.common.client.images.CommonImageBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class ControlPanel {

  /**
   * Interface to handle search events
   */
  public interface SearchListener {
    /**
     * Handle a request search
     * 
     * @param keywords
     *          the search keywords
     */
    public void onSearch(String keywords);
  }

  /**
   * Interface to handle control panel events
   * 
   */
  public interface ControlPanelListener extends SearchListener {
    /**
     * Handle a open being selected
     * 
     * @param option
     *          the selected option
     */
    public void onOptionSelected(int option);

    /**
     * Handle a request search
     * 
     * @param keywords
     *          the search keywords
     */
    public void onSearch(String keywords);
  }

  /**
   * Internal search panel
   */
  public class SearchPanel extends SimplePanel {

    private CommonImageBundle commonImageBundle = (CommonImageBundle) GWT.create(CommonImageBundle.class);

    private final HorizontalPanel layout;

    private final TextBox searchBox;

    private final Image searchGo;

    private final Image searchClear;

    private boolean clearOn;

    private List<SearchListener> listeners;

    /**
     * Search panel constructor
     * 
     */
    public SearchPanel() {
      layout = new HorizontalPanel();
      searchBox = new TextBox();
      searchGo = commonImageBundle.forwardLight().createImage();
      searchClear = commonImageBundle.crossLight().createImage();
      clearOn = false;
      listeners = new Vector<SearchListener>();

      layout.add(searchBox);
      layout.add(searchGo);

      this.setWidget(layout);

      searchBox.addKeyboardListener(new KeyboardListener() {

        public void onKeyDown(Widget sender, char keyCode, int modifiers) {
        }

        public void onKeyPress(Widget sender, char keyCode, int modifiers) {
        }

        public void onKeyUp(Widget sender, char keyCode, int modifiers) {
          if (keyCode == KEY_ENTER) {
            onSearch();
          } else {
            if (searchBox.getText().length() > 0) {
              setClear(false);
            }
          }
        }

      });

      searchGo.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          onSearch();
        }

      });

      searchClear.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          searchBox.setText("");
          onSearch();
        }

      });

      layout.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);

      this.addStyleName("wui-search-compact");
      layout.addStyleName("search-layout");
      searchBox.addStyleName("search-box");
      searchGo.addStyleName("search-button");
      searchClear.addStyleName("search-button");
    }

    private void setClear(boolean clear) {
      if (clearOn != clear) {
        if (clearOn) {
          layout.remove(searchClear);
          layout.add(searchGo);
        } else {
          layout.remove(searchGo);
          layout.add(searchClear);
        }
        clearOn = clear;
      }
    }

    private void onSearch() {
      String searchString = searchBox.getText();
      for (SearchListener listener : listeners) {
        listener.onSearch(searchString);
      }
      setClear(searchString.length() > 0);

    }

    /**
     * Add a new search listener
     * 
     * @param listener
     */
    public void addSearchListener(SearchListener listener) {
      listeners.add(listener);
    }

    /**
     * Remove a search listener
     * 
     * @param listener
     */
    public void removeSearchListener(SearchListener listener) {
      listeners.remove(listener);

    }
  }

  private final VerticalPanel layout;

  private final VerticalPanel whitebox;

  private final Label title;

  private int selectedOptionIndex;

  private final VerticalPanel optionsPanel;

  private final Label searchTitle;

  private final SearchPanel searchPanel;

  private final VerticalPanel actionsPanel;

  private final List<ControlPanelListener> listeners;

  private boolean optionsEnabled;

  /**
   * Create a control panel setting title and search title
   * 
   * @param title
   *          the control panel title
   * @param searchTitle
   *          the search panel title
   */
  public ControlPanel(String title, String searchTitle) {
    this();
    setTitle(title);
    setSearchTitle(searchTitle);
  }

  /**
   * Create a new control panel. Don't forget to set control panel title and
   * search panel title.
   * 
   */
  public ControlPanel() {
    layout = new VerticalPanel();
    whitebox = new VerticalPanel();
    title = new Label();
    selectedOptionIndex = -1;
    optionsEnabled = true;
    optionsPanel = new VerticalPanel();
    searchTitle = new Label();
    searchPanel = new SearchPanel();
    actionsPanel = new VerticalPanel();

    listeners = new Vector<ControlPanelListener>();

    whitebox.add(title);
    whitebox.add(optionsPanel);
    whitebox.add(searchTitle);
    whitebox.add(searchPanel);

    layout.add(whitebox);
    layout.add(actionsPanel);

    layout.addStyleName("wui-controlPanel");
    whitebox.addStyleName("controlPanel-whitebox");
    title.addStyleName("controlPanel-title");
    optionsPanel.addStyleName("controlPanel-options");
    searchTitle.addStyleName("controlPanel-search-title");
    searchPanel.addStyleName("controlPanel-search");
    actionsPanel.addStyleName("controlPanel-actions");
  }

  /**
   * Get the control panel title
   * 
   * @return the title
   */
  public String getTitle() {
    return title.getText();
  }

  /**
   * Set the control panel title
   * 
   * @param title
   */
  public void setTitle(String title) {
    this.title.setText(title);
  }

  /**
   * Get the search panel title
   * 
   * @return the title
   */
  public String getSearchTitle() {
    return searchTitle.getText();
  }

  /**
   * Set the search panel title
   * 
   * @param searchTitle
   */
  public void setSearchTitle(String searchTitle) {
    this.searchTitle.setText(searchTitle);
  }

  /**
   * Add an option to the control panel
   * 
   * @param optionText
   */
  public void addOption(String optionText) {
    final Label optionLabel = new Label(optionText);
    optionsPanel.add(optionLabel);
    optionLabel.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        if (optionsEnabled) {
          setSelectedOptionIndex(optionsPanel.getWidgetIndex(optionLabel));
        }
      }

    });
    optionLabel.setStylePrimaryName("option");
  }

  /**
   * Get the selected option index
   * 
   * @return the index of the option, by the order they were inserted
   */
  public int getSelectedOptionIndex() {
    return selectedOptionIndex;
  }

  /**
   * Programatically set the selected option
   * 
   * @param selected
   *          the option index
   */
  public void setSelectedOptionIndex(int selected) {
    if (selectedOptionIndex != selected) {
      selectedOptionIndex = selected;
      for (int i = 0; i < optionsPanel.getWidgetCount(); i++) {
        Label option = (Label) optionsPanel.getWidget(i);
        if (selected == i) {
          option.addStyleDependentName("selected");
        } else {
          option.removeStyleDependentName("selected");
        }
      }
      onOptionSelected(selectedOptionIndex);
    }
  }

  private void onOptionSelected(int option) {
    for (ControlPanelListener listener : listeners) {
      listener.onOptionSelected(option);
    }
  }

  /**
   * Add a control panel listener
   * 
   * @param listener
   */
  public void addControlPanelListener(ControlPanelListener listener) {
    listeners.add(listener);
    searchPanel.addSearchListener(listener);
  }

  /**
   * Remove a control panel listener
   * 
   * @param listener
   */
  public void removeControlPanelListener(ControlPanelListener listener) {
    listeners.remove(listener);
    searchPanel.removeSearchListener(listener);
  }

  /**
   * Add an action button to the control panel
   * 
   * @param actionButton
   */
  public void addActionButton(WUIButton actionButton) {
    actionsPanel.add(actionButton);
    actionButton.addStyleName("controlPanel-action");
  }

  /**
   * Remove an action button from the control panel
   * 
   * @param actionButton
   */
  public void removeActionButton(WUIButton actionButton) {
    actionsPanel.remove(actionButton);
    actionButton.removeStyleName("controlPanel-action");
  }

  /**
   * Add a widget to the actions part of the control panel
   * 
   * @param widget
   */
  public void addActionWidget(Widget widget) {
    actionsPanel.add(widget);
  }

  /**
   * Remove a widget to the actions part of the control panel
   * 
   * @param widget
   */
  public void removeActionWidget(Widget widget) {
    actionsPanel.remove(widget);
  }

  /**
   * Get the control panel container widget
   * 
   * @return the widget that represents the control panel
   */
  public Widget getWidget() {
    return layout;
  }

  /**
   * Clear the control panel. All options and action will be removed.
   * 
   */
  public void clear() {
    optionsPanel.clear();
    actionsPanel.clear();
    selectedOptionIndex = -1;
  }

  /**
   * Can options be selected by user
   * 
   * @return true if the user is allowed to change the selected option, false
   *         otherwise
   */
  public boolean areOptionsEnabled() {
    return optionsEnabled;
  }

  /**
   * Set if user is allowed to change the selected option
   * 
   * @param optionsEnabled
   */
  public void setOptionsEnabled(boolean optionsEnabled) {
    this.optionsEnabled = optionsEnabled;
  }

}
