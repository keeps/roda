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
package org.roda.wui.common.client.widgets;

import java.util.ArrayList;
import java.util.List;

import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author Luis Faria
 * 
 */
public class LazyScroll extends ScrollPanel {

  /**
   * Distance, in pixels, from the end or the beginning that the scroll should
   * be to activate loading events
   */
  private static final int SCROLL_THRESHOLD = 50;

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private boolean refreshing = false;
  private final Widget element;
  private final Loader loader;
  private final int blockSize;
  private final int maxSize;
  private int offset;
  private int windowOffset;
  private boolean initialized;
  private boolean working;
  private final List<LoadListener> loadListeners;
  private final List<LazyScrollListener> lazyScrollListeners;

  /**
   * Loader interface. Implementations should add content to the inner panel
   * when load is called.
   * 
   * @author Luis Faria
   */
  public interface Loader {
    /**
     * Load items to inner panel.
     * 
     * @param offset
     *          The starting item index to load
     * @param widgetOffset
     *          The offset of the loaded widget list
     * @param limit
     *          The maximum number of items to load
     * @param loaded
     *          The number of items really loaded
     */
    public void load(int offset, int widgetOffset, int limit, AsyncCallback<Integer> loaded);

    /**
     * Remove items from inner panel.
     * 
     * @param offset
     *          The starting item index to remove
     * @param widgetOffset
     *          The offset of the loaded widget list
     * @param limit
     *          The maximum number of items to remove
     * @param removed
     *          The number of items really removed
     */
    public void remove(int offset, int widgetOffset, int limit, AsyncCallback<Integer> removed);

    /**
     * Update the current load items in scroll
     * 
     * @param widgetOffset
     *          the offset of item displayed in layout widget
     * @param count
     *          the total number of items displayed
     * @param updatedOffset
     *          handler to update the offset
     */
    public void update(int widgetOffset, int count, AsyncCallback<Integer> updatedOffset);
  }

  @FunctionalInterface
  private interface LoadListener {
    /**
     * add a command to call when worker is idle
     * 
     * @param done
     *          command to call
     */
    public void onIdle(Command done);

  }

  /**
   * Lazy scroll event listener
   */
  public interface LazyScrollListener {
    /**
     * Called when loading of information starts
     */
    public void onLoadStart();

    /**
     * Called when loading of information ends
     */
    public void onLoadEnd();

    /**
     * Called when update starts
     */
    public void onUpdateStart();

    /**
     * Called when update ends
     */
    public void onUpdateEnd();

    /**
     * Called when reset starts
     */
    public void onResetStart();

    /**
     * Called when reset ends
     */
    public void onResetEnd();
  }

  /**
   * Create a new scroll panel.
   * 
   * @param element
   *          The inner panel. Add loaded content to this panel.
   * @param blockSize
   * @param maxSize
   * @param loader
   *          The loader which is responsible for adding content to the inner
   *          panel.
   * @param limit
   *          Number of items to retrieve with each load.
   */
  public LazyScroll(final Widget element, int blockSize, int maxSize, Loader loader) {
    super(element);
    this.element = element;
    this.loader = loader;
    this.blockSize = blockSize;
    this.maxSize = maxSize;
    this.loadListeners = new ArrayList<>();
    this.lazyScrollListeners = new ArrayList<>();
    offset = 0;
    windowOffset = 0;

    sinkEvents(Event.ONSCROLL);

    setWidth("100%");
    initialized = false;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    init();
  }

  /**
   * Start listening for browse events and do first fill.
   * 
   */
  protected void init() {
    initialized = true;
    fill();
  }

  /**
   * Fill the scroll
   */
  public void fill() {
    fill(new AsyncCallback<Integer>() {
      @Override
      public void onFailure(Throwable caught) {
        // nothing to do
      }

      @Override
      public void onSuccess(Integer result) {
        // nothing to do
      }
    });
  }

  /**
   * Fill initial content into the scroll panel. Call this on startup (after the
   * widget has been added to the DOM). Also call this method as the last item
   * in {@link Loader#load(int, int)} to keep filling until the scrollbar
   * appears.
   * 
   * @param callback
   *          Handle the finish of the fill, returning how many items where
   *          loaded
   */
  public void fill(AsyncCallback<Integer> callback) {
    if (this.isAttached() && this.isVisible() && element.isVisible()
      && element.getOffsetHeight() <= getOffsetHeight()) {
      refreshing = true;
      load(blockSize, callback);
    } else {
      callback.onSuccess(0);
    }

  }

  /**
   * Set lazy scroll as not initialized and reset all variables. Clear the inner
   * panel on the callback
   * 
   * @param cleanInnerPanel
   * @param callback
   */
  public void reset(final Command cleanInnerPanel, final AsyncCallback<Integer> callback) {
    addLoadListener(new LoadListener() {

      @Override
      public void onIdle(Command done) {
        onResetStart();
        offset = 0;
        windowOffset = 0;
        cleanInnerPanel.execute();
        fill(callback);
        done.execute();
        onResetEnd();
      }

    });

  }

