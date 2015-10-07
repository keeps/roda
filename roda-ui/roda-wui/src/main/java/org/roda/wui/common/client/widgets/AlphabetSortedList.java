/**
 * 
 */
package org.roda.wui.common.client.widgets;

import java.util.List;
import java.util.Vector;

import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.widgets.LazyScroll.Loader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.CommonConstants;

/**
 * @author Luis Faria
 * 
 */
public abstract class AlphabetSortedList extends DockPanel {

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static CommonConstants constants = (CommonConstants) GWT.create(CommonConstants.class);

  private static int BLOCK_SIZE = 30;

  private static int MAX_SIZE = 900;

  private final List<AlphabetListItem> loadedItems;

  private AlphabetListItem selectedItem;

  private final HorizontalPanel header;

  private final HorizontalPanel alphabetPanel;

  private final LazyScroll scroll;

  private final VerticalPanel widgetListPanel;

  private int itemsCount;

  private int loadedItemsCount;

  private Widget onLetter = null;

  private boolean[] activeLetters;

  private Label allLetter;

  private Label[] letters;

  private Label sizeLabel;

  /**
   * Interface for AlphabetSortedList event handlers
   */
  public interface AlphabetSortedListListener {
    /**
     * Handle an item selection
     * 
     * @param item
     *          the selected item or null if none is selected
     */
    public void onItemSelect(AlphabetListItem item);
  }

  private final List<AlphabetSortedListListener> alphabetSortedListListeners;

  /**
   * Create a new Alphabet sorted list
   * 
   */
  public AlphabetSortedList() {
    this.loadedItems = new Vector<AlphabetListItem>();
    this.itemsCount = 0;
    this.loadedItemsCount = 0;

    this.header = new HorizontalPanel();
    this.alphabetPanel = new HorizontalPanel();

    allLetter = new Label(constants.AlphabetSortedListAll());
    allLetter.addClickListener(new ClickListener() {
      public void onClick(Widget sender) {
        setAllLetters();
      }
    });
    alphabetPanel.add(allLetter);
    allLetter.addStyleName("letter");

    char[] alphabet = getAlphabet();

    activeLetters = new boolean[alphabet.length];
    letters = new Label[alphabet.length];

    for (char i = 0; i < alphabet.length; i++) {
      Label letter = new Label(alphabet[i] + "");
      letter.addClickListener(createLetterClickListener(i));
      alphabetPanel.add(letter);
      letters[i] = letter;
      activeLetters[i] = false;
      letter.addStyleName("letter-off");
    }

    setAllLetters();

    sizeLabel = new Label(getSizeCountMessage(0));

    alphabetSortedListListeners = new Vector<AlphabetSortedListListener>();

    this.add(header, NORTH);
    header.add(alphabetPanel);
    header.add(sizeLabel);

    this.widgetListPanel = new VerticalPanel();
    this.scroll = new LazyScroll(widgetListPanel, BLOCK_SIZE, MAX_SIZE, new Loader() {

      public void load(final int offset, final int windowOffset, final int limit, final AsyncCallback<Integer> callback) {
        final int newLimit = (limit > itemsCount - offset) ? itemsCount - offset : limit;
        logger.debug("loading to alphabet list [" + offset + ", " + (offset + newLimit - 1) + "]");
        getItems(offset, newLimit, new AsyncCallback<AlphabetListItem[]>() {

          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          public void onSuccess(AlphabetListItem[] items) {
            for (int i = 0; i < items.length; i++) {
              final AlphabetListItem item = items[i];
              Widget w = item.getWidget();
              widgetListPanel.insert(w, offset + i - windowOffset);
              loadedItems.add(item);
              loadedItemsCount++;

            }
            logger.debug("table size: " + widgetListPanel.getWidgetCount());
            callback.onSuccess(new Integer(items.length));
          }

        });

      }

      public void remove(int offset, int widgetOffset, int limit, AsyncCallback<Integer> callback) {
        int last = (itemsCount < offset + limit) ? itemsCount : offset + limit;
        logger.debug("removing from alphabet list [" + offset + ", " + (last - 1) + "]");

        for (int i = last - 1; i >= offset; i--) {
          widgetListPanel.remove(i - widgetOffset);
          loadedItems.remove(i - widgetOffset);
          loadedItemsCount--;

        }
        callback.onSuccess(last - offset);
        logger.debug("table size: " + widgetListPanel.getWidgetCount());
      }

      public void update(int widgetOffset, int count, AsyncCallback<Integer> updatedOffset) {
        updatedOffset.onSuccess(new Integer(widgetOffset + count));

      }

    });
    this.add(scroll, CENTER);
    this.setCellHeight(scroll, "100%");

    this.addStyleName("AlphabetSortedList");
    this.header.addStyleName("alphabet-header");
    this.alphabetPanel.addStyleName("AlphabetPanel");
    this.scroll.addStyleName("ScrollPanel");
    this.widgetListPanel.addStyleName("WidgetSortedListPanel");
    this.sizeLabel.addStyleName("sizeLabel");

    this.selectedItem = null;

  }

  private ClickListener createLetterClickListener(final char letterIndex) {
    ClickListener clickListener = new ClickListener() {

      public void onClick(Widget sender) {
        if (activeLetters[letterIndex]) {
          setLetter(letterIndex);
          if (onLetter != null) {
            onLetter.removeStyleName("letter-on");
          }
          sender.addStyleName("letter-on");
          onLetter = sender;
        }

      }

    };
    return clickListener;
  }

