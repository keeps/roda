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
package org.roda.wui.client.browse;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.SortParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.v2.IndexedAIP;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.widgets.LazyScroll;
import org.roda.wui.common.client.widgets.LazyScroll.Loader;
import org.roda.wui.common.client.widgets.ListHeaderPanel;
import org.roda.wui.common.client.widgets.ListHeaderPanel.ListHeaderListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.BrowseConstants;

/**
 * @author Luis Faria
 * 
 */
public abstract class ElementVerticalScrollPanel extends Composite implements ListHeaderListener {

  // data
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private boolean loaded;

  private int count;

  private final ElementsMemCache memProxy;

  private Filter filter;

  private Sorter sorter;

  // interface

  private static BrowseConstants constants = (BrowseConstants) GWT.create(BrowseConstants.class);

  private final DockPanel layout;
  private final ListHeaderPanel header;

  private final LazyScroll elementScroll;

  private final VerticalPanel elementLayout;

  private final int blockSize;

  private final int maxSize;

  private int waitingToLoad;

  /**
   * Element vertical scroll panel
   * 
   * @param filter
   * @param sorter
   * @param memProxy
   * @param blockSize
   * @param maxSize
   */
  public ElementVerticalScrollPanel(Filter filter, Sorter sorter, ElementsMemCache memProxy, int blockSize, int maxSize) {
    this.loaded = false;
    this.filter = filter;
    this.sorter = sorter;
    this.memProxy = memProxy;
    this.blockSize = blockSize;
    this.maxSize = maxSize;
    this.layout = new DockPanel();
    this.header = new ListHeaderPanel(new ListHeaderListener() {

      public void setSorter(Sorter sorter) {
        ElementVerticalScrollPanel.this.setSorter(sorter);
      }

    });

    this.elementLayout = new VerticalPanel();
    this.elementScroll = new LazyScroll(elementLayout, this.blockSize, this.maxSize, new Loader() {

      public void load(final int offset, final int windowOffset, final int limit, final AsyncCallback<Integer> callback) {
        ensureLoaded(new AsyncCallback<Integer>() {

          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          public void onSuccess(Integer count) {
            loadElements(offset, windowOffset, limit, callback);
          }

        });

      }

      public void remove(int offset, int windowOffset, int limit, AsyncCallback<Integer> callback) {
        final int last = (count < (offset + limit)) ? count : offset + limit;
        for (int i = last - 1; i >= offset; i--) {
          removeWidget(elementLayout.getWidget(i - windowOffset));
          elementLayout.remove(i - windowOffset);
        }
        callback.onSuccess(new Integer(last - offset));
      }

      public void update(int widgetOffset, int count, AsyncCallback<Integer> updatedOffset) {
        updatedOffset.onSuccess(new Integer(widgetOffset + count));

      }

    });
    ensureLoaded(new AsyncCallback<Integer>() {

      public void onFailure(Throwable caught) {
        logger.error("Error loading ElementVerticalScroll", caught);
      }

      public void onSuccess(Integer count) {
        initHeader();
      }

    });

    layout.add(header, DockPanel.NORTH);
    layout.add(elementScroll, DockPanel.CENTER);
    initWidget(layout);

    this.waitingToLoad = 0;

    this.setStylePrimaryName("elementVerticalScrollPanel");
    elementLayout.addStyleName("layout");
    elementScroll.addStyleName("scrollAutoLoader");

  }

  private void initHeader() {
    header.addHeader(constants.elementHeaderId(), "element-header-id", new SortParameter[] {
      new SortParameter("id", false), new SortParameter("dateInitial", true)}, true);
    header.addHeader(constants.elementHeaderTitle(), "element-header-title", new SortParameter[] {
      new SortParameter("title", false), new SortParameter("id", false)}, true);
    header.addHeader(constants.elementHeaderDateInitial(), "element-header-date-initial", new SortParameter[] {
      new SortParameter("dateInitial", false), new SortParameter("dateFinal", false)}, false);
    header.addHeader(constants.elementHeaderDateFinal(), "element-header-date-final", new SortParameter[] {
      new SortParameter("dateFinal", false), new SortParameter("dateInitial", false)}, false);

    header.setSelectedHeader(0);
    header.setFillerHeader(0);

  }