  /**
   * Ensure an item is loaded
   * 
   * @param index
   *          the item index
   * @param limit
   * @param callback
   */
  public void ensureLoaded(final int index, final AsyncCallback<Integer> callback) {
    addLoadListener(new LoadListener() {

      @Override
      public void onIdle(final Command done) {
        if (index < windowOffset) {
          logger.debug("ensure loading of " + index + " requires moving window to " + index);
          refreshing = true;
          moveWindow(index, new AsyncCallback<Integer>() {

            @Override
            public void onFailure(Throwable caught) {
              refreshing = false;
              callback.onFailure(caught);
              done.execute();
            }

            @Override
            public void onSuccess(Integer result) {
              refreshing = false;
              callback.onSuccess(result);
              done.execute();
            }

          });

        } else if (index >= offset) {
          int newLimit = index - offset + 1;
          logger.debug("ensuring " + index + " is loaded, with offset=" + offset + " requires loading more" + newLimit);
          load(newLimit, callback);
          done.execute();
        } else {
          logger.debug(index + " is loaded because offset=" + offset);
          callback.onSuccess(0);
          done.execute();
        }
      }
    });

  }

  /**
   * Method using workaround for popup panels
   * 
   * @param object
   */
  public void ensureVisibleWorkarround(UIObject object) {
    ensureVisible(this.getElement(), object.getElement());
  }

  /**
   * Workaround ScrollPanel's ensureVisible method bug Ensures element e is
   * visible on ScrollPanel scroll
   * 
   * @param scroll
   * @param e
   */
  private native void ensureVisible(Element scroll, Element e)/*-{
		if (!e)
			return;

		var item = e;
		var realOffset = 0;
		while (item && (item != scroll && item != scroll.offsetParent)) {
			realOffset += item.offsetTop;
			item = item.offsetParent;
		}

		scroll.scrollTop = realOffset - scroll.offsetTop;
  }-*/;

  @Override
  public void onBrowserEvent(Event e) {
    if (DOM.eventGetType(e) == Event.ONSCROLL) {
      if (initialized && !refreshing
        && element.getOffsetHeight() - getScrollPosition() - SCROLL_THRESHOLD < getOffsetHeight()) {
        load(blockSize, null);

      } else if (initialized && !refreshing && getScrollPosition() < SCROLL_THRESHOLD && windowOffset > 0) {

        addLoadListener(new LoadListener() {

          @Override
          public void onIdle(final Command done) {
            refreshing = true;

            moveWindow(windowOffset - blockSize, new AsyncCallback<Integer>() {

              @Override
              public void onFailure(Throwable caught) {
                logger.error("error moving window", caught);
                refreshing = false;
                done.execute();
              }

              @Override
              public void onSuccess(Integer result) {
                refreshing = false;
                done.execute();
              }

            });
          }

        });

      }
    } else {
      super.onBrowserEvent(e);
    }
  }

  /**
   * Get current offset
   * 
   * @return
   */
  public int getOffset() {
    return offset;
  }

  /**
   * Set current offset
   * 
   * @param offset
   */
  protected void setOffset(int offset) {
    this.offset = offset;
  }

  private void load(final int howMany, AsyncCallback<Integer> done) {
    final AsyncCallback<Integer> callback = (done != null) ? done : new AsyncCallback<Integer>() {

      @Override
      public void onFailure(Throwable caught) {
        // do nothing
      }

      @Override
      public void onSuccess(Integer result) {
        // do nothing
      }

    };
    refreshing = true;
    addLoadListener(new LoadListener() {

      @Override
      public void onIdle(final Command done) {
        onLoadStart();
        loader.load(offset, windowOffset, howMany, new AsyncCallback<Integer>() {

          @Override
          public void onFailure(Throwable caught) {
            refreshing = false;
            callback.onFailure(caught);
            done.execute();
          }

          @Override
          public void onSuccess(Integer result) {
            int loaded = result.intValue();
            offset += loaded;
            if (offset + howMany > windowOffset + maxSize) {
              int newWindowOffset = offset + howMany - maxSize;
              moveWindow(newWindowOffset, new AsyncCallback<Integer>() {

                @Override
                public void onFailure(Throwable caught) {
                  logger.error("Error moving window", caught);
                  callback.onFailure(caught);
                  done.execute();

                }

                @Override
                public void onSuccess(Integer result) {
                  refreshing = false;
                  callback.onSuccess(result);
                  done.execute();
                  onLoadEnd();
                }

              });
            } else {
              refreshing = false;
              callback.onSuccess(0);
              done.execute();
              onLoadEnd();
            }
            if (loaded > 0) {
              addLoadListener(new LoadListener() {

                @Override
                public void onIdle(Command done) {
                  fill();
                  done.execute();
                  onLoadEnd();
                }

              });
            }
          }
        });
      }

    });

  }