  /**
   * Initialize alphabet sorted list
   * 
   * @param count
   *          the total number of items in alphabet sorted list
   */
  public void init(final int count) {

    getLetterList(new AsyncCallback<Character[]>() {

      public void onFailure(Throwable caught) {
        logger.error("Error getting letter list", caught);
      }

      public void onSuccess(Character[] letterList) {
        for (int i = 0; i < letterList.length; i++) {
          int index = lookupInAlphabet(letterList[i].charValue());

          if (index >= 0 && !activeLetters[index]) {
            letters[index].removeStyleName("letter-off");
            letters[index].addStyleName("letter");
            activeLetters[index] = true;
          }
        }
        update(count);
      }

    });

  }

  /**
   * Get a list with the first letter of all items keywords
   * 
   * @param callback
   *          handle the letter list
   */
  public abstract void getLetterList(AsyncCallback<Character[]> callback);

  /**
   * Update the alphabet list, reloading all items
   * 
   * @param count
   *          the total number of items
   */
  public void update(int count) {
    this.itemsCount = count;
    this.sizeLabel.setText(getSizeCountMessage(itemsCount));
    scroll.fill();
  }

  /**
   * Get items to insert into the alphabet list
   * 
   * @param startItem
   *          the index of the first item of the interval that should be
   *          retrieved from server
   * @param limit
   *          the maximum number of item on the return interval
   * @param callback
   *          handle the returned list of items
   */
  public abstract void getItems(int startItem, int limit, AsyncCallback<AlphabetListItem[]> callback);

  protected String getSizeCountMessage(int i) {
    return "" + i;
  }

  public void clear() {
    scroll.reset(new Command() {

      public void execute() {
        widgetListPanel.clear();
        itemsCount = 0;
        loadedItemsCount = 0;
        loadedItems.clear();
      }

    }, new AsyncCallback<Integer>() {

      public void onFailure(Throwable caught) {
        logger.error("Error reseting scroll", caught);
      }

      public void onSuccess(Integer result) {
        logger.debug("Lazy scroll successfuly reset");
      }

    });

  }

  /**
   * Get currently selected item
   * 
   * @return The selected AlphabetListItem or null of none selected
   */
  public AlphabetListItem getSelectedItem() {
    return selectedItem;
  }

  /**
   * Set the selected item
   * 
   * @param selected
   */
  public void setSelectedItem(AlphabetListItem selected) {
    if (selectedItem != null && selectedItem != selected) {
      selectedItem.setSelected(false);
    }
    selectedItem = selected;
  }

  /**
   * Ensure a widget is visible adjusting the ScrollPanel.
   * 
   * @param index
   *          the index of the widget
   * 
   * @param target
   */
  public void ensureVisible(final int index) {
    if (index < loadedItemsCount) {
      Widget w = (Widget) loadedItems.get(index);
      scroll.ensureVisible(w);
    } else {
      scroll.ensureLoaded(index, BLOCK_SIZE, new AsyncCallback<Integer>() {

        public void onFailure(Throwable caught) {
          logger.error("error ensure scroll loaded", caught);
        }

        public void onSuccess(Integer count) {
          // TODO test this code
          Widget w = widgetListPanel.getWidget(index);
          scroll.ensureVisible(w);
        }

      });

    }

  }

  /**
   * Show only items that match a regular expression
   * 
   * @param regex
   *          The regular expression
   */
  public abstract void setFilter(String regex);

  /**
   * Set the selected letter. Only items which keyword starts with that letter
   * will be presented
   * 
   * @param letterIndex
   *          the letter index
   */
  public void setLetter(int letterIndex) {
    if (activeLetters[letterIndex]) {
      if (onLetter != null) {
        onLetter.removeStyleName("letter-on");
      }
      letters[letterIndex].addStyleName("letter-on");
      onLetter = letters[letterIndex];
    }
  }

  /**
   * Set ALL letters visible. All items will be presented.
   * 
   */
  public void setAllLetters() {
    if (onLetter != null) {
      onLetter.removeStyleName("letter-on");
    }
    allLetter.addStyleName("letter-on");
    onLetter = allLetter;
  }

  protected char[] getAlphabet() {
    char[] alphabet = new char['Z' - 'A' + 1];
    for (char i = 'A'; i <= 'Z'; i++) {
      alphabet[i - 'A'] = i;
    }
    return alphabet;
  }

  private int lookupInAlphabet(char letter) {
    int index = 0;
    boolean found = false;
    char[] alphabet = getAlphabet();
    while (index < alphabet.length && !found) {
      if (alphabet[index] == letter) {
        found = true;
      } else {
        index++;
      }
    }
    if (index == alphabet.length) {
      index = -1;
      logger.error("Letter '" + letter + "' not in alphabet.");
    }
    return index;
  }

  /**
   * Add a new alphabet sorted list listener
   * 
   * @param listener
   */
  public void addAlphabetSortedListListener(AlphabetSortedListListener listener) {
    alphabetSortedListListeners.add(listener);
  }

  /**
   * Remove an alphabet sorted list listener
   * 
   * @param listener
   */
  public void removeAlphabetSortedListListener(AlphabetSortedListListener listener) {
    alphabetSortedListListeners.remove(listener);
  }

  /**
   * Call all listeners to the event that an item has been selected / unselected
   * 
   * @param item
   *          the selected item or null if it was unselected
   */
  protected void onItemSelect(AlphabetListItem item) {
    for (AlphabetSortedListListener listener : alphabetSortedListListeners) {
      listener.onItemSelect(item);
    }
  }

}