  private void ensureLoaded(final AsyncCallback<Integer> callback) {
    if (!loaded) {
      memProxy.ensureLoaded(new AsyncCallback<Integer>() {

        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        public void onSuccess(Integer count) {
          memProxy.getCount(new AsyncCallback<Integer>() {

            public void onFailure(Throwable caught) {
              callback.onFailure(caught);
            }

            public void onSuccess(Integer count) {
              ElementVerticalScrollPanel.this.count = count.intValue();
              callback.onSuccess(count);
            }

          });
        }

      });
    } else {
      callback.onSuccess(count);
    }
  }

  protected void loadElements(final int offset, final int windowOffset, final int limit,
    final AsyncCallback<Integer> callback) {
    if (offset < count) {
      final int last = (count < (offset + limit)) ? count : offset + limit;
      waitingToLoad += last - offset;
      logger.debug("Loading elements [" + offset + ", " + (last - 1) + "], queue length: " + waitingToLoad);

      for (int i = offset; i < last; i++) {
        final int index = i;
        ElementVerticalScrollPanel.this.memProxy.getElement(i, new AsyncCallback<IndexedAIP>() {

          public void onFailure(Throwable caught) {
            logger.error("Error loading ElementVerticalScroll", caught);
            callback.onFailure(caught);
          }

          public void onSuccess(IndexedAIP aip) {
            if (aip == null) {
              logger.error("getElement(" + index + ") returned null");
            } else {
              Widget widget = createWidget(aip, index - windowOffset);
              logger.debug("inserting widget before " + (index - windowOffset));
              elementLayout.insert(widget, index - windowOffset);
              if (--waitingToLoad == 0) {
                // logger.debug("filling scroll...");
                // if (last != count) {
                // scroll.fill();
                // }
                callback.onSuccess(new Integer(last - offset));
              }

            }
          }

        });
      }
    } else {
      callback.onSuccess(new Integer(0));
    }
  }

  protected abstract Widget createWidget(IndexedAIP aip, int position);

  protected abstract void removeWidget(Widget widget);

  protected LazyScroll getScroll() {
    return elementScroll;
  }

  protected int getBlockSize() {
    return blockSize;
  }

  protected VerticalPanel getLayout() {
    return elementLayout;
  }

  protected int getMaxSize() {
    return maxSize;
  }

  /**
   * Refresh all elements. When calling this method the cache is emptied, the
   * layout is cleared and the lazy scroll is reseted, so it loads again from
   * the server
   * 
   * @param callback
   */
  public void clear(final AsyncCallback<Integer> callback) {
    logger.debug("reseting scroll");
    elementScroll.reset(new Command() {

      public void execute() {
        logger.debug("clearing layout");
        elementLayout.clear();
        logger.debug("clearing memory proxy");
        memProxy.clear();
      }

    }, new AsyncCallback<Integer>() {

      public void onFailure(Throwable caught) {
        callback.onFailure(caught);
      }

      public void onSuccess(Integer result) {
        logger.debug("ensuring element vertical panel is reloaded");
        loaded = false;
        ensureLoaded(callback);
      }

    });

  }

  /**
   * Get current filter
   * 
   * @return {@link Filter}
   */
  public Filter getFilter() {
    return filter;
  }

  /**
   * Set filter and clear
   * 
   * @param filter
   */
  public void setFilter(Filter filter) {
    this.filter = filter;
    memProxy.setFilter(filter);
    clear(new AsyncCallback<Integer>() {

      public void onFailure(Throwable caught) {
        logger.error("Error setting filter", caught);

      }

      public void onSuccess(Integer result) {
        // nothing to do

      }

    });
  }

  /**
   * Get current sorter
   * 
   * @return {@link Sorter}
   */
  public Sorter getSorter() {
    return sorter;
  }

  /**
   * Set sorter and clear
   * 
   * @param sorter
   */
  public void setSorter(Sorter sorter) {
    this.sorter = sorter;
    memProxy.setSorter(sorter);
    clear(new AsyncCallback<Integer>() {

      public void onFailure(Throwable caught) {
        logger.error("Error setting sorter", caught);

      }

      public void onSuccess(Integer result) {
        // nothing to do

      }

    });
  }

}