  /**
   * Move the window offset, this method works on the assumption that the window
   * is already full. It removes the initial excess if window max size was
   * exceeded or adds to the beginning and removes in the end if we lowering the
   * window offset.
   * 
   * @param offset
   *          the new window offset
   */
  private void moveWindow(final int offset, final AsyncCallback<Integer> callback) {
    final int initialHeight = element.getOffsetHeight();
    if (offset > windowOffset) {
      loader.remove(windowOffset, windowOffset, offset - windowOffset, new AsyncCallback<Integer>() {

        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(Integer loaded) {
          windowOffset -= loaded;
          int finalHeight = element.getOffsetHeight();
          int newPosition = getScrollPosition() - initialHeight + finalHeight;
          setScrollPosition(newPosition);
          logger.debug("Moving scroll " + (finalHeight - initialHeight));
          callback.onSuccess(null);
        }

      });

    } else if (offset < windowOffset) {
      int windowBlockSize = windowOffset - offset;
      final int maxOffset = offset + maxSize;
      logger.debug("Move window [" + windowOffset + ", " + this.offset + "] > [" + offset + ", " + maxOffset + "]");
      windowOffset = offset;
      loader.load(offset, 0, windowBlockSize, new AsyncCallback<Integer>() {

        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(Integer loaded) {
          if (LazyScroll.this.offset > maxOffset) {
            loader.remove(maxOffset, windowOffset, LazyScroll.this.offset - maxOffset, new AsyncCallback<Integer>() {

              @Override
              public void onFailure(Throwable caught) {
                callback.onFailure(caught);
              }

              @Override
              public void onSuccess(Integer removed) {
                LazyScroll.this.offset -= removed;
                logger.debug("scroll offset=" + offset);
                int finalHeight = element.getOffsetHeight();
                int newPosition = getScrollPosition() - initialHeight + finalHeight;
                setScrollPosition(newPosition);
                logger.debug("Moving scroll " + (finalHeight - initialHeight));
                callback.onSuccess(null);

              }

            });
          }
        }

      });

    } else {
      callback.onSuccess(0);
    }

  }

  /**
   * Get current window offset
   * 
   * @return
   */
  public int getWindowOffset() {
    return windowOffset;
  }

  protected void setWindowOffset(int offset) {
    windowOffset = offset;
  }

  private void work() {

    if (!working && !loadListeners.isEmpty()) {
      working = true;
      LoadListener listener = loadListeners.remove(0);
      listener.onIdle(new Command() {
        @Override
        public void execute() {
          working = false;
          work();
        }
      });
    }
  }

  private void addLoadListener(LoadListener listener) {
    loadListeners.add(listener);
    work();
  }

  /**
   * Update scroll, reloading all data
   * 
   * @param callback
   */
  public void update(final AsyncCallback<Integer> callback) {
    addLoadListener(new LoadListener() {

      @Override
      public void onIdle(final Command done) {
        onUpdateStart();
        refreshing = true;
        loader.update(windowOffset, offset - windowOffset, new AsyncCallback<Integer>() {

          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
            done.execute();

          }

          @Override
          public void onSuccess(Integer loaded) {
            offset = loaded;
            callback.onSuccess(loaded);
            done.execute();
            fill();
            onUpdateEnd();
          }

        });
        refreshing = false;
      }

    });
  }

  /**
   * Add a lazy scroll listener
   * 
   * @param listener
   */
  public void addLazyScrollListener(LazyScrollListener listener) {
    lazyScrollListeners.add(listener);
  }

  /**
   * Remove a lazy scroll listener
   * 
   * @param listener
   */
  public void removeLazyScrollListener(LazyScrollListener listener) {
    lazyScrollListeners.remove(listener);
  }

  protected void onLoadStart() {
    for (LazyScrollListener listener : lazyScrollListeners) {
      listener.onLoadStart();
    }
  }

  protected void onLoadEnd() {
    for (LazyScrollListener listener : lazyScrollListeners) {
      listener.onLoadEnd();
    }
  }

  protected void onUpdateStart() {
    for (LazyScrollListener listener : lazyScrollListeners) {
      listener.onUpdateStart();
    }
  }

  protected void onUpdateEnd() {
    for (LazyScrollListener listener : lazyScrollListeners) {
      listener.onUpdateEnd();
    }
  }

  protected void onResetStart() {
    for (LazyScrollListener listener : lazyScrollListeners) {
      listener.onResetStart();
    }
  }

  protected void onResetEnd() {
    for (LazyScrollListener listener : lazyScrollListeners) {
      listener.onResetEnd();
    }
  }

}
