/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.client.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.CommonConstants;
import config.i18n.client.CommonMessages;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.images.CommonImageBundle;
import pt.gov.dgarq.roda.wui.common.client.widgets.LazyScroll.LazyScrollListener;
import pt.gov.dgarq.roda.wui.common.client.widgets.LazyScroll.Loader;
import pt.gov.dgarq.roda.wui.common.client.widgets.ListHeaderPanel.ListHeaderListener;

/**
 * Vertical List that loads elements as it needs
 * 
 * @author Luis Faria
 * @param <T>
 *          the type of the elements to load
 * 
 */
public class LazyVerticalList<T> {

  protected static final int DEFAULT_SCROLL_BLOCK_SIZE = 30;
  protected static final int DEFAULT_SCROLL_MAX_SIZE = 5000;
  protected static final int DEFAULT_UPDATE_DELAY_MS = 10000;
  protected static final String DEFAULT_SCROLL_HEIGHT = "400px";
  protected static final int DEFAULT_MAX_REPORT_SIZE = 1000;

  private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT.create(CommonImageBundle.class);

  private static CommonConstants constants = (CommonConstants) GWT.create(CommonConstants.class);
  private static CommonMessages messages = (CommonMessages) GWT.create(CommonMessages.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private final DockPanel layout;
  private final VerticalPanel northLayout;
  private final HorizontalPanel listStatusLayout;
  private final Image loadingImage;
  private final Label loadingMessage;
  private final Label totalLabel;
  private int total;
  private final Image printPDF;
  private final Image printCSV;
  private final ListHeaderPanel header;
  private final LazyScroll lazyScroll;
  private final VerticalPanel listLayout;

  private boolean scrollLoading;
  private boolean scrollUpdating;
  private boolean scrollReseting;
  private boolean printing;

  private boolean userMessage;
  private String userMessageText;

  /**
   * Content Source interface, which allows loading of the elements and its
   * mapping to widgets.
   * 
   * @param <T>
   *          the element type
   */
  public interface ContentSource<T> {
    /**
     * Asynchronously get element count
     * 
     * @param filter
     *          the applied content filter
     * @param callback
     */
    public void getCount(Filter filter, AsyncCallback<Integer> callback);

    /**
     * Asynchronously get elements
     * 
     * @param adapter
     *          Content adapter that defined the filter, sub-list and sorting.
     * @param callback
     */
    public void getElements(ContentAdapter adapter, AsyncCallback<T[]> callback);

    /**
     * Get the element panel which defined this element
     * 
     * @param element
     * 
     * @return {@link ElementPanel}
     */
    public ElementPanel<T> getElementPanel(T element);

    /**
     * Get the total element count message to show on top of the lazy vertical
     * list
     * 
     * @param total
     * @return the message with the total
     */
    public String getTotalMessage(int total);

    /**
     * Get the generated report download id
     * 
     * @param adapter
     * @param locale
     * @param callback
     */
    public void setReportInfo(ContentAdapter adapter, String locale, AsyncCallback<Void> callback);
  }

  /**
   * Interface to listen to events
   * 
   * @param <T>
   *          the element type
   */
  public interface LazyVerticalListListener<T> {
    /**
     * On element selected
     * 
     * @param elementPanel
     */
    public void onElementSelected(ElementPanel<T> elementPanel);

    /**
     * On update begin, please use this to lock user options that could trigger
     * another update of the lazy vertical list.
     */
    public void onUpdateBegin();

    /**
     * On update finish, to unlock user options
     */
    public void onUpdateFinish();
  }

  private final ContentSource<T> contentSource;

  private ContentAdapter contentAdapter;

  private final int scrollBlockSize;
  private final int scrollMaxSize;
  private int updateDelayMs;
  private final int maxReportSize;
  private boolean autoUpdate;

  private ElementPanel<T> selectedElementPanel = null;

  private final Timer updateTimer;

  private final List<LazyVerticalListListener<T>> listeners;

  /**
   * Create a new lazy vertical list
   * 
   * @param contentSource
   *          the content source
   * @param autoUpdate
   *          if the list should automatically update
   * @param initialFilter
   */
  public LazyVerticalList(ContentSource<T> contentSource, boolean autoUpdate, Filter initialFilter) {
    this(contentSource, DEFAULT_SCROLL_BLOCK_SIZE, DEFAULT_SCROLL_MAX_SIZE, DEFAULT_UPDATE_DELAY_MS,
      DEFAULT_MAX_REPORT_SIZE, initialFilter);
    setAutoUpdate(autoUpdate);
  }

  /**
   * Create a new lazy vertical list
   * 
   * @param contentSource
   *          the content source
   * @param autoUpdateDelayMs
   *          delay of the auto update in milliseconds
   * @param initialFilter
   */
  public LazyVerticalList(ContentSource<T> contentSource, int autoUpdateDelayMs, Filter initialFilter) {
    this(contentSource, DEFAULT_SCROLL_BLOCK_SIZE, DEFAULT_SCROLL_MAX_SIZE, autoUpdateDelayMs, DEFAULT_MAX_REPORT_SIZE,
      initialFilter);
  }

  /**
   * Create a new lazy vertical list
   * 
   * @param contentSource
   *          the content source
   * @param scrollBlockSize
   *          the scroll block fetch size
   * @param scrollMaxSize
   *          the scroll max size
   * @param updateDelayMs
   *          the update delay in milliseconds
   * @param maxReportSize
   *          the maximum number of elements to print in a report
   * @param initialFilter
   */
  public LazyVerticalList(ContentSource<T> contentSource, int scrollBlockSize, int scrollMaxSize, int updateDelayMs,
    int maxReportSize, Filter initialFilter) {
    this.contentSource = contentSource;
    this.scrollBlockSize = scrollBlockSize;
    this.scrollMaxSize = scrollMaxSize;
    this.updateDelayMs = updateDelayMs;
    this.maxReportSize = maxReportSize;
    autoUpdate = updateDelayMs > 0;
    contentAdapter = new ContentAdapter();
    contentAdapter.setFilter(initialFilter);
    contentAdapter.setSorter(new Sorter());

    scrollLoading = false;
    scrollUpdating = false;
    scrollReseting = false;
    printing = false;

    userMessage = false;

    layout = new DockPanel();
    northLayout = new VerticalPanel();
    listStatusLayout = new HorizontalPanel();
    totalLabel = new Label();
    printPDF = commonImageBundle.printPDF().createImage();
    printCSV = commonImageBundle.printCSV().createImage();
    loadingImage = new Image(GWT.getModuleBaseURL() + "images/loadingSmall.gif");
    loadingMessage = new Label();
    header = new ListHeaderPanel(new ListHeaderListener() {

      public void setSorter(Sorter sorter) {
        contentAdapter.setSorter(sorter);
        reset();
      }

    });
    listLayout = new VerticalPanel();
    lazyScroll = new LazyScroll(listLayout, this.scrollBlockSize, this.scrollMaxSize, new Loader() {

      public void load(final int offset, final int widgetOffset, int limit, final AsyncCallback<Integer> loaded) {
        contentAdapter.setSublist(new Sublist(offset, limit));
        LazyVerticalList.this.contentSource.getElements(contentAdapter, new AsyncCallback<T[]>() {

          public void onFailure(Throwable caught) {

          }

          public void onSuccess(T[] elements) {
            createWidgets(elements, offset, widgetOffset);
            loaded.onSuccess(elements.length);
          }

        });

      }

      public void remove(int offset, int widgetOffset, int limit, AsyncCallback<Integer> removed) {
        int last = offset - widgetOffset + limit;
        last = last < listLayout.getWidgetCount() ? last : listLayout.getWidgetCount();
        for (int i = offset - widgetOffset; i < last; i++) {
          listLayout.remove(i);
        }
        removed.onSuccess(last - offset + widgetOffset);

      }

      public void update(final int widgetOffset, int count, final AsyncCallback<Integer> updatedOffset) {
        contentAdapter.setSublist(new Sublist(widgetOffset, count));
        LazyVerticalList.this.contentSource.getElements(contentAdapter, new AsyncCallback<T[]>() {

          public void onFailure(Throwable caught) {
            updatedOffset.onFailure(caught);
          }

          public void onSuccess(T[] elements) {
            updateLayout(elements);
            updatedOffset.onSuccess(widgetOffset + elements.length);
          }

        });
      }

    });

    lazyScroll.addLazyScrollListener(new LazyScrollListener() {

      public void onLoadStart() {
        scrollLoading = true;
        updateLoadingMessage();
        updateTimer.cancel();
      }

      public void onLoadEnd() {
        scrollLoading = false;
        updateLoadingMessage();
        if (LazyVerticalList.this.updateDelayMs > 0 && autoUpdate) {
          updateTimer.schedule(LazyVerticalList.this.updateDelayMs);
        }
      }

      public void onUpdateStart() {
        scrollUpdating = true;
        updateLoadingMessage();

      }

      public void onUpdateEnd() {
        scrollUpdating = false;
        updateLoadingMessage();

      }

      public void onResetStart() {
        scrollReseting = true;
        updateLoadingMessage();
      }

      public void onResetEnd() {
        scrollReseting = false;
        updateLoadingMessage();
      }

    });

    printPDF.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        ContentAdapter printAdapter = new ContentAdapter(contentAdapter);
        boolean confirm = true;
        if (total > LazyVerticalList.this.maxReportSize) {
          confirm = Window.confirm(messages.reportPrintMaxSize(LazyVerticalList.this.maxReportSize, total));
          printAdapter.setSublist(new Sublist(0, LazyVerticalList.this.maxReportSize));
        } else {
          printAdapter.setSublist(new Sublist(0, total));
        }

        if (confirm) {
          printing = true;
          updateLoadingMessage();
          LazyVerticalList.this.contentSource.setReportInfo(printAdapter, constants.locale(),
            new AsyncCallback<Void>() {

              public void onFailure(Throwable caught) {
                logger.error("Error getting print id", caught);
              }

              public void onSuccess(Void printId) {
                printing = false;
                updateLoadingMessage();
              }

            });
          Window.open(GWT.getModuleBaseURL() + "ReportDownload?type=CONTENT_ADAPTER&locale=" + constants.locale(),
            "_blank", "");
        }

      }

    });

    printPDF.setTitle(constants.lazyListPrintPDF());

    printCSV.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        ContentAdapter printAdapter = new ContentAdapter(contentAdapter);
        boolean confirm = true;
        if (total > LazyVerticalList.this.maxReportSize) {
          confirm = Window.confirm(messages.reportPrintMaxSize(LazyVerticalList.this.maxReportSize, total));
          printAdapter.setSublist(new Sublist(0, LazyVerticalList.this.maxReportSize));
        } else {
          printAdapter.setSublist(new Sublist(0, total));
        }

        if (confirm) {
          printing = true;
          updateLoadingMessage();
          LazyVerticalList.this.contentSource.setReportInfo(printAdapter, constants.locale(),
            new AsyncCallback<Void>() {

              public void onFailure(Throwable caught) {
                logger.error("Error getting print id", caught);
              }

              public void onSuccess(Void printId) {
                printing = false;
                updateLoadingMessage();
              }

            });
          Window.open(GWT.getModuleBaseURL() + "ReportDownload?type=CONTENT_ADAPTER&locale=" + constants.locale()
            + "&output=CSV", "_blank", "");
        }

      }

    });

    printCSV.setTitle(constants.lazyListPrintCSV());

    loadingImage.setVisible(false);
    loadingMessage.setVisible(false);

    listStatusLayout.add(loadingImage);
    listStatusLayout.add(loadingMessage);
    listStatusLayout.add(totalLabel);
    listStatusLayout.add(printPDF);
    listStatusLayout.add(printCSV);

    northLayout.add(listStatusLayout);
    northLayout.add(header);

    layout.add(northLayout, DockPanel.NORTH);
    layout.add(lazyScroll, DockPanel.CENTER);

    setScrollHeight(DEFAULT_SCROLL_HEIGHT);

    updateTimer = new Timer() {

      @Override
      public void run() {
        update();
      }

    };

    listeners = new ArrayList<LazyVerticalListListener<T>>();

    updateTotal();

    if (updateDelayMs > 0 && autoUpdate) {
      updateTimer.schedule(this.updateDelayMs);
    }

    listStatusLayout.setCellWidth(totalLabel, "100%");

    layout.addStyleName("wui-lazy-list-vertical");
    northLayout.addStyleName("lazy-list-vertical-north");
    listStatusLayout.addStyleName("lazy-list-status-layout");
    loadingImage.addStyleName("lazy-list-loading-image");
    loadingMessage.addStyleName("lazy-list-loading-message");
    totalLabel.addStyleName("lazy-list-vertical-total");
    printPDF.addStyleName("lazy-list-vertical-print");
    printCSV.addStyleName("lazy-list-vertical-print");
    header.addStyleName("lazy-list-vertical-headers");
    lazyScroll.addStyleName("lazy-list-vertical-scroll");
    listLayout.addStyleName("lazy-list-vertical-scroll-layout");

  }

  /**
   * Add a user message to appear in loading bar, when no other loading messages
   * are visible. To remove user message use
   * {@link LazyVerticalList#removeUserMessage()}
   * 
   * @param message
   */
  public void setUserMessage(String message) {
    userMessage = true;
    userMessageText = message;
    updateLoadingMessage();
  }

  /**
   * Remove any user message added via
   * {@link LazyVerticalList#setUserMessage(String)}
   */
  public void removeUserMessage() {
    userMessage = false;
    updateLoadingMessage();
  }

  protected void updateLoadingMessage() {
    if (printing) {
      loadingMessage.setText(constants.lazyListPrinting());
    } else if (scrollReseting) {
      loadingMessage.setText(constants.lazyListReseting());
    } else if (scrollUpdating) {
      loadingMessage.setText(constants.lazyListUpdating());
    } else if (scrollLoading) {
      loadingMessage.setText(constants.lazyListLoading());
    } else if (userMessage) {
      loadingMessage.setText(userMessageText);
    }

    loadingImage.setVisible(printing || scrollReseting || scrollUpdating || scrollLoading || userMessage);
    loadingMessage.setVisible(printing || scrollReseting || scrollUpdating || scrollLoading || userMessage);

  }

  protected void createWidgets(T[] elements, int offset, int widgetOffset) {
    for (int i = 0; i < elements.length; i++) {
      listLayout.insert(createWidget(elements[i]), offset - widgetOffset + i);
    }
  }

  private Widget createWidget(final T element) {
    final ElementPanel<T> elementPanel = contentSource.getElementPanel(element);
    elementPanel.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        if (selectedElementPanel != null && selectedElementPanel != elementPanel) {
          selectedElementPanel.setSelected(false);
        }
        selectedElementPanel = elementPanel;
        selectedElementPanel.setSelected(true);
        onElementSelected(elementPanel);

      }

    });
    return elementPanel;
  }

  @SuppressWarnings("unchecked")
  protected void updateLayout(T[] elements) {
    onUpdateBegin();

    int elementIndex = elements.length - 1;
    int elementPanelIndex = listLayout.getWidgetCount() - 1;

    Set<T> remainingElements = new HashSet<T>(Arrays.asList(elements));

    while (elementIndex >= 0 || elementPanelIndex >= 0) {
      if (elementIndex < 0) {
        while (elementPanelIndex >= 0) {
          listLayout.remove(elementPanelIndex--);
        }
      } else if (elementPanelIndex < 0) {
        while (elementIndex >= 0) {
          ElementPanel<T> elementPanel = contentSource.getElementPanel(elements[elementIndex--]);
          listLayout.add(elementPanel);
        }
      } else {
        T updatedElement = elements[elementIndex];
        ElementPanel<T> elementPanel = (ElementPanel<T>) listLayout.getWidget(elementPanelIndex);
        T localElement = elementPanel.get();
        if (updatedElement.equals(localElement)) {
          elementPanel.set(updatedElement);
          elementIndex--;
          elementPanelIndex--;
          remainingElements.remove(updatedElement);
        } else if (remainingElements.contains(localElement)) {
          listLayout.insert(createWidget(updatedElement), elementPanelIndex + 1);
          elementIndex--;
          remainingElements.remove(updatedElement);
        } else {
          listLayout.remove(elementPanelIndex);
          elementPanelIndex--;
        }
      }

    }
    onUpdateFinish();
  }

  /**
   * Get currently defined filter
   */
  public void getFilter() {
    contentAdapter.getFilter();
  }

  /**
   * Set currently defined filter
   * 
   * @param filter
   */
  public void setFilter(Filter filter) {
    contentAdapter.setFilter(filter);
  }

  /**
   * Update total element count
   */
  protected void updateTotal() {
    contentSource.getCount(contentAdapter.getFilter(), new AsyncCallback<Integer>() {

      public void onFailure(Throwable caught) {
        logger.error("Error updating total", caught);
      }

      public void onSuccess(Integer taskCount) {
        total = taskCount;
        totalLabel.setText(contentSource.getTotalMessage(taskCount));
      }

    });
  }

  /**
   * Update lazy vertical list
   */
  public void update() {
    if (layout.isAttached()) {
      updateTimer.cancel();
      updateTotal();
      lazyScroll.update(new AsyncCallback<Integer>() {
        public void onFailure(Throwable caught) {
          logger.error("Error updating SIP lazy scroll", caught);
        }

        public void onSuccess(Integer result) {
          if (autoUpdate) {
            updateTimer.schedule(updateDelayMs);
          }
        }
      });
    }
  }

  /**
   * Reset lazy vertical list
   */
  public void reset() {
    selectedElementPanel = null;
    onElementSelected(null);
    onUpdateBegin();
    updateTimer.cancel();
    updateTotal();
    lazyScroll.reset(new Command() {

      public void execute() {
        listLayout.clear();
      }

    }, new AsyncCallback<Integer>() {

      public void onFailure(Throwable caught) {
        onUpdateFinish();
        logger.error("Error reseting lazy vertical list", caught);
      }

      public void onSuccess(Integer result) {
        onUpdateFinish();
        if (autoUpdate) {
          updateTimer.schedule(updateDelayMs);
        }
      }

    });

  }

  /**
   * Get current selected element panel
   * 
   * @return {@link ElementPanel}
   */
  public ElementPanel<T> getSelected() {
    return selectedElementPanel;
  }

  /**
   * Add a new lazy vertical list listener
   * 
   * @param listener
   */
  public void addLazyVerticalListListener(LazyVerticalListListener<T> listener) {
    listeners.add(listener);
  }

  /**
   * Remove a lazy vertical list listener
   * 
   * @param listener
   */
  public void removeLazyVerticalListListener(LazyVerticalListListener<T> listener) {
    listeners.remove(listener);
  }

  protected void onElementSelected(ElementPanel<T> elementPanel) {
    for (LazyVerticalListListener<T> listener : listeners) {
      listener.onElementSelected(elementPanel);
    }
  }

  protected void onUpdateBegin() {
    for (LazyVerticalListListener<T> listener : listeners) {
      listener.onUpdateBegin();
    }
  }

  protected void onUpdateFinish() {
    for (LazyVerticalListListener<T> listener : listeners) {
      listener.onUpdateFinish();
    }
  }

  /**
   * Get update delay in milliseconds
   * 
   * @return the update delay
   */
  public int getUpdateDelayMs() {
    return updateDelayMs;
  }

  /**
   * Set update delay in milliseconds
   * 
   * @param updateDelayMs
   */
  public void setUpdateDelayMs(int updateDelayMs) {
    if (this.updateDelayMs != updateDelayMs) {
      if (this.updateDelayMs > 0) {
        updateTimer.cancel();
      }
      this.updateDelayMs = updateDelayMs;
      if (this.updateDelayMs > 0) {
        updateTimer.schedule(this.updateDelayMs);
      }
    }

  }

  /**
   * 
   * @param autoUpdate
   */
  public void setAutoUpdate(boolean autoUpdate) {
    if (this.autoUpdate != autoUpdate) {
      this.autoUpdate = autoUpdate;
      updateTimer.cancel();
      if (autoUpdate) {
        updateTimer.schedule(updateDelayMs);
      }
    }
  }

  /**
   * Set scroll height
   * 
   * @param height
   */
  public void setScrollHeight(String height) {
    layout.setCellHeight(lazyScroll, height);
  }

  /**
   * Get widget
   * 
   * @return the widget
   */
  public Widget getWidget() {
    return layout;
  }

  /**
   * Get list header panel
   * 
   * @return {@link ListHeaderPanel}
   */
  public ListHeaderPanel getHeader() {
    return header;
  }

}
